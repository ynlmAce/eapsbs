package com.jobhelper.dto.job;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 岗位发布DTO
 */
@Data
public class JobPublishDTO {

    /**
     * 岗位标题
     */
    private String title;

    /**
     * 岗位描述
     */
    private String description;

    /**
     * 岗位要求
     */
    private String requirement;

    /**
     * 工作地点
     */
    private String location;

    /**
     * 薪资范围
     */
    private String salary;

    /**
     * 学历要求
     */
    private String education;

    /**
     * 工作经验
     */
    private String experience;

    /**
     * 工作类型
     */
    private String jobType;

    /**
     * 招聘人数
     */
    private Integer headcount;

    /**
     * 工作时间
     */
    private String workTime;

    /**
     * 福利标签
     */
    private List<String> welfareTags;

    /**
     * 岗位标签
     */
    private List<String> jobTags;

    /**
     * 有效期
     */
    private LocalDateTime validUntil;

    /**
     * 联系人
     */
    private String contactPerson;

    /**
     * 联系方式
     */
    private String contactMethod;

    /**
     * 是否显示联系方式
     */
    private Boolean showContact;
}