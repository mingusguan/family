package com.youlai.boot.album.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.youlai.boot.album.model.AlbumModels.AlbumTagSaveRequest;
import com.youlai.boot.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * APP 相册标签实体。
 */
@Getter
@Setter
@TableName("app_album_tag")
public class AlbumTag extends BaseEntity {

    private String name;
    private String color;
    private Integer sort;

    @JsonIgnore
    public static AlbumTag create(AlbumTagSaveRequest request) {
        AlbumTag tag = new AlbumTag();
        tag.updateMetadata(request);
        return tag;
    }

    public void updateMetadata(AlbumTagSaveRequest request) {
        this.name = request.getName();
        this.color = request.getColor();
        this.sort = request.getSort() == null ? 0 : request.getSort();
    }
}
