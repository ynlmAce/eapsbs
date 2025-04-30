package com.bs.eaps.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bs.eaps.entity.ForumPostLike;

/**
 * 论坛帖子点赞记录Service接口
 */
public interface ForumPostLikeService extends IService<ForumPostLike> {
    /** 判断用户是否已点赞 */
    boolean exists(Long postId, Long userId);

    /** 撤销点赞 */
    void removeByPostIdAndUserId(Long postId, Long userId);
}