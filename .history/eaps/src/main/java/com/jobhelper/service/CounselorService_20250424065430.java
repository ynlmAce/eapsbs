package com.jobhelper.service;

import com.jobhelper.dto.counselor.OperationHistoryQueryDTO;
import com.jobhelper.dto.counselor.TaskListQueryDTO;
import com.jobhelper.dto.counselor.TaskProcessDTO;

/**
 * 辅导员服务接口
 */
public interface CounselorService {

    /**
     * 获取待处理任务统计
     * 
     * @param counselorId 辅导员ID
     * @return 任务统计
     */
    Object getTasksCount(Long counselorId);

    /**
     * 获取任务列表
     * 
     * @param counselorId 辅导员ID
     * @param queryDTO    查询条件
     * @return 任务列表
     */
    Object getTasksList(Long counselorId, TaskListQueryDTO queryDTO);

    /**
     * 处理任务
     * 
     * @param counselorId 辅导员ID
     * @param processDTO  处理信息
     * @return 处理结果
     */
    Object processTask(Long counselorId, TaskProcessDTO processDTO);

    /**
     * 获取历史操作记录
     * 
     * @param counselorId 辅导员ID
     * @param queryDTO    查询条件
     * @return 操作记录列表
     */
    Object getOperationsHistory(Long counselorId, OperationHistoryQueryDTO queryDTO);
}