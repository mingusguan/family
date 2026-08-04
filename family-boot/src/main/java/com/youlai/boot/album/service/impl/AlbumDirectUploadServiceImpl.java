package com.youlai.boot.album.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.youlai.boot.album.enums.AlbumMediaTypeEnum;
import com.youlai.boot.album.mapper.AlbumAssetMapper;
import com.youlai.boot.album.mapper.AlbumGroupMapper;
import com.youlai.boot.album.mapper.AlbumUploadBatchMapper;
import com.youlai.boot.album.model.AlbumModels.AlbumDirectUploadConfirmRequest;
import com.youlai.boot.album.model.AlbumModels.AlbumDirectUploadFileRequest;
import com.youlai.boot.album.model.AlbumModels.AlbumDirectUploadInitRequest;
import com.youlai.boot.album.model.AlbumModels.AlbumDirectUploadInitVO;
import com.youlai.boot.album.model.AlbumModels.AlbumDirectUploadItemVO;
import com.youlai.boot.album.model.AlbumModels.AlbumDirectUploadStatusVO;
import com.youlai.boot.album.model.AlbumModels.AlbumDirectUploadTicketVO;
import com.youlai.boot.album.model.entity.AlbumAsset;
import com.youlai.boot.album.model.entity.AlbumGroup;
import com.youlai.boot.album.model.entity.AlbumUploadBatch;
import com.youlai.boot.album.service.AlbumDirectUploadService;
import com.youlai.boot.appuser.service.AppUserService;
import com.youlai.boot.common.exception.BusinessException;
import com.youlai.boot.family.service.FamilyService;
import com.youlai.boot.file.service.DirectUploadStorageService;
import com.youlai.boot.framework.integration.wxma.service.WxContentSecurityService;
import com.youlai.boot.framework.security.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

/** 小程序相册直传用例实现。 */
@Service
public class AlbumDirectUploadServiceImpl implements AlbumDirectUploadService {
    private static final long MAX_FILE_SIZE = 100L * 1024 * 1024;
    private static final long MAX_BATCH_SIZE = 500L * 1024 * 1024;
    private static final long MAX_THUMBNAIL_SIZE = 10L * 1024 * 1024;
    private static final long TICKET_SECONDS = 10 * 60L;

    private final AlbumAssetMapper assetMapper;
    private final AlbumUploadBatchMapper batchMapper;
    private final AlbumGroupMapper groupMapper;
    private final AppUserService appUserService;
    private final FamilyService familyService;
    private final WxContentSecurityService contentSecurityService;
    private final DirectUploadStorageService storageService;
    private final AlbumDirectUploadProcessor processor;
    private final Executor executor;

    public AlbumDirectUploadServiceImpl(
            AlbumAssetMapper assetMapper,
            AlbumUploadBatchMapper batchMapper,
            AlbumGroupMapper groupMapper,
            AppUserService appUserService,
            FamilyService familyService,
            WxContentSecurityService contentSecurityService,
            DirectUploadStorageService storageService,
            AlbumDirectUploadProcessor processor,
            @Qualifier("albumMediaExecutor") Executor executor
    ) {
        this.assetMapper = assetMapper;
        this.batchMapper = batchMapper;
        this.groupMapper = groupMapper;
        this.appUserService = appUserService;
        this.familyService = familyService;
        this.contentSecurityService = contentSecurityService;
        this.storageService = storageService;
        this.processor = processor;
        this.executor = executor;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AlbumDirectUploadInitVO initialize(AlbumDirectUploadInitRequest request) {
        Long userId = requireCurrentUserId();
        if (appUserService.getById(userId) == null) {
            throw new BusinessException("所属用户不存在");
        }
        familyService.ensureCurrentUserMember(request.getFamilyId());
        familyService.ensureAlbumBelongsToFamily(request.getAlbumId(), request.getFamilyId());
        contentSecurityService.checkText(userId, List.of(StrUtil.blankToDefault(request.getDescription(), "")));

        long declaredTotal = request.getFiles().stream()
                .mapToLong(AlbumDirectUploadFileRequest::getFileSize)
                .sum();
        if (declaredTotal > MAX_BATCH_SIZE) {
            throw new BusinessException("一次上传的文件总大小不能超过500MB");
        }

        String batchId = UUID.randomUUID().toString().replace("-", "");
        AlbumUploadBatch batch = new AlbumUploadBatch();
        batch.setBatchId(batchId);
        batch.setUploaderId(userId);
        batch.setFamilyId(request.getFamilyId());
        batch.setAlbumId(request.getAlbumId());
        batch.setTotalCount(request.getFiles().size());
        batch.setProcessingCount(0);
        batch.setSuccessCount(0);
        batch.setFailedCount(0);
        batch.setStatus("INIT");
        batchMapper.insert(batch);

        List<AlbumAsset> assets = new ArrayList<>(request.getFiles().size());
        List<AlbumDirectUploadItemVO> uploads = new ArrayList<>(request.getFiles().size());
        String dateFolder = LocalDate.now().toString().replace("-", "");
        for (int index = 0; index < request.getFiles().size(); index++) {
            AlbumDirectUploadFileRequest file = request.getFiles().get(index);
            String suffix = safeSuffix(file.getOriginalName());
            String prefix = "album-direct/" + dateFolder + "/" + userId + "/" + batchId + "/";
            String objectKey = prefix + index + "-" + UUID.randomUUID().toString().replace("-", "") + suffix;
            String thumbnailKey = Boolean.TRUE.equals(file.getHasThumbnail())
                    ? prefix + index + "-cover-" + UUID.randomUUID().toString().replace("-", "") + ".jpg"
                    : null;

            AlbumAsset asset = AlbumAsset.createPendingDirect(
                    userId, request.getFamilyId(), request.getAlbumId(), batchId,
                    request.getDescription(), file, storageService.getObjectUrl(objectKey),
                    thumbnailKey == null ? null : storageService.getObjectUrl(thumbnailKey)
            );
            asset.changeGroup(resolveMediaGroupId(file.getMediaType()));
            assets.add(asset);

            AlbumDirectUploadItemVO item = new AlbumDirectUploadItemVO();
            item.setIndex(index);
            item.setFile(toTicket(storageService.createUploadTicket(objectKey, MAX_FILE_SIZE, TICKET_SECONDS)));
            if (thumbnailKey != null) {
                item.setThumbnail(toTicket(storageService.createUploadTicket(
                        thumbnailKey, MAX_THUMBNAIL_SIZE, TICKET_SECONDS)));
            }
            uploads.add(item);
        }
        if (!Db.saveBatch(assets)) {
            throw new BusinessException("创建上传批次失败");
        }

        AlbumDirectUploadInitVO result = new AlbumDirectUploadInitVO();
        result.setBatchId(batchId);
        result.setExpiresAt(LocalDateTime.now().plusSeconds(TICKET_SECONDS));
        result.setUploads(uploads);
        return result;
    }

    @Override
    public AlbumDirectUploadStatusVO confirm(AlbumDirectUploadConfirmRequest request) {
        Long userId = requireCurrentUserId();
        AlbumUploadBatch batch = requireOwnedBatch(request.getBatchId(), userId);
        if (!"INIT".equals(batch.getStatus())) {
            return toStatus(batchMapper.selectById(batch.getId()));
        }

        List<AlbumAsset> assets = assetMapper.selectList(new LambdaQueryWrapper<AlbumAsset>()
                .eq(AlbumAsset::getUploadBatchId, batch.getBatchId())
                .eq(AlbumAsset::getUploaderId, userId)
                .eq(AlbumAsset::getStatus, 0)
                .orderByAsc(AlbumAsset::getId));
        Set<Integer> uploaded = new HashSet<>(request.getUploadedIndexes());
        Set<Integer> thumbnails = request.getThumbnailUploadedIndexes() == null
                ? Set.of() : new HashSet<>(request.getThumbnailUploadedIndexes());
        List<AlbumAsset> processing = new ArrayList<>();
        long actualTotal = 0;

        for (int index = 0; index < assets.size(); index++) {
            AlbumAsset asset = assets.get(index);
            if (!uploaded.contains(index)) {
                assetMapper.deleteById(asset.getId());
                continue;
            }
            try {
                DirectUploadStorageService.StoredObject stored = storageService.headObject(asset.getUrl());
                if (stored.size() <= 0 || stored.size() > MAX_FILE_SIZE) {
                    throw new IllegalArgumentException("文件大小不合法");
                }
                actualTotal += stored.size();
                asset.setFileSize(stored.size());
                asset.setMimeType(StrUtil.blankToDefault(stored.contentType(), asset.getMimeType()));
                if (StrUtil.isNotBlank(asset.getThumbnailUrl())) {
                    if (thumbnails.contains(index)) {
                        DirectUploadStorageService.StoredObject thumbnail = storageService.headObject(asset.getThumbnailUrl());
                        actualTotal += thumbnail.size();
                    } else {
                        asset.removeThumbnail();
                    }
                }
                assetMapper.updateById(asset);
                processing.add(asset);
            } catch (Exception exception) {
                safeDelete(asset.getUrl());
                safeDelete(asset.getThumbnailUrl());
                assetMapper.deleteById(asset.getId());
            }
        }
        if (actualTotal > MAX_BATCH_SIZE) {
            processing.forEach(asset -> {
                safeDelete(asset.getUrl());
                safeDelete(asset.getThumbnailUrl());
                assetMapper.deleteById(asset.getId());
            });
            processing.clear();
        }

        int failed = batch.getTotalCount() - processing.size();
        if (batchMapper.confirm(batch.getBatchId(), processing.size(), failed) == 0) {
            return getStatus(batch.getBatchId());
        }
        for (AlbumAsset asset : processing) {
            try {
                executor.execute(() -> processor.process(asset.getId(), batch.getBatchId()));
            } catch (RejectedExecutionException exception) {
                safeDelete(asset.getUrl());
                safeDelete(asset.getThumbnailUrl());
                assetMapper.deleteById(asset.getId());
                batchMapper.incrementFailure(batch.getBatchId());
            }
        }
        return getStatus(batch.getBatchId());
    }

    @Override
    public AlbumDirectUploadStatusVO getStatus(String batchId) {
        return toStatus(requireOwnedBatch(batchId, requireCurrentUserId()));
    }

    private AlbumUploadBatch requireOwnedBatch(String batchId, Long userId) {
        AlbumUploadBatch batch = batchMapper.selectOne(new LambdaQueryWrapper<AlbumUploadBatch>()
                .eq(AlbumUploadBatch::getBatchId, batchId)
                .eq(AlbumUploadBatch::getUploaderId, userId)
                .last("LIMIT 1"));
        if (batch == null) {
            throw new BusinessException("上传批次不存在");
        }
        return batch;
    }

    private AlbumDirectUploadStatusVO toStatus(AlbumUploadBatch batch) {
        AlbumDirectUploadStatusVO result = new AlbumDirectUploadStatusVO();
        result.setBatchId(batch.getBatchId());
        result.setStatus(batch.getStatus());
        result.setTotal(batch.getTotalCount());
        result.setProcessing(batch.getProcessingCount());
        result.setSuccess(batch.getSuccessCount());
        result.setFailed(batch.getFailedCount());
        return result;
    }

    private AlbumDirectUploadTicketVO toTicket(DirectUploadStorageService.UploadTicket source) {
        AlbumDirectUploadTicketVO target = new AlbumDirectUploadTicketVO();
        target.setUploadUrl(source.uploadUrl());
        target.setObjectKey(source.objectKey());
        target.setPolicy(source.policy());
        target.setQSignAlgorithm(source.qSignAlgorithm());
        target.setQAk(source.qAk());
        target.setQKeyTime(source.qKeyTime());
        target.setQSignature(source.qSignature());
        target.setSecurityToken(source.securityToken());
        return target;
    }

    private Long resolveMediaGroupId(AlbumMediaTypeEnum mediaType) {
        AlbumGroup group = groupMapper.selectOne(new LambdaQueryWrapper<AlbumGroup>()
                .eq(AlbumGroup::getName, mediaType.getLabel())
                .last("LIMIT 1"));
        if (group == null) {
            throw new BusinessException("缺少“" + mediaType.getLabel() + "”系统分组");
        }
        return group.getId();
    }

    private String safeSuffix(String originalName) {
        String suffix = FileUtil.getSuffix(StrUtil.blankToDefault(originalName, ""));
        return suffix.matches("(?i)[a-z0-9]{1,10}") ? "." + suffix.toLowerCase() : "";
    }

    private Long requireCurrentUserId() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            throw new BusinessException("请先登录后再操作相册");
        }
        return userId;
    }

    private void safeDelete(String url) {
        if (StrUtil.isBlank(url)) {
            return;
        }
        try {
            storageService.deleteObject(url);
        } catch (Exception ignored) {
            // 清理失败不影响其他资源确认，后续可通过COS生命周期回收直传前缀。
        }
    }
}
