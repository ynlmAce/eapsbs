package com.bs.eaps.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 用户信息DTO
 */
@Data
@Builder
public class UserInfo {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 角色
     */
    private String role;

    /**
     * 姓名/企业名称
     */
    private String name;

    /**
     * JWT令牌
     */
    private String token;
}