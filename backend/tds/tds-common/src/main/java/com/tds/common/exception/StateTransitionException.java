package com.tds.common.exception;

/**
 * 状态流转异常
 */
public class StateTransitionException extends RuntimeException {

    private int code = 409;

    public StateTransitionException(String message) {
        super(message);
    }

    public StateTransitionException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}