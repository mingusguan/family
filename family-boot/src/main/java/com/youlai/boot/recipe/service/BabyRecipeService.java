package com.youlai.boot.recipe.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.youlai.boot.recipe.model.RecipeModels.BabyRecipeDetailVO;
import com.youlai.boot.recipe.model.RecipeModels.BabyRecipeQuery;
import com.youlai.boot.recipe.model.RecipeModels.BabyRecipeSaveRequest;
import com.youlai.boot.recipe.model.RecipeModels.BabyRecipeVO;
import com.youlai.boot.recipe.model.RecipeModels.JumdataRecipeSyncRequest;
import com.youlai.boot.recipe.model.RecipeModels.JumdataRecipeCategoryVO;
import com.youlai.boot.recipe.model.RecipeModels.JumdataRecipePreviewPageVO;
import com.youlai.boot.recipe.model.RecipeModels.JumdataRecipePreviewQuery;
import com.youlai.boot.recipe.model.RecipeModels.JumdataSelectedRecipeSyncRequest;
import com.youlai.boot.recipe.model.RecipeModels.RecipeSyncTaskVO;
import com.youlai.boot.recipe.model.RecipeModels.RecipeScreenResultVO;
import com.youlai.boot.recipe.model.RecipeModels.RecipeRuleSaveRequest;
import com.youlai.boot.recipe.model.RecipeModels.RecipeRuleVO;
import com.youlai.boot.recipe.enums.RecipeAudienceTypeEnum;

import java.util.List;

public interface BabyRecipeService {

    IPage<BabyRecipeVO> getAppRecipePage(BabyRecipeQuery query);

    BabyRecipeDetailVO getAppRecipeDetail(Long id);

    List<String> listAppRecipeCategories(RecipeAudienceTypeEnum audienceType);

    boolean favorite(Long id);

    boolean cancelFavorite(Long id);

    boolean markCooked(Long id);

    IPage<BabyRecipeVO> getManagementPage(BabyRecipeQuery query);

    List<String> listRecipeCategories();

    BabyRecipeDetailVO getManagementDetail(Long id);

    Long saveRecipe(BabyRecipeSaveRequest request);

    boolean updateRecipe(Long id, BabyRecipeSaveRequest request);

    RecipeScreenResultVO screenRecipe(Long id);

    boolean approveRecipe(Long id);

    boolean rejectRecipe(Long id);

    boolean publishRecipe(Long id);

    boolean offlineRecipe(Long id);

    List<JumdataRecipeCategoryVO> listJumdataCategories();

    JumdataRecipePreviewPageVO previewJumdataRecipes(JumdataRecipePreviewQuery query);

    RecipeSyncTaskVO startCategorySyncTask(JumdataRecipeSyncRequest request);

    RecipeSyncTaskVO startSelectedSyncTask(JumdataSelectedRecipeSyncRequest request);

    RecipeSyncTaskVO getSyncTask(String taskNo);
    List<RecipeSyncTaskVO> listRecentSyncTasks();
    List<RecipeRuleVO> listRules();
    Long createRule(RecipeRuleSaveRequest request);
    boolean updateRule(Long id, RecipeRuleSaveRequest request);
    boolean deleteRule(Long id);
}
