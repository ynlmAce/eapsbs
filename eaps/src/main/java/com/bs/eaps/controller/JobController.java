package com.bs.eaps.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bs.eaps.common.ApiResponse;
import com.bs.eaps.utils.SecurityUtils;
import com.bs.eaps.dto.JobTagDTO;
import com.bs.eaps.dto.job.*;
import com.bs.eaps.entity.Company;
import com.bs.eaps.entity.JobTag;
import com.bs.eaps.entity.WelfareTag;
import com.bs.eaps.entity.Application;
import com.bs.eaps.mapper.CompanyMapper;
import com.bs.eaps.mapper.ApplicationMapper;
import com.bs.eaps.service.JobService;
import com.bs.eaps.service.JobTagService;
import com.bs.eaps.service.JobTagRelationService;
import com.bs.eaps.service.WelfareTagService;
import com.bs.eaps.service.JobWelfareService;
import com.bs.eaps.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

/**
 * 岗位模块控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/job")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final JobTagService jobTagService;
    private final WelfareTagService welfareTagService;
    private final JobTagRelationService jobTagRelationService;
    private final JobWelfareService jobWelfareService;
    private final CompanyMapper companyMapper;
    private final CompanyService companyService;
    private final ApplicationMapper applicationMapper;

    /**
     * 获取岗位列表
     */
    @PostMapping("/list")
    public ApiResponse getJobList(@RequestBody JobQueryDTO queryDTO) {
        return ApiResponse.success(jobService.getJobList(queryDTO));
    }

    /**
     * 获取岗位详情
     */
    @PostMapping("/detail")
    public ApiResponse getJobDetail(@RequestBody JobDetailQueryDTO queryDTO) {
        Map<String, Object> jobDetail = (Map<String, Object>) jobService.getJobDetail(queryDTO.getJobId());

        // 获取并添加标签信息
        try {
            Long jobId = queryDTO.getJobId();

            // 获取岗位标签
            List<String> jobTags = jobTagRelationService.getTagNamesByJobId(jobId);
            jobDetail.put("jobTags", jobTags);

            // 获取福利标签
            List<String> welfareTags = jobWelfareService.getTagNamesByJobId(jobId);
            jobDetail.put("welfareTags", welfareTags);
        } catch (Exception e) {
            // 记录错误但不影响正常响应
            log.error("获取标签信息失败", e);
        }

        return ApiResponse.success(jobDetail);
    }

    /**
     * 发布新岗位
     */
    @PostMapping("/publish")
    public ApiResponse publishJob(@RequestBody JobPublishDTO jobDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(jobService.publishJob(userId, jobDTO));
    }

    /**
     * 创建新岗位 (与publish接口功能相同，作为别名提供)
     */
    @PostMapping("/create")
    public ApiResponse<String> createJob(@RequestBody JobDTO jobDTO) {
        log.info("收到岗位发布请求, userId: {}, jobDTO: {}", SecurityUtils.getCurrentUserId(), jobDTO);

        // 检查当前用户是否有关联的公司信息
        Long userId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<Company> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Company::getUserId, userId);
        Company company = companyMapper.selectOne(queryWrapper);

        if (company == null) {
            log.error("用户 ID: {} 没有关联的公司信息，无法发布岗位", userId);
            return ApiResponse.error(400, "请先完善公司信息后再发布岗位");
        }

        log.info("找到公司信息: companyId={}, companyName={}", company.getId(), company.getName());

        // 设置公司ID到岗位DTO中
        jobDTO.setCompanyId(company.getId());

        return jobService.publishJob(userId, jobDTO);
    }

    /**
     * 学生投递简历
     */
    @PostMapping("/apply")
    public ApiResponse applyJob(@RequestBody JobApplicationDTO applicationDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(jobService.applyJob(userId, applicationDTO));
    }

    /**
     * 获取学生投递记录
     */
    @PostMapping("/application/list/student")
    public ApiResponse getStudentApplicationList(@RequestBody StudentApplicationQueryDTO queryDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(jobService.getStudentApplicationList(userId, queryDTO));
    }

    /**
     * 获取企业收到的申请
     */
    @PostMapping("/application/list/company")
    public ApiResponse getCompanyApplicationList(@RequestBody CompanyApplicationQueryDTO queryDTO) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("获取企业收到的申请列表, userId={}, queryDTO={}", userId, queryDTO);

        // 首先根据用户ID查找关联的企业ID
        LambdaQueryWrapper<Company> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Company::getUserId, userId);
        Company company = companyMapper.selectOne(queryWrapper);

        if (company == null) {
            log.error("用户ID: {} 没有关联的企业信息", userId);
            return ApiResponse.error(400, "找不到企业信息，请先完善企业资料");
        }

        Long companyId = company.getId();
        log.info("找到企业ID: {}, 企业名称: {}", companyId, company.getName());

        return ApiResponse.success(jobService.getCompanyApplicationList(companyId, queryDTO));
    }

    /**
     * 处理学生申请
     * 
     * @param request 请求参数，包含申请ID、处理状态和备注
     * @return 处理结果
     */
    @PostMapping("/application/process")
    public ApiResponse processApplication(@RequestBody Map<String, Object> request) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("处理学生申请: userId={}, request={}", userId, request);

        // 获取参数
        Long applicationId = request.containsKey("applicationId") ? ((Number) request.get("applicationId")).longValue()
                : null;
        String status = request.containsKey("status") ? (String) request.get("status") : null;
        String notes = request.containsKey("notes") ? (String) request.get("notes") : "";

        // 参数校验
        if (applicationId == null || status == null) {
            return ApiResponse.businessError("缺少必要参数");
        }

        // 调用CompanyService处理申请
        try {
            boolean result = companyService.processApplication(userId, applicationId, status, notes);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("处理学生申请失败: applicationId={}", applicationId, e);
            return ApiResponse.businessError("处理申请失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有岗位标签
     */
    @PostMapping("/tags")
    public ApiResponse getJobTags() {
        List<JobTag> tags = jobTagService.getAllTags();
        List<JobTagDTO> tagDTOs = tags.stream()
                .map(tag -> {
                    JobTagDTO dto = new JobTagDTO();
                    dto.setId(tag.getId());
                    dto.setName(tag.getName());
                    dto.setCreatedAt(tag.getCreatedAt());
                    return dto;
                })
                .collect(Collectors.toList());
        return ApiResponse.success(tagDTOs);
    }

    /**
     * 获取所有福利标签
     */
    @PostMapping("/welfare-tags")
    public ApiResponse getWelfareTags() {
        List<WelfareTag> tags = welfareTagService.getAllTags();
        return ApiResponse.success(tags);
    }

    /**
     * 学生撤回申请
     * 
     * @param request 包含applicationId
     * @return 处理结果
     */
    @PostMapping("/application/withdraw")
    public ApiResponse withdrawApplication(@RequestBody Map<String, Object> request) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("学生撤回申请: userId={}, request={}", userId, request);

        // 获取参数
        Long applicationId = request.containsKey("applicationId") ? ((Number) request.get("applicationId")).longValue()
                : null;

        // 参数校验
        if (applicationId == null) {
            return ApiResponse.businessError("缺少必要参数");
        }

        try {
            // 查询申请记录
            Application application = applicationMapper.selectById(applicationId);
            if (application == null) {
                log.error("申请记录不存在: applicationId={}", applicationId);
                return ApiResponse.businessError("申请记录不存在");
            }

            // 验证是否是当前用户的申请
            Long studentId = jobService.getStudentIdByUserId(userId);
            if (!application.getStudentId().equals(studentId)) {
                log.error("无权操作此申请: userId={}, applicationId={}, studentId={}, applicationStudentId={}",
                        userId, applicationId, studentId, application.getStudentId());
                return ApiResponse.businessError("无权操作此申请");
            }

            // 检查申请状态是否允许撤回
            // 通常只有"pending"或"viewed"状态的申请才能撤回
            String status = application.getStatus();
            if (!("pending".equals(status) || "viewed".equals(status))) {
                log.error("当前状态不允许撤回: applicationId={}, status={}", applicationId, status);
                return ApiResponse.businessError("当前状态不允许撤回申请");
            }

            // 从数据库中删除此申请
            int result = applicationMapper.deleteById(applicationId);

            if (result > 0) {
                log.info("申请撤回成功: applicationId={}", applicationId);
                return ApiResponse.success(true);
            } else {
                log.error("申请撤回失败: applicationId={}", applicationId);
                return ApiResponse.businessError("撤回申请失败");
            }
        } catch (Exception e) {
            log.error("撤回申请发生异常: applicationId={}", applicationId, e);
            return ApiResponse.businessError("撤回申请失败: " + e.getMessage());
        }
    }
}