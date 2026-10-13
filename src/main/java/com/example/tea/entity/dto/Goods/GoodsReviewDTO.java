package com.example.tea.entity.dto.Goods;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GoodsReviewDTO {
    private Long goodsId;
    private String content;
    private Integer level;
}
