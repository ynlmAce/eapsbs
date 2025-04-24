package com.employmentsupport.eaps.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 注册请求DTO
 */
@Data
public class RegisterRequest {

    /**
     * 用户名/学号/工号
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$", message = "密码必须至少包含8个字符，至少一个大写字母，一个小写字母和一个数字")
    private String password;

    /**
     * 确认密码
     */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    /**
     * 角色：student-学生, company-企业, counselor-辅导员
     */
    @NotBlank(message = "角色不能为空")
    private String role;

    /**
     * 联系方式
     */
    @NotBlank(message = "联系方式不能为空")
    private String contact;

    /**
     * 姓名/企业名称
     */
    @NotBlank(message = "姓名/企业名称不能为空")
    private String name;

    /**
     * 统一社会信用代码（企业）
     */
    private String unifiedSocialCreditCode;

    /**
     * 学校（学生）
     */
    private String school;

    /**
     * 专业（学生）
     */
    private String major;
}