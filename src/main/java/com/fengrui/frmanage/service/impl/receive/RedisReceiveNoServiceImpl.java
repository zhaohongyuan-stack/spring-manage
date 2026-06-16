package com.fengrui.frmanage.service.impl.receive;

import com.fengrui.frmanage.exception.BusinessException;
import com.fengrui.frmanage.service.receive.ReceiveNoService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 原子自增的领用单号服务实现。
 */
@Service
public class RedisReceiveNoServiceImpl implements ReceiveNoService {

    private static final String RECEIVE_NO_PREFIX = "LY";

    private static final String RECEIVE_NO_KEY_PREFIX = "receive_no:";

    private static final int MAX_DAILY_SEQUENCE = 999;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final StringRedisTemplate stringRedisTemplate;

    public RedisReceiveNoServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 生成领用单号。
     *
     * @return 领用单号
     */
    @Override
    public String generateReceiveNo() {
        String dateText = LocalDate.now().format(DATE_FORMATTER);
        String redisKey = RECEIVE_NO_KEY_PREFIX + dateText;
        Long sequence = stringRedisTemplate.opsForValue().increment(redisKey);
        if (sequence == null) {
            throw new BusinessException("领用单号生成失败");
        }
        if (sequence == 1L) {
            stringRedisTemplate.expire(redisKey, 2, TimeUnit.DAYS);
        }
        if (sequence > MAX_DAILY_SEQUENCE) {
            throw new BusinessException("当天领用单号流水已超过最大值");
        }
        return "%s-%s-%03d".formatted(RECEIVE_NO_PREFIX, dateText, sequence);
    }
}
