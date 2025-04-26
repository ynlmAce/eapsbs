package com.bs.eaps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bs.eaps.entity.StudentFavorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 企业收藏学生Mapper接口
 */
@Mapper
public interface StudentFavoriteMapper extends BaseMapper<StudentFavorite> {
}