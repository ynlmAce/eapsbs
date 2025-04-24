package com.bs.eaps.controller;

import com.bs.eaps.common.ApiResponse;
import com.bs.eaps.controller.AuthController.ResetPasswordRequest;
import com.bs.eaps.controller.AuthController.SecurityQuestionResponse;
import com.bs.eaps.controller.AuthController.VerifySecurityAnswerRequest;
import com.bs.eaps.controller.AuthController.VerifyUsernameRequest;
import com.bs.eaps.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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
}