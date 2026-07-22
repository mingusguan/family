package com.youlai.boot.family.model.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.youlai.boot.common.base.BaseEntity;
import com.youlai.boot.family.model.FamilyModels.FamilyAlbumCreateRequest;
import lombok.Getter;
import lombok.Setter;

/** 家庭内由成员创建的相册。 */
@Getter
@Setter
@TableName("app_family_album")
public class FamilyAlbum extends BaseEntity {
    private Long familyId;
    private String name;
    private String description;
    private String coverUrl;
    private Long creatorId;

    @TableLogic
    private Integer isDeleted;

    /** 在指定家庭下创建相册。 */
    @JsonIgnore
    public static FamilyAlbum create(Long familyId, Long creatorId, FamilyAlbumCreateRequest request) {
        FamilyAlbum album = new FamilyAlbum();
        album.familyId = familyId;
        album.creatorId = creatorId;
        album.name = request.getName().trim();
        album.description = request.getDescription();
        album.isDeleted = 0;
        return album;
    }
}
