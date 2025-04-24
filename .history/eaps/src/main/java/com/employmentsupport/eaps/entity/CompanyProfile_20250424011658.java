package com.employmentsupport.eaps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 企业资料实体类
 */
@Data
@TableName("company_profile")
public class CompanyProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 企业名称
     */
    @TableField("name")
    private String name;

    /**
     * 统一社会信用代码
     */
    @TableField("unified_social_credit_code")
    private String unifiedSocialCreditCode;

    /**
     * 所属行业
     */
    @TableField("industry")
    private String industry;

    /**
     * 企业规模
     */
    @TableField("size")
    private String size;

    /**
     * 企业地址
     */
    @TableField("address")
    private String address;

    /**
     * 企业简介
     */
    @TableField("description")
    private String description;

    /**
     * HR联系方式
     */
    @TableField("hr_contact")
    private String hrContact;

    /**
     * 企业Logo路径
     */
    @TableField("logo_path")
    private String logoPath;

    /**
     * 认证状态：pending-待认证, certified-已认证, rejected-已驳回, expired-已过期
     */
    @TableField("certification_status")
    private String certificationStatus;

    /**
     * 认证有效期
     */
    @TableField("certification_expiry_date")
    private LocalDateTime certificationExpiryDate;

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