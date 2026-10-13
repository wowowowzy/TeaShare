package com.example.tea.ai.rag;

import com.example.tea.ai.rag.dto.KnowledgeHit;
import com.example.tea.mapper.KnowledgeMapper;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于 MySQL 全文索引(ngram)的关键词召回，作为向量召回的补充（混合检索中的 BM25 侧）。
 */
public class MySqlBm25DocumentRetriever implements DocumentRetriever {

    private final KnowledgeMapper knowledgeMapper;

    private final int topK;

    public MySqlBm25DocumentRetriever(KnowledgeMapper knowledgeMapper, int topK) {
        this.knowledgeMapper = knowledgeMapper;
        this.topK = topK;
    }

    @Override
    public List<Document> retrieve(Query query) {
        if (query == null || query.text() == null || query.text().isBlank()) {
            return List.of();
        }
        List<KnowledgeHit> hits = knowledgeMapper.search(query.text(), topK);
        List<Document> documents = new ArrayList<>(hits.size());
        for (KnowledgeHit hit : hits) {
            documents.add(Document.builder()
                    .id(hit.getId())
                    .text(hit.getContent())
                    .metadata("bm25Score", hit.getScore() == null ? 0d : hit.getScore())
                    .build());
        }
        return documents;
    }

}