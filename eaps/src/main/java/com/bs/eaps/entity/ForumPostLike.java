package com.bs.eaps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 论坛帖子点赞记录表实体
 */
@Data
@TableName("forum_post_like")
public class ForumPostLike {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 帖子ID */
    private Long postId;
    /** 点赞用户ID */
    private Long userId;
    /** 点赞时间 */
    private Date createdAt;
}