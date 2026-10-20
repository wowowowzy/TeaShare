package com.example.tea.service;

/**
 * 秒杀订单落库服务。
 * <p>
 * 独立成 Bean 的原因：事务注解只有在跨 Bean 调用（经过 Spring 代理）时才生效。
 * 消费者拿到 MQ 消息后同步调用本接口，方法正常返回即代表事务已提交。
 */
public interface SeckillOrderService {

    /**
     * 创建秒杀订单（写订单、写订单详情、扣秒杀库存、扣商品库存）。
     * <p>
     * 任一步失败都会抛异常并回滚整个事务，由调用方决定补偿。
     *
     * @param goodsId 商品ID
     * @param userId  用户ID
     */
    void createSeckillOrder(Long goodsId, Long userId);
}
