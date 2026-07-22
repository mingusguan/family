<template>
  <view class="detail-page">
    <custom-navbar title="家庭详情" placeholder />

    <view v-if="loading" class="detail-state">
      <wd-loading color="#7567dc" />
      <text>正在读取家庭信息...</text>
    </view>

    <template v-else-if="detail">
      <view class="family-hero">
        <image
          v-if="detail.coverUrl"
          class="family-hero__cover"
          :src="detail.coverUrl"
          mode="aspectFill"
        />
        <view v-else class="family-hero__cover family-hero__cover--empty">
          <wd-icon name="home" size="58rpx" color="#ffffff" />
        </view>
        <view class="family-hero__content">
          <view class="family-hero__heading">
            <text class="family-hero__name">{{ detail.name }}</text>
            <text class="family-hero__role">{{ detail.role === "OWNER" ? "创建者" : "成员" }}</text>
          </view>
          <text class="family-hero__desc">
            {{ detail.description || "一起记录家庭里的每一件小事" }}
          </text>
          <view class="family-hero__stats">
            <text>{{ detail.memberCount }} 位成员</text>
            <view class="family-hero__dot" />
            <text>{{ detail.albumCount }} 个相册</text>
          </view>
        </view>
      </view>

      <view class="invite-card">
        <view class="invite-card__icon">
          <wd-icon name="user-add" size="34rpx" color="#7567dc" />
        </view>
        <view class="invite-card__content">
          <text class="invite-card__title">邀请家人加入</text>
          <text class="invite-card__desc">通过微信分享邀请，链接 7 天内有效</text>
        </view>
        <button class="invite-card__button" open-type="share" :disabled="!invite?.code">
          微信邀请
        </button>
      </view>

      <view class="member-section">
        <view class="section-heading">
          <text class="section-heading__title">家庭成员</text>
          <text class="section-heading__count">{{ detail.members.length }} 人</text>
        </view>
        <view class="member-list">
          <view v-for="member in detail.members" :key="member.userId" class="member-item">
            <image
              v-if="member.avatar"
              class="member-item__avatar"
              :src="member.avatar"
              mode="aspectFill"
            />
            <view v-else class="member-item__avatar member-item__avatar--empty">
              <wd-icon name="user" size="34rpx" color="#8a7de2" />
            </view>
            <view class="member-item__content">
              <text class="member-item__name">{{ member.nickname || "家庭成员" }}</text>
              <text class="member-item__joined">{{ formatJoinedAt(member.joinedAt) }} 加入</text>
            </view>
            <text class="member-item__role">
              {{ member.role === "OWNER" ? "创建者" : "成员" }}
            </text>
          </view>
        </view>
      </view>
    </template>

    <view v-else class="detail-state detail-state--empty">
      <wd-icon name="warning" size="48rpx" color="#9a91ad" />
      <text>家庭信息加载失败</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import dayjs from "dayjs";
import { onLoad, onShareAppMessage, onShow } from "@dcloudio/uni-app";
import FamilyAPI, { type FamilyDetail, type FamilyInvite } from "@/api/family";

definePage({
  name: "family-detail",
  style: {
    navigationStyle: "custom",
    backgroundColor: "#f5f3fb",
    enableShareAppMessage: true,
  },
});

const familyId = ref(0);
const detail = ref<FamilyDetail>();
const invite = ref<FamilyInvite>();
const loading = ref(false);

async function loadPage() {
  if (!familyId.value || loading.value) return;
  loading.value = true;
  try {
    detail.value = await FamilyAPI.getFamilyDetail(familyId.value);
    try {
      invite.value = await FamilyAPI.createInvite(familyId.value);
    } catch (error) {
      console.error("创建家庭邀请失败", error);
      invite.value = undefined;
    }
  } catch (error: any) {
    console.error("读取家庭详情失败", error);
    uni.showToast({ title: error?.message || "家庭详情加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

function formatJoinedAt(value: string) {
  return value ? dayjs(value).format("YYYY年MM月DD日") : "最近";
}

onLoad((options) => {
  familyId.value = Number(options?.familyId || 0);
  if (!familyId.value) {
    uni.showToast({ title: "家庭参数缺失", icon: "none" });
    setTimeout(() => uni.navigateBack(), 800);
  }
});

onShow(loadPage);

onShareAppMessage(() => ({
  title: detail.value ? `邀请你加入「${detail.value.name}」` : "邀请你加入我的家庭",
  path: invite.value?.code
    ? `/pages/index/index?inviteCode=${encodeURIComponent(invite.value.code)}`
    : "/pages/index/index",
  imageUrl: detail.value?.coverUrl,
}));
</script>

<style lang="scss" scoped>
.detail-page {
  box-sizing: border-box;
  min-height: 100vh;
  padding: 24rpx 28rpx 60rpx;
  background: #f5f3fb;
}

.detail-state {
  display: flex;
  gap: 18rpx;
  align-items: center;
  justify-content: center;
  min-height: 420rpx;
  font-size: 24rpx;
  color: #928b9e;
}

.detail-state--empty {
  flex-direction: column;
}

.family-hero {
  display: flex;
  gap: 26rpx;
  padding: 30rpx;
  color: #fff;
  background: linear-gradient(135deg, #7061d9, #a671ce);
  border-radius: 34rpx;
  box-shadow: 0 22rpx 44rpx rgb(86 64 176 / 22%);
}

.family-hero__cover {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 142rpx;
  height: 142rpx;
  border: 3rpx solid rgb(255 255 255 / 28%);
  border-radius: 28rpx;
}

.family-hero__cover--empty {
  background: rgb(255 255 255 / 17%);
}

.family-hero__content {
  flex: 1;
  min-width: 0;
}

.family-hero__heading,
.family-hero__stats {
  display: flex;
  align-items: center;
}

.family-hero__name {
  flex: 1;
  overflow: hidden;
  font-size: 34rpx;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.family-hero__role {
  padding: 6rpx 13rpx;
  font-size: 19rpx;
  background: rgb(255 255 255 / 18%);
  border-radius: 999rpx;
}

.family-hero__desc {
  display: -webkit-box;
  margin-top: 13rpx;
  overflow: hidden;
  font-size: 22rpx;
  line-height: 1.5;
  opacity: 0.8;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.family-hero__stats {
  gap: 13rpx;
  margin-top: 17rpx;
  font-size: 21rpx;
  opacity: 0.88;
}

.family-hero__dot {
  width: 5rpx;
  height: 5rpx;
  background: rgb(255 255 255 / 60%);
  border-radius: 50%;
}

.invite-card {
  display: flex;
  gap: 18rpx;
  align-items: center;
  padding: 25rpx 24rpx;
  margin-top: 24rpx;
  background: #fff;
  border-radius: 28rpx;
  box-shadow: 0 12rpx 30rpx rgb(61 46 108 / 7%);
}

.invite-card__icon {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 72rpx;
  height: 72rpx;
  background: #eeebff;
  border-radius: 22rpx;
}

.invite-card__content {
  flex: 1;
  min-width: 0;
}

.invite-card__title,
.invite-card__desc {
  display: block;
}

.invite-card__title {
  font-size: 25rpx;
  font-weight: 650;
  color: #403949;
}

.invite-card__desc {
  margin-top: 7rpx;
  font-size: 19rpx;
  color: #9b94a5;
}

.invite-card__button {
  flex-shrink: 0;
  height: 62rpx;
  padding: 0 22rpx;
  margin: 0;
  font-size: 22rpx;
  line-height: 62rpx;
  color: #fff;
  background: linear-gradient(135deg, #7667dc, #9d70d2);
  border: 0;
  border-radius: 999rpx;
}

.invite-card__button::after {
  border: 0;
}

.invite-card__button[disabled] {
  color: #aaa4b2;
  background: #edeaf1;
}

.member-section {
  padding: 28rpx;
  margin-top: 24rpx;
  background: #fff;
  border-radius: 30rpx;
  box-shadow: 0 12rpx 30rpx rgb(61 46 108 / 6%);
}

.section-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 13rpx;
}

.section-heading__title {
  font-size: 29rpx;
  font-weight: 680;
  color: #3d3746;
}

.section-heading__count {
  font-size: 21rpx;
  color: #968fa0;
}

.member-item {
  display: flex;
  gap: 18rpx;
  align-items: center;
  padding: 22rpx 0;
  border-bottom: 1rpx solid #f0edf4;
}

.member-item:last-child {
  border-bottom: 0;
}

.member-item__avatar {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 82rpx;
  height: 82rpx;
  border-radius: 50%;
}

.member-item__avatar--empty {
  background: #efecfb;
}

.member-item__content {
  flex: 1;
  min-width: 0;
}

.member-item__name,
.member-item__joined {
  display: block;
}

.member-item__name {
  overflow: hidden;
  font-size: 25rpx;
  font-weight: 600;
  color: #49424f;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.member-item__joined {
  margin-top: 7rpx;
  font-size: 19rpx;
  color: #a09aa6;
}

.member-item__role {
  padding: 7rpx 13rpx;
  font-size: 19rpx;
  color: #7668d8;
  background: #efecff;
  border-radius: 999rpx;
}
</style>
