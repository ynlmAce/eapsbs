package com.bs.eaps.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 企业资料实体类
 */
@Data
@TableName("company_profile")
public class CompanyProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    private String unifiedSocialCreditCode;

    private String industry;

    private String size;

    private String address;

    private String description;

    private String hrContact;

    private String logoPath;

    private String certificationStatus;

    private Date certificationExpiryDate;

    private Date createdAt;

    private Date updatedAt;
}