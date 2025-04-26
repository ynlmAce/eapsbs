package com.bs.eaps.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 企业资料实体类
 */
@Data
@TableName(value = "company_profile", autoResultMap = true)
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

    @TableField(typeHandler = JacksonTypeHandler.class)
    private HrContact hrContact;

    private String logoPath;

    private String certificationStatus;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date certificationExpiryDate;

    private Date createdAt;

    private Date updatedAt;
}