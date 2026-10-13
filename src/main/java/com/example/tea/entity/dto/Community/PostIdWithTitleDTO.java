package com.example.tea.entity.dto.Community;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostIdWithTitleDTO {
    private Long postId;
    private String title;
}
