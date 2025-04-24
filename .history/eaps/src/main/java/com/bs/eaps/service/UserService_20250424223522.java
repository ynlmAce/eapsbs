package com.bs.eaps.service;

import com.bs.eaps.dto.LoginRequest;
import com.bs.eaps.dto.RegisterRequest;
import com.bs.eaps.dto.UserInfo;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求
     * @return 用户信息
     */
    UserInfo login(LoginRequest loginRequest);

    /**
     * 用户注册
     * 
     * @param registerRequest 注册请求
     * @return 是否注册成功
     */
    boolean register(RegisterRequest registerRequest);

    /**
     * 修改密码
     * 
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 根据用户名查询用户信息
     * 
     * @param username 用户名
     * @return 用户信息
     */
    UserInfo getUserByUsername(String username);

    /**
     * 根据用户ID查询用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    UserInfo getUserById(Long userId);

    /**
     * 获取用户密保问题
     * 
     * @param username 用户名
     * @return 密保问题
     */
    String getSecurityQuestion(String username);

    /**
     * 验证密保答案
     * 
     * @param username       用户名
     * @param securityAnswer 密保答案
     * @return 是否验证成功
     */
    boolean verifySecurityAnswer(String username, String securityAnswer);

    /**
     * 重置密码
     * 
     * @param username       用户名
     * @param securityAnswer 密保答案
     * @param newPassword    新密码
     * @return 是否重置成功
     */
    boolean resetPassword(String username, String securityAnswer, String newPassword);
}