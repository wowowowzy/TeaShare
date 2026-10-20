package com.example.tea.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // 本项目的登录鉴权由 JWT + MVC 拦截器完成（UserInterceptor / AdminInterceptor），
                // Spring Security 侧没有 Authentication 上下文，所以这里必须统一放行，
                // 否则 anyRequest().authenticated() 会把所有请求拦成 401/403。
                // 真正的“公开接口”定义在 WebMvcConfig#addInterceptors 的 excludePathPatterns 中；
                // /api/user/upload 需要登录（拦截器校验 token 并写入 ThreadLocal），不在放行之列。
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
