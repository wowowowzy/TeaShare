package com.example.tea.entity.vo.Goods;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GoodsReviewVO {
    private Long reviewId;
    private Long userId;        // 用户ID
    private String username;
    private String content;     // 评价内容
    private Integer level;      // 星级 1-5
    private Integer likeCount;  // 点赞数
    private String image;
    private String userImage;
    private Integer isCancel;
}
