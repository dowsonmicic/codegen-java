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

/**
 * 全局异常处理器
 * 用于统一处理系统中抛出的异常，返回格式化的错误信息给客户端
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理方法参数验证异常
     * 当使用@Valid注解验证请求参数失败时会抛出此异常
     * 
     * @param ex MethodArgumentNotValidException异常实例
     * @return 包含验证错误信息的统一响应结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        // 遍历所有验证错误
        for (var error : ex.getBindingResult().getAllErrors()) {
            // 判断是否为字段错误
            String field = error instanceof FieldError ? ((FieldError) error).getField() : error.getObjectName();
            fieldErrors.put(field, error.getDefaultMessage());
        }
        return Result.failure(HttpStatus.BAD_REQUEST.value(), "参数验证失败", fieldErrors);
    }

    /**
     * 处理约束违反异常
     * 当使用@Validated注解在类级别验证失败时会抛出此异常
     * 
     * @param ex ConstraintViolationException异常实例
     * @return 包含验证错误信息的统一响应结果
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        // 遍历所有约束违反信息
        ex.getConstraintViolations().forEach(v -> fieldErrors.put(v.getPropertyPath().toString(), v.getMessage()));
        return Result.failure(HttpStatus.BAD_REQUEST.value(), "参数验证失败", fieldErrors);
    }

    /**
     * 处理业务异常
     * 当业务逻辑中抛出自定义BusinessException时会进入此处理方法
     * 
     * @param ex BusinessException异常实例
     * @return 包含业务错误码和错误信息的统一响应结果
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException ex) {
        return Result.failure(ex.getCode(), ex.getMessage());
    }

    /**
     * 处理运行时异常
     * 当系统发生未预期的运行时异常时会进入此处理方法
     * 
     * @param ex RuntimeException异常实例
     * @return 统一的系统错误响应结果
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException ex) {
        return Result.failure(HttpStatus.INTERNAL_SERVER_ERROR.value(), "业务处理异常");
    }

    /**
     * 处理通用异常
     * 作为兜底的异常处理方法，处理所有未被上述方法处理的异常
     * 
     * @param ex Exception异常实例
     * @return 统一的系统错误响应结果
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleGenericException(Exception ex) {
        return Result.failure(HttpStatus.INTERNAL_SERVER_ERROR.value(), "服务器内部错误，请稍后重试");
    }
}