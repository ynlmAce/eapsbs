package com.bs.eaps.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bs.eaps.common.ApiResponse;
import com.bs.eaps.entity.SkillTag;
import com.bs.eaps.entity.StudentProfile;
import com.bs.eaps.entity.StudentResumeFile;
import com.bs.eaps.entity.StudentSkill;
import com.bs.eaps.entity.StudentStructuredResume;
import com.bs.eaps.entity.CounselorProfile;
import com.bs.eaps.mapper.SkillTagMapper;
import com.bs.eaps.mapper.StudentSkillMapper;
import com.bs.eaps.service.StudentService;
import com.bs.eaps.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bs.eaps.mapper.CounselorProfileMapper;

/**
 * 学生模块控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final SkillTagMapper skillTagMapper;
    private final StudentSkillMapper studentSkillMapper;
    private final CounselorProfileMapper counselorProfileMapper;

    /**
     * 获取学生档案
     * 
     * @param request 请求参数，可能包含studentId
     * @return 学生档案信息
     */
    @PostMapping("/profile")
    public ApiResponse getStudentProfile(@RequestBody(required = false) Map<String, Object> request) {
        // 获取当前登录用户ID
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("获取学生档案，当前登录用户ID: {}", currentUserId);

        // 如果请求中包含studentId，则使用该ID
        Long studentId = null;
        if (request != null && request.containsKey("studentId")) {
            Object idObj = request.get("studentId");
            if (idObj instanceof Number) {
                studentId = ((Number) idObj).longValue();
            } else if (idObj instanceof String) {
                try {
                    studentId = Long.parseLong((String) idObj);
                } catch (NumberFormatException e) {
                    log.warn("转换studentId失败: {}", idObj, e);
                }
            }
        }

        // 如果没有提供studentId，则使用当前登录用户ID
        if (studentId == null) {
            studentId = currentUserId;
            log.info("使用当前登录用户ID作为学生ID: {}", studentId);
        }

        // 验证权限 - 只有当前用户或管理员可以查看
        if (currentUserId == null) {
            return ApiResponse.unauthorized();
        }

        try {
            StudentProfile profile = studentService.getStudentProfile(studentId);
            return ApiResponse.success(profile);
        } catch (Exception e) {
            log.error("获取学生档案异常", e);
            return ApiResponse.businessError(e.getMessage());
        }
    }

    /**
     * 更新学生档案
     * 
     * @param profile 学生档案信息
     * @return 更新结果
     */
    @PostMapping("/profile/update")
    public ApiResponse updateStudentProfile(@RequestBody StudentProfile profile) {
        // 获取当前登录用户ID
        Long currentUserId = SecurityUtils.getCurrentUserId();

        if (currentUserId == null) {
            return ApiResponse.unauthorized();
        }

        // 安全校验：确保只能更新当前登录用户的档案
        if (profile.getUserId() != null && !profile.getUserId().equals(currentUserId)) {
            return ApiResponse.businessError("无权限修改其他学生的档案");
        }

        // 设置用户ID
        profile.setUserId(currentUserId);

        try {
            boolean result = studentService.updateStudentProfile(profile);
            return result ? ApiResponse.success(true) : ApiResponse.businessError("更新失败");
        } catch (Exception e) {
            log.error("更新学生档案异常", e);
            return ApiResponse.businessError(e.getMessage());
        }
    }

    /**
     * 上传学生简历
     * 
     * @param file 简历文件
     * @return 上传结果
     */
    @PostMapping("/resume/upload")
    public ApiResponse uploadResume(MultipartFile file) {
        // 获取当前登录用户ID
        Long currentUserId = SecurityUtils.getCurrentUserId();

        if (currentUserId == null) {
            return ApiResponse.unauthorized();
        }

        try {
            StudentResumeFile resumeFile = studentService.uploadStudentResume(currentUserId, file);
            return ApiResponse.success(resumeFile);
        } catch (Exception e) {
            log.error("上传简历异常", e);
            return ApiResponse.businessError(e.getMessage());
        }
    }

    /**
     * 获取学生的项目和实习经历
     * 
     * @param request 请求参数，可能包含studentId
     * @return 项目和实习经历数据
     */
    @PostMapping("/resume/experiences")
    public ApiResponse getProjectExperiences(@RequestBody(required = false) Map<String, Object> request) {
        // 获取当前登录用户ID
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("获取学生项目/实习经历，当前登录用户ID: {}", currentUserId);

        if (currentUserId == null) {
            return ApiResponse.unauthorized();
        }

        // 如果请求中包含studentId，则使用该ID
        Long studentId = null;
        if (request != null && request.containsKey("studentId")) {
            Object idObj = request.get("studentId");
            if (idObj instanceof Number) {
                studentId = ((Number) idObj).longValue();
            } else if (idObj instanceof String) {
                try {
                    studentId = Long.parseLong((String) idObj);
                } catch (NumberFormatException e) {
                    log.warn("转换studentId失败: {}", idObj, e);
                }
            }
        }

        // 如果没有提供studentId，则使用当前登录用户ID
        if (studentId == null) {
            studentId = currentUserId;
            log.info("使用当前登录用户ID作为学生ID: {}", studentId);
        }

        try {
            // 通过学生ID获取学生个人资料
            StudentProfile profile = studentService.getStudentProfile(studentId);

            // 从数据库获取对应的项目和实习经历
            List<StudentStructuredResume> projectData = studentService.getStructuredResumeByType(profile.getId(),
                    "project");
            List<StudentStructuredResume> internshipData = studentService.getStructuredResumeByType(profile.getId(),
                    "experience");

            // 合并结果
            Map<String, Object> result = new HashMap<>();

            List<Map<String, Object>> combinedExperiences = new ArrayList<>();

            // 处理项目经历
            for (StudentStructuredResume project : projectData) {
                if (project.getContent() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> content = (Map<String, Object>) project.getContent();
                    content.put("type", "project");
                    combinedExperiences.add(content);
                }
            }

            // 处理实习经历
            for (StudentStructuredResume internship : internshipData) {
                if (internship.getContent() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> content = (Map<String, Object>) internship.getContent();
                    content.put("type", "internship");
                    combinedExperiences.add(content);
                }
            }

            result.put("projectExperiences", combinedExperiences);

            log.info("成功获取学生项目/实习经历数据，数量: {}", combinedExperiences.size());
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("获取学生项目/实习经历异常", e);
            return ApiResponse.businessError(e.getMessage());
        }
    }

    /**
     * 更新项目/实习经历
     * 
     * @param requestData 包含项目/实习经历的请求数据
     * @return 更新结果
     */
    @PostMapping("/resume/project")
    public ApiResponse updateProjectExperience(@RequestBody Map<String, Object> requestData) {
        // 获取当前登录用户ID
        Long currentUserId = SecurityUtils.getCurrentUserId();

        if (currentUserId == null) {
            return ApiResponse.unauthorized();
        }

        try {
            log.info("接收到项目/实习经历更新请求: {}", requestData);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> projectExperiences = (List<Map<String, Object>>) requestData
                    .get("projectExperiences");

            if (projectExperiences == null || projectExperiences.isEmpty()) {
                return ApiResponse.businessError("未提供项目/实习经历数据");
            }

            boolean result = studentService.updateProjectExperience(currentUserId, projectExperiences);
            return result ? ApiResponse.success(true) : ApiResponse.businessError("更新失败");
        } catch (ClassCastException e) {
            log.error("项目/实习经历数据格式错误", e);
            return ApiResponse.businessError("数据格式错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("更新项目/实习经历异常", e);
            return ApiResponse.businessError(e.getMessage());
        }
    }

    /**
     * 更新教育经历
     * 
     * @param requestData 包含教育经历的请求数据
     * @return 更新结果
     */
    @PostMapping("/resume/education")
    public ApiResponse updateEducationExperience(@RequestBody Map<String, Object> requestData) {
        // 获取当前登录用户ID
        Long currentUserId = SecurityUtils.getCurrentUserId();

        if (currentUserId == null) {
            return ApiResponse.unauthorized();
        }

        try {
            log.info("接收到教育经历更新请求: {}", requestData);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> educationExperiences = (List<Map<String, Object>>) requestData
                    .get("educationExperiences");

            if (educationExperiences == null || educationExperiences.isEmpty()) {
                return ApiResponse.businessError("未提供教育经历数据");
            }

            boolean result = studentService.updateEducationExperience(currentUserId, educationExperiences);
            return result ? ApiResponse.success(true) : ApiResponse.businessError("更新失败");
        } catch (ClassCastException e) {
            log.error("教育经历数据格式错误", e);
            return ApiResponse.businessError("数据格式错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("更新教育经历异常", e);
            return ApiResponse.businessError(e.getMessage());
        }
    }

    /**
     * 更新技能标签
     * 
     * @param requestData 包含技能标签的请求数据
     * @return 更新结果
     */
    @PostMapping("/resume/skills")
    public ApiResponse updateSkillTags(@RequestBody Map<String, Object> requestData) {
        // 获取当前登录用户ID
        Long currentUserId = SecurityUtils.getCurrentUserId();

        if (currentUserId == null) {
            return ApiResponse.unauthorized();
        }

        try {
            log.info("接收到技能标签更新请求: {}", requestData);

            @SuppressWarnings("unchecked")
            List<String> skillTags = (List<String>) requestData.get("skillTags");

            if (skillTags == null || skillTags.isEmpty()) {
                return ApiResponse.businessError("未提供技能标签数据");
            }

            boolean result = studentService.updateSkillTags(currentUserId, skillTags);
            return result ? ApiResponse.success(true) : ApiResponse.businessError("更新失败");
        } catch (ClassCastException e) {
            log.error("技能标签数据格式错误", e);
            return ApiResponse.businessError("数据格式错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("更新技能标签异常", e);
            return ApiResponse.businessError(e.getMessage());
        }
    }

    /**
     * 获取学生的教育经历
     * 
     * @param request 请求参数，可能包含studentId
     * @return 教育经历数据
     */
    @PostMapping("/resume/education-experiences")
    public ApiResponse getEducationExperiences(@RequestBody(required = false) Map<String, Object> request) {
        // 获取当前登录用户ID
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("获取学生教育经历，当前登录用户ID: {}", currentUserId);

        if (currentUserId == null) {
            return ApiResponse.unauthorized();
        }

        // 如果请求中包含studentId，则使用该ID
        Long studentId = null;
        if (request != null && request.containsKey("studentId")) {
            Object idObj = request.get("studentId");
            if (idObj instanceof Number) {
                studentId = ((Number) idObj).longValue();
            } else if (idObj instanceof String) {
                try {
                    studentId = Long.parseLong((String) idObj);
                } catch (NumberFormatException e) {
                    log.warn("转换studentId失败: {}", idObj, e);
                }
            }
        }

        // 如果没有提供studentId，则使用当前登录用户ID
        if (studentId == null) {
            studentId = currentUserId;
            log.info("使用当前登录用户ID作为学生ID: {}", studentId);
        }

        try {
            // 通过学生ID获取学生个人资料
            StudentProfile profile = studentService.getStudentProfile(studentId);

            // 从数据库获取对应的教育经历
            List<StudentStructuredResume> educationData = studentService.getStructuredResumeByType(profile.getId(),
                    "education");

            // 处理结果
            Map<String, Object> result = new HashMap<>();
            List<Map<String, Object>> educationExperiences = new ArrayList<>();

            // 处理教育经历
            for (StudentStructuredResume education : educationData) {
                if (education.getContent() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> content = (Map<String, Object>) education.getContent();
                    educationExperiences.add(content);
                }
            }

            result.put("educationExperiences", educationExperiences);

            log.info("成功获取学生教育经历数据，数量: {}", educationExperiences.size());
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("获取学生教育经历异常", e);
            return ApiResponse.businessError(e.getMessage());
        }
    }

    /**
     * 添加学生技能标签关联
     * 
     * @param requestData 请求数据，包含skillTagId
     * @return 处理结果
     */
    @PostMapping("/skill/add")
    public ApiResponse addStudentSkill(@RequestBody Map<String, Object> requestData) {
        // 获取当前登录用户ID
        Long currentUserId = SecurityUtils.getCurrentUserId();

        if (currentUserId == null) {
            return ApiResponse.unauthorized();
        }

        try {
            log.info("接收到添加技能标签请求: {}", requestData);

            if (!requestData.containsKey("skillTagId")) {
                return ApiResponse.businessError("未提供技能标签ID");
            }

            String skillTagName = String.valueOf(requestData.get("skillTagId"));

            // 获取学生档案
            StudentProfile profile = studentService.getStudentProfile(currentUserId);
            if (profile == null) {
                return ApiResponse.businessError("学生档案不存在");
            }

            // 获取或创建技能标签
            Long tagId = skillTagMapper.getOrCreateByName(skillTagName);
            if (tagId == null) {
                return ApiResponse.businessError("创建技能标签失败");
            }

            // 添加关联
            List<Long> skillTagIds = new ArrayList<>();
            skillTagIds.add(tagId);
            int result = studentSkillMapper.batchInsert(profile.getId(), skillTagIds);

            return result > 0 ? ApiResponse.success(true) : ApiResponse.businessError("添加失败");
        } catch (Exception e) {
            log.error("添加技能标签异常", e);
            return ApiResponse.businessError(e.getMessage());
        }
    }

    /**
     * 移除学生技能标签关联
     * 
     * @param requestData 请求数据，包含skillTagId
     * @return 处理结果
     */
    @PostMapping("/skill/remove")
    public ApiResponse removeStudentSkill(@RequestBody Map<String, Object> requestData) {
        // 获取当前登录用户ID
        Long currentUserId = SecurityUtils.getCurrentUserId();

        if (currentUserId == null) {
            return ApiResponse.unauthorized();
        }

        try {
            log.info("接收到移除技能标签请求: {}", requestData);

            if (!requestData.containsKey("skillTagId")) {
                return ApiResponse.businessError("未提供技能标签ID");
            }

            String skillTagName = String.valueOf(requestData.get("skillTagId"));

            // 获取学生档案
            StudentProfile profile = studentService.getStudentProfile(currentUserId);
            if (profile == null) {
                return ApiResponse.businessError("学生档案不存在");
            }

            // 获取技能标签ID
            SkillTag tag = skillTagMapper.selectOne(new LambdaQueryWrapper<SkillTag>()
                    .eq(SkillTag::getName, skillTagName));

            if (tag == null) {
                return ApiResponse.businessError("技能标签不存在");
            }

            // 删除关联
            int result = studentSkillMapper.delete(new LambdaQueryWrapper<StudentSkill>()
                    .eq(StudentSkill::getStudentProfileId, profile.getId())
                    .eq(StudentSkill::getSkillTagId, tag.getId()));

            return result > 0 ? ApiResponse.success(true) : ApiResponse.businessError("移除失败");
        } catch (Exception e) {
            log.error("移除技能标签异常", e);
            return ApiResponse.businessError(e.getMessage());
        }
    }

    /**
     * 获取学生的技能标签列表
     * 
     * @return 技能标签列表
     */
    @PostMapping("/skills")
    public ApiResponse getStudentSkills() {
        // 获取当前登录用户ID
        Long currentUserId = SecurityUtils.getCurrentUserId();

        if (currentUserId == null) {
            return ApiResponse.unauthorized();
        }

        try {
            log.info("获取学生技能标签列表, 用户ID: {}", currentUserId);

            // 获取学生的技能标签
            List<String> skillTags = studentService.getStudentSkillTags(currentUserId);

            return ApiResponse.success(skillTags);
        } catch (Exception e) {
            log.error("获取学生技能标签异常", e);
            return ApiResponse.businessError(e.getMessage());
        }
    }

    /**
     * 获取学生列表，支持多条件筛选
     * 
     * @param params 查询参数
     * @return 学生列表
     */
    @PostMapping("/list")
    public ApiResponse getStudentList(@RequestBody Map<String, Object> params) {
        try {
            LambdaQueryWrapper<StudentProfile> query = new LambdaQueryWrapper<>();
            if (params != null) {
                if (params.get("name") != null && !params.get("name").toString().isEmpty()) {
                    query.like(StudentProfile::getName, params.get("name").toString());
                }
                if (params.get("studentId") != null && !params.get("studentId").toString().isEmpty()) {
                    query.eq(StudentProfile::getUserId, params.get("studentId").toString());
                }
                if (params.get("major") != null && !params.get("major").toString().isEmpty()) {
                    query.like(StudentProfile::getMajor, params.get("major").toString());
                }
                if (params.get("grade") != null && !params.get("grade").toString().isEmpty()) {
                    query.eq(StudentProfile::getGrade, params.get("grade").toString());
                }
                if (params.get("counselorAssigned") != null) {
                    boolean assigned = Boolean.parseBoolean(params.get("counselorAssigned").toString());
                    if (assigned) {
                        query.isNotNull(StudentProfile::getCounselorId);
                    } else {
                        query.isNull(StudentProfile::getCounselorId);
                    }
                }
            }
            List<StudentProfile> list = studentService.list(query);
            List<Map<String, Object>> voList = new ArrayList<>();
            for (StudentProfile s : list) {
                Map<String, Object> vo = new HashMap<>();
                vo.put("id", s.getId());
                vo.put("userId", s.getUserId());
                vo.put("name", s.getName());
                vo.put("studentId", s.getId());
                vo.put("major", s.getMajor());
                vo.put("grade", s.getGrade());
                vo.put("counselorId", s.getCounselorId());
                String counselorName = null;
                if (s.getCounselorId() != null) {
                    CounselorProfile counselor = counselorProfileMapper.selectById(s.getCounselorId());
                    if (counselor != null) {
                        counselorName = counselor.getName();
                    }
                }
                vo.put("counselorName", counselorName);
                voList.add(vo);
            }
            Map<String, Object> body = new HashMap<>();
            body.put("list", voList);
            return ApiResponse.success(body);
        } catch (Exception e) {
            log.error("获取学生列表异常", e);
            return ApiResponse.businessError(e.getMessage());
        }
    }

    /**
     * 批量分配学生到辅导员
     * 
     * @param params {counselorId, studentIds}
     * @return 处理结果
     */
    @PostMapping("/assign-counselor")
    public ApiResponse assignStudentsToCounselor(@RequestBody Map<String, Object> params) {
        try {
            if (params == null || !params.containsKey("counselorId") || !params.containsKey("studentIds")) {
                return ApiResponse.businessError("参数不完整");
            }
            Long counselorId = null;
            Object counselorIdObj = params.get("counselorId");
            if (counselorIdObj instanceof Number) {
                counselorId = ((Number) counselorIdObj).longValue();
            } else if (counselorIdObj instanceof String) {
                counselorId = Long.parseLong((String) counselorIdObj);
            }
            @SuppressWarnings("unchecked")
            List<Object> studentIdsRaw = (List<Object>) params.get("studentIds");
            if (studentIdsRaw == null || studentIdsRaw.isEmpty()) {
                return ApiResponse.businessError("未选择学生");
            }
            List<Long> studentIds = new ArrayList<>();
            for (Object idObj : studentIdsRaw) {
                if (idObj instanceof Number) {
                    studentIds.add(((Number) idObj).longValue());
                } else if (idObj instanceof String) {
                    studentIds.add(Long.parseLong((String) idObj));
                }
            }
            // 批量更新
            int updated = 0;
            for (Long studentId : studentIds) {
                StudentProfile profile = studentService.getById(studentId);
                if (profile != null) {
                    profile.setCounselorId(counselorId);
                    boolean ok = studentService.updateById(profile);
                    if (ok)
                        updated++;
                }
            }
            return ApiResponse.success(updated);
        } catch (Exception e) {
            log.error("批量分配学生到辅导员异常", e);
            return ApiResponse.businessError(e.getMessage());
        }
    }
}