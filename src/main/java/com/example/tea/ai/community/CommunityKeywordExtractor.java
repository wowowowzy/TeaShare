package com.example.tea.ai.community;

import com.example.tea.mapper.CommunityMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 帖子关键词抽取器。
 * <p>
 * 推荐算法用「帖子关键词」作为帖子的语义代理：它既是向量召回的 query，
 * 也是用户画像里可读的兴趣标签。这里统一「查缓存 → 回表取标题 → 调大模型抽取 → 回写缓存」，
 * 供消费者(异步预热)与召回侧(懒加载兜底)共用，避免同一段逻辑散落多处。
 */
@Slf4j
@Component
public class CommunityKeywordExtractor {

    /** 关键词缓存 key 前缀。 */
    public static final String POST_WORD_PREFIX = "postWord:";

    private static final String PROMPT = "请你严格按照规则执行：\n"
            + "1. 只从下面的标题中提取【一个】和茶叶相关的核心关键词\n"
            + "2. 只输出关键词，不要任何标点、解释、多余文字\n"
            + "3. 不要回答，不要描述，只返回结果\n";

    /** 关键词抽取属于工具型调用，使用无 Advisor 的客户端，避免污染记忆/RAG。 */
    @Resource(name = "utilityChatClient")
    private ChatClient utilityChatClient;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private final CommunityMapper communityMapper;

    public CommunityKeywordExtractor(CommunityMapper communityMapper) {
        this.communityMapper = communityMapper;
    }

    /**
     * 取帖子关键词：优先缓存，缓存未命中则由大模型抽取并回写缓存。
     *
     * @return 关键词；帖子不存在或抽取失败时返回 {@code null}
     */
    public String keywordOf(Long postId) {
        if (postId == null) {
            return null;
        }
        String cacheKey = POST_WORD_PREFIX + postId;
        String cached = (String) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null && !cached.isBlank()) {
            return cached;
        }
        String title = communityMapper.getTitle(postId);
        if (title == null || title.isBlank()) {
            return null;
        }
        String keyword = extract(title);
        if (keyword != null) {
            redisTemplate.opsForValue().set(cacheKey, keyword);
        }
        return keyword;
    }

    /**
     * 仅做「标题 → 关键词」抽取（不读写缓存），供已有标题的场景复用。
     *
     * @return 清洗后的关键词；失败返回 {@code null}
     */
    public String extract(String title) {
        if (title == null || title.isBlank()) {
            return null;
        }
        try {
            String raw = utilityChatClient.prompt(PROMPT)
                    .messages(UserMessage.builder().text(title).build())
                    .stream().content()
                    .collectList()
                    .map(list -> String.join("", list))
                    .block();
            if (raw == null || raw.isBlank()) {
                return null;
            }
            return raw.trim();
        } catch (Exception e) {
            log.warn("抽取帖子关键词失败: title=\"{}\" err={}", title, e.getMessage());
            return null;
        }
    }

    /** 删除帖子关键词缓存（帖子被修改/删除时调用）。 */
    public void evict(Long postId) {
        if (postId != null) {
            redisTemplate.delete(POST_WORD_PREFIX + postId);
        }
    }

}