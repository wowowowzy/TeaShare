package com.example.tea.service.impl;

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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
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
    private final String GOODSID = "goodsId:";
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

    @Override
    @Cacheable(value = "goods",key = "#goodsId")
    public GoodsWithQuantityVO findGoodById(Long goodsId) {
        log.info("降级查询Redis");
        GoodsWithQuantityVO vo = (GoodsWithQuantityVO) redisTemplate.opsForValue().get(GOODSID + goodsId);
        GoodsWithQuantityVO goodsVO = Optional.ofNullable(vo).orElseGet(() -> {
            log.info("降级查询DB");
            GoodsWithQuantityVO result = goodsMapper.findGoodDetail(goodsId);
            if (result!=null){
                //新增数据到缓存
                log.info("新增redis缓存");
                redisTemplate.opsForValue().set(GOODSID+goodsId,result);
            }
            return result;
        });
        return goodsVO;
    }

    // 新增
    @Override
    public void addGoods(GoodsInsertDTO dto) {
        goodsMapper.insert(dto);
    }

    // 修改
    @Override
    @CacheEvict(value = "goods", key = "#goodsId")
    public void updateGoods(Long goodsId,GoodsInsertDTO dto) {
        Goods goods = new Goods();
        BeanUtils.copyProperties(dto, goods);
        goods.setGoodsId(goodsId);
        goodsMapper.updateById(goods);
        //删掉Redis数据
        redisTemplate.delete(GOODSID+goodsId);
    }

    // 物理删除
    @Override
    @CacheEvict(value = "goods", key = "#goodsId")
    public void deleteGoods(Long goodsId) {
        goodsMapper.deleteById(goodsId);
        //删掉Redis数据
        redisTemplate.delete(GOODSID+goodsId);
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
