package com.fengrui.frmanage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 基础配置。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 临时放行开关：当前先闭环基础信息模块，JWT 延后开发。
     * 正式接入 JWT 后改为 false，并将 anyRequest 调整为 authenticated 或角色权限规则。
     */
    private static final boolean TEMPORARY_PERMIT_ALL = true;

    /**
     * 配置安全过滤链。
     * 临时策略：放行所有接口，便于基础模块联调。
     * 正式策略：保留 Swagger 放行，业务接口通过 JWT 和角色权限拦截。
     *
     * @param http HTTP 安全配置
     * @return 安全过滤链
     * @throws Exception Spring Security 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(registry -> {
                    registry.requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll();
                    if (TEMPORARY_PERMIT_ALL) {
                        registry.anyRequest().permitAll();
                    } else {
                        registry.anyRequest().authenticated();
                    }
                })
                .build();
    }

    /**
     * 用户密码加密器。
     *
     * @return BCrypt 密码加密器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
