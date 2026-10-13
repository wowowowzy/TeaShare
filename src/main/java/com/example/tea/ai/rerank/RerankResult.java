package com.example.tea.ai.rerank;

/**
 * 重排结果：在原始文档列表中的下标 + 相关性得分。
 *
 * @param index 原始文档下标
 * @param score 相关性得分（越大越相关）
 */
public record RerankResult(int index, double score) {
}