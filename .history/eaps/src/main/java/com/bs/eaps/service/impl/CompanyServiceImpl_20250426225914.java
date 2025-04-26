package com.bs.eaps.service.impl;

import com.bs.eaps.entity.CompanyLicenseFile;
import com.bs.eaps.entity.CompanyProfile;
import com.bs.eaps.entity.StudentBlacklist;
import com.bs.eaps.entity.Application;
import com.bs.eaps.entity.StudentFavorite;
import com.bs.eaps.entity.StudentProfile;
import com.bs.eaps.mapper.CompanyLicenseFileMapper;
import com.bs.eaps.mapper.CompanyProfileMapper;
import com.bs.eaps.mapper.StudentBlacklistMapper;
import com.bs.eaps.mapper.ApplicationMapper;
import com.bs.eaps.mapper.JobPostingMapper;
import com.bs.eaps.mapper.StudentFavoriteMapper;
import com.bs.eaps.mapper.StudentProfileMapper;
import com.bs.eaps.service.CompanyService;
import com.bs.eaps.service.CounselorTaskService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import jakarta.annotation.Resource;

/**
 * 企业服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyProfileMapper companyProfileMapper;
    private final CompanyLicenseFileMapper companyLicenseFileMapper;
    @Resource
    private StudentBlacklistMapper studentBlacklistMapper;
    private final CounselorTaskService counselorTaskService;
    private final ApplicationMapper applicationMapper;
    private final JobPostingMapper jobPostingMapper;
    @Resource
    private StudentFavoriteMapper studentFavoriteMapper;
    @Resource
    private StudentProfileMapper studentProfileMapper;

    @Value("${upload.path:/uploads}")
    private String uploadPath;

    @Override
    public CompanyProfile getCompanyProfile(Long userId) {
        try {
            if (userId == null) {
                throw new IllegalArgumentException("企业ID不能为空");
            }

            // 通过userId查询企业信息
            CompanyProfile profile = companyProfileMapper.selectOne(
                    new LambdaQueryWrapper<CompanyProfile>()
                            .eq(CompanyProfile::getUserId, userId));

            // 如果没有找到记录，返回一个空的企业档案对象
            if (profile == null) {
                profile = new CompanyProfile();
                profile.setUserId(userId);
                // 默认状态
                profile.setCertificationStatus("uncertified");
            }

            return profile;
        } catch (Exception e) {
            // 输出详细日志以便调试
            log.error("获取企业信息失败，用户ID：{}", userId, e);
            throw new RuntimeException("获取企业资料失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public boolean updateCompanyProfile(CompanyProfile profile) {
        try {
            if (profile.getUserId() == null) {
                throw new IllegalArgumentException("用户ID不能为空");
            }

            // 先查询是否存在企业资料
            CompanyProfile existingProfile = companyProfileMapper.selectOne(
                    new LambdaQueryWrapper<CompanyProfile>()
                            .eq(CompanyProfile::getUserId, profile.getUserId()));

            if (existingProfile != null) {
                // 如果存在，则更新现有记录
                // 保留不应被覆盖的字段值
                profile.setId(existingProfile.getId());
                profile.setName(existingProfile.getName());
                profile.setUnifiedSocialCreditCode(existingProfile.getUnifiedSocialCreditCode());
                profile.setLogoPath(existingProfile.getLogoPath());
                profile.setCertificationStatus(existingProfile.getCertificationStatus());
                profile.setCertificationExpiryDate(existingProfile.getCertificationExpiryDate());
                profile.setCreatedAt(existingProfile.getCreatedAt());

                // 设置更新时间
                profile.setUpdatedAt(new Date());

                return companyProfileMapper.updateById(profile) > 0;
            } else {
                // 如果不存在，则创建新记录
                profile.setCreatedAt(new Date());
                profile.setUpdatedAt(new Date());
                profile.setCertificationStatus("uncertified"); // 默认未认证状态

                return companyProfileMapper.insert(profile) > 0;
            }
        } catch (Exception e) {
            // 记录错误日志
            log.error("更新企业资料失败，用户ID：{}", profile.getUserId(), e);
            throw new RuntimeException("更新企业资料失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Object uploadCompanyLogo(Long userId, MultipartFile file) {
        // 创建上传目录
        Path directoryPath = Paths.get(uploadPath, "company", "logo");
        try {
            Files.createDirectories(directoryPath);

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = directoryPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath);

            // 查询企业信息
            CompanyProfile profile = companyProfileMapper.selectOne(
                    new LambdaQueryWrapper<CompanyProfile>()
                            .eq(CompanyProfile::getUserId, userId));

            // 如果不存在则创建新的企业档案
            if (profile == null) {
                profile = new CompanyProfile();
                profile.setUserId(userId);
                profile.setCertificationStatus("uncertified");
                profile.setLogoPath("/uploads/company/logo/" + newFilename);
                companyProfileMapper.insert(profile);
            } else {
                // 更新企业Logo路径
                profile.setLogoPath("/uploads/company/logo/" + newFilename);
                companyProfileMapper.updateById(profile);
            }

            // 返回文件信息
            Map<String, Object> result = new HashMap<>();
            result.put("fileName", originalFilename);
            result.put("filePath", profile.getLogoPath());
            return result;

        } catch (IOException e) {
            throw new RuntimeException("Logo上传失败", e);
        }
    }

    @Override
    @Transactional
    public CompanyLicenseFile uploadCompanyLicense(Long userId, MultipartFile file) {
        // 创建上传目录
        Path directoryPath = Paths.get(uploadPath, "company", "license");
        try {
            Files.createDirectories(directoryPath);

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = directoryPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath);

            // 查询企业信息
            CompanyProfile profile = companyProfileMapper.selectOne(
                    new LambdaQueryWrapper<CompanyProfile>()
                            .eq(CompanyProfile::getUserId, userId));

            if (profile == null) {
                throw new RuntimeException("未找到企业信息");
            }

            // 保存文件记录
            CompanyLicenseFile licenseFile = new CompanyLicenseFile();
            licenseFile.setCompanyProfileId(profile.getId());
            licenseFile.setFileName(originalFilename);
            licenseFile.setFilePath("/uploads/company/license/" + newFilename);
            licenseFile.setFileSize(file.getSize());
            licenseFile.setFileType(file.getContentType());

            companyLicenseFileMapper.insert(licenseFile);

            // 更新企业认证状态为待审核
            profile.setCertificationStatus("pending");
            companyProfileMapper.updateById(profile);

            // 创建辅导员任务
            String taskTitle = "企业认证审核: " + profile.getName();
            if (counselorTaskService != null) {
                counselorTaskService.createCompanyCertificationTask(profile.getId(), taskTitle);
                log.info("已创建企业认证审核任务：企业ID={}, 企业名称={}", profile.getId(), profile.getName());
            } else {
                log.error("创建辅导员任务失败：counselorTaskService为null");
            }

            return licenseFile;
        } catch (IOException e) {
            throw new RuntimeException("上传企业营业执照失败", e);
        }
    }

    @Override
    @Transactional
    public boolean toggleStudentBlacklist(Long companyUserId, Long studentId, String reason, Boolean isBlacklisted) {
        try {
            // 获取企业信息
            CompanyProfile profile = getCompanyProfile(companyUserId);
            if (profile == null || profile.getId() == null) {
                log.error("无法操作黑名单，未找到企业信息，用户ID：{}", companyUserId);
                throw new RuntimeException("未找到企业信息");
            }

            Long companyId = profile.getId();

            // 查询是否已经存在黑名单记录
            LambdaQueryWrapper<StudentBlacklist> queryWrapper = new LambdaQueryWrapper<StudentBlacklist>()
                    .eq(StudentBlacklist::getCompanyId, companyId)
                    .eq(StudentBlacklist::getStudentId, studentId);

            StudentBlacklist blacklist = studentBlacklistMapper.selectOne(queryWrapper);

            // 如果需要加入黑名单
            if (isBlacklisted) {
                // 如果已有记录，则不需要操作
                if (blacklist != null) {
                    log.info("学生已在黑名单中，companyId: {}, studentId: {}", companyId, studentId);
                    return true;
                }

                // 创建新记录
                blacklist = new StudentBlacklist();
                blacklist.setCompanyId(companyId);
                blacklist.setStudentId(studentId);
                blacklist.setReason(reason);
                blacklist.setCreatedAt(new Date());

                return studentBlacklistMapper.insert(blacklist) > 0;
            }
            // 如果需要移出黑名单
            else {
                // 如果没有记录，则不需要操作
                if (blacklist == null) {
                    log.info("学生不在黑名单中，无需移除，companyId: {}, studentId: {}", companyId, studentId);
                    return true;
                }

                // 删除记录
                return studentBlacklistMapper.delete(queryWrapper) > 0;
            }
        } catch (Exception e) {
            log.error("黑名单操作失败，companyUserId: {}, studentId: {}, isBlacklisted: {}",
                    companyUserId, studentId, isBlacklisted, e);
            throw new RuntimeException("黑名单操作失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getBlacklistedStudents(Long companyUserId, int page, int limit) {
        try {
            // 获取企业信息
            CompanyProfile profile = getCompanyProfile(companyUserId);
            if (profile == null || profile.getId() == null) {
                log.error("获取黑名单失败，未找到企业信息，用户ID：{}", companyUserId);
                throw new RuntimeException("未找到企业信息");
            }

            Long companyId = profile.getId();

            // 创建分页对象
            Page<Map<String, Object>> pageObj = new Page<>(page, limit);

            // 查询带有学生详细信息的黑名单列表
            Page<Map<String, Object>> resultPage = (Page<Map<String, Object>>) studentBlacklistMapper
                    .selectBlacklistedStudentsWithDetails(pageObj, companyId);

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("total", resultPage.getTotal());
            result.put("list", resultPage.getRecords());
            result.put("page", page);
            result.put("limit", limit);

            return result;
        } catch (Exception e) {
            log.error("获取黑名单列表失败，companyUserId: {}", companyUserId, e);
            throw new RuntimeException("获取黑名单列表失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public boolean processApplication(Long companyUserId, Long applicationId, String status, String notes) {
        log.info("处理学生申请: companyUserId={}, applicationId={}, status={}, notes={}",
                companyUserId, applicationId, status, notes);

        try {
            // 1. 获取申请记录
            Application application = applicationMapper.selectById(applicationId);
            if (application == null) {
                log.error("申请记录不存在: applicationId={}", applicationId);
                throw new RuntimeException("申请记录不存在");
            }

            // 2. 验证权限 - 确认申请的岗位属于该企业
            Long jobId = application.getJobId();
            Long companyId = getCompanyIdByUserId(companyUserId);

            // 通过jobId获取岗位信息并验证是否属于当前企业
            boolean hasPermission = jobPostingMapper.selectCount(
                    new LambdaQueryWrapper<com.bs.eaps.entity.JobPosting>()
                            .eq(com.bs.eaps.entity.JobPosting::getId, jobId)
                            .eq(com.bs.eaps.entity.JobPosting::getCompanyId, companyId)) > 0;

            if (!hasPermission) {
                log.error("企业无权处理此申请: companyUserId={}, applicationId={}", companyUserId, applicationId);
                throw new RuntimeException("无权处理此申请");
            }

            // 3. 更新申请状态
            application.setStatus(status);
            application.setCompanyNotes(notes);
            application.setLastUpdatedAt(LocalDateTime.now());

            int result = applicationMapper.updateById(application);

            log.info("申请状态更新结果: applicationId={}, status={}, result={}",
                    applicationId, status, result > 0 ? "成功" : "失败");

            return result > 0;
        } catch (Exception e) {
            log.error("处理学生申请失败: applicationId={}", applicationId, e);
            throw new RuntimeException("处理申请失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean toggleFavoriteStudent(Long companyUserId, Long studentId, Boolean isFavorite) {
        // 获取企业ID
        Long companyId = getCompanyIdByUserId(companyUserId);
        if (companyId == null) {
            log.error("企业不存在: userId={}", companyUserId);
            throw new RuntimeException("企业不存在");
        }

        // 检查学生是否存在
        StudentProfile student = studentProfileMapper.selectById(studentId);
        if (student == null) {
            log.error("学生不存在: studentId={}", studentId);
            throw new RuntimeException("学生不存在");
        }

        // 查询是否已收藏
        LambdaQueryWrapper<StudentFavorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StudentFavorite::getCompanyId, companyId)
                .eq(StudentFavorite::getStudentId, studentId);
        StudentFavorite existingFavorite = studentFavoriteMapper.selectOne(queryWrapper);

        if (Boolean.TRUE.equals(isFavorite)) {
            // 添加收藏
            if (existingFavorite == null) {
                StudentFavorite favorite = new StudentFavorite();
                favorite.setCompanyId(companyId);
                favorite.setStudentId(studentId);
                favorite.setCreatedAt(LocalDateTime.now());
                return studentFavoriteMapper.insert(favorite) > 0;
            }
            return true; // 已收藏
        } else {
            // 取消收藏
            if (existingFavorite != null) {
                return studentFavoriteMapper.deleteById(existingFavorite.getId()) > 0;
            }
            return true; // 未收藏
        }
    }

    @Override
    public Map<String, Object> getFavoriteStudents(Long companyUserId, int page, int limit) {
        // 获取企业ID
        Long companyId = getCompanyIdByUserId(companyUserId);
        if (companyId == null) {
            log.error("企业不存在: userId={}", companyUserId);
            throw new RuntimeException("企业不存在");
        }

        Map<String, Object> result = new HashMap<>();

        // 查询收藏学生记录
        LambdaQueryWrapper<StudentFavorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StudentFavorite::getCompanyId, companyId)
                .orderByDesc(StudentFavorite::getCreatedAt);

        Page<StudentFavorite> pageParam = new Page<>(page, limit);
        Page<StudentFavorite> favoriteRecords = studentFavoriteMapper.selectPage(pageParam, queryWrapper);

        // 获取学生详细信息
        List<Map<String, Object>> studentList = new ArrayList<>();
        for (StudentFavorite favorite : favoriteRecords.getRecords()) {
            StudentProfile student = studentProfileMapper.selectById(favorite.getStudentId());
            if (student != null) {
                Map<String, Object> studentInfo = new HashMap<>();
                studentInfo.put("id", student.getId());
                studentInfo.put("name", student.getName());
                studentInfo.put("school", student.getSchool());
                studentInfo.put("major", student.getMajor());
                studentInfo.put("grade", student.getGrade());
                studentInfo.put("favoriteTime", favorite.getCreatedAt());

                // 查询是否被拉黑
                LambdaQueryWrapper<StudentBlacklist> blacklistQuery = new LambdaQueryWrapper<>();
                blacklistQuery.eq(StudentBlacklist::getCompanyId, companyId)
                        .eq(StudentBlacklist::getStudentId, student.getId());
                studentInfo.put("isBlacklisted", studentBlacklistMapper.selectCount(blacklistQuery) > 0);

                studentList.add(studentInfo);
            }
        }

        result.put("total", favoriteRecords.getTotal());
        result.put("data", studentList);

        return result;
    }

    @Override
    public Long getCompanyUserIdByJobId(Long jobId) {
        if (jobId == null) {
            throw new IllegalArgumentException("岗位ID不能为空");
        }

        try {
            // 使用BaseMapper方法查询岗位信息
            JobPosting jobPosting = jobPostingMapper.selectById(jobId);
            if (jobPosting == null) {
                log.error("未找到对应的岗位信息，岗位ID: {}", jobId);
                return null;
            }

            // 获取企业ID
            Long companyId = jobPosting.getCompanyId();
            if (companyId == null) {
                log.error("岗位信息中不包含企业ID，岗位ID: {}", jobId);
                return null;
            }

            // 查询企业信息，获取用户ID
            CompanyProfile companyProfile = companyProfileMapper.selectById(companyId);
            if (companyProfile == null) {
                log.error("未找到对应的企业信息，企业ID: {}", companyId);
                return null;
            }

            return companyProfile.getUserId();
        } catch (Exception e) {
            log.error("获取企业用户ID失败，岗位ID: {}", jobId, e);
            throw new RuntimeException("获取企业用户ID失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据用户ID获取企业ID
     */
    private Long getCompanyIdByUserId(Long userId) {
        CompanyProfile company = companyProfileMapper.selectOne(
                new LambdaQueryWrapper<CompanyProfile>()
                        .eq(CompanyProfile::getUserId, userId));

        if (company == null) {
            log.error("未找到企业信息: userId={}", userId);
            throw new RuntimeException("未找到企业信息");
        }

        return company.getId();
    }
}