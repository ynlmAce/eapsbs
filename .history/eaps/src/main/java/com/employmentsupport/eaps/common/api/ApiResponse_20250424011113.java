package com.employmentsupport.eaps.common.api;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一API响应结果
 */
@Data
public class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码：0表示成功，其他表示错误
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
     * 构造方法
     */
    public ApiResponse() {
        this.error = 0;
        this.message = "";
    }

    /**
     * 构造方法
     *
     * @param error   错误码
     * @param body    响应数据
     * @param message 错误消息
     */
    public ApiResponse(Integer error, T body, String message) {
        this.error = error;
        this.body = body;
        this.message = message;
    }

    /**
     * 成功返回
     *
     * @param <T> 数据类型
     * @return ApiResult对象
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(0, null, "");
    }

    /**
     * 成功返回（带数据）
     *
     * @param body 响应数据
     * @param <T>  数据类型
     * @return ApiResult对象
     */
    public static <T> ApiResponse<T> success(T body) {
        return new ApiResponse<>(0, body, "");
    }

    /**
     * 系统异常返回
     *
     * @param <T> 数据类型
     * @return ApiResult对象
     */
    public static <T> ApiResponse<T> error() {
        return new ApiResponse<>(500, null, "系统异常，请稍后重试");
    }

    /**
     * 系统异常返回（带错误消息）
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return ApiResult对象
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(500, null, message);
    }

    /**
     * 业务异常返回
     *
     * @param error   错误码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return ApiResult对象
     */
    public static <T> ApiResponse<T> error(Integer error, String message) {
        return new ApiResponse<>(error, null, message);
    }

    /**
     * 未登录状态返回
     *
     * @param <T> 数据类型
     * @return ApiResult对象
     */
    public static <T> ApiResponse<T> unauthorized() {
        return new ApiResponse<>(401, null, "请先登录");
    }
}