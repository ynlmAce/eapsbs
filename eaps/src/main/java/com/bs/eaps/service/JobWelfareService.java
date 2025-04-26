package com.bs.eaps.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bs.eaps.entity.JobWelfare;

import java.util.List;

/**
 * 岗位福利关联服务接口
 */
public interface JobWelfareService extends IService<JobWelfare> {

    /**
     * 保存岗位与福利标签的关联关系
     *
     * @param jobId  岗位ID
     * @param tagIds 福利标签ID列表
     */
    void saveJobWelfareRelations(Long jobId, List<Long> tagIds);

    /**
     * 删除岗位的所有福利标签关联
     *
     * @param jobId 岗位ID
     */
    void removeByJobId(Long jobId);

    /**
     * 获取岗位关联的所有福利标签
     *
     * @param jobId 岗位ID
     * @return 标签列表
     */
    List<String> getTagNamesByJobId(Long jobId);
}