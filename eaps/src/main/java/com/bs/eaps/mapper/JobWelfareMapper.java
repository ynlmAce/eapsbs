package com.bs.eaps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bs.eaps.entity.JobWelfare;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 岗位福利关联Mapper接口
 */
@Mapper
public interface JobWelfareMapper extends BaseMapper<JobWelfare> {

    /**
     * 根据岗位ID获取福利标签名称列表
     *
     * @param jobId 岗位ID
     * @return 福利标签名称列表
     */
    @Select("SELECT t.name FROM welfare_tag t " +
            "INNER JOIN job_welfare r ON t.id = r.welfare_tag_id " +
            "WHERE r.job_id = #{jobId}")
    List<String> getTagNamesByJobId(@Param("jobId") Long jobId);
}