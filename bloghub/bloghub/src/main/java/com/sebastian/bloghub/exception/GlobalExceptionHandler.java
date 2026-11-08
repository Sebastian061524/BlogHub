package com.sebastian.bloghub.exception;

import com.sebastian.bloghub.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 统一转换 MVC 调用链中的异常，使错误响应也采用 Result 结构。
 * {@code @RestControllerAdvice} 为控制器提供全局异常建议，@ExceptionHandler 根据异常类型选择处理方法；
 * 具体类型的处理优先于 Exception 兜底，并非按方法的书写顺序匹配。
 * 400 表示请求数据有误；401 表示身份未通过；403 表示身份已知但权限不足；404 表示资源不存在；
 * 415 表示请求内容类型不受支持；500 表示服务端处理异常。HTTP 状态与响应体 status=0 分别表达两层含义。
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 保留业务层明确指定的 HTTP 状态与公开消息，例如重复注册的 409、无权限的 403。
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException ex){
        return ResponseEntity.status(ex.getHttpStatus()).body(Result.error(ex.getMessage()));
    }

    /**
     * 兜底处理其他异常：完整异常仅记入服务端日志，对客户端返回通用 500 消息，避免暴露内部细节。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception ex){
        log.error("未处理的服务器异常", ex);
        return ResponseEntity.status(500).body(Result.error("系统内部错误, 请稍后再试"));
    }

    /**
     * 处理 @Valid 触发的 DTO 字段约束失败，取一个字段错误消息并返回 400；没有字段错误时使用通用消息。
     */
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

    /**
     * 处理 URL 路径或查询参数无法转成方法声明类型的情况，例如把 abc 传给 Integer 页码，返回 400。
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex){
        return ResponseEntity.status(400).body(Result.error("请求参数类型不合法"));
    }

    /**
     * 处理 JSON 无法读取或绑定的情况，例如 JSON 格式错误、请求体缺失或字段类型不匹配，返回 400。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex){
        return ResponseEntity.status(400).body(Result.error("请求参数不合法"));
    }

    /**
     * 处理 Content-Type 与接口支持的请求体格式不匹配的情况，返回 415，而不是普通字段校验的 400。
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex){
        return ResponseEntity.status(415).body(Result.error("不支持的请求内容类型"));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNoResourceFoundException(NoResourceFoundException ex){
        return ResponseEntity.status(404).body(Result.error("请求的资源不存在"));
    }
}
