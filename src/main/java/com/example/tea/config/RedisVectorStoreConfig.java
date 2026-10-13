package com.example.tea.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

@Configuration
public class RedisVectorStoreConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private Integer redisPort;

    // Redis 连接
    @Bean
    public JedisPooled jedisPooled() {
        return new JedisPooled(redisHost, redisPort);
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