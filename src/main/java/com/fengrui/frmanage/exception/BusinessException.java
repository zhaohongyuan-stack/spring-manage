package com.fengrui.frmanage.exception;

import com.fengrui.frmanage.common.enums.BizErrorCode;

/**
 * 业务异常。
 */
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        this(400, message);
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 使用统一业务错误码构造异常。
     *
     * @param errorCode 业务错误码
     */
    public BusinessException(BizErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMessage());
    }

    public Integer getCode() {
        return code;
    }
}
