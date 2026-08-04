package com.youlai.boot.album.service;

import com.youlai.boot.album.model.AlbumModels.AlbumDirectUploadConfirmRequest;
import com.youlai.boot.album.model.AlbumModels.AlbumDirectUploadInitRequest;
import com.youlai.boot.album.model.AlbumModels.AlbumDirectUploadInitVO;
import com.youlai.boot.album.model.AlbumModels.AlbumDirectUploadStatusVO;

/** 小程序相册直传用例服务。 */
public interface AlbumDirectUploadService {
    AlbumDirectUploadInitVO initialize(AlbumDirectUploadInitRequest request);
    AlbumDirectUploadStatusVO confirm(AlbumDirectUploadConfirmRequest request);
    AlbumDirectUploadStatusVO getStatus(String batchId);
}
