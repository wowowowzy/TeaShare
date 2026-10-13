package com.example.tea.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {

    @Bean("seckillExecutor")
    public ThreadPoolExecutor seckillExecutor() {
        return new ThreadPoolExecutor(
                2,
                6,
                10,
                TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(1000),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }
}
