import request from "@/utils/request";
import type {
  AlbumAssetForm,
  AlbumAssetItem,
  AlbumAssetQuery,
  AlbumGroupForm,
  AlbumGroupItem,
  AlbumTagForm,
  AlbumTagItem,
} from "./types";

const BASE_URL = "/api/v1/album";

const AlbumAPI = {
  getAssetPage(params: AlbumAssetQuery) {
    return request<unknown, PageResult<AlbumAssetItem>>({
      url: `${BASE_URL}/assets`,
      method: "get",
      params,
    });
  },
  getAssetForm(id: string) {
    return request<unknown, AlbumAssetForm>({
      url: `${BASE_URL}/assets/${id}/form`,
      method: "get",
    });
  },
  createAsset(data: AlbumAssetForm) {
    return request({ url: `${BASE_URL}/assets`, method: "post", data });
  },
  updateAsset(id: string, data: AlbumAssetForm) {
    return request({ url: `${BASE_URL}/assets/${id}`, method: "put", data });
  },
  deleteAssets(ids: string) {
    return request({ url: `${BASE_URL}/assets/${ids}`, method: "delete" });
  },
  changeAssetGroup(ids: string, groupId?: string) {
    return request({ url: `${BASE_URL}/assets/${ids}/group`, method: "put", data: { groupId } });
  },
  replaceAssetTags(ids: string, tagIds: string[]) {
    return request({ url: `${BASE_URL}/assets/${ids}/tags`, method: "put", data: { tagIds } });
  },
  getGroups(params?: { keyword?: string }) {
    return request<unknown, AlbumGroupItem[]>({
      url: `${BASE_URL}/groups`,
      method: "get",
      params,
    });
  },
  getGroupForm(id: string) {
    return request<unknown, AlbumGroupForm>({
      url: `${BASE_URL}/groups/${id}/form`,
      method: "get",
    });
  },
  createGroup(data: AlbumGroupForm) {
    return request({ url: `${BASE_URL}/groups`, method: "post", data });
  },
  updateGroup(id: string, data: AlbumGroupForm) {
    return request({ url: `${BASE_URL}/groups/${id}`, method: "put", data });
  },
  deleteGroups(ids: string) {
    return request({ url: `${BASE_URL}/groups/${ids}`, method: "delete" });
  },
  getTags(params?: { keyword?: string }) {
    return request<unknown, AlbumTagItem[]>({
      url: `${BASE_URL}/tags`,
      method: "get",
      params,
    });
  },
  getTagForm(id: string) {
    return request<unknown, AlbumTagForm>({
      url: `${BASE_URL}/tags/${id}/form`,
      method: "get",
    });
  },
  createTag(data: AlbumTagForm) {
    return request({ url: `${BASE_URL}/tags`, method: "post", data });
  },
  updateTag(id: string, data: AlbumTagForm) {
    return request({ url: `${BASE_URL}/tags/${id}`, method: "put", data });
  },
  deleteTags(ids: string) {
    return request({ url: `${BASE_URL}/tags/${ids}`, method: "delete" });
  },
};

export default AlbumAPI;
export * from "./types";
