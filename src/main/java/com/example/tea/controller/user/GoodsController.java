package com.example.tea.controller.user;

import com.example.tea.entity.dto.Goods.GoodsQueryDTO;
import com.example.tea.entity.dto.Goods.GoodsReviewDTO;
import com.example.tea.entity.pojo.Result;
import com.example.tea.service.GoodsService;
import com.example.tea.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
public class GoodsController {
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private SeckillService seckillService;

    /**
     * 商品全查找(包含分类查找)
     *
     * @param goodsQueryDTO
     * @return
     */
    @GetMapping("/findAllGoods")
    public Result findAllGoods(GoodsQueryDTO goodsQueryDTO) {
        return Result.success(goodsService.findAllGoods(goodsQueryDTO));
    }

    /**
     * 查找商品详情
     *
     * @param goodsId
     * @return
     */
    @GetMapping("/findGoodById")
    public Result findGoodById(Long goodsId) {
        return Result.success(goodsService.findGoodById(goodsId));
    }

    /**
     * 获取所有秒杀商品
     *
     * @return
     */
    @GetMapping("getAllSeckillGoods")
    public Result getAllSeckillGoods() {
        return Result.success(seckillService.getAllSeckillGoods());
    }

    /**
     * 评价商品
     * @return
     */
    @PostMapping("review")
    public Result review(GoodsReviewDTO dto, @RequestParam(required = false) MultipartFile file){
        if (dto.getLevel()==null||dto.getGoodsId()==null||dto.getContent().isBlank()){
            return Result.error("必须参数未填写");
        }
        try {
            goodsService.review(dto,file);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
        return Result.success("评价成功");
    }

    /**
     * 查看评论列表
     * @param goodsId
     * @return
     */
    @GetMapping("getReviewByGoodsId")
    public Result getReviewByGoodsId(Long goodsId){
        return Result.success(goodsService.getReviewByGoodsId(goodsId));
    }

    /**
     * 点赞/取消评价
     * @param reviewId
     * @param cancel
     * @return
     */
    @PostMapping("switchLikeReview")
    public Result switchLikeReview(Long reviewId,Integer cancel){
        goodsService.switchLikeReview(reviewId,cancel);
        return Result.success();
    }

    /**
     * 获取当前用户所有评价
     * @return
     */
    @GetMapping("getMyReview")
    public Result getMyReview(){
        return Result.success(goodsService.getMyReview());
    }
}
