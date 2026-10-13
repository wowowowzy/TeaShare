package com.example.tea.rabbitmq.consumer;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.example.tea.config.RabbitMQseckillConfig;
import com.example.tea.entity.dto.Goods.SeckillGoodsMessageDTO;
import com.example.tea.entity.pojo.Order.Order;
import com.example.tea.entity.pojo.Order.OrderDetail;
import com.example.tea.mapper.GoodsMapper;
import com.example.tea.mapper.OrderMapper;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀消息消费者
 * 从MQ拿消息 → 真正创建订单
 */
@Component
@Slf4j
@RequiredArgsConstructor
@RabbitListener(queues = RabbitMQseckillConfig.SECKILL_QUEUE)
public class SeckillConsumer {
    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Resource
    private ThreadPoolExecutor seckillExecutor;
    @Autowired
    @Lazy
    private SeckillConsumer consumer;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    private final Snowflake snowflake = IdUtil.getSnowflake();
    private static final String STOCK_PREFIX = "seckill:stock:";
    private static final String USER_PREFIX = "seckill:user:";



    /**
     * 监听秒杀队列 → 异步消费
     */
    @RabbitHandler
    public void seckill(SeckillGoodsMessageDTO message){
        try {
            seckillExecutor.execute(()->consumer.handleSeckillMessage(message));
        } catch (Exception e) {
            log.error("秒杀线程池异步执行错误");
        }
    }

    /**
     * 处理秒杀消息
     */
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRES_NEW)
    public void handleSeckillMessage(SeckillGoodsMessageDTO message){
        try {
            Long goodsId = message.getGoodsId();
            Long userId = message.getUserId();
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
            List<Order> orderList = new ArrayList<>();
            orderList.add(order);
            orderMapper.pay(orderList);
            BigDecimal goodsPrice = goodsMapper.findSeckillGoodById(goodsId);
            OrderDetail orderDetail = OrderDetail.builder()
                    .orderId(String.valueOf(orderId))
                    .userId(userId)
                    .totalPrice(goodsPrice)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            goodsMapper.subSeckillStock(goodsId);
            goodsMapper.subGoodsStock(goodsId);
            orderMapper.insertDetail(orderDetail);
        } catch (Exception e) {
            consumer.remedy(message);
            throw new RuntimeException(e);
        }
    }

    /**
     * 秒杀失败补救措施
     */
    private void remedy(SeckillGoodsMessageDTO message){
        String stockKey = STOCK_PREFIX + message.getGoodsId();
        redisTemplate.opsForValue().increment(stockKey);
        String userKey = USER_PREFIX + message.getGoodsId() + ":" + message.getUserId();
        redisTemplate.delete(userKey);
    }
}
