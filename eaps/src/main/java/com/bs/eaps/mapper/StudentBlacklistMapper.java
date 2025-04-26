package com.bs.eaps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bs.eaps.entity.StudentBlacklist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 学生黑名单Mapper接口
 */
@Mapper
public interface StudentBlacklistMapper extends BaseMapper<StudentBlacklist> {

    /**
     * 分页查询企业拉黑的学生列表，包含学生详细信息
     * 
     * @param page      分页对象
     * @param companyId 企业ID
     * @return 带有学生详细信息的黑名单列表
     */
    IPage<Map<String, Object>> selectBlacklistedStudentsWithDetails(Page<Map<String, Object>> page,
            @Param("companyId") Long companyId);
}