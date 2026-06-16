package com.fengrui.frmanage.vo.product;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品列表响应数据。
 */
@Getter
@Setter
@Schema(description = "商品列表响应数据")
public class ProductVO {

    @Schema(description = "商品ID", example = "1")
    private Long productId;

    @Schema(description = "商品名称", example = "一次性牙具套装")
    private String productName;

    @Schema(description = "商品分类", example = "客房用品")
    private String category;

    @Schema(description = "规格型号", example = "6g牙膏+软毛牙刷")
    private String spec;

    @Schema(description = "单位", example = "套")
    private String unit;

    @Schema(description = "成本单价", example = "2.20")
    private BigDecimal costPrice;

    @Schema(description = "预警阈值", example = "50")
    private Integer warningThreshold;

    @Schema(description = "状态：1启用，0禁用", example = "1")
    private Short status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间", example = "2026-06-01 08:00:00")
    private LocalDateTime createTime;
}
