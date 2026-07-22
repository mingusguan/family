package com.youlai.boot.album.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 相册资源与标签关联实体。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("app_album_asset_tag")
public class AlbumAssetTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long assetId;

    private Long tagId;

    public AlbumAssetTag(Long assetId, Long tagId) {
        this.assetId = assetId;
        this.tagId = tagId;
    }
}
