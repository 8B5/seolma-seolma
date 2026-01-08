package com.ecommerce.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private String code;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
            CommonResultCode.SUCCESS.getCode(),
            CommonResultCode.SUCCESS.getMessage(),
            data,
            LocalDateTime.now()
        );
    }
    
    public static ApiResponse<Void> success() {
        return new ApiResponse<>(
            CommonResultCode.SUCCESS.getCode(),
            CommonResultCode.SUCCESS.getMessage(),
            null,
            LocalDateTime.now()
        );
    }
    
    public static <T> ApiResponse<T> error(ResultCode resultCode) {
        return new ApiResponse<>(
            resultCode.getCode(),
            resultCode.getMessage(),
            null,
            LocalDateTime.now()
        );
    }
    
    public static <T> ApiResponse<T> error(ResultCode resultCode, String customMessage) {
        return new ApiResponse<>(
            resultCode.getCode(),
            customMessage,
            null,
            LocalDateTime.now()
        );
    }
}