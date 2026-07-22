import request from "@/utils/request";
import type { FamilyCardConfigForm, FamilyCardConfigItem } from "./types";

const BASE_URL = "/api/v1/family-card-configs";

const FamilyCardAPI = {
  listConfigs() {
    return request<unknown, FamilyCardConfigItem[]>({
      url: BASE_URL,
      method: "get",
    });
  },
  getConfigForm(id: string) {
    return request<unknown, FamilyCardConfigForm>({
      url: `${BASE_URL}/${id}/form`,
      method: "get",
    });
  },
  updateConfig(id: string, data: FamilyCardConfigForm) {
    return request({
      url: `${BASE_URL}/${id}`,
      method: "put",
      data,
    });
  },
};

export default FamilyCardAPI;
export * from "./types";
