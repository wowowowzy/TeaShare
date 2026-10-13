package com.example.tea.entity.dto.Goods;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GoodsShowDTO {

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

}
