package com.youlai.boot.album.service.impl;

import cn.hutool.core.util.StrUtil;
import com.youlai.boot.album.enums.AlbumMediaTypeEnum;
import com.youlai.boot.album.mapper.AlbumAssetMapper;
import com.youlai.boot.album.mapper.AlbumUploadBatchMapper;
import com.youlai.boot.album.model.entity.AlbumAsset;
import com.youlai.boot.file.service.DirectUploadStorageService;
import com.youlai.boot.file.service.MediaCaptureTimeExtractor;
import com.youlai.boot.framework.integration.wxma.service.WxContentSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

/** 单个直传媒体的隔离处理器，任何一个文件失败都不会回滚其他文件。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlbumDirectUploadProcessor {
    private static final long MAX_FILE_SIZE = 100L * 1024 * 1024;

    private final AlbumAssetMapper assetMapper;
    private final AlbumUploadBatchMapper batchMapper;
    private final DirectUploadStorageService storageService;
    private final MediaCaptureTimeExtractor captureTimeExtractor;
    private final WxContentSecurityService contentSecurityService;

    public void process(Long assetId, String batchId) {
        AlbumAsset asset = assetMapper.selectById(assetId);
        if (asset == null) {
            batchMapper.incrementFailure(batchId);
            return;
        }
        Path tempFile = null;
        try {
            DirectUploadStorageService.StoredObject stored = storageService.headObject(asset.getUrl());
            if (stored.size() <= 0 || stored.size() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("COS对象大小不合法");
            }

            String suffix = StrUtil.subAfter(StrUtil.blankToDefault(asset.getOriginalName(), "media.tmp"), '.', true);
            tempFile = Files.createTempFile("album-direct-", StrUtil.isBlank(suffix) ? ".tmp" : "." + suffix);
            try (InputStream input = storageService.openStream(asset.getUrl())) {
                Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // 图片通过微信安全检测后才从隐藏状态切换为可见，视频和音频保持现有免检规则。
            if (asset.getMediaType() == AlbumMediaTypeEnum.IMAGE) {
                contentSecurityService.checkImage(tempFile);
            }
            LocalDateTime capturedAt = captureTimeExtractor
                    .extract(tempFile, stored.size(), stored.contentType())
                    .orElse(asset.getCreateTime() == null ? LocalDateTime.now() : asset.getCreateTime());
            String verifiedMime = StrUtil.blankToDefault(stored.contentType(), asset.getMimeType());
            asset.approveDirectUpload(stored.size(), verifiedMime, capturedAt);
            assetMapper.updateById(asset);
            batchMapper.incrementSuccess(batchId);
        } catch (Exception exception) {
            log.warn("相册直传资源处理失败，batchId={}, assetId={}", batchId, assetId, exception);
            safeDelete(asset.getUrl());
            safeDelete(asset.getThumbnailUrl());
            assetMapper.deleteById(assetId);
            batchMapper.incrementFailure(batchId);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (Exception exception) {
                    log.warn("删除相册直传临时文件失败，path={}", tempFile, exception);
                }
            }
        }
    }

    private void safeDelete(String objectUrl) {
        if (StrUtil.isBlank(objectUrl)) {
            return;
        }
        try {
            storageService.deleteObject(objectUrl);
        } catch (Exception exception) {
            log.warn("清理直传COS对象失败，url={}", objectUrl, exception);
        }
    }
}
