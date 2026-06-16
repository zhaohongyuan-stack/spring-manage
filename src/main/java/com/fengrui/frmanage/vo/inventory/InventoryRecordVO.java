package com.fengrui.frmanage.vo.inventory;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 库存流水响应数据。
 */
@Getter
@Setter
@Schema(description = "库存流水响应数据")
public class InventoryRecordVO {

    @Schema(description = "流水ID", example = "301")
    private Long recordId;

    @Schema(description = "商品ID", example = "1")
    private Long productId;

    @Schema(description = "商品名称", example = "一次性牙具套装")
    private String productName;

    @Schema(description = "变动类型", example = "2")
    private Integer changeType;

    @Schema(description = "变动类型名称", example = "正常出库")
    private String changeTypeName;

    @Schema(description = "变动数量", example = "-50")
    private Integer changeQuantity;

    @Schema(description = "变动前库存", example = "100")
    private Integer beforeQuantity;

    @Schema(description = "变动后库存", example = "50")
    private Integer afterQuantity;

    @Schema(description = "关联单号", example = "LY-20260616-001")
    private String relatedNo;

    @Schema(description = "操作人ID", example = "1")
    private Long operatorId;

    @Schema(description = "操作人姓名", example = "管理员")
    private String operatorName;

    @Schema(description = "备注", example = "领用出库")
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间", example = "2026-06-16 10:00:00")
    private LocalDateTime createTime;
}
