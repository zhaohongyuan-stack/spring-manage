package com.fengrui.frmanage;

import com.fengrui.frmanage.common.enums.BizErrorCode;
import com.fengrui.frmanage.exception.BusinessException;
import com.fengrui.frmanage.exception.GlobalExceptionHandler;
import com.fengrui.frmanage.service.impl.purchase.RedisPurchaseNoServiceImpl;
import com.fengrui.frmanage.service.purchase.PurchaseNoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * 采购单号服务测试。
 */
@ExtendWith(MockitoExtension.class)
class RedisPurchaseNoServiceImplTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private PurchaseNoService purchaseNoService;

    @BeforeEach
    void setUp() {
        purchaseNoService = new RedisPurchaseNoServiceImpl(stringRedisTemplate);
    }

    @Test
    void generatePurchaseNoShouldThrowBusinessErrorWhenRedisUnavailable() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(org.mockito.ArgumentMatchers.anyString()))
                .thenThrow(new RedisConnectionFailureException("Unable to connect to Redis"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> purchaseNoService.generatePurchaseNo()
        );
        assertEquals(BizErrorCode.REDIS_UNAVAILABLE.getCode(), exception.getCode());
        assertEquals(BizErrorCode.REDIS_UNAVAILABLE.getMessage(), exception.getMessage());
    }
}
