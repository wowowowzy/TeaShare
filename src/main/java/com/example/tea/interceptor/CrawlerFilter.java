package com.example.tea.interceptor;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebFilter("/*")
public class CrawlerFilter implements Filter {

    // 只拦截爬虫扫街的垃圾路径
    // 你的 /admin、/phpmyadmin 完全不受影响！
    private static final List<String> BLOCK_PATHS = List.of(
            "analytics.html",
            "tuiliu",
            "login.php",
            "wp-admin",
            "console.php"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String uri = req.getRequestURI().toLowerCase();

        // 只拦上面这些明确的爬虫垃圾
        for (String path : BLOCK_PATHS) {
            if (uri.contains(path)) {
                resp.setStatus(404);
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
