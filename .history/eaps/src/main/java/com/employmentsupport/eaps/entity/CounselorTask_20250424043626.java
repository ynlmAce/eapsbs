package com.employmentsupport.eaps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 辅导员任务实体类
 */
@Data
@TableName("counselor_task")
public class CounselorTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务类型：companyCertification-企业认证, jobAudit-岗位审核, reportHandling-举报处理
     */
    @TableField("type")
    private String type;

    /**
     * 目标项类型：Company, Job, Rating, ChatMessage, ChatSession
     */
    @TableField("target_item_type")
    private String targetItemType;

    /**
     * 目标项ID
     */
    @TableField("target_item_id")
    private Long targetItemId;

    /**
     * 辅导员ID
     */
    @TableField("counselor_id")
    private Long counselorId;

    /**
     * 备注
     */
    @TableField("notes")
    private String notes;

    /**
     * 优先级：high-高, normal-正常, low-低
     */
    @TableField("priority")
    private String priority;

    /**
     * 状态：pending-待处理, processing-处理中, resolved-已解决
     */
    @TableField("status")
    private String status;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}