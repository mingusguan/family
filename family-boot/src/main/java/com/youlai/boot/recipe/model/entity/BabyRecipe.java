package com.youlai.boot.recipe.model.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.youlai.boot.common.base.BaseEntity;
import com.youlai.boot.recipe.enums.BabyRecipeMealTypeEnum;
import com.youlai.boot.recipe.enums.BabyRecipeStatusEnum;
import com.youlai.boot.recipe.enums.BabyRecipeTextureTypeEnum;
import com.youlai.boot.recipe.enums.RecipeAudienceTypeEnum;
import com.youlai.boot.common.exception.BusinessException;
import com.youlai.boot.recipe.model.RecipeModels.BabyRecipeSaveRequest;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("baby_recipe")
public class BabyRecipe extends BaseEntity {

    private Long sourceId;
    private String categoryId;
    private String categoryName;
    private String title;
    private String summary;
    private String coverUrl;
    private RecipeAudienceTypeEnum audienceType;
    private Integer minMonthAge;
    private Integer maxMonthAge;
    private BabyRecipeTextureTypeEnum textureType;
    private BabyRecipeMealTypeEnum mealType;
    private String difficulty;
    private Integer cookTimeMinutes;
    private String servingSize;
    private String ingredientsJson;
    private String stepsJson;
    private String nutritionJson;
    private String allergenTags;
    private String riskTags;
    private Integer contentVersion;
    private Integer screenedContentVersion;
    private String screeningVersion;
    private LocalDateTime screenedAt;
    private BabyRecipeStatusEnum status;
    private Long reviewerId;
    private LocalDateTime reviewedAt;
    private LocalDateTime publishedAt;

    @TableLogic
    private Integer isDeleted;

    @JsonIgnore
    public static BabyRecipe create(BabyRecipeSaveRequest request, String ingredientsJson, String stepsJson) {
        BabyRecipe recipe = new BabyRecipe();
        recipe.apply(request, ingredientsJson, stepsJson);
        recipe.status = BabyRecipeStatusEnum.DRAFT;
        recipe.contentVersion = 1;
        recipe.isDeleted = 0;
        return recipe;
    }

    public void update(BabyRecipeSaveRequest request, String ingredientsJson, String stepsJson) {
        Long originalSourceId = this.sourceId;
        this.status = BabyRecipeStatusEnum.DRAFT;
        this.publishedAt = null;
        this.reviewerId = null;
        this.reviewedAt = null;
        this.screenedContentVersion = null;
        this.screeningVersion = null;
        this.screenedAt = null;
        this.contentVersion = this.contentVersion == null ? 1 : this.contentVersion + 1;
        apply(request, ingredientsJson, stepsJson);
        this.sourceId = originalSourceId;
    }

    public void markAutoScreened(String ruleSetVersion) {
        this.status = BabyRecipeStatusEnum.APPROVED;
        this.screenedContentVersion = this.contentVersion;
        this.screeningVersion = ruleSetVersion;
        this.screenedAt = LocalDateTime.now();
        this.reviewedAt = LocalDateTime.now();
    }

    public void approve(Long reviewerId) {
        if (!BabyRecipeStatusEnum.PENDING_REVIEW.equals(this.status)) {
            throw new BusinessException("只有待审核食谱可以审核通过");
        }
        if (screenedContentVersion == null || !screenedContentVersion.equals(contentVersion)) {
            throw new BusinessException("食谱内容尚未完成最新规则筛查");
        }
        this.reviewerId = reviewerId;
        this.reviewedAt = LocalDateTime.now();
        this.status = BabyRecipeStatusEnum.APPROVED;
    }

    public void reject(Long reviewerId) {
        if (!BabyRecipeStatusEnum.PENDING_REVIEW.equals(this.status)) {
            throw new BusinessException("只有待审核食谱可以驳回");
        }
        this.reviewerId = reviewerId;
        this.reviewedAt = LocalDateTime.now();
        this.status = BabyRecipeStatusEnum.REJECTED;
        this.publishedAt = null;
    }

    public void publish() {
        if (!BabyRecipeStatusEnum.APPROVED.equals(this.status)) {
            throw new BusinessException("只有审核通过的食谱可以发布");
        }
        this.status = BabyRecipeStatusEnum.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void offline() {
        if (!BabyRecipeStatusEnum.PUBLISHED.equals(this.status)) {
            throw new BusinessException("只有已发布食谱可以下线");
        }
        this.status = BabyRecipeStatusEnum.OFFLINE;
    }

    private void apply(BabyRecipeSaveRequest request, String ingredientsJson, String stepsJson) {
        this.sourceId = request.getSourceId();
        this.categoryId = request.getCategoryId();
        this.categoryName = request.getCategoryName();
        this.title = request.getTitle();
        this.summary = request.getSummary();
        this.coverUrl = request.getCoverUrl();
        this.audienceType = request.getAudienceType();
        this.minMonthAge = request.getMinMonthAge();
        this.maxMonthAge = request.getMaxMonthAge();
        this.textureType = request.getTextureType();
        this.mealType = request.getMealType();
        this.difficulty = request.getDifficulty();
        this.cookTimeMinutes = request.getCookTimeMinutes();
        this.servingSize = request.getServingSize();
        this.ingredientsJson = ingredientsJson;
        this.stepsJson = stepsJson;
        this.allergenTags = request.getAllergenTags();
        this.riskTags = request.getRiskTags();
    }
}
