package com.dowson.codegen.exception;

import com.dowson.codegen.vo.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (var error : ex.getBindingResult().getAllErrors()) {
            String field = error instanceof FieldError ? ((FieldError) error).getField() : error.getObjectName();
            fieldErrors.put(field, error.getDefaultMessage());
        }
        return Result.failure(HttpStatus.BAD_REQUEST.value(), "参数验证失败", fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getConstraintViolations().forEach(v -> fieldErrors.put(v.getPropertyPath().toString(), v.getMessage()));
        return Result.failure(HttpStatus.BAD_REQUEST.value(), "参数验证失败", fieldErrors);
    }

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException ex) {
        return Result.failure(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException ex) {
        return Result.failure(HttpStatus.INTERNAL_SERVER_ERROR.value(), "业务处理异常");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleGenericException(Exception ex) {
        return Result.failure(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误，请稍后重试");
    }
}
