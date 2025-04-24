package com.employmentsupport.eaps.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件记录实体类
 */
@Data
@TableName("file")
public class File implements Serializable {

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
     * 文件类型：resume-简历, company_license-营业执照, company_logo-企业Logo, chat_file-聊天文件
     */
    @TableField("file_type")
    private String fileType;

    /**
     * 关联对象ID（如学生简历ID、企业ID、聊天消息ID等）
     */
    @TableField("related_id")
    private Long relatedId;

    /**
     * 原始文件名
     */
    @TableField("original_filename")
    private String originalFilename;

    /**
     * 文件存储路径
     */
    @TableField("file_path")
    private String filePath;

    /**
     * 文件大小(字节)
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 文件扩展名
     */
    @TableField("extension")
    private String extension;

    /**
     * 上传时间
     */
    @TableField("uploaded_at")
    private LocalDateTime uploadedAt;
}