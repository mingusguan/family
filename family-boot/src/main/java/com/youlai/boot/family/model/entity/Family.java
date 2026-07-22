package com.youlai.boot.family.model.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.youlai.boot.common.base.BaseEntity;
import com.youlai.boot.family.model.FamilyModels.FamilyCreateRequest;
import lombok.Getter;
import lombok.Setter;

/** 用户创建的家庭。 */
@Getter
@Setter
@TableName("app_family")
public class Family extends BaseEntity {
    private String name;
    private String description;
    private String coverUrl;
    private Long creatorId;

    @TableLogic
    private Integer isDeleted;

    /** 创建家庭并确立创建者。 */
    @JsonIgnore
    public static Family create(Long creatorId, FamilyCreateRequest request) {
        Family family = new Family();
        family.name = request.getName().trim();
        family.description = request.getDescription();
        family.creatorId = creatorId;
        family.isDeleted = 0;
        return family;
    }
}
