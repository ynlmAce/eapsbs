package com.bs.eaps.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bs.eaps.entity.JobTag;
import com.bs.eaps.mapper.JobTagMapper;
import com.bs.eaps.service.JobTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 岗位标签服务实现类
 */
@Service
@RequiredArgsConstructor
public class JobTagServiceImpl extends ServiceImpl<JobTagMapper, JobTag> implements JobTagService {

    private final JobTagMapper jobTagMapper;

    /**
     * 获取所有岗位标签
     *
     * @return 标签列表
     */
    @Override
    public List<JobTag> getAllTags() {
        LambdaQueryWrapper<JobTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobTag::getStatus, 1); // 只获取启用状态的标签
        wrapper.orderByAsc(JobTag::getId);
        return list(wrapper);
    }

    /**
     * 根据ID获取标签
     *
     * @param id 标签ID
     * @return 标签对象
     */
    @Override
    public JobTag getTagById(Long id) {
        return getById(id);
    }

    /**
     * 保存或更新标签
     *
     * @param jobTag 标签对象
     * @return 是否成功
     */
    @Override
    public boolean saveOrUpdateTag(JobTag jobTag) {
        // 设置时间
        LocalDateTime now = LocalDateTime.now();
        if (jobTag.getId() == null) {
            // 新增
            jobTag.setCreateTime(now);
            jobTag.setUpdateTime(now);
            if (jobTag.getStatus() == null) {
                jobTag.setStatus(1); // 默认启用
            }
        } else {
            // 更新
            jobTag.setUpdateTime(now);
        }
        return saveOrUpdate(jobTag);
    }

    /**
     * 删除标签
     *
     * @param id 标签ID
     * @return 是否成功
     */
    @Override
    public boolean removeTagById(Long id) {
        return removeById(id);
    }
}