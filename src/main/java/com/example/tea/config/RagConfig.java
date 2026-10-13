package com.example.tea.config;

import com.example.tea.ai.rag.HybridDocumentRetriever;
import com.example.tea.ai.rag.MySqlBm25DocumentRetriever;
import com.example.tea.ai.rag.RagProperties;
import com.example.tea.ai.rag.RagSystemPromptAdvisor;
import com.example.tea.ai.rag.RerankDocumentPostProcessor;
import com.example.tea.ai.rerank.RerankClient;
import com.example.tea.mapper.KnowledgeMapper;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RAG 链路装配：向量召回 + BM25 关键词召回 -> RRF 融合 -> 重排 -> system prompt。
 */
@Configuration
public class RagConfig {

    /** BM25 侧召回（MySQL ngram 全文）。 */
    @Bean
    public DocumentRetriever bm25DocumentRetriever(KnowledgeMapper knowledgeMapper, RagProperties ragProperties) {
        return new MySqlBm25DocumentRetriever(knowledgeMapper, ragProperties.getBm25TopK());
    }

    /** 混合召回：向量 + BM25，用 RRF 融合。 */
    @Bean
    public DocumentRetriever hybridDocumentRetriever(@Qualifier("ragVectorStore") VectorStore ragVectorStore,
                                                     @Qualifier("bm25DocumentRetriever") DocumentRetriever bm25DocumentRetriever,
                                                     RagProperties ragProperties) {
        DocumentRetriever vectorRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(ragVectorStore)
                .topK(ragProperties.getVectorTopK())
                .similarityThreshold(ragProperties.getSimilarityThreshold())
                .build();
        return new HybridDocumentRetriever(vectorRetriever, bm25DocumentRetriever,
                ragProperties.getHybridTopK(), ragProperties.getRrfK());
    }

    /** 重排后置处理器。 */
    @Bean
    public DocumentPostProcessor rerankDocumentPostProcessor(RerankClient rerankClient, RagProperties ragProperties) {
        return new RerankDocumentPostProcessor(rerankClient,
                ragProperties.getRerankTopN(), ragProperties.isRerankEnabled());
    }

    /** RAG Advisor：把召回+重排后的资料写进 system prompt。 */
    @Bean
    public Advisor ragAdvisor(@Qualifier("hybridDocumentRetriever") DocumentRetriever hybridDocumentRetriever,
                              @Qualifier("rerankDocumentPostProcessor") DocumentPostProcessor rerankDocumentPostProcessor) {
        return RagSystemPromptAdvisor.builder()
                .documentRetriever(hybridDocumentRetriever)
                .documentPostProcessors(rerankDocumentPostProcessor)
                .order(10)
                .build();
    }

}