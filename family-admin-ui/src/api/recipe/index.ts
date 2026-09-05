import request from "@/utils/request";
import type {
  BabyRecipeDetail,
  BabyRecipeForm,
  BabyRecipeItem,
  BabyRecipeQuery,
  JumdataRecipeCategory,
  JumdataRecipePreviewPage,
  JumdataRecipePreviewQuery,
  JumdataSelectedRecipeSyncRequest,
  RecipeSyncTask,
  JumdataRecipeSyncRequest,
  RecipeScreenResult,
  RecipeRuleForm,
  RecipeRuleItem,
} from "./types";

const BASE_URL = "/api/v1/recipes";

const RecipeAPI = {
  getPage(params: BabyRecipeQuery) {
    return request<unknown, PageResult<BabyRecipeItem>>({ url: BASE_URL, method: "get", params });
  },
  getCategories() {
    return request<unknown, string[]>({ url: `${BASE_URL}/options/categories`, method: "get" });
  },
  getDetail(id: string) {
    return request<unknown, BabyRecipeDetail>({ url: `${BASE_URL}/${id}`, method: "get" });
  },
  create(data: BabyRecipeForm) {
    return request<unknown, string>({ url: BASE_URL, method: "post", data });
  },
  update(id: string, data: BabyRecipeForm) {
    return request({ url: `${BASE_URL}/${id}`, method: "put", data });
  },
  screen(id: string) {
    return request<unknown, RecipeScreenResult>({ url: `${BASE_URL}/${id}/screen`, method: "post" });
  },
  approve(id: string) {
    return request({ url: `${BASE_URL}/${id}/approve`, method: "post" });
  },
  reject(id: string) {
    return request({ url: `${BASE_URL}/${id}/reject`, method: "post" });
  },
  publish(id: string) {
    return request({ url: `${BASE_URL}/${id}/publish`, method: "post" });
  },
  offline(id: string) {
    return request({ url: `${BASE_URL}/${id}/offline`, method: "post" });
  },
  previewJumdata(params: JumdataRecipePreviewQuery) {
    return request<unknown, JumdataRecipePreviewPage>({ url: `${BASE_URL}/jisu/preview`, method: "get", params });
  },
  startCategorySync(data: JumdataRecipeSyncRequest) {
    return request<unknown, RecipeSyncTask>({ url: `${BASE_URL}/jisu/sync-tasks/category`, method: "post", data });
  },
  startSelectedSync(data: JumdataSelectedRecipeSyncRequest) {
    return request<unknown, RecipeSyncTask>({ url: `${BASE_URL}/jisu/sync-tasks/selected`, method: "post", data });
  },
  getSyncTask(taskNo: string) {
    return request<unknown, RecipeSyncTask>({ url: `${BASE_URL}/jisu/sync-tasks/${taskNo}`, method: "get" });
  },
  getJumdataCategories() {
    return request<unknown, JumdataRecipeCategory[]>({ url: (BASE_URL + "/jisu/categories"), method: "get" });
  },
  getRecentSyncTasks() {
    return request<unknown, RecipeSyncTask[]>({ url: `${BASE_URL}/jisu/sync-tasks`, method: "get" });
  },
  getRules() {
    return request<unknown, RecipeRuleItem[]>({ url: `${BASE_URL}/rules`, method: "get" });
  },
  createRule(data: RecipeRuleForm) {
    return request<unknown, string>({ url: `${BASE_URL}/rules`, method: "post", data });
  },
  updateRule(id: string, data: RecipeRuleForm) {
    return request({ url: `${BASE_URL}/rules/${id}`, method: "put", data });
  },
  deleteRule(id: string) {
    return request({ url: `${BASE_URL}/rules/${id}`, method: "delete" });
  },
};

export default RecipeAPI;
export * from "./types";
