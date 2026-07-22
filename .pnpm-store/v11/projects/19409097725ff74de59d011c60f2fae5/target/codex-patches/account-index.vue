<template>
  <view class="page py-16rpx">
    <wd-card>
      <wd-cell-group border>
        <wd-cell icon="user" title="登录账号" :value="userProfile?.username || '-'" />
        <wd-cell icon="wechat" title="登录方式" value="微信" />
        <wd-cell icon="mobile" title="手机号" :value="userProfile?.mobile || '未绑定（非必填）'" />
        <wd-cell title="注册时间" :value="userProfile?.createTime || '-'" />
      </wd-cell-group>
    </wd-card>
  </view>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import UserAPI, { type UserProfile } from "@/api/user";

const userProfile = ref<UserProfile>();

const loadUserProfile = async () => {
  userProfile.value = await UserAPI.getProfile();
};

onMounted(loadUserProfile);
</script>