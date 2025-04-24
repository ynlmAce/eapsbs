package com.employmentsupport.eaps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.employmentsupport.eaps.entity.JobPosting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 岗位发布Mapper接口
 */
@Mapper
public interface JobPostingMapper extends BaseMapper<JobPosting> {

    /**
     * 多条件查询岗位列表
     * 
     * @param page   分页参数
     * @param params 查询条件参数
     * @return 岗位列表
     */
    Page<JobPosting> selectJobList(Page<JobPosting> page, @Param("params") Map<String, Object> params);
}