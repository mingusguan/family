<template>
  <view class="placeholder-page page--tabbar">
    <custom-navbar title="菜单" :show-back="false" />
    <view class="placeholder-card">
      <view class="placeholder-card__icon"><wd-icon name="menu" size="64rpx" color="#7c6ff6" /></view>
      <text class="placeholder-card__title">家庭菜单</text>
      <text class="placeholder-card__desc">菜单功能将在相册完成后继续建设</text>
    </view>
  </view>
</template>

<script setup lang="ts">
definePage({ name: "menu", style: { navigationStyle: "custom" }, layout: "tabbar" });
</script>

<style lang="scss" scoped>
.placeholder-page { min-height: 100vh; background: #f6f5fb; }
.placeholder-card { display: flex; flex-direction: column; align-items: center; padding: 180rpx 40rpx 0; }
.placeholder-card__icon { display: flex; align-items: center; justify-content: center; width: 136rpx; height: 136rpx; background: #ebe9f8; border-radius: 42rpx; }
.placeholder-card__title { margin-top: 30rpx; font-size: 34rpx; font-weight: 650; color: #383344; }
.placeholder-card__desc { margin-top: 12rpx; font-size: 25rpx; color: #9a95a5; }
</style>
