package com.example.tea.service;

import com.example.tea.entity.dto.Goods.GoodsInsertDTO;
import com.example.tea.entity.dto.Goods.GoodsQueryDTO;
import com.example.tea.entity.dto.Goods.GoodsReviewDTO;
import com.example.tea.entity.pojo.Goods.Goods;
import com.example.tea.entity.pojo.PageResult;
import com.example.tea.entity.vo.Goods.GoodsReviewVO;
import com.example.tea.entity.vo.Goods.GoodsWithQuantityVO;
import com.example.tea.entity.vo.Goods.MyReviewVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface GoodsService {
    PageResult findAllGoods(GoodsQueryDTO goodsQueryDTO);

    GoodsWithQuantityVO findGoodById(Long goodsId);


    // 新增商品
    void addGoods(GoodsInsertDTO dto);

    // 修改商品
    void updateGoods(Long goodsId,GoodsInsertDTO dto);

    // 删除商品
    void deleteGoods(Long goodsId);

    PageResult findAllGoodsAdmin(GoodsQueryDTO goodsQueryDTO);

    Goods findGoodByIdAdmin(Long goodsId);

    void review(GoodsReviewDTO dto, MultipartFile file);

    List<GoodsReviewVO> getReviewByGoodsId(Long goodsId);

    void switchLikeReview(Long reviewId,Integer cancel);

   List<MyReviewVO> getMyReview();
}
