package com.bs.eaps.service;

import com.bs.eaps.entity.CompanyProfile;
import com.bs.eaps.entity.CompanyLicenseFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 企业服务接口
 */
public interface CompanyService {

    /**
     * 获取企业资料
     * 
     * @param userId 用户ID
     * @return 企业资料
     */
    CompanyProfile getCompanyProfile(Long userId);

    /**
     * 更新企业资料
     * 
     * @param profile 企业资料
     * @return 是否更新成功
     */
    boolean updateCompanyProfile(CompanyProfile profile);

    /**
     * 上传企业Logo
     * 
     * @param userId 用户ID
     * @param file   Logo文件
     * @return 文件信息
     */
    Object uploadCompanyLogo(Long userId, MultipartFile file);

    /**
     * 上传企业营业执照
     * 
     * @param userId 用户ID
     * @param file   营业执照文件
     * @return 文件信息
     */
    CompanyLicenseFile uploadCompanyLicense(Long userId, MultipartFile file);

    /**
     * 将学生加入或移出黑名单
     * 
     * @param companyUserId 企业用户ID
     * @param studentId     学生ID
     * @param reason        拉黑原因
     * @param isBlacklisted 是否加入黑名单
     * @return 操作结果
     */
    boolean toggleStudentBlacklist(Long companyUserId, Long studentId, String reason, Boolean isBlacklisted);

    /**
     * 获取企业已拉黑的学生列表
     * 
     * @param companyUserId 企业用户ID
     * @param page          页码
     * @param limit         每页数量
     * @return 黑名单学生列表
     */
    Map<String, Object> getBlacklistedStudents(Long companyUserId, int page, int limit);

    /**
     * 处理学生申请
     * 
     * @param companyUserId 企业用户ID
     * @param applicationId 申请ID
     * @param status        处理状态
     * @param notes         处理备注
     * @return 操作结果
     */
    boolean processApplication(Long companyUserId, Long applicationId, String status, String notes);

    /**
     * 收藏/取消收藏学生
     * 
     * @param companyUserId 企业用户ID
     * @param studentId     学生ID
     * @param isFavorite    是否收藏
     * @return 操作结果
     */
    boolean toggleFavoriteStudent(Long companyUserId, Long studentId, Boolean isFavorite);

    /**
     * 获取企业收藏的学生列表
     * 
     * @param companyUserId 企业用户ID
     * @param page          页码
     * @param limit         每页数量
     * @return 收藏学生列表
     */
    Map<String, Object> getFavoriteStudents(Long companyUserId, int page, int limit);

    /**
     * 通过岗位ID获取企业用户ID
     * 
     * @param jobId 岗位ID
     * @return 企业用户ID
     */
    Long getCompanyUserIdByJobId(Long jobId);

    /**
     * 获取企业详情通过企业ID
     * 
     * @param companyId 企业ID
     * @return 企业详细信息
     */
    Map<String, Object> getCompanyDetailById(Long companyId);
}