package com.example.tea.ai.rag;

import com.example.tea.ai.rerank.RerankClient;
import com.example.tea.ai.rerank.RerankResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;

import java.util.Comparator;
import java.util.List;

/**
 * 重排后置处理器：对混合检索的候选文档调用重排模型精排，并截断到 topN。
 * 重排失败时自动降级为原始召回顺序，保证主链路可用。
 */
@Slf4j
public class RerankDocumentPostProcessor implements DocumentPostProcessor {

    private final RerankClient rerankClient;

    private final int topN;

    private final boolean enabled;

    public RerankDocumentPostProcessor(RerankClient rerankClient, int topN, boolean enabled) {
        this.rerankClient = rerankClient;
        this.topN = topN;
        this.enabled = enabled;
    }

    @Override
    public List<Document> process(Query query, List<Document> documents) {
        if (!enabled || documents == null || documents.size() <= 1) {
            return documents;
        }
        try {
            List<String> texts = documents.stream()
                    .map(document -> document.getText() == null ? "" : document.getText())
                    .toList();
            List<RerankResult> results = rerankClient.rerank(query.text(), texts, topN);
            if (results == null || results.isEmpty()) {
                log.warn("重排返回空结果，降级为原始召回顺序");
                return documents.stream().limit(topN).toList();
            }
            log.info("重排完成: 候选={} 采纳={} 最高分={}", documents.size(), results.size(), results.get(0).score());
            return results.stream()
                    .filter(result -> result.index() >= 0 && result.index() < documents.size())
                    .sorted(Comparator.comparingDouble(RerankResult::score).reversed())
                    .limit(topN)
                    .map(result -> documents.get(result.index()).mutate().score(result.score()).build())
                    .toList();
        } catch (Exception e) {
            log.warn("重排失败，降级为原始召回顺序: {}", e.getMessage());
            return documents;
        }
    }

}