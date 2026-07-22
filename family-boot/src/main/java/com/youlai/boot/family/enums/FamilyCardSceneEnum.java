package com.youlai.boot.family.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.youlai.boot.common.base.IBaseEnum;
import lombok.Getter;

/** 家庭状态卡片场景。 */
@Getter
public enum FamilyCardSceneEnum implements IBaseEnum<String> {
    CREATE_FAMILY("CREATE_FAMILY", "创建家庭"),
    INVITE_MEMBER("INVITE_MEMBER", "邀请家人"),
    UPCOMING_SCHEDULE("UPCOMING_SCHEDULE", "近期计划"),
    RECENT_PHOTO("RECENT_PHOTO", "新增照片"),
    TODAY_MEMORY("TODAY_MEMORY", "那年今日"),
    SPECIAL_DATE("SPECIAL_DATE", "特殊日期");

    @EnumValue
    private final String value;
    private final String label;

    FamilyCardSceneEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }
}
