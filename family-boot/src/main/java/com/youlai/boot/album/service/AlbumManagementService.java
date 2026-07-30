package com.youlai.boot.album.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.youlai.boot.album.model.AlbumModels.AlbumAssetQuery;
import com.youlai.boot.album.model.AlbumModels.AlbumAssetSaveRequest;
import com.youlai.boot.album.model.AlbumModels.AlbumAssetVO;
import com.youlai.boot.album.model.AlbumModels.AlbumMomentCreateRequest;
import com.youlai.boot.album.model.AlbumModels.AlbumMomentBatchVO;
import com.youlai.boot.album.model.AlbumModels.AlbumMomentDetailVO;
import com.youlai.boot.album.model.AlbumModels.AlbumMomentQuery;
import com.youlai.boot.album.model.AlbumModels.AlbumGroupSaveRequest;
import com.youlai.boot.album.model.AlbumModels.AlbumGroupVO;

import java.util.List;

/**
 * 相册后台管理服务。
 */
public interface AlbumManagementService {

    IPage<AlbumAssetVO> getAssetPage(AlbumAssetQuery query);

    IPage<AlbumAssetVO> getMomentPage(AlbumMomentQuery query);

    IPage<AlbumMomentBatchVO> getMomentBatchPage(AlbumMomentQuery query);

    AlbumMomentDetailVO getMomentDetail(String batchId, Long familyId, Long albumId);

    boolean saveMoment(AlbumMomentCreateRequest request);

    boolean deleteOwnMoment(Long id);

    AlbumAssetSaveRequest getAssetForm(Long id);

    boolean saveAsset(AlbumAssetSaveRequest request);

    boolean updateAsset(Long id, AlbumAssetSaveRequest request);

    boolean deleteAssets(String ids);

    boolean changeAssetGroup(String ids, Long groupId);

    List<AlbumGroupVO> listGroups(String keyword);

    AlbumGroupSaveRequest getGroupForm(Long id);

    boolean saveGroup(AlbumGroupSaveRequest request);

    boolean updateGroup(Long id, AlbumGroupSaveRequest request);

    boolean deleteGroups(String ids);
}
