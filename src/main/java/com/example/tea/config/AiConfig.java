package com.example.tea.config;

import com.example.tea.FunctionCalling.QueryExpressTool;
import com.example.tea.FunctionCalling.TeaTool;
import com.example.tea.ai.rag.RagProperties;
import com.example.tea.entity.pojo.ChatModel.Prompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 层装配。
 * <ul>
 *   <li><b>短期记忆</b>：{@link MessageWindowChatMemory} + {@link ChatMemoryRepository}(落在 sys_history)；</li>
 *   <li><b>长期记忆</b>：{@link VectorStoreChatMemoryAdvisor} + 独立向量库；</li>
 *   <li><b>RAG</b>：{@code RagSystemPromptAdvisor}（向量+BM25+重排，写入 system prompt）；</li>
 *   <li>主 {@link ChatClient} 串起整条 Advisor 链，另提供一个无 Advisor 的
 *       {@code utilityChatClient} 供「抽关键词」这类工具型调用使用。</li>
 * </ul>
 */
@Configuration
@EnableConfigurationProperties(RagProperties.class)
public class AiConfig {

    /**
     * 短期记忆：窗口式记忆，底层用我们的 MyBatis 仓储。
     */
    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository,
                                 @Value("${tea.ai.memory.max-messages:20}") int maxMessages) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(maxMessages)
                .build();
    }

    /** 短期记忆 Advisor：把历史消息注入本轮提示，并在轮次结束后保存助手回复。 */
    @Bean
    public Advisor chatMemoryAdvisor(ChatMemory chatMemory) {
        return MessageChatMemoryAdvisor.builder(chatMemory).order(0).build();
    }

    /** 长期记忆 Advisor：跨轮次把消息沉淀到向量库，并按会话语义召回。 */
    @Bean
    public Advisor longTermMemoryAdvisor(@Qualifier("chatMemoryVectorStore") VectorStore chatMemoryVectorStore,
                                         @Value("${tea.ai.memory.long-term-top-k:5}") int topK) {
        return VectorStoreChatMemoryAdvisor.builder(chatMemoryVectorStore)
                .defaultTopK(topK)
                .order(20)
                .build();
    }

    /**
     * 主聊天客户端：默认 system prompt + Advisor 链 + 工具。
     * Advisor 顺序：短期记忆(0) -> RAG(10) -> 长期记忆(20)。
     */
    @Bean
    public ChatClient chatClient(OpenAiChatModel openAiChatModel,
                                 @Qualifier("chatMemoryAdvisor") Advisor chatMemoryAdvisor,
                                 @Qualifier("ragAdvisor") Advisor ragAdvisor,
                                 @Qualifier("longTermMemoryAdvisor") Advisor longTermMemoryAdvisor,
                                 TeaTool teaTool,
                                 QueryExpressTool queryExpressTool) {
        return ChatClient.builder(openAiChatModel)
                .defaultSystem(Prompt.promptWord)
                .defaultAdvisors(chatMemoryAdvisor, ragAdvisor, longTermMemoryAdvisor)
                .defaultTools(teaTool, queryExpressTool)
                .build();
    }

    /**
     * 工具型客户端：无记忆、无 RAG、无工具，只做纯粹的单次文本生成。
     */
    @Bean("utilityChatClient")
    public ChatClient utilityChatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel).build();
    }

}