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
     * 验证用户名并获取密保问题
     *
     * @param request 验证用户名请求
     * @return 密保问题
     */
    @PostMapping("/verify-username")
    public ApiResponse<SecurityQuestionResponse> verifyUsername(@Valid @RequestBody VerifyUsernameRequest request) {
        log.info("验证用户名请求: {}", request.getUsername());
        String securityQuestion = userService.getSecurityQuestion(request.getUsername());
        SecurityQuestionResponse response = new SecurityQuestionResponse(securityQuestion);
        return ApiResponse.success(response);
    }

    /**
     * 验证密保答案
     *
     * @param request 验证密保答案请求
     * @return 验证结果
     */
    @PostMapping("/verify-security-answer")
    public ApiResponse<Boolean> verifySecurityAnswer(@Valid @RequestBody VerifySecurityAnswerRequest request) {
        log.info("验证密保答案请求: {}", request.getUsername());
        boolean success = userService.verifySecurityAnswer(request.getUsername(), request.getSecurityAnswer());
        return ApiResponse.success(success);
    }

    /**
     * 重置密码
     *
     * @param request 重置密码请求
     * @return 重置结果
     */
    @PostMapping("/reset-password")
    public ApiResponse<Boolean> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("重置密码请求: {}", request.getUsername());
        boolean success = userService.resetPassword(request.getUsername(), request.getSecurityAnswer(),
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

    /**
     * 验证用户名请求
     */
    @Data
    public static class VerifyUsernameRequest {
        private String username;
    }

    /**
     * 密保问题响应
     */
    @Data
    public static class SecurityQuestionResponse {
        private String securityQuestion;

        public SecurityQuestionResponse(String securityQuestion) {
            this.securityQuestion = securityQuestion;
        }
    }

    /**
     * 验证密保答案请求
     */
    @Data
    public static class VerifySecurityAnswerRequest {
        private String username;
        private String securityAnswer;
    }

    /**
     * 重置密码请求
     */
    @Data
    public static class ResetPasswordRequest {
        private String username;
        private String securityAnswer;
        private String newPassword;
    }
}