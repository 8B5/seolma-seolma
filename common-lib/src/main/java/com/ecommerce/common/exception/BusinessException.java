package com.ecommerce.common.exception;

import com.ecommerce.common.response.ResultCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    
    private final ResultCode resultCode;
    
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }
    
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }
    
    public BusinessException(ResultCode resultCode, Throwable cause) {
        super(resultCode.getMessage(), cause);
        this.resultCode = resultCode;
    }
}