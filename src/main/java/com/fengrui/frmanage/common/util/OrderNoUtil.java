package com.fengrui.frmanage.common.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 单据编号工具类。
 */
public final class OrderNoUtil {

    private static final String PURCHASE_PREFIX = "CG";

    private static final String INBOUND_PREFIX = "RK";

    private static final String RECEIVE_PREFIX = "LY";

    private static final int MAX_DAILY_SEQUENCE = 999;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 单机初始化阶段使用内存维护每日流水；后续集群部署时应替换为 Redis 自增或数据库序列表。
     */
    private static final ConcurrentMap<String, AtomicInteger> DAILY_SEQUENCE_MAP = new ConcurrentHashMap<>();

    private OrderNoUtil() {
    }

    /**
     * 生成采购单号，格式：CG-yyyyMMdd-三位流水。
     *
     * @return 采购单号
     */
    public static String generatePurchaseNo() {
        return generateOrderNo(PURCHASE_PREFIX);
    }

    /**
     * 生成入库单号，格式：RK-yyyyMMdd-三位流水。
     *
     * @return 入库单号
     */
    public static String generateInboundNo() {
        return generateOrderNo(INBOUND_PREFIX);
    }

    /**
     * 生成领用单号，格式：LY-yyyyMMdd-三位流水。
     *
     * @return 领用单号
     */
    public static String generateReceiveNo() {
        return generateOrderNo(RECEIVE_PREFIX);
    }

    /**
     * 根据单据前缀生成当天流水号。
     *
     * @param prefix 单据前缀
     * @return 单据编号
     */
    private static String generateOrderNo(String prefix) {
        String dateText = LocalDate.now().format(DATE_FORMATTER);
        String sequenceKey = prefix + ":" + dateText;
        int sequence = DAILY_SEQUENCE_MAP
                .computeIfAbsent(sequenceKey, key -> new AtomicInteger())
                .incrementAndGet();
        if (sequence > MAX_DAILY_SEQUENCE) {
            throw new IllegalStateException("当天单据流水号已超过最大值");
        }
        return "%s-%s-%03d".formatted(prefix, dateText, sequence);
    }
}
