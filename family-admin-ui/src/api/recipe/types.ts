import type { BaseQueryParams } from "@/api/common";

export type BabyRecipeStatus =
  | "DRAFT"
  | "AUTO_REJECTED"
  | "PENDING_REVIEW"
  | "REJECTED"
  | "APPROVED"
  | "PUBLISHED"
  | "OFFLINE";

export type BabyRecipeTextureType = "PUREE" | "MINCED" | "SOFT_CHUNK" | "FAMILY_SOFT";
export type BabyRecipeMealType = "BREAKFAST" | "LUNCH" | "DINNER" | "SNACK";
export type RecipeRuleSeverity = "BLOCK" | "REVIEW" | "WARN";
export type RecipeAudienceType = "GENERAL" | "INFANT";

export interface BabyRecipeQuery extends BaseQueryParams {
  keyword?: string;
  monthAge?: number;
  textureType?: BabyRecipeTextureType;
  mealType?: BabyRecipeMealType;
  status?: BabyRecipeStatus;
  audienceType?: RecipeAudienceType;
  categoryName?: string;
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

export interface RecipeRuleHit {
  ruleCode: string;
  severity: RecipeRuleSeverity;
  severityLabel?: string;
  matchedText?: string;
  suggestion?: string;
  ruleVersion?: number;
  evidenceSource?: string;
  evidenceUrl?: string;
}

export interface BabyRecipeItem {
  id: string;
  title: string;
  summary?: string;
  coverUrl?: string;
  audienceType: RecipeAudienceType;
  audienceTypeLabel?: string;
  minMonthAge?: number;
  maxMonthAge?: number;
  textureType?: BabyRecipeTextureType;
  textureTypeLabel?: string;
  mealType?: BabyRecipeMealType;
  mealTypeLabel?: string;
  difficulty?: string;
  cookTimeMinutes?: number;
  servingSize?: string;
  allergenTags?: string;
  riskTags?: string;
  sourceId?: string;
  categoryId?: string;
  categoryName?: string;
  sourceProvider?: string;
  sourceRecipeId?: string;
  status: BabyRecipeStatus;
  statusLabel?: string;
  riskLevel?: RecipeRuleSeverity;
  riskLevelLabel?: string;
  ruleHitCount?: number;
  favorite?: boolean;
  createTime?: string;
  publishedAt?: string;
}

export interface BabyRecipeDetail extends BabyRecipeItem {
  ingredients: RecipeIngredient[];
  steps: RecipeStep[];
  ruleHits: RecipeRuleHit[];
}

export interface BabyRecipeForm {
  sourceId?: string;
  categoryId?: string;
  categoryName?: string;
  title: string;
  summary?: string;
  coverUrl?: string;
  audienceType: RecipeAudienceType;
  minMonthAge?: number;
  maxMonthAge?: number;
  textureType?: BabyRecipeTextureType;
  mealType?: BabyRecipeMealType;
  difficulty?: string;
  cookTimeMinutes?: number;
  servingSize?: string;
  ingredients: RecipeIngredient[];
  steps: RecipeStep[];
  allergenTags?: string;
  riskTags?: string;
}

export interface RecipeScreenResult {
  recipeId: string;
  status: BabyRecipeStatus;
  hits: RecipeRuleHit[];
}

export interface JumdataRecipeCategory {
  id: string;
  name: string;
  parentId?: string;
  queryValue: string;
  children: JumdataRecipeCategory[];
}

export interface JumdataCategorySyncItem {
  categoryId: string;
  categoryName: string;
  audienceType?: RecipeAudienceType;
  keyword?: string;
  limit: number;
}

export interface JumdataRecipeSyncRequest {
  categories?: JumdataCategorySyncItem[];
  keywords?: string[];
  limitPerKeyword?: number;
}

export interface RecipeSyncResult {
  fetchedCount: number;
  importedCount: number;
  duplicatedCount: number;
  blockedCount: number;
  warnedCount: number;
  importedRecipes: BabyRecipeItem[];
}

export interface JumdataRecipePreviewQuery extends BaseQueryParams {
  categoryId: string;
  categoryName: string;
  keyword?: string;
}

export interface JumdataRecipePreviewItem {
  sourceRecipeId: string;
  title: string;
  summary?: string;
  coverUrl?: string;
}

export interface JumdataRecipePreviewPage {
  list: JumdataRecipePreviewItem[];
  remoteTotal: number;
  pageNum: number;
  pageSize: number;
  totalPages: number;
  excludedLocalCount: number;
  availableTotalEstimate?: number;
}

export interface JumdataSelectedRecipeSyncRequest {
  categoryId: string;
  categoryName: string;
  audienceType?: RecipeAudienceType;
  sourceRecipeIds: string[];
}

export type RecipeSyncTaskStatus = "PENDING" | "RUNNING" | "SUCCEEDED" | "PARTIAL_FAILED" | "FAILED";

export interface RecipeSyncTask {
  taskNo: string;
  mode: "CATEGORY_COUNT" | "SELECTED_RECIPES";
  status: RecipeSyncTaskStatus;
  totalCount: number;
  requestedCount: number;
  processedCount: number;
  importedCount: number;
  duplicatedCount: number;
  blockedCount: number;
  warnedCount: number;
  failedCount: number;
  errorMessage?: string;
  failures?: Array<{ sourceRecipeId: string; message: string }>;
  startedAt?: string;
  finishedAt?: string;
}

export interface RecipeRuleForm {
  ruleCode: string;
  ruleName: string;
  minMonthAge: number;
  maxMonthAge: number;
  matchType: "AGE" | "INGREDIENT" | "TEXT";
  keywords: string;
  severity: RecipeRuleSeverity;
  suggestion?: string;
  evidenceSource: string;
  evidenceUrl?: string;
  effectiveDate?: string;
  enabled: number;
}

export interface RecipeRuleItem extends RecipeRuleForm {
  id: string;
  ruleVersion: number;
  createTime?: string;
  updateTime?: string;
}
