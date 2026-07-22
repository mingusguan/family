import request from "@/utils/request";

const FAMILY_BASE_URL = "/api/v1/app/families";

const FamilyAPI = {
  listMyFamilies() {
    return request<FamilyInfo[]>({
      url: FAMILY_BASE_URL,
      method: "GET",
    });
  },

  createFamily(data: FamilyCreateRequest) {
    return request<FamilyInfo>({
      url: FAMILY_BASE_URL,
      method: "POST",
      data,
    });
  },

  getFamilyDetail(familyId: number) {
    return request<FamilyDetail>({
      url: FAMILY_BASE_URL + "/" + familyId,
      method: "GET",
    });
  },

  createInvite(familyId: number) {
    return request<FamilyInvite>({
      url: FAMILY_BASE_URL + "/" + familyId + "/invites",
      method: "POST",
    });
  },

  getInvite(code: string) {
    return request<FamilyInvite>({
      url: FAMILY_BASE_URL + "/invites/" + encodeURIComponent(code),
      method: "GET",
    });
  },

  acceptInvite(code: string) {
    return request<FamilyInfo>({
      url: FAMILY_BASE_URL + "/invites/" + encodeURIComponent(code) + "/accept",
      method: "POST",
    });
  },

  listAlbums(familyId: number) {
    return request<FamilyAlbum[]>({
      url: FAMILY_BASE_URL + "/" + familyId + "/albums",
      method: "GET",
    });
  },

  createAlbum(familyId: number, data: FamilyAlbumCreateRequest) {
    return request<FamilyAlbum>({
      url: FAMILY_BASE_URL + "/" + familyId + "/albums",
      method: "POST",
      data,
    });
  },
};

export default FamilyAPI;

export interface FamilyCreateRequest {
  name: string;
  description?: string;
}

export interface FamilyAlbumCreateRequest {
  name: string;
  description?: string;
}

export interface FamilyInfo {
  id: number;
  name: string;
  description?: string;
  coverUrl?: string;
  creatorId: number;
  role: "OWNER" | "MEMBER";
  memberCount: number;
  albumCount: number;
  createTime: string;
}

export interface FamilyAlbum {
  id: number;
  familyId: number;
  name: string;
  description?: string;
  coverUrl?: string;
  creatorId: number;
  assetCount: number;
  createTime: string;
}
export interface FamilyMember {
  userId: number;
  nickname: string;
  avatar?: string;
  role: "OWNER" | "MEMBER";
  joinedAt: string;
}

export interface FamilyDetail extends FamilyInfo {
  members: FamilyMember[];
}

export interface FamilyInvite {
  code: string;
  familyId: number;
  familyName: string;
  inviterName: string;
  expiresAt: string;
  alreadyMember: boolean;
}
