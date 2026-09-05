package com.youlai.boot.recipe.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.youlai.boot.common.base.IBaseEnum;
import lombok.Getter;

@Getter
public enum BabyRecipeRuleSeverityEnum implements IBaseEnum<String> {

    BLOCK("BLOCK", "拦截"),
    REVIEW("REVIEW", "人工审核"),
    WARN("WARN", "提示");

    @EnumValue
    private final String value;
    private final String label;

    BabyRecipeRuleSeverityEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }
}
