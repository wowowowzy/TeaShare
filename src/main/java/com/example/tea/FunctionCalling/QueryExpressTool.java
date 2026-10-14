package com.example.tea.FunctionCalling;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class QueryExpressTool {
    private static final String MCP_URL = "https://dashscope.aliyuncs.com/api/v1/mcps/market-cmapi021863/mcp";

    /** 快递查询 MCP 的 DashScope API Key，从环境变量注入，禁止硬编码。 */
    @org.springframework.beans.factory.annotation.Value("${tea.mcp.express.api-key:}")
    private String apiKey;

    /**
     * 专用 HTTP 客户端（文件内单例，线程安全）。
     * <p>
     * 为什么不用全局 Bean：全局 AI 的 RestClient 与 Spring AI 共享连接池，
     * 给它设超时会波及 LLM 流式调用；这里自建小池并设短超时（外部工具不能拖死请求线程）。
     * 原实现每次调用都 {@code new RestTemplate()}，既不池化也无超时。
     */
    private static final RestTemplate REST_TEMPLATE = buildRestTemplate();

    private static RestTemplate buildRestTemplate() {
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(10))
                .setSocketTimeout(Timeout.ofSeconds(30))
                .build();
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(20);
        connManager.setDefaultMaxPerRoute(10);
        connManager.setDefaultConnectionConfig(connectionConfig);
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connManager)
                .evictExpiredConnections()
                .evictIdleConnections(TimeValue.ofSeconds(30))
                .build();
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
    }


    @Tool(description = "当用户需要查看快递信息的时候,记住用户快递编号查询信息并且整理好")
    public String queryExpress(String no) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        String requestBody = String.format("""
            {
                "jsonrpc": "2.0",
                "id": 1,
                "method": "tools/call",
                "params": {
                    "name": "全国快递物流查询",
                    "arguments": {
                        "no": "%s"
                    }
                }
            }
        """, no);


        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = REST_TEMPLATE.postForEntity(MCP_URL, requestEntity, String.class);

        return response.getBody();
    }

}
