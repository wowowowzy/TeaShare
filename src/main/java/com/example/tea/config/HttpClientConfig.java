package com.example.tea.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpClientConfig {

    @Primary
    @Bean
    public RestClient.Builder aiRestClientBuilder() {
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();

        connManager.setMaxTotal(50);
        connManager.setDefaultMaxPerRoute(20);

        // ✅ 修复弃用警告：使用 ConnectionConfig 替代直接调用 setValidateAfterInactivity
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setValidateAfterInactivity(TimeValue.ofSeconds(20)) // 空闲20秒后，复用前校验连接活性
                .build();
        connManager.setDefaultConnectionConfig(connectionConfig);

        HttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connManager)
                .evictExpiredConnections()          // 后台线程清理过期连接
                .evictIdleConnections(TimeValue.ofSeconds(30)) // 后台线程清理空闲>30秒的连接
                .build();

        return RestClient.builder()
                .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));
    }
}
