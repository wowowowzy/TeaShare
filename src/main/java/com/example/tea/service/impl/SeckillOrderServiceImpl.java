package com.example.tea.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.example.tea.entity.pojo.Order.Order;
import com.example.tea.entity.pojo.Order.OrderDetail;
import com.example.tea.mapper.GoodsMapper;
import com.example.tea.mapper.OrderMapper;
import com.example.tea.service.SeckillOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 秒杀订单落库实现。
 * <p>
 * 事务边界只在这里：方法正常返回 = 已提交，抛异常 = 已回滚。
 * 消费者必须在本方法<b>返回之后</b>再写 done 标记，否则回滚后会留下“假完成”。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SeckillOrderServiceImpl implements SeckillOrderService {

    private final GoodsMapper goodsMapper;

    private final OrderMapper orderMapper;

    /** 全局唯一雪花生成器（统一 Bean，禁止 new Snowflake()）。 */
    private final Snowflake snowflake;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSeckillOrder(Long goodsId, Long userId) {
        Long orderId = snowflake.nextId();
        Order order = Order.builder()
                .userId(userId)
                .goodsId(Math.toIntExact(goodsId))
                .orderId(orderId)
                .quantity(1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .status(0)
                .build();
        orderMapper.pay(List.of(order));

        BigDecimal goodsPrice = goodsMapper.findSeckillGoodById(goodsId);
        if (goodsPrice == null) {
            throw new IllegalStateException("秒杀商品不存在或价格缺失，goodsId=" + goodsId);
        }

        // 秒杀库存与普通商品库存都必须扣减成功；影响 0 行说明库存已不足，直接回滚
        int seckillRows = goodsMapper.subSeckillStock(goodsId);
        if (seckillRows == 0) {
            throw new IllegalStateException("秒杀库存不足，goodsId=" + goodsId);
        }
        int goodsRows = goodsMapper.subGoodsStock(goodsId);
        if (goodsRows == 0) {
            throw new IllegalStateException("商品库存不足，goodsId=" + goodsId);
        }

        OrderDetail orderDetail = OrderDetail.builder()
                .orderId(String.valueOf(orderId))
                .userId(userId)
                .totalPrice(goodsPrice)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        orderMapper.insertDetail(orderDetail);

        log.info("秒杀订单创建成功 orderId={}, goodsId={}, userId={}", orderId, goodsId, userId);
    }
}
