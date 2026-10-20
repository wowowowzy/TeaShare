package com.example.tea.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 一级缓存（本地 Caffeine）。
 * <p>
 * 该 Bean 存在时 Spring Boot 不会自动装配 Redis CacheManager，因此
 * {@code @Cacheable} 只作用于本地 Caffeine；Redis 作为二级缓存由业务代码
 * 通过 {@code RedisTemplate} 手动读写（见 {@link CacheKeys}）。
 * <p>
 * L1 过期时间应显著短于 L2：多实例部署时，别的实例改了数据只能删掉 Redis，
 * 删不掉本机 Caffeine，靠短 TTL 把脏读窗口控制住。
 */
@Configuration
public class CaffeineConfig {

    /** 商品详情一级缓存名称，需与 {@code @Cacheable(value = ...)} 保持一致。 */
    public static final String GOODS_CACHE = "goods";

    @Bean
    public CaffeineCacheManager caffeineCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(GOODS_CACHE);
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(1000));
        return manager;
    }
}
