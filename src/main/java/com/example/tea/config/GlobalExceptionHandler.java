package com.example.tea.config;

import com.example.tea.entity.pojo.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
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
     * 忽略扫描器乱访问页面的错误（返回 404 JSON，而非 200 空响应）
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result> handleNoResourceFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.error("资源不存在"));
    }

    /**
     * 非法请求参数
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Result.error(e.getMessage() == null ? "非法参数" : e.getMessage()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result> handleMethodNotSupported() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(Result.error("请求方法不支持"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result> handleMessageNotReadable() {
        return ResponseEntity.badRequest().body(Result.error("请求体格式错误"));
    }

    /**
     * 请求参数类型不匹配（如 sessionId 传了非数字、数值溢出）。
     * <p>
     * 这类异常发生在 Controller 方法执行<b>之前</b>的参数绑定阶段，因此
     * Controller 内部的 {@code onErrorResume} 兜底触及不到，必须在这里处理。
     * 若该接口是 SSE，同样要按 SSE 语义返回，否则前端会收到一坨裸 JSON。
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException e,
                                                HttpServletRequest request) {
        String name = e.getName();
        String msg = "参数 " + name + " 格式不正确";

        if (isEventStream(request)) {
            // SSE 接口：以 HTTP 200 + 结构化错误事件返回，保持流式语义一致
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body("{\"type\":\"error\",\"code\":0,\"msg\":\"" + msg + "\"}");
        }
        return ResponseEntity.badRequest().body(Result.error(msg));
    }

    /**
     * 判断本次请求是否走 SSE。
     * <p>
     * 依据：请求路径落在流式接口下 或 客户端显式声明了 Accept: text/event-stream。
     * 目的只有一个 —— 让错误响应与正常响应保持同一种媒体类型，前端才解析得动。
     */
    private boolean isEventStream(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains(MediaType.TEXT_EVENT_STREAM_VALUE)) {
            return true;
        }
        String uri = request.getRequestURI();
        return uri != null && uri.contains("/ai/generateStream");
    }
    /**
     * 拦截畸形 JSON/XML 请求体（常见于反序列化漏洞探测）
     */
    /**
     * 4. 拦截超大文件上传（防 DoS 攻击）
     * 直接返回 413，切断连接。
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Result> handleMaxUploadSizeExceeded() {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(Result.error("文件大小超出限制"));
    }
    /**
     * 其他未知异常。
     * <p>
     * 对 SSE 接口特殊处理：普通 JSON 错误体写进 {@code text/event-stream} 响应，
     * 前端只会收到一坨无法识别的裸 JSON。因此这里按流式语义返回结构化错误事件，
     * 与 {@code ChatController} 内部的 {@code onErrorResume} 兜底格式保持一致。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e, HttpServletRequest request) {
        log.error("【全局异常捕获】系统未知异常：", e);

        if (isEventStream(request)) {
            String msg = "AI 服务暂时不可用，请稍后重试";
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body("{\"type\":\"error\",\"code\":0,\"msg\":\"" + msg + "\"}");
        }
        return ResponseEntity.ok(Result.error("未知异常"));
    }
}
