package com.example.tea.config;

import com.example.tea.interceptor.AdminInterceptor;
import com.example.tea.interceptor.UserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private UserInterceptor userInterceptor;
    @Autowired
    private AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. 注册用户拦截器（先执行）
        registry.addInterceptor(userInterceptor)
                .addPathPatterns("/api/user/**") // 拦截所有/api开头的接口
                .excludePathPatterns(       // 放行公开接口
                        "/api/user/register/**",
                        "/api/user/login/**",
                        "/api/user/logout/**",
                        "/api/user/findAllGoods/**",
                        "/api/user/findGoodById/**",
                        "/api/user/captcha/**"

                );

        // 2. 注册管理员拦截器（后执行，仅拦截后台管理接口）
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/api/admin/**")// 仅拦截/admin开头的后台接口
                .excludePathPatterns(
                        "/api/admin/user/login/**"
                );
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(asyncTaskExecutor());
        // 可选：设置异步请求超时时间（毫秒），默认是 10000ms (10秒)
        configurer.setDefaultTimeout(30000);
    }

    @Bean("asyncTaskExecutor")
    public ThreadPoolTaskExecutor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(200);
        // 线程空闲存活时间
        executor.setKeepAliveSeconds(60);
        // 线程名前缀，方便排查问题
        executor.setThreadNamePrefix("async-mvc-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
