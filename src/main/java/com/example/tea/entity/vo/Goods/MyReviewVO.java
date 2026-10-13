package com.example.tea.entity.vo.Goods;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyReviewVO {
    private Long reviewId;
    private String content;
    private Integer level;
    private Integer likeCount;
    private String image;
}
