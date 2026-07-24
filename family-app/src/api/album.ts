import request from "@/utils/request";

const ALBUM_BASE_URL = "/api/v1/app/album";

const AlbumAPI = {
  getMomentPage(query: AlbumMomentQuery) {
    const data = Object.fromEntries(
      Object.entries(query).filter(
        ([, value]) => value !== undefined && value !== null && value !== ""
      )
    ) as unknown as AlbumMomentQuery;
    return request<PageResult<AlbumMoment>>({
      url: ALBUM_BASE_URL + "/moments",
      method: "GET",
      data,
    });
  },

  createMoment(data: AlbumMomentCreateRequest) {
    return request<void>({
      url: ALBUM_BASE_URL + "/moments",
      method: "POST",
      data,
    });
  },

  deleteMoment(id: number) {
    return request<void>({
      url: ALBUM_BASE_URL + "/moments/" + id,
      method: "DELETE",
    });
  },

  listTags(keyword?: string) {
    return request<AlbumTag[]>({
      url: ALBUM_BASE_URL + "/tags",
      method: "GET",
      data: keyword ? { keyword } : undefined,
    });
  },

  createTag(name: string) {
    return request<AlbumTag>({
      url: ALBUM_BASE_URL + "/tags",
      method: "POST",
      data: { name },
    });
  },
};

export default AlbumAPI;

export type AlbumMediaType = "IMAGE" | "VIDEO" | "AUDIO";

export interface AlbumMomentQuery extends PageQuery {
  familyId: number;
  albumId: number;
  mine?: boolean;
  keyword?: string;
  startDate?: string;
  endDate?: string;
  months?: string;
  mediaTypes?: string;
}

export interface AlbumMomentCreateRequest {
  familyId: number;
  albumId: number;
  resources: AlbumMomentResourceRequest[];
  tagIds?: number[];
  description?: string;
  capturedAt?: string;
}

export interface AlbumMomentResourceRequest {
  mediaType: AlbumMediaType;
  url: string;
  thumbnailUrl?: string;
  originalName: string;
  mimeType?: string;
  fileSize?: number;
  duration?: number;
  width?: number;
  height?: number;
  capturedAt?: string;
}

export interface AlbumTag {
  id: number;
  name: string;
  color?: string;
}

export interface AlbumMoment {
  id: number;
  uploaderId: number;
  uploaderName?: string;
  familyId: number;
  albumId: number;
  mediaType: AlbumMediaType;
  mediaTypeLabel: string;
  url: string;
  previewUrl: string;
  thumbnailUrl?: string;
  thumbnailPreviewUrl?: string;
  originalName: string;
  mimeType?: string;
  fileSize?: number;
  duration?: number;
  width?: number;
  height?: number;
  tags?: AlbumTag[];
  description?: string;
  capturedAt?: string;
  createTime: string;
}
