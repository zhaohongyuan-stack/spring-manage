package com.fengrui.frmanage.service.inbound;

import com.fengrui.frmanage.dto.inbound.AddInboundDTO;
import com.fengrui.frmanage.dto.inbound.InboundConfirmDTO;
import com.fengrui.frmanage.dto.inbound.InboundListQueryDTO;
import com.fengrui.frmanage.vo.PageResultVO;
import com.fengrui.frmanage.vo.inbound.InboundDetailVO;
import com.fengrui.frmanage.vo.inbound.InboundListItemVO;

import java.util.Map;

/**
 * 入库业务服务。
 */
public interface InboundService {

    /**
     * 新增入库单。
     *
     * @param addInboundDTO 新增入库单参数
     * @return 入库单ID和单号
     */
    Map<String, Object> addInbound(AddInboundDTO addInboundDTO);

    /**
     * 分页查询入库单。
     *
     * @param queryDTO 查询参数
     * @return 入库单分页列表
     */
    PageResultVO<InboundListItemVO> pageList(InboundListQueryDTO queryDTO);

    /**
     * 查询入库单详情。
     *
     * @param inboundId 入库单ID
     * @return 入库单详情
     */
    InboundDetailVO detail(Long inboundId);

    /**
     * 验收入库单。
     *
     * @param confirmDTO 验收参数
     */
    void confirm(InboundConfirmDTO confirmDTO);
}
