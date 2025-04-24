package com.bs.eaps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 举报实体类
 */
@Data
@TableName("report")
public class Report implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 举报用户ID
     */
    @TableField("reporting_user_id")
    private Long reportingUserId;

    /**
     * 被举报项类型：rating-评价, message-消息, chat_session-聊天会话
     */
    @TableField("reported_item_type")
    private String reportedItemType;

    /**
     * 被举报项ID
     */
    @TableField("reported_item_id")
    private Long reportedItemId;

    /**
     * 举报理由
     */
    @TableField("reason")
    private String reason;

    /**
     * 状态：pending-待处理, resolved-已解决
     */
    @TableField("status")
    private String status;

    /**
     * 处理的辅导员ID
     */
    @TableField("counselor_id")
    private Long counselorId;

    /**
     * 处理结果
     */
    @TableField("resolution")
    private String resolution;

    /**
     * 举报时间
     */
    @TableField("reported_at")
    private LocalDateTime reportedAt;

    /**
     * 处理时间
     */
    @TableField("resolved_at")
    private LocalDateTime resolvedAt;
}