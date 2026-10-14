package com.example.tea.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类（适配jjwt-jackson依赖）
 * <p>
 * 注意：这里的 ObjectMapper 必须<b>独立于全局 Bean</b>。本类会调用
 * {@code activateDefaultTyping} 写入类型信息（Redis 需要它来还原对象类型），
 * 而 Spring Boot 的全局 ObjectMapper 带 {@code @ConditionalOnMissingBean}，
 * 一旦被替换/复用，所有 HTTP 响应 JSON 都会多出 {@code "@class"} 字段。
 * 所以此处保留文件内独立单例（static final，线程安全）。
 */
@Slf4j
@Configuration
public class RedisConfiguration {

    /** 独立单例：仅用于 Redis 值序列化，禁止替换为全局 ObjectMapper Bean。 */
    private static final ObjectMapper REDIS_OBJECT_MAPPER = createRedisObjectMapper();

    private static ObjectMapper createRedisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return objectMapper;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("开始创建Redis模板对象");
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        // 1. 设置Redis连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 2. 字符串序列化器（key/hashKey）
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // 3. JSON序列化器（复用本类独立单例，避免全局污染）
        Jackson2JsonRedisSerializer<Object> jacksonSerializer = new Jackson2JsonRedisSerializer<>(REDIS_OBJECT_MAPPER, Object.class);


        // 4. 全局序列化配置
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(jacksonSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(jacksonSerializer);

        // 5. 初始化模板
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }
}
