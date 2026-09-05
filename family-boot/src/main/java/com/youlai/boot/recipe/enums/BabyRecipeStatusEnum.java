package com.youlai.boot.recipe.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.youlai.boot.common.base.IBaseEnum;
import lombok.Getter;

@Getter
public enum BabyRecipeStatusEnum implements IBaseEnum<String> {

    DRAFT("DRAFT", "草稿"),
    AUTO_REJECTED("AUTO_REJECTED", "自动拦截"),
    PENDING_REVIEW("PENDING_REVIEW", "待审核"),
    REJECTED("REJECTED", "审核驳回"),
    APPROVED("APPROVED", "审核通过"),
    PUBLISHED("PUBLISHED", "已发布"),
    OFFLINE("OFFLINE", "已下架");

    @EnumValue
    private final String value;
    private final String label;

    BabyRecipeStatusEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }
}
