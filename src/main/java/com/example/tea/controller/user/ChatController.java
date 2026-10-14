package com.example.tea.controller.user;

import cn.hutool.core.lang.Snowflake;
import com.example.tea.entity.pojo.ChatModel.History;
import com.example.tea.entity.pojo.ChatModel.Prompt;
import com.example.tea.entity.pojo.Result;
import com.example.tea.mapper.ChatMapper;
import com.example.tea.utils.ThreadLocalUserIdUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 对话接口。
 * <p>
 * 改造要点：
 * <ul>
 *   <li>历史不再手工拼装：由 {@code MessageChatMemoryAdvisor} + ChatMemory 负责；</li>
 *   <li>人设/提示词走 system prompt（默认 system），RAG 资料也由 Advisor 写入 system prompt；</li>
 *   <li>RAG 检索(向量+BM25+重排)与长期记忆均由 Advisor 链完成。</li>
 * </ul>
 */
@RestController
@RequestMapping(value = "/api/user/ai")
public class ChatController {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ChatMapper chatMapper;

    /** 全局唯一雪花生成器（禁止 new Snowflake()，否则与其它实例撞号）。 */
    @Autowired
    private Snowflake snowflake;

    /**
     * 核心ai接口(Stream)
     */
    @PostMapping(value = "/generateStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateStream
            (@RequestParam(value = "message", required = false) String message,
             @RequestParam(value = "sessionId") Long sessionId,
             @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {

        Long userId = ThreadLocalUserIdUtil.getCurrentId();
        String conversationId = String.valueOf(sessionId);

        String text = message;
        List<Media> media = new ArrayList<>();
        // 多模态消息
        if (image != null && !image.isEmpty()) {
            MimeType mimeType = MimeTypeUtils.parseMimeType(image.getContentType());
            media.add(Media.builder()
                    .mimeType(mimeType)
                    .data(image.getBytes())
                    .build());
            text = (text == null || text.isBlank())
                    ? Prompt.imagePromptWord
                    : Prompt.imagePromptWord + "\n" + text;
        }

        Map<String, Object> metadata = new HashMap<>();
        if (userId != null) {
            metadata.put("userId", userId);
        }
        UserMessage.Builder userMessageBuilder = UserMessage.builder()
                .text(text == null ? "" : text)
                .metadata(metadata);
        if (!media.isEmpty()) {
            userMessageBuilder.media(media);
        }

        Map<String, Object> toolContext = new HashMap<>();
        if (userId != null) {
            toolContext.put("userId", userId);
        }

        // 历史记忆 / RAG / 长期记忆 全部通过 Advisor 链完成，conversationId 关联本次会话。
        return chatClient.prompt()
                .messages(userMessageBuilder.build())
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .toolContext(toolContext)
                .stream()
                .content();
    }

    /**
     * 查询当前登录用户的聊天历史记录。
     * <p>
     * 安全修复：用户身份只从 token 解析（UserInterceptor 已写入 ThreadLocal）。
     * 方法上不声明 userId —— 客户端即使仍带 {@code ?userId=xxx}，Spring MVC 对未声明的
     * 多余参数直接忽略、不会报错，因此旧前端无需改动；同时服务端也不可能再采信该参数，
     * 彻底杜绝任意登录用户传他人 userId 越权读取聊天记录。
     */
    @GetMapping("/getHistories")
    public Result getHistories() {
        Long currentUserId = ThreadLocalUserIdUtil.getCurrentId();
        if (currentUserId == null) {
            return Result.error("未登录，请先登录");
        }
        List<History> histories = chatMapper.getHisoriesByUserId(currentUserId);
        Map<Long, List<History>> groupBySessionAndSort = histories.stream()
                .collect(Collectors.groupingBy(
                        History::getSessionId, // 第一步：按sessionId分组
                        // 第二步：对每个分组的列表按datetime排序（升序：从早到晚）
                        Collectors.collectingAndThen(
                                Collectors.toList(), // 先收集为列表
                                list -> list.stream()
                                        .sorted(Comparator.comparing(History::getDatetime))
                                        .toList()
                        )
                ));
        return Result.success(groupBySessionAndSort);
    }

    /**
     * 新建session(新对话)
     */
    @GetMapping("/newSession")
    public Result newSession() {
        return Result.success(snowflake.nextId());
    }

}