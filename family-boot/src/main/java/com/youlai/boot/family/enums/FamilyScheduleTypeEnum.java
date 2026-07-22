package com.youlai.boot.family.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.youlai.boot.common.base.IBaseEnum;
import lombok.Getter;

/** 家庭日程类型。 */
@Getter
public enum FamilyScheduleTypeEnum implements IBaseEnum<String> {
    PLAN("PLAN", "家庭计划"),
    BIRTHDAY("BIRTHDAY", "生日"),
    ANNIVERSARY("ANNIVERSARY", "纪念日"),
    FESTIVAL("FESTIVAL", "节日");

    @EnumValue
    private final String value;
    private final String label;

    FamilyScheduleTypeEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public boolean isSpecialDate() {
        return this != PLAN;
    }
}
