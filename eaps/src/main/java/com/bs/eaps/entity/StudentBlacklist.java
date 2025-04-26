package com.bs.eaps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 学生黑名单实体类
 */
@Data
@TableName("student_blacklist")
public class StudentBlacklist {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 企业ID
     */
    private Long companyId;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 拉黑原因
     */
    private String reason;

    /**
     * 创建时间
     */
    private Date createdAt;
}