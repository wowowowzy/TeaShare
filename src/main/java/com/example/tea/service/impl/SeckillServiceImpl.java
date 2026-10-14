package com.example.tea.service.impl;

import com.example.tea.config.RabbitMQseckillConfig;
import com.example.tea.entity.dto.Goods.GoodsStockDTO;
import com.example.tea.entity.dto.Goods.SeckillGoodsMessageDTO;
import com.example.tea.entity.vo.Goods.GoodsVO;
import com.example.tea.mapper.GoodsMapper;
import com.example.tea.service.SeckillService;
import com.example.tea.utils.ThreadLocalUserIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    private GoodsMapper goodsMapper;

    private final StringRedisTemplate redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    // Redis key 前缀
    private static final String STOCK_PREFIX = "seckill:stock:";
    private static final String USER_PREFIX = "seckill:user:";

    /**
     * 秒杀核心方法
     */
    @Override
    public String seckill(Long goodsId) {
        if (goodsMapper.getGoodStock(goodsId)<=0){
            return "已抢完";
        }
        Long userId = ThreadLocalUserIdUtil.getCurrentId();
        // 1. 拼接key
        String stockKey = STOCK_PREFIX + goodsId;
        String userKey = USER_PREFIX + goodsId + ":" + userId;

        // 2. 判断用户是否已经抢过（Redis判断）
        if (Boolean.TRUE.equals(redisTemplate.hasKey(userKey))) {
            return "您已经抢购过此商品";
        }

        // 2.1 Redis 库存不存在（未初始化）或已为 0：直接拒绝，不进入扣减
        String currentStock = redisTemplate.opsForValue().get(stockKey);
        if (currentStock == null) {
            return "秒杀尚未开始";
        }
        if (Long.parseLong(currentStock) <= 0) {
            return "已抢完";
        }

        // 3. 原子扣库存（最核心！防超卖）
        Long remainStock = redisTemplate.opsForValue().decrement(stockKey);

        // 4. Redis库存不足
        if (remainStock < 0) {
            // 把多减的 1 加回去
            redisTemplate.opsForValue().increment(stockKey);
            return "已抢完";
        }

        // 5. 标记用户已抢（24小时过期）
        redisTemplate.opsForValue().set(userKey, "1", 24, TimeUnit.HOURS);

        // 6. 发送消息到 MQ → 异步下单
        SeckillGoodsMessageDTO message = new SeckillGoodsMessageDTO();
        message.setGoodsId(goodsId);
        message.setUserId(userId);

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQseckillConfig.SECKILL_EXCHANGE,
                    RabbitMQseckillConfig.SECKILL_ROUTING_KEY,
                    message
            );
        } catch (Exception e) {
            // MQ发送失败 → 库存回滚
            redisTemplate.opsForValue().increment(stockKey);
            redisTemplate.delete(userKey);
            log.error("MQ发送失败，库存回滚");
            return "系统繁忙，请重试";
        }
        return "抢购成功，已排队生成订单";
    }

    /**
     * 初始化库存到 Redis（秒杀前调用）
     */
    @Override
    public String initStock(Long goodsId, int stock, BigDecimal goodsPrice) {
            String stockKey = STOCK_PREFIX + goodsId;
            redisTemplate.opsForValue().set(stockKey, String.valueOf(stock));
            log.info("商品{} 初始化库存：{}", goodsId, stock);
        GoodsVO goodsVO = goodsMapper.findGoodById(goodsId);
        goodsVO.setStockNum(stock);
        goodsVO.setGoodsPrice(goodsPrice);
        //设置or修改秒杀商品库存
        if (goodsMapper.vaildateSeckill(goodsId)==0) {
            goodsMapper.initStock(goodsVO);
        } else {
            goodsMapper.updateSeckillStocks(goodsId,stock,goodsPrice);
        }
        return "初始化成功";
    }

    @Override
    public List<GoodsVO> getAllSeckillGoods() {
        List<GoodsVO> seckillGoods = goodsMapper.getAllSeckillGoods();
        if (seckillGoods == null || seckillGoods.isEmpty()) {
            return seckillGoods == null ? Collections.emptyList() : seckillGoods;
        }
        List<Long> goodsIds = seckillGoods.stream().map(GoodsVO::getGoodsId).toList();
        Map<Long, Integer> collect = goodsMapper.getGoodsByList(goodsIds).stream().collect(Collectors.toMap(GoodsStockDTO::getGoodsId, GoodsStockDTO::getStockNum));
        seckillGoods.forEach(dto->{
            Integer goodsStock = collect.get(dto.getGoodsId());
            //真实库存不足时
            if (goodsStock != null && goodsStock <= dto.getStockNum()){
                String stockKey = STOCK_PREFIX + dto.getGoodsId();
                //修改redis库存防止超卖：只降不升——Redis 中可能已有更低的在途预留值（用户已扣、MQ 排队中），
                //直接覆盖会把库存抬高重新放出，导致超卖。取两者较小值写回并展示。
                int newStock = goodsStock;
                String current = redisTemplate.opsForValue().get(stockKey);
                if (current != null) {
                    try {
                        newStock = Math.min(Integer.parseInt(current), goodsStock);
                    } catch (NumberFormatException ignored) {
                        //Redis 值异常时按 DB 库存写回
                    }
                }
                dto.setStockNum(newStock);
                redisTemplate.opsForValue().set(stockKey, String.valueOf(newStock));
            }
        });
        return seckillGoods;
    }
}
