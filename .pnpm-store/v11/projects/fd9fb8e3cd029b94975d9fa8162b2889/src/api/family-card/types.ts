export type FamilyCardScene =
  | "CREATE_FAMILY"
  | "INVITE_MEMBER"
  | "UPCOMING_SCHEDULE"
  | "RECENT_PHOTO"
  | "TODAY_MEMORY"
  | "SPECIAL_DATE";

export interface FamilyCardConfigItem {
  id: string;
  scene: FamilyCardScene;
  sceneLabel: string;
  titleTemplate: string;
  descriptionTemplate: string;
  tagText?: string;
  icon?: string;
  backgroundUrl?: string;
  actionText?: string;
  priority: number;
  windowDays: number;
  status: number;
  updateTime?: string;
}

export interface FamilyCardConfigForm {
  titleTemplate?: string;
  descriptionTemplate?: string;
  tagText?: string;
  icon?: string;
  backgroundUrl?: string;
  actionText?: string;
  priority: number;
  windowDays: number;
  status: number;
}
