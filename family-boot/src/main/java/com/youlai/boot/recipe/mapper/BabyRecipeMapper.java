package com.youlai.boot.recipe.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youlai.boot.recipe.model.entity.BabyRecipe;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BabyRecipeMapper extends BaseMapper<BabyRecipe> {
}
