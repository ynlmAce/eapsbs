package com.bs.eaps.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bs.eaps.entity.ForumPost;
import com.bs.eaps.mapper.ForumPostMapper;
import com.bs.eaps.service.ForumPostService;
import org.springframework.stereotype.Service;

@Service
public class ForumPostServiceImpl extends ServiceImpl<ForumPostMapper, ForumPost> implements ForumPostService {
    // 可扩展自定义方法
}