package com.example.tea.ai.rag.dto;

import lombok.Data;

/**
 * MySQL 全文检索命中的一条知识切片。
 */
@Data
public class KnowledgeHit {

    private String id;

    private String content;

    /** MySQL 全文相关性得分（InnoDB 全文基于 BM25 变体）。 */
    private Double score;

}