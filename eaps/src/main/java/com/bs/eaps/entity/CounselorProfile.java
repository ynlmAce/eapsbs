package com.bs.eaps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 辅导员档案实体类
 */
@Data
@TableName("counselor_profile")
public class CounselorProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 工号
     */
    @TableField("employee_id")
    private String employeeId;

    /**
     * 姓名
     */
    @TableField("name")
    private String name;

    /**
     * 联系方式
     */
    @TableField("contact")
    private String contact;

    /**
     * 电子邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 所属学院
     */
    @TableField("college")
    private String college;

    /**
     * 职称
     */
    @TableField("title")
    private String title;

    /**
     * 专业方向
     */
    @TableField("specialization")
    private String specialization;

    /**
     * 带队经验(年)
     */
    @TableField("experience")
    private Integer experience;

    /**
     * 办公室地点
     */
    @TableField("office_location")
    private String officeLocation;

    /**
     * 办公时间
     */
    @TableField("office_hours")
    private String officeHours;

    /**
     * 个人简介
     */
    @TableField("introduction")
    private String introduction;

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