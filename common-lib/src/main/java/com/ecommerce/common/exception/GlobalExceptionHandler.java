package com.ecommerce.common.exception;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.CommonResultCode;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("Business exception occurred: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getResultCode()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResponse<Void>> handleValidationException(Exception e) {
        String errorMessage = "잘못된 파라미터값 입니다.";
        
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            if (ex.getBindingResult().hasFieldErrors()) {
                errorMessage = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
            }
        } else if (e instanceof BindException) {
            BindException ex = (BindException) e;
            if (ex.getBindingResult().hasFieldErrors()) {
                errorMessage = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
            }
        }
        
        log.warn("Validation exception occurred: {}", errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(CommonResultCode.INVALID_PARAMETER, errorMessage));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        String errorMessage = "잘못된 요청 형식입니다.";
        
        // Enum 변환 에러인지 확인
        if (e.getCause() instanceof InvalidFormatException) {
            InvalidFormatException invalidFormatException = (InvalidFormatException) e.getCause();
            
            // Enum 타입 에러인지 확인
            if (invalidFormatException.getTargetType() != null && 
                invalidFormatException.getTargetType().isEnum()) {
                
                String fieldName = getFieldName(invalidFormatException);
                String invalidValue = String.valueOf(invalidFormatException.getValue());
                String validValues = getEnumValues(invalidFormatException.getTargetType());
                
                errorMessage = String.format("%s 필드에 잘못된 값 '%s'이 입력되었습니다. 유효한 값: %s", 
                    fieldName, invalidValue, validValues);
            }
        }
        
        log.warn("HTTP message not readable: {}", errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(CommonResultCode.INVALID_PARAMETER, errorMessage));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException e) {
        log.warn("Authentication failed: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(CommonResultCode.UNAUTHORIZED));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(CommonResultCode.FORBIDDEN));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Invalid argument: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(CommonResultCode.INVALID_PARAMETER, e.getMessage()));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataAccessException(DataAccessException e) {
        log.error("Database error occurred", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(CommonResultCode.DATABASE_ERROR));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception e) {
        log.error("Unexpected error occurred", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(CommonResultCode.SERVER_ERROR));
    }
    
    /**
     * JsonMappingException에서 필드명 추출
     */
    private String getFieldName(JsonMappingException e) {
        if (e.getPath() != null && !e.getPath().isEmpty()) {
            return e.getPath().get(e.getPath().size() - 1).getFieldName();
        }
        return "알 수 없는 필드";
    }
    
    /**
     * Enum 클래스의 유효한 값들을 문자열로 반환
     */
    private String getEnumValues(Class<?> enumClass) {
        if (enumClass.isEnum()) {
            Object[] enumConstants = enumClass.getEnumConstants();
            return Arrays.toString(enumConstants);
        }
        return "알 수 없음";
    }
}