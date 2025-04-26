package com.bs.eaps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bs.eaps.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天会话Mapper接口
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}