package com.bs.eaps.service.impl;

import com.bs.eaps.entity.CounselorTask;
import com.bs.eaps.mapper.CounselorTaskMapper;
import com.bs.eaps.service.CounselorTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 辅导员任务服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CounselorTaskServiceImpl implements CounselorTaskService {

    private final CounselorTaskMapper counselorTaskMapper;

    /**
     * 创建企业认证任务
     */
    @Override
    @Transactional
    public Long createCompanyCertificationTask(Long companyId, String title) {
        CounselorTask task = new CounselorTask();
        task.setType("companyCertification");
        task.setTargetItemType("Company");
        task.setTargetItemId(companyId);
        task.setTitle(title);
        task.setCreatedAt(LocalDateTime.now());
        task.setStatus("pending");
        task.setPriority("normal");

        counselorTaskMapper.insert(task);
        log.info("创建企业认证任务成功：companyId={}, taskId={}", companyId, task.getId());
        return task.getId();
    }

    /**
     * 创建岗位审核任务
     */
    @Override
    @Transactional
    public Long createJobAuditTask(Long jobId, String title) {
        CounselorTask task = new CounselorTask();
        task.setType("jobAudit");
        task.setTargetItemType("Job");
        task.setTargetItemId(jobId);
        task.setTitle(title);
        task.setCreatedAt(LocalDateTime.now());
        task.setStatus("pending");
        task.setPriority("normal");

        counselorTaskMapper.insert(task);
        log.info("创建岗位审核任务成功：jobId={}, taskId={}", jobId, task.getId());
        return task.getId();
    }

    /**
     * 创建举报处理任务
     */
    @Override
    @Transactional
    public Long createReportHandlingTask(String reportedItemType, Long reportedItemId, String title) {
        CounselorTask task = new CounselorTask();
        task.setType("reportHandling");
        task.setTargetItemType(reportedItemType);
        task.setTargetItemId(reportedItemId);
        task.setTitle(title);
        task.setCreatedAt(LocalDateTime.now());
        task.setStatus("pending");
        task.setPriority("high"); // 举报处理通常优先级较高

        counselorTaskMapper.insert(task);
        log.info("创建举报处理任务成功：reportedItemType={}, reportedItemId={}, taskId={}",
                reportedItemType, reportedItemId, task.getId());
        return task.getId();
    }
}