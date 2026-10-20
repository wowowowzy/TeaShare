package com.example.tea.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpClientConfig {

    /**
     * 共享连接池（供 Spring AI 的 WebClient 复用）。
     * <p>
     * 说明：这里的连接复用策略直接关系到 AI 流式调用会不会出现
     * {@code java.net.SocketException: Connection reset}。
     * <ul>
     *   <li>{@code validateAfterInactivity(20s)}：连接空闲超过 20s 后，复用前先做活性校验。
     *       空闲连接可能已被对端（网关/负载均衡）单方面关闭，不做校验直接复用就会踩到 RST。</li>
     *   <li>{@code evictIdleConnections(30s)}：后台线程主动清理空闲连接，减少「拿到死连接」的概率。</li>
     *   <li>总连接数 / 单路由上限保持与上游 QPS 匹配，避免排队等待被对端超时断开。</li>
     * </ul>
     * 注意：该 Builder 是 Spring AI 自动配置 WebClient 时使用的 {@code RestClient.Builder}
     * （{@code @Primary}），改动会影响所有走 AI 的 HTTP 调用。
     */
    @Primary
    @Bean
    public RestClient.Builder aiRestClientBuilder() {
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();

        connManager.setMaxTotal(50);
        connManager.setDefaultMaxPerRoute(20);

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                // 空闲 20 秒后，复用前校验连接活性（防止复用已被对端关闭的连接）
                .setValidateAfterInactivity(TimeValue.ofSeconds(20))
                // 建立连接超时：避免上游不可达时长时间挂起
                .setConnectTimeout(Timeout.ofSeconds(15))
                // 从池中获取连接的超时：池被占满时快速失败，而不是无限等待
                .setSocketTimeout(Timeout.ofMinutes(5))
                .build();
        connManager.setDefaultConnectionConfig(connectionConfig);

        HttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connManager)
                .evictExpiredConnections()          // 后台线程清理过期连接
                .evictIdleConnections(TimeValue.ofSeconds(30)) // 后台线程清理空闲>30秒的连接
                // 连接建立失败时重试（幂等）；流式响应内容读取阶段不会重试，由上层兜底
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectionRequestTimeout(Timeout.ofSeconds(15))
                        .setResponseTimeout(Timeout.ofMinutes(5))
                        .build())
                .build();

        return RestClient.builder()
                .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));
    }
}
