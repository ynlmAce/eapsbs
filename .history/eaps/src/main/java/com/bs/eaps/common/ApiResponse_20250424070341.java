package com.bs.eaps.common;

import lombok.Data;

/**
 * API响应通用类
 */
@Data
public class ApiResponse<T> {
    /**
     * 错误码，0表示成功
     */
    private Integer error;

    /**
     * 响应数据
     */
    private T body;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setError(0);
        response.setBody(data);
        response.setMessage("");
        return response;
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    /**
     * 错误响应
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setError(code);
        response.setBody(null);
        response.setMessage(message);
        return response;
    }

    /**
     * 系统异常
     */
    public static <T> ApiResponse<T> systemError(String message) {
        return error(500, message);
    }

    /**
     * 业务异常
     */
    public static <T> ApiResponse<T> businessError(String message) {
        return error(400, message);
    }

    /**
     * 未授权
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return error(401, message);
    }
}