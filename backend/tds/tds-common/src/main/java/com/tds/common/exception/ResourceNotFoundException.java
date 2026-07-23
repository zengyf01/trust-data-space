package com.tds.common.exception;

/**
 * 资源不存在异常
 */
public class ResourceNotFoundException extends RuntimeException {

    private int code = 404;

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, String id) {
        super(resource + "不存在: " + id);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}