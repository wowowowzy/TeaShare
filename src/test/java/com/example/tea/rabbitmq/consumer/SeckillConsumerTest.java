package com.example.tea.rabbitmq.consumer;

import com.example.tea.entity.dto.Goods.SeckillGoodsMessageDTO;
import com.example.tea.service.SeckillOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link SeckillConsumer} 的幂等 / 补偿单元测试（不依赖 Redis、MQ、MySQL）。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SeckillConsumerTest {

    private static final Long GOODS_ID = 1001L;
    private static final Long USER_ID = 2002L;

    private static final String CLAIM_KEY = "seckill:claim:" + GOODS_ID + ":" + USER_ID;
    private static final String DONE_KEY = "seckill:done:" + GOODS_ID + ":" + USER_ID;
    private static final String STOCK_KEY = "seckill:stock:" + GOODS_ID;
    private static final String USER_KEY = "seckill:user:" + GOODS_ID + ":" + USER_ID;

    private static final Duration CLAIM_TTL = Duration.ofSeconds(30);
    private static final Duration DONE_TTL = Duration.ofHours(2);

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SeckillOrderService seckillOrderService;

    private SeckillConsumer consumer;

    @BeforeEach
    void setUp() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        consumer = new SeckillConsumer(stringRedisTemplate, seckillOrderService);
    }

    @Test
    @DisplayName("正常消费：建单成功 → 写 done → 释放 claim")
    void shouldCreateOrderThenMarkDone() {
        when(stringRedisTemplate.hasKey(DONE_KEY)).thenReturn(false, false);
        when(valueOperations.setIfAbsent(eq(CLAIM_KEY), anyString(), eq(CLAIM_TTL))).thenReturn(true);

        consumer.seckill(message(GOODS_ID, USER_ID));

        verify(seckillOrderService, times(1)).createSeckillOrder(GOODS_ID, USER_ID);
        verify(valueOperations).set(eq(DONE_KEY), eq("1"), eq(DONE_TTL));
        verify(stringRedisTemplate).execute(any(RedisScript.class), eq(List.of(CLAIM_KEY)), anyString());
    }

    @Test
    @DisplayName("done 已存在：直接确认，不建单、不补偿")
    void shouldSkipWhenAlreadyDone() {
        when(stringRedisTemplate.hasKey(DONE_KEY)).thenReturn(true);

        consumer.seckill(message(GOODS_ID, USER_ID));

        verifyNoInteractions(seckillOrderService);
        verify(valueOperations, never()).setIfAbsent(anyString(), anyString(), any(Duration.class));
        verify(valueOperations, never()).increment(anyString());
    }

    @Test
    @DisplayName("并发重复消息：等待期间出现 done → 直接确认，不重复建单")
    void shouldSkipWhenConcurrentConsumerFinishes() {
        // 第 1 次：处理前检查 done = false；等待期间复查 done = true
        when(stringRedisTemplate.hasKey(DONE_KEY)).thenReturn(false, true);
        when(valueOperations.setIfAbsent(eq(CLAIM_KEY), anyString(), eq(CLAIM_TTL))).thenReturn(false);

        consumer.seckill(message(GOODS_ID, USER_ID));

        verifyNoInteractions(seckillOrderService);
        verify(valueOperations, never()).increment(anyString());
    }

    @Test
    @DisplayName("落库失败：补偿只执行一次，且不向外抛异常")
    void shouldCompensateOnlyOnceAndNotRethrow() {
        when(stringRedisTemplate.hasKey(DONE_KEY)).thenReturn(false, false);
        when(valueOperations.setIfAbsent(eq(CLAIM_KEY), anyString(), eq(CLAIM_TTL))).thenReturn(true);
        doThrow(new IllegalStateException("库存不足"))
                .when(seckillOrderService).createSeckillOrder(GOODS_ID, USER_ID);

        assertDoesNotThrow(() -> consumer.seckill(message(GOODS_ID, USER_ID)));

        // 100/300/900ms 三次重试后最终失败（含首次共 4 次尝试）
        verify(seckillOrderService, times(4)).createSeckillOrder(GOODS_ID, USER_ID);
        // 补偿只执行一次
        verify(valueOperations, times(1)).increment(STOCK_KEY);
        verify(stringRedisTemplate, times(1)).delete(USER_KEY);
        // 失败不能留下 done 标记
        verify(valueOperations, never()).set(eq(DONE_KEY), anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("消息缺少 goodsId/userId：只记 WARN，不碰 Redis、不建单")
    void shouldIgnoreMessageWithMissingFields() {
        SeckillGoodsMessageDTO empty = new SeckillGoodsMessageDTO();

        assertDoesNotThrow(() -> consumer.seckill(empty));

        verifyNoInteractions(stringRedisTemplate, seckillOrderService);
    }

    private SeckillGoodsMessageDTO message(Long goodsId, Long userId) {
        SeckillGoodsMessageDTO message = new SeckillGoodsMessageDTO();
        message.setGoodsId(goodsId);
        message.setUserId(userId);
        return message;
    }
}
