package com.bs.eaps.controller;

import com.bs.eaps.common.ApiResponse;
import com.bs.eaps.dto.JobTagDTO;
import com.bs.eaps.entity.JobTag;
import com.bs.eaps.service.JobTagService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 岗位标签控制器
 */
@RestController
@RequestMapping("/api/job-tag")
@RequiredArgsConstructor
public class JobTagController {

    private final JobTagService jobTagService;

    /**
     * 获取所有岗位标签
     */
    @PostMapping("/list")
    public ApiResponse getAllTags() {
        List<JobTag> tags = jobTagService.getAllTags();
        List<JobTagDTO> tagDTOs = tags.stream().map(this::convertToDTO).collect(Collectors.toList());
        return ApiResponse.success(tagDTOs);
    }

    /**
     * 获取标签详情
     */
    @PostMapping("/detail")
    public ApiResponse getTagDetail(@RequestBody TagDetailRequest request) {
        JobTag tag = jobTagService.getTagById(request.getId());
        if (tag == null) {
            return ApiResponse.businessError("标签不存在");
        }
        return ApiResponse.success(convertToDTO(tag));
    }

    /**
     * 保存或更新标签
     */
    @PostMapping("/save")
    public ApiResponse saveOrUpdateTag(@RequestBody JobTagDTO tagDTO) {
        JobTag tag = new JobTag();
        BeanUtils.copyProperties(tagDTO, tag);
        boolean result = jobTagService.saveOrUpdateTag(tag);
        return ApiResponse.success(result);
    }

    /**
     * 删除标签
     */
    @PostMapping("/remove")
    public ApiResponse removeTag(@RequestBody TagDetailRequest request) {
        JobTag tag = jobTagService.getTagById(request.getId());
        if (tag == null) {
            return ApiResponse.businessError("标签不存在");
        }
        boolean result = jobTagService.removeTagById(request.getId());
        return ApiResponse.success(result);
    }

    /**
     * 将实体转换为DTO
     */
    private JobTagDTO convertToDTO(JobTag tag) {
        JobTagDTO dto = new JobTagDTO();
        BeanUtils.copyProperties(tag, dto);
        return dto;
    }

    /**
     * 标签详情请求
     */
    @Data
    public static class TagDetailRequest {
        private Long id;
    }
}