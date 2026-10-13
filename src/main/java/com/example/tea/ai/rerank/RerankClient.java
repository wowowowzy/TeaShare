package com.example.tea.ai.rerank;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 重排客户端。
 * <p>
 * 阿里云百炼(MaaS) 的 OpenAI 兼容端点只提供 chat / embedding，重排要用「原生」路由：
 * <pre>POST {scheme}://{host}/api/v1/services/rerank/text-rerank/text-rerank</pre>
 * 这里从 {@code spring.ai.openai.base-url}（形如 {@code https://xxx/compatible-mode}）推导出 host，
 * 再拼上可配置的 {@code tea.ai.rerank.path}，所以无需额外维护一份域名配置。
 */
@Slf4j
@Component
public class RerankClient {

    private final RestTemplate restTemplate = new RestTemplate();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String url;

    private final String apiKey;

    private final String model;

    public RerankClient(@Value("${spring.ai.openai.base-url}") String baseUrl,
                        @Value("${spring.ai.openai.api-key}") String apiKey,
                        @Value("${tea.ai.rerank.path:/api/v1/services/rerank/text-rerank/text-rerank}") String path,
                        @Value("${tea.ai.rerank.model:qwen3.7-text-rerank}") String model) {
        URI base = URI.create(baseUrl);
        StringBuilder origin = new StringBuilder(base.getScheme()).append("://").append(base.getHost());
        if (base.getPort() > 0) {
            origin.append(":").append(base.getPort());
        }
        this.url = origin.append(path).toString();
        this.apiKey = apiKey;
        this.model = model;
        log.info("重排端点初始化完成: {}", this.url);
    }

    /**
     * 对候选文档重排，返回相关性从高到低的结果。
     *
     * @param query     用户查询
     * @param documents 候选文档文本
     * @param topN      返回条数
     */
    public List<RerankResult> rerank(String query, List<String> documents, int topN) {
        if (documents == null || documents.isEmpty()) {
            return List.of();
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("input", Map.of("query", query == null ? "" : query, "documents", documents));
        body.put("parameters", Map.of(
                "top_n", Math.min(topN, documents.size()),
                "return_documents", false));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        final String payload;
        try {
            payload = objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new IllegalStateException("序列化重排请求失败", e);
        }

        ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(payload, headers), String.class);
        try {
            JsonNode results = objectMapper.readTree(response.getBody()).path("output").path("results");
            List<RerankResult> list = new ArrayList<>();
            for (JsonNode node : results) {
                list.add(new RerankResult(node.path("index").asInt(-1), node.path("relevance_score").asDouble(0d)));
            }
            return list;
        } catch (Exception e) {
            throw new IllegalStateException("解析重排响应失败", e);
        }
    }

}