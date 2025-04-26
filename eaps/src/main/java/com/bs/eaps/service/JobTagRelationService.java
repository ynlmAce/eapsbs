package com.bs.eaps.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bs.eaps.entity.JobTagRelation;

import java.util.List;

/**
 * 岗位标签关联服务接口
 */
public interface JobTagRelationService extends IService<JobTagRelation> {

    /**
     * 保存岗位与标签的关联关系
     *
     * @param jobId  岗位ID
     * @param tagIds 标签ID列表
     */
    void saveJobTagRelations(Long jobId, List<Long> tagIds);

    /**
     * 删除岗位的所有标签关联
     *
     * @param jobId 岗位ID
     */
    void removeByJobId(Long jobId);

    /**
     * 获取岗位关联的所有标签
     *
     * @param jobId 岗位ID
     * @return 标签列表
     */
    List<String> getTagNamesByJobId(Long jobId);
}