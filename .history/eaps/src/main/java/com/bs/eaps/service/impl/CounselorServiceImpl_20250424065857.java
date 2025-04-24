package com.bs.eaps.service.impl;

import com.bs.eaps.dto.counselor.TaskCountDTO;
import com.bs.eaps.dto.counselor.TaskDTO;
import com.bs.eaps.dto.counselor.TaskProcessDTO;
import com.bs.eaps.dto.counselor.TaskProcessResultDTO;
import com.bs.eaps.dto.counselor.OperationHistoryDTO;
import com.bs.eaps.dto.common.PageRequestDTO;
import com.bs.eaps.dto.common.PageResultDTO;
import com.bs.eaps.entity.CounselorTask;
import com.bs.eaps.entity.CounselorOperationLog;
import com.bs.eaps.entity.Company;
import com.bs.eaps.mapper.CounselorTaskMapper;
import com.bs.eaps.mapper.CounselorOperationLogMapper;
import com.bs.eaps.mapper.CompanyMapper;
import com.bs.eaps.service.CounselorService;
import com.bs.eaps.utils.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 辅导员服务实现类
 */
@Service
@RequiredArgsConstructor
public class CounselorServiceImpl implements CounselorService {

    private final CounselorTaskMapper counselorTaskMapper;
    private final CounselorOperationLogMapper operationLogMapper;
    private final CompanyMapper companyMapper;

    /**
     * 获取辅导员待处理的各类任务数量统计
     */
    @Override
    public TaskCountDTO getTasksCount() {
        TaskCountDTO result = new TaskCountDTO();

        // 统计企业认证任务数量
        Integer companyCertCount = counselorTaskMapper.countByType("companyCertification");
        // 统计岗位审核任务数量
        Integer jobAuditCount = counselorTaskMapper.countByType("jobAudit");
        // 统计举报处理任务数量
        Integer reportHandlingCount = counselorTaskMapper.countByType("reportHandling");
        // 统计任务总数
        Integer totalCount = counselorTaskMapper.countPendingTasks();

        result.setCompanyCertification(companyCertCount);
        result.setJobAudit(jobAuditCount);
        result.setReportHandling(reportHandlingCount);
        result.setTotal(totalCount);

        return result;
    }

    /**
     * 获取辅导员待处理的特定类型任务列表
     */
    @Override
    public PageResultDTO<TaskDTO> getTasksList(String type, PageRequestDTO pageRequest) {
        LambdaQueryWrapper<CounselorTask> queryWrapper = new LambdaQueryWrapper<>();

        // 根据任务类型筛选
        if (StringUtils.hasText(type) && !"all".equals(type)) {
            queryWrapper.eq(CounselorTask::getType, type);
        }

        // 只查询待处理的任务
        queryWrapper.eq(CounselorTask::getStatus, "pending");

        // 根据优先级和创建时间排序
        queryWrapper.orderByDesc(CounselorTask::getPriority)
                .orderByAsc(CounselorTask::getCreatedAt);

        // 分页查询
        Page<CounselorTask> page = new Page<>(pageRequest.getPage(), pageRequest.getPageSize());
        IPage<CounselorTask> taskPage = counselorTaskMapper.selectPage(page, queryWrapper);

        // 转换为DTO
        List<TaskDTO> taskDTOList = taskPage.getRecords().stream().map(task -> {
            TaskDTO taskDTO = new TaskDTO();
            BeanUtils.copyProperties(task, taskDTO);

            // 获取公司名称
            if (task.getCompanyId() != null) {
                Company company = companyMapper.selectById(task.getCompanyId());
                if (company != null) {
                    taskDTO.setCompanyName(company.getName());
                }
            }

            return taskDTO;
        }).collect(Collectors.toList());

        return PageResultDTO.of(taskPage.getTotal(), taskDTOList);
    }

    /**
     * 辅导员处理特定任务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskProcessResultDTO processTask(TaskProcessDTO taskProcessDTO) {
        TaskProcessResultDTO result = new TaskProcessResultDTO();

        // 查询任务
        CounselorTask task = counselorTaskMapper.selectById(taskProcessDTO.getTaskId());
        if (task == null) {
            result.setSuccess(false);
            return result;
        }

        // 更新任务状态
        task.setStatus("completed");
        task.setCounselorId(SecurityUtils.getCurrentUserId());
        task.setNotes(taskProcessDTO.getNotes());
        counselorTaskMapper.updateById(task);

        // 记录操作日志
        CounselorOperationLog operationLog = new CounselorOperationLog();
        operationLog.setCounselorId(SecurityUtils.getCurrentUserId());
        operationLog.setOperationType(task.getType());
        operationLog.setTargetItemType(task.getTargetItemType());
        operationLog.setTargetItemId(task.getTargetItemId());
        operationLog.setResult(taskProcessDTO.getAction());
        operationLog.setReason(taskProcessDTO.getReason());
        operationLog.setNotes(taskProcessDTO.getNotes());
        operationLog.setTimestamp(LocalDateTime.now());
        operationLogMapper.insert(operationLog);

        // TODO: 根据任务类型和处理结果执行相应的业务操作
        // 例如：更新企业认证状态、岗位状态、处理举报等

        result.setSuccess(true);
        result.setOperationId(operationLog.getId());
        result.setProcessedAt(operationLog.getTimestamp());

        return result;
    }

    /**
     * 获取辅导员的历史操作记录
     */
    @Override
    public PageResultDTO<OperationHistoryDTO> getOperationsHistory(LocalDate startDate, LocalDate endDate,
            String type, PageRequestDTO pageRequest) {
        LambdaQueryWrapper<CounselorOperationLog> queryWrapper = new LambdaQueryWrapper<>();

        // 根据操作类型筛选
        if (StringUtils.hasText(type) && !"all".equals(type)) {
            queryWrapper.eq(CounselorOperationLog::getOperationType, type);
        }

        // 根据当前登录的辅导员ID筛选
        queryWrapper.eq(CounselorOperationLog::getCounselorId, SecurityUtils.getCurrentUserId());

        // 根据日期范围筛选
        if (startDate != null) {
            queryWrapper.ge(CounselorOperationLog::getTimestamp, LocalDateTime.of(startDate, LocalTime.MIN));
        }
        if (endDate != null) {
            queryWrapper.le(CounselorOperationLog::getTimestamp, LocalDateTime.of(endDate, LocalTime.MAX));
        }

        // 按时间倒序排序
        queryWrapper.orderByDesc(CounselorOperationLog::getTimestamp);

        // 分页查询
        Page<CounselorOperationLog> page = new Page<>(pageRequest.getPage(), pageRequest.getPageSize());
        IPage<CounselorOperationLog> logPage = operationLogMapper.selectPage(page, queryWrapper);

        // 转换为DTO
        List<OperationHistoryDTO> operationDTOList = logPage.getRecords().stream().map(log -> {
            OperationHistoryDTO operationDTO = new OperationHistoryDTO();
            BeanUtils.copyProperties(log, operationDTO);

            // 构建目标名称
            StringBuilder targetNameBuilder = new StringBuilder();

            // TODO: 根据目标项类型查询对应的名称
            // 这里简化处理，实际应该根据targetItemType查询不同的表

            operationDTO.setTargetName(targetNameBuilder.toString());
            operationDTO.setAction(log.getResult());

            return operationDTO;
        }).collect(Collectors.toList());

        return PageResultDTO.of(logPage.getTotal(), operationDTOList);
    }
}