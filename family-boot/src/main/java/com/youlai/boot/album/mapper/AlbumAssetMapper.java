package com.youlai.boot.album.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.album.model.AlbumModels.AlbumMomentBatchRow;
import com.youlai.boot.album.model.AlbumModels.AlbumMomentQuery;
import com.youlai.boot.album.model.entity.AlbumAsset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 相册资源 Mapper。
 */
@Mapper
public interface AlbumAssetMapper extends BaseMapper<AlbumAsset> {

    Page<AlbumMomentBatchRow> selectMomentBatchPage(
            Page<AlbumMomentBatchRow> page,
            @Param("query") AlbumMomentQuery query,
            @Param("taggedAssetIds") List<Long> taggedAssetIds,
            @Param("currentUserId") Long currentUserId
    );


    int updateGroupBatch(@Param("assetIds") List<Long> assetIds, @Param("groupId") Long groupId);
}
