package com.sebastian.bloghub.exception;

import com.sebastian.bloghub.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException ex){
        return ResponseEntity.status(ex.getHttpStatus()).body(Result.error(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception ex){
        log.error("未处理的服务器异常", ex);
        return ResponseEntity.status(500).body(Result.error("系统内部错误, 请稍后再试"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidationException(MethodArgumentNotValidException ex){
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message;
        if(fieldError != null){
            message = fieldError.getDefaultMessage();
        } else {
            message = "请求参数不合法";
        }
        return ResponseEntity.status(400).body(Result.error(message));
    }
}
