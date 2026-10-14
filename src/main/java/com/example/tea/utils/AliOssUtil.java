package com.example.tea.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;

@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    /**
     * 文件上传
     * <p>
     * 修复：上传失败必须抛异常，绝不返回"假成功"的 URL。
     * 原实现捕获 OSSException/ClientException 后仅打印日志，仍然拼出访问地址返回，
     * 调用方会误以为上传成功并把该地址写进数据库（指向不存在的文件）。
     *
     * @param bytes      文件字节
     * @param objectName 对象名（含扩展名）
     * @return 可访问的文件地址
     * @throws IllegalArgumentException 入参为空
     * @throws RuntimeException         上传失败（被 OSS 拒绝或网络异常）
     */
    public String upload(byte[] bytes, String objectName) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("上传文件内容为空");
        }
        if (objectName == null || objectName.isBlank()) {
            throw new IllegalArgumentException("上传对象名为空");
        }

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 创建PutObject请求。
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
        } catch (OSSException oe) {
            // 请求已到达 OSS 但被拒绝：密钥被禁用、无权限、Bucket 不存在等
            log.error("OSS 上传被拒绝: errorCode={} errorMessage={} requestId={} hostId={} objectName={}",
                    oe.getErrorCode(), oe.getErrorMessage(), oe.getRequestId(), oe.getHostId(), objectName, oe);
            throw new RuntimeException("文件上传失败: " + oe.getErrorMessage(), oe);
        } catch (ClientException ce) {
            // 客户端/网络层问题：无法连通、超时等
            log.error("OSS 客户端异常: objectName={} message={}", objectName, ce.getMessage(), ce);
            throw new RuntimeException("文件上传失败: " + ce.getMessage(), ce);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        //文件访问路径规则 https://BucketName.Endpoint/ObjectName
        StringBuilder stringBuilder = new StringBuilder("https://");
        stringBuilder
                .append(bucketName)
                .append(".")
                .append(endpoint)
                .append("/")
                .append(objectName);

        log.info("文件上传到:{}", stringBuilder.toString());

        return stringBuilder.toString();
    }
}
