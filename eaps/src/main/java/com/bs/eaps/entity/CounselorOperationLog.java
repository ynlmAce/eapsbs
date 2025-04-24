package com.bs.eaps.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 辅导员操作日志实体类
 */
@Data
@TableName("counselor_operation_log")
public class CounselorOperationLog {

    /**
     * 操作ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 辅导员ID
     */
    private Long counselorId;

    /**
     * 操作类型：companyCertification, jobAudit, reportHandling
     */
    private String operationType;

    /**
     * 目标项类型：Company, Job, Rating, ChatMessage, ChatSession
     */
    private String targetItemType;

    /**
     * 目标项ID
     */
    private Long targetItemId;

    /**
     * 操作结果：approve, reject, delete
     */
    private String result;

    /**
     * 操作原因
     */
    private String reason;

    /**
     * 操作备注
     */
    private String notes;

    /**
     * 操作时间
     */
    private LocalDateTime timestamp;
}