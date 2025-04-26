package com.bs.eaps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生技能关联实体类
 */
@Data
@TableName("student_skill")
public class StudentSkill {

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
     * 技能标签ID
     */
    private Long skillTagId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}