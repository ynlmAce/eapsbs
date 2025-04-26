package com.bs.eaps.controller;

import com.bs.eaps.common.ApiResponse;
import com.bs.eaps.entity.CompanyProfile;
import com.bs.eaps.service.CompanyService;
import com.bs.eaps.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 企业模块控制器
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    /**
     * 获取企业资料
     */
    @PostMapping("/company/profile")
    public ApiResponse getCompanyProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        CompanyProfile profile = companyService.getCompanyProfile(userId);
        return ApiResponse.success(profile);
    }

    /**
     * 更新企业资料
     */
    @PostMapping("/company/profile/update")
    public ApiResponse updateCompanyProfile(@RequestBody CompanyProfile profile) {
        Long userId = SecurityUtils.getCurrentUserId();
        profile.setUserId(userId);
        boolean result = companyService.updateCompanyProfile(profile);
        return ApiResponse.success(result);
    }

    /**
     * 上传企业Logo
     */
    @PostMapping("/company/logo/upload")
    public ApiResponse uploadCompanyLogo(@RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtils.getCurrentUserId();
        Object logoFile = companyService.uploadCompanyLogo(userId, file);
        return ApiResponse.success(logoFile);
    }

    /**
     * 上传企业营业执照
     */
    @PostMapping("/company/license/upload")
    public ApiResponse uploadCompanyLicense(@RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtils.getCurrentUserId();
        Object licenseFile = companyService.uploadCompanyLicense(userId, file);
        return ApiResponse.success(licenseFile);
    }

    /**
     * 将学生加入或移出黑名单
     * 
     * @param request 请求参数，包含studentId、reason和isBlacklisted
     * @return 处理结果
     */
    @PostMapping("/company/student/blacklist")
    public ApiResponse toggleStudentBlacklist(@RequestBody Map<String, Object> request) {
        Long companyUserId = SecurityUtils.getCurrentUserId();

        // 获取参数
        Long studentId = request.containsKey("studentId") ? ((Number) request.get("studentId")).longValue() : null;
        String reason = request.containsKey("reason") ? (String) request.get("reason") : null;
        Boolean isBlacklisted = request.containsKey("isBlacklisted") ? (Boolean) request.get("isBlacklisted") : null;

        // 参数校验
        if (studentId == null || isBlacklisted == null) {
            return ApiResponse.businessError("缺少必要参数");
        }

        // 调用服务处理黑名单操作
        boolean result = companyService.toggleStudentBlacklist(companyUserId, studentId, reason, isBlacklisted);

        return ApiResponse.success(result);
    }

    /**
     * 获取企业已拉黑的学生列表
     * 
     * @param request 请求参数，包含分页信息
     * @return 黑名单学生列表
     */
    @PostMapping("/company/blacklisted-students")
    public ApiResponse getBlacklistedStudents(@RequestBody(required = false) Map<String, Object> request) {
        Long companyUserId = SecurityUtils.getCurrentUserId();

        // 获取分页参数，默认第1页，每页10条
        int page = 1;
        int limit = 10;

        if (request != null) {
            if (request.containsKey("page") && request.get("page") instanceof Number) {
                page = ((Number) request.get("page")).intValue();
            }
            if (request.containsKey("limit") && request.get("limit") instanceof Number) {
                limit = ((Number) request.get("limit")).intValue();
            }
        }

        // 调用服务获取黑名单列表
        return ApiResponse.success(companyService.getBlacklistedStudents(companyUserId, page, limit));
    }

    /**
     * 处理学生申请
     * 
     * @param request 请求参数，包含申请ID、处理状态和备注
     * @return 处理结果
     */
    @PostMapping("/company/application/process")
    public ApiResponse processApplication(@RequestBody Map<String, Object> request) {
        Long companyUserId = SecurityUtils.getCurrentUserId();

        // 获取参数
        Long applicationId = request.containsKey("applicationId") ? ((Number) request.get("applicationId")).longValue()
                : null;
        String status = request.containsKey("status") ? (String) request.get("status") : null;
        String notes = request.containsKey("notes") ? (String) request.get("notes") : "";

        // 参数校验
        if (applicationId == null || status == null) {
            return ApiResponse.businessError("缺少必要参数");
        }

        log.info("处理学生申请: companyUserId={}, applicationId={}, status={}", companyUserId, applicationId, status);

        // 调用服务处理申请
        boolean result = companyService.processApplication(companyUserId, applicationId, status, notes);

        return ApiResponse.success(result);
    }

    /**
     * 收藏/取消收藏学生
     * 
     * @param request 请求参数，包含studentId和isFavorite
     * @return 处理结果
     */
    @PostMapping("/company/student/favorite")
    public ApiResponse toggleFavoriteStudent(@RequestBody Map<String, Object> request) {
        Long companyUserId = SecurityUtils.getCurrentUserId();

        // 获取参数
        Long studentId = request.containsKey("studentId") ? ((Number) request.get("studentId")).longValue() : null;
        Boolean isFavorite = request.containsKey("isFavorite") ? (Boolean) request.get("isFavorite") : null;

        // 参数校验
        if (studentId == null || isFavorite == null) {
            return ApiResponse.businessError("缺少必要参数");
        }

        log.info("收藏/取消收藏学生: companyUserId={}, studentId={}, isFavorite={}", companyUserId, studentId, isFavorite);

        // 调用服务处理收藏操作
        boolean result = companyService.toggleFavoriteStudent(companyUserId, studentId, isFavorite);

        return ApiResponse.success(result);
    }

    /**
     * 获取收藏的学生列表
     * 
     * @param request 请求参数，包含分页信息
     * @return 收藏学生列表
     */
    @PostMapping("/company/favorite-students")
    public ApiResponse getFavoriteStudents(@RequestBody(required = false) Map<String, Object> request) {
        Long companyUserId = SecurityUtils.getCurrentUserId();

        // 获取分页参数，默认第1页，每页10条
        int page = 1;
        int limit = 10;

        if (request != null) {
            if (request.containsKey("page") && request.get("page") instanceof Number) {
                page = ((Number) request.get("page")).intValue();
            }
            if (request.containsKey("limit") && request.get("limit") instanceof Number) {
                limit = ((Number) request.get("limit")).intValue();
            }
        }

        // 调用服务获取收藏学生列表
        return ApiResponse.success(companyService.getFavoriteStudents(companyUserId, page, limit));
    }

    /**
     * 通过岗位ID获取企业用户ID
     */
    @PostMapping("/user-id-by-job")
    public ApiResponse getCompanyUserIdByJobId(@RequestBody Map<String, Long> params) {
        Long jobId = params.get("jobId");
        if (jobId == null) {
            return ApiResponse.error(400, "岗位ID不能为空");
        }
        return ApiResponse.success(companyService.getCompanyUserIdByJobId(jobId));
    }
}