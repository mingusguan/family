package com.youlai.boot.family.model.entity;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.youlai.boot.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** 家庭微信分享邀请凭证。 */
@Getter
@Setter
@TableName("app_family_invite")
public class FamilyInvite extends BaseEntity {

    private Long familyId;
    private Long inviterId;
    private String code;
    private LocalDateTime expiresAt;

    @TableLogic
    private Integer isDeleted;

    /** 创建指定有效期的家庭邀请凭证。 */
    @JsonIgnore
    public static FamilyInvite create(Long familyId, Long inviterId, LocalDateTime expiresAt) {
        FamilyInvite invite = new FamilyInvite();
        invite.familyId = familyId;
        invite.inviterId = inviterId;
        invite.code = IdUtil.fastSimpleUUID();
        invite.expiresAt = expiresAt;
        invite.isDeleted = 0;
        return invite;
    }

    /** 判断邀请在指定时间是否仍然有效。 */
    public boolean isValidAt(LocalDateTime time) {
        return expiresAt != null && expiresAt.isAfter(time);
    }
}