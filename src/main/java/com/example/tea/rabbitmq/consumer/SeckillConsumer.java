package com.example.tea.rabbitmq.consumer;

import com.example.tea.config.RabbitMQseckillConfig;
import com.example.tea.entity.dto.Goods.SeckillGoodsMessageDTO;
import com.example.tea.service.SeckillOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

/**
 * 秒杀消息消费者。
 * <p>
 * 处理流程（顺序不可调整）：
 * <ol>
 *   <li>done 幂等检查：已处理过直接 ack，不做任何补偿；</li>
 *   <li>{@code SET NX} 获取 claim（30 秒，值为随机 UUID）；</li>
 *   <li>拿不到 claim 时轮询等待 done 出现或 claim 释放，最长约 40 秒；</li>
 *   <li>拿到 claim 后<b>再查一次 done</b>，关闭并发窗口；</li>
 *   <li>同步调用独立事务 Bean 落库（不再丢线程池、不再提前 ack）；</li>
 *   <li>事务提交后写 done（2 小时），再释放 claim；</li>
 *   <li>落库失败只补偿一次（Redis 库存 +1、删用户资格），不向 MQ 抛异常。</li>
 * </ol>
 * 配合 {@code acknowledge-mode=auto}，ack 时机与数据库事务提交对齐。
 */
@Component
@Slf4j
@RequiredArgsConstructor
@RabbitListener(queues = RabbitMQseckillConfig.SECKILL_QUEUE)
public class SeckillConsumer {

    private static final String STOCK_PREFIX = "seckill:stock:";
    private static final String USER_PREFIX = "seckill:user:";
    private static final String CLAIM_PREFIX = "seckill:claim:";
    private static final String DONE_PREFIX = "seckill:done:";

    /** 处理中占位锁：必须明显短于 done，崩溃残留最多 30 秒后自动释放。 */
    private static final Duration CLAIM_TTL = Duration.ofSeconds(30);

    /**
     * 成功标记：2 小时。
     * <p>
     * 必须明显小于 {@code seckill:user:} 的 24 小时，否则用户第二天重新获得资格、
     * 秒杀接口又扣了一次 Redis 库存时，消费者会因 done 仍在而跳过建单。
     */
    private static final Duration DONE_TTL = Duration.ofHours(2);
    private static final String DONE_VALUE = "1";

    private static final long CLAIM_WAIT_INTERVAL_MILLIS = 500L;
    private static final long CLAIM_WAIT_TIMEOUT_MILLIS = 40_000L;

    /** compare-and-delete：只删除自己持有的 claim，避免误删其他消费者的锁。 */
    private static final DefaultRedisScript<Long> RELEASE_CLAIM_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final StringRedisTemplate stringRedisTemplate;

    private final SeckillOrderService seckillOrderService;

    /**
     * 监听秒杀队列 → 同步落库。
     */
    @RabbitHandler
    public void seckill(SeckillGoodsMessageDTO message) {
        Long goodsId = message == null ? null : message.getGoodsId();
        Long userId = message == null ? null : message.getUserId();
        if (goodsId == null || userId == null) {
            log.warn("秒杀消息缺少 goodsId/userId，直接确认。message={}", message);
            return;
        }

        String claimKey = CLAIM_PREFIX + goodsId + ":" + userId;
        String doneKey = DONE_PREFIX + goodsId + ":" + userId;

        // 1. 已成功处理过的重复投递：直接确认，不需要补偿（这条消息没有扣过库存）
        if (isDone(doneKey, goodsId, userId)) {
            log.info("重复秒杀消息，订单已生成，直接确认 goodsId={}, userId={}", goodsId, userId);
            return;
        }

        // 2. 获取处理中占位锁
        String claimToken;
        try {
            claimToken = tryClaim(claimKey);
            if (claimToken == null) {
                // 3. 有重复消息正在处理，或上次进程崩溃留下的残留锁
                Boolean done = waitForClaimOrDone(claimKey, doneKey);
                if (done == null) {
                    log.error("等待秒杀处理锁超时，放弃本次消息 goodsId={}, userId={}", goodsId, userId);
                    return;
                }
                if (done) {
                    log.info("秒杀订单已由其他消费者处理完成，直接确认 goodsId={}, userId={}", goodsId, userId);
                    return;
                }
                // claim 已消失（前一次处理失败并释放），再抢一次
                claimToken = tryClaim(claimKey);
                if (claimToken == null) {
                    log.error("秒杀处理锁仍被占用，放弃本次消息 goodsId={}, userId={}", goodsId, userId);
                    return;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("等待秒杀处理锁时线程被中断，放弃本次消息 goodsId={}, userId={}", goodsId, userId);
            return;
        } catch (Exception e) {
            // Redis 不可用时无法做幂等，宁可记 ERROR 也不在未知状态下建单
            log.error("获取秒杀处理锁失败（Redis 不可用），放弃本次消息 goodsId={}, userId={}", goodsId, userId, e);
            return;
        }

        boolean success = false;
        try {
            // 4. 拿到锁后再查一次 done：两个消费者交替时仍可能重复建单，这一步不能省
            if (isDone(doneKey, goodsId, userId)) {
                log.info("持锁后二次检查：订单已生成，直接确认 goodsId={}, userId={}", goodsId, userId);
                return;
            }

            // 5. 独立事务 Bean 落库；全部重试失败才走补偿
            createOrderWithRetry(goodsId, userId);
            success = true;
        } catch (Exception e) {
            log.error("秒杀订单落库失败，释放用户资格并回补 Redis 库存 goodsId={}, userId={}", goodsId, userId, e);
            // 先释放 claim，再补偿；补偿只在这一处执行，绝不因 MQ 重投而重复
            releaseClaim(claimKey, claimToken);
            compensate(goodsId, userId);
            return;
        } finally {
            // 订单已提交：先写 done，再释放 claim。这两步失败只记日志，绝不补偿
            if (success) {
                markDone(doneKey, goodsId, userId);
            }
            releaseClaim(claimKey, claimToken);
        }
    }

    /**
     * 获取处理锁。
     *
     * @return 成功返回本次持有的随机 token；已被占用返回 null
     */
    private String tryClaim(String claimKey) {
        String token = UUID.randomUUID().toString();
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(claimKey, token, CLAIM_TTL);
        return Boolean.TRUE.equals(acquired) ? token : null;
    }

    /**
     * 等待 claim 释放或 done 出现。
     *
     * @return {@code TRUE}=done 已出现；{@code FALSE}=claim 已释放；{@code null}=等待超时
     */
    private Boolean waitForClaimOrDone(String claimKey, String doneKey) throws InterruptedException {
        long deadline = System.currentTimeMillis() + CLAIM_WAIT_TIMEOUT_MILLIS;
        while (System.currentTimeMillis() < deadline) {
            Thread.sleep(CLAIM_WAIT_INTERVAL_MILLIS);
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(doneKey))) {
                return Boolean.TRUE;
            }
            if (!Boolean.TRUE.equals(stringRedisTemplate.hasKey(claimKey))) {
                return Boolean.FALSE;
            }
        }
        return null;
    }

    private boolean isDone(String doneKey, Long goodsId, Long userId) {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(doneKey));
        } catch (Exception e) {
            log.error("查询秒杀完成标记失败，按未完成处理 goodsId={}, userId={}", goodsId, userId, e);
            return false;
        }
    }

    /**
     * 写成功标记。必须在事务 Bean 返回（提交）之后调用。
     */
    private void markDone(String doneKey, Long goodsId, Long userId) {
        try {
            stringRedisTemplate.opsForValue().set(doneKey, DONE_VALUE, DONE_TTL);
        } catch (Exception e) {
            log.error("写入秒杀完成标记失败（订单已提交，不回滚、不补偿）goodsId={}, userId={}",
                    goodsId, userId, e);
        }
    }

    /**
     * 释放处理锁（CAS 删除，只删自己持有的锁）。
     */
    private void releaseClaim(String claimKey, String claimToken) {
        if (claimToken == null) {
            return;
        }
        try {
            stringRedisTemplate.execute(RELEASE_CLAIM_SCRIPT, Collections.singletonList(claimKey), claimToken);
        } catch (Exception e) {
            log.warn("释放秒杀处理锁失败，等待 TTL 自动过期 claimKey={}，原因：{}", claimKey, e.getMessage());
        }
    }

    /**
     * 补偿：Redis 库存 +1，并删除用户资格键，让用户可以重新抢。
     * 仅在最终失败路径调用一次。
     */
    private void compensate(Long goodsId, Long userId) {
        try {
            stringRedisTemplate.opsForValue().increment(STOCK_PREFIX + goodsId);
        } catch (Exception e) {
            log.error("秒杀补偿失败：回补 Redis 库存异常 goodsId={}, userId={}", goodsId, userId, e);
        }
        try {
            stringRedisTemplate.delete(USER_PREFIX + goodsId + ":" + userId);
        } catch (Exception e) {
            log.error("秒杀补偿失败：删除用户资格异常 goodsId={}, userId={}", goodsId, userId, e);
        }
    }

    /**
     * 本地短退避重试：100ms / 300ms / 900ms 共 3 次重试（含首次共 4 次尝试）。
     * 事务方法每次失败都已回滚，重试是安全的；全部失败才抛给外层补偿。
     */
    private void createOrderWithRetry(Long goodsId, Long userId) throws Exception {
        long[] retryDelayMillis = {100L, 300L, 900L};
        Exception lastError = null;
        for (int attempt = 0; ; attempt++) {
            try {
                seckillOrderService.createSeckillOrder(goodsId, userId);
                return;
            } catch (Exception e) {
                lastError = e;
                if (attempt >= retryDelayMillis.length) {
                    break;
                }
                log.warn("秒杀建单第 {} 次失败，{}ms 后重试 goodsId={}, userId={}，原因：{}",
                        attempt + 1, retryDelayMillis[attempt], goodsId, userId, e.getMessage());
                try {
                    Thread.sleep(retryDelayMillis[attempt]);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw interrupted;
                }
            }
        }
        throw lastError;
    }
}
