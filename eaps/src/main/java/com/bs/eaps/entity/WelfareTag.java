package com.bs.eaps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 福利标签实体类
 */
@Data
@TableName("welfare_tag")
public class WelfareTag {

    /**
     * 标签ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 标签分类
     */
    private String category;

    /**
     * 状态：active-激活, pending-待审核, rejected-已拒绝
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}