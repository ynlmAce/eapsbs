package com.bs.eaps.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bs.eaps.common.ApiResponse;
import com.bs.eaps.entity.SkillTag;
import com.bs.eaps.mapper.SkillTagMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 通用接口控制器
 * 处理各模块通用的API请求
 */
@Slf4j
@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class CommonController {

    private final SkillTagMapper skillTagMapper;

    /**
     * 获取所有技能标签
     * 
     * @return 技能标签列表
     */
    @PostMapping("/skill-tags")
    public ApiResponse getSkillTags() {
        log.info("获取技能标签列表");
        try {
            List<SkillTag> skillTags = skillTagMapper.selectList(new LambdaQueryWrapper<>());

            // 转换为前端需要的格式 {value: 'id', label: 'name'}
            List<Object> result = skillTags.stream()
                    .map(tag -> {
                        return new Object() {
                            public final String value = tag.getName();
                            public final String label = tag.getName();
                        };
                    })
                    .collect(Collectors.toList());

            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("获取技能标签列表失败", e);
            return ApiResponse.businessError("获取技能标签列表失败: " + e.getMessage());
        }
    }
}