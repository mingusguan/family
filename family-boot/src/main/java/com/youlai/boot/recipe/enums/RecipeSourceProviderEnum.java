package com.youlai.boot.recipe.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.youlai.boot.common.base.IBaseEnum;
import lombok.Getter;

@Getter
public enum RecipeSourceProviderEnum implements IBaseEnum<String> {

    JUMDATA("JUMDATA", "聚美智数"),
    JISU("JISU", "极速数据"),
    MANUAL("MANUAL", "人工录入");

    @EnumValue
    private final String value;
    private final String label;

    RecipeSourceProviderEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }
}
