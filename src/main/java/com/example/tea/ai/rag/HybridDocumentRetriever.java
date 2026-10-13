package com.example.tea.ai.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 混合检索：同时执行「向量召回」与「BM25 关键词召回」，再用 RRF(Reciprocal Rank Fusion) 融合。
 * <p>
 * RRF 对两种完全不同量纲的得分（余弦相似度 vs 全文相关性）天然免调参：
 * <pre>score(d) = Σ 1 / (k + rank_i(d))</pre>
 */
public class HybridDocumentRetriever implements DocumentRetriever {

    private final DocumentRetriever vectorRetriever;

    private final DocumentRetriever bm25Retriever;

    private final int topK;

    private final int rrfK;

    public HybridDocumentRetriever(DocumentRetriever vectorRetriever, DocumentRetriever bm25Retriever, int topK, int rrfK) {
        this.vectorRetriever = vectorRetriever;
        this.bm25Retriever = bm25Retriever;
        this.topK = topK;
        this.rrfK = rrfK;
    }

    @Override
    public List<Document> retrieve(Query query) {
        Map<String, Double> fusedScores = new HashMap<>();
        Map<String, Document> documentById = new HashMap<>();

        accumulate(safeRetrieve(vectorRetriever, query), fusedScores, documentById);
        accumulate(safeRetrieve(bm25Retriever, query), fusedScores, documentById);

        return fusedScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(topK)
                .map(entry -> documentById.get(entry.getKey()))
                .filter(Objects::nonNull)
                .toList();
    }

    private void accumulate(List<Document> documents, Map<String, Double> fusedScores, Map<String, Document> documentById) {
        for (int rank = 0; rank < documents.size(); rank++) {
            Document document = documents.get(rank);
            String key = document.getId();
            if (key == null) {
                key = DigestUtils.md5DigestAsHex((document.getText() == null ? "" : document.getText()).getBytes(StandardCharsets.UTF_8));
            }
            documentById.putIfAbsent(key, document);
            fusedScores.merge(key, 1.0 / (rrfK + rank + 1), Double::sum);
        }
    }

    private List<Document> safeRetrieve(DocumentRetriever retriever, Query query) {
        try {
            List<Document> result = retriever.retrieve(query);
            return result == null ? List.of() : result;
        } catch (Exception e) {
            return List.of();
        }
    }

}