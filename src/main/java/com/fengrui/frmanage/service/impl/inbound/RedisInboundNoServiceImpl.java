package com.fengrui.frmanage.service.impl.inbound;

import com.fengrui.frmanage.exception.BusinessException;
import com.fengrui.frmanage.service.inbound.InboundNoService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 原子自增的入库单号服务实现。
 */
@Service
public class RedisInboundNoServiceImpl implements InboundNoService {

    private static final String INBOUND_NO_PREFIX = "RK";

    private static final String INBOUND_NO_KEY_PREFIX = "inbound_no:";

    private static final int MAX_DAILY_SEQUENCE = 999;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final StringRedisTemplate stringRedisTemplate;

    public RedisInboundNoServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 生成入库单号。
     *
     * @return 入库单号
     */
    @Override
    public String generateInboundNo() {
        String dateText = LocalDate.now().format(DATE_FORMATTER);
        String redisKey = INBOUND_NO_KEY_PREFIX + dateText;
        Long sequence = stringRedisTemplate.opsForValue().increment(redisKey);
        if (sequence == null) {
            throw new BusinessException("入库单号生成失败");
        }
        if (sequence == 1L) {
            stringRedisTemplate.expire(redisKey, 2, TimeUnit.DAYS);
        }
        if (sequence > MAX_DAILY_SEQUENCE) {
            throw new BusinessException("当天入库单号流水已超过最大值");
        }
        return "%s-%s-%03d".formatted(INBOUND_NO_PREFIX, dateText, sequence);
    }
}
