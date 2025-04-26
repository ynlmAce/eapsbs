package com.bs.eaps.service.impl;

import com.bs.eaps.dto.counselor.TaskCountDTO;
import com.bs.eaps.dto.counselor.TaskDTO;
import com.bs.eaps.dto.counselor.TaskProcessDTO;
import com.bs.eaps.dto.counselor.TaskProcessResultDTO;
import com.bs.eaps.dto.counselor.OperationHistoryDTO;
import com.bs.eaps.dto.counselor.CounselorProfileDTO;
import com.bs.eaps.dto.common.PageRequestDTO;
import com.bs.eaps.dto.common.PageResultDTO;
import com.bs.eaps.entity.CounselorTask;
import com.bs.eaps.entity.CounselorOperationLog;
import com.bs.eaps.entity.Company;
import com.bs.eaps.entity.CounselorProfile;
import com.bs.eaps.entity.JobPosting;
import com.bs.eaps.mapper.CounselorTaskMapper;
import com.bs.eaps.mapper.CounselorOperationLogMapper;
import com.bs.eaps.mapper.CompanyMapper;
import com.bs.eaps.mapper.CounselorProfileMapper;
import com.bs.eaps.mapper.JobPostingMapper;
import com.bs.eaps.service.CounselorService;
import com.bs.eaps.utils.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Service
@RequiredArgsConstructor
public class CounselorServiceImpl implements CounselorService {

    private final CounselorTaskMapper counselorTaskMapper;
    private final CounselorOperationLogMapper operationLogMapper;
    private final CompanyMapper companyMapper;
    private final CounselorProfileMapper counselorProfileMapper;
    private final JobPostingMapper jobPostingMapper;

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

            // 获取公司名称（根据目标项类型）
            if ("Company".equals(task.getTargetItemType())) {
                // 如果目标是公司，直接查询公司信息
                Company company = companyMapper.selectById(task.getTargetItemId());
                if (company != null) {
                    taskDTO.setCompanyName(company.getName());
                }
            } else if ("Job".equals(task.getTargetItemType())) {
                // 如果目标是岗位，需要先查询岗位，再查询关联的公司
                JobPosting job = jobPostingMapper.selectById(task.getTargetItemId());
                if (job != null) {
                    Company company = companyMapper.selectById(job.getCompanyId());
                    if (company != null) {
                        taskDTO.setCompanyName(company.getName());
                    }
                }
            }
            // 其他类型的目标项可能也需要关联企业，根据实际情况添加

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

        // 获取当前登录的辅导员用户ID
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("处理任务，当前登录用户ID: {}", currentUserId);

        if (currentUserId == null) {
            log.error("处理任务失败：未登录或登录已过期");
            result.setSuccess(false);
            return result;
        }

        // 查询辅导员档案获取辅导员ID
        CounselorProfile profile = counselorProfileMapper.selectOne(
                new LambdaQueryWrapper<CounselorProfile>()
                        .eq(CounselorProfile::getUserId, currentUserId));

        if (profile == null) {
            log.error("处理任务失败：未找到对应的辅导员档案记录");
            result.setSuccess(false);
            return result;
        }

        // 更新任务状态，使用辅导员档案ID
        task.setStatus("completed");
        task.setCounselorId(profile.getId()); // 使用辅导员ID而不是用户ID
        task.setNotes(taskProcessDTO.getNotes());
        counselorTaskMapper.updateById(task);

        // 记录操作日志
        CounselorOperationLog operationLog = new CounselorOperationLog();
        operationLog.setCounselorId(profile.getId()); // 使用辅导员ID而不是用户ID
        operationLog.setOperationType(task.getType());
        operationLog.setTargetItemType(task.getTargetItemType());
        operationLog.setTargetItemId(task.getTargetItemId());
        operationLog.setResult(taskProcessDTO.getAction());
        operationLog.setReason(taskProcessDTO.getReason());
        operationLog.setNotes(taskProcessDTO.getNotes());
        operationLog.setTimestamp(LocalDateTime.now());
        operationLogMapper.insert(operationLog);

        // 根据任务类型和处理结果执行相应的业务操作
        String action = taskProcessDTO.getAction();
        if ("companyCertification".equals(task.getType())) {
            // 处理企业认证任务
            Long companyId = task.getTargetItemId();
            Company company = companyMapper.selectById(companyId);

            if (company != null) {
                if ("approve".equals(action)) {
                    // 审核通过，更新企业状态为已认证
                    company.setCertificationStatus("certified");
                    // 设置认证有效期为一年
                    company.setCertificationExpiryDate(LocalDateTime.now().plusYears(1));
                    log.info("企业认证审核通过: companyId={}, 有效期至={}", companyId, company.getCertificationExpiryDate());
                } else if ("reject".equals(action)) {
                    // 审核不通过，更新企业状态为认证失败
                    company.setCertificationStatus("rejected");
                    log.info("企业认证审核不通过: companyId={}, 原因={}", companyId, taskProcessDTO.getReason());
                }
                // 保存更新
                companyMapper.updateById(company);
            } else {
                log.error("处理企业认证任务失败: 未找到企业信息, companyId={}", companyId);
                result.setSuccess(false);
                return result;
            }
        } else if ("jobAudit".equals(task.getType())) {
            // 处理岗位审核任务
            Long jobId = task.getTargetItemId();
            JobPosting job = jobPostingMapper.selectById(jobId);

            if (job != null) {
                if ("approve".equals(action)) {
                    // 审核通过，更新岗位状态为活动
                    job.setStatus("active");
                    job.setPublishedAt(LocalDateTime.now());
                    log.info("岗位审核通过: jobId={}", jobId);
                } else if ("reject".equals(action)) {
                    // 审核不通过，更新岗位状态为被拒绝
                    job.setStatus("rejected");
                    log.info("岗位审核不通过: jobId={}, 原因={}", jobId, taskProcessDTO.getReason());
                }
                // 保存更新
                jobPostingMapper.updateById(job);
            } else {
                log.error("处理岗位审核任务失败: 未找到岗位信息, jobId={}", jobId);
                result.setSuccess(false);
                return result;
            }
        } else if ("reportHandling".equals(task.getType())) {
            // 处理举报任务
            // TODO: 根据举报对象类型和处理结果执行相应操作
            log.info("处理举报任务: type={}, itemId={}, 操作={}",
                    task.getTargetItemType(), task.getTargetItemId(), action);
            // 这里需要根据不同的举报对象类型执行不同的操作
        }

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

    /**
     * 获取辅导员个人资料
     */
    @Override
    public CounselorProfileDTO getCounselorProfile() {
        // 获取当前登录的辅导员用户ID
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("获取辅导员档案，当前登录用户ID: {}", currentUserId);

        if (currentUserId == null) {
            log.error("获取辅导员档案失败：未登录或登录已过期");
            return null;
        }

        // 查询辅导员档案
        CounselorProfile profile = counselorProfileMapper.selectOne(
                new LambdaQueryWrapper<CounselorProfile>()
                        .eq(CounselorProfile::getUserId, currentUserId));

        if (profile == null) {
            log.error("获取辅导员档案失败：未找到对应的档案记录");
            return null;
        }

        // 转换为DTO对象
        CounselorProfileDTO profileDTO = new CounselorProfileDTO();
        BeanUtils.copyProperties(profile, profileDTO);

        return profileDTO;
    }

    /**
     * 更新辅导员个人资料
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCounselorProfile(CounselorProfileDTO profileDTO) {
        // 获取当前登录的辅导员用户ID
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("更新辅导员档案，当前登录用户ID: {}", currentUserId);

        if (currentUserId == null) {
            log.error("更新辅导员档案失败：未登录或登录已过期");
            return false;
        }

        // 查询辅导员档案
        CounselorProfile profile = counselorProfileMapper.selectOne(
                new LambdaQueryWrapper<CounselorProfile>()
                        .eq(CounselorProfile::getUserId, currentUserId));

        if (profile == null) {
            log.error("更新辅导员档案失败：未找到对应的档案记录");
            return false;
        }

        // 更新档案信息，但保留一些不可修改的字段
        String originalEmployeeId = profile.getEmployeeId();
        String originalName = profile.getName();
        String originalContact = profile.getContact();

        BeanUtils.copyProperties(profileDTO, profile);

        // 还原不可修改的字段
        profile.setEmployeeId(originalEmployeeId);
        profile.setName(originalName);
        profile.setContact(originalContact);
        profile.setUpdatedAt(LocalDateTime.now());

        // 保存更新后的档案
        int rows = counselorProfileMapper.updateById(profile);

        return rows > 0;
    }
}