package com.bs.eaps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生结构化简历实体类
 */
@Data
@TableName(value = "student_structured_resume", autoResultMap = true)
public class StudentStructuredResume {

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 学生档案ID
     */
    private Long studentProfileId;

    /**
     * 简历部分类型
     */
    private String sectionType;

    /**
     * 内容（JSON格式）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object content;

    /**
     * 排序号
     */
    private Integer orderNum;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}