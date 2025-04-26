package com.bs.eaps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 岗位发布实体类
 */
@Data
@TableName("job_posting")
public class JobPosting implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 企业ID
     */
    @TableField("company_id")
    private Long companyId;

    /**
     * 岗位标题
     */
    @TableField("title")
    private String title;

    /**
     * 岗位描述
     */
    @TableField("description")
    private String description;

    /**
     * 岗位要求
     */
    @TableField("requirement")
    private String requirement;

    /**
     * 工作地点
     */
    @TableField("location")
    private String location;

    /**
     * 薪资范围
     */
    @TableField("salary")
    private String salary;

    /**
     * 学历要求
     */
    @TableField("education")
    private String education;

    /**
     * 经验要求
     */
    @TableField("experience")
    private String experience;

    /**
     * 工作类型：全职、兼职、实习
     */
    @TableField("job_type")
    private String jobType;

    /**
     * 招聘人数
     */
    @TableField("headcount")
    private Integer headcount;

    /**
     * 工作时间描述
     */
    @TableField("work_time")
    private String workTime;

    /**
     * 岗位有效期截止日
     */
    @TableField("valid_until")
    private LocalDate validUntil;

    /**
     * 联系人
     */
    @TableField("contact_person")
    private String contactPerson;

    /**
     * 联系方式
     */
    @TableField("contact_method")
    private String contactMethod;

    /**
     * 是否显示联系方式
     */
    @TableField("show_contact")
    private Boolean showContact;

    /**
     * 岗位状态：draft-草稿, pending-待审核, active-招聘中, rejected-已驳回, closed-已结束
     */
    @TableField("status")
    private String status;

    /**
     * 发布时间
     */
    @TableField("published_at")
    private LocalDateTime publishedAt;

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