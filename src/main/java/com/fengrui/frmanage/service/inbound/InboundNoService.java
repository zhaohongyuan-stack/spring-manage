package com.fengrui.frmanage.service.inbound;

/**
 * 入库单号服务。
 */
public interface InboundNoService {

    /**
     * 生成入库单号。
     *
     * @return 入库单号
     */
    String generateInboundNo();
}
