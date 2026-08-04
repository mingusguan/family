<template>
  <view class="page login">
    <!-- 背景装饰 -->
    <view class="login__decoration">
      <view class="login__circle login__circle--1" />
      <view class="login__circle login__circle--2" />
    </view>

    <!-- 导航栏 -->
    <view class="login__navbar" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="login__navbar-bar" :style="{ height: `${navBarHeight}px` }">
        <view class="login__navbar-btn" hover-class="login__navbar-btn--active" @click="handleBack">
          <text class="login__navbar-icon">‹</text>
        </view>
        <view class="login__navbar-title" />
        <view class="login__navbar-placeholder" />
      </view>
    </view>

    <!-- 主内容 -->
    <view class="login__body" :style="{ paddingTop: `${statusBarHeight + navBarHeight + 4}px` }">
      <!-- Logo -->
      <view class="login__brand">
        <image class="login__logo" src="/static/images/tongyan-logo-flat.png" mode="aspectFit" />
        <text class="login__brand-name">同檐时光</text>
      </view>

      <!-- 登录卡片 -->
      <view class="login__card">
        <!-- 标题 -->
        <view class="login__card-head">
          <text class="login__card-title">欢迎登录</text>
          <text class="login__card-subtitle">使用微信快捷登录</text>
        </view>

        <!-- #ifdef MP-WEIXIN -->
        <!-- 微信登录区域 -->
        <view class="login__form">
          <view class="login__form-item">
            <button class="login__wx-btn" :disabled="isLoading" @click="handleWechatLogin">
              <image class="login__wx-btn-icon" src="/static/icons/weixin.png" mode="aspectFit" />
              微信一键登录
            </button>
          </view>
        </view>

        <!-- 协议勾选 -->
        <view class="login__policy">
          <wd-checkbox v-model="isAgreePolicy" type="square" size="32rpx">
            <text class="login__policy-text">
              我已阅读并同意
              <text class="login__policy-link" @click.stop="navigateToAgreement('user')">
                《用户协议》
              </text>
              与
              <text class="login__policy-link" @click.stop="navigateToAgreement('privacy')">
                《隐私政策》
              </text>
            </text>
          </wd-checkbox>
        </view>
        <!-- #endif -->
      </view>
    </view>

    <!-- 协议确认弹窗 -->
    <wd-dialog selector="policy-box" root-portal />

    <wd-toast />
  </view>
</template>

<script lang="ts" setup>
import { onLoad, onShow } from "@dcloudio/uni-app";
import { useToast, useDialog } from "@wot-ui/ui";

import { useUserStore } from "@/store/modules/user";

definePage({
  name: "login",
  style: { navigationStyle: "custom", navigationBarTitleText: "" },
});

const toast = useToast();
const dialog = useDialog("policy-box");
const userStore = useUserStore();

// 导航栏尺寸
const statusBarHeight = ref(20);
const navBarHeight = ref(44);

// 登录状态
const isLoading = ref(false);
const isAgreePolicy = ref(false);
const redirect = ref("/pages/index/index");

// 协议弹窗
const openPolicyDialog = () => {
  dialog
    .confirm({
      title: "提示",
      msg: "请阅读并同意《用户协议》与《隐私政策》",
    })
    .then(async () => {
      isAgreePolicy.value = true;
      await doWechatLogin();
    })
    .catch(() => undefined);
};

// 微信登录
const handleWechatLogin = async () => {
  if (isLoading.value) return;
  if (!isAgreePolicy.value) {
    openPolicyDialog();
    return;
  }
  await doWechatLogin();
};

async function doWechatLogin() {
  isLoading.value = true;
  try {
    const { code } = await uni.login();
    const loginResult = await userStore.loginByWxMa(code);
    await userStore.getInfo();
    toast.success("登录成功");
    const shouldCompleteProfile = loginResult.isNewUser || !userStore.isUserInfoComplete();
    const targetUrl = shouldCompleteProfile
      ? `/pages/mine/profile/complete-profile?redirect=${encodeURIComponent(redirect.value)}`
      : redirect.value;
    setTimeout(() => uni.reLaunch({ url: targetUrl }), 800);
  } catch (error: any) {
    toast.error(error?.message || "微信登录失败");
  } finally {
    isLoading.value = false;
  }
}

const navigateToAgreement = (type: string) => {
  const url =
    type === "user" ? "/pages/mine/settings/agreement/index" : "/pages/mine/settings/privacy/index";
  uni.navigateTo({ url });
};

const handleBack = () => {
  if (getCurrentPages().length > 1) {
    uni.navigateBack();
    return;
  }
  uni.reLaunch({ url: "/pages/index/index" });
};

// 生命周期
onLoad((options: any) => {
  const fromQuery = options?.redirect ? decodeURIComponent(options.redirect) : "";
  if (fromQuery && fromQuery !== "/pages/login/index") redirect.value = fromQuery;
  uni.setNavigationBarTitle({ title: "" });
  const systemInfo = uni.getSystemInfoSync();
  statusBarHeight.value = systemInfo.statusBarHeight || 20;
  navBarHeight.value = 44;
  // #ifdef MP-WEIXIN
  const menuButton = uni.getMenuButtonBoundingClientRect();
  navBarHeight.value = menuButton.height + (menuButton.top - statusBarHeight.value) * 2;
  // #endif
});

onShow(() => uni.setNavigationBarTitle({ title: "" }));
</script>

<style lang="scss" scoped>
.login {
  background: linear-gradient(
    135deg,
    var(--color-bg-tertiary) 0%,
    var(--color-bg) 50%,
    var(--color-primary-light) 100%
  );
}

.login__decoration {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  overflow: hidden;
  pointer-events: none;
}

.login__circle {
  position: absolute;
  filter: blur(64px);
  border-radius: 50%;

  &--1 {
    top: -160rpx;
    left: -160rpx;
    width: 480rpx;
    height: 480rpx;
    background-color: var(--color-primary-alpha-20);
  }

  &--2 {
    right: -160rpx;
    bottom: -160rpx;
    width: 640rpx;
    height: 640rpx;
    background-color: var(--color-primary-alpha-15);
  }
}

.login__navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: var(--z-navbar);
  padding: 0 32rpx;
}

.login__navbar-bar {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login__navbar-btn,
.login__navbar-placeholder {
  position: absolute;
  top: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72rpx;
  height: 72rpx;
  transform: translateY(-50%);
}

.login__navbar-btn {
  left: 0;
  background-color: var(--color-glass);
  border: 2rpx solid var(--color-border-glass);
  border-radius: 999rpx;

  &--active {
    opacity: 0.8;
  }
}

.login__navbar-placeholder {
  right: 0;
}

.login__navbar-icon {
  margin-top: -4rpx;
  font-size: 44rpx;
  font-weight: 500;
  line-height: 1;
  color: var(--color-text);
}

.login__navbar-title {
  font-size: 32rpx;
  font-weight: 600;
  letter-spacing: 0.08em;
  color: var(--color-text);
}

.login__body {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 96rpx;
}

.login__brand {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 8rpx;
  margin-bottom: 40rpx;
}

.login__logo {
  width: 120rpx;
  height: 120rpx;
  margin-bottom: 20rpx;
}

.login__brand-name {
  font-size: 36rpx;
  font-weight: 600;
  letter-spacing: 0.05em;
  color: var(--color-text);
}

.login__card {
  width: 100%;
  padding: 44rpx;
  background-color: var(--color-bg);
  border: 2rpx solid var(--color-border-light);
  border-radius: 48rpx;
  box-shadow: 0 20rpx 50rpx -10rpx rgba(0, 0, 0, 0.1);

  @supports (backdrop-filter: blur(24px)) or (-webkit-backdrop-filter: blur(24px)) {
    background-color: var(--color-bg-alpha-95);
    -webkit-backdrop-filter: blur(24px);
    backdrop-filter: blur(24px);
  }
}

// 卡片头部
.login__card-head {
  margin-bottom: 36rpx;
  text-align: center;
}

.login__card-title {
  font-size: 40rpx;
  font-weight: 700;
  color: var(--color-text);
}

.login__card-subtitle {
  display: block;
  margin-top: 8rpx;
  font-size: 26rpx;
  color: var(--color-text-secondary);
}

.login__form-item {
  margin-top: 28rpx;

  &:first-child {
    margin-top: 0;
  }
}

// 覆盖 wd-form-item 默认样式，仅用于校验和错误提示
:deep(.wd-form-item) {
  padding: 0 !important;
  margin-top: 28rpx;
  background: transparent;

  &:first-child {
    margin-top: 0;
  }
}

// 垂直布局下隐藏左侧区域（无标题时）
:deep(.wd-form-item .wd-cell__left) {
  display: none;
}

:deep(.wd-form-item .wd-cell__wrapper) {
  flex-direction: column;
}

:deep(.wd-form-item .wd-cell__right) {
  width: 100%;
}

// 错误提示：独占一行，显示在输入框下方
:deep(.wd-form-item__error-message) {
  display: block !important;
  width: 100% !important;
  padding: 8rpx 0 0 !important;
  font-size: 24rpx !important;
  line-height: 1.5 !important;
  color: var(--color-danger) !important;
}

// 输入框行
.login__field {
  display: flex;
  align-items: center;
  height: 88rpx;
  padding: 0 32rpx;
  background-color: var(--color-bg-secondary);
  border-radius: 24rpx;
}

.login__field-input {
  flex: 1;
  height: 100%;
  margin-left: 24rpx;
  font-size: 28rpx;
  color: var(--color-text);
}

// 图形验证码图片
.login__captcha-img {
  width: 200rpx;
  height: 72rpx;
  border-radius: 12rpx;
}

.login__wx-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 88rpx;
  font-size: 32rpx;
  font-weight: 600;
  color: var(--color-text-inverse);
  background-color: var(--color-wx-green);
  border-radius: 24rpx;
  box-shadow: 0 20rpx 30rpx -6rpx rgba(34, 197, 94, 0.3);

  &:active {
    transform: scale(0.98);
  }
}

.login__wx-btn-icon {
  width: 40rpx;
  height: 40rpx;
  margin-right: 16rpx;
}

.login__code-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 64rpx;
  padding: 0 32rpx;
  font-size: 28rpx;
  font-weight: 500;
  border-radius: 16rpx;
  &--active {
    color: var(--color-primary);
    background-color: var(--color-primary-light);
  }

  &--disabled {
    color: var(--color-text-placeholder);
    background-color: var(--color-bg-tertiary);
  }
}

.login__mode-switch {
  display: flex;
  justify-content: center;
  padding-top: 24rpx;
}

.login__mode-switch-text {
  font-size: 28rpx;
  color: var(--color-text-secondary);
}

.login__mode-switch-link {
  margin-left: 8rpx;
  font-size: 28rpx;
  font-weight: 500;
  color: var(--color-primary);
  border-bottom: 2rpx solid var(--color-primary);
}

.login__divider {
  display: flex;
  align-items: center;
  margin: 32rpx 0;
}

.login__divider-line {
  flex: 1;
  height: 2rpx;
  background-color: var(--color-border);
}

.login__divider-text {
  padding: 0 32rpx;
  font-size: 24rpx;
  color: var(--color-text-placeholder);
}

.login__oauth-row {
  display: flex;
  justify-content: center;
  gap: 64rpx;
}

.login__wx-icon {
  width: 72rpx;
  height: 72rpx;

  &:active {
    transform: scale(0.95);
  }
}

.login__policy {
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 24rpx;
  margin-top: 32rpx;
  border-top: 2rpx solid var(--color-border-light);
}

.login__policy-text {
  font-size: 24rpx;
  line-height: 1.5;
  color: var(--color-text-secondary);
}

.login__policy-link {
  color: var(--color-primary);
}

.login__demo-hint {
  display: flex;
  justify-content: center;
  margin-top: 24rpx;
}

.login__demo-hint-text {
  padding: 8rpx 24rpx;
  font-size: 24rpx;
  color: var(--color-warning);
  background-color: var(--color-warning-light);
  border-radius: 8rpx;
}
</style>

<style lang="scss">
/* 暗黑模式覆盖 - 非 scoped，因 .wot-theme-dark 位于页面根元素 */

.wot-theme-dark {
  .login__card {
    background-color: #252d3a;
    border-color: rgba(255, 255, 255, 0.08);
    box-shadow: 0 20rpx 50rpx -10rpx rgba(0, 0, 0, 0.4);
  }

  .login__divider-line {
    background-color: rgba(255, 255, 255, 0.12);
  }

  .login__field {
    background-color: rgba(255, 255, 255, 0.06);
  }

  .login__navbar-btn {
    background-color: rgba(255, 255, 255, 0.08);
    border-color: rgba(255, 255, 255, 0.08);
  }
}
</style>
