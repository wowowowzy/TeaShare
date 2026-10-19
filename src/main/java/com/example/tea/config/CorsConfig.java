package com.example.tea.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        // 1. 创建 CORS 配置对象
        CorsConfiguration config = new CorsConfiguration();
        // 允许的源
        // 生产域名（前台 https://wowowzy.art/tea/ 与后台 https://wowowzy.art/tea/admin/）
        config.addAllowedOriginPattern("https://wowowzy.art");
        config.addAllowedOriginPattern("https://*.wowowzy.art");
        config.addAllowedOriginPattern("http://wowowzy.art");
        config.addAllowedOriginPattern("http://*.wowowzy.art");
        // 旧服务器地址（保留兼容）
        config.addAllowedOriginPattern("http://101.43.39.143:666");
        config.addAllowedOriginPattern("http://101.43.39.143:10086");
        // 本地开发（Vite dev server）
        config.addAllowedOriginPattern("http://localhost:*");
        config.addAllowedOriginPattern("http://127.0.0.1:*");
//        config.addAllowedOriginPattern("*");
        // 允许携带 Cookie（前后端分离带 Token 时需开启）
        config.setAllowCredentials(true);
        // 允许的请求方法（GET/POST/PUT/DELETE 等）
        config.addAllowedMethod("*");
        // 允许的请求头（如 Authorization/Content-Type 等）
        config.addAllowedHeader("*");
        // 预检请求有效期（秒），避免频繁发 OPTIONS 请求
        config.setMaxAge(3600L);

        // 2. 配置生效路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 所有接口都生效
        source.registerCorsConfiguration("/**", config);

        // 3. 返回过滤器
        return new CorsFilter(source);
    }
}
