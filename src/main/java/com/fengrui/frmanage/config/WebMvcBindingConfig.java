package com.fengrui.frmanage.config;

import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * Query 参数绑定配置：空字符串视为未传参（null），避免可选筛选条件必须手写 null。
 */
@ControllerAdvice
public class WebMvcBindingConfig {

    /**
     * 注册空字符串转 null 的编辑器，用于 GET 列表等 @ModelAttribute 绑定。
     *
     * @param binder 数据绑定器
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
        binder.registerCustomEditor(Long.class, new CustomNumberEditor(Long.class, true));
        binder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, true));
    }
}
