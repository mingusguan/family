package com.youlai.boot.recipe.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youlai.boot.recipe.model.entity.RecipeFavorite;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RecipeFavoriteMapper extends BaseMapper<RecipeFavorite> {
}
