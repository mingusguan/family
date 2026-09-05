package com.youlai.boot.recipe.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "recipe.jisu")
public class JumdataRecipeProperties {

    /**
     * 极速数据分配的 appkey。
     */
    private String appKey;

    /**
     * 菜谱搜索接口地址。
     */
    private String searchUrl = "https://api.jisuapi.com/recipe/search";

    /**
     * 按分类检索接口地址。
     */
    private String byClassUrl = "https://api.jisuapi.com/recipe/byclass";

    /**
     * 菜谱分类接口地址。
     */
    private String categoryUrl = "https://api.jisuapi.com/recipe/class";

    /**
     * 菜谱详情接口地址。
     */
    private String detailUrl = "https://api.jisuapi.com/recipe/detail";

    /**
     * 分类参数名。
     */
    private String categoryParamName = "classid";

    /**
     * 菜名/关键词查询参数名。
     */
    private String keywordParamName = "keyword";

    /**
     * 详情查询中的菜谱ID参数名。
     */
    private String idParamName = "id";

    /**
     * 页码参数名。
     */
    private String pageParamName = "start";

    /**
     * 每页数量参数名。
     */
    private String pageSizeParamName = "num";

    /**
     * 极速数据菜谱查询接口允许的最大 num。
     */
    private Integer maxPageSize = 20;

    /**
     * 一期默认只同步婴幼儿相关关键词。
     */
    private List<String> defaultKeywords = new ArrayList<>(List.of("婴幼儿", "宝宝食谱", "儿童"));
}
