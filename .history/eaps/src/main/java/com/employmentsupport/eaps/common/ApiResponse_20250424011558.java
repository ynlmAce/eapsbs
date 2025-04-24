package com.employmentsupport.eaps.common;

import lombok.Data;

/**
 * 统一API响应结果
 */
@Data
public class ApiResponse<T> {

    private int error;
    private T body;
    private String message;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setError(0);
        response.setBody(data);
        response.setMessage("");
        return response;
    }

    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setError(code);
        response.setMessage(message);
        return response;
    }

    public static <T> ApiResponse<T> businessError(String message) {
        return error(400, message);
    }

    public static <T> ApiResponse<T> unauthorized() {
        return error(401, "请先登录");
    }

    public static <T> ApiResponse<T> serverError() {
        return error(500, "系统内部错误");
    }
}