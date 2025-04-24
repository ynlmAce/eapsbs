package com.bs.eaps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 学生技能标签实体类
 */
@Data
@TableName("skill_tag")
public class SkillTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
}