package com.youlai.boot.album.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.youlai.boot.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/** 小程序相册直传批次。 */
@Getter
@Setter
@TableName("app_album_upload_batch")
public class AlbumUploadBatch extends BaseEntity {
    private String batchId;
    private Long uploaderId;
    private Long familyId;
    private Long albumId;
    private Integer totalCount;
    private Integer processingCount;
    private Integer successCount;
    private Integer failedCount;
    private String status;
}
