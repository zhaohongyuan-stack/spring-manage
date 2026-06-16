package com.fengrui.frmanage.service.impl.purchase;

import com.fengrui.frmanage.exception.BusinessException;
import com.fengrui.frmanage.service.purchase.PurchaseNoService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 原子自增的采购单号服务实现。
 */
@Service
public class RedisPurchaseNoServiceImpl implements PurchaseNoService {

    private static final String PURCHASE_NO_PREFIX = "CG";

    private static final String PURCHASE_NO_KEY_PREFIX = "purchase_no:";

    private static final int MAX_DAILY_SEQUENCE = 999;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final StringRedisTemplate stringRedisTemplate;

    public RedisPurchaseNoServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 生成采购单号。
     *
     * @return 采购单号
     */
    @Override
    public String generatePurchaseNo() {
        String dateText = LocalDate.now().format(DATE_FORMATTER);
        String redisKey = PURCHASE_NO_KEY_PREFIX + dateText;
        Long sequence = stringRedisTemplate.opsForValue().increment(redisKey);
        if (sequence == null) {
            throw new BusinessException("采购单号生成失败");
        }
        if (sequence == 1L) {
            stringRedisTemplate.expire(redisKey, 2, TimeUnit.DAYS);
        }
        if (sequence > MAX_DAILY_SEQUENCE) {
            throw new BusinessException("当天采购单号流水已超过最大值");
        }
        return "%s-%s-%03d".formatted(PURCHASE_NO_PREFIX, dateText, sequence);
    }
}
