package com.bs.eaps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bs.eaps.entity.StudentProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生档案Mapper接口
 */
@Mapper
public interface StudentProfileMapper extends BaseMapper<StudentProfile> {
}