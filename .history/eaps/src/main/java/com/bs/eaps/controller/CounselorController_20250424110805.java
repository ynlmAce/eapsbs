package com.bs.eaps.controller;

import com.bs.eaps.common.ApiResponse;
import com.bs.eaps.dto.counselor.OperationHistoryQueryDTO;
import com.bs.eaps.dto.counselor.TaskListQueryDTO;
import com.bs.eaps.dto.counselor.TaskProcessDTO;
import com.bs.eaps.service.CounselorService;
import com.bs.eaps.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 辅导员模块控制器
 */
@RestController
@RequestMapping("/api/counselor")
@RequiredArgsConstructor
public class CounselorController {

    private final CounselorService counselorService;

    /**
     * 获取待处理任务统计
     */
    @PostMapping("/tasks/count")
    public ApiResponse getTasksCount() {
        Long counselorId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(counselorService.getTasksCount(counselorId));
    }

    /**
     * 获取任务列表
     */
    @PostMapping("/tasks/list")
    public ApiResponse getTasksList(@RequestBody TaskListQueryDTO queryDTO) {
        Long counselorId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(counselorService.getTasksList(counselorId, queryDTO));
    }

    /**
     * 处理任务
     */
    @PostMapping("/task/process")
    public ApiResponse processTask(@RequestBody TaskProcessDTO processDTO) {
        Long counselorId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(counselorService.processTask(counselorId, processDTO));
    }

    /**
     * 获取历史操作记录
     */
    @PostMapping("/operations/history")
    public ApiResponse getOperationsHistory(@RequestBody OperationHistoryQueryDTO queryDTO) {
        Long counselorId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(counselorService.getOperationsHistory(counselorId, queryDTO));
    }
}