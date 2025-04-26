package com.bs.eaps.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 企业收藏学生实体类
 */
@Data
@TableName("student_favorite")
public class StudentFavorite {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 企业ID
     */
    private Long companyId;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 收藏时间
     */
    private LocalDateTime createdAt;
}