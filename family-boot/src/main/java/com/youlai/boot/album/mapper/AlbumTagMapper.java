package com.youlai.boot.album.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youlai.boot.album.model.entity.AlbumTag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 相册标签 Mapper。
 */
@Mapper
public interface AlbumTagMapper extends BaseMapper<AlbumTag> {
}
