package com.bs.eaps.dto.counselor;

import lombok.Data;

/**
 * 辅导员个人资料DTO
 */
@Data
public class CounselorProfileDTO {

    /**
     * 工号
     */
    private String employeeId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 联系方式
     */
    private String contact;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 所属学院
     */
    private String college;

    /**
     * 职称
     */
    private String title;

    /**
     * 专业方向
     */
    private String specialization;

    /**
     * 带队经验(年)
     */
    private Integer experience;

    /**
     * 办公室地点
     */
    private String officeLocation;

    /**
     * 办公时间
     */
    private String officeHours;

    /**
     * 个人简介
     */
    private String introduction;

    /**
     * 主键ID
     */
    private Long id;
}