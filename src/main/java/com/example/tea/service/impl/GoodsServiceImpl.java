package com.example.tea.service.impl;

import com.example.tea.config.CacheKeys;
import com.example.tea.config.CaffeineConfig;
import com.example.tea.entity.dto.Goods.GoodsDTO;
import com.example.tea.entity.dto.Goods.GoodsInsertDTO;
import com.example.tea.entity.dto.Goods.GoodsQueryDTO;
import com.example.tea.entity.dto.Goods.GoodsReviewDTO;
import com.example.tea.entity.pojo.Community.Like;
import com.example.tea.entity.pojo.Goods.Goods;
import com.example.tea.entity.pojo.Goods.GoodsReview;
import com.example.tea.entity.pojo.PageResult;
import com.example.tea.entity.vo.Goods.GoodsReviewVO;
import com.example.tea.entity.vo.Goods.GoodsWithQuantityVO;
import com.example.tea.entity.vo.Goods.MyReviewVO;
import com.example.tea.mapper.CommunityMapper;
import com.example.tea.mapper.GoodsMapper;
import com.example.tea.mapper.OrderMapper;
import com.example.tea.service.GoodsService;
import com.example.tea.utils.AliOssUtil;
import com.example.tea.utils.ThreadLocalUserIdUtil;
import com.github.houbb.sensitive.word.core.SensitiveWordHelper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@Slf4j
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    private AliOssUtil aliOssUtil;
    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private CommunityMapper communityMapper;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @Autowired
    private CacheManager cacheManager;
    @Override
    public PageResult findAllGoods(GoodsQueryDTO goodsQueryDTO) {

        PageHelper.startPage(
                goodsQueryDTO.getPage()==null ? 1 : goodsQueryDTO.getPage(),
                goodsQueryDTO.getPageSize()==null ? 10 : goodsQueryDTO.getPageSize()
        );
        Page<GoodsDTO> page = goodsMapper.findAllGoods(goodsQueryDTO);
        return PageResult.builder()
                .total(page.getTotal())
                .records(page.getResult())
                .build();
    }

    /**
     * 商品详情：一级缓存 Caffeine（{@code @Cacheable}）→ 二级缓存 Redis（手动读写）→ DB。
     * <p>
     * 只由 {@code @Cacheable} 管理 Caffeine，Redis 全部走 {@code RedisTemplate}，
     * 两层使用同一份 key 定义并同时失效（见 {@link #evictGoodsCache(Long)}），
     * 避免出现「Caffeine 已失效但 Redis 还是旧值」。
     */
    @Override
    @Cacheable(value = CaffeineConfig.GOODS_CACHE, key = "#goodsId")
    public GoodsWithQuantityVO findGoodById(Long goodsId) {
        String cacheKey = CacheKeys.GOODS_DETAIL_PREFIX + goodsId;
        GoodsWithQuantityVO cached = getGoodsFromRedis(cacheKey);
        if (cached != null) {
            log.info("二级缓存命中 Redis，goodsId={}", goodsId);
            return cached;
        }
        log.info("二级缓存未命中，回源 DB，goodsId={}", goodsId);
        GoodsWithQuantityVO result = goodsMapper.findGoodDetail(goodsId);
        if (result != null) {
            putGoodsToRedis(cacheKey, result);
        }
        return result;
    }

    /** 读 Redis：Redis 不可用时降级查库，不因缓存故障阻断商品详情。 */
    private GoodsWithQuantityVO getGoodsFromRedis(String cacheKey) {
        try {
            return (GoodsWithQuantityVO) redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
            log.warn("读取商品二级缓存失败，降级查库，key={}，原因：{}", cacheKey, e.getMessage());
            return null;
        }
    }

    /** 写 Redis：必须有 TTL，避免缓存数据永久驻留导致脏读无法自愈。 */
    private void putGoodsToRedis(String cacheKey, GoodsWithQuantityVO value) {
        try {
            redisTemplate.opsForValue().set(cacheKey, value, CacheKeys.GOODS_DETAIL_TTL);
            log.info("已写入商品二级缓存，key={}", cacheKey);
        } catch (Exception e) {
            log.warn("写入商品二级缓存失败，key={}，原因：{}", cacheKey, e.getMessage());
        }
    }

    /**
     * 同时失效一级缓存（Caffeine）与二级缓存（Redis）。
     * <p>
     * 所有商品写操作都必须走这里，保证两层缓存的失效边界一致。
     */
    private void evictGoodsCache(Long goodsId) {
        Cache localCache = cacheManager.getCache(CaffeineConfig.GOODS_CACHE);
        if (localCache != null) {
            localCache.evict(goodsId);
        }
        try {
            redisTemplate.delete(CacheKeys.GOODS_DETAIL_PREFIX + goodsId);
        } catch (Exception e) {
            log.warn("删除商品二级缓存失败，goodsId={}，原因：{}", goodsId, e.getMessage());
        }
    }

    // 新增
    @Override
    public void addGoods(GoodsInsertDTO dto) {
        goodsMapper.insert(dto);
    }

    // 修改
    @Override
    public void updateGoods(Long goodsId,GoodsInsertDTO dto) {
        Goods goods = new Goods();
        BeanUtils.copyProperties(dto, goods);
        goods.setGoodsId(goodsId);
        goodsMapper.updateById(goods);
        // 数据库更新成功后，Caffeine + Redis 一起失效
        evictGoodsCache(goodsId);
    }

    // 物理删除
    @Override
    public void deleteGoods(Long goodsId) {
        goodsMapper.deleteById(goodsId);
        // 数据库删除成功后，Caffeine + Redis 一起失效
        evictGoodsCache(goodsId);
    }

    @Override
    public PageResult findAllGoodsAdmin(GoodsQueryDTO goodsQueryDTO) {
        PageHelper.startPage(
                goodsQueryDTO.getPage()==null ? 1 : goodsQueryDTO.getPage(),
                goodsQueryDTO.getPageSize()==null ? 10 : goodsQueryDTO.getPageSize()
        );
        Page<Goods> page = goodsMapper.findAllGoodsAdmin(goodsQueryDTO);
        return PageResult.builder()
                .total(page.getTotal())
                .records(page.getResult())
                .build();
    }

    @Override
    public Goods findGoodByIdAdmin(Long goodsId) {
        return goodsMapper.findGoodByIdAdmin(goodsId);
    }

    @Override
    public void review(GoodsReviewDTO dto, MultipartFile file) {
        Long userId = ThreadLocalUserIdUtil.getCurrentId();
        Optional.ofNullable(orderMapper.validateGoods(userId, dto.getGoodsId()))
                .orElseThrow(()-> new RuntimeException("没有购买商品无法评价"));
            //校验星(1-5)
            Integer level = dto.getLevel();
            if (level < 1 || level > 5) {
                throw new RuntimeException("星级评分必须在1-5之间");
            }
            //违禁词校验
            if(SensitiveWordHelper.contains(dto.getContent())){
                String string = "包含违禁词" +
                        SensitiveWordHelper.findAll(dto.getContent());
                throw new RuntimeException(string);
            }

            //评价图片上传
        if (file!=null) {
            try {
                String originalFilename = file.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String name = UUID.randomUUID() + extension;
                String filePath = aliOssUtil.upload(file.getBytes(), name);
                goodsMapper.addReview(GoodsReview.builder()
                        .goodsId(dto.getGoodsId())
                        .userId(userId)
                        .content(dto.getContent())
                        .level(dto.getLevel())
                        .likeCount(0)
                        .image(filePath)
                        .build());
                return;
            } catch (Exception e) {
                throw new RuntimeException("图片上传失败");
            }
        }
        goodsMapper.addReview(GoodsReview.builder()
                .goodsId(dto.getGoodsId())
                .userId(userId)
                .content(dto.getContent())
                .level(dto.getLevel())
                .likeCount(0)
                .build());

    }

    @Override
    public List<GoodsReviewVO> getReviewByGoodsId(Long goodsId) {
        return goodsMapper.getReviewByGoodsId(goodsId,ThreadLocalUserIdUtil.getCurrentId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void switchLikeReview(Long reviewId, Integer cancel) {
        //cancel=-1时为取消点赞
        //查看是否是新纪录
        Like like =  communityMapper.check(reviewId,3,ThreadLocalUserIdUtil.getCurrentId());
        if(like == null){
            //新纪录一定是点赞 cancel=1(加记录到like_record)
            communityMapper.addLike(Like.builder()
                    .userId(ThreadLocalUserIdUtil.getCurrentId())
                    .targetId(reviewId)
                    .targetType(3)
                    .createTime(LocalDateTime.now())
                    .isCancel(1).build());
            //操作评价点赞数
            goodsMapper.likeReview(reviewId,cancel);
        }else {
            //老记录则传cancel=1为点赞,-1为取消(加记录到like_record)
            communityMapper.updateLike(reviewId, ThreadLocalUserIdUtil.getCurrentId(),3, cancel);
            //操作评价点赞数
            goodsMapper.likeReview(reviewId,cancel);
        }
    }

    @Override
    public List<MyReviewVO> getMyReview() {
        return goodsMapper.getMyReview(ThreadLocalUserIdUtil.getCurrentId());
    }


}
