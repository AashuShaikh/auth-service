package com.aashushaikh.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResponse<T> {

    private boolean status;
    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ErrorCode code;

    private T data;

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .status(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, ErrorCode code) {
        return ApiResponse.<T>builder()
                .status(false)
                .message(message)
                .code(code)
                .data(null)
                .build();
    }
}
