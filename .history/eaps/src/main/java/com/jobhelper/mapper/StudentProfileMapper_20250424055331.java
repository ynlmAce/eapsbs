package com.jobhelper.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jobhelper.entity.StudentProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生个人资料Mapper接口
 */
@Mapper
public interface StudentProfileMapper extends BaseMapper<StudentProfile> {
}