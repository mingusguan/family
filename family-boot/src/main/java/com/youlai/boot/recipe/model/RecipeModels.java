package com.youlai.boot.recipe.model;

import com.youlai.boot.common.base.BaseQuery;
import com.youlai.boot.recipe.enums.BabyRecipeMealTypeEnum;
import com.youlai.boot.recipe.enums.BabyRecipeRuleSeverityEnum;
import com.youlai.boot.recipe.enums.BabyRecipeStatusEnum;
import com.youlai.boot.recipe.enums.BabyRecipeTextureTypeEnum;
import com.youlai.boot.recipe.enums.RecipeAudienceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

public final class RecipeModels {

    private RecipeModels() {
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "宝宝食谱分页查询")
    public static class BabyRecipeQuery extends BaseQuery {
        @Schema(description = "关键词")
        private String keyword;
        @Schema(description = "宝宝月龄")
        @Min(value = 0, message = "月龄不能小于0")
        @Max(value = 36, message = "第一期仅支持0-36月龄")
        private Integer monthAge;
        @Schema(description = "食物性状")
        private BabyRecipeTextureTypeEnum textureType;
        @Schema(description = "餐次")
        private BabyRecipeMealTypeEnum mealType;
        @Schema(description = "发布状态")
        private BabyRecipeStatusEnum status;
        private RecipeAudienceTypeEnum audienceType;
        @Schema(description = "分类名称")
        private String categoryName;
        @Schema(description = "只看当前用户收藏")
        private Boolean favoriteOnly;
        @Schema(description = "只看当前用户做过")
        private Boolean cookedOnly;
    }

    @Data
    @Schema(description = "极速数据手动同步参数")
    public static class JumdataRecipeSyncRequest {
        @Valid
        @Size(max = 100, message = "一次最多选择100个分类")
        @Schema(description = "Selected Jisu categories; preferred over keywords when present")
        private List<JumdataCategorySyncItem> categories;
        @Schema(description = "同步关键词；为空时使用系统默认的婴幼儿/宝宝食谱关键词")
        private List<String> keywords;
        @Min(value = 1, message = "每个关键词同步数量不能小于1")
        @Max(value = 50, message = "第一期每个关键词最多同步50条")
        private Integer limitPerKeyword = 20;
    }

    @Data
    @Schema(description = "Jisu category synchronization item")
    public static class JumdataCategorySyncItem {
        @NotBlank(message = "categoryId must not be blank")
        private String categoryId;
        @NotBlank(message = "categoryName must not be blank")
        private String categoryName;
        @Schema(description = "同步目标人群；为空时按分类名称智能判断")
        private RecipeAudienceTypeEnum audienceType;
        @Size(max = 128, message = "keyword must not exceed 128 characters")
        private String keyword;
        @NotNull(message = "limit must not be null")
        @Min(value = 1, message = "limit must be at least 1")
        @Max(value = 10000, message = "limit must not exceed 10000")
        private Integer limit = 20;
    }

    @Data
    @Schema(description = "Jisu recipe category")
    public static class JumdataRecipeCategoryVO {
        private String id;
        private String name;
        private String parentId;
        private String queryValue;
        private List<JumdataRecipeCategoryVO> children;
    }
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class JumdataRecipePreviewQuery extends BaseQuery {
        @NotBlank(message = "categoryId must not be blank")
        private String categoryId;
        @NotBlank(message = "categoryName must not be blank")
        private String categoryName;
        @Size(max = 128, message = "keyword must not exceed 128 characters")
        private String keyword;

        @Min(value = 1, message = "页码不能小于1")
        @Override
        public Integer getPageNum() { return super.getPageNum(); }

        @Min(value = 1, message = "每页数量不能小于1")
        @Max(value = 20, message = "每页最多20条")
        @Override
        public Integer getPageSize() { return super.getPageSize(); }
    }

    @Data
    public static class JumdataRecipePreviewVO {
        private String sourceRecipeId;
        private String title;
        private String summary;
        private String coverUrl;
    }

    @Data
    public static class JumdataRecipePreviewPageVO {
        private List<JumdataRecipePreviewVO> list;
        private Long remoteTotal;
        private Integer pageNum;
        private Integer pageSize;
        private Integer totalPages;
        private Integer excludedLocalCount;
        private Long availableTotalEstimate;
    }

    @Data
    public static class JumdataSelectedRecipeSyncRequest {
        @NotBlank(message = "categoryId must not be blank")
        private String categoryId;
        @NotBlank(message = "categoryName must not be blank")
        private String categoryName;
        private RecipeAudienceTypeEnum audienceType;
        @NotEmpty(message = "sourceRecipeIds must not be empty")
        @Size(max = 10000, message = "at most 10000 recipes can be selected")
        private List<@NotBlank String> sourceRecipeIds;
    }

    @Data
    public static class RecipeSyncTaskVO {
        private String taskNo;
        private String mode;
        private String status;
        private Integer totalCount;
        private Integer requestedCount;
        private Integer processedCount;
        private Integer importedCount;
        private Integer duplicatedCount;
        private Integer blockedCount;
        private Integer warnedCount;
        private Integer failedCount;
        private String errorMessage;
        private List<RecipeSyncFailureVO> failures;
        private LocalDateTime startedAt;
        private LocalDateTime finishedAt;
    }

    @Data
    public static class RecipeSyncFailureVO {
        private String sourceRecipeId;
        private String message;
    }


    @Data
    @Schema(description = "宝宝食谱保存参数")
    public static class BabyRecipeSaveRequest {
        @Schema(description = "来源记录ID")
        private Long sourceId;
        @Size(max = 128, message = "分类ID不能超过128个字符")
        private String categoryId;
        @Size(max = 255, message = "分类名称不能超过255个字符")
        private String categoryName;
        @NotBlank(message = "食谱标题不能为空")
        @Size(max = 255, message = "食谱标题不能超过255个字符")
        private String title;
        @Size(max = 5000, message = "摘要不能超过5000个字符")
        private String summary;
        @Size(max = 2048, message = "封面地址不能超过2048个字符")
        private String coverUrl;
        @NotNull(message = "适用人群不能为空")
        private RecipeAudienceTypeEnum audienceType = RecipeAudienceTypeEnum.INFANT;
        @Min(value = 0, message = "最小月龄不能小于0")
        @Max(value = 36, message = "第一期仅支持0-36月龄")
        private Integer minMonthAge;
        @Min(value = 0, message = "最大月龄不能小于0")
        @Max(value = 36, message = "第一期仅支持0-36月龄")
        private Integer maxMonthAge;
        private BabyRecipeTextureTypeEnum textureType;
        private BabyRecipeMealTypeEnum mealType;
        @Size(max = 32, message = "难度不能超过32个字符")
        private String difficulty;
        @Min(value = 0, message = "烹饪时间不能小于0")
        private Integer cookTimeMinutes;
        @Size(max = 64, message = "份量不能超过64个字符")
        private String servingSize;
        @Valid
        @NotEmpty(message = "食材不能为空")
        private List<RecipeIngredientRequest> ingredients;
        @Valid
        @NotEmpty(message = "步骤不能为空")
        private List<RecipeStepRequest> steps;
        @Size(max = 500, message = "过敏原标签不能超过500个字符")
        private String allergenTags;
        @Size(max = 500, message = "风险标签不能超过500个字符")
        private String riskTags;
    }

    @Data
    @Schema(description = "食材")
    public static class RecipeIngredientRequest {
        @NotBlank(message = "食材名称不能为空")
        @Size(max = 255, message = "食材名称不能超过255个字符")
        private String name;
        @Size(max = 255, message = "用量不能超过255个字符")
        private String amount;
        @Size(max = 255, message = "备注不能超过255个字符")
        private String note;
    }

    @Data
    @Schema(description = "制作步骤")
    public static class RecipeStepRequest {
        @NotBlank(message = "步骤内容不能为空")
        @Size(max = 5000, message = "步骤内容不能超过5000个字符")
        private String content;
        @Size(max = 2048, message = "步骤图片地址不能超过2048个字符")
        private String imageUrl;
    }

    @Data
    @Schema(description = "规则命中结果")
    public static class RecipeRuleHitVO {
        private String ruleCode;
        private BabyRecipeRuleSeverityEnum severity;
        private String severityLabel;
        private String matchedText;
        private String suggestion;
        private Integer ruleVersion;
        private String evidenceSource;
        private String evidenceUrl;
    }

    @Data
    @Schema(description = "宝宝食谱列表项")
    public static class BabyRecipeVO {
        private Long id;
        private String title;
        private String summary;
        private String coverUrl;
        private RecipeAudienceTypeEnum audienceType;
        private String audienceTypeLabel;
        private Integer minMonthAge;
        private Integer maxMonthAge;
        private BabyRecipeTextureTypeEnum textureType;
        private String textureTypeLabel;
        private BabyRecipeMealTypeEnum mealType;
        private String mealTypeLabel;
        private String difficulty;
        private Integer cookTimeMinutes;
        private String servingSize;
        private String allergenTags;
        private String riskTags;
        private Long sourceId;
        private String categoryId;
        private String categoryName;
        private String sourceProvider;
        private String sourceRecipeId;
        private BabyRecipeStatusEnum status;
        private String statusLabel;
        private BabyRecipeRuleSeverityEnum riskLevel;
        private String riskLevelLabel;
        private Integer ruleHitCount;
        private Boolean favorite;
        private Boolean cooked;
        private LocalDateTime createTime;
        private LocalDateTime publishedAt;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "宝宝食谱详情")
    public static class BabyRecipeDetailVO extends BabyRecipeVO {
        private List<RecipeIngredientRequest> ingredients;
        private List<RecipeStepRequest> steps;
        private List<RecipeRuleHitVO> ruleHits;
    }

    @Data
    @Schema(description = "自动筛查结果")
    public static class RecipeScreenResultVO {
        private Long recipeId;
        private BabyRecipeStatusEnum status;
        private List<RecipeRuleHitVO> hits;
    }

    @Data
    @Schema(description = "极速数据同步结果")
    public static class RecipeSyncResultVO {
        private Integer fetchedCount;
        private Integer importedCount;
        private Integer duplicatedCount;
        private Integer blockedCount;
        private Integer warnedCount;
        private List<BabyRecipeVO> importedRecipes;
    }

    @Data
    @Schema(description = "婴幼儿筛查规则保存参数")
    public static class RecipeRuleSaveRequest {
        @NotBlank @Size(max = 64) private String ruleCode;
        @NotBlank @Size(max = 128) private String ruleName;
        @NotNull @Min(0) @Max(36) private Integer minMonthAge;
        @NotNull @Min(0) @Max(36) private Integer maxMonthAge;
        @NotBlank private String matchType;
        @NotBlank @Size(max = 1000) private String keywords;
        @NotNull private BabyRecipeRuleSeverityEnum severity;
        @Size(max = 500) private String suggestion;
        @NotBlank @Size(max = 255) private String evidenceSource;
        @Size(max = 1000) private String evidenceUrl;
        private LocalDate effectiveDate;
        @NotNull private Integer enabled = 1;
    }

    @Data
    public static class RecipeRuleVO extends RecipeRuleSaveRequest {
        private Long id;
        private Integer ruleVersion;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
    }
}
