package com.youlai.boot.album.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youlai.boot.album.model.entity.AlbumAssetTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 相册资源标签关系 Mapper。
 */
@Mapper
public interface AlbumAssetTagMapper extends BaseMapper<AlbumAssetTag> {

    /**
     * 批量新增资源标签关系。
     *
     * @param relations 资源标签关系
     * @return 新增数量
     */
    int insertBatch(@Param("relations") List<AlbumAssetTag> relations);
}
