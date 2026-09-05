<template>
  <view class="recipe-detail">
    <image v-if="recipe?.coverUrl" class="detail-cover" :src="recipe.coverUrl" mode="aspectFill" />
    <view v-else class="detail-cover detail-cover--empty">
      <wd-icon name="menu" size="60rpx" color="#ffffff" />
    </view>

    <view v-if="recipe" class="detail-body">
      <view class="title-row">
        <text class="detail-title">{{ recipe.title }}</text>
        <view class="icon-button" @click="toggleFavorite">
          <wd-icon :name="favoriteIcon" size="22" color="#e3a127" />
        </view>
      </view>

      <view class="meta-row">
        <text
          v-for="tag in displayTags"
          :key="tag.text"
          :class="'meta-tag--' + tag.type"
        >
          {{ tag.text }}
        </text>
      </view>

      <text v-if="displaySummary" class="summary">{{ displaySummary }}</text>

      <view class="safety-card">
        <text class="section-title">安全提示</text>
        <text class="safety-text">首次添加新食材请少量尝试并观察。存在过敏史或特殊喂养需求时，以医生或营养师建议为准。</text>
        <text v-if="recipe.allergenTags" class="tag-line">过敏原：{{ recipe.allergenTags }}</text>
        <text v-if="recipe.riskTags" class="tag-line">注意项：{{ recipe.riskTags }}</text>
        <view v-if="recipe.ruleHits?.length" class="rule-hit-list">
          <view
            v-for="hit in recipe.ruleHits"
            :key="hit.ruleCode + hit.matchedText"
            class="rule-hit"
            :class="{ 'rule-hit--block': hit.severity === 'BLOCK' }"
          >
            <text class="rule-hit__level">{{ hit.severity === "BLOCK" ? "BLOCK" : hit.severity === "REVIEW" ? "REVIEW" : "WARN" }}</text>
            <view class="rule-hit__body">
              <text class="rule-hit__title">{{ hit.matchedText ? "命中：" + hit.matchedText : hit.ruleCode }}</text>
              <text v-if="hit.suggestion" class="rule-hit__desc">{{ hit.suggestion }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="section">
        <text class="section-title">食材</text>
        <view v-for="(item, index) in recipe.ingredients" :key="index" class="ingredient-row">
          <text>{{ item.name }}</text>
          <text>{{ item.amount || item.note || "适量" }}</text>
        </view>
      </view>

      <view class="section">
        <text class="section-title">做法</text>
        <view v-for="(step, index) in recipe.steps" :key="index" class="step-row">
          <text class="step-index">{{ index + 1 }}</text>
          <view class="step-body">
            <text class="step-text">{{ cleanText(step.content) }}</text>
            <image
              v-if="step.imageUrl"
              class="step-image"
              :src="step.imageUrl"
              mode="aspectFill"
              lazy-load
              @click="previewStepImage(step.imageUrl)"
            />
          </view>
        </view>
      </view>

      <wd-button block type="primary" :disabled="recipe.cooked" @click="markCooked">
        {{ recipe.cooked ? "已记录做过" : "标记做过" }}
      </wd-button>
    </view>

    <view v-else class="loading-state">
      <wd-loading color="#69a66d" />
      <text>正在加载食谱...</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onLoad } from "@dcloudio/uni-app";
import { computed, ref } from "vue";
import RecipeAPI, { BabyRecipe, BabyRecipeDetail } from "@/api/recipe";

type RecipeTagType = "category" | "risk-block" | "risk-warn" | "meta";

interface RecipeTag {
  text: string;
  type: RecipeTagType;
}

const recipe = ref<BabyRecipeDetail | null>(null);
const favoriteIcon = computed(() => (recipe.value?.favorite ? "star-on" : "star"));
const displaySummary = computed(() => cleanText(recipe.value?.summary));
const displayTags = computed(() => recipe.value ? recipeTags(recipe.value) : []);
let recipeId = 0;

async function loadDetail() {
  if (!recipeId) return;
  try {
    recipe.value = await RecipeAPI.getRecipeDetail(recipeId);
  } catch (error: any) {
    uni.showToast({ title: error?.message || "食谱加载失败", icon: "none" });
  }
}

async function toggleFavorite() {
  if (!recipe.value) return;
  try {
    if (recipe.value.favorite) {
      await RecipeAPI.cancelFavorite(recipe.value.id);
      recipe.value.favorite = false;
    } else {
      await RecipeAPI.favorite(recipe.value.id);
      recipe.value.favorite = true;
    }
  } catch (error: any) {
    uni.showToast({ title: error?.message || "操作失败", icon: "none" });
  }
}

async function markCooked() {
  if (!recipe.value || recipe.value.cooked) return;
  try {
    await RecipeAPI.markCooked(recipe.value.id);
    recipe.value.cooked = true;
    uni.showToast({ title: "已记录", icon: "success" });
  } catch (error: any) {
    uni.showToast({ title: error?.message || "操作失败", icon: "none" });
  }
}

function previewStepImage(imageUrl: string) {
  if (!recipe.value) return;
  const urls = recipe.value.steps.map((step) => step.imageUrl).filter(Boolean) as string[];
  uni.previewImage({ current: imageUrl, urls });
}

function recipeTags(recipe: BabyRecipe): RecipeTag[] {
  const tags: RecipeTag[] = [];
  categoryTags(recipe.categoryName).forEach((text) => tags.push({ text, type: "category" }));
  if (recipe.riskLevel) {
    tags.push({
      text: riskTagText(recipe),
      type: recipe.riskLevel === "BLOCK" ? "risk-block" : "risk-warn",
    });
  }
  if (tags.length === 0 && recipe.audienceTypeLabel) {
    tags.push({ text: "适用：" + recipe.audienceTypeLabel, type: "meta" });
  }
  if (recipe.mealTypeLabel) {
    tags.push({ text: "餐次：" + recipe.mealTypeLabel, type: "meta" });
  }
  if (shouldShowTexture(recipe)) {
    tags.push({ text: "性状：" + recipe.textureTypeLabel, type: "meta" });
  }
  if (recipe.cookTimeMinutes) {
    tags.push({ text: recipe.cookTimeMinutes + "分钟", type: "meta" });
  }
  return tags;
}

function categoryTags(categoryName?: string) {
  if (!categoryName) return [];
  return categoryName.split(/[、,，]/)
    .map((name) => name.trim())
    .filter(Boolean)
    .map((name) => {
      const parts = name.split("/").map((part) => part.trim()).filter(Boolean);
      if (parts.length > 1) return parts[0] + "：" + parts.slice(1).join(" / ");
      return "分类：" + parts[0];
    });
}

function shouldShowTexture(recipe: BabyRecipe) {
  return Boolean(recipe.textureTypeLabel && !(recipe.sourceProvider === "JISU" && recipe.textureType === "SOFT_CHUNK"));
}

function riskTagText(recipe: BabyRecipe) {
  const count = Number(recipe.ruleHitCount || 0);
  const suffix = count > 0 ? count + "项" : "";
  return "注意" + suffix;
}

function cleanText(text?: string) {
  if (!text) return "";
  return text
    .replace(/<br\s*\/?>/gi, " ")
    .replace(/<[^>]+>/g, " ")
    .replace(/&quot;/g, "\"")
    .replace(/&#39;|&apos;/g, "'")
    .replace(/&lt;/g, "<")
    .replace(/&gt;/g, ">")
    .replace(/&amp;/g, "&")
    .replace(/&nbsp;/g, " ")
    .replace(/\s+/g, " ")
    .trim();
}

onLoad((options) => {
  recipeId = Number(options?.id || 0);
  loadDetail();
});
</script>

<style lang="scss" scoped>
.recipe-detail { min-height: 100vh; background: #f4f7f1; padding-bottom: 44rpx; }
.detail-cover { width: 100%; height: 430rpx; background: #dfeadf; }
.detail-cover--empty { display: flex; align-items: center; justify-content: center; background: #a9c9a7; }
.detail-body { margin-top: -28rpx; padding: 30rpx 32rpx 44rpx; background: #f4f7f1; border-radius: 8rpx 8rpx 0 0; }
.title-row { display: flex; align-items: flex-start; justify-content: space-between; gap: 18rpx; }
.detail-title { flex: 1; color: #26362b; font-size: 42rpx; font-weight: 700; line-height: 1.25; }
.icon-button { display: flex; align-items: center; justify-content: center; width: 70rpx; height: 70rpx; background: #ffffff; border-radius: 8rpx; }
.meta-row { display: flex; flex-wrap: wrap; gap: 12rpx; margin-top: 20rpx; }
.meta-row text { max-width: 100%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; padding: 8rpx 14rpx; border-radius: 8rpx; background: #e8f1e6; color: #557a58; font-size: 23rpx; }
.meta-row .meta-tag--risk-block { background: #fff0ef; color: #c8443d; }
.meta-row .meta-tag--risk-warn { background: #fff8e8; color: #9b6a17; }
.meta-row .meta-tag--meta { background: #f3f5f1; color: #69746a; }
.summary { display: block; margin-top: 20rpx; color: #657463; font-size: 27rpx; line-height: 1.6; }
.safety-card, .section { margin-top: 24rpx; padding: 24rpx; background: #ffffff; border-radius: 8rpx; box-shadow: 0 10rpx 26rpx rgba(68, 94, 72, 0.07); }
.section-title { display: block; margin-bottom: 18rpx; font-size: 30rpx; font-weight: 650; color: #26362b; }
.safety-text, .tag-line { display: block; color: #6f7f6d; font-size: 25rpx; line-height: 1.6; }
.tag-line { margin-top: 10rpx; color: #8a672d; }
.rule-hit-list { margin-top: 18rpx; }
.rule-hit { display: flex; gap: 14rpx; padding: 16rpx; margin-top: 12rpx; border-radius: 8rpx; background: #fff8e8; }
.rule-hit--block { background: #fff0ef; }
.rule-hit__level { display: flex; align-items: center; justify-content: center; flex: 0 0 auto; width: 88rpx; height: 42rpx; border-radius: 8rpx; background: #d99b29; color: #ffffff; font-size: 21rpx; font-weight: 650; }
.rule-hit--block .rule-hit__level { background: #c8443d; }
.rule-hit__body { flex: 1; min-width: 0; }
.rule-hit__title { display: block; color: #4b4030; font-size: 25rpx; line-height: 1.45; }
.rule-hit__desc { display: block; margin-top: 6rpx; color: #806335; font-size: 23rpx; line-height: 1.5; }
.rule-hit--block .rule-hit__title { color: #7c302c; }
.rule-hit--block .rule-hit__desc { color: #98433e; }
.ingredient-row { display: flex; justify-content: space-between; gap: 20rpx; padding: 16rpx 0; border-bottom: 1rpx solid #edf1ec; color: #324034; font-size: 26rpx; }
.ingredient-row:last-child { border-bottom: 0; }
.step-row { display: flex; gap: 18rpx; margin-bottom: 26rpx; }
.step-index { display: flex; align-items: center; justify-content: center; flex: 0 0 auto; width: 44rpx; height: 44rpx; border-radius: 8rpx; background: #5f9564; color: #ffffff; font-size: 24rpx; }
.step-body { flex: 1; min-width: 0; }
.step-text { display: block; color: #324034; font-size: 27rpx; line-height: 1.6; }
.step-image { width: 100%; height: 300rpx; margin-top: 14rpx; border-radius: 8rpx; background: #dfeadf; }
.loading-state { display: flex; flex-direction: column; align-items: center; padding-top: 180rpx; color: #718070; font-size: 25rpx; }
</style>
