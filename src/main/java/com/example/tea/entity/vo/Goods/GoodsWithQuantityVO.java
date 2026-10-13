package com.example.tea.entity.vo.Goods;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GoodsWithQuantityVO {
    private Long goodsId;

    /**
     * 商品名字
     */
    private String goodsName;

    /**
     * 商品介绍
     */
    private String goodsIntro;

    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 商品分类ID
     */
    private Long categoryId;

    /**
     * 库存数量
     */
    private Integer stockNum;

    /*
     * 商品头像
     */
    private String goodsImage;
    private int quantity;
}
