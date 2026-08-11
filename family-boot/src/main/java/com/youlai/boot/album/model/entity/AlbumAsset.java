package com.youlai.boot.album.model.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.youlai.boot.album.enums.AlbumMediaTypeEnum;
import com.youlai.boot.album.model.AlbumModels.AlbumAssetSaveRequest;
import com.youlai.boot.album.model.AlbumModels.AlbumMomentCreateRequest;
import com.youlai.boot.album.model.AlbumModels.AlbumMomentResourceRequest;
import com.youlai.boot.album.model.AlbumModels.AlbumDirectUploadFileRequest;
import com.youlai.boot.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * APP 相册资源实体。
 */
@Getter
@Setter
@TableName("app_album_asset")
public class AlbumAsset extends BaseEntity {

    private Long uploaderId;
    private Long familyId;
    private Long albumId;
    private String uploadBatchId;
    private AlbumMediaTypeEnum mediaType;
    private String url;
    private String thumbnailUrl;
    private String originalName;
    private String mimeType;
    private Long fileSize;
    private Long duration;
    private Integer width;
    private Integer height;
    private Long groupId;
    private String description;
    private LocalDateTime capturedAt;
    private Integer status;

    @TableLogic
    private Integer isDeleted;

    /**
     * 根据当前登录用户创建可公开展示的图片时刻。
     */
    @JsonIgnore
    public static AlbumAsset createMoment(
            Long uploaderId,
            AlbumMomentCreateRequest request,
            AlbumMomentResourceRequest resource
    ) {
        AlbumAsset asset = new AlbumAsset();
        asset.uploaderId = uploaderId;
        asset.familyId = request.getFamilyId();
        asset.albumId = request.getAlbumId();
        asset.mediaType = resource.getMediaType();
        asset.url = resource.getUrl();
        asset.thumbnailUrl = resource.getThumbnailUrl();
        asset.originalName = resource.getOriginalName();
        asset.mimeType = resource.getMimeType();
        asset.fileSize = resource.getFileSize();
        asset.duration = resource.getDuration();
        asset.width = resource.getWidth();
        asset.height = resource.getHeight();
        asset.description = request.getDescription();
        // 每个媒体优先保存自身元数据时间，旧客户端未传递时兼容批次级时间。
        asset.capturedAt = resource.getCapturedAt() == null
                ? request.getCapturedAt() : resource.getCapturedAt();
        asset.status = 1;
        return asset;
    }

    /** 创建尚未确认对象已上传完成的直传资源。 */
    @JsonIgnore
    public static AlbumAsset createPendingDirect(
            Long uploaderId,
            Long familyId,
            Long albumId,
            String batchId,
            String description,
            AlbumDirectUploadFileRequest resource,
            String url,
            String thumbnailUrl
    ) {
        AlbumAsset asset = new AlbumAsset();
        asset.uploaderId = uploaderId;
        asset.familyId = familyId;
        asset.albumId = albumId;
        asset.uploadBatchId = batchId;
        asset.mediaType = resource.getMediaType();
        asset.url = url;
        asset.thumbnailUrl = thumbnailUrl;
        asset.originalName = resource.getOriginalName();
        asset.mimeType = resource.getMimeType();
        asset.fileSize = resource.getFileSize();
        asset.duration = resource.getDuration();
        asset.width = resource.getWidth();
        asset.height = resource.getHeight();
        asset.description = description;
        asset.status = 0;
        return asset;
    }

    /** COS 对象确认存在后立即公开，拍摄时间暂时使用上传时间。 */
    public void publishVerifiedDirectUpload(long verifiedSize, String verifiedMimeType) {
        this.fileSize = verifiedSize;
        this.mimeType = verifiedMimeType;
        this.capturedAt = getCreateTime() == null ? LocalDateTime.now() : getCreateTime();
        this.status = 1;
    }

    /** 异步处理完成后补充经过校验的媒体元数据。 */
    public void completeDirectUploadMetadata(long verifiedSize, String verifiedMimeType, LocalDateTime capturedAt) {
        this.fileSize = verifiedSize;
        this.mimeType = verifiedMimeType;
        this.capturedAt = capturedAt;
    }

    /** 封面直传失败时保留原视频并移除无效封面地址。 */
    public void removeThumbnail() {
        this.thumbnailUrl = null;
    }
    /**
     * 创建相册资源并统一应用资源元数据规则。
     */
    @JsonIgnore
    public static AlbumAsset create(AlbumAssetSaveRequest request) {
        AlbumAsset asset = new AlbumAsset();
        asset.apply(request);
        return asset;
    }

    /**
     * 更新允许后台维护的资源元数据。
     */
    public void updateMetadata(AlbumAssetSaveRequest request) {
        apply(request);
    }

    /**
     * 调整资源所属分组，空值表示移出分组。
     */
    public void changeGroup(Long groupId) {
        this.groupId = groupId;
    }

    /**
     * 标记资源所属的客户端批量发布批次。
     */
    public void assignUploadBatch(String uploadBatchId) {
        this.uploadBatchId = uploadBatchId;
    }


    private void apply(AlbumAssetSaveRequest request) {
        this.uploaderId = request.getUploaderId();
        this.familyId = request.getFamilyId();
        this.albumId = request.getAlbumId();
        this.mediaType = request.getMediaType();
        this.url = request.getUrl();
        this.thumbnailUrl = request.getThumbnailUrl();
        this.originalName = request.getOriginalName();
        this.mimeType = request.getMimeType();
        this.fileSize = request.getFileSize();
        this.duration = request.getDuration();
        this.width = request.getWidth();
        this.height = request.getHeight();
        this.groupId = request.getGroupId();
        this.description = request.getDescription();
        this.capturedAt = request.getCapturedAt();
        this.status = request.getStatus() == null ? 1 : request.getStatus();
    }
}
