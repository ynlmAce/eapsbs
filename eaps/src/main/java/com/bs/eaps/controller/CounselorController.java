package com.bs.eaps.controller;

import com.bs.eaps.common.ApiResponse;
import com.bs.eaps.dto.counselor.OperationHistoryQueryDTO;
import com.bs.eaps.dto.counselor.TaskListQueryDTO;
import com.bs.eaps.dto.counselor.TaskProcessDTO;
import com.bs.eaps.dto.counselor.CounselorProfileDTO;
import com.bs.eaps.service.CounselorService;
import com.bs.eaps.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.bs.eaps.dto.common.PageRequestDTO;

/**
 * 辅导员模块控制器
 */
@Slf4j
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
        // 不需要传入counselorId参数
        return ApiResponse.success(counselorService.getTasksCount());
    }

    /**
     * 获取任务列表
     */
    @PostMapping("/tasks/list")
    public ApiResponse getTasksList(@RequestBody TaskListQueryDTO queryDTO) {
        // 从queryDTO中提取taskType和分页信息
        String taskType = queryDTO.getType();
        PageRequestDTO pageRequest = new PageRequestDTO();
        pageRequest.setPage(queryDTO.getPage());
        pageRequest.setPageSize(queryDTO.getPageSize());

        return ApiResponse.success(counselorService.getTasksList(taskType, pageRequest));
    }

    /**
     * 处理任务
     */
    @PostMapping("/task/process")
    public ApiResponse processTask(@RequestBody TaskProcessDTO processDTO) {
        return ApiResponse.success(counselorService.processTask(processDTO));
    }

    /**
     * 获取历史操作记录
     */
    @PostMapping("/operations/history")
    public ApiResponse getOperationsHistory(@RequestBody OperationHistoryQueryDTO queryDTO) {
        // 从queryDTO中提取开始日期、结束日期、操作类型和分页信息
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate = LocalDate.parse(queryDTO.getStartDate(), formatter);
        LocalDate endDate = LocalDate.parse(queryDTO.getEndDate(), formatter);
        String type = queryDTO.getType();

        PageRequestDTO pageRequest = new PageRequestDTO();
        pageRequest.setPage(queryDTO.getPage());
        pageRequest.setPageSize(queryDTO.getPageSize());

        return ApiResponse.success(counselorService.getOperationsHistory(startDate, endDate, type, pageRequest));
    }

    /**
     * 获取辅导员个人资料
     */
    @PostMapping("/profile")
    public ApiResponse getCounselorProfile() {
        log.info("获取辅导员个人资料");
        CounselorProfileDTO profile = counselorService.getCounselorProfile();
        if (profile == null) {
            return ApiResponse.error(500, "获取辅导员个人资料失败");
        }
        return ApiResponse.success(profile);
    }

    /**
     * 更新辅导员个人资料
     */
    @PostMapping("/profile/update")
    public ApiResponse updateCounselorProfile(@RequestBody CounselorProfileDTO profileDTO) {
        log.info("更新辅导员个人资料：{}", profileDTO);
        boolean result = counselorService.updateCounselorProfile(profileDTO);
        if (!result) {
            return ApiResponse.error(500, "更新辅导员个人资料失败");
        }
        return ApiResponse.success(true);
    }
}