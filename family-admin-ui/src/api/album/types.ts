import type { BaseQueryParams } from "@/api/common";

export type AlbumMediaType = "IMAGE" | "VIDEO" | "AUDIO";

export interface AlbumAssetQuery extends BaseQueryParams {
  keyword?: string;
  mediaType?: AlbumMediaType;
  uploaderId?: string;
  groupId?: string;
  tagId?: string;
  status?: number;
}

export interface AlbumTagItem {
  id: string;
  name: string;
  color?: string;
  sort?: number;
  createTime?: string;
}

export interface AlbumGroupItem {
  id: string;
  name: string;
  description?: string;
  sort?: number;
  createTime?: string;
}

export interface AlbumAssetItem {
  id: string;
  uploaderId: string;
  uploaderName?: string;
  mediaType: AlbumMediaType;
  mediaTypeLabel?: string;
  url: string;
  previewUrl?: string;
  thumbnailUrl?: string;
  thumbnailPreviewUrl?: string;
  originalName: string;
  mimeType?: string;
  fileSize?: number;
  duration?: number;
  width?: number;
  height?: number;
  groupId?: string;
  groupName?: string;
  tags?: AlbumTagItem[];
  description?: string;
  capturedAt?: string;
  status: number;
  createTime?: string;
  updateTime?: string;
}

export interface AlbumAssetForm {
  id?: string;
  uploaderId?: string;
  mediaType?: AlbumMediaType;
  url?: string;
  previewUrl?: string;
  thumbnailUrl?: string;
  thumbnailPreviewUrl?: string;
  originalName?: string;
  mimeType?: string;
  fileSize?: number;
  duration?: number;
  width?: number;
  height?: number;
  groupId?: string;
  tagIds?: string[];
  description?: string;
  capturedAt?: string;
  status?: number;
}

export interface AlbumGroupForm {
  id?: string;
  name?: string;
  description?: string;
  sort?: number;
}

export interface AlbumTagForm {
  id?: string;
  name?: string;
  color?: string;
  sort?: number;
}
