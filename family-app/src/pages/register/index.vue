<template>
  <view class="register-page">
    <view class="register-page__glow register-page__glow--top" />
    <view class="register-page__glow register-page__glow--bottom" />

    <view class="register-navbar" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="register-navbar__bar" :style="{ height: `${navBarHeight}px` }">
        <view class="register-navbar__back" @click="goBack">
          <text>‹</text>
        </view>
      </view>
    </view>

    <view
      class="register-content"
      :style="{ paddingTop: `${statusBarHeight + navBarHeight + 10}px` }"
    >
      <view class="register-heading">
        <view class="register-heading__badge">
          <wd-icon name="user-add" size="44rpx" color="#ffffff" />
        </view>
        <text class="register-heading__title">创建你的家庭账号</text>
        <text class="register-heading__subtitle">加入我们，一起记录每一个温暖瞬间</text>
      </view>

      <view class="register-card">
        <wd-form
          ref="registerFormRef"
          :model="formData"
          :schema="formSchema"
          error-type="message"
          hide-asterisk
        >
          <wd-form-item prop="nickname" layout="vertical">
            <view class="register-field">
              <view class="register-field__icon">
                <wd-icon name="user" size="19" color="#8175df" />
              </view>
              <input
                v-model="formData.nickname"
                class="register-field__input"
                placeholder="怎么称呼你"
                :maxlength="20"
              />
            </view>
          </wd-form-item>

          <wd-form-item prop="username" layout="vertical">
            <view class="register-field">
              <view class="register-field__icon">
                <wd-icon name="account" size="19" color="#8175df" />
              </view>
              <input
                v-model="formData.username"
                class="register-field__input"
                placeholder="设置登录用户名"
                :maxlength="20"
                autocapitalize="off"
              />
            </view>
          </wd-form-item>

          <wd-form-item prop="password" layout="vertical">
            <view class="register-field">
              <view class="register-field__icon">
                <wd-icon name="lock" size="19" color="#8175df" />
              </view>
              <input
                v-model="formData.password"
                class="register-field__input"
                placeholder="设置密码（至少6位）"
                :maxlength="32"
                password
              />
            </view>
          </wd-form-item>

          <wd-form-item prop="confirmPassword" layout="vertical">
            <view class="register-field">
              <view class="register-field__icon">
                <wd-icon name="safe" size="19" color="#8175df" />
              </view>
              <input
                v-model="formData.confirmPassword"
                class="register-field__input"
                placeholder="再次输入密码"
                :maxlength="32"
                password
                @confirm="handleRegister"
              />
            </view>
          </wd-form-item>
        </wd-form>

        <view class="register-policy">
          <wd-checkbox v-model="isAgreePolicy" type="square" size="30rpx">
            <text class="register-policy__text">
              我已阅读并同意
              <text class="register-policy__link" @click.stop="openAgreement">《用户协议》</text>
              和
              <text class="register-policy__link" @click.stop="openPrivacy">《隐私政策》</text>
            </text>
          </wd-checkbox>
        </view>

        <wd-button
          type="primary"
          block
          round
          :loading="submitting"
          custom-class="register-submit"
          @click="handleRegister"
        >
          注册并登录
        </wd-button>

        <view class="register-login" @click="goBack">
          <text>已经有账号？</text>
          <text class="register-login__link">返回登录</text>
        </view>
      </view>

      <view class="register-security">
        <wd-icon name="safe" size="28rpx" color="#9d97ad" />
        <text>密码将安全加密保存，我们不会读取你的明文密码</text>
      </view>
    </view>

    <wd-toast />
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { useToast } from "@wot-ui/ui";
import type { FormSchema } from "@wot-ui/ui/components/wd-form/types";
import { useUserStore } from "@/store/modules/user";

const toast = useToast();
const userStore = useUserStore();
const registerFormRef = ref();
const statusBarHeight = ref(20);
const navBarHeight = ref(44);
const submitting = ref(false);
const isAgreePolicy = ref(false);

const formData = ref({
  nickname: "",
  username: "",
  password: "",
  confirmPassword: "",
});

definePage({
  name: "register",
  style: { navigationStyle: "custom", navigationBarTitleText: "" },
});

const formSchema = computed<FormSchema>(() => ({
  validate(model) {
    const issues: { path: string[]; message: string }[] = [];
    const nickname = (model.nickname || "").trim();
    const username = (model.username || "").trim();
    const password = model.password || "";

    if (nickname.length < 2 || nickname.length > 20) {
      issues.push({ path: ["nickname"], message: "昵称长度必须为2到20个字符" });
    }
    if (!/^[A-Za-z][A-Za-z0-9_]{3,19}$/.test(username)) {
      issues.push({
        path: ["username"],
        message: "用户名需以字母开头，可包含字母、数字和下划线",
      });
    }
    if (password.length < 6 || password.length > 32) {
      issues.push({ path: ["password"], message: "密码长度必须为6到32个字符" });
    }
    if (model.confirmPassword !== password) {
      issues.push({ path: ["confirmPassword"], message: "两次输入的密码不一致" });
    }
    return issues;
  },
}));

async function handleRegister() {
  if (submitting.value) return;
  const { valid } = await registerFormRef.value?.validate();
  if (!valid) return;
  if (!isAgreePolicy.value) {
    toast.info("请先阅读并同意用户协议与隐私政策");
    return;
  }

  submitting.value = true;
  try {
    await userStore.register({
      nickname: formData.value.nickname.trim(),
      username: formData.value.username.trim(),
      password: formData.value.password,
    });
    await userStore.getInfo();
    toast.success("注册成功，欢迎加入");
    setTimeout(() => uni.reLaunch({ url: "/pages/album/index" }), 700);
  } catch (error: any) {
    toast.error(error?.message || "注册失败，请稍后重试");
  } finally {
    submitting.value = false;
  }
}

function goBack() {
  if (getCurrentPages().length > 1) {
    uni.navigateBack();
    return;
  }
  uni.redirectTo({ url: "/pages/login/index" });
}

function openAgreement() {
  uni.navigateTo({ url: "/pages/mine/settings/agreement/index" });
}

function openPrivacy() {
  uni.navigateTo({ url: "/pages/mine/settings/privacy/index" });
}

onLoad(() => {
  const systemInfo = uni.getSystemInfoSync();
  statusBarHeight.value = systemInfo.statusBarHeight || 20;
  // #ifdef MP-WEIXIN
  const menuButton = uni.getMenuButtonBoundingClientRect();
  navBarHeight.value = menuButton.height + (menuButton.top - statusBarHeight.value) * 2;
  // #endif
});
</script>

<style lang="scss" scoped>
.register-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  background: linear-gradient(155deg, #f8f6ff 0%, #fdf9fc 48%, #f0edff 100%);
}

.register-page__glow {
  position: fixed;
  border-radius: 50%;
  filter: blur(12rpx);
  pointer-events: none;
}

.register-page__glow--top {
  top: -180rpx;
  right: -130rpx;
  width: 480rpx;
  height: 480rpx;
  background: rgb(137 113 232 / 18%);
}

.register-page__glow--bottom {
  bottom: -220rpx;
  left: -180rpx;
  width: 520rpx;
  height: 520rpx;
  background: rgb(226 143 193 / 14%);
}

.register-navbar {
  position: absolute;
  top: 0;
  right: 0;
  left: 0;
  z-index: 5;
  padding: 0 30rpx;
}

.register-navbar__bar {
  display: flex;
  align-items: center;
}

.register-navbar__back {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 70rpx;
  height: 70rpx;
  font-size: 46rpx;
  color: #4e4860;
  background: rgb(255 255 255 / 72%);
  border: 1rpx solid rgb(107 93 168 / 12%);
  border-radius: 50%;
  box-shadow: 0 8rpx 24rpx rgb(61 48 116 / 8%);
}

.register-content {
  position: relative;
  z-index: 1;
  box-sizing: border-box;
  padding-right: 44rpx;
  padding-bottom: 50rpx;
  padding-left: 44rpx;
}

.register-heading {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24rpx 0 42rpx;
  text-align: center;
}

.register-heading__badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 96rpx;
  height: 96rpx;
  margin-bottom: 24rpx;
  background: linear-gradient(135deg, #7667e6, #a979dd);
  border-radius: 32rpx;
  box-shadow: 0 18rpx 38rpx rgb(101 80 204 / 28%);
  transform: rotate(-4deg);
}

.register-heading__title {
  font-size: 42rpx;
  font-weight: 720;
  color: #332e41;
}

.register-heading__subtitle {
  margin-top: 10rpx;
  font-size: 25rpx;
  color: #918b9f;
}

.register-card {
  padding: 42rpx 34rpx 34rpx;
  background: rgb(255 255 255 / 90%);
  border: 1rpx solid rgb(99 81 165 / 8%);
  border-radius: 38rpx;
  box-shadow: 0 24rpx 70rpx rgb(62 47 113 / 12%);
  backdrop-filter: blur(18rpx);
}

.register-field {
  display: flex;
  align-items: center;
  height: 92rpx;
  padding: 0 24rpx;
  background: #f7f5fb;
  border: 2rpx solid transparent;
  border-radius: 22rpx;
}

.register-field:focus-within {
  background: #ffffff;
  border-color: rgb(124 111 246 / 32%);
  box-shadow: 0 0 0 6rpx rgb(124 111 246 / 6%);
}

.register-field__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 54rpx;
  height: 54rpx;
  background: #ece9fb;
  border-radius: 16rpx;
}

.register-field__input {
  flex: 1;
  height: 100%;
  margin-left: 20rpx;
  font-size: 27rpx;
  color: #3e3949;
}

:deep(.wd-form-item) {
  padding: 0 !important;
  margin-top: 22rpx;
  background: transparent;
}

:deep(.wd-form-item:first-child) {
  margin-top: 0;
}

:deep(.wd-form-item .wd-cell__left) {
  display: none;
}

:deep(.wd-form-item .wd-cell__wrapper) {
  flex-direction: column;
}

:deep(.wd-form-item .wd-cell__right) {
  width: 100%;
}

:deep(.wd-form-item__error-message) {
  display: block !important;
  width: 100% !important;
  padding: 7rpx 4rpx 0 !important;
  font-size: 22rpx !important;
  color: #e5484d !important;
}

.register-policy {
  padding: 26rpx 2rpx 24rpx;
}

.register-policy__text {
  font-size: 22rpx;
  line-height: 1.6;
  color: #8c8798;
}

.register-policy__link {
  color: #6f62d9;
}

:deep(.register-submit) {
  height: 88rpx !important;
  font-size: 29rpx !important;
  font-weight: 600;
  background: linear-gradient(135deg, #7162df, #9b70d8) !important;
  box-shadow: 0 14rpx 30rpx rgb(102 81 201 / 24%);
}

.register-login {
  display: flex;
  justify-content: center;
  padding-top: 28rpx;
  font-size: 25rpx;
  color: #8d8799;
}

.register-login__link {
  margin-left: 8rpx;
  font-weight: 600;
  color: #6f62d9;
}

.register-security {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  padding: 30rpx 20rpx 0;
  font-size: 21rpx;
  color: #9d97ad;
  text-align: center;
}
</style>
