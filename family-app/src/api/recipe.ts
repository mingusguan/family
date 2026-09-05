import request from "@/utils/request";

const RECIPE_BASE_URL = "/api/v1/app/recipes";

const RecipeAPI = {
  getRecipePage(query: RecipeQuery) {
    const data = Object.fromEntries(
      Object.entries(query).filter(([, value]) => value !== undefined && value !== null && value !== "")
    ) as unknown as RecipeQuery;
    return request<PageResult<BabyRecipe>>({
      url: RECIPE_BASE_URL,
      method: "GET",
      data,
    });
  },

  getRecipeDetail(id: number) {
    return request<BabyRecipeDetail>({
      url: RECIPE_BASE_URL + "/" + id,
      method: "GET",
    });
  },

  getCategories(audienceType?: RecipeAudienceType) {
    return request<string[]>({
      url: RECIPE_BASE_URL + "/categories",
      method: "GET",
      data: audienceType ? { audienceType } : undefined,
    });
  },

  favorite(id: number) {
    return request<void>({
      url: RECIPE_BASE_URL + "/" + id + "/favorite",
      method: "POST",
    });
  },

  cancelFavorite(id: number) {
    return request<void>({
      url: RECIPE_BASE_URL + "/" + id + "/favorite",
      method: "DELETE",
    });
  },

  markCooked(id: number) {
    return request<void>({
      url: RECIPE_BASE_URL + "/" + id + "/cooked",
      method: "POST",
    });
  },
};

export default RecipeAPI;

export type RecipeTextureType = "PUREE" | "MINCED" | "SOFT_CHUNK" | "FAMILY_SOFT";
export type RecipeMealType = "BREAKFAST" | "LUNCH" | "DINNER" | "SNACK";
export type RecipeAudienceType = "GENERAL" | "INFANT";
export type RecipeRuleSeverity = "BLOCK" | "REVIEW" | "WARN";

export interface RecipeQuery extends PageQuery {
  keyword?: string;
  audienceType?: RecipeAudienceType;
  categoryName?: string;
  textureType?: RecipeTextureType;
  mealType?: RecipeMealType;
  favoriteOnly?: boolean;
  cookedOnly?: boolean;
}

export interface RecipeIngredient {
  name: string;
  amount?: string;
  note?: string;
}

export interface RecipeStep {
  content: string;
  imageUrl?: string;
}

export interface BabyRecipe {
  id: number;
  title: string;
  summary?: string;
  coverUrl?: string;
  audienceType: RecipeAudienceType;
  audienceTypeLabel?: string;
  minMonthAge?: number;
  maxMonthAge?: number;
  textureType?: RecipeTextureType;
  textureTypeLabel?: string;
  mealType?: RecipeMealType;
  mealTypeLabel?: string;
  difficulty?: string;
  cookTimeMinutes?: number;
  servingSize?: string;
  allergenTags?: string;
  riskTags?: string;
  favorite?: boolean;
  cooked?: boolean;
  sourceProvider?: string;
  riskLevel?: RecipeRuleSeverity;
  riskLevelLabel?: string;
  ruleHitCount?: number;
  publishedAt?: string;
}

export interface BabyRecipeDetail extends BabyRecipe {
  ingredients: RecipeIngredient[];
  steps: RecipeStep[];
  ruleHits?: RecipeRuleHit[];
}

export interface RecipeRuleHit {
  ruleCode: string;
  severity: RecipeRuleSeverity;
  severityLabel?: string;
  matchedText?: string;
  suggestion?: string;
  evidenceSource?: string;
  evidenceUrl?: string;
}
