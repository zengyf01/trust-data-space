package com.tds.dos.msp.common.exception;

import lombok.Getter;

/**
 * Business exception for MSP module
 */
@Getter
public class BusinessException extends RuntimeException {
    private final int code;
    private final String message;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(String message) {
        this(400, message);
    }

    public BusinessException(int code) {
        this(code, "业务处理异常");
    }
}