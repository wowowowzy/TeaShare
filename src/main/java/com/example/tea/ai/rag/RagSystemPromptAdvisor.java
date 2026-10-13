package com.example.tea.ai.rag;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.util.Assert;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RAG 召回 Advisor：
 * <ol>
 *   <li>用混合检索(向量 + BM25 + RRF)召回候选文档；</li>
 *   <li>用重排模型对候选做精排；</li>
 *   <li>把最终资料写入 <b>SystemMessage</b>（而不是塞进 UserMessage）。</li>
 * </ol>
 * 复用 Spring AI 的 {@link DocumentRetriever} / {@link DocumentPostProcessor} 扩展点，
 * 因此检索与重排都可独立替换、独立测试。
 */
@Slf4j
public class RagSystemPromptAdvisor implements BaseAdvisor {

    /** 上下文 key：本次召回并重排后的文档，便于观测/取用。 */
    public static final String RAG_DOCUMENTS = "rag_documents";

    private static final PromptTemplate DEFAULT_SYSTEM_PROMPT_TEMPLATE = new PromptTemplate("""
            {instructions}

            【知识库检索资料】
            请优先依据下面的资料回答用户问题；若资料未覆盖，可结合你的茶叶知识补充，但不要编造。
            {context}
            """);

    private final DocumentRetriever documentRetriever;

    private final List<DocumentPostProcessor> documentPostProcessors;

    private final PromptTemplate systemPromptTemplate;

    private final int order;

    private RagSystemPromptAdvisor(DocumentRetriever documentRetriever,
                                   List<DocumentPostProcessor> documentPostProcessors,
                                   PromptTemplate systemPromptTemplate,
                                   int order) {
        Assert.notNull(documentRetriever, "documentRetriever cannot be null");
        this.documentRetriever = documentRetriever;
        this.documentPostProcessors = documentPostProcessors == null ? List.of() : documentPostProcessors;
        this.systemPromptTemplate = systemPromptTemplate == null ? DEFAULT_SYSTEM_PROMPT_TEMPLATE : systemPromptTemplate;
        this.order = order;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain advisorChain) {
        var prompt = request.prompt();
        // 此时短期记忆 Advisor 已把历史消息注入进来，这里必须取「最后一条」用户消息，
        // 否则用户消息在列表中不唯一时会拿错检索文本。
        List<UserMessage> userMessages = prompt.getUserMessages();
        if (userMessages.isEmpty()) {
            return request;
        }
        UserMessage userMessage = userMessages.get(userMessages.size() - 1);
        if (userMessage.getText() == null || userMessage.getText().isBlank()) {
            return request;
        }

        Query query = Query.builder()
                .text(userMessage.getText())
                .history(prompt.getInstructions())
                .context(request.context())
                .build();

        List<Document> documents = documentRetriever.retrieve(query);
        int candidateSize = documents == null ? 0 : documents.size();
        for (DocumentPostProcessor postProcessor : this.documentPostProcessors) {
            documents = postProcessor.process(query, documents);
        }
        if (documents == null) {
            documents = List.of();
        }
        log.info("RAG 召回完成: query=\"{}\" 候选={} 最终={}", query.text(), candidateSize, documents.size());

        String context = documents.isEmpty()
                ? "（知识库中没有检索到相关资料，请基于你自身的茶叶知识回答，不要编造。）"
                : documents.stream().map(Document::getText).collect(Collectors.joining("\n\n"));

        SystemMessage systemMessage = prompt.getSystemMessage();
        String instructions = systemMessage == null ? "" : systemMessage.getText();
        String augmentedSystem = this.systemPromptTemplate.render(Map.of(
                "instructions", instructions,
                "context", context));

        Map<String, Object> contextMap = new HashMap<>(request.context());
        contextMap.put(RAG_DOCUMENTS, documents);

        return request.mutate()
                .prompt(prompt.augmentSystemMessage(augmentedSystem))
                .context(contextMap)
                .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain advisorChain) {
        return response;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private DocumentRetriever documentRetriever;

        private List<DocumentPostProcessor> documentPostProcessors;

        private PromptTemplate systemPromptTemplate;

        private int order = 10;

        public Builder documentRetriever(DocumentRetriever documentRetriever) {
            this.documentRetriever = documentRetriever;
            return this;
        }

        public Builder documentPostProcessors(List<DocumentPostProcessor> documentPostProcessors) {
            this.documentPostProcessors = documentPostProcessors;
            return this;
        }

        public Builder documentPostProcessors(DocumentPostProcessor... documentPostProcessors) {
            this.documentPostProcessors = List.of(documentPostProcessors);
            return this;
        }

        public Builder systemPromptTemplate(PromptTemplate systemPromptTemplate) {
            this.systemPromptTemplate = systemPromptTemplate;
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public RagSystemPromptAdvisor build() {
            return new RagSystemPromptAdvisor(this.documentRetriever, this.documentPostProcessors,
                    this.systemPromptTemplate, this.order);
        }

    }

}