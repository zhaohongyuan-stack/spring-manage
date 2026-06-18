package com.fengrui.frmanage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 新增用户响应数据。
 */
@Getter
@AllArgsConstructor
@Schema(description = "新增用户响应数据")
public class AddUserVO {

    @Schema(description = "【用户ID】新增成功返回的主键", example = "101")
    private Long userId;
}
