package com.youlai.boot.album.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.youlai.boot.album.model.AlbumModels.AlbumGroupSaveRequest;
import com.youlai.boot.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * APP 相册分组实体。
 */
@Getter
@Setter
@TableName("app_album_group")
public class AlbumGroup extends BaseEntity {

    private String name;
    private String description;
    private Integer sort;

    @JsonIgnore
    public static AlbumGroup create(AlbumGroupSaveRequest request) {
        AlbumGroup group = new AlbumGroup();
        group.updateMetadata(request);
        return group;
    }

    public void updateMetadata(AlbumGroupSaveRequest request) {
        this.name = request.getName();
        this.description = request.getDescription();
        this.sort = request.getSort() == null ? 0 : request.getSort();
    }
}
