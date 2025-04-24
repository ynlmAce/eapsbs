package com.example.eaps.model;

/**
 * API统一响应封装
 */
public class ApiResponse {

    private int error;
    private Object body;
    private String message;

    public ApiResponse() {
    }

    public ApiResponse(int error, Object body, String message) {
        this.error = error;
        this.body = body;
        this.message = message;
    }

    /**
     * 成功响应，不带数据
     */
    public static ApiResponse success() {
        return new ApiResponse(0, null, "");
    }

    /**
     * 成功响应，带数据
     */
    public static ApiResponse success(Object data) {
        return new ApiResponse(0, data, "");
    }

    /**
     * 业务异常响应
     */
    public static ApiResponse error(String message) {
        return new ApiResponse(400, null, message);
    }

    /**
     * 系统异常响应
     */
    public static ApiResponse systemError(String message) {
        return new ApiResponse(500, null, message);
    }

    /**
     * 未登录响应
     */
    public static ApiResponse unauthorized() {
        return new ApiResponse(401, null, "未登录或登录已过期");
    }

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}