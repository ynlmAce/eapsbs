package com.bs.eaps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bs.eaps.entity.JobTagRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 岗位标签关联Mapper接口
 */
@Mapper
public interface JobTagRelationMapper extends BaseMapper<JobTagRelation> {

    /**
     * 根据岗位ID获取标签名称列表
     *
     * @param jobId 岗位ID
     * @return 标签名称列表
     */
    @Select("SELECT t.name FROM job_tag t " +
            "INNER JOIN job_tag_relation r ON t.id = r.job_tag_id " +
            "WHERE r.job_id = #{jobId}")
    List<String> getTagNamesByJobId(@Param("jobId") Long jobId);
}