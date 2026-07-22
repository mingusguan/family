import request from "@/utils/request";

const APP_BASE_URL = "/api/v1/app";

const FamilyCardAPI = {
  getCurrentCards(familyId?: number) {
    return request<FamilyStatusCard[]>({
      url: APP_BASE_URL + "/family-card",
      method: "GET",
      data: familyId ? { familyId } : undefined,
    });
  },

  markPhotoRead(familyId: number) {
    return request<void>({
      url: APP_BASE_URL + "/family-card/" + familyId + "/photo-read",
      method: "POST",
    });
  },

  listSchedules(familyId: number) {
    return request<FamilySchedule[]>({
      url: APP_BASE_URL + "/family-schedules",
      method: "GET",
      data: { familyId },
    });
  },

  createSchedule(data: FamilyScheduleForm) {
    return request<FamilySchedule>({
      url: APP_BASE_URL + "/family-schedules",
      method: "POST",
      data,
    });
  },

  updateSchedule(id: number, data: FamilyScheduleForm) {
    return request<FamilySchedule>({
      url: APP_BASE_URL + "/family-schedules/" + id,
      method: "PUT",
      data,
    });
  },

  completeSchedule(id: number) {
    return request<void>({
      url: APP_BASE_URL + "/family-schedules/" + id + "/complete",
      method: "PUT",
    });
  },

  deleteSchedule(id: number) {
    return request<void>({
      url: APP_BASE_URL + "/family-schedules/" + id,
      method: "DELETE",
    });
  },
};

export default FamilyCardAPI;

export type FamilyCardScene =
  | "CREATE_FAMILY"
  | "INVITE_MEMBER"
  | "UPCOMING_SCHEDULE"
  | "RECENT_PHOTO"
  | "TODAY_MEMORY"
  | "SPECIAL_DATE";

export interface FamilyStatusCard {
  scene: FamilyCardScene;
  familyId?: number;
  title: string;
  description: string;
  tagText?: string;
  icon?: string;
  backgroundUrl?: string;
  actionText?: string;
  actionPath: string;
  referenceId?: number;
  count?: number;
}

export type FamilyScheduleType = "PLAN" | "BIRTHDAY" | "ANNIVERSARY" | "FESTIVAL";

export interface FamilyScheduleForm {
  familyId: number;
  type: FamilyScheduleType;
  title: string;
  description?: string;
  eventTime: string;
  repeatYearly?: boolean;
}

export interface FamilySchedule extends FamilyScheduleForm {
  id: number;
  creatorId: number;
  typeLabel: string;
  status: number;
  createTime: string;
}
