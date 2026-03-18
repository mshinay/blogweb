package com.boot.blogserver.handler;

import com.blog.exception.BaseException;
import com.blog.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Result<Object>> handleBaseException(BaseException ex) {
        log.warn("业务异常: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.valueOf(ex.getHttpStatus()))
                .body(Result.error(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldError() != null
                ? ex.getBindingResult().getFieldError().getDefaultMessage()
                : "请求参数不合法";
        return buildValidationError(message);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Object>> handleBindException(BindException ex) {
        String message = ex.getBindingResult().getFieldError() != null
                ? ex.getBindingResult().getFieldError().getDefaultMessage()
                : "请求参数不合法";
        return buildValidationError(message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<Object>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex) {
        return buildValidationError("缺少必要参数: " + ex.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Object>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {
        return buildValidationError("参数类型不正确: " + ex.getName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return buildValidationError("请求体格式不正确");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Object>> handleException(Exception ex) {
        log.error("系统异常", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(Result.INTERNAL_ERROR, "系统繁忙，请稍后重试"));
    }

    private ResponseEntity<Result<Object>> buildValidationError(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.error(Result.VALIDATION_ERROR, message));
    }
}

