package com.bs.eaps.controller;

import com.bs.eaps.common.ApiResponse;
import com.bs.eaps.entity.User;
import com.bs.eaps.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
     * @param request 登录请求
     * @return 登录结果
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("登录请求: {}", request.getUsername());
        String token = userService.login(request.getUsername(), request.getPassword());
        return ApiResponse.success(new LoginResponse(token));
    }

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    public ApiResponse<User> register(@Valid @RequestBody RegisterRequest request) {
        log.info("注册请求: {}", request.getUsername());
        User user = userService.register(request);
        return ApiResponse.success(user);
    }

    /**
     * 修改密码
     *
     * @param request 修改密码请求
     * @return 修改结果
     */
    @PostMapping("/change-password")
    public ApiResponse<Boolean> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.info("修改密码请求: {}", request.getUsername());
        boolean success = userService.changePassword(request.getUsername(), request.getOldPassword(),
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

    @Data
    public static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        @NotBlank(message = "密码不能为空")
        private String password;
    }

    @Data
    public static class LoginResponse {
        private String token;

        public LoginResponse(String token) {
            this.token = token;
        }
    }

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        @NotBlank(message = "密码不能为空")
        private String password;
        @NotBlank(message = "角色不能为空")
        private String role;
        @NotBlank(message = "密保问题不能为空")
        private String securityQuestion;
        @NotBlank(message = "密保答案不能为空")
        private String securityAnswer;
    }

    @Data
    public static class ChangePasswordRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        @NotBlank(message = "旧密码不能为空")
        private String oldPassword;
        @NotBlank(message = "新密码不能为空")
        private String newPassword;
    }

    @Data
    public static class VerifyUsernameRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
    }

    @Data
    public static class SecurityQuestionResponse {
        private String securityQuestion;

        public SecurityQuestionResponse(String securityQuestion) {
            this.securityQuestion = securityQuestion;
        }
    }

    @Data
    public static class VerifySecurityAnswerRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        @NotBlank(message = "密保答案不能为空")
        private String securityAnswer;
    }

    @Data
    public static class ResetPasswordRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        @NotBlank(message = "密保答案不能为空")
        private String securityAnswer;
        @NotBlank(message = "新密码不能为空")
        private String newPassword;
    }
}