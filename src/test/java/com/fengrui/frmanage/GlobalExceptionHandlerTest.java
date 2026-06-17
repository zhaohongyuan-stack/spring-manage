package com.fengrui.frmanage;

import com.fengrui.frmanage.common.enums.BizErrorCode;
import com.fengrui.frmanage.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 全局异常处理器测试。
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    void handleRedisConnectionFailureShouldReturnSpecificCode() {
        var result = globalExceptionHandler.handleRedisConnectionFailureException(
                new RedisConnectionFailureException("Unable to connect to Redis")
        );

        assertEquals(BizErrorCode.REDIS_UNAVAILABLE.getCode(), result.getCode());
        assertEquals(BizErrorCode.REDIS_UNAVAILABLE.getMessage(), result.getMessage());
    }
}
