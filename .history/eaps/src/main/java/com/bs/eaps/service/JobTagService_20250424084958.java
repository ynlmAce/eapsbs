package com.bs.eaps.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bs.eaps.entity.JobTag;

import java.util.List;

/**
 * 岗位标签服务接口
 */
public interface JobTagService extends IService<JobTag> {

    /**
     * 获取所有岗位标签
     *
     * @return 标签列表
     */
    List<JobTag> getAllTags();

    /**
     * 根据ID获取标签
     *
     * @param id 标签ID
     * @return 标签对象
     */
    JobTag getTagById(Long id);

    /**
     * 保存或更新标签
     *
     * @param jobTag 标签对象
     * @return 是否成功
     */
    boolean saveOrUpdateTag(JobTag jobTag);

    /**
     * 删除标签
     *
     * @param id 标签ID
     * @return 是否成功
     */
    boolean removeTagById(Long id);
}