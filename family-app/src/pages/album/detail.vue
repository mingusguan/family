<template>
  <view class="detail-page">
    <view v-if="loading" class="detail-state">
      <wd-loading size="42rpx" />
      <text>正在加载这段回忆...</text>
    </view>

    <template v-else-if="detail">
      <view class="detail-header">
        <view>
          <text class="detail-header__owner">{{ detail.uploaderName || "家庭成员" }}</text>
          <text class="detail-header__time">{{ formatDate(detail.capturedAt || detail.createTime) }}</text>
        </view>
        <text class="detail-header__count">{{ detail.assets.length }} 项内容</text>
      </view>

      <view v-if="detail.description" class="detail-description">
        {{ detail.description }}
      </view>

      <view class="asset-grid">
        <view v-for="asset in detail.assets" :key="asset.id" class="asset-card">
          <image
            v-if="asset.mediaType === 'IMAGE'"
            class="asset-card__media"
            :src="asset.thumbnailPreviewUrl || asset.previewUrl"
            mode="aspectFill"
            lazy-load
            @click="previewImage(asset)"
          />
          <video
            v-else-if="asset.mediaType === 'VIDEO'"
            class="asset-card__media"
            :src="asset.previewUrl"
            :poster="asset.thumbnailPreviewUrl"
            controls
            object-fit="cover"
          />
          <view v-else class="asset-card__audio" @click="playAudio(asset)">
            <wd-icon name="play-circle" size="58rpx" color="#ffffff" />
            <text>播放声音</text>
          </view>
          <view class="asset-card__meta">
            <text>{{ asset.mediaTypeLabel }}</text>
            <text>{{ formatAssetMeta(asset) }}</text>
          </view>
        </view>
      </view>
    </template>

    <view v-else class="detail-state">
      <wd-icon name="warning" size="72rpx" color="#9a91aa" />
      <text>这段回忆暂时无法查看</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onLoad, onUnload } from "@dcloudio/uni-app";
import dayjs from "dayjs";
import { ref } from "vue";
import AlbumAPI, { type AlbumMoment, type AlbumMomentDetail } from "@/api/album";

const loading = ref(true);
const detail = ref<AlbumMomentDetail>();
let audioContext: UniApp.InnerAudioContext | undefined;

definePage({
  name: "album-detail",
  style: {
    navigationBarTitleText: "回忆详情",
    backgroundColor: "#f5f3fa",
  },
});

onLoad(async (options) => {
  const batchId = decodeURIComponent(String(options?.batchId || ""));
  const familyId = Number(options?.familyId || 0);
  const albumId = Number(options?.albumId || 0);
  if (!batchId || !familyId || !albumId) {
    loading.value = false;
    return;
  }
  try {
    detail.value = await AlbumAPI.getMomentDetail(batchId, familyId, albumId);
  } catch (error: any) {
    console.error("读取相册批次详情失败", error);
    uni.showToast({ title: error?.message || "详情加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
});

onUnload(() => {
  audioContext?.destroy();
});

function previewImage(asset: AlbumMoment) {
  if (!detail.value) return;
  const images = detail.value.assets
    .filter((item) => item.mediaType === "IMAGE")
    .map((item) => item.previewUrl);
  uni.previewImage({ current: asset.previewUrl, urls: images });
}

function playAudio(asset: AlbumMoment) {
  audioContext?.destroy();
  audioContext = uni.createInnerAudioContext();
  audioContext.src = asset.previewUrl;
  audioContext.play();
}

function formatDate(value: string) {
  return dayjs(value).format("YYYY年MM月DD日 HH:mm");
}

function formatAssetMeta(asset: AlbumMoment) {
  if (asset.duration != null) {
    const seconds = Math.round(asset.duration / 1000);
    return Math.floor(seconds / 60) + ":" + String(seconds % 60).padStart(2, "0");
  }
  if (asset.fileSize == null) return "";
  if (asset.fileSize < 1024 * 1024) return (asset.fileSize / 1024).toFixed(0) + " KB";
  return (asset.fileSize / 1024 / 1024).toFixed(1) + " MB";
}
</script>

<style lang="scss" scoped>
.detail-page {
  min-height: 100vh;
  padding: 28rpx;
  background: #f5f3fa;
}

.detail-state {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  color: #8f8799;
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx;
  background: #fff;
  border-radius: 26rpx;
}

.detail-header__owner,
.detail-header__time {
  display: block;
}

.detail-header__owner {
  font-size: 30rpx;
  font-weight: 650;
  color: #403949;
}

.detail-header__time {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #9a93a3;
}

.detail-header__count {
  padding: 10rpx 16rpx;
  font-size: 21rpx;
  color: #7162d2;
  background: #eeebff;
  border-radius: 999rpx;
}

.detail-description {
  padding: 24rpx 28rpx;
  margin-top: 20rpx;
  font-size: 26rpx;
  line-height: 1.7;
  color: #4c4554;
  white-space: pre-wrap;
  background: #fff;
  border-radius: 24rpx;
}

.asset-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18rpx;
  margin-top: 24rpx;
}

.asset-card {
  overflow: hidden;
  background: #fff;
  border-radius: 24rpx;
  box-shadow: 0 10rpx 28rpx rgb(55 43 92 / 8%);
}

.asset-card__media,
.asset-card__audio {
  width: 100%;
  height: 300rpx;
}

.asset-card__audio {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  color: #fff;
  background: linear-gradient(145deg, #796bdd, #bc78bd);
}

.asset-card__meta {
  display: flex;
  justify-content: space-between;
  padding: 15rpx 18rpx;
  font-size: 19rpx;
  color: #9891a2;
}
</style>
