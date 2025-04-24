package com.bs.eaps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 福利标签实体类
 */
@Data
@TableName("welfare_tag")
public class WelfareTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String category;
}