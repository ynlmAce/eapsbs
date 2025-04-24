package com.jobhelper.controller;

import com.jobhelper.common.ApiResponse;
import com.jobhelper.entity.CompanyProfile;
import com.jobhelper.service.CompanyService;
import com.jobhelper.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 企业模块控制器
 */
@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    /**
     * 获取企业资料
     */
    @PostMapping("/profile")
    public ApiResponse getCompanyProfile() {
        Long companyId = SecurityUtils.getCurrentUserId();
        CompanyProfile profile = companyService.getCompanyProfile(companyId);
        return ApiResponse.success(profile);
    }

    /**
     * 更新企业资料
     */
    @PostMapping("/profile/update")
    public ApiResponse updateCompanyProfile(@RequestBody CompanyProfile profile) {
        Long companyId = SecurityUtils.getCurrentUserId();
        profile.setId(companyId);
        boolean result = companyService.updateCompanyProfile(profile);
        return ApiResponse.success(result);
    }

    /**
     * 上传企业营业执照
     */
    @PostMapping("/license/upload")
    public ApiResponse uploadCompanyLicense(MultipartFile file) {
        Long companyId = SecurityUtils.getCurrentUserId();
        Object licenseFile = companyService.uploadCompanyLicense(companyId, file);
        return ApiResponse.success(licenseFile);
    }
}