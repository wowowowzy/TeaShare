package com.example.tea.mapper;

import com.example.tea.ai.rag.dto.KnowledgeHit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * RAG 知识切片（MySQL 全文检索侧）。
 */
@Mapper
public interface KnowledgeMapper {

    /** 写入/更新知识切片（内容哈希作为主键，保证重复初始化幂等）。 */
    void upsert(@Param("id") String id, @Param("content") String content);

    /** ngram 全文检索，返回按相关性倒序的切片。 */
    List<KnowledgeHit> search(@Param("query") String query, @Param("limit") int limit);

}