package com.youlai.boot.recipe.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.youlai.boot.common.base.IBaseEnum;
import lombok.Getter;

@Getter
public enum BabyRecipeMealTypeEnum implements IBaseEnum<String> {

    BREAKFAST("BREAKFAST", "早餐"),
    LUNCH("LUNCH", "午餐"),
    DINNER("DINNER", "晚餐"),
    SNACK("SNACK", "加餐");

    @EnumValue
    private final String value;
    private final String label;

    BabyRecipeMealTypeEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }
}
