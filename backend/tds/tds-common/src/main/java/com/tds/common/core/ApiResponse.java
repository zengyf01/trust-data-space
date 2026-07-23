package com.tds.common.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

/**
 * 统一响应格式
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private int code;
    private String msg;
    private T data;
    private Long timestamp;

    private ApiResponse() {
        this.timestamp = Instant.now().toEpochMilli();
    }

    public static <T> ApiResponse<T> success() {
        ApiResponse<T> response = new ApiResponse<>();
        response.code = 200;
        response.msg = "success";
        return response;
    }

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.code = 200;
        response.msg = "success";
        response.data = data;
        return response;
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.code = code;
        response.msg = message;
        return response;
    }

    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.code = 500;
        response.msg = message;
        return response;
    }

    // Getters
    public int getCode() { return code; }
    public String getMsg() { return msg; }
    public T getData() { return data; }
    public Long getTimestamp() { return timestamp; }
}