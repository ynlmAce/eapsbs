package com.bs.eaps.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bs.eaps.entity.ForumComment;
import com.bs.eaps.mapper.ForumCommentMapper;
import com.bs.eaps.service.ForumCommentService;
import org.springframework.stereotype.Service;

@Service
public class ForumCommentServiceImpl extends ServiceImpl<ForumCommentMapper, ForumComment>
        implements ForumCommentService {
    // 可扩展自定义方法
}