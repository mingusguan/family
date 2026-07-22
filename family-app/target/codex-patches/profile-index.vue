<template>
  <view class="page">
    <view class="profile-content__card-area">
      <wd-card v-if="userProfile">
        <wd-cell-group border>
          <wd-cell class="avatar-cell" title="头像" center is-link>
            <view class="avatar-cell__avatar">
              <view v-if="!userProfile.avatar" class="avatar-cell__img" @click="handleAvatarUpload">
                <wd-icon name="fill-camera" custom-class="avatar-cell__img-icon" />
              </view>
              <image
                v-else
                class="avatar-cell__img"
                :src="userProfile.avatar"
                mode="aspectFill"
                @click="handleAvatarUpload"
              />
            </view>
          </wd-cell>
          <wd-cell title="昵称" :value="userProfile.nickname || '未设置'" is-link @click="openDialog" />
          <wd-cell title="微信账号" :value="userProfile.username || '-'" />
          <wd-cell title="手机号" :value="userProfile.mobile || '未绑定（非必填）'" />
          <wd-cell title="注册时间" :value="userProfile.createTime || '-'" />
        </wd-cell-group>
      </wd-card>
    </view>

    <wd-img-cropper
      v-if="avatarShow"
      v-model="avatarShow"
      :img-src="originalSrc"
      @confirm="handleAvatarConfirm"
    />

    <wd-popup v-if="dialogState.visible" v-model="dialogState.visible" position="bottom">
      <wd-form ref="userProfileFormRef" :model="userProfileForm" custom-class="edit-form">
        <wd-cell-group border>
          <wd-input
            v-model="userProfileForm.nickname"
            label="昵称"
            label-width="160rpx"
            placeholder="请输入昵称"
            prop="nickname"
            :rules="rules.nickname"
          />
        </wd-cell-group>
        <view class="edit-form__submit">
          <wd-button type="primary" size="large" block @click="handleSubmit">提交</wd-button>
        </view>
      </wd-form>
    </wd-popup>
  </view>
</template>

<script setup lang="ts">
import FileAPI, { type FileInfo } from "@/api/file";
import UserAPI, { type UserProfile, type UserProfileForm } from "@/api/user";
import { checkLogin } from "@/utils/auth";

const originalSrc = ref("");
const avatarShow = ref(false);
const userProfile = ref<UserProfile>();

const loadUserProfile = async () => {
  userProfile.value = await UserAPI.getProfile();
};

function handleAvatarUpload() {
  uni.chooseImage({
    count: 1,
    success: (res) => {
      originalSrc.value = res.tempFilePaths[0];
      avatarShow.value = true;
    },
  });
}

async function handleAvatarConfirm(event: { tempFilePath: string }) {
  const fileInfo: FileInfo = await FileAPI.upload(event.tempFilePath);
  await UserAPI.updateProfile({ avatar: fileInfo.url });
  uni.showToast({ title: "头像上传成功", icon: "none" });
  await loadUserProfile();
}

const rules = reactive({
  nickname: [
    { required: true, message: "请填写昵称" },
    { min: 2, max: 20, message: "昵称长度为2-20个字符" },
  ],
});

const dialogState = reactive({ visible: false });
const userProfileForm = reactive<UserProfileForm>({});
const userProfileFormRef = ref();

const openDialog = () => {
  userProfileForm.nickname = userProfile.value?.nickname;
  dialogState.visible = true;
};

function handleSubmit() {
  userProfileFormRef.value.validate().then(async ({ valid }: { valid: boolean }) => {
    if (!valid) return;
    await UserAPI.updateProfile({ nickname: userProfileForm.nickname });
    uni.showToast({ title: "昵称修改成功", icon: "none" });
    dialogState.visible = false;
    await loadUserProfile();
  });
}

onLoad(() => {
  if (!checkLogin()) return;

  // #ifdef H5
  document.addEventListener("touchstart", handleTouchStart, { passive: false });
  document.addEventListener("touchmove", handleTouchMove, { passive: false });
  // #endif
  loadUserProfile();
});

onBeforeUnmount(() => {
  // #ifdef H5
  document.removeEventListener("touchstart", handleTouchStart);
  document.removeEventListener("touchmove", handleTouchMove);
  // #endif
});

function handleTouchStart(event: TouchEvent) {
  if (event.touches.length > 1) event.preventDefault();
}

function handleTouchMove(event: TouchEvent) {
  event.preventDefault();
}
</script>

<style lang="scss" scoped>
.avatar-cell {
  :deep(.wd-cell__body) {
    align-items: center;
  }
}

.avatar-cell__avatar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.avatar-cell__img {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  background-color: rgba(0, 0, 0, 0.04);
  border-radius: 50%;
}

.avatar-cell__img-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  color: var(--color-text-inverse);
  transform: translate(-50%, -50%);
}

.profile-content__card-area {
  padding-top: 20rpx;
}

.edit-form {
  padding-top: 40rpx;

  &__submit {
    padding: 24rpx;
  }
}
</style>