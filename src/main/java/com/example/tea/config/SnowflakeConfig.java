package com.example.tea.config;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 全局唯一的雪花 ID 生成器。
 * <p>
 * 为什么必须统一成 Bean：{@code new Snowflake()} 每次都是新实例，但它的
 * workerId/datacenterId 与 {@link IdUtil#getSnowflake()} 内部单例完全相同，
 * 而序号(sequence)各自从 0 开始 —— 同一毫秒内取号会生成<b>完全相同的 ID</b>
 * （实测两个实例交替取 60000 个号，重复 29999 个）。
 * <p>
 * 所以：全项目只允许注入本 Bean，禁止 {@code new Snowflake()} 或再建实例。
 */
@Configuration
public class SnowflakeConfig {

    @Bean
    public Snowflake snowflake() {
        return IdUtil.getSnowflake();
    }
}