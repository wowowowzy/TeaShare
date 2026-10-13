package com.example.tea.config;

import com.example.tea.entity.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局统一异常处理
 * 捕获所有Controller抛出的异常，包括未捕获的Exception
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 忽略扫描器乱访问页面的错误
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public void handleNoResourceFound() {}

    /**
     * 忽略非法请求字符错误
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public void handleIllegalArgument() {}


    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public void handleMethodNotSupported() {}

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public void handleMessageNotReadable() {
    }
    /**
     * 拦截畸形 JSON/XML 请求体（常见于反序列化漏洞探测）
     */
    /**
     * 4. 拦截超大文件上传（防 DoS 攻击）
     * 直接返回 413，切断连接。
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public void handleMaxUploadSizeExceeded() {}
    /**
     * 其他未知异常
     */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        log.error("【全局异常捕获】系统未知异常：", e);
        return Result.error("未知异常");
    }
}
