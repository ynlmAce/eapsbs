package com.bs.eaps.service;

import com.bs.eaps.entity.CounselorTask;

/**
 * 辅导员任务服务接口
 */
public interface CounselorTaskService {

    /**
     * 创建企业认证任务
     * 
     * @param companyId 企业ID
     * @param title     任务标题
     * @return 创建的任务ID
     */
    Long createCompanyCertificationTask(Long companyId, String title);

    /**
     * 创建岗位审核任务
     * 
     * @param jobId 岗位ID
     * @param title 任务标题
     * @return 创建的任务ID
     */
    Long createJobAuditTask(Long jobId, String title);

    /**
     * 创建举报处理任务
     * 
     * @param reportedItemType 举报项类型
     * @param reportedItemId   举报项ID
     * @param title            任务标题
     * @return 创建的任务ID
     */
    Long createReportHandlingTask(String reportedItemType, Long reportedItemId, String title);
}