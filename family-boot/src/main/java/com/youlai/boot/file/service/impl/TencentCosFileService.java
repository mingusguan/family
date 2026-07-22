package com.youlai.boot.file.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import com.youlai.boot.common.exception.BusinessException;
import com.youlai.boot.common.result.ResultCode;
import com.youlai.boot.file.model.FileInfo;
import com.youlai.boot.file.service.FileService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 腾讯云 COS 文件服务。
 */
@Data
@Slf4j
@Component
@ConditionalOnProperty(value = "oss.type", havingValue = "cos")
@ConfigurationProperties(prefix = "oss.cos")
@RequiredArgsConstructor
public class TencentCosFileService implements FileService {

    /** COS 地域简称，例如 ap-guangzhou。 */
    private String region;

    /** 包含 APPID 的完整存储桶名称。 */
    private String bucketName;

    /** 腾讯云 API SecretId。 */
    private String secretId;

    /** 腾讯云 API SecretKey。 */
    private String secretKey;

    /** 自定义 CDN 或 COS 访问域名。 */
    private String customDomain;

    /** 私有资源临时签名地址的有效期，单位秒。 */
    private long presignedUrlExpirationSeconds = 3600;

    private COSClient cosClient;

    /** 初始化 COS 客户端。 */
    @PostConstruct
    public void init() {
        COSCredentials credentials = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        clientConfig.setHttpProtocol(HttpProtocol.https);
        cosClient = new COSClient(credentials, clientConfig);
    }

    /**
     * 上传文件到腾讯云 COS。
     *
     * @param file 表单文件对象
     * @return 文件信息
     */
    @Override
    public FileInfo uploadFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        String dateFolder = DateUtil.format(LocalDateTime.now(), "yyyyMMdd");
        String fileName = IdUtil.simpleUUID() + (StrUtil.isBlank(suffix) ? "" : "." + suffix);
        String objectKey = dateFolder + "/" + fileName;

        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            // 服务端统一持有密钥并将文件流写入 COS，避免向前端暴露长期密钥
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectKey, inputStream, metadata);
            cosClient.putObject(putObjectRequest);

            FileInfo fileInfo = new FileInfo();
            fileInfo.setName(originalFilename);
            String fileUrl = buildFileUrl(objectKey);
            fileInfo.setUrl(fileUrl);
            fileInfo.setPreviewUrl(getAccessUrl(fileUrl));
            return fileInfo;
        } catch (Exception e) {
            log.error("腾讯云 COS 文件上传失败，bucketName={}", bucketName, e);
            throw new BusinessException(ResultCode.UPLOAD_FILE_EXCEPTION, e.getMessage());
        }
    }

    /**
     * 删除腾讯云 COS 文件。
     *
     * @param filePath 文件完整 URL 或对象键
     * @return 是否删除成功
     */
    @Override
    public boolean deleteFile(String filePath) {
        Assert.notBlank(filePath, "删除文件路径不能为空");
        try {
            cosClient.deleteObject(bucketName, extractObjectKey(filePath));
            return true;
        } catch (Exception e) {
            log.error("腾讯云 COS 文件删除失败，bucketName={}, filePath={}", bucketName, filePath, e);
            throw new BusinessException(ResultCode.DELETE_FILE_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public String getAccessUrl(String filePath) {
        Assert.notBlank(filePath, "文件路径不能为空");
        String objectKey = extractObjectKey(removeQueryString(filePath));
        Date expiration = new Date(System.currentTimeMillis() + presignedUrlExpirationSeconds * 1000);

        // 私有读对象每次展示时动态签名，避免将会过期的地址持久化
        return generateInlinePreviewUrl(objectKey, expiration);
    }

    private String generateInlinePreviewUrl(String objectKey, Date expiration) {
        // 覆盖对象原有的下载响应头，让浏览器按图片、视频或音频类型直接预览
        com.qcloud.cos.model.ResponseHeaderOverrides responseHeaders = new com.qcloud.cos.model.ResponseHeaderOverrides();
        responseHeaders.setContentDisposition("inline");
        com.qcloud.cos.model.GeneratePresignedUrlRequest request = new com.qcloud.cos.model.GeneratePresignedUrlRequest(
                bucketName,
                objectKey,
                HttpMethodName.GET
        );
        request.setExpiration(expiration);
        request.setResponseHeaders(responseHeaders);
        return cosClient.generatePresignedUrl(request).toString();
    }

    private String buildFileUrl(String objectKey) {
        return resolveAccessDomain() + "/" + objectKey;
    }

    private String extractObjectKey(String filePath) {
        String prefix = resolveAccessDomain() + "/";

        // 删除接口既接受上传接口返回的完整 URL，也兼容直接传入对象键
        if (filePath.startsWith(prefix)) {
            return filePath.substring(prefix.length());
        }

        // 兼容修复前已经保存的无协议自定义域名 URL
        if (StrUtil.isNotBlank(customDomain)) {
            String legacyPrefix = StrUtil.removeSuffix(customDomain.trim(), "/") + "/";
            if (filePath.startsWith(legacyPrefix)) {
                return filePath.substring(legacyPrefix.length());
            }
        }
        return StrUtil.removePrefix(filePath, "/");
    }

    private String resolveAccessDomain() {
        if (StrUtil.isBlank(customDomain)) {
            return "https://" + bucketName + ".cos." + region + ".myqcloud.com";
        }

        String domain = StrUtil.removeSuffix(customDomain.trim(), "/");
        // 浏览器会将不带协议的域名当成当前后台站点的相对路径
        boolean hasHttpScheme = domain.regionMatches(true, 0, "http://", 0, "http://".length())
                || domain.regionMatches(true, 0, "https://", 0, "https://".length());
        return hasHttpScheme ? domain : "https://" + domain;
    }

    private String removeQueryString(String filePath) {
        int queryIndex = filePath.indexOf('?');
        return queryIndex < 0 ? filePath : filePath.substring(0, queryIndex);
    }

    /** 应用关闭时释放 COS 客户端连接资源。 */
    @PreDestroy
    public void destroy() {
        if (cosClient != null) {
            cosClient.shutdown();
        }
    }
}
