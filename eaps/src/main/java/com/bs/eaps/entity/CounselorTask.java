package com.bs.eaps.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import java.time.LocalDateTime;

/**
 * 辅导员任务实体类
 */
@Data
@TableName("counselor_task")
public class CounselorTask {

    /**
     * 任务ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务类型：companyCertification, jobAudit, reportHandling
     */
    private String type;

    /**
     * 目标项类型：Company, Job, Rating, ChatMessage, ChatSession
     */
    private String targetItemType;

    /**
     * 目标项ID
     */
    private Long targetItemId;

    /**
     * 任务标题
     */
    @TableField("title")
    private String title;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 状态：pending, processing, completed
     */
    private String status;

    /**
     * 处理人ID
     */
    private Long counselorId;

    /**
     * 处理备注
     */
    private String notes;

    /**
     * 优先级：high, normal, low
     */
    private String priority;
}