import request from "@/utils/request";
import type { OptionItem } from "@/api/common";
import type { AppUserItem, AppUserQuery } from "./types";

const BASE_URL = "/api/v1/app-users";

const AppUserAPI = {
  getPage(params: AppUserQuery) {
    return request<unknown, PageResult<AppUserItem>>({ url: BASE_URL, method: "get", params });
  },
  getOptions(keyword?: string) {
    return request<unknown, OptionItem[]>({
      url: `${BASE_URL}/options`,
      method: "get",
      params: { keyword },
    });
  },
  updateStatus(userId: string, status: number) {
    return request({
      url: `${BASE_URL}/${userId}/status`,
      method: "patch",
      params: { status },
    });
  },
  deleteUsers(ids: string) {
    return request({ url: `${BASE_URL}/${ids}`, method: "delete" });
  },
};

export default AppUserAPI;
export * from "./types";

