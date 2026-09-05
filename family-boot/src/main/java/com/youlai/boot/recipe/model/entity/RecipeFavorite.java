package com.youlai.boot.recipe.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.youlai.boot.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("recipe_favorite")
public class RecipeFavorite extends BaseEntity {

    private Long userId;
    private Long recipeId;
}
