package com.bs.eaps.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bs.eaps.entity.JobTag;
import com.bs.eaps.entity.JobTagRelation;
import com.bs.eaps.mapper.JobTagMapper;
import com.bs.eaps.mapper.JobTagRelationMapper;
import com.bs.eaps.service.JobTagRelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 岗位标签关联服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobTagRelationServiceImpl extends ServiceImpl<JobTagRelationMapper, JobTagRelation>
        implements JobTagRelationService {

    private final JobTagRelationMapper jobTagRelationMapper;
    private final JobTagMapper jobTagMapper;

    @Override
    @Transactional
    public void saveJobTagRelations(Long jobId, List<Long> tagIds) {
        if (jobId == null || tagIds == null || tagIds.isEmpty()) {
            return;
        }

        // 先删除现有关联
        removeByJobId(jobId);

        // 创建标签关联
        List<JobTagRelation> relations = new ArrayList<>();
        for (Long tagId : tagIds) {
            if (tagId != null) {
                // 创建关联记录
                JobTagRelation relation = new JobTagRelation();
                relation.setJobId(jobId);
                relation.setJobTagId(tagId);
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

        LambdaQueryWrapper<JobTagRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(JobTagRelation::getJobId, jobId);
        remove(queryWrapper);
    }

    @Override
    public List<String> getTagNamesByJobId(Long jobId) {
        if (jobId == null) {
            return new ArrayList<>();
        }
        return jobTagRelationMapper.getTagNamesByJobId(jobId);
    }
}