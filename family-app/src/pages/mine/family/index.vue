<template>
  <view class="family-page">
    <custom-navbar title="我的家庭" placeholder />

    <view class="family-banner">
      <text class="family-banner__eyebrow">OUR FAMILIES</text>
      <text class="family-banner__title">和家人共享生活里的温暖</text>
      <text class="family-banner__desc">创建家庭后，就可以建立相册并邀请成员共同记录</text>
      <wd-button type="primary" round custom-class="family-banner__button" @click="createFamily">
        <wd-icon name="add" size="16" />
        新建家庭
      </wd-button>
    </view>

    <view v-if="loading" class="family-state">
      <wd-loading color="#7567df" />
      <text>正在读取家庭...</text>
    </view>

    <view v-else-if="families.length === 0" class="family-state family-state--empty">
      <view class="family-state__icon">
        <wd-icon name="home" size="54rpx" color="#8175df" />
      </view>
      <text class="family-state__title">还没有所属家庭</text>
      <text class="family-state__desc">创建第一个家庭，开始整理共同回忆吧</text>
      <wd-button type="primary" round @click="createFamily">创建家庭</wd-button>
    </view>

    <view v-else class="family-list">
      <view
        v-for="family in families"
        :key="family.id"
        class="family-card"
        hover-class="family-card--pressed"
        @click="goDetail(family)"
      >
        <image
          v-if="family.coverUrl"
          class="family-card__cover"
          :src="family.coverUrl"
          mode="aspectFill"
        />
        <view v-else class="family-card__cover family-card__cover--empty">
          <wd-icon name="home" size="46rpx" color="#ffffff" />
        </view>
        <view class="family-card__body">
          <view class="family-card__title-row">
            <text class="family-card__title">{{ family.name }}</text>
            <text class="family-card__role">{{ family.role === "OWNER" ? "创建者" : "成员" }}</text>
          </view>
          <text class="family-card__desc">
            {{ family.description || "一起记录家庭里的每一件小事" }}
          </text>
          <view class="family-card__stats">
            <text>{{ family.memberCount }} 位成员</text>
            <text>{{ family.albumCount }} 个相册</text>
          </view>
        </view>
      </view>
    </view>

    <wd-toast />
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import FamilyAPI, { type FamilyInfo } from "@/api/family";

definePage({
  name: "mine-family",
  style: { navigationStyle: "custom", backgroundColor: "#f5f3fb" },
});

const families = ref<FamilyInfo[]>([]);
const loading = ref(false);

async function loadFamilies() {
  loading.value = true;
  try {
    families.value = await FamilyAPI.listMyFamilies();
  } catch (error) {
    console.error("读取家庭失败", error);
    uni.showToast({ title: "家庭列表加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

function goDetail(family: FamilyInfo) {
  uni.navigateTo({ url: "/pages/mine/family/detail/index?familyId=" + family.id });
}
function createFamily() {
  uni.showModal({
    title: "新建家庭",
    editable: true,
    placeholderText: "例如：幸福一家人",
    confirmText: "创建",
    success: async (result) => {
      const name = (result.content || "").trim();
      if (!result.confirm) return;
      if (!name) {
        uni.showToast({ title: "请输入家庭名称", icon: "none" });
        return;
      }
      try {
        await FamilyAPI.createFamily({ name });
        uni.showToast({ title: "家庭创建成功", icon: "success" });
        await loadFamilies();
      } catch (error: any) {
        uni.showToast({ title: error?.message || "创建失败", icon: "none" });
      }
    },
  });
}

onShow(loadFamilies);
</script>

<style lang="scss" scoped>
.family-page {
  min-height: 100vh;
  padding-bottom: 48rpx;
  background: #f5f3fb;
}

.family-banner {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 52rpx 38rpx 46rpx;
  margin: 24rpx 28rpx;
  color: #fff;
  background: linear-gradient(135deg, #6f61da, #a36fcf);
  border-radius: 34rpx;
  box-shadow: 0 24rpx 50rpx rgb(91 70 180 / 22%);
}

.family-banner__eyebrow {
  font-size: 20rpx;
  letter-spacing: 5rpx;
  opacity: 0.72;
}

.family-banner__title {
  margin-top: 18rpx;
  font-size: 38rpx;
  font-weight: 700;
}

.family-banner__desc {
  margin-top: 14rpx;
  font-size: 24rpx;
  line-height: 1.6;
  opacity: 0.82;
}

:deep(.family-banner__button) {
  margin-top: 30rpx;
  color: #6758d0 !important;
  background: #fff !important;
  border: 0 !important;
}

.family-state {
  display: flex;
  gap: 22rpx;
  align-items: center;
  justify-content: center;
  padding: 100rpx 30rpx;
  color: #8c8699;
}

.family-state--empty {
  flex-direction: column;
  margin: 34rpx 28rpx;
  background: #fff;
  border-radius: 32rpx;
}

.family-state__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 110rpx;
  height: 110rpx;
  background: #eeebff;
  border-radius: 34rpx;
}

.family-state__title {
  font-size: 31rpx;
  font-weight: 650;
  color: #3e384b;
}

.family-state__desc {
  margin-bottom: 14rpx;
  font-size: 24rpx;
  color: #9690a2;
}

.family-list {
  display: flex;
  flex-direction: column;
  gap: 22rpx;
  padding: 12rpx 28rpx;
}

.family-card {
  display: flex;
  gap: 24rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: 30rpx;
  box-shadow: 0 14rpx 36rpx rgb(56 43 101 / 8%);
}

.family-card--pressed {
  opacity: 0.9;
  transform: scale(0.985);
}
.family-card__cover {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 150rpx;
  height: 150rpx;
  border-radius: 24rpx;
}

.family-card__cover--empty {
  background: linear-gradient(145deg, #8b7bea, #d18ec1);
}

.family-card__body {
  flex: 1;
  min-width: 0;
}

.family-card__title-row,
.family-card__stats {
  display: flex;
  align-items: center;
}

.family-card__title {
  flex: 1;
  overflow: hidden;
  font-size: 30rpx;
  font-weight: 650;
  color: #3b3547;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.family-card__role {
  padding: 6rpx 12rpx;
  font-size: 19rpx;
  color: #7667d8;
  background: #efecff;
  border-radius: 999rpx;
}

.family-card__desc {
  display: -webkit-box;
  margin-top: 14rpx;
  overflow: hidden;
  font-size: 23rpx;
  line-height: 1.5;
  color: #928c9e;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.family-card__stats {
  gap: 24rpx;
  margin-top: 18rpx;
  font-size: 21rpx;
  color: #746b82;
}
</style>
