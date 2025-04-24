package com.bs.eaps.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一API响应结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    /**
     * 错误码，0表示没有异常
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
     *
     * @param data 响应数据
     * @return API响应对象
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, data, "");
    }

    /**
     * 错误响应
     *
     * @param code    错误码
     * @param message 错误信息
     * @return API响应对象
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, null, message);
    }

    /**
     * 业务错误响应
     *
     * @param message 错误信息
     * @return API响应对象
     */
    public static <T> ApiResponse<T> businessError(String message) {
        return new ApiResponse<>(400, null, message);
    }

    /**
     * 系统错误响应
     *
     * @param message 错误信息
     * @return API响应对象
     */
    public static <T> ApiResponse<T> systemError(String message) {
        return new ApiResponse<>(500, null, message);
    }

    /**
     * 未授权错误响应
     *
     * @return API响应对象
     */
    public static <T> ApiResponse<T> unauthorized() {
        return new ApiResponse<>(401, null, "请先登录");
    }
}