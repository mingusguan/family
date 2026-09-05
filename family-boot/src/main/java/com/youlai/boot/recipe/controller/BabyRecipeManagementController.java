package com.youlai.boot.recipe.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.youlai.boot.common.annotation.RepeatSubmit;
import com.youlai.boot.common.result.PageResult;
import com.youlai.boot.common.result.Result;
import com.youlai.boot.recipe.model.RecipeModels.BabyRecipeDetailVO;
import com.youlai.boot.recipe.model.RecipeModels.BabyRecipeQuery;
import com.youlai.boot.recipe.model.RecipeModels.BabyRecipeSaveRequest;
import com.youlai.boot.recipe.model.RecipeModels.BabyRecipeVO;
import com.youlai.boot.recipe.model.RecipeModels.JumdataRecipePreviewPageVO;
import com.youlai.boot.recipe.model.RecipeModels.JumdataRecipePreviewQuery;
import com.youlai.boot.recipe.model.RecipeModels.JumdataSelectedRecipeSyncRequest;
import com.youlai.boot.recipe.model.RecipeModels.RecipeSyncTaskVO;
import com.youlai.boot.recipe.model.RecipeModels.JumdataRecipeSyncRequest;
import com.youlai.boot.recipe.model.RecipeModels.JumdataRecipeCategoryVO;
import com.youlai.boot.recipe.model.RecipeModels.RecipeScreenResultVO;
import com.youlai.boot.recipe.model.RecipeModels.RecipeRuleSaveRequest;
import com.youlai.boot.recipe.model.RecipeModels.RecipeRuleVO;
import com.youlai.boot.recipe.service.BabyRecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;

@Tag(name = "宝宝食谱管理")
@RestController
@RequestMapping("/api/v1/recipes")
@RequiredArgsConstructor
public class BabyRecipeManagementController {

    private final BabyRecipeService babyRecipeService;

    @Operation(summary = "宝宝食谱管理分页")
    @GetMapping
    @PreAuthorize("@ss.hasPerm('recipe:list')")
    public PageResult<BabyRecipeVO> getPage(@Valid BabyRecipeQuery query) {
        IPage<BabyRecipeVO> page = babyRecipeService.getManagementPage(query);
        return PageResult.success(page);
    }

    @Operation(summary = "宝宝食谱分类列表")
    @GetMapping({"/options/categories", "/categories"})
    @PreAuthorize("@ss.hasPerm('recipe:list')")
    public Result<List<String>> listRecipeCategories() {
        return Result.success(babyRecipeService.listRecipeCategories());
    }

    @Operation(summary = "宝宝食谱管理详情")
    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('recipe:list')")
    public Result<BabyRecipeDetailVO> getDetail(@PathVariable Long id) {
        return Result.success(babyRecipeService.getManagementDetail(id));
    }

    @Operation(summary = "新增宝宝食谱")
    @PostMapping
    @RepeatSubmit
    @PreAuthorize("@ss.hasPerm('recipe:create')")
    public Result<Long> saveRecipe(@Valid @RequestBody BabyRecipeSaveRequest request) {
        return Result.success(babyRecipeService.saveRecipe(request));
    }

    @Operation(summary = "修改宝宝食谱")
    @PutMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('recipe:update')")
    public Result<Void> updateRecipe(@PathVariable Long id, @Valid @RequestBody BabyRecipeSaveRequest request) {
        return Result.judge(babyRecipeService.updateRecipe(id, request));
    }

    @Operation(summary = "自动筛查宝宝食谱")
    @PostMapping("/{id}/screen")
    @PreAuthorize("@ss.hasPerm('recipe:review')")
    public Result<RecipeScreenResultVO> screenRecipe(@PathVariable Long id) {
        return Result.success(babyRecipeService.screenRecipe(id));
    }

    @Operation(summary = "审核通过宝宝食谱")
    @PostMapping("/{id}/approve")
    @PreAuthorize("@ss.hasPerm('recipe:review')")
    public Result<Void> approveRecipe(@PathVariable Long id) {
        return Result.judge(babyRecipeService.approveRecipe(id));
    }

    @Operation(summary = "审核驳回宝宝食谱")
    @PostMapping("/{id}/reject")
    @PreAuthorize("@ss.hasPerm('recipe:review')")
    public Result<Void> rejectRecipe(@PathVariable Long id) {
        return Result.judge(babyRecipeService.rejectRecipe(id));
    }

    @Operation(summary = "发布宝宝食谱")
    @PostMapping("/{id}/publish")
    @PreAuthorize("@ss.hasPerm('recipe:publish')")
    public Result<Void> publishRecipe(@PathVariable Long id) {
        return Result.judge(babyRecipeService.publishRecipe(id));
    }

    @Operation(summary = "下架宝宝食谱")
    @PostMapping("/{id}/offline")
    @PreAuthorize("@ss.hasPerm('recipe:publish')")
    public Result<Void> offlineRecipe(@PathVariable Long id) {
        return Result.judge(babyRecipeService.offlineRecipe(id));
    }
    @Operation(summary = "List Jisu recipe categories")
    @GetMapping({"/jisu/categories", "/jumdata/categories"})
    @PreAuthorize("@ss.hasPerm('recipe:sync')")
    public Result<java.util.List<JumdataRecipeCategoryVO>> listJumdataCategories() {

        return Result.success(babyRecipeService.listJumdataCategories());
    }
    @Operation(summary = "Preview Jisu recipes and exclude recipes already stored locally")
    @GetMapping({"/jisu/preview", "/jumdata/preview"})
    @PreAuthorize("@ss.hasPerm('recipe:sync')")
    public Result<JumdataRecipePreviewPageVO> previewJumdataRecipes(@Valid JumdataRecipePreviewQuery query) {
        return Result.success(babyRecipeService.previewJumdataRecipes(query));
    }

    @Operation(summary = "Create an asynchronous category/count synchronization task")
    @PostMapping({"/jisu/sync-tasks/category", "/jumdata/sync-tasks/category"})
    @PreAuthorize("@ss.hasPerm('recipe:sync')")
    public Result<RecipeSyncTaskVO> startCategorySyncTask(@Valid @RequestBody JumdataRecipeSyncRequest request) {
        return Result.success(babyRecipeService.startCategorySyncTask(request));
    }

    @Operation(summary = "Create an asynchronous selected recipe synchronization task")
    @PostMapping({"/jisu/sync-tasks/selected", "/jumdata/sync-tasks/selected"})
    @PreAuthorize("@ss.hasPerm('recipe:sync')")
    public Result<RecipeSyncTaskVO> startSelectedSyncTask(@Valid @RequestBody JumdataSelectedRecipeSyncRequest request) {
        return Result.success(babyRecipeService.startSelectedSyncTask(request));
    }

    @Operation(summary = "Get recipe synchronization task progress")
    @GetMapping({"/jisu/sync-tasks/{taskNo}", "/jumdata/sync-tasks/{taskNo}"})
    @PreAuthorize("@ss.hasPerm('recipe:sync')")
    public Result<RecipeSyncTaskVO> getSyncTask(@PathVariable String taskNo) {
        return Result.success(babyRecipeService.getSyncTask(taskNo));
    }

    @Operation(summary = "最近同步任务")
    @GetMapping({"/jisu/sync-tasks", "/jumdata/sync-tasks"})
    @PreAuthorize("@ss.hasPerm('recipe:sync')")
    public Result<List<RecipeSyncTaskVO>> listRecentSyncTasks() {
        return Result.success(babyRecipeService.listRecentSyncTasks());
    }

    @GetMapping("/rules")
    @PreAuthorize("@ss.hasPerm('recipe:rule')")
    public Result<List<RecipeRuleVO>> listRules() {
        return Result.success(babyRecipeService.listRules());
    }

    @PostMapping("/rules")
    @PreAuthorize("@ss.hasPerm('recipe:rule')")
    public Result<Long> createRule(@Valid @RequestBody RecipeRuleSaveRequest request) {
        return Result.success(babyRecipeService.createRule(request));
    }

    @PutMapping("/rules/{id}")
    @PreAuthorize("@ss.hasPerm('recipe:rule')")
    public Result<Void> updateRule(@PathVariable Long id, @Valid @RequestBody RecipeRuleSaveRequest request) {
        return Result.judge(babyRecipeService.updateRule(id, request));
    }

    @DeleteMapping("/rules/{id}")
    @PreAuthorize("@ss.hasPerm('recipe:rule')")
    public Result<Void> deleteRule(@PathVariable Long id) {
        return Result.judge(babyRecipeService.deleteRule(id));
    }
}
