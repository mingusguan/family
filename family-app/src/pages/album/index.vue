<template>
  <view class="album-page page--tabbar">
    <view class="album-hero" :style="{ paddingTop: `${navbar.totalHeight.value}px` }">
      <custom-navbar
        title="家庭相册"
        bg-color="transparent"
        title-color="#ffffff"
        icon-color="#ffffff"
        :show-back="false"
        :placeholder="false"
      />
      <view class="album-hero__content">
        <text class="album-hero__eyebrow">FAMILY MEMORIES</text>
        <text class="album-hero__title">收藏属于家人的共同回忆</text>
        <view v-if="currentFamily" class="family-selector" @click="selectFamily">
          <wd-icon name="home" size="17" color="#ffffff" />
          <text>{{ currentFamily.name }}</text>
          <wd-icon v-if="families.length > 1" name="arrow-down" size="14" color="#ffffff" />
        </view>
      </view>
    </view>

    <view v-if="!isLogged" class="empty-state">
      <view class="empty-state__icon"><wd-icon name="user" size="52rpx" color="#7668de" /></view>
      <text class="empty-state__title">登录后查看家庭相册</text>
      <text class="empty-state__desc">与家人一起上传照片、视频和声音</text>
      <wd-button type="primary" round @click="goLogin">立即登录</wd-button>
    </view>

    <view v-else-if="pageLoading" class="page-loading">
      <wd-loading color="#7668de" />
      <text>正在整理家庭回忆...</text>
    </view>

    <view v-else-if="families.length === 0" class="empty-state">
      <view class="empty-state__icon"><wd-icon name="home" size="52rpx" color="#7668de" /></view>
      <text class="empty-state__title">还没有所属家庭</text>
      <text class="empty-state__desc">请先在“我的”中创建家庭，再建立共享相册</text>
      <wd-button type="primary" round @click="goFamilyManage">去创建家庭</wd-button>
    </view>

    <template v-else>
      <view class="album-section">
        <view class="section-heading">
          <view>
            <text class="section-heading__title">家庭相册</text>
            <text class="section-heading__subtitle">选择一个相册查看内容</text>
          </view>
          <view class="section-heading__action" @click="createAlbum">
            <wd-icon name="add" size="16" color="#7567dc" />
            <text>新建相册</text>
          </view>
        </view>

        <scroll-view v-if="albums.length > 0" class="album-scroll" scroll-x enable-flex>
          <view class="album-row">
            <view
              v-for="album in albums"
              :key="album.id"
              class="album-card"
              :class="{ 'album-card--active': currentAlbum?.id === album.id }"
              @click="changeAlbum(album)"
            >
              <image
                v-if="album.coverUrl"
                class="album-card__cover"
                :src="album.coverUrl"
                mode="aspectFill"
              />
              <view v-else class="album-card__cover album-card__cover--empty">
                <wd-icon name="image" size="40rpx" color="#ffffff" />
              </view>
              <view class="album-card__shade" />
              <view class="album-card__meta">
                <text class="album-card__name">{{ album.name }}</text>
                <text class="album-card__count">{{ album.assetCount }} 项内容</text>
              </view>
            </view>
          </view>
        </scroll-view>

        <view v-else class="album-empty" @click="createAlbum">
          <wd-icon name="add-circle" size="48rpx" color="#8578df" />
          <text class="album-empty__title">这个家庭还没有相册</text>
          <text class="album-empty__desc">点击创建第一个家庭相册</text>
        </view>
      </view>

      <view v-if="currentAlbum" class="moment-section">
        <view class="section-heading section-heading--moments">
          <view>
            <text class="section-heading__title">{{ currentAlbum.name }}</text>
            <text class="section-heading__subtitle">{{ total }} 个家庭时刻</text>
          </view>
          <view class="scope-toggle" @click="toggleMine">
            <wd-icon :name="mineOnly ? 'user' : 'view'" size="15" color="#7567dc" />
            <text>{{ mineOnly ? "只看我的" : "全部成员" }}</text>
          </view>
        </view>

        <view v-if="momentsLoading && moments.length === 0" class="moment-loading">
          <wd-loading color="#7668de" />
        </view>

        <view v-else-if="moments.length === 0" class="moment-empty">
          <view class="moment-empty__visual">
            <wd-icon name="image" size="56rpx" color="#9b91dd" />
          </view>
          <text class="moment-empty__title">相册还是空的</text>
          <text class="moment-empty__desc">上传照片、视频或一段声音，留下第一个回忆</text>
          <wd-button type="primary" round size="small" @click="goPublish">上传内容</wd-button>
        </view>

        <view v-else class="moment-grid">
          <view v-for="(moment, index) in moments" :key="moment.id" class="moment-card">
            <image
              v-if="moment.mediaType === 'IMAGE'"
              class="moment-card__media"
              :src="moment.thumbnailPreviewUrl || moment.previewUrl"
              mode="aspectFill"
              lazy-load
              @click="previewImage(index)"
            />
            <video
              v-else-if="moment.mediaType === 'VIDEO'"
              class="moment-card__media"
              :src="moment.previewUrl"
              :poster="moment.thumbnailPreviewUrl"
              controls
              object-fit="cover"
            />
            <view v-else class="moment-card__audio" @click="playAudio(moment)">
              <view class="audio-disc">
                <wd-icon name="play-circle" size="42rpx" color="#ffffff" />
              </view>
              <text>播放声音</text>
              <text v-if="moment.duration" class="moment-card__duration">
                {{ formatDuration(moment.duration) }}
              </text>
            </view>

            <view class="moment-card__body">
              <text v-if="moment.description" class="moment-card__description">
                {{ moment.description }}
              </text>
              <view v-if="moment.tags?.length" class="moment-card__tags">
                <text v-for="tag in moment.tags" :key="tag.id">#{{ tag.name }}</text>
              </view>
              <view class="moment-card__footer">
                <text>{{ moment.uploaderName || "家庭成员" }}</text>
                <text>{{ formatDate(moment.capturedAt || moment.createTime) }}</text>
              </view>
            </view>
          </view>
        </view>

        <view v-if="moments.length > 0" class="load-more" @click="loadMore">
          <text v-if="momentsLoading">正在加载...</text>
          <text v-else-if="finished">已经看到全部回忆啦</text>
          <text v-else>继续加载</text>
        </view>
      </view>

      <view
        v-if="currentAlbum"
        class="publish-fab"
        hover-class="publish-fab--pressed"
        :hover-stay-time="80"
        @click="goPublish"
      >
        <view class="publish-fab__icon">
          <wd-icon name="camera-fill" size="30rpx" color="#ffffff" />
        </view>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import dayjs from "dayjs";
import { onPullDownRefresh, onReachBottom, onShow } from "@dcloudio/uni-app";
import AlbumAPI, { type AlbumMoment } from "@/api/album";
import FamilyAPI, { type FamilyAlbum, type FamilyInfo } from "@/api/family";
import { ALBUM_NAVIGATION_TARGET_KEY } from "@/constants";
import { useNavbar } from "@/composables/useNavbar";
import { isLoggedIn } from "@/utils/auth";
import { Storage } from "@/utils/storage";

const PAGE_SIZE = 12;
const navbar = useNavbar({ hasTabbar: true });
const families = ref<FamilyInfo[]>([]);
const albums = ref<FamilyAlbum[]>([]);
const currentFamily = ref<FamilyInfo>();
const currentAlbum = ref<FamilyAlbum>();
const moments = ref<AlbumMoment[]>([]);
const pageNum = ref(1);
const total = ref(0);
const pageLoading = ref(false);
const momentsLoading = ref(false);
const initialized = ref(false);
const mineOnly = ref(false);
const isLogged = computed(() => isLoggedIn());
const finished = computed(() => initialized.value && moments.value.length >= total.value);
let audioContext: UniApp.InnerAudioContext | undefined;

definePage({
  name: "album",
  style: {
    navigationStyle: "custom",
    enablePullDownRefresh: true,
    backgroundColor: "#f5f3fa",
  },
  layout: "tabbar",
});

async function initialize() {
  if (!isLogged.value) {
    resetAll();
    return;
  }
  pageLoading.value = true;
  try {
    const navigationTarget = Storage.get<{ familyId: number; albumId: number } | undefined>(
      ALBUM_NAVIGATION_TARGET_KEY
    );
    if (navigationTarget) Storage.remove(ALBUM_NAVIGATION_TARGET_KEY);
    families.value = await FamilyAPI.listMyFamilies();
    if (families.value.length === 0) {
      resetAlbumData();
      return;
    }
    const selectedFamilyId = navigationTarget?.familyId || currentFamily.value?.id;
    const selected = families.value.find((item) => item.id === selectedFamilyId);
    currentFamily.value = selected || families.value[0];
    await loadAlbums(navigationTarget?.albumId);
  } catch (error) {
    console.error("初始化家庭相册失败", error);
    uni.showToast({ title: "家庭相册加载失败", icon: "none" });
  } finally {
    pageLoading.value = false;
    uni.stopPullDownRefresh();
  }
}

async function loadAlbums(preferredAlbumId?: number) {
  if (!currentFamily.value) return;
  albums.value = await FamilyAPI.listAlbums(currentFamily.value.id);
  const selectedAlbumId = preferredAlbumId || currentAlbum.value?.id;
  const selected = albums.value.find((item) => item.id === selectedAlbumId);
  currentAlbum.value = selected || albums.value[0];
  if (currentAlbum.value) {
    await loadMoments(true);
  } else {
    moments.value = [];
    total.value = 0;
  }
}

async function loadMoments(reset = false) {
  if (!currentFamily.value || !currentAlbum.value || momentsLoading.value) return;
  if (!reset && finished.value) return;
  momentsLoading.value = true;
  if (reset) pageNum.value = 1;
  try {
    const result = await AlbumAPI.getMomentPage({
      pageNum: pageNum.value,
      pageSize: PAGE_SIZE,
      familyId: currentFamily.value.id,
      albumId: currentAlbum.value.id,
      mine: mineOnly.value,
    });
    moments.value = reset ? result.list : moments.value.concat(result.list);
    total.value = result.total;
    initialized.value = true;
  } catch (error) {
    console.error("读取相册内容失败", error);
    uni.showToast({ title: "相册内容加载失败", icon: "none" });
  } finally {
    momentsLoading.value = false;
    uni.stopPullDownRefresh();
  }
}

function selectFamily() {
  if (families.value.length <= 1) return;
  uni.showActionSheet({
    itemList: families.value.map((item) => item.name),
    success: async (result) => {
      currentFamily.value = families.value[result.tapIndex];
      currentAlbum.value = undefined;
      await loadAlbums();
    },
  });
}

function changeAlbum(album: FamilyAlbum) {
  if (currentAlbum.value?.id === album.id) return;
  currentAlbum.value = album;
  moments.value = [];
  initialized.value = false;
  loadMoments(true);
}

function createAlbum() {
  if (!currentFamily.value) return;
  uni.showModal({
    title: "新建相册",
    editable: true,
    placeholderText: "例如：宝贝成长记",
    confirmText: "创建",
    success: async (result) => {
      const name = (result.content || "").trim();
      if (!result.confirm) return;
      if (!name) {
        uni.showToast({ title: "请输入相册名称", icon: "none" });
        return;
      }
      try {
        const album = await FamilyAPI.createAlbum(currentFamily.value!.id, { name });
        await loadAlbums();
        currentAlbum.value = albums.value.find((item) => item.id === album.id) || album;
        await loadMoments(true);
        uni.showToast({ title: "相册创建成功", icon: "success" });
      } catch (error: any) {
        uni.showToast({ title: error?.message || "创建失败", icon: "none" });
      }
    },
  });
}

function goPublish() {
  if (!currentFamily.value || !currentAlbum.value) return;
  uni.navigateTo({
    url:
      "/pages/album/publish?familyId=" +
      currentFamily.value.id +
      "&albumId=" +
      currentAlbum.value.id +
      "&albumName=" +
      encodeURIComponent(currentAlbum.value.name),
  });
}

function toggleMine() {
  mineOnly.value = !mineOnly.value;
  moments.value = [];
  initialized.value = false;
  loadMoments(true);
}

function loadMore() {
  if (momentsLoading.value || finished.value) return;
  pageNum.value += 1;
  loadMoments().catch(() => {
    pageNum.value -= 1;
  });
}

function previewImage(index: number) {
  const images = moments.value.filter((item) => item.mediaType === "IMAGE");
  const current = moments.value[index];
  uni.previewImage({
    current: current.previewUrl,
    urls: images.map((item) => item.previewUrl),
  });
}

function playAudio(moment: AlbumMoment) {
  if (audioContext) audioContext.destroy();
  audioContext = uni.createInnerAudioContext();
  audioContext.src = moment.previewUrl;
  audioContext.play();
  uni.showToast({ title: "正在播放声音", icon: "none" });
}

function formatDuration(duration: number) {
  const seconds = Math.round(duration / 1000);
  const minutes = Math.floor(seconds / 60);
  return String(minutes).padStart(2, "0") + ":" + String(seconds % 60).padStart(2, "0");
}

function formatDate(value: string) {
  return dayjs(value).format("MM月DD日 HH:mm");
}

function goLogin() {
  uni.navigateTo({ url: "/pages/login/index" });
}

function goFamilyManage() {
  uni.navigateTo({ url: "/pages/mine/family/index" });
}

function resetAlbumData() {
  albums.value = [];
  currentFamily.value = undefined;
  currentAlbum.value = undefined;
  moments.value = [];
  total.value = 0;
}

function resetAll() {
  families.value = [];
  resetAlbumData();
}

onShow(initialize);
onPullDownRefresh(initialize);
onReachBottom(loadMore);
</script>

<style lang="scss" scoped>
.album-page {
  min-height: 100vh;
  padding-bottom: 150rpx;
  background: #f5f3fa;
}

.album-hero {
  min-height: 350rpx;
  color: #fff;
  background: linear-gradient(145deg, #6556d2, #9d6fd4 58%, #ce8fb7);
  border-radius: 0 0 52rpx 52rpx;
}

.album-hero__content {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 20rpx 38rpx 48rpx;
}

.album-hero__eyebrow {
  font-size: 20rpx;
  letter-spacing: 5rpx;
  opacity: 0.7;
}

.album-hero__title {
  margin-top: 18rpx;
  font-size: 39rpx;
  font-weight: 720;
}

.family-selector {
  display: flex;
  gap: 12rpx;
  align-items: center;
  padding: 13rpx 20rpx;
  margin-top: 28rpx;
  font-size: 24rpx;
  background: rgb(255 255 255 / 17%);
  border: 1rpx solid rgb(255 255 255 / 22%);
  border-radius: 999rpx;
}

.empty-state,
.page-loading,
.moment-empty,
.album-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.empty-state {
  padding: 100rpx 38rpx;
  margin: 34rpx 28rpx;
  background: #fff;
  border-radius: 34rpx;
}

.empty-state__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 110rpx;
  height: 110rpx;
  margin-bottom: 26rpx;
  background: #efecff;
  border-radius: 36rpx;
}

.empty-state__title,
.moment-empty__title {
  font-size: 31rpx;
  font-weight: 650;
  color: #3d3748;
}

.empty-state__desc,
.moment-empty__desc {
  margin: 14rpx 0 28rpx;
  font-size: 24rpx;
  color: #9790a2;
  text-align: center;
}

.page-loading {
  flex-direction: row;
  gap: 20rpx;
  padding: 120rpx 0;
  color: #8e8799;
}

.album-section,
.moment-section {
  padding: 34rpx 28rpx 0;
}

.section-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.section-heading__title,
.section-heading__subtitle {
  display: block;
}

.section-heading__title {
  font-size: 31rpx;
  font-weight: 680;
  color: #383242;
}

.section-heading__subtitle {
  margin-top: 7rpx;
  font-size: 22rpx;
  color: #9891a2;
}

.section-heading__action,
.scope-toggle {
  display: flex;
  gap: 8rpx;
  align-items: center;
  padding: 12rpx 18rpx;
  font-size: 22rpx;
  color: #7567dc;
  background: #ebe8fc;
  border-radius: 999rpx;
}

.album-scroll {
  width: 100%;
  white-space: nowrap;
}

.album-row {
  display: flex;
  gap: 20rpx;
  padding: 2rpx 2rpx 18rpx;
}

.album-card {
  position: relative;
  flex-shrink: 0;
  width: 280rpx;
  height: 190rpx;
  overflow: hidden;
  border: 4rpx solid transparent;
  border-radius: 28rpx;
  box-shadow: 0 15rpx 34rpx rgb(56 43 92 / 12%);
}

.album-card--active {
  border-color: #8678e5;
}

.album-card__cover,
.album-card__shade {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.album-card__cover--empty {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(145deg, #8d80e7, #d59bbb);
}

.album-card__shade {
  background: linear-gradient(0deg, rgb(27 20 48 / 70%), transparent 70%);
}

.album-card__meta {
  position: absolute;
  right: 22rpx;
  bottom: 18rpx;
  left: 22rpx;
  color: #fff;
}

.album-card__name,
.album-card__count {
  display: block;
}

.album-card__name {
  overflow: hidden;
  font-size: 27rpx;
  font-weight: 650;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.album-card__count {
  margin-top: 7rpx;
  font-size: 20rpx;
  opacity: 0.8;
}

.album-empty {
  padding: 58rpx 20rpx;
  background: #fff;
  border: 2rpx dashed #d8d2ef;
  border-radius: 30rpx;
}

.album-empty__title {
  margin-top: 18rpx;
  font-size: 27rpx;
  font-weight: 600;
  color: #4e4759;
}

.album-empty__desc {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #9891a2;
}

.moment-section {
  padding-top: 42rpx;
}

.moment-loading {
  display: flex;
  justify-content: center;
  padding: 80rpx 0;
}

.moment-empty {
  padding: 64rpx 28rpx;
  background: #fff;
  border-radius: 32rpx;
}

.moment-empty__visual {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 106rpx;
  height: 106rpx;
  margin-bottom: 22rpx;
  background: #efecff;
  border-radius: 34rpx;
}

.moment-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20rpx;
}

.moment-card {
  overflow: hidden;
  background: #fff;
  border-radius: 26rpx;
  box-shadow: 0 12rpx 32rpx rgb(55 43 92 / 8%);
}

.moment-card__media,
.moment-card__audio {
  width: 100%;
  height: 270rpx;
}

.moment-card__audio {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  align-items: center;
  justify-content: center;
  font-size: 23rpx;
  color: #fff;
  background: linear-gradient(145deg, #796bdd, #bc78bd);
}

.audio-disc {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 92rpx;
  height: 92rpx;
  background: rgb(255 255 255 / 18%);
  border: 2rpx solid rgb(255 255 255 / 30%);
  border-radius: 50%;
}

.moment-card__duration {
  font-size: 19rpx;
  opacity: 0.72;
}

.moment-card__body {
  padding: 18rpx;
}

.moment-card__description {
  display: -webkit-box;
  overflow: hidden;
  font-size: 23rpx;
  line-height: 1.5;
  color: #46404f;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.moment-card__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-top: 12rpx;
  font-size: 20rpx;
  color: #796bdd;
}

.moment-card__footer {
  display: flex;
  justify-content: space-between;
  margin-top: 14rpx;
  font-size: 18rpx;
  color: #a09aa8;
}

.load-more {
  padding: 34rpx 0;
  font-size: 22rpx;
  color: #9991a3;
  text-align: center;
}

.publish-fab {
  position: fixed;
  right: 28rpx;
  bottom: calc(var(--window-bottom) + 146rpx);
  z-index: 20;
  display: flex;
  box-sizing: border-box;
  align-items: center;
  justify-content: center;
  width: 76rpx;
  height: 76rpx;
  padding: 0;
  background: linear-gradient(135deg, #7162dc 0%, #8b6de1 55%, #ac79d2 100%);
  border: 2rpx solid rgb(255 255 255 / 72%);
  border-radius: 999rpx;
  box-shadow:
    0 14rpx 32rpx rgb(88 67 184 / 28%),
    0 3rpx 8rpx rgb(69 50 146 / 16%);
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.publish-fab--pressed {
  box-shadow: 0 8rpx 20rpx rgb(88 67 184 / 24%);
  transform: scale(0.95);
}

.publish-fab__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 46rpx;
  height: 46rpx;
  background: rgb(255 255 255 / 17%);
  border: 1rpx solid rgb(255 255 255 / 24%);
  border-radius: 50%;
}

</style>
