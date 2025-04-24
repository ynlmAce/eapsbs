package com.employmentsupport.eaps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天会话实体类
 */
@Data
@TableName("chat_session")
public class ChatSession implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会话类型：SE-学生企业, SC-学生辅导员, SS-学生群组
     */
    @TableField("type")
    private String type;

    /**
     * 相关岗位ID（SE类型）
     */
    @TableField("related_job_id")
    private Long relatedJobId;

    /**
     * 群组ID（SS类型）
     */
    @TableField("group_id")
    private Long groupId;

    /**
     * 参与者1 ID
     */
    @TableField("participant1_id")
    private Long participant1Id;

    /**
     * 参与者2 ID
     */
    @TableField("participant2_id")
    private Long participant2Id;

    /**
     * 会话状态：active-活跃, archived-已归档
     */
    @TableField("status")
    private String status;

    /**
     * 是否只读
     */
    @TableField("is_readonly")
    private Boolean isReadonly;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 最近活跃时间
     */
    @TableField("last_active_at")
    private LocalDateTime lastActiveAt;
}