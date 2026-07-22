package com.youlai.boot.family.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.youlai.boot.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** 用户查看家庭动态卡片的时间游标。 */
@Getter
@Setter
@TableName("app_family_card_read_state")
public class FamilyCardReadState extends BaseEntity {
    private Long userId;
    private Long familyId;
    private LocalDateTime lastPhotoReadAt;

    /** 创建用户在指定家庭下的首次已读状态。 */
    @JsonIgnore
    public static FamilyCardReadState create(Long userId, Long familyId, LocalDateTime readAt) {
        FamilyCardReadState state = new FamilyCardReadState();
        state.userId = userId;
        state.familyId = familyId;
        state.lastPhotoReadAt = readAt;
        return state;
    }

    public void markPhotoRead(LocalDateTime readAt) {
        this.lastPhotoReadAt = readAt;
    }
}
