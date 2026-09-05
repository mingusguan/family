package com.youlai.boot.recipe.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.youlai.boot.common.base.IBaseEnum;
import lombok.Getter;

@Getter
public enum BabyRecipeTextureTypeEnum implements IBaseEnum<String> {

    PUREE("PUREE", "泥糊"),
    MINCED("MINCED", "碎末"),
    SOFT_CHUNK("SOFT_CHUNK", "软烂小块"),
    FAMILY_SOFT("FAMILY_SOFT", "清淡家庭餐");

    @EnumValue
    private final String value;
    private final String label;

    BabyRecipeTextureTypeEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }
}
