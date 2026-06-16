package com.fengrui.frmanage.service.receive;

import com.fengrui.frmanage.dto.receive.AddReceiveDTO;
import com.fengrui.frmanage.dto.receive.ReceiveApproveDTO;
import com.fengrui.frmanage.dto.receive.ReceiveCancelDTO;
import com.fengrui.frmanage.dto.receive.ReceiveConfirmDTO;
import com.fengrui.frmanage.dto.receive.ReceiveListQueryDTO;
import com.fengrui.frmanage.vo.PageResultVO;
import com.fengrui.frmanage.vo.receive.ReceiveDetailVO;
import com.fengrui.frmanage.vo.receive.ReceiveListItemVO;

import java.util.Map;

/**
 * 领用业务服务。
 */
public interface ReceiveService {

    /**
     * 新增领用单。
     *
     * @param addReceiveDTO 新增领用单参数
     * @return 领用单ID和单号
     */
    Map<String, Object> addReceive(AddReceiveDTO addReceiveDTO);

    /**
     * 分页查询领用单。
     *
     * @param queryDTO 查询参数
     * @return 领用单分页列表
     */
    PageResultVO<ReceiveListItemVO> pageList(ReceiveListQueryDTO queryDTO);

    /**
     * 查询领用单详情。
     *
     * @param receiveId 领用单ID
     * @return 领用单详情
     */
    ReceiveDetailVO detail(Long receiveId);

    /**
     * 审批领用单。
     *
     * @param approveDTO 审批参数
     */
    void approve(ReceiveApproveDTO approveDTO);

    /**
     * 确认出库。
     *
     * @param confirmDTO 出库确认参数
     */
    void confirm(ReceiveConfirmDTO confirmDTO);

    /**
     * 取消领用单。
     *
     * @param cancelDTO 取消参数
     */
    void cancel(ReceiveCancelDTO cancelDTO);
}
