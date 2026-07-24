<template>
  <view class="home-page page--tabbar">
    <view class="hero">
      <image v-if="heroCover" class="hero__cover" :src="heroCover" mode="aspectFill" />
      <view class="hero__mask" />
      <view class="hero__content" :style="{ paddingTop: `${navbar.totalHeight.value + 12}px` }">
        <view class="hero__heading">
          <view>
            <text class="hero__title">{{ greeting }}，{{ displayName }}</text>
            <text class="hero__subtitle">
              {{ isLogged ? "记录生活，也珍藏彼此的成长" : "把属于家人的每个瞬间留在这里" }}
            </text>
          </view>
          <image
            v-if="userStore.userInfo?.avatar"
            class="hero__avatar"
            :src="userStore.userInfo.avatar"
            mode="aspectFill"
          />
          <view v-else class="hero__avatar hero__avatar--empty">
            <wd-icon name="user" size="24" color="#fff" />
          </view>
        </view>
        <view v-if="isLogged && currentFamily" class="family-pill" @click="openFamilySelector">
          <view class="family-pill__icon"><wd-icon name="home" size="18" color="#fff" /></view>
          <view class="family-pill__body">
            <text class="family-pill__name">{{ currentFamily.name }}</text>
            <text class="family-pill__meta">
              {{ currentFamily.memberCount }} 位家人 · {{ currentFamily.albumCount }} 个相册
            </text>
          </view>
          <wd-icon
            :name="families.length > 1 ? 'arrow-down' : 'arrow-right'"
            size="15"
            color="#fff"
          />
        </view>
        <view v-else-if="isLogged" class="family-pill" @click="goFamilyManage">
          <view class="family-pill__icon"><wd-icon name="add" size="18" color="#fff" /></view>
          <view class="family-pill__body">
            <text class="family-pill__name">创建你的第一个家庭</text>
            <text class="family-pill__meta">邀请家人，一起记录生活</text>
          </view>
          <wd-icon name="arrow-right" size="15" color="#fff" />
        </view>
        <view v-else class="hero__login" @click="goLogin">
          登录并创建家庭
          <wd-icon name="arrow-right" size="14" color="#fff" />
        </view>
      </view>
    </view>

    <view class="home-body">
      <view class="quick-card">
        <view
          v-for="action in quickActions"
          :key="action.key"
          class="quick-item"
          @click="handleQuickAction(action.key)"
        >
          <view class="quick-item__icon" :style="{ background: action.background }">
            <wd-icon :name="action.icon" size="25" :color="action.color" />
          </view>
          <text>{{ action.label }}</text>
        </view>
      </view>

      <view v-if="pageLoading && isLogged" class="loading-state">
        <wd-loading color="#7668de" />
        <text>正在整理家庭首页...</text>
      </view>

      <template v-else-if="isLogged && currentFamily">
        <view class="family-overview" @click="goFamilyDetail">
          <view class="section-heading">
            <view>
              <text class="section-title">今天的家庭</text>
              <text class="section-desc">
                {{ currentFamily.description || "一起记录家庭里的每一件小事" }}
              </text>
            </view>
            <view class="circle-arrow">
              <wd-icon name="arrow-right" size="17" color="#7567dc" />
            </view>
          </view>
          <view class="stats">
            <view class="stat">
              <text>{{ currentFamily.memberCount }}</text>
              <text>家庭成员</text>
            </view>
            <view class="stat-line" />
            <view class="stat">
              <text>{{ albums.length }}</text>
              <text>家庭相册</text>
            </view>
            <view class="stat-line" />
            <view class="stat">
              <text>{{ totalMemoryCount }}</text>
              <text>珍藏回忆</text>
            </view>
          </view>
        </view>

        <view class="content-card">
          <view class="section-heading card-heading">
            <view>
              <text class="section-title">最近回忆</text>
              <text class="section-desc">刚刚发生的温暖瞬间</text>
            </view>
            <text class="section-more" @click="goAlbum">查看全部</text>
          </view>
          <scroll-view
            v-if="recentMoments.length"
            class="memory-scroll"
            scroll-x
            enable-flex
            :show-scrollbar="false"
          >
            <view class="memory-row">
              <view
                v-for="moment in recentMoments"
                :key="moment.id"
                class="memory"
                @click="openMoment(moment)"
              >
                <image
                  v-if="moment.mediaType !== 'AUDIO'"
                  class="memory__cover"
                  :src="moment.thumbnailPreviewUrl || moment.previewUrl"
                  mode="aspectFill"
                  lazy-load
                />
                <view v-else class="memory__cover memory__audio">
                  <wd-icon name="voice" size="34" color="#fff" />
                </view>
                <view class="memory__mask" />
                <view v-if="moment.mediaType !== 'IMAGE'" class="memory__type">
                  <wd-icon
                    :name="moment.mediaType === 'VIDEO' ? 'play-circle' : 'voice'"
                    size="15"
                    color="#fff"
                  />
                </view>
                <view class="memory__meta">
                  <text>{{ moment.description || moment.originalName || "家庭时刻" }}</text>
                  <text>{{ formatMomentDate(moment) }}</text>
                </view>
              </view>
            </view>
          </scroll-view>
          <view v-else class="empty-row" @click="goPublish">
            <view class="empty-row__icon"><wd-icon name="camera" size="29" color="#806fda" /></view>
            <view>
              <text>还没有家庭回忆</text>
              <text>上传第一张照片，留下此刻</text>
            </view>
            <wd-icon name="arrow-right" size="15" color="#aaa2b0" />
          </view>
        </view>

        <view class="content-card">
          <view class="section-heading card-heading">
            <view>
              <text class="section-title">近期计划</text>
              <text class="section-desc">重要的日子，不再错过</text>
            </view>
            <text class="section-more" @click="goPlan">查看全部</text>
          </view>
          <view v-if="upcomingSchedules.length" class="schedule-list">
            <view
              v-for="schedule in upcomingSchedules"
              :key="schedule.id"
              class="schedule"
              @click="openSchedule(schedule)"
            >
              <view class="schedule__date">
                <text>{{ scheduleDisplay(schedule).month }}</text>
                <text>{{ scheduleDisplay(schedule).day }}</text>
              </view>
              <view class="schedule__body">
                <view>
                  <text class="schedule__title">{{ schedule.title }}</text>
                  <text class="schedule__type">{{ schedule.typeLabel }}</text>
                </view>
                <text class="schedule__time">
                  {{ scheduleDisplay(schedule).relative }} · {{ scheduleDisplay(schedule).time }}
                </text>
              </view>
              <wd-icon name="arrow-right" size="15" color="#aaa2b0" />
            </view>
          </view>
          <view v-else class="empty-row" @click="goPlan">
            <view class="empty-row__icon empty-row__icon--pink">
              <wd-icon name="calendar" size="28" color="#d47796" />
            </view>
            <view>
              <text>近期没有安排</text>
              <text>创建聚餐、生日或纪念日提醒</text>
            </view>
            <wd-icon name="arrow-right" size="15" color="#aaa2b0" />
          </view>
        </view>

        <view class="content-card">
          <view class="section-heading card-heading">
            <view>
              <text class="section-title">家庭动态与通知</text>
              <text class="section-desc">看看最近发生了什么</text>
            </view>
            <text v-if="noticeList.length" class="section-more" @click="openNoticeList">
              全部通知
            </text>
          </view>
          <view v-if="activityItems.length" class="activity-list">
            <view
              v-for="item in activityItems"
              :key="item.key"
              class="activity"
              @click="handleActivityClick(item)"
            >
              <view class="activity__icon" :class="`activity__icon--${item.kind}`">
                <wd-icon :name="item.icon" size="19" :color="item.color" />
              </view>
              <view class="activity__body">
                <text>{{ item.title }}</text>
                <text>{{ item.time }}</text>
              </view>
              <wd-icon name="arrow-right" size="14" color="#b1aab7" />
            </view>
          </view>
          <view v-else class="activity-empty">
            <wd-icon name="check" size="20" color="#7567dc" />
            <text>一切都好，暂时没有新动态</text>
          </view>
        </view>
      </template>

      <view v-else class="welcome-card">
        <view class="welcome-card__icon">
          <wd-icon :name="isLogged ? 'home' : 'user'" size="45" color="#fff" />
        </view>
        <text class="welcome-card__title">
          {{ isLogged ? "建立属于你们的家庭空间" : "登录后开启家庭时光" }}
        </text>
        <text class="welcome-card__desc">
          {{
            isLogged
              ? "创建家庭后，可以共享相册、安排计划并邀请家人加入。"
              : "和家人一起上传照片、记录计划，把温暖的瞬间永久保存。"
          }}
        </text>
        <wd-button type="primary" round @click="isLogged ? goFamilyManage() : goLogin()">
          {{ isLogged ? "创建家庭" : "立即登录" }}
        </wd-button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import dayjs from "dayjs";
import { onLoad, onShow } from "@dcloudio/uni-app";
import AlbumAPI, { type AlbumMoment } from "@/api/album";
import FamilyAPI, { type FamilyAlbum, type FamilyInfo, type FamilyInvite } from "@/api/family";
import FamilyCardAPI, { type FamilySchedule } from "@/api/family-card";
import NoticeAPI, { type NoticeItem } from "@/api/notice";
import { useNavbar } from "@/composables/useNavbar";
import { useUserStore } from "@/store";
import { ALBUM_NAVIGATION_TARGET_KEY, PENDING_FAMILY_INVITE_KEY } from "@/constants";
import { isLoggedIn } from "@/utils/auth";
import { Storage } from "@/utils/storage";

definePage({
  name: "home",
  style: { navigationStyle: "custom", backgroundColor: "#f6f3fa" },
  layout: "tabbar",
});

type QuickActionKey = "publish" | "album" | "plan" | "invite";
interface ActivityItem {
  key: string;
  kind: "memory" | "notice";
  icon: string;
  color: string;
  title: string;
  time: string;
  moment?: AlbumMoment;
}

const HOME_FAMILY_ID_KEY = "home-family-id";
const FAMILY_PLAN_TARGET_KEY = "family-plan-target";
const navbar = useNavbar({ hasTabbar: true });
const userStore = useUserStore();
const families = ref<FamilyInfo[]>([]);
const currentFamily = ref<FamilyInfo>();
const albums = ref<FamilyAlbum[]>([]);
const recentMoments = ref<AlbumMoment[]>([]);
const schedules = ref<FamilySchedule[]>([]);
const noticeList = ref<NoticeItem[]>([]);
const pageLoading = ref(false);
const inviteProcessing = ref(false);
let homeLoadVersion = 0;

const quickActions: Array<{
  key: QuickActionKey;
  label: string;
  icon: string;
  color: string;
  background: string;
}> = [
  {
    key: "publish",
    label: "上传回忆",
    icon: "camera",
    color: "#725ed7",
    background: "linear-gradient(145deg,#eee9ff,#e2dbff)",
  },
  {
    key: "album",
    label: "家庭相册",
    icon: "image",
    color: "#d16e8d",
    background: "linear-gradient(145deg,#fff0f4,#ffe2eb)",
  },
  {
    key: "plan",
    label: "家庭计划",
    icon: "calendar",
    color: "#d68849",
    background: "linear-gradient(145deg,#fff5e8,#ffe8cb)",
  },
  {
    key: "invite",
    label: "邀请家人",
    icon: "user-add",
    color: "#428b80",
    background: "linear-gradient(145deg,#e8f8f3,#d5f0e7)",
  },
];

const isLogged = computed(() => isLoggedIn());
const displayName = computed(
  () =>
    userStore.userInfo?.nickname ||
    userStore.userInfo?.username ||
    (isLogged.value ? "家人" : "朋友")
);
const greeting = computed(() => {
  const hour = new Date().getHours();
  return hour < 6
    ? "夜深了"
    : hour < 11
      ? "早上好"
      : hour < 14
        ? "中午好"
        : hour < 18
          ? "下午好"
          : "晚上好";
});
const totalMemoryCount = computed(() =>
  albums.value.reduce((sum, item) => sum + Number(item.assetCount || 0), 0)
);
const heroCover = computed(
  () =>
    currentFamily.value?.coverUrl ||
    recentMoments.value.find((item) => item.mediaType === "IMAGE")?.previewUrl ||
    albums.value.find((item) => item.coverUrl)?.coverUrl ||
    ""
);
const upcomingSchedules = computed(() =>
  schedules.value
    .filter((item) => item.status === 1)
    .sort((a, b) => getScheduleDate(a).valueOf() - getScheduleDate(b).valueOf())
    .slice(0, 3)
);
const activityItems = computed<ActivityItem[]>(() => {
  const memories = recentMoments.value.slice(0, 2).map((moment) => ({
    key: `memory-${moment.id}`,
    kind: "memory" as const,
    icon:
      moment.mediaType === "IMAGE"
        ? "image"
        : moment.mediaType === "VIDEO"
          ? "play-circle"
          : "voice",
    color: "#7567dc",
    title: `${moment.uploaderName || "家庭成员"}上传了${moment.mediaTypeLabel || "新的回忆"}`,
    time: formatRelativeTime(moment.capturedAt || moment.createTime),
    moment,
  }));
  const notices = noticeList.value.slice(0, 2).map((notice) => ({
    key: `notice-${notice.id}`,
    kind: "notice" as const,
    icon: "notification",
    color: "#d47796",
    title: notice.title || "新的通知",
    time: notice.publishTime ? formatRelativeTime(String(notice.publishTime)) : "最近",
  }));
  return [...memories, ...notices].slice(0, 4);
});

onLoad((options) => {
  if (options?.inviteCode)
    Storage.set(PENDING_FAMILY_INVITE_KEY, decodeURIComponent(options.inviteCode));
});
onShow(async () => {
  await processPendingFamilyInvite();
  await loadHomeData();
});

async function loadHomeData() {
  const version = ++homeLoadVersion;
  if (!isLogged.value) {
    resetHomeData();
    return;
  }
  pageLoading.value = true;
  try {
    const [familyList, notices] = await Promise.all([
      FamilyAPI.listMyFamilies(),
      NoticeAPI.getMyNoticePage({ pageNum: 1, pageSize: 3 }).catch(() => ({ list: [], total: 0 })),
    ]);
    if (version !== homeLoadVersion) return;
    families.value = familyList || [];
    noticeList.value = notices.list || [];
    if (!families.value.length) {
      currentFamily.value = undefined;
      resetFamilyContent();
      return;
    }
    const storedId = Storage.get<number | undefined>(HOME_FAMILY_ID_KEY);
    currentFamily.value = families.value.find((item) => item.id === storedId) || families.value[0];
    Storage.set(HOME_FAMILY_ID_KEY, currentFamily.value.id);
    await loadFamilyContent(version);
  } catch (error) {
    if (version !== homeLoadVersion) return;
    console.error("家庭首页加载失败", error);
    uni.showToast({ title: "家庭首页加载失败", icon: "none" });
  } finally {
    if (version === homeLoadVersion) pageLoading.value = false;
  }
}

async function loadFamilyContent(version = homeLoadVersion) {
  const family = currentFamily.value;
  if (!family) {
    resetFamilyContent();
    return;
  }
  const familyId = family.id;
  try {
    const [albumList, scheduleList] = await Promise.all([
      FamilyAPI.listAlbums(familyId),
      FamilyCardAPI.listSchedules(familyId),
    ]);
    if (version !== homeLoadVersion || currentFamily.value?.id !== familyId) return;
    albums.value = albumList || [];
    schedules.value = scheduleList || [];
    if (!albums.value[0]) {
      recentMoments.value = [];
      return;
    }
    const result = await AlbumAPI.getMomentPage({
      pageNum: 1,
      pageSize: 8,
      familyId,
      albumId: albums.value[0].id,
    });
    if (version === homeLoadVersion && currentFamily.value?.id === familyId)
      recentMoments.value = result.list || [];
  } catch (error) {
    if (version !== homeLoadVersion) return;
    console.error("家庭首页内容加载失败", error);
    resetFamilyContent();
  }
}

function resetFamilyContent() {
  albums.value = [];
  recentMoments.value = [];
  schedules.value = [];
}
function resetHomeData() {
  families.value = [];
  currentFamily.value = undefined;
  noticeList.value = [];
  resetFamilyContent();
  pageLoading.value = false;
}

function openFamilySelector() {
  if (!currentFamily.value) return;
  if (families.value.length <= 1) {
    goFamilyDetail();
    return;
  }
  uni.showActionSheet({
    itemList: families.value.map((item) => item.name),
    success: async ({ tapIndex }) => {
      const selected = families.value[tapIndex];
      if (!selected || selected.id === currentFamily.value?.id) return;
      currentFamily.value = selected;
      Storage.set(HOME_FAMILY_ID_KEY, selected.id);
      resetFamilyContent();
      const version = ++homeLoadVersion;
      pageLoading.value = true;
      await loadFamilyContent(version);
      if (version === homeLoadVersion) pageLoading.value = false;
    },
  });
}

function handleQuickAction(key: QuickActionKey) {
  if (!isLogged.value) {
    goLogin();
    return;
  }
  if (!currentFamily.value) {
    goFamilyManage();
    return;
  }
  if (key === "publish") goPublish();
  else if (key === "album") goAlbum();
  else if (key === "plan") goPlan();
  else goFamilyDetail();
}
function goPublish() {
  const family = currentFamily.value,
    album = albums.value[0];
  if (!family || !album) {
    uni.showToast({ title: "请先创建家庭相册", icon: "none" });
    goAlbum();
    return;
  }
  uni.navigateTo({
    url: `/pages/album/publish?familyId=${family.id}&albumId=${album.id}&albumName=${encodeURIComponent(album.name)}`,
  });
}
function goAlbum() {
  if (currentFamily.value && albums.value[0])
    Storage.set(ALBUM_NAVIGATION_TARGET_KEY, {
      familyId: currentFamily.value.id,
      albumId: albums.value[0].id,
    });
  uni.switchTab({ url: "/pages/album/index" });
}
function goPlan() {
  uni.switchTab({ url: "/pages/plan/index" });
}
function openSchedule(schedule: FamilySchedule) {
  if (!currentFamily.value) return;
  uni.setStorageSync(FAMILY_PLAN_TARGET_KEY, {
    familyId: currentFamily.value.id,
    scheduleId: schedule.id,
  });
  goPlan();
}
function goFamilyDetail() {
  if (!currentFamily.value) {
    goFamilyManage();
    return;
  }
  uni.navigateTo({ url: `/pages/mine/family/detail/index?familyId=${currentFamily.value.id}` });
}
function goFamilyManage() {
  uni.navigateTo({ url: "/pages/mine/family/index" });
}
function goLogin() {
  uni.navigateTo({ url: "/pages/login/index" });
}
function openMoment(moment: AlbumMoment) {
  if (moment.mediaType !== "IMAGE") {
    goAlbum();
    return;
  }
  const images = recentMoments.value.filter((item) => item.mediaType === "IMAGE");
  uni.previewImage({ current: moment.previewUrl, urls: images.map((item) => item.previewUrl) });
}
function openNoticeList() {
  uni.navigateTo({ url: "/pages/work/notice/index" });
}
function handleActivityClick(item: ActivityItem) {
  item.kind === "memory" && item.moment ? openMoment(item.moment) : openNoticeList();
}
function formatMomentDate(moment: AlbumMoment) {
  return dayjs(moment.capturedAt || moment.createTime).format("MM月DD日 HH:mm");
}
function formatRelativeTime(value: string) {
  const date = dayjs(value),
    now = dayjs();
  if (!date.isValid()) return "最近";
  if (date.isSame(now, "day")) return `今天 ${date.format("HH:mm")}`;
  if (date.isSame(now.subtract(1, "day"), "day")) return `昨天 ${date.format("HH:mm")}`;
  return date.format("MM月DD日");
}
function getScheduleDate(schedule: FamilySchedule) {
  const original = dayjs(schedule.eventTime);
  if (!schedule.repeatYearly || !original.isValid()) return original;
  const now = dayjs();
  let candidate = original.year(now.year());
  if (candidate.isBefore(now, "minute")) candidate = candidate.add(1, "year");
  return candidate;
}
function scheduleDisplay(schedule: FamilySchedule) {
  const date = getScheduleDate(schedule),
    today = dayjs();
  const relative = date.isSame(today, "day")
    ? "今天"
    : date.isSame(today.add(1, "day"), "day")
      ? "明天"
      : date.isBefore(today, "day")
        ? "已过期"
        : date.format("MM月DD日");
  return {
    month: `${date.month() + 1}月`,
    day: date.format("DD"),
    time: date.format("HH:mm"),
    relative,
  };
}

function confirmFamilyInvite(invite: FamilyInvite) {
  return new Promise<boolean>((resolve) =>
    uni.showModal({
      title: "家庭邀请",
      content: `${invite.inviterName} 邀请你加入「${invite.familyName}」，是否加入？`,
      confirmText: invite.alreadyMember ? "查看家庭" : "确认加入",
      cancelText: "暂不加入",
      success: (result) => resolve(result.confirm),
      fail: () => resolve(false),
    })
  );
}
async function processPendingFamilyInvite() {
  if (inviteProcessing.value) return;
  const code = Storage.get<string>(PENDING_FAMILY_INVITE_KEY, "");
  if (!code) return;
  if (!isLogged.value) {
    uni.navigateTo({
      url: `/pages/login/index?redirect=${encodeURIComponent("/pages/index/index")}`,
    });
    return;
  }
  inviteProcessing.value = true;
  try {
    const invite = await FamilyAPI.getInvite(code);
    if (!(await confirmFamilyInvite(invite))) {
      Storage.remove(PENDING_FAMILY_INVITE_KEY);
      return;
    }
    const family = await FamilyAPI.acceptInvite(code);
    Storage.remove(PENDING_FAMILY_INVITE_KEY);
    Storage.set(HOME_FAMILY_ID_KEY, family.id);
    uni.showToast({ title: invite.alreadyMember ? "即将进入家庭" : "已加入家庭", icon: "success" });
    setTimeout(
      () => uni.navigateTo({ url: `/pages/mine/family/detail/index?familyId=${family.id}` }),
      600
    );
  } catch (error: any) {
    Storage.remove(PENDING_FAMILY_INVITE_KEY);
    uni.showToast({ title: error?.message || "家庭邀请处理失败", icon: "none" });
  } finally {
    inviteProcessing.value = false;
  }
}
</script>

<style lang="scss" scoped>
.home-page {
  min-height: 100vh;
  padding-bottom: 150rpx;
  color: #403849;
  background: #f6f3fa;
}
.hero {
  position: relative;
  min-height: 440rpx;
  overflow: hidden;
  color: #fff;
  background: linear-gradient(145deg, #6556d2, #986fd4 58%, #cf8eb4);
}
.hero__cover,
.hero__mask {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}
.hero__cover {
  opacity: 0.72;
}
.hero__mask {
  background:
    linear-gradient(180deg, rgb(39 28 77/25%), rgb(52 35 83/78%)),
    linear-gradient(135deg, rgb(95 74 199/40%), rgb(210 125 160/30%));
}
.hero__content {
  position: relative;
  z-index: 2;
  box-sizing: border-box;
  min-height: 440rpx;
  padding: 0 32rpx 70rpx;
}
.hero__heading {
  display: flex;
  gap: 24rpx;
  align-items: flex-start;
  justify-content: space-between;
}
.hero__title,
.hero__subtitle {
  display: block;
}
.hero__title {
  font-size: 38rpx;
  font-weight: 800;
}
.hero__subtitle {
  margin-top: 12rpx;
  font-size: 23rpx;
  color: rgb(255 255 255/82%);
}
.hero__avatar {
  width: 70rpx;
  height: 70rpx;
  border: 3rpx solid rgb(255 255 255/70%);
  border-radius: 50%;
}
.hero__avatar--empty {
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgb(255 255 255/16%);
}
.family-pill {
  display: flex;
  gap: 16rpx;
  align-items: center;
  padding: 18rpx 20rpx;
  margin-top: 42rpx;
  background: rgb(255 255 255/14%);
  border: 1rpx solid rgb(255 255 255/24%);
  border-radius: 24rpx;
}
.family-pill__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 58rpx;
  height: 58rpx;
  background: rgb(255 255 255/16%);
  border-radius: 18rpx;
}
.family-pill__body {
  flex: 1;
  min-width: 0;
}
.family-pill__name,
.family-pill__meta {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.family-pill__name {
  font-size: 27rpx;
  font-weight: 700;
}
.family-pill__meta {
  margin-top: 6rpx;
  font-size: 20rpx;
  color: rgb(255 255 255/75%);
}
.hero__login {
  display: flex;
  gap: 8rpx;
  align-items: center;
  justify-content: center;
  width: 270rpx;
  padding: 17rpx;
  margin-top: 42rpx;
  font-size: 24rpx;
  background: rgb(255 255 255/15%);
  border: 1rpx solid rgb(255 255 255/25%);
  border-radius: 999rpx;
}
.home-body {
  position: relative;
  z-index: 3;
  padding: 0 24rpx;
  margin-top: -52rpx;
}
.quick-card {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  padding: 24rpx 12rpx 22rpx;
  background: #fff;
  border-radius: 30rpx;
  box-shadow: 0 16rpx 42rpx rgb(67 48 106/12%);
}
.quick-item {
  display: flex;
  flex-direction: column;
  gap: 13rpx;
  align-items: center;
  font-size: 21rpx;
  color: #5a5263;
}
.quick-item__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 76rpx;
  height: 76rpx;
  border-radius: 24rpx;
}
.loading-state {
  display: flex;
  gap: 16rpx;
  align-items: center;
  justify-content: center;
  padding: 90rpx 0;
  font-size: 22rpx;
  color: #8f879a;
}
.family-overview,
.content-card,
.welcome-card {
  margin-top: 24rpx;
  background: #fff;
  border: 1rpx solid rgb(117 103 220/8%);
  border-radius: 28rpx;
  box-shadow: 0 10rpx 30rpx rgb(55 43 92/5%);
}
.family-overview {
  padding: 28rpx;
}
.section-heading {
  display: flex;
  gap: 20rpx;
  align-items: center;
  justify-content: space-between;
}
.section-title,
.section-desc {
  display: block;
}
.section-title {
  font-size: 29rpx;
  font-weight: 800;
}
.section-desc {
  max-width: 500rpx;
  margin-top: 6rpx;
  overflow: hidden;
  font-size: 20rpx;
  color: #9c94a5;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.circle-arrow {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52rpx;
  height: 52rpx;
  background: #f0edff;
  border-radius: 17rpx;
}
.stats {
  display: flex;
  align-items: center;
  padding-top: 26rpx;
  margin-top: 24rpx;
  border-top: 1rpx solid #eeeaf3;
}
.stat {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 6rpx;
  align-items: center;
}
.stat text:first-child {
  font-size: 35rpx;
  font-weight: 800;
  color: #6f60cf;
}
.stat text:last-child {
  font-size: 19rpx;
  color: #9c94a5;
}
.stat-line {
  width: 1rpx;
  height: 48rpx;
  background: #eeeaf3;
}
.content-card {
  padding: 28rpx 0;
}
.card-heading {
  padding: 0 28rpx 22rpx;
}
.section-more {
  flex-shrink: 0;
  font-size: 21rpx;
  color: #7567dc;
}
.memory-scroll {
  width: 100%;
  white-space: nowrap;
}
.memory-row {
  display: inline-flex;
  gap: 18rpx;
  padding: 0 28rpx;
}
.memory {
  position: relative;
  width: 238rpx;
  height: 286rpx;
  overflow: hidden;
  white-space: normal;
  background: #eeeaf3;
  border-radius: 23rpx;
}
.memory__cover,
.memory__mask {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}
.memory__audio {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(145deg, #7567dc, #c27fae);
}
.memory__mask {
  background: linear-gradient(180deg, transparent 38%, rgb(29 22 43/82%));
}
.memory__type {
  position: absolute;
  top: 14rpx;
  right: 14rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 45rpx;
  height: 45rpx;
  background: rgb(25 21 34/58%);
  border-radius: 50%;
}
.memory__meta {
  position: absolute;
  right: 16rpx;
  bottom: 16rpx;
  left: 16rpx;
  color: #fff;
}
.memory__meta text {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.memory__meta text:first-child {
  font-size: 22rpx;
  font-weight: 650;
}
.memory__meta text:last-child {
  margin-top: 7rpx;
  font-size: 18rpx;
  color: rgb(255 255 255/72%);
}
.empty-row {
  display: flex;
  gap: 18rpx;
  align-items: center;
  margin: 0 28rpx;
  padding: 24rpx;
  background: #f8f6fa;
  border-radius: 22rpx;
}
.empty-row__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 66rpx;
  height: 66rpx;
  background: #ece8ff;
  border-radius: 20rpx;
}
.empty-row__icon--pink {
  background: #fff0f4;
}
.empty-row > view:nth-child(2) {
  flex: 1;
}
.empty-row text {
  display: block;
}
.empty-row text:first-child {
  font-size: 23rpx;
  font-weight: 700;
}
.empty-row text:last-child {
  margin-top: 5rpx;
  font-size: 19rpx;
  color: #9b93a4;
}
.schedule-list,
.activity-list {
  padding: 0 28rpx;
}
.schedule,
.activity {
  display: flex;
  gap: 18rpx;
  align-items: center;
  padding: 18rpx 0;
}
.schedule + .schedule,
.activity + .activity {
  border-top: 1rpx solid #eeeaf3;
}
.schedule__date {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 72rpx;
  height: 78rpx;
  color: #7263d2;
  background: #f0edff;
  border-radius: 19rpx;
}
.schedule__date text:first-child {
  font-size: 17rpx;
}
.schedule__date text:last-child {
  font-size: 29rpx;
  font-weight: 800;
}
.schedule__body,
.activity__body {
  flex: 1;
  min-width: 0;
}
.schedule__body > view {
  display: flex;
  gap: 10rpx;
  align-items: center;
}
.schedule__title {
  max-width: 70%;
  overflow: hidden;
  font-size: 23rpx;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.schedule__type {
  padding: 4rpx 9rpx;
  font-size: 17rpx;
  color: #8b7cda;
  background: #f1eeff;
  border-radius: 999rpx;
}
.schedule__time {
  display: block;
  margin-top: 8rpx;
  font-size: 19rpx;
  color: #9b94a3;
}
.activity {
  gap: 16rpx;
}
.activity__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 58rpx;
  height: 58rpx;
  border-radius: 18rpx;
}
.activity__icon--memory {
  background: #efecff;
}
.activity__icon--notice {
  background: #fff0f4;
}
.activity__body text {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.activity__body text:first-child {
  font-size: 22rpx;
}
.activity__body text:last-child {
  margin-top: 5rpx;
  font-size: 18rpx;
  color: #a099a7;
}
.activity-empty {
  display: flex;
  gap: 12rpx;
  align-items: center;
  margin: 0 28rpx;
  padding: 22rpx;
  font-size: 21rpx;
  color: #877d91;
  background: #f7f5fa;
  border-radius: 20rpx;
}
.welcome-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 62rpx 42rpx;
  text-align: center;
}
.welcome-card__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 116rpx;
  height: 116rpx;
  background: linear-gradient(145deg, #7567dc, #aa78d1);
  border-radius: 36rpx;
}
.welcome-card__title {
  margin-top: 28rpx;
  font-size: 30rpx;
  font-weight: 800;
}
.welcome-card__desc {
  margin: 14rpx 0 28rpx;
  font-size: 22rpx;
  line-height: 1.7;
  color: #938b9c;
}
</style>
