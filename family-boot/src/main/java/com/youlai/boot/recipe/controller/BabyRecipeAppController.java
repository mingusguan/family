package com.youlai.boot.recipe.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.youlai.boot.common.annotation.RepeatSubmit;
import com.youlai.boot.common.result.PageResult;
import com.youlai.boot.common.result.Result;
import com.youlai.boot.recipe.model.RecipeModels.BabyRecipeDetailVO;
import com.youlai.boot.recipe.model.RecipeModels.BabyRecipeQuery;
import com.youlai.boot.recipe.model.RecipeModels.BabyRecipeVO;
import com.youlai.boot.recipe.enums.RecipeAudienceTypeEnum;
import com.youlai.boot.recipe.service.BabyRecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "APP宝宝食谱")
@RestController
@RequestMapping("/api/v1/app/recipes")
@RequiredArgsConstructor
public class BabyRecipeAppController {

    private final BabyRecipeService babyRecipeService;

    @Operation(summary = "宝宝食谱分页")
    @GetMapping
    public PageResult<BabyRecipeVO> getPage(@Valid BabyRecipeQuery query) {
        IPage<BabyRecipeVO> page = babyRecipeService.getAppRecipePage(query);
        return PageResult.success(page);
    }

    @Operation(summary = "已发布食谱分类")
    @GetMapping("/categories")
    public Result<List<String>> listCategories(@RequestParam(required = false) RecipeAudienceTypeEnum audienceType) {
        return Result.success(babyRecipeService.listAppRecipeCategories(audienceType));
    }

    @Operation(summary = "宝宝食谱详情")
    @GetMapping("/{id}")
    public Result<BabyRecipeDetailVO> getDetail(@PathVariable Long id) {
        return Result.success(babyRecipeService.getAppRecipeDetail(id));
    }

    @Operation(summary = "收藏宝宝食谱")
    @PostMapping("/{id}/favorite")
    @RepeatSubmit
    @PreAuthorize("isAuthenticated()")
    public Result<Void> favorite(@PathVariable Long id) {
        return Result.judge(babyRecipeService.favorite(id));
    }

    @Operation(summary = "取消收藏宝宝食谱")
    @DeleteMapping("/{id}/favorite")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> cancelFavorite(@PathVariable Long id) {
        return Result.judge(babyRecipeService.cancelFavorite(id));
    }

    @Operation(summary = "标记做过")
    @PostMapping("/{id}/cooked")
    @RepeatSubmit
    @PreAuthorize("isAuthenticated()")
    public Result<Void> markCooked(@PathVariable Long id) {
        return Result.judge(babyRecipeService.markCooked(id));
    }
}
