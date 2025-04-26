package com.bs.eaps.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bs.eaps.entity.WelfareTag;
import com.bs.eaps.entity.JobWelfare;
import com.bs.eaps.mapper.WelfareTagMapper;
import com.bs.eaps.mapper.JobWelfareMapper;
import com.bs.eaps.service.JobWelfareService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 岗位福利关联服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobWelfareServiceImpl extends ServiceImpl<JobWelfareMapper, JobWelfare> implements JobWelfareService {

    private final JobWelfareMapper jobWelfareMapper;
    private final WelfareTagMapper welfareTagMapper;

    @Override
    @Transactional
    public void saveJobWelfareRelations(Long jobId, List<Long> tagIds) {
        if (jobId == null || tagIds == null || tagIds.isEmpty()) {
            return;
        }

        // 先删除现有关联
        removeByJobId(jobId);

        // 创建福利标签关联
        List<JobWelfare> relations = new ArrayList<>();
        for (Long tagId : tagIds) {
            if (tagId != null) {
                // 创建关联记录
                JobWelfare relation = new JobWelfare();
                relation.setJobId(jobId);
                relation.setWelfareTagId(tagId);
                relation.setCreatedAt(LocalDateTime.now());
                relations.add(relation);
            }
        }

        // 批量保存关联
        if (!relations.isEmpty()) {
            saveBatch(relations);
        }
    }

    @Override
    public void removeByJobId(Long jobId) {
        if (jobId == null) {
            return;
        }

        LambdaQueryWrapper<JobWelfare> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(JobWelfare::getJobId, jobId);
        remove(queryWrapper);
    }

    @Override
    public List<String> getTagNamesByJobId(Long jobId) {
        if (jobId == null) {
            return new ArrayList<>();
        }
        return jobWelfareMapper.getTagNamesByJobId(jobId);
    }
}