package com.example.tea.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ThreadPoolConfig {

    /**
     * 启动期初始化任务线程池。
     * <p>
     * 单线程串行执行：初始化里既有 embedding 也有逐帖调用大模型，本身就是 IO 密集，
     * 并发反而容易触发限流；同时避免和应用启动线程、Web 线程争抢。
     */
    @Bean("initExecutor")
    public ThreadPoolTaskExecutor initExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(1);
        executor.setThreadNamePrefix("app-init-");
        // 关闭时不等待未完成的初始化任务，避免拖慢停机
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.initialize();
        return executor;
    }
}
