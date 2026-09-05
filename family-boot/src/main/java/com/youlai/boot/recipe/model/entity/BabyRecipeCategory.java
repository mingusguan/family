package com.youlai.boot.recipe.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.youlai.boot.common.base.BaseEntity;
import com.youlai.boot.recipe.enums.RecipeSourceProviderEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("baby_recipe_category")
public class BabyRecipeCategory extends BaseEntity {

    private Long recipeId;
    private RecipeSourceProviderEnum provider;
    private String categoryId;
    private String categoryName;
}
