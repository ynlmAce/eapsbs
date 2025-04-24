package com.bs.eaps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 岗位标签实体类
 */
@Data
@TableName("job_tag")
public class JobTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
}