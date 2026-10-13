package com.example.tea.ai.memory;

import com.example.tea.entity.pojo.ChatModel.History;
import com.example.tea.mapper.ChatMapper;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 以现有 {@code sys_history} 表为存储的聊天记忆仓储。
 * <p>
 * Spring AI 的 {@code MessageWindowChatMemory} 每次 {@code saveAll} 传入的是
 * <b>整段窗口</b>（历史 + 本轮，且已按窗口裁剪）。本仓储把窗口与库中尾部对齐，
 * 只<b>追加</b>尚未落库的新消息：{@code sys_history} 保留完整会话记录（供前端查历史），
 * 窗口大小只影响喂给模型的历史长度。
 * 入参 userId 由 UserMessage 的 metadata 携带（controller 侧写入）。
 */
@Component
public class MyBatisChatMemoryRepository implements ChatMemoryRepository {

    /** 单次从库里读取的消息条数（与 ChatMemory 窗口保持一致或更大）。 */
    private final int windowSize;

    private final ChatMapper chatMapper;

    public MyBatisChatMemoryRepository(ChatMapper chatMapper,
                                       @Value("${tea.ai.memory.window-size:20}") int windowSize) {
        this.chatMapper = chatMapper;
        this.windowSize = windowSize;
    }

    @Override
    public List<String> findConversationIds() {
        return chatMapper.listSessionIds().stream().map(String::valueOf).toList();
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        Long sessionId = parse(conversationId);
        if (sessionId == null) {
            return List.of();
        }
        List<History> rows = chatMapper.listWindowBySessionId(sessionId, windowSize);
        List<Message> messages = new ArrayList<>(rows.size());
        for (History row : rows) {
            // 工具调用轮次可能产生空内容的助手消息，不纳入记忆，避免挤占窗口
            if (nullToEmpty(row.getContent()).isBlank()) {
                continue;
            }
            messages.add(toMessage(row));
        }
        return messages;
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        Long sessionId = parse(conversationId);
        if (sessionId == null || messages == null || messages.isEmpty()) {
            return;
        }
        // Spring AI 传入的是「窗口」(历史 + 本轮, 已裁剪)。这里只做**追加**：
        // 把窗口与库中尾部对齐，算出尚未落库的新消息，其余原样保留。
        // 这样 sys_history 始终是完整会话记录（前端历史可用），窗口大小只影响喂给模型的历史长度。
        List<Message> persistable = messages.stream()
                .filter(message -> !nullToEmpty(message.getText()).isBlank())
                .toList();
        if (persistable.isEmpty()) {
            return;
        }
        List<List<String>> desired = persistable.stream()
                .map(message -> List.of(roleOf(message), nullToEmpty(message.getText())))
                .toList();
        List<History> tail = chatMapper.listWindowBySessionId(sessionId, desired.size());

        int matched = 0;
        for (int len = Math.min(tail.size(), desired.size()); len > 0; len--) {
            if (matches(tail.subList(tail.size() - len, tail.size()), desired.subList(0, len))) {
                matched = len;
                break;
            }
        }

        // 尾部对齐失败(极端情况)：仅补写最后一条，避免污染历史
        List<Message> newMessages = matched == 0
                ? List.of(persistable.get(persistable.size() - 1))
                : persistable.subList(matched, persistable.size());
        if (newMessages.isEmpty()) {
            return;
        }

        Long fallbackUserId = tail.stream()
                .map(History::getUserId)
                .filter(Objects::nonNull)
                .reduce((first, second) -> second)
                .orElse(null);

        List<History> toInsert = new ArrayList<>(newMessages.size());
        for (Message message : newMessages) {
            toInsert.add(History.builder()
                    .datetime(LocalDateTime.now())
                    .content(nullToEmpty(message.getText()))
                    .role(roleOf(message))
                    .sessionId(sessionId)
                    .userId(resolveUserId(message, fallbackUserId))
                    .build());
        }
        chatMapper.batchInsert(toInsert);
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        Long sessionId = parse(conversationId);
        if (sessionId != null) {
            chatMapper.deleteBySessionId(sessionId);
        }
    }

    private boolean matches(List<History> stored, List<List<String>> desired) {
        if (stored.size() != desired.size()) {
            return false;
        }
        for (int i = 0; i < stored.size(); i++) {
            History history = stored.get(i);
            List<String> message = desired.get(i);
            if (!Objects.equals(nullToEmpty(history.getRole()), message.get(0))
                    || !Objects.equals(nullToEmpty(history.getContent()), message.get(1))) {
                return false;
            }
        }
        return true;
    }

    private Long resolveUserId(Message message, Long fallback) {
        Object value = message.getMetadata() == null ? null : message.getMetadata().get("userId");
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static Message toMessage(History history) {
        Map<String, Object> metadata = new HashMap<>();
        if (history.getDatetime() != null) {
            metadata.put("datetime", history.getDatetime().toString());
        }
        if (history.getUserId() != null) {
            metadata.put("userId", history.getUserId());
        }
        String content = nullToEmpty(history.getContent());
        return switch (history.getRole() == null ? "user" : history.getRole()) {
            case "assistant" -> AssistantMessage.builder().content(content).properties(metadata).build();
            case "system" -> new SystemMessage(content);
            default -> UserMessage.builder().text(content).metadata(metadata).build();
        };
    }

    private static String roleOf(Message message) {
        MessageType type = message.getMessageType();
        return switch (type) {
            case ASSISTANT -> "assistant";
            case SYSTEM -> "system";
            case TOOL -> "tool";
            default -> "user";
        };
    }

    private static Long parse(String conversationId) {
        try {
            return Long.valueOf(conversationId);
        } catch (Exception e) {
            return null;
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

}