package com.bs.eaps.controller;

import com.bs.eaps.common.ApiResponse;
import com.bs.eaps.entity.JobTag;
import com.bs.eaps.service.JobTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return ApiResponse.success(tags);
    }

    /**
     * 获取标签详情
     */
    @PostMapping("/detail")
    public ApiResponse getTagDetail(@RequestBody TagDetailRequest request) {
        JobTag tag = jobTagService.getTagById(request.getId());
        return ApiResponse.success(tag);
    }

    /**
     * 保存或更新标签
     */
    @PostMapping("/save")
    public ApiResponse saveOrUpdateTag(@RequestBody JobTag jobTag) {
        boolean result = jobTagService.saveOrUpdateTag(jobTag);
        return ApiResponse.success(result);
    }

    /**
     * 删除标签
     */
    @PostMapping("/remove")
    public ApiResponse removeTag(@RequestBody TagDetailRequest request) {
        boolean result = jobTagService.removeTagById(request.getId());
        return ApiResponse.success(result);
    }

    /**
     * 标签详情请求
     */
    public static class TagDetailRequest {
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }
}