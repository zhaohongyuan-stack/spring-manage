package com.fengrui.frmanage.vo.dict;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 字典项响应数据。
 */
@Getter
@AllArgsConstructor
@Schema(description = "字典项响应数据")
public class DictItemVO {

    @Schema(description = "字典编码", example = "admin")
    private String code;

    @Schema(description = "字典名称", example = "系统管理员")
    private String name;
}
