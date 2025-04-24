package com.bs.eaps.common;

/**
 * 自定义业务异常
 */
public class BusinessException extends RuntimeException {

    /**
     * 异常代码
     */
    private Integer code;

    /**
     * 创建业务异常
     *
     * @param message 异常消息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    /**
     * 创建业务异常
     *
     * @param code    异常代码
     * @param message 异常消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 获取异常代码
     *
     * @return 异常代码
     */
    public Integer getCode() {
        return code;
    }
}