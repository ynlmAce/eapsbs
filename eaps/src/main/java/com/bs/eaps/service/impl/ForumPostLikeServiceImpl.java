package com.bs.eaps.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bs.eaps.entity.ForumPostLike;
import com.bs.eaps.mapper.ForumPostLikeMapper;
import com.bs.eaps.service.ForumPostLikeService;
import org.springframework.stereotype.Service;

/**
 * 论坛帖子点赞记录Service实现
 */
@Service
public class ForumPostLikeServiceImpl extends ServiceImpl<ForumPostLikeMapper, ForumPostLike>
        implements ForumPostLikeService {
    @Override
    public boolean exists(Long postId, Long userId) {
        return lambdaQuery().eq(ForumPostLike::getPostId, postId).eq(ForumPostLike::getUserId, userId).exists();
    }

    @Override
    public void removeByPostIdAndUserId(Long postId, Long userId) {
        lambdaUpdate().eq(ForumPostLike::getPostId, postId).eq(ForumPostLike::getUserId, userId).remove();
    }
}