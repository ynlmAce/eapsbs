package com.bs.eaps.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 企业实体类
 */
@Data
@TableName("company_profile")
public class Company {

    /**
     * 企业ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 企业名称
     */
    private String name;

    /**
     * 统一社会信用代码
     */
    private String unifiedSocialCreditCode;

    /**
     * 行业
     */
    private String industry;

    /**
     * 规模
     */
    private String size;

    /**
     * 地址
     */
    private String address;

    /**
     * 企业简介
     */
    private String description;

    /**
     * HR联系方式
     */
    private String hrContact;

    /**
     * 企业Logo路径
     */
    private String logoPath;

    /**
     * 认证状态：未认证、待认证、已认证、认证失败
     */
    private String certificationStatus;

    /**
     * 认证有效期
     */
    private LocalDateTime certificationExpiryDate;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}