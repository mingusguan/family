package com.youlai.boot.family.model.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.youlai.boot.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/** 家庭成员关系。 */
@Getter
@Setter
@TableName("app_family_member")
public class FamilyMember extends BaseEntity {
    public static final String ROLE_OWNER = "OWNER";
    public static final String ROLE_MEMBER = "MEMBER";

    private Long familyId;
    private Long userId;
    private String role;

    @TableLogic
    private Integer isDeleted;

    /** 将受邀用户登记为普通家庭成员。 */
    @JsonIgnore
    public static FamilyMember member(Long familyId, Long userId) {
        FamilyMember member = new FamilyMember();
        member.familyId = familyId;
        member.userId = userId;
        member.role = ROLE_MEMBER;
        member.isDeleted = 0;
        return member;
    }
    /** 将家庭创建者登记为家庭拥有者。 */
    @JsonIgnore
    public static FamilyMember owner(Long familyId, Long userId) {
        FamilyMember member = new FamilyMember();
        member.familyId = familyId;
        member.userId = userId;
        member.role = ROLE_OWNER;
        member.isDeleted = 0;
        return member;
    }
}
