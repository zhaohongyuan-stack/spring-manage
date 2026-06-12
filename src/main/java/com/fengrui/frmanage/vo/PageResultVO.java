package com.fengrui.frmanage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 分页响应数据。
 *
 * @param <T> 列表数据类型
 */
@Getter
@AllArgsConstructor
@Schema(description = "分页响应数据")
public class PageResultVO<T> {

    @Schema(description = "总记录数", example = "2")
    private Long total;

    @Schema(description = "当前页码", example = "1")
    private Long pageNum;

    @Schema(description = "每页条数", example = "10")
    private Long pageSize;

    @Schema(description = "当前页数据")
    private List<T> list;
}
