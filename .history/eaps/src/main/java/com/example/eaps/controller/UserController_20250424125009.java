package com.example.eaps.controller;

import com.example.eaps.model.ApiResponse;
import com.example.eaps.model.dto.UserLoginDTO;
import com.example.eaps.model.dto.UserRegisterDTO;
import com.example.eaps.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户认证相关接口
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse login(@RequestBody UserLoginDTO loginDTO) {
        try {
            String token = userService.login(loginDTO.getUsername(), loginDTO.getPassword(), loginDTO.getRole());
            return ApiResponse.success(token);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ApiResponse register(@RequestBody UserRegisterDTO registerDTO) {
        try {
            boolean result = userService.register(registerDTO);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取用户信息
     */
    @PostMapping("/info")
    public ApiResponse getUserInfo(HttpServletRequest request) {
        try {
            String token = request.getHeader("auth");
            Object userInfo = userService.getUserInfo(token);
            return ApiResponse.success(userInfo);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public ApiResponse changePassword(@RequestBody Object passwordData, HttpServletRequest request) {
        try {
            String token = request.getHeader("auth");
            boolean result = userService.changePassword(token, passwordData);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ApiResponse logout(HttpServletRequest request) {
        try {
            String token = request.getHeader("auth");
            boolean result = userService.logout(token);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}