package com.bs.eaps.service;

import com.bs.eaps.entity.WelfareTag;

import java.util.List;

/**
 * 福利标签服务接口
 */
public interface WelfareTagService {

    /**
     * 获取所有福利标签
     * 
     * @return 福利标签列表
     */
    List<WelfareTag> getAllTags();

    /**
     * 根据ID获取福利标签
     * 
     * @param id 标签ID
     * @return 福利标签
     */
    WelfareTag getTagById(Long id);

    /**
     * 保存或更新福利标签
     * 
     * @param tag 福利标签
     * @return 是否成功
     */
    boolean saveOrUpdateTag(WelfareTag tag);

    /**
     * 根据ID删除福利标签
     * 
     * @param id 标签ID
     * @return 是否成功
     */
    boolean removeTagById(Long id);
}