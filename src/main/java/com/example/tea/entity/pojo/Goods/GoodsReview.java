package com.example.tea.entity.pojo.Goods;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GoodsReview {

    private Long id;            // 评价ID
    private Long goodsId;       // 商品ID
    private Long userId;        // 用户ID
    private String content;     // 评价内容
    private Integer level;      // 星级 1-5
    private Integer likeCount;  // 点赞数
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String image;
}
