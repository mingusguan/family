package com.youlai.boot.recipe.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.youlai.boot.common.base.BaseEntity;
import com.youlai.boot.recipe.enums.RecipeSourceProviderEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("recipe_source")
public class RecipeSource extends BaseEntity {

    private RecipeSourceProviderEnum provider;
    private String providerRecipeId;
    private String categoryId;
    private String categoryName;
    private String title;
    private String coverUrl;
    private String rawPayload;
    private String syncStatus;
    private LocalDateTime syncedAt;
}
