package com.bs.eaps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bs.eaps.entity.ForumPostLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 论坛帖子点赞记录Mapper接口
 */
@Mapper
public interface ForumPostLikeMapper extends BaseMapper<ForumPostLike> {
}