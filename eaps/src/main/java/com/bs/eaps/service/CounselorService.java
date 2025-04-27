package com.bs.eaps.service;

import com.bs.eaps.dto.counselor.TaskCountDTO;
import com.bs.eaps.dto.counselor.TaskDTO;
import com.bs.eaps.dto.counselor.TaskProcessDTO;
import com.bs.eaps.dto.counselor.TaskProcessResultDTO;
import com.bs.eaps.dto.counselor.OperationHistoryDTO;
import com.bs.eaps.dto.counselor.CounselorProfileDTO;
import com.bs.eaps.dto.common.PageRequestDTO;
import com.bs.eaps.dto.common.PageResultDTO;

import java.time.LocalDate;

/**
 * 辅导员服务接口
 */
public interface CounselorService {

        /**
         * 获取辅导员待处理的各类任务数量统计
         * 
         * @return 任务数量统计
         */
        TaskCountDTO getTasksCount();

        /**
         * 获取辅导员待处理的特定类型任务列表
         * 
         * @param type        任务类型：companyCertification, jobAudit, reportHandling
         * @param pageRequest 分页请求
         * @return 任务列表
         */
        PageResultDTO<TaskDTO> getTasksList(String type, PageRequestDTO pageRequest);

        /**
         * 获取所有状态的特定类型任务列表
         * 
         * @param type         任务类型：companyCertification, jobAudit, reportHandling
         * @param pageRequest  分页请求
         * @param includeAll   是否包含所有状态（true表示包含所有状态，false只包含pending状态）
         * @param statusFilter 状态筛选（可选，用于筛选岗位状态或企业认证状态）
         * @return 任务列表
         */
        PageResultDTO<TaskDTO> getAllTasksList(String type, PageRequestDTO pageRequest, boolean includeAll,
                        String statusFilter);

        /**
         * 辅导员处理特定任务
         * 
         * @param taskProcessDTO 任务处理参数
         * @return 处理结果
         */
        TaskProcessResultDTO processTask(TaskProcessDTO taskProcessDTO);

        /**
         * 获取辅导员的历史操作记录
         * 
         * @param startDate   开始日期
         * @param endDate     结束日期
         * @param type        操作类型
         * @param pageRequest 分页请求
         * @return 历史操作记录
         */
        PageResultDTO<OperationHistoryDTO> getOperationsHistory(LocalDate startDate, LocalDate endDate,
                        String type, PageRequestDTO pageRequest);

        /**
         * 获取辅导员个人资料
         * 
         * @return 辅导员个人资料
         */
        CounselorProfileDTO getCounselorProfile();

        /**
         * 更新辅导员个人资料
         * 
         * @param profileDTO 辅导员个人资料
         * @return 是否更新成功
         */
        boolean updateCounselorProfile(CounselorProfileDTO profileDTO);
}