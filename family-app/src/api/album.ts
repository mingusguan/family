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

  initializeDirectUpload(data: AlbumDirectUploadInitRequest) {
    return request<AlbumDirectUploadInitResult>({
      url: ALBUM_BASE_URL + "/direct-uploads",
      method: "POST",
      data,
      timeout: 3 * 60 * 1000,
    });
  },

  confirmDirectUpload(data: AlbumDirectUploadConfirmRequest) {
    return request<AlbumDirectUploadStatus>({
      url: ALBUM_BASE_URL + "/direct-uploads/confirm",
      method: "POST",
      data,
      timeout: 3 * 60 * 1000,
    });
  },

  getDirectUploadStatus(batchId: string) {
    return request<AlbumDirectUploadStatus>({
      url: ALBUM_BASE_URL + "/direct-uploads/" + batchId,
      method: "GET",
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

export interface AlbumDirectUploadInitRequest {
  familyId: number;
  albumId: number;
  description?: string;
  files: AlbumDirectUploadFile[];
}

export interface AlbumDirectUploadFile {
  mediaType: AlbumMediaType;
  originalName: string;
  mimeType?: string;
  fileSize: number;
  duration?: number;
  width?: number;
  height?: number;
  hasThumbnail?: boolean;
}

export interface CosPostUploadTicket {
  uploadUrl: string;
  objectKey: string;
  policy: string;
  qSignAlgorithm: string;
  qAk: string;
  qKeyTime: string;
  qSignature: string;
  securityToken?: string;
}

export interface AlbumDirectUploadItem {
  index: number;
  file: CosPostUploadTicket;
  thumbnail?: CosPostUploadTicket;
}

export interface AlbumDirectUploadInitResult {
  batchId: string;
  expiresAt: string;
  uploads: AlbumDirectUploadItem[];
}

export interface AlbumDirectUploadConfirmRequest {
  batchId: string;
  uploadedIndexes: number[];
  thumbnailUploadedIndexes: number[];
}

export interface AlbumDirectUploadStatus {
  batchId: string;
  status: "INIT" | "PROCESSING" | "COMPLETED";
  total: number;
  processing: number;
  success: number;
  failed: number;
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
