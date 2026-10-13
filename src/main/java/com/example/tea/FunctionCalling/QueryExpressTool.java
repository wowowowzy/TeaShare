package com.example.tea.FunctionCalling;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class QueryExpressTool {
    private static final String MCP_URL = "https://dashscope.aliyuncs.com/api/v1/mcps/market-cmapi021863/mcp";

    /** 快递查询 MCP 的 DashScope API Key，从环境变量注入，禁止硬编码。 */
    @org.springframework.beans.factory.annotation.Value("${tea.mcp.express.api-key:}")
    private String apiKey;


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
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(MCP_URL, requestEntity, String.class);

        return response.getBody();
    }

}
