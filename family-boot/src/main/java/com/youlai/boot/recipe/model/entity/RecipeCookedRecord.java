package com.youlai.boot.recipe.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.youlai.boot.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("recipe_cooked_record")
public class RecipeCookedRecord extends BaseEntity {

    private Long userId;
    private Long recipeId;
    private LocalDateTime cookedAt;
}
