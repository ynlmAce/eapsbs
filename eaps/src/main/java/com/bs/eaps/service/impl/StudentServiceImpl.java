package com.bs.eaps.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bs.eaps.common.BusinessException;
import com.bs.eaps.common.Constants;
import com.bs.eaps.entity.StudentProfile;
import com.bs.eaps.entity.StudentResumeFile;
import com.bs.eaps.entity.StudentStructuredResume;
import com.bs.eaps.mapper.SkillTagMapper;
import com.bs.eaps.mapper.StudentProfileMapper;
import com.bs.eaps.mapper.StudentResumeFileMapper;
import com.bs.eaps.mapper.StudentSkillMapper;
import com.bs.eaps.mapper.StudentStructuredResumeMapper;
import com.bs.eaps.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 学生服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentProfileMapper studentProfileMapper;
    private final StudentResumeFileMapper studentResumeFileMapper;
    private final StudentStructuredResumeMapper studentStructuredResumeMapper;
    private final SkillTagMapper skillTagMapper;
    private final StudentSkillMapper studentSkillMapper;

    @Value("${file.upload.path:/tmp/uploads}")
    private String fileUploadPath;

    @Value("${file.upload.use.absolute.path:false}")
    private boolean useAbsolutePath;

    @Value("${file.base.dir:}")
    private String baseDir;

    private static final List<String> ALLOWED_RESUME_EXTENSIONS = Arrays.asList("pdf", "docx");

    @Override
    public StudentProfile getStudentProfile(Long userId) {
        if (userId == null) {
            log.error("获取学生档案失败: 用户ID为空");
            throw new BusinessException("学生ID不能为空");
        }

        log.info("查询学生档案, userId={}", userId);

        try {
            // 先通过userId查找学生档案
            LambdaQueryWrapper<StudentProfile> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(StudentProfile::getUserId, userId);
            StudentProfile profile = studentProfileMapper.selectOne(queryWrapper);

            // 如果没找到，记录日志并抛出异常
            if (profile == null) {
                log.error("找不到对应的学生档案, userId={}", userId);
                throw new BusinessException("找不到对应的学生档案");
            }

            log.info("成功查询到学生档案信息: id={}, name={}, userId={}",
                    profile.getId(), profile.getName(), profile.getUserId());

            return profile;
        } catch (BusinessException e) {
            // 业务异常直接抛出
            throw e;
        } catch (Exception e) {
            // 其他异常包装成业务异常
            log.error("查询学生档案时发生异常", e);
            throw new BusinessException("获取学生档案时发生系统错误: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean updateStudentProfile(StudentProfile profile) {
        if (profile == null || profile.getId() == null) {
            throw new BusinessException("学生信息不完整");
        }

        // 获取当前学生的完整信息，以保留基础信息
        StudentProfile existingProfile = studentProfileMapper.selectById(profile.getId());
        if (existingProfile == null) {
            throw new BusinessException("找不到对应的学生信息");
        }

        // 只更新允许修改的字段，保留基础信息字段
        existingProfile.setSelfIntroduction(profile.getSelfIntroduction());
        existingProfile.setJobPreferences(profile.getJobPreferences());

        // 校验可更新的学生资料（不再校验基础信息）
        validateUpdatableStudentProfile(existingProfile);

        // 更新学生信息
        int result = studentProfileMapper.updateById(existingProfile);

        // 可以在这里更新学生的教育经历、项目经历、技能标签等关联信息
        // 这部分需要根据实际的数据模型和业务需求来完善

        return result > 0;
    }

    /**
     * 校验可更新的学生资料
     *
     * @param profile 学生资料
     */
    private void validateUpdatableStudentProfile(StudentProfile profile) {
        // 只校验可以更新的字段
        // 不再校验基础信息字段，因为它们是只读的

        // 可以添加自我介绍、求职意向等其他可更新字段的校验逻辑
    }

    @Override
    public StudentResumeFile uploadStudentResume(Long studentId, MultipartFile file) {
        if (studentId == null) {
            throw new BusinessException("学生ID不能为空");
        }

        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_RESUME_EXTENSIONS.contains(extension)) {
            throw new BusinessException("不支持的文件类型，只允许上传PDF或DOCX格式的文件");
        }

        // 验证文件大小（最大5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException("文件大小不能超过5MB");
        }

        try {
            // 检查学生是否存在 - 使用getStudentById方法代替直接查询
            StudentProfile profile = getStudentById(studentId);
            log.info("找到学生信息: id={}, name={}, userId={}", profile.getId(), profile.getName(), profile.getUserId());

            String uploadDir;
            String relativePath = "uploads/resumes/" + profile.getId();

            if (useAbsolutePath) {
                // 使用绝对路径创建上传目录
                File uploadBaseDir = new File(fileUploadPath);
                if (!uploadBaseDir.exists()) {
                    if (!uploadBaseDir.mkdirs()) {
                        log.error("创建基础上传目录失败: {}", fileUploadPath);
                        throw new BusinessException("创建上传目录失败");
                    }
                }

                // 构建完整的目录路径
                File studentDir = new File(uploadBaseDir, "resumes/" + profile.getId());
                if (!studentDir.exists()) {
                    if (!studentDir.mkdirs()) {
                        log.error("创建学生简历目录失败: {}", studentDir.getAbsolutePath());
                        throw new BusinessException("创建上传目录失败");
                    }
                }

                // 使用File对象获取规范化的绝对路径
                uploadDir = studentDir.getAbsolutePath();
            } else {
                // 使用相对路径，让Web服务器决定文件存放的实际位置
                // 创建完整的目录结构，使用应用程序根目录
                // 获取应用根目录，优先使用配置的baseDir
                String rootDir = StringUtils.hasText(baseDir) ? baseDir : System.getProperty("user.dir");
                File uploadBaseDir = new File(rootDir, relativePath);

                // 确保目录存在
                if (!uploadBaseDir.exists()) {
                    if (!uploadBaseDir.mkdirs()) {
                        log.error("创建目录结构失败: {}", uploadBaseDir.getAbsolutePath());
                        throw new BusinessException("创建上传目录失败");
                    }
                }

                uploadDir = relativePath;
            }

            log.info("上传目录: {}", uploadDir);

            // 生成文件名
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String filename = profile.getId() + "_" + timestamp + "_" + UUID.randomUUID().toString().substring(0, 8)
                    + "." + extension;

            // 文件路径 - 根据是否使用绝对路径来构建
            String filePath;
            File destFile;
            if (useAbsolutePath) {
                filePath = uploadDir + File.separator + filename;
                destFile = new File(filePath);
            } else {
                filePath = relativePath + "/" + filename; // 数据库中存储相对路径
                // 使用应用根目录构建完整路径
                String rootDir = StringUtils.hasText(baseDir) ? baseDir : System.getProperty("user.dir");
                destFile = new File(rootDir, filePath);
            }

            log.info("文件将保存为: {}", filePath);

            // 确保目标目录存在
            File parentDir = destFile.getParentFile();
            if (!parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    log.error("创建目标文件父目录失败: {}", parentDir.getAbsolutePath());
                    throw new BusinessException("创建上传目录失败");
                }
            }

            // 保存文件
            file.transferTo(destFile);
            log.info("文件保存成功: {}", destFile.getAbsolutePath());

            // 保存文件信息到数据库
            StudentResumeFile resumeFile = new StudentResumeFile();
            resumeFile.setStudentProfileId(profile.getId());
            resumeFile.setFileName(originalFilename);
            resumeFile.setFilePath(filePath);
            resumeFile.setFileSize(file.getSize());
            resumeFile.setFileType(extension);
            resumeFile.setUploadedAt(LocalDateTime.now());
            studentResumeFileMapper.insert(resumeFile);

            return resumeFile;
        } catch (IOException e) {
            log.error("文件上传IO异常", e);
            throw new BusinessException("文件上传失败：" + e.getMessage());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件上传过程中发生错误", e);
            throw new BusinessException("文件上传过程中发生错误：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean updateProjectExperience(Long studentId, List<Map<String, Object>> projectExperiences) {
        if (studentId == null) {
            throw new BusinessException("学生ID不能为空");
        }

        if (projectExperiences == null || projectExperiences.isEmpty()) {
            throw new BusinessException("项目/实习经历不能为空");
        }

        try {
            // 检查学生是否存在 - 先尝试直接通过studentId获取档案
            StudentProfile profile = null;

            try {
                profile = studentProfileMapper.selectById(studentId);
            } catch (Exception e) {
                log.warn("通过studentId={}直接查询学生档案失败: {}", studentId, e.getMessage());
            }

            // 如果直接通过ID找不到，则尝试通过userId查找
            if (profile == null) {
                log.info("尝试将studentId={}作为userId查询对应的学生档案", studentId);
                LambdaQueryWrapper<StudentProfile> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(StudentProfile::getUserId, studentId);
                profile = studentProfileMapper.selectOne(queryWrapper);

                if (profile == null) {
                    log.error("无法找到学生档案，studentId={}既不是有效的档案ID也不是有效的用户ID", studentId);
                    throw new BusinessException("找不到对应的学生信息");
                }

                log.info("成功通过userId={}找到学生档案，档案ID={}", studentId, profile.getId());
            }

            log.info("更新学生(id={}, name={})的项目/实习经历，数量: {}",
                    profile.getId(), profile.getName(), projectExperiences.size());

            // 删除现有的项目和实习经历记录
            studentStructuredResumeMapper.deleteByStudentIdAndType(profile.getId(), "project");
            studentStructuredResumeMapper.deleteByStudentIdAndType(profile.getId(), "experience");

            // 保存新的项目/实习经历记录
            int orderNum = 0;
            for (Map<String, Object> experience : projectExperiences) {
                String type = (String) experience.get("type");

                // 根据类型确定section_type
                String sectionType = "project";
                if ("internship".equals(type)) {
                    sectionType = "experience";
                }

                // 创建结构化简历记录
                StudentStructuredResume resume = new StudentStructuredResume();
                resume.setStudentProfileId(profile.getId());
                resume.setSectionType(sectionType);
                resume.setContent(experience);
                resume.setOrderNum(orderNum++);
                resume.setCreatedAt(LocalDateTime.now());
                resume.setUpdatedAt(LocalDateTime.now());

                // 保存到数据库
                studentStructuredResumeMapper.insert(resume);
            }

            log.info("成功更新学生(id={})的项目/实习经历", profile.getId());
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新项目/实习经历时发生异常", e);
            throw new BusinessException("更新项目/实习经历失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean updateEducationExperience(Long studentId, List<Map<String, Object>> educationExperiences) {
        if (studentId == null) {
            throw new BusinessException("学生ID不能为空");
        }

        if (educationExperiences == null || educationExperiences.isEmpty()) {
            throw new BusinessException("教育经历不能为空");
        }

        try {
            // 检查学生是否存在 - 先尝试直接通过studentId获取档案
            StudentProfile profile = null;

            try {
                profile = studentProfileMapper.selectById(studentId);
            } catch (Exception e) {
                log.warn("通过studentId={}直接查询学生档案失败: {}", studentId, e.getMessage());
            }

            // 如果直接通过ID找不到，则尝试通过userId查找
            if (profile == null) {
                log.info("尝试将studentId={}作为userId查询对应的学生档案", studentId);
                LambdaQueryWrapper<StudentProfile> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(StudentProfile::getUserId, studentId);
                profile = studentProfileMapper.selectOne(queryWrapper);

                if (profile == null) {
                    log.error("无法找到学生档案，studentId={}既不是有效的档案ID也不是有效的用户ID", studentId);
                    throw new BusinessException("找不到对应的学生信息");
                }

                log.info("成功通过userId={}找到学生档案，档案ID={}", studentId, profile.getId());
            }

            log.info("更新学生(id={}, name={})的教育经历，数量: {}",
                    profile.getId(), profile.getName(), educationExperiences.size());

            // 删除现有的教育经历记录
            studentStructuredResumeMapper.deleteByStudentIdAndType(profile.getId(), "education");

            // 保存新的教育经历记录
            int orderNum = 0;
            for (Map<String, Object> education : educationExperiences) {
                // 创建结构化简历记录
                StudentStructuredResume resume = new StudentStructuredResume();
                resume.setStudentProfileId(profile.getId());
                resume.setSectionType("education");
                resume.setContent(education);
                resume.setOrderNum(orderNum++);
                resume.setCreatedAt(LocalDateTime.now());
                resume.setUpdatedAt(LocalDateTime.now());

                // 保存到数据库
                studentStructuredResumeMapper.insert(resume);
            }

            log.info("成功更新学生(id={})的教育经历", profile.getId());
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新教育经历时发生异常", e);
            throw new BusinessException("更新教育经历失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean updateSkillTags(Long studentId, List<String> skillTags) {
        if (studentId == null) {
            throw new BusinessException("学生ID不能为空");
        }

        if (skillTags == null || skillTags.isEmpty()) {
            throw new BusinessException("技能标签不能为空");
        }

        try {
            // 检查学生是否存在 - 先尝试直接通过studentId获取档案
            StudentProfile profile = null;

            try {
                profile = studentProfileMapper.selectById(studentId);
            } catch (Exception e) {
                log.warn("通过studentId={}直接查询学生档案失败: {}", studentId, e.getMessage());
            }

            // 如果直接通过ID找不到，则尝试通过userId查找
            if (profile == null) {
                log.info("尝试将studentId={}作为userId查询对应的学生档案", studentId);
                LambdaQueryWrapper<StudentProfile> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(StudentProfile::getUserId, studentId);
                profile = studentProfileMapper.selectOne(queryWrapper);

                if (profile == null) {
                    log.error("无法找到学生档案，studentId={}既不是有效的档案ID也不是有效的用户ID", studentId);
                    throw new BusinessException("找不到对应的学生信息");
                }

                log.info("成功通过userId={}找到学生档案，档案ID={}", studentId, profile.getId());
            }

            log.info("更新学生(id={}, name={})的技能标签，数量: {}",
                    profile.getId(), profile.getName(), skillTags.size());

            // 删除学生现有的技能标签关联
            studentSkillMapper.deleteByStudentId(profile.getId());

            // 为每个技能标签创建或获取ID，并与学生关联
            List<Long> skillTagIds = new ArrayList<>();
            for (String tagName : skillTags) {
                if (StringUtils.hasText(tagName)) {
                    // 获取或创建技能标签
                    Long tagId = skillTagMapper.getOrCreateByName(tagName);
                    if (tagId != null) {
                        skillTagIds.add(tagId);
                    }
                }
            }

            // 如果有技能标签需要关联
            if (!skillTagIds.isEmpty()) {
                // 批量插入学生技能关联记录
                studentSkillMapper.batchInsert(profile.getId(), skillTagIds);
            }

            log.info("成功更新学生(id={})的技能标签，关联数量: {}", profile.getId(), skillTagIds.size());
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新技能标签时发生异常", e);
            throw new BusinessException("更新技能标签失败: " + e.getMessage());
        }
    }

    /**
     * 根据学生ID和简历部分类型获取结构化简历数据
     * 
     * @param studentProfileId 学生档案ID
     * @param sectionType      简历部分类型
     * @return 简历部分数据列表
     */
    @Override
    public List<StudentStructuredResume> getStructuredResumeByType(Long studentProfileId, String sectionType) {
        if (studentProfileId == null) {
            throw new BusinessException("学生ID不能为空");
        }

        if (sectionType == null || sectionType.isEmpty()) {
            throw new BusinessException("简历部分类型不能为空");
        }

        try {
            // 查询指定类型的结构化简历数据
            LambdaQueryWrapper<StudentStructuredResume> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(StudentStructuredResume::getStudentProfileId, studentProfileId)
                    .eq(StudentStructuredResume::getSectionType, sectionType)
                    .orderByAsc(StudentStructuredResume::getOrderNum);

            List<StudentStructuredResume> resumeList = studentStructuredResumeMapper.selectList(queryWrapper);
            log.info("获取学生(id={})的{}类型简历数据，条数: {}", studentProfileId, sectionType, resumeList.size());

            return resumeList;
        } catch (Exception e) {
            log.error("获取学生结构化简历数据时发生异常", e);
            throw new BusinessException("获取简历数据失败: " + e.getMessage());
        }
    }

    @Override
    public List<String> getStudentSkillTags(Long studentId) {
        if (studentId == null) {
            throw new BusinessException("学生ID不能为空");
        }

        try {
            // 获取学生档案信息
            StudentProfile profile = getStudentById(studentId);
            if (profile == null) {
                throw new BusinessException("找不到对应的学生信息");
            }

            // 使用SQL查询来获取学生关联的所有技能标签名称
            log.info("查询学生(id={})的技能标签", profile.getId());

            // 查询学生所有关联的技能标签
            List<String> skillTags = skillTagMapper.getStudentSkillTags(profile.getId());

            log.info("获取到学生(id={})的技能标签数量: {}", profile.getId(), skillTags.size());
            return skillTags;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取学生技能标签时发生异常", e);
            throw new BusinessException("获取技能标签失败: " + e.getMessage());
        }
    }

    /**
     * 根据学生ID获取学生档案信息
     * 如果学生不存在则抛出异常
     * 
     * @param studentId 学生ID
     * @return 学生档案信息
     */
    private StudentProfile getStudentById(Long studentId) {
        // 先尝试直接通过studentId获取档案
        StudentProfile profile = studentProfileMapper.selectById(studentId);

        // 如果直接通过ID找不到，则尝试通过userId查找
        if (profile == null) {
            log.info("尝试将studentId={}作为userId查询对应的学生档案", studentId);
            LambdaQueryWrapper<StudentProfile> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(StudentProfile::getUserId, studentId);
            profile = studentProfileMapper.selectOne(queryWrapper);
        }

        if (profile == null) {
            throw new BusinessException("找不到对应的学生信息");
        }

        return profile;
    }
}