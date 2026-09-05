package com.youlai.boot.recipe.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RecipeAudienceTypeEnum {
    GENERAL("普通家庭"),
    INFANT("婴幼儿");

    private final String label;
}
