package com.fengrui.frmanage.exception;

import com.fengrui.frmanage.common.Result;
import com.fengrui.frmanage.common.enums.BizErrorCode;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常。
     *
     * @param exception 业务异常
     * @return 统一失败响应
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBusinessException(BusinessException exception) {
        return Result.failure(exception.getCode(), exception.getMessage());
    }

    /**
     * 处理业务参数异常。
     *
     * @param exception 参数异常
     * @return 统一失败响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException exception) {
        return Result.failure(HttpStatus.BAD_REQUEST.value(), exception.getMessage());
    }

    /**
     * 处理 JSON 请求体参数校验异常。
     *
     * @param exception 参数校验异常
     * @return 统一失败响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        return Result.failure(HttpStatus.BAD_REQUEST.value(), getValidateMessage(exception));
    }

    /**
     * 处理 Query 参数绑定校验异常。
     *
     * @param exception 参数绑定异常
     * @return 统一失败响应
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException exception) {
        return Result.failure(HttpStatus.BAD_REQUEST.value(), getValidateMessage(exception));
    }

    /**
     * 处理单个请求参数校验异常。
     *
     * @param exception 参数校验异常
     * @return 统一失败响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations()
                .stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.joining("；"));
        return Result.failure(HttpStatus.BAD_REQUEST.value(), message);
    }

    /**
     * 处理缺少必填请求参数异常。
     *
     * @param exception 缺少请求参数异常
     * @return 统一失败响应
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException exception) {
        return Result.failure(HttpStatus.BAD_REQUEST.value(), exception.getParameterName() + "不能为空");
    }

    /**
     * 处理 Redis 连接失败异常。
     *
     * @param exception Redis 连接异常
     * @return 统一失败响应
     */
    @ExceptionHandler(RedisConnectionFailureException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Result<Void> handleRedisConnectionFailureException(RedisConnectionFailureException exception) {
        log.error("Redis 连接失败", exception);
        return Result.failure(BizErrorCode.REDIS_UNAVAILABLE.getCode(), BizErrorCode.REDIS_UNAVAILABLE.getMessage());
    }

    /**
     * 处理数据库访问异常。
     *
     * @param exception 数据库访问异常
     * @return 统一失败响应
     */
    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleDataAccessException(DataAccessException exception) {
        log.error("数据库操作异常", exception);
        return Result.failure(BizErrorCode.DATABASE_ERROR.getCode(), BizErrorCode.DATABASE_ERROR.getMessage());
    }

    /**
     * 处理未被业务显式捕获的异常。
     *
     * @param exception 系统异常
     * @return 统一失败响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception exception) {
        log.error("系统异常", exception);
        return Result.failure(HttpStatus.INTERNAL_SERVER_ERROR.value(), "系统异常，请稍后重试");
    }

    /**
     * 提取字段校验错误消息。
     *
     * @param exception 参数绑定异常
     * @return 校验错误消息
     */
    private String getValidateMessage(BindException exception) {
        return exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
    }
}
