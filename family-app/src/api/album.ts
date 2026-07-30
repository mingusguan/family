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

  getMomentBatchPage(query: AlbumMomentQuery) {
    const data = Object.fromEntries(
      Object.entries(query).filter(
        ([, value]) => value !== undefined && value !== null && value !== ""
      )
    ) as unknown as AlbumMomentQuery;
    return request<PageResult<AlbumMomentBatch>>({
      url: ALBUM_BASE_URL + "/moment-batches",
      method: "GET",
      data,
    });
  },

  getMomentDetail(batchId: string, familyId: number, albumId: number) {
    return request<AlbumMomentDetail>({
      url: ALBUM_BASE_URL + "/moments/batches/" + batchId,
      method: "GET",
      data: { familyId, albumId },
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

export interface AlbumMomentCover {
  mediaType: AlbumMediaType;
  previewUrl?: string;
}

export interface AlbumMomentBatch {
  batchId: string;
  uploaderId: number;
  uploaderName?: string;
  familyId: number;
  albumId: number;
  description?: string;
  capturedAt?: string;
  createTime: string;
  assetCount: number | string;
  covers: AlbumMomentCover[];
}

export interface AlbumMomentDetail {
  batchId: string;
  uploaderId: number;
  uploaderName?: string;
  familyId: number;
  albumId: number;
  description?: string;
  capturedAt?: string;
  createTime: string;
  assets: AlbumMoment[];
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
  description?: string;
  capturedAt?: string;
  createTime: string;
}
