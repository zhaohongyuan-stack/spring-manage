package com.fengrui.frmanage.service.inventory;

import com.fengrui.frmanage.dto.inventory.InitInventoryDTO;
import com.fengrui.frmanage.dto.inventory.InventoryQueryDTO;
import com.fengrui.frmanage.dto.inventory.InventoryRecordQueryDTO;
import com.fengrui.frmanage.vo.PageResultVO;
import com.fengrui.frmanage.vo.inventory.InventoryRecordVO;
import com.fengrui.frmanage.vo.inventory.InventoryVO;
import com.fengrui.frmanage.vo.inventory.InventoryWarningVO;

/**
 * 库存业务服务。
 */
public interface InventoryService {

    /**
     * 分页查询当前库存。
     *
     * @param queryDTO 查询参数
     * @return 当前库存分页数据
     */
    PageResultVO<InventoryVO> pageInventory(InventoryQueryDTO queryDTO);

    /**
     * 分页查询库存预警。
     *
     * @param queryDTO 查询参数
     * @return 库存预警分页数据
     */
    PageResultVO<InventoryWarningVO> pageWarning(InventoryQueryDTO queryDTO);

    /**
     * 分页查询库存流水。
     *
     * @param queryDTO 查询参数
     * @return 库存流水分页数据
     */
    PageResultVO<InventoryRecordVO> pageRecord(InventoryRecordQueryDTO queryDTO);

    /**
     * 初始化库存。
     *
     * @param initInventoryDTO 初始化参数
     */
    void initInventory(InitInventoryDTO initInventoryDTO);
}
