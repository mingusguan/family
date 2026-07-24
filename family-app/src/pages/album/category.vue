<template>
  <view class="category-page">
    <custom-navbar title="分类浏览" placeholder />

    <view class="filter-panel">
      <view class="filter-row">
        <text class="filter-row__label">内容类型</text>
        <view class="media-options">
          <view
            v-for="item in mediaOptions"
            :key="item.value"
            class="media-option"
            :class="{ 'media-option--active': selectedMediaTypes.includes(item.value) }"
            @click="toggleMediaType(item.value)"
          >
            <wd-icon
              :name="item.icon"
              size="17"
              :color="selectedMediaTypes.includes(item.value) ? '#ffffff' : '#7567dc'"
            />
            <text>{{ item.label }}</text>
          </view>
        </view>
      </view>

      <view class="filter-row filter-row--month">
        <view class="month-heading">
          <view>
            <text class="filter-row__label">年月分类</text>
            <text class="month-heading__hint">不选择月份时展示全部年月</text>
          </view>
          <picker mode="date" fields="year" :value="`${filterYear}-01-01`" @change="changeYear">
            <view class="year-picker">
              <text>{{ filterYear }} 年</text>
              <wd-icon name="arrow-down" size="12" color="#7567dc" />
            </view>
          </picker>
        </view>

        <view class="month-grid">
          <view
            v-for="month in months"
            :key="month.value"
            class="month-option"
            :class="{ 'month-option--active': selectedMonths.includes(month.value) }"
            @click="toggleMonth(month.value)"
          >
            {{ month.number }}月
          </view>
        </view>

        <view v-if="selectedMonths.length" class="selected-filter">
          <text>已选 {{ selectedMonths.length }} 个月份</text>
          <text @click="clearMonths">清除月份</text>
        </view>
      </view>
    </view>

    <view class="result-section">
      <view class="result-heading">
        <view>
          <text class="result-heading__title">分类内容</text>
          <text class="result-heading__subtitle">按年月倒序展示</text>
        </view>
        <text class="result-heading__count">{{ total }} 项</text>
      </view>

      <view v-if="loading && moments.length === 0" class="result-loading">
        <wd-loading color="#7668de" />
        <text>正在整理相册内容...</text>
      </view>

      <view v-else-if="monthGroups.length === 0" class="result-empty">
        <view class="result-empty__icon">
          <wd-icon name="image" size="42" color="#9488df" />
        </view>
        <text class="result-empty__title">没有符合条件的内容</text>
        <text class="result-empty__desc">可以更换月份或内容类型再试试</text>
      </view>

      <view v-else class="month-groups">
        <view v-for="group in monthGroups" :key="group.key" class="month-group">
          <view class="month-group__heading">
            <text class="month-group__title">{{ group.label }}</text>
            <text class="month-group__count">{{ group.items.length }} 项</text>
          </view>

          <scroll-view class="moment-scroll" scroll-x enable-flex :show-scrollbar="false">
            <view class="moment-row">
              <view v-for="moment in group.items" :key="moment.id" class="moment-card">
                <view class="moment-card__visual">
                  <image
                    v-if="moment.mediaType === 'IMAGE'"
                    class="moment-card__media"
                    :src="moment.thumbnailPreviewUrl || moment.previewUrl"
                    mode="aspectFill"
                    lazy-load
                    @click="previewImage(moment)"
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
                      <wd-icon name="voice" size="30" color="#ffffff" />
                    </view>
                    <text>点击播放音频</text>
                  </view>
                  <cover-view class="moment-card__badge">
                    {{ formatMediaMeta(moment) }}
                  </cover-view>
                </view>

                <view class="moment-card__body">
                  <text class="moment-card__description">
                    {{ moment.description || moment.originalName || "家庭时刻" }}
                  </text>
                  <view class="moment-card__footer">
                    <text>{{ formatMomentTime(moment) }}</text>
                    <text v-if="moment.tags?.length">#{{ moment.tags[0].name }}</text>
                  </view>
                </view>
              </view>
            </view>
          </scroll-view>
        </view>
      </view>

      <view v-if="loadingMore" class="load-more"><wd-loading color="#7668de" /></view>
      <text v-else-if="finished && moments.length" class="load-more">已经看到全部内容啦</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import dayjs from "dayjs";
import { onLoad, onReachBottom, onUnload } from "@dcloudio/uni-app";
import AlbumAPI, { type AlbumMediaType, type AlbumMoment } from "@/api/album";

definePage({
  name: "album-category",
  style: {
    navigationStyle: "custom",
    backgroundColor: "#f5f3fa",
    onReachBottomDistance: 120,
  },
});

const PAGE_SIZE = 60;
const familyId = ref(0);
const albumId = ref(0);
const filterYear = ref(new Date().getFullYear());
const selectedMonths = ref<string[]>([]);
const selectedMediaTypes = ref<AlbumMediaType[]>(["IMAGE"]);
const moments = ref<AlbumMoment[]>([]);
const pageNum = ref(1);
const total = ref(0);
const loading = ref(false);
const loadingMore = ref(false);
let filterVersion = 0;
let audioContext: UniApp.InnerAudioContext | undefined;

const mediaOptions: Array<{ value: AlbumMediaType; label: string; icon: string }> = [
  { value: "IMAGE", label: "图片", icon: "image" },
  { value: "VIDEO", label: "视频", icon: "play-circle" },
  { value: "AUDIO", label: "音频", icon: "voice" },
];

const months = computed(() =>
  Array.from({ length: 12 }, (_, index) => {
    const number = index + 1;
    return {
      number,
      value: `${filterYear.value}-${String(number).padStart(2, "0")}`,
    };
  })
);

const monthGroups = computed(() => {
  const groups = new Map<string, AlbumMoment[]>();
  for (const moment of moments.value) {
    const key = dayjs(moment.capturedAt || moment.createTime).format("YYYY-MM");
    const items = groups.get(key) || [];
    items.push(moment);
    groups.set(key, items);
  }
  return [...groups.entries()]
    .sort(([left], [right]) => right.localeCompare(left))
    .map(([key, items]) => {
      const [year, month] = key.split("-");
      return {
        key,
        label: `${year}年${Number(month)}月`,
        items,
      };
    });
});

const finished = computed(() => moments.value.length >= total.value);

onLoad((options) => {
  familyId.value = Number(options?.familyId || 0);
  albumId.value = Number(options?.albumId || 0);
  if (!familyId.value || !albumId.value) {
    uni.showToast({ title: "缺少家庭或相册信息", icon: "none" });
    setTimeout(() => uni.navigateBack(), 600);
    return;
  }
  reloadMoments();
});

onUnload(() => {
  audioContext?.destroy();
  audioContext = undefined;
});

async function reloadMoments() {
  const version = ++filterVersion;
  pageNum.value = 1;
  moments.value = [];
  total.value = 0;
  await fetchMoments(true, version);
}

async function fetchMoments(reset: boolean, version: number) {
  if (!familyId.value || !albumId.value) return false;
  if (reset) loading.value = true;
  else loadingMore.value = true;
  try {
    const result = await AlbumAPI.getMomentPage({
      pageNum: pageNum.value,
      pageSize: PAGE_SIZE,
      familyId: familyId.value,
      albumId: albumId.value,
      months: selectedMonths.value.length ? selectedMonths.value.join(",") : undefined,
      mediaTypes: selectedMediaTypes.value.join(","),
    });
    if (version !== filterVersion) return false;
    moments.value = reset ? result.list : mergeMoments(moments.value, result.list);
    total.value = result.total;
    return true;
  } catch (error) {
    if (version !== filterVersion) return false;
    console.error("读取分类相册内容失败", error);
    uni.showToast({ title: "分类内容加载失败", icon: "none" });
    return false;
  } finally {
    if (version === filterVersion) {
      loading.value = false;
      loadingMore.value = false;
    }
  }
}

function mergeMoments(current: AlbumMoment[], incoming: AlbumMoment[]) {
  const existingIds = new Set(current.map((item) => item.id));
  return current.concat(incoming.filter((item) => !existingIds.has(item.id)));
}

function toggleMediaType(type: AlbumMediaType) {
  if (selectedMediaTypes.value.includes(type)) {
    if (selectedMediaTypes.value.length === 1) {
      uni.showToast({ title: "至少选择一种内容类型", icon: "none" });
      return;
    }
    selectedMediaTypes.value = selectedMediaTypes.value.filter((item) => item !== type);
  } else {
    selectedMediaTypes.value = [...selectedMediaTypes.value, type];
  }
  reloadMoments();
}

function toggleMonth(month: string) {
  selectedMonths.value = selectedMonths.value.includes(month)
    ? selectedMonths.value.filter((item) => item !== month)
    : [...selectedMonths.value, month];
  reloadMoments();
}

function clearMonths() {
  selectedMonths.value = [];
  reloadMoments();
}

function changeYear(event: { detail: { value: string } }) {
  filterYear.value = Number(event.detail.value.slice(0, 4));
}

async function loadMore() {
  if (loading.value || loadingMore.value || finished.value) return;
  pageNum.value += 1;
  const loaded = await fetchMoments(false, filterVersion);
  if (!loaded) pageNum.value -= 1;
}

function previewImage(moment: AlbumMoment) {
  const images = moments.value.filter((item) => item.mediaType === "IMAGE");
  uni.previewImage({
    current: moment.previewUrl,
    urls: images.map((item) => item.previewUrl),
  });
}

function playAudio(moment: AlbumMoment) {
  audioContext?.destroy();
  audioContext = uni.createInnerAudioContext();
  audioContext.src = moment.previewUrl;
  audioContext.play();
  uni.showToast({ title: "正在播放音频", icon: "none" });
}

function formatMomentTime(moment: AlbumMoment) {
  return dayjs(moment.capturedAt || moment.createTime).format("MM月DD日 HH:mm");
}

function formatMediaMeta(moment: AlbumMoment) {
  if (moment.mediaType === "IMAGE") return formatFileSize(moment.fileSize);
  if (moment.duration != null) return formatDuration(moment.duration);
  return moment.mediaTypeLabel;
}

function formatFileSize(fileSize?: number) {
  if (fileSize == null) return "图片";
  if (fileSize < 1024) return `${fileSize} B`;
  const units = ["KB", "MB", "GB"];
  let value = fileSize / 1024;
  let unitIndex = 0;
  while (value >= 1024 && unitIndex < units.length - 1) {
    value /= 1024;
    unitIndex += 1;
  }
  return `${value.toFixed(value >= 10 ? 0 : 1)} ${units[unitIndex]}`;
}

function formatDuration(duration: number) {
  const seconds = Math.round(duration / 1000);
  const minutes = Math.floor(seconds / 60);
  return `${String(minutes).padStart(2, "0")}:${String(seconds % 60).padStart(2, "0")}`;
}

onReachBottom(loadMore);
</script>

<style lang="scss" scoped>
.category-page {
  min-height: 100vh;
  padding-bottom: 60rpx;
  color: #3f3849;
  background: #f5f3fa;
}

.filter-panel {
  padding: 26rpx 28rpx;
  background: #fff;
  border-bottom: 1rpx solid rgb(117 103 220 / 9%);
  box-shadow: 0 10rpx 30rpx rgb(55 43 92 / 5%);
}

.filter-row + .filter-row {
  padding-top: 26rpx;
  margin-top: 26rpx;
  border-top: 1rpx solid #eeeaf3;
}

.filter-row__label {
  display: block;
  margin-bottom: 18rpx;
  font-size: 27rpx;
  font-weight: 700;
  color: #41394a;
}

.media-options {
  display: flex;
  gap: 16rpx;
}

.media-option {
  display: flex;
  flex: 1;
  gap: 9rpx;
  align-items: center;
  justify-content: center;
  height: 68rpx;
  font-size: 23rpx;
  color: #675b76;
  background: #f5f2fa;
  border: 2rpx solid transparent;
  border-radius: 20rpx;
}

.media-option--active {
  color: #fff;
  background: linear-gradient(135deg, #7668de, #9568df);
  border-color: #7668de;
  box-shadow: 0 8rpx 18rpx rgb(117 103 220 / 18%);
}

.month-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.month-heading .filter-row__label {
  margin-bottom: 5rpx;
}

.month-heading__hint {
  display: block;
  margin-bottom: 18rpx;
  font-size: 20rpx;
  color: #a099aa;
}

.year-picker {
  display: flex;
  gap: 7rpx;
  align-items: center;
  padding: 12rpx 16rpx;
  font-size: 21rpx;
  color: #6758cc;
  background: #f0edff;
  border-radius: 15rpx;
}

.month-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12rpx;
}

.month-option {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 58rpx;
  font-size: 20rpx;
  color: #746b7d;
  background: #f6f4f8;
  border: 2rpx solid transparent;
  border-radius: 16rpx;
}

.month-option--active {
  font-weight: 600;
  color: #6758cc;
  background: #eeebff;
  border-color: #8e80e5;
}

.selected-filter {
  display: flex;
  justify-content: space-between;
  margin-top: 18rpx;
  font-size: 20rpx;
  color: #91899a;
}

.selected-filter text:last-child {
  color: #7567dc;
}

.result-section {
  padding: 32rpx 0 0;
}

.result-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28rpx 22rpx;
}

.result-heading__title,
.result-heading__subtitle {
  display: block;
}

.result-heading__title {
  font-size: 31rpx;
  font-weight: 700;
  color: #3e3747;
}

.result-heading__subtitle {
  margin-top: 6rpx;
  font-size: 21rpx;
  color: #9d95a5;
}

.result-heading__count {
  padding: 9rpx 15rpx;
  font-size: 20rpx;
  color: #7567dc;
  background: #ece8ff;
  border-radius: 999rpx;
}

.result-loading,
.result-empty {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
  align-items: center;
  justify-content: center;
  margin: 0 28rpx;
  padding: 90rpx 30rpx;
  color: #91899a;
  background: #fff;
  border-radius: 28rpx;
}

.result-loading {
  font-size: 22rpx;
}

.result-empty__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100rpx;
  height: 100rpx;
  background: #f0edff;
  border-radius: 30rpx;
}

.result-empty__title {
  font-size: 27rpx;
  font-weight: 700;
  color: #4a4253;
}

.result-empty__desc {
  font-size: 21rpx;
}

.month-group {
  padding: 26rpx 0 30rpx;
  margin-bottom: 22rpx;
  background: #fff;
  border-radius: 30rpx;
  box-shadow: 0 10rpx 28rpx rgb(55 43 92 / 5%);
}

.month-group__heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28rpx 20rpx;
}

.month-group__title {
  font-size: 30rpx;
  font-weight: 800;
  color: #403849;
}

.month-group__count {
  font-size: 20rpx;
  color: #9c94a5;
}

.moment-scroll {
  width: 100%;
  white-space: nowrap;
}

.moment-row {
  display: inline-flex;
  gap: 18rpx;
  padding: 0 28rpx;
}

.moment-card {
  width: 270rpx;
  overflow: hidden;
  white-space: normal;
  background: #f8f6fa;
  border: 1rpx solid #eeeaf3;
  border-radius: 22rpx;
}

.moment-card__visual {
  position: relative;
  width: 270rpx;
  height: 220rpx;
  overflow: hidden;
  background: #ece8f1;
}

.moment-card__media,
.moment-card__audio {
  width: 100%;
  height: 100%;
}

.moment-card__audio {
  display: flex;
  flex-direction: column;
  gap: 14rpx;
  align-items: center;
  justify-content: center;
  font-size: 20rpx;
  color: #fff;
  background: linear-gradient(145deg, #796bdd, #bc78bd);
}

.audio-disc {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 76rpx;
  height: 76rpx;
  background: rgb(255 255 255 / 18%);
  border: 2rpx solid rgb(255 255 255 / 38%);
  border-radius: 50%;
}

.moment-card__badge {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  z-index: 3;
  height: 36rpx;
  padding: 0 11rpx;
  font-size: 17rpx;
  line-height: 36rpx;
  color: #fff;
  background: rgb(24 20 35 / 68%);
  border-radius: 999rpx;
}

.moment-card__body {
  padding: 16rpx;
}

.moment-card__description {
  display: block;
  overflow: hidden;
  font-size: 22rpx;
  color: #4d4655;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.moment-card__footer {
  display: flex;
  gap: 10rpx;
  align-items: center;
  justify-content: space-between;
  margin-top: 12rpx;
  font-size: 18rpx;
  color: #9b94a2;
}

.moment-card__footer text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.moment-card__footer text:first-child {
  flex-shrink: 0;
}

.moment-card__footer text:last-child {
  color: #7567dc;
}

.load-more {
  display: flex;
  justify-content: center;
  padding: 32rpx 0;
  font-size: 21rpx;
  color: #a19aa9;
}
</style>
