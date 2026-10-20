package com.example.tea.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPooled;

import java.time.Duration;

/**
 * Redis 向量库配置。
 * <p>
 * 这里复用 Spring Boot 的 {@code spring.data.redis.*} 连接参数（host/port/password/ssl/timeout），
 * 但数据库必须单独指定：RediSearch/Redis Stack <b>只允许在 DB 0 创建向量索引</b>，
 * 在非 0 库执行 {@code FT.CREATE} 会直接报
 * {@code Cannot create index on db != 0}。
 * <p>
 * 因此本项目的实际布局是：
 * <ul>
 *   <li>向量库（RediSearch）：DB 0（由 {@code tea.vector.database} 控制，必须是 0）；</li>
 *   <li>业务缓存（RedisTemplate/StringRedisTemplate）：{@code spring.data.redis.database}（同样为 0）。</li>
 * </ul>
 */
@Configuration
public class RedisVectorStoreConfig {

    private final RedisProperties redisProperties;

    /** 向量库固定使用的 DB：RediSearch 索引只能建在 DB 0。 */
    @Value("${tea.vector.database:0}")
    private int vectorDatabase;

    public RedisVectorStoreConfig(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    /**
     * Redis 连接（与 RedisTemplate 共用同一个库、同一份认证信息）。
     */
    @Bean
    public JedisPooled jedisPooled() {
        // 与 spring.data.redis.timeout 保持一致；未配置时给一个保守的 1 秒
        Duration timeout = redisProperties.getTimeout() == null
                ? Duration.ofSeconds(1)
                : redisProperties.getTimeout();

        DefaultJedisClientConfig.Builder clientConfig = DefaultJedisClientConfig.builder()
                // RediSearch 不支持在非 0 库建索引，向量库必须固定 DB 0
                .database(vectorDatabase)
                .connectionTimeoutMillis((int) timeout.toMillis())
                .socketTimeoutMillis((int) timeout.toMillis());

        if (StringUtils.hasText(redisProperties.getUsername())) {
            clientConfig.user(redisProperties.getUsername());
        }
        if (StringUtils.hasText(redisProperties.getPassword())) {
            clientConfig.password(redisProperties.getPassword());
        }
        if (redisProperties.getSsl().isEnabled()) {
            clientConfig.ssl(true);
        }

        return new JedisPooled(
                new HostAndPort(redisProperties.getHost(), redisProperties.getPort()),
                clientConfig.build());
    }

    /**
     * RAG 知识库向量库。
     * 显式指定 HNSW 索引算法；召回参数(topK/相似度阈值)在检索侧通过 SearchRequest 控制。
     */
    @Bean("ragVectorStore")
    public VectorStore ragVectorStore(JedisPooled jedisPooled, EmbeddingModel embeddingModel) {
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName("custom-index")
                .prefix("custom-prefix")
                .vectorAlgorithm(RedisVectorStore.Algorithm.HNSW)
                .initializeSchema(true)
                .build();
    }

    /**
     * 社区帖子推荐向量库。
     */
    @Bean("recommendVectorStore")
    public VectorStore recommendVectorStore(JedisPooled jedisPooled, EmbeddingModel embeddingModel) {
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName("post_recommend_vector")
                .prefix("recommend:")
                .vectorAlgorithm(RedisVectorStore.Algorithm.HNSW)
                .initializeSchema(true)
                .build();
    }

    /**
     * 长期记忆向量库。
     * 必须把 {@code conversationId} 声明为 TAG 元数据字段，{@code VectorStoreChatMemoryAdvisor}
     * 会用它做按会话过滤检索。
     */
    @Bean("chatMemoryVectorStore")
    public VectorStore chatMemoryVectorStore(JedisPooled jedisPooled, EmbeddingModel embeddingModel) {
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName("chat_memory_vector")
                .prefix("chat-memory:")
                .vectorAlgorithm(RedisVectorStore.Algorithm.HNSW)
                .metadataFields(
                        RedisVectorStore.MetadataField.tag("conversationId"),
                        RedisVectorStore.MetadataField.tag("messageType"))
                .initializeSchema(true)
                .build();
    }

}
