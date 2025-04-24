package com.bs.eaps.controller;

import com.bs.eaps.common.ApiResponse;
import com.bs.eaps.dto.LoginRequest;
import com.bs.eaps.dto.RegisterRequest;
import com.bs.eaps.dto.UserInfo;
import com.bs.eaps.service.UserService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求
     * @return 用户信息
     */
    @PostMapping("/login")
    public ApiResponse<UserInfo> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("用户登录请求: {}", loginRequest.getUsername());
        UserInfo userInfo = userService.login(loginRequest);
        return ApiResponse.success(userInfo);
    }

    /**
     * 用户注册
     * 
     * @param registerRequest 注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    public ApiResponse<Boolean> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("用户注册请求: {}", registerRequest.getUsername());
        boolean success = userService.register(registerRequest);
        return ApiResponse.success(success);
    }

    /**
     * 修改密码
     * 
     * @param request     修改密码请求
     * @param userDetails 当前登录用户
     * @return 修改结果
     */
    @PostMapping("/changePassword")
    public ApiResponse<Boolean> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("用户修改密码请求: {}", userDetails.getUsername());
        UserInfo userInfo = userService.getUserByUsername(userDetails.getUsername());
        boolean success = userService.changePassword(userInfo.getId(), request.getOldPassword(),
                request.getNewPassword());
        return ApiResponse.success(success);
    }

    /**
     * 修改密码请求
     */
    @Data
    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;
    }
}