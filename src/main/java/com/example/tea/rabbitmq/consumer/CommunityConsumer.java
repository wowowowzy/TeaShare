package com.example.tea.rabbitmq.consumer;

import com.example.tea.ai.community.CommunityKeywordExtractor;
import com.example.tea.config.RabbitMQCommunityConfig;
import com.example.tea.entity.dto.Community.RabbitMQDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 社区行为异步消费者：把用户的浏览/点赞/评论/收藏行为累加成「帖子级」兴趣画像。
 * <p>
 * 画像结构：{@code userRecommend:{userId}} 为 ZSET，
 * <b>member = postId</b>，<b>score = 行为权重累加</b>。
 * 这里刻意不再用「关键词」当 member：不同帖子若抽到同一关键词会权重合并，
 * 导致「最高分关键词」并不能代表用户最感兴趣的帖子。关键词只作为标签写入
 * {@code postWord:{postId}}，供召回侧生成查询向量使用。
 */
@Slf4j
@Component
public class CommunityConsumer {

    /** 用户兴趣画像 key 前缀。 */
    public static final String USER_RECOMMEND_PREFIX = "userRecommend:";

    /** 画像有效期（分钟）。 */
    private static final int PROFILE_TTL_MINUTES = 30;

    @Autowired
    private CommunityKeywordExtractor keywordExtractor;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @RabbitListener(queues = RabbitMQCommunityConfig.COMMUNITY_QUEUE)
    public void receiveCommunityMessage(RabbitMQDTO message) {
        Long postId = message.getPostId();
        Long userId = message.getUserId();
        Long weight = message.getWeight();
        if (postId == null || userId == null || weight == null) {
            log.warn("社区消息字段缺失，已忽略: {}", message);
            return;
        }

        // 关键词仅作为帖子标签/召回 query（供向量检索），不参与权重累计
        keywordExtractor.keywordOf(postId);

        // 按帖子累计兴趣权重：不同帖子的同一关键词不再合并
        String userKey = USER_RECOMMEND_PREFIX + userId;
        try {
            redisTemplate.opsForZSet().incrementScore(userKey, postId.toString(), weight);
            redisTemplate.expire(userKey, PROFILE_TTL_MINUTES, TimeUnit.MINUTES);
            log.debug("画像更新成功: userId={} postId={} weight={}", userId, postId, weight);
        } catch (Exception e) {
            log.warn("画像更新失败: userId={} postId={} err={}", userId, postId, e.getMessage());
        }
    }

}