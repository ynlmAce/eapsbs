package com.bs.eaps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bs.eaps.entity.Report;
import org.apache.ibatis.annotations.Mapper;

/**
 * 举报记录Mapper接口
 */
@Mapper
public interface ReportMapper extends BaseMapper<Report> {
}