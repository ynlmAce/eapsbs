package com.bs.eaps.service;

import com.bs.eaps.entity.CompanyProfile;
import com.bs.eaps.entity.CompanyLicenseFile;
import org.springframework.web.multipart.MultipartFile;

/**
 * 企业服务接口
 */
public interface CompanyService {

    /**
     * 获取企业资料
     * 
     * @param companyId 企业ID
     * @return 企业资料
     */
    CompanyProfile getCompanyProfile(Long companyId);

    /**
     * 更新企业资料
     * 
     * @param profile 企业资料
     * @return 是否更新成功
     */
    boolean updateCompanyProfile(CompanyProfile profile);

    /**
     * 上传企业营业执照
     * 
     * @param companyId 企业ID
     * @param file      营业执照文件
     * @return 文件信息
     */
    CompanyLicenseFile uploadCompanyLicense(Long companyId, MultipartFile file);
}