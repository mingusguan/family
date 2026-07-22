package com.youlai.boot.album.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youlai.boot.album.model.entity.AlbumGroup;
import org.apache.ibatis.annotations.Mapper;

/**
 * 相册分组 Mapper。
 */
@Mapper
public interface AlbumGroupMapper extends BaseMapper<AlbumGroup> {
}
