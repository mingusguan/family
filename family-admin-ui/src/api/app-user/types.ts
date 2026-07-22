import type { BaseQueryParams } from "@/api/common";

export interface AppUserQuery extends BaseQueryParams {
  keyword?: string;
  status?: number;
}

export interface AppUserItem {
  id: string;
  username?: string;
  nickname?: string;
  mobile?: string;
  avatar?: string;
  status: number;
  wechatBound?: boolean;
  createTime?: string;
  updateTime?: string;
}
