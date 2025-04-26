package com.bs.eaps.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 企业营业执照文件实体类
 */
@Data
@TableName("company_license_file")
public class CompanyLicenseFile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long companyProfileId;

    private String fileName;

    private String filePath;

    private Long fileSize;

    private String fileType;

    private Date uploadedAt;
}