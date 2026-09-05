<template>
  <view class="recipe-page page--tabbar">
    <custom-navbar title="家庭食谱" :show-back="false" placeholder />

    <view class="audience-switch">
      <view
        v-for="option in audienceOptions"
        :key="option.value"
        class="audience-switch__item"
        :class="{ 'audience-switch__item--active': selectedAudience === option.value }"
        @click="changeAudience(option.value)"
      >
        {{ option.label }}
      </view>
    </view>

    <view class="search-bar">
      <wd-icon name="search" size="18" color="#8f879a" />
      <input
        v-model="keyword"
        class="search-bar__input"
        placeholder="搜索南瓜、小米、鸡蛋..."
        confirm-type="search"
        @confirm="refreshRecipes"
      />
      <view v-if="keyword" class="search-bar__clear" @click="clearKeyword">
        <wd-icon name="close" size="14" color="#8f879a" />
      </view>
      <view class="search-bar__button" @click="refreshRecipes">搜索</view>
    </view>

    <view class="category-panel">
      <scroll-view class="category-scroll" scroll-x :show-scrollbar="false">
        <view class="category-row">
          <view
            class="category-chip"
            :class="{ 'category-chip--active': !selectedPrimaryCategory }"
            @click="changePrimaryCategory('')"
          >
            全部
          </view>
          <view v-if="categoriesLoading" class="category-chip category-chip--muted">
            加载中
          </view>
          <view
            v-for="category in categoryTree"
            :key="category.name"
            class="category-chip"
            :class="{ 'category-chip--active': selectedPrimaryCategory === category.name }"
            @click="changePrimaryCategory(category.name)"
          >
            {{ category.name }}
          </view>
        </view>
      </scroll-view>

      <scroll-view v-if="secondaryCategories.length > 0" class="category-scroll category-scroll--sub" scroll-x :show-scrollbar="false">
        <view class="category-row">
          <view
            class="category-chip category-chip--sub"
            :class="{ 'category-chip--active': !selectedSecondaryCategory }"
            @click="changeSecondaryCategory('')"
          >
            全部{{ selectedPrimaryCategory }}
          </view>
          <view
            v-for="category in secondaryCategories"
            :key="category.value"
            class="category-chip category-chip--sub"
            :class="{ 'category-chip--active': selectedSecondaryCategory === category.value }"
            @click="changeSecondaryCategory(category.value)"
          >
            {{ category.label }}
          </view>
        </view>
      </scroll-view>
    </view>

    <view class="scope-switch">
      <view
        v-for="option in scopeOptions"
        :key="option.value"
        class="scope-switch__item"
        :class="{ 'scope-switch__item--active': selectedScope === option.value }"
        @click="changeScope(option.value)"
      >
        {{ option.label }}
      </view>
    </view>

    <view class="notice-banner">
      <wd-icon name="warning" size="17" color="#c08322" />
      <text>食谱建议仅作家庭参考，首次添加新食材请少量尝试并观察。</text>
    </view>

    <view v-if="loading && recipes.length === 0" class="loading-state">
      <wd-loading color="#69a66d" />
      <text>正在加载食谱...</text>
    </view>

    <view v-else-if="recipes.length === 0" class="empty-state">
      <view class="empty-state__icon"><wd-icon name="menu" size="56rpx" color="#78a979" /></view>
      <text class="empty-state__title">{{ emptyTitle }}</text>
      <text class="empty-state__desc">{{ emptyDesc }}</text>
    </view>

    <view v-else class="recipe-list">
      <view v-for="recipe in recipes" :key="recipe.id" class="recipe-card" @click="goDetail(recipe.id)">
        <image v-if="recipe.coverUrl" class="recipe-card__cover" :src="recipe.coverUrl" mode="aspectFill" lazy-load />
        <view v-else class="recipe-card__cover recipe-card__cover--empty">
          <wd-icon name="menu" size="44rpx" color="#ffffff" />
        </view>
        <view class="recipe-card__body">
          <view class="recipe-card__top">
            <text class="recipe-card__title">{{ recipe.title }}</text>
            <view class="favorite-button" @click.stop="toggleFavorite(recipe)">
              <wd-icon :name="recipe.favorite ? 'star-on' : 'star'" size="18" color="#e3a127" />
            </view>
          </view>
          <text v-if="displaySummary(recipe.summary)" class="recipe-card__summary">{{ displaySummary(recipe.summary) }}</text>
          <view class="recipe-card__tags">
            <text
              v-for="tag in recipeTags(recipe)"
              :key="tag.text"
              :class="'recipe-card__tag--' + tag.type"
            >
              {{ tag.text }}
            </text>
          </view>
          <view class="recipe-card__footer">
            <text class="recipe-card__count">{{ recipe.mealTypeLabel || recipe.servingSize || "家庭餐桌" }}</text>
            <view class="cooked-action" :class="{ 'cooked-action--done': recipe.cooked }" @click.stop="markCookedFromList(recipe)">
              <wd-icon name="check" size="14" :color="recipe.cooked ? '#ffffff' : '#5f9564'" />
              <text>{{ recipe.cooked ? "已做过" : "做过" }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view v-if="recipes.length > 0" class="load-more" @click="loadMore">
      <text v-if="loading">正在加载...</text>
      <text v-else-if="finished">已经看到全部食谱</text>
      <text v-else>继续加载</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { onPullDownRefresh, onReachBottom } from "@dcloudio/uni-app";
import RecipeAPI, { BabyRecipe, RecipeAudienceType } from "@/api/recipe";

definePage({ name: "menu", style: { navigationStyle: "custom" }, layout: "tabbar" });

type ScopeType = "all" | "favorite" | "cooked";
type RecipeTagType = "category" | "risk-block" | "risk-warn" | "meta";

interface CategoryNode {
  name: string;
  children: Array<{ label: string; value: string }>;
}

interface RecipeTag {
  text: string;
  type: RecipeTagType;
}

const audienceOptions: Array<{ label: string; value: RecipeAudienceType }> = [
  { label: "宝宝辅食", value: "INFANT" },
  { label: "家庭菜谱", value: "GENERAL" },
];

const scopeOptions: Array<{ label: string; value: ScopeType }> = [
  { label: "全部", value: "all" },
  { label: "收藏", value: "favorite" },
  { label: "做过", value: "cooked" },
];

const selectedAudience = ref<RecipeAudienceType>("INFANT");
const selectedScope = ref<ScopeType>("all");
const selectedPrimaryCategory = ref("");
const selectedSecondaryCategory = ref("");
const keyword = ref("");
const pageNum = ref(1);
const pageSize = 10;
const total = ref(0);
const loading = ref(false);
const categoriesLoading = ref(false);
const categories = ref<string[]>([]);
const recipes = ref<BabyRecipe[]>([]);

const finished = computed(() => recipes.value.length >= total.value && total.value > 0);
const categoryTree = computed<CategoryNode[]>(() => {
  const groups = new Map<string, Map<string, string>>();
  categories.value.forEach((category) => {
    const parsed = parseCategory(category);
    if (!groups.has(parsed.primary)) {
      groups.set(parsed.primary, new Map<string, string>());
    }
    if (parsed.secondary) {
      groups.get(parsed.primary)?.set(parsed.secondary, category);
    }
  });
  return Array.from(groups.entries()).map(([name, children]) => ({
    name,
    children: Array.from(children.entries())
      .map(([label, value]) => ({ label: formatCategoryLabel(name, label), value, sort: categorySortValue(name, label) }))
      .sort((left, right) => left.sort - right.sort || left.label.localeCompare(right.label, "zh-Hans-CN")),
  })).sort((left, right) => left.name.localeCompare(right.name, "zh-Hans-CN"));
});
const secondaryCategories = computed(() => categoryTree.value.find((item) => item.name === selectedPrimaryCategory.value)?.children || []);
const selectedCategory = computed(() => selectedSecondaryCategory.value || selectedPrimaryCategory.value);
const emptyTitle = computed(() => selectedScope.value === "favorite" ? "还没有收藏食谱" : selectedScope.value === "cooked" ? "还没有做过记录" : "还没有可展示食谱");
const emptyDesc = computed(() => selectedScope.value === "all" ? "后台发布审核通过的食谱后，这里会自动出现。" : "换个筛选条件看看，也可以先去收藏或记录做过。");

async function loadCategories() {
  categoriesLoading.value = true;
  try {
    categories.value = await RecipeAPI.getCategories(selectedAudience.value);
  } catch (error: any) {
    categories.value = [];
    uni.showToast({ title: error?.message || "分类加载失败", icon: "none" });
  } finally {
    categoriesLoading.value = false;
  }
}

async function loadRecipes(reset = false) {
  if (loading.value || (!reset && finished.value)) return;
  loading.value = true;
  try {
    if (reset) {
      pageNum.value = 1;
      recipes.value = [];
      total.value = 0;
    }
    const page = await RecipeAPI.getRecipePage({
      pageNum: pageNum.value,
      pageSize,
      audienceType: selectedAudience.value,
      categoryName: selectedCategory.value,
      keyword: keyword.value.trim(),
      favoriteOnly: selectedScope.value === "favorite",
      cookedOnly: selectedScope.value === "cooked",
    });
    recipes.value = reset ? page.list : recipes.value.concat(page.list);
    total.value = Number(page.total || 0);
    pageNum.value += 1;
  } catch (error: any) {
    uni.showToast({ title: error?.message || "食谱加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

function refreshRecipes() {
  loadRecipes(true);
}

function loadMore() {
  loadRecipes(false);
}

function changeAudience(audience: RecipeAudienceType) {
  if (selectedAudience.value === audience) return;
  selectedAudience.value = audience;
  selectedPrimaryCategory.value = "";
  selectedSecondaryCategory.value = "";
  categories.value = [];
  loadCategories();
  refreshRecipes();
}

function changePrimaryCategory(category: string) {
  if (selectedPrimaryCategory.value === category) return;
  selectedPrimaryCategory.value = category;
  selectedSecondaryCategory.value = "";
  refreshRecipes();
}

function changeSecondaryCategory(category: string) {
  if (selectedSecondaryCategory.value === category) return;
  selectedSecondaryCategory.value = category;
  refreshRecipes();
}

function changeScope(scope: ScopeType) {
  if (selectedScope.value === scope) return;
  selectedScope.value = scope;
  refreshRecipes();
}

function clearKeyword() {
  keyword.value = "";
  refreshRecipes();
}

async function toggleFavorite(recipe: BabyRecipe) {
  try {
    if (recipe.favorite) {
      await RecipeAPI.cancelFavorite(recipe.id);
      recipe.favorite = false;
      if (selectedScope.value === "favorite") {
        recipes.value = recipes.value.filter((item) => item.id !== recipe.id);
        total.value = Math.max(0, total.value - 1);
      }
    } else {
      await RecipeAPI.favorite(recipe.id);
      recipe.favorite = true;
    }
  } catch (error: any) {
    uni.showToast({ title: error?.message || "操作失败", icon: "none" });
  }
}

async function markCookedFromList(recipe: BabyRecipe) {
  if (recipe.cooked) return;
  try {
    await RecipeAPI.markCooked(recipe.id);
    recipe.cooked = true;
    uni.showToast({ title: "已记录", icon: "success" });
  } catch (error: any) {
    uni.showToast({ title: error?.message || "操作失败", icon: "none" });
  }
}

function parseCategory(categoryName: string) {
  const names = categoryName.split("/").map((name) => name.trim()).filter(Boolean);
  return {
    primary: names[0] || categoryName,
    secondary: names.slice(1).join(" / "),
  };
}

function formatCategoryLabel(primary: string, secondary: string) {
  return primary === "人群" && secondary === "宝宝" ? "宝宝（泛分类）" : secondary;
}

function categorySortValue(primary: string, secondary: string) {
  if (primary !== "人群") return 100;
  const order = ["婴儿", "幼儿", "一岁宝宝", "两岁宝宝", "三岁宝宝", "宝宝"];
  const index = order.indexOf(secondary);
  return index >= 0 ? index : 90;
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
  return tags.slice(0, 5);
}

function categoryTags(categoryName?: string) {
  if (!categoryName) return [];
  return categoryName.split(/[、,，]/)
    .map((name) => name.trim())
    .filter(Boolean)
    .map((name) => {
      const parsed = parseCategory(name);
      return parsed.secondary ? parsed.primary + "：" + parsed.secondary : "分类：" + parsed.primary;
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

function displaySummary(summary?: string) {
  return cleanText(summary);
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

function goDetail(id: number) {
  uni.navigateTo({ url: `/pages/menu/detail?id=${id}` });
}

onMounted(() => {
  loadCategories();
  refreshRecipes();
});

onPullDownRefresh(() => {
  Promise.all([loadCategories(), loadRecipes(true)]).finally(() => uni.stopPullDownRefresh());
});

onReachBottom(() => {
  loadMore();
});
</script>

<style lang="scss" scoped>
.recipe-page { min-height: 100vh; background: #f4f7f1; padding-bottom: 36rpx; }
.audience-switch { display: grid; grid-template-columns: 1fr 1fr; gap: 12rpx; margin: 16rpx 32rpx 14rpx; padding: 8rpx; background: #e8efe7; border-radius: 8rpx; }
.audience-switch__item { padding: 16rpx 0; text-align: center; border-radius: 8rpx; color: #60725f; font-size: 26rpx; }
.audience-switch__item--active { background: #ffffff; color: #315c36; font-weight: 650; box-shadow: 0 8rpx 20rpx rgba(67, 92, 70, 0.08); }
.search-bar { display: flex; align-items: center; margin: 0 32rpx; padding: 16rpx 16rpx 16rpx 22rpx; background: #ffffff; border-radius: 8rpx; }
.search-bar__input { flex: 1; min-width: 0; margin-left: 12rpx; font-size: 26rpx; color: #29332d; }
.search-bar__clear { display: flex; align-items: center; justify-content: center; width: 44rpx; height: 44rpx; }
.search-bar__button { padding: 10rpx 20rpx; border-radius: 8rpx; background: #edf5ec; color: #4f8554; font-size: 24rpx; }
.category-panel { margin-top: 12rpx; }
.category-scroll { width: 100%; white-space: nowrap; }
.category-scroll--sub { margin-top: 12rpx; }
.category-row { display: inline-flex; gap: 12rpx; padding: 0 32rpx; }
.category-chip { max-width: 280rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; padding: 12rpx 18rpx; border-radius: 8rpx; background: #ffffff; color: #647365; font-size: 24rpx; border: 1rpx solid #e3eae2; }
.category-chip--sub { background: #f9fbf8; font-size: 23rpx; }
.category-chip--active { color: #ffffff; background: #5f9564; border-color: #5f9564; }
.category-chip--muted { color: #94a092; background: #f7faf6; }
.scope-switch { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10rpx; margin: 14rpx 32rpx 0; }
.scope-switch__item { padding: 14rpx 0; text-align: center; border-radius: 8rpx; color: #60725f; font-size: 25rpx; background: #e8efe7; }
.scope-switch__item--active { background: #315c36; color: #ffffff; font-weight: 650; }
.notice-banner { display: flex; gap: 12rpx; align-items: flex-start; margin: 14rpx 32rpx 8rpx; padding: 14rpx 18rpx; border-radius: 8rpx; background: #fff8e8; color: #8a672d; font-size: 23rpx; line-height: 1.5; }
.loading-state, .empty-state { display: flex; flex-direction: column; align-items: center; padding: 130rpx 40rpx 0; color: #718070; font-size: 25rpx; }
.empty-state__icon { display: flex; align-items: center; justify-content: center; width: 124rpx; height: 124rpx; background: #dfeadf; border-radius: 8rpx; }
.empty-state__title { margin-top: 28rpx; font-size: 32rpx; font-weight: 650; color: #2f3b33; }
.empty-state__desc { margin-top: 10rpx; font-size: 25rpx; color: #7d887b; text-align: center; }
.recipe-list { padding: 18rpx 32rpx 0; }
.recipe-card { display: flex; gap: 20rpx; padding: 18rpx; margin-bottom: 18rpx; background: #ffffff; border-radius: 8rpx; box-shadow: 0 10rpx 26rpx rgba(68, 94, 72, 0.07); }
.recipe-card__cover { flex: 0 0 auto; width: 180rpx; height: 150rpx; border-radius: 8rpx; background: #dfeadf; }
.recipe-card__cover--empty { display: flex; align-items: center; justify-content: center; background: #a9c9a7; }
.recipe-card__body { flex: 1; min-width: 0; }
.recipe-card__top { display: flex; align-items: flex-start; justify-content: space-between; gap: 12rpx; }
.recipe-card__title { flex: 1; font-size: 30rpx; font-weight: 650; line-height: 1.35; color: #26362b; }
.favorite-button { display: flex; align-items: center; justify-content: center; flex: 0 0 auto; width: 48rpx; height: 48rpx; }
.recipe-card__summary { display: -webkit-box; margin-top: 8rpx; overflow: hidden; color: #748173; font-size: 24rpx; line-height: 1.45; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.recipe-card__tags { display: flex; flex-wrap: wrap; gap: 10rpx; margin-top: 14rpx; }
.recipe-card__tags text { max-width: 230rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; padding: 6rpx 12rpx; border-radius: 8rpx; background: #eef5ed; color: #5b7f5d; font-size: 21rpx; }
.recipe-card__tag--risk-block { background: #fff0ef !important; color: #c8443d !important; }
.recipe-card__tag--risk-warn { background: #fff8e8 !important; color: #9b6a17 !important; }
.recipe-card__tag--meta { background: #f3f5f1 !important; color: #69746a !important; }
.recipe-card__footer { display: flex; justify-content: space-between; align-items: center; gap: 12rpx; margin-top: 14rpx; }
.recipe-card__count { flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: #94a092; font-size: 22rpx; }
.cooked-action { display: flex; align-items: center; gap: 6rpx; flex: 0 0 auto; padding: 8rpx 14rpx; border-radius: 8rpx; color: #5f9564; background: #eef5ed; font-size: 22rpx; }
.cooked-action--done { color: #ffffff; background: #5f9564; }
.load-more { padding: 22rpx 0 4rpx; text-align: center; color: #7d887b; font-size: 24rpx; }
</style>
