<template>
  <view class="page">
    <view class="hero">
      <view class="hero__title">完善个人信息</view>
      <view class="hero__subtitle">设置头像和昵称即可开始使用</view>
    </view>

    <view class="profile-card">
      <view class="field-label">头像</view>
      <!-- #ifdef MP-WEIXIN -->
      <button class="avatar-picker" open-type="chooseAvatar" @chooseavatar="handleChooseAvatar">
        <image v-if="avatarPreviewUrl" :src="avatarPreviewUrl" class="avatar" mode="aspectFill" />
        <view v-else class="avatar-placeholder">
          <wd-icon name="camera" size="40" color="var(--color-text-placeholder)" />
          <text>选择微信头像</text>
        </view>
      </button>
      <!-- #endif -->
      <!-- #ifndef MP-WEIXIN -->
      <view class="avatar-picker" @click="chooseAvatar">
        <image v-if="avatarPreviewUrl" :src="avatarPreviewUrl" class="avatar" mode="aspectFill" />
        <view v-else class="avatar-placeholder">
          <wd-icon name="camera" size="40" color="var(--color-text-placeholder)" />
          <text>选择头像</text>
        </view>
      </view>
      <!-- #endif -->

      <view class="field-label nickname-label">
        昵称
        <text class="required">*</text>
      </view>
      <input
        v-model="profileForm.nickname"
        type="nickname"
        class="nickname-input"
        placeholder="请输入2-20个字符的昵称"
        :maxlength="20"
      />
    </view>

    <wd-button
      type="primary"
      size="large"
      block
      :disabled="!canComplete || isLoading"
      :loading="isLoading"
      @click="handleComplete"
    >
      完成
    </wd-button>
    <view class="skip" @click="handleSkip">暂时跳过</view>
    <wd-toast />
  </view>
</template>

<script lang="ts" setup>
import { computed, reactive, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import FileAPI, { type FileInfo } from "@/api/file";
import UserAPI, { type UserProfileForm } from "@/api/user";
import { useUserStore } from "@/store";

const toast = useToast();
const { confirm } = useDialog();
const userStore = useUserStore();
const redirect = ref("/pages/index/index");
const profileForm = reactive<UserProfileForm>({ nickname: "" });
const avatarStorageUrl = ref("");
const avatarPreviewUrl = ref("");
const isLoading = ref(false);
const canComplete = computed(() => {
  const nickname = profileForm.nickname?.trim() || "";
  return nickname.length >= 2 && nickname.length <= 20;
});

onLoad((options: any) => {
  if (options?.redirect) redirect.value = decodeURIComponent(options.redirect);
  profileForm.nickname = userStore.userInfo?.nickname || "";
  avatarPreviewUrl.value = userStore.userInfo?.avatar || "";
});

const uploadAvatar = async (path: string) => {
  try {
    toast.loading("上传中...");
    const fileInfo: FileInfo = await FileAPI.upload(path);
    avatarStorageUrl.value = fileInfo.url;
    avatarPreviewUrl.value = fileInfo.previewUrl || fileInfo.url;
    toast.success("头像上传成功");
  } catch {
    toast.error("头像上传失败");
  }
};

const handleChooseAvatar = (event: { detail: { avatarUrl: string } }) => {
  uploadAvatar(event.detail.avatarUrl);
};

const chooseAvatar = () => {
  // #ifndef MP-WEIXIN
  uni.chooseImage({
    count: 1,
    sourceType: ["album", "camera"],
    success: (res) => uploadAvatar(res.tempFilePaths[0]),
  });
  // #endif
};

const handleComplete = async () => {
  const nickname = profileForm.nickname?.trim() || "";
  if (nickname.length < 2 || nickname.length > 20) {
    toast.error("昵称长度为2-20个字符");
    return;
  }

  try {
    isLoading.value = true;
    const updateData: UserProfileForm = { nickname };
    if (avatarStorageUrl.value) updateData.avatar = avatarStorageUrl.value;
    await UserAPI.updateProfile(updateData);
    await userStore.getInfo();
    toast.success("信息完善成功");
    setTimeout(() => uni.reLaunch({ url: redirect.value }), 1000);
  } catch (error: any) {
    toast.error(error?.message || "完善信息失败");
  } finally {
    isLoading.value = false;
  }
};

const handleSkip = async () => {
  try {
    await confirm({ title: "提示", msg: "确定暂时跳过资料完善吗？", headerImage: "warning" });
    uni.reLaunch({ url: redirect.value });
  } catch {
    return;
  }
};
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  padding: 80rpx 40rpx 40rpx;
  background: linear-gradient(160deg, var(--color-primary), var(--color-primary-light));
}

.hero {
  margin-bottom: 60rpx;
  color: var(--color-text-inverse);
  text-align: center;

  &__title {
    margin-bottom: 20rpx;
    font-size: 48rpx;
    font-weight: bold;
  }

  &__subtitle {
    font-size: 28rpx;
    opacity: 0.8;
  }
}

.profile-card {
  padding: 40rpx;
  margin-bottom: 40rpx;
  background: var(--color-bg);
  border-radius: 24rpx;
  box-shadow: 0 8rpx 40rpx rgba(0, 0, 0, 0.1);
}

.field-label {
  margin-bottom: 20rpx;
  font-size: 32rpx;
  font-weight: 600;
  color: var(--color-text);
}

.nickname-label {
  margin-top: 40rpx;
}

.required {
  color: var(--color-danger);
}

.avatar-picker,
.avatar,
.avatar-placeholder {
  width: 160rpx;
  height: 160rpx;
  margin: 0 auto;
  border-radius: 50%;
}

.avatar-picker {
  padding: 0;
  overflow: hidden;
  line-height: normal;
  background: transparent;
  border: 0;

  &::after {
    border: 0;
  }
}

.avatar-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  color: var(--color-text-placeholder);
  background: var(--color-bg-tertiary);
  border: 2rpx dashed var(--color-border);
}

.nickname-input {
  box-sizing: border-box;
  width: 100%;
  height: 88rpx;
  padding: 0 20rpx;
  background: var(--color-bg-tertiary);
  border: 1rpx solid var(--color-border-light);
  border-radius: 12rpx;
}

.skip {
  margin-top: 30rpx;
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
  text-align: center;
  text-decoration: underline;
}
</style>
