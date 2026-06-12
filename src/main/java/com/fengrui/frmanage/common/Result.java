package com.fengrui.frmanage.common;

/**
 * 接口统一返回结果。
 *
 * @param <T> 响应数据类型
 */
public class Result<T> {

    private Integer code;

    private String message;

    private T data;

    public Result() {
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 返回默认成功响应。
     *
     * @param data 响应数据
     * @param <T> 响应数据类型
     * @return 统一返回结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 返回指定消息的成功响应。
     *
     * @param message 成功消息
     * @param data 响应数据
     * @param <T> 响应数据类型
     * @return 统一返回结果
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /**
     * 返回失败响应。
     *
     * @param code 业务状态码
     * @param message 失败消息
     * @param <T> 响应数据类型
     * @return 统一返回结果
     */
    public static <T> Result<T> failure(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
