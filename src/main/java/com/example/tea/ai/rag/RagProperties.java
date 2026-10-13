package com.example.tea.ai.rag;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RAG 检索相关参数（向量召回 / BM25 关键词召回 / 混合融合 / 重排）。
 * 全部可通过 application.yml 或环境变量覆盖，避免把参数写死在代码里。
 */
@Data
@ConfigurationProperties(prefix = "tea.ai.rag")
public class RagProperties {

    /** 向量召回条数（候选集大小）。 */
    private int vectorTopK = 5;

    /** 向量召回相似度阈值，低于该值的结果会被丢弃。 */
    private double similarityThreshold = 0.5;

    /** BM25(MySQL 全文) 关键词召回条数。 */
    private int bm25TopK = 5;

    /** 混合融合(RRF)后保留的候选条数。 */
    private int hybridTopK = 5;

    /** RRF 平滑参数 k，越大越弱化高位排名差异。 */
    private int rrfK = 60;

    /** 是否开启重排。 */
    private boolean rerankEnabled = true;

    /** 重排后最终送入上下文的文档条数。 */
    private int rerankTopN = 3;

}