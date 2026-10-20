package com.example.tea.service;

import com.example.tea.config.CacheKeys;
import com.example.tea.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 注销 token 黑名单。
 * <p>
 * JWT 是无状态的，服务端没法像 Session 那样直接删掉登录态，只能在注销时
 * 记住「这个 token 已作废」：把 token 的摘要写入 Redis，TTL 与 token 剩余
 * 有效期一致，到期自动清理，黑名单不会无限增长。
 * <p>
 * 这里使用 token 的 MD5 作为 key，避免把完整凭证明文落到 Redis key 里。
 * Redis 不可用时按「未注销」处理（fail-open），否则缓存故障会导致全站 401。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate stringRedisTemplate;

    private final JwtUtil jwtUtil;

    /**
     * 注销 token。幂等：token 为空或已过期时无需写入。
     */
    public void revoke(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        long ttlMillis = jwtUtil.getRemainingTtlMillis(token);
        if (ttlMillis <= 0) {
            return;
        }
        try {
            stringRedisTemplate.opsForValue()
                    .set(key(token), "1", ttlMillis, TimeUnit.MILLISECONDS);
            log.info("token 已加入注销黑名单，剩余有效期 {} ms", ttlMillis);
        } catch (Exception e) {
            log.warn("写入注销黑名单失败（Redis 不可用），本次注销仅清理客户端凭证：{}", e.getMessage());
        }
    }

    /**
     * token 是否已被注销。
     */
    public boolean isRevoked(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key(token)));
        } catch (Exception e) {
            log.warn("查询注销黑名单失败（Redis 不可用），按未注销处理：{}", e.getMessage());
            return false;
        }
    }

    private String key(String token) {
        String digest = DigestUtils.md5DigestAsHex(token.getBytes(StandardCharsets.UTF_8));
        return CacheKeys.LOGOUT_TOKEN_PREFIX + digest;
    }
}
