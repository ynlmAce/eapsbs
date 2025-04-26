package com.bs.eaps.entity;

import lombok.Data;

/**
 * HR联系信息实体类
 */
@Data
public class HrContact {

    /**
     * HR姓名
     */
    private String name;

    /**
     * HR电话
     */
    private String phone;

    /**
     * HR邮箱
     */
    private String email;

    /**
     * 工作时间
     */
    private String workTime;

    /**
     * 无参构造函数
     */
    public HrContact() {
    }

    /**
     * 从电话号码（Long类型）创建HR联系信息
     * 用于兼容数据库中只存了电话号码的情况
     */
    public HrContact(Long phoneNumber) {
        if (phoneNumber != null) {
            this.phone = String.valueOf(phoneNumber);
            this.name = "HR联系人";
            this.email = "";
            this.workTime = "周一至周五 9:00-18:00";
        }
    }

    /**
     * 从电话号码（String类型）创建HR联系信息
     * 用于兼容数据库中只存了电话号码的情况
     */
    public HrContact(String phoneNumber) {
        this.phone = phoneNumber;
        this.name = "HR联系人";
        this.email = "";
        this.workTime = "周一至周五 9:00-18:00";
    }

    /**
     * 完整构造函数
     */
    public HrContact(String name, String phone, String email, String workTime) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.workTime = workTime;
    }
}