<template>
  <view class="plan-page page--tabbar">
    <custom-navbar title="家庭计划" :show-back="false" />

    <view class="plan-toolbar">
      <picker
        v-if="families.length"
        :range="families"
        range-key="name"
        :value="familyIndex"
        @change="handleFamilyChange"
      >
        <view class="family-selector">
          <wd-icon name="home" size="18" color="#7464db" />
          <text>{{ currentFamily?.name }}</text>
          <wd-icon name="arrow-down" size="14" color="#928aa3" />
        </view>
      </picker>
      <wd-button v-if="families.length" size="small" type="primary" round @click="openEditor()">
        <wd-icon name="add" size="16" />
        新建日程
      </wd-button>
    </view>

    <view v-if="loading" class="plan-state">
      <wd-loading color="#7567df" />
      <text>正在读取家庭日程...</text>
    </view>

    <view v-else-if="families.length === 0" class="plan-state plan-state--empty">
      <view class="plan-state__icon"><wd-icon name="home" size="48rpx" color="#8175df" /></view>
      <text class="plan-state__title">先创建一个家庭</text>
      <text class="plan-state__desc">创建家庭后即可安排计划、生日与纪念日</text>
      <wd-button type="primary" round @click="openFamilies">去创建家庭</wd-button>
    </view>

    <template v-else>
      <view v-if="upcomingSchedules.length" class="schedule-section">
        <text class="schedule-section__title">接下来</text>
        <view
          v-for="item in upcomingSchedules"
          :id="`schedule-${item.id}`"
          :key="item.id"
          class="schedule-card"
          :class="[
            `schedule-card--${item.type.toLowerCase()}`,
            { 'schedule-card--highlighted': highlightedScheduleId === item.id },
          ]"
          @click="openEditor(item)"
        >
          <view class="schedule-card__date">
            <text class="schedule-card__day">{{ formatDay(displayEventTime(item)) }}</text>
            <text class="schedule-card__month">{{ formatMonth(displayEventTime(item)) }}</text>
          </view>
          <view class="schedule-card__body">
            <view class="schedule-card__heading">
              <text class="schedule-card__title">{{ item.title }}</text>
              <text class="schedule-card__type">{{ item.typeLabel }}</text>
            </view>
            <text class="schedule-card__time">{{ formatDateTime(displayEventTime(item)) }}</text>
            <text v-if="item.description" class="schedule-card__desc">{{ item.description }}</text>
          </view>
          <view class="schedule-card__actions" @click.stop>
            <wd-icon
              v-if="item.type === 'PLAN'"
              name="check"
              size="18"
              color="#55a979"
              @click="completeSchedule(item)"
            />
            <wd-icon name="delete" size="18" color="#c48a94" @click="deleteSchedule(item)" />
          </view>
        </view>
      </view>

      <view v-if="completedSchedules.length" class="schedule-section">
        <text class="schedule-section__title">已完成</text>
        <view
          v-for="item in completedSchedules"
          :key="item.id"
          class="schedule-card schedule-card--completed"
        >
          <view class="schedule-card__body">
            <text class="schedule-card__title">{{ item.title }}</text>
            <text class="schedule-card__time">{{ formatDateTime(displayEventTime(item)) }}</text>
          </view>
          <wd-icon name="delete" size="18" color="#b8b2c0" @click="deleteSchedule(item)" />
        </view>
      </view>

      <view v-if="!upcomingSchedules.length && !completedSchedules.length" class="plan-state plan-state--empty">
        <view class="plan-state__icon"><wd-icon name="calendar" size="48rpx" color="#8175df" /></view>
        <text class="plan-state__title">还没有家庭日程</text>
        <text class="plan-state__desc">记录聚餐、出游、生日或纪念日，重要时刻不再错过</text>
        <wd-button type="primary" round @click="openEditor()">添加第一条日程</wd-button>
      </view>
    </template>

    <view v-if="editorVisible" class="editor-mask" @click="closeEditor">
      <view class="editor-panel" @click.stop>
        <view class="editor-panel__header">
          <text>{{ editingId ? "编辑家庭日程" : "新建家庭日程" }}</text>
          <wd-icon name="close" size="20" color="#777080" @click="closeEditor" />
        </view>

        <view class="form-item">
          <text class="form-label">日程类型</text>
          <picker :range="typeOptions" range-key="label" :value="typeIndex" @change="handleTypeChange">
            <view class="form-control">{{ typeOptions[typeIndex].label }}</view>
          </picker>
        </view>
        <view class="form-item">
          <text class="form-label">标题</text>
          <input v-model="form.title" class="form-control" :maxlength="80" placeholder="例如：家庭聚餐" />
        </view>
        <view class="form-grid">
          <view class="form-item">
            <text class="form-label">日期</text>
            <picker mode="date" :value="form.date" @change="handleDateChange">
              <view class="form-control">{{ form.date }}</view>
            </picker>
          </view>
          <view class="form-item">
            <text class="form-label">时间</text>
            <picker mode="time" :value="form.time" @change="handleTimeChange">
              <view class="form-control">{{ form.time }}</view>
            </picker>
          </view>
        </view>
        <view class="form-item">
          <text class="form-label">说明</text>
          <textarea v-model="form.description" class="form-textarea" :maxlength="255" placeholder="补充地点、准备事项等" />
        </view>
        <view v-if="form.type !== 'PLAN'" class="repeat-row">
          <view>
            <text class="form-label">每年提醒</text>
            <text class="repeat-row__hint">适合生日、纪念日和固定节日</text>
          </view>
          <switch color="#7567df" :checked="form.repeatYearly" @change="handleRepeatChange" />
        </view>
        <wd-button block type="primary" :loading="submitting" @click="submitSchedule">
          {{ editingId ? "保存修改" : "创建日程" }}
        </wd-button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from "vue";
import { onShow } from "@dcloudio/uni-app";
import FamilyAPI, { type FamilyInfo } from "@/api/family";
import FamilyCardAPI, {
  type FamilySchedule,
  type FamilyScheduleType,
} from "@/api/family-card";
import { getAccessToken } from "@/utils/auth";

definePage({ name: "plan", style: { navigationStyle: "custom" }, layout: "tabbar" });

const typeOptions: Array<{ label: string; value: FamilyScheduleType }> = [
  { label: "家庭计划", value: "PLAN" },
  { label: "生日", value: "BIRTHDAY" },
  { label: "纪念日", value: "ANNIVERSARY" },
  { label: "节日", value: "FESTIVAL" },
];
const families = ref<FamilyInfo[]>([]);
const schedules = ref<FamilySchedule[]>([]);
const selectedFamilyId = ref<number>();
const loading = ref(false);
const submitting = ref(false);
const editorVisible = ref(false);
const editingId = ref<number>();
const highlightedScheduleId = ref<number>();
const FAMILY_PLAN_TARGET_KEY = "family-plan-target";

const tomorrow = new Date(Date.now() + 24 * 60 * 60 * 1000);
const pad = (value: number) => String(value).padStart(2, "0");
const initialDate = `${tomorrow.getFullYear()}-${pad(tomorrow.getMonth() + 1)}-${pad(tomorrow.getDate())}`;
const form = reactive({
  type: "PLAN" as FamilyScheduleType,
  title: "",
  description: "",
  date: initialDate,
  time: "19:00",
  repeatYearly: false,
});

const familyIndex = computed(() =>
  Math.max(0, families.value.findIndex((item) => item.id === selectedFamilyId.value))
);
const currentFamily = computed(() => families.value[familyIndex.value]);
const typeIndex = computed(() => Math.max(0, typeOptions.findIndex((item) => item.value === form.type)));
const upcomingSchedules = computed(() => schedules.value.filter((item) => item.status === 1));
const completedSchedules = computed(() => schedules.value.filter((item) => item.status === 2));

async function loadPage() {
  if (!getAccessToken()) {
    families.value = [];
    schedules.value = [];
    return;
  }
  loading.value = true;
  try {
    const target = uni.getStorageSync(FAMILY_PLAN_TARGET_KEY) as {
      familyId?: number;
      scheduleId?: number;
    } | undefined;
    uni.removeStorageSync(FAMILY_PLAN_TARGET_KEY);
    families.value = await FamilyAPI.listMyFamilies();
    if (!families.value.length) {
      selectedFamilyId.value = undefined;
      schedules.value = [];
      return;
    }
    if (target?.familyId && families.value.some((item) => item.id === target.familyId)) {
      selectedFamilyId.value = target.familyId;
    } else if (!families.value.some((item) => item.id === selectedFamilyId.value)) {
      selectedFamilyId.value = families.value[0].id;
    }
    await loadSchedules();
    if (target?.scheduleId && schedules.value.some((item) => item.id === target.scheduleId)) {
      highlightedScheduleId.value = target.scheduleId;
      // 等加载态关闭并渲染日程卡片后，再滚动到轮播卡片对应的日程。
      setTimeout(() => {
        uni.pageScrollTo({ selector: `#schedule-${target.scheduleId}`, duration: 300 });
      }, 80);
    } else {
      highlightedScheduleId.value = undefined;
    }
  } catch (error) {
    console.error("家庭日程加载失败", error);
    uni.showToast({ title: "家庭日程加载失败", icon: "none" });
  } finally {
    loading.value = false;
  }
}

async function loadSchedules() {
  if (!selectedFamilyId.value) return;
  schedules.value = await FamilyCardAPI.listSchedules(selectedFamilyId.value);
}

function handleFamilyChange(event: any) {
  const index = Number(event.detail.value);
  selectedFamilyId.value = families.value[index]?.id;
  loadSchedules();
}

function openEditor(item?: FamilySchedule) {
  editingId.value = item?.id;
  if (item) {
    form.type = item.type;
    form.title = item.title;
    form.description = item.description || "";
    const [date, time = "00:00:00"] = item.eventTime.split(" ");
    form.date = date;
    form.time = time.slice(0, 5);
    form.repeatYearly = !!item.repeatYearly;
  } else {
    Object.assign(form, {
      type: "PLAN",
      title: "",
      description: "",
      date: initialDate,
      time: "19:00",
      repeatYearly: false,
    });
  }
  editorVisible.value = true;
}

function closeEditor() {
  if (!submitting.value) editorVisible.value = false;
}

function handleTypeChange(event: any) {
  const selected = typeOptions[Number(event.detail.value)];
  form.type = selected.value;
  form.repeatYearly = selected.value !== "PLAN";
}

function handleDateChange(event: any) {
  form.date = event.detail.value;
}

function handleTimeChange(event: any) {
  form.time = event.detail.value;
}

function handleRepeatChange(event: any) {
  form.repeatYearly = event.detail.value;
}

async function submitSchedule() {
  const title = form.title.trim();
  if (!selectedFamilyId.value || !title) {
    uni.showToast({ title: "请输入日程标题", icon: "none" });
    return;
  }
  submitting.value = true;
  try {
    const payload = {
      familyId: selectedFamilyId.value,
      type: form.type,
      title,
      description: form.description.trim() || undefined,
      eventTime: `${form.date} ${form.time}:00`,
      repeatYearly: form.type === "PLAN" ? false : form.repeatYearly,
    };
    if (editingId.value) await FamilyCardAPI.updateSchedule(editingId.value, payload);
    else await FamilyCardAPI.createSchedule(payload);
    uni.showToast({ title: editingId.value ? "修改成功" : "创建成功", icon: "success" });
    editorVisible.value = false;
    await loadSchedules();
  } catch (error: any) {
    uni.showToast({ title: error?.message || "保存失败", icon: "none" });
  } finally {
    submitting.value = false;
  }
}

function completeSchedule(item: FamilySchedule) {
  uni.showModal({
    title: "完成计划",
    content: `确认完成“${item.title}”吗？`,
    success: async (result) => {
      if (!result.confirm) return;
      await FamilyCardAPI.completeSchedule(item.id);
      await loadSchedules();
    },
  });
}

function deleteSchedule(item: FamilySchedule) {
  uni.showModal({
    title: "删除日程",
    content: `确认删除“${item.title}”吗？`,
    confirmColor: "#d45b6a",
    success: async (result) => {
      if (!result.confirm) return;
      await FamilyCardAPI.deleteSchedule(item.id);
      await loadSchedules();
    },
  });
}

function displayEventTime(item: FamilySchedule) {
  if (!item.repeatYearly) return item.eventTime;
  const now = new Date();
  const month = Number(item.eventTime.slice(5, 7));
  const day = Number(item.eventTime.slice(8, 10));
  const hour = Number(item.eventTime.slice(11, 13));
  const minute = Number(item.eventTime.slice(14, 16));
  const candidate = new Date(now.getFullYear(), month - 1, day, hour, minute);
  const year = candidate.getTime() < now.getTime() ? now.getFullYear() + 1 : now.getFullYear();
  return year + item.eventTime.slice(4);
}
function formatDateTime(value: string) {
  return value ? value.slice(0, 16).replace("-", "年").replace("-", "月").replace(" ", "日 ") : "";
}

function formatDay(value: string) {
  return value?.slice(8, 10) || "--";
}

function formatMonth(value: string) {
  return value ? Number(value.slice(5, 7)) + "月" : "";
}

function openFamilies() {
  uni.navigateTo({ url: "/pages/mine/family/index" });
}

onShow(loadPage);
</script>

<style lang="scss" scoped>
.plan-page {
  min-height: 100vh;
  padding-bottom: 160rpx;
  background: #f6f5fb;
}

.plan-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx;
}

.family-selector {
  display: flex;
  gap: 12rpx;
  align-items: center;
  min-width: 260rpx;
  padding: 18rpx 22rpx;
  font-size: 27rpx;
  font-weight: 600;
  color: #40394c;
  background: #fff;
  border-radius: 22rpx;
  box-shadow: 0 12rpx 30rpx rgb(70 55 110 / 8%);
}

.plan-state {
  display: flex;
  gap: 20rpx;
  align-items: center;
  justify-content: center;
  padding: 120rpx 30rpx;
  color: #8e8798;
}

.plan-state--empty {
  flex-direction: column;
  margin: 30rpx 28rpx;
  text-align: center;
  background: #fff;
  border-radius: 32rpx;
}

.plan-state__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 108rpx;
  height: 108rpx;
  background: #eeebff;
  border-radius: 34rpx;
}

.plan-state__title {
  font-size: 31rpx;
  font-weight: 650;
  color: #3e384b;
}

.plan-state__desc {
  max-width: 520rpx;
  margin-bottom: 12rpx;
  font-size: 24rpx;
  line-height: 1.6;
}

.schedule-section {
  padding: 0 28rpx 24rpx;
}

.schedule-section__title {
  display: block;
  margin: 12rpx 6rpx 18rpx;
  font-size: 27rpx;
  font-weight: 650;
  color: #4d4658;
}

.schedule-card {
  display: flex;
  gap: 22rpx;
  align-items: center;
  padding: 24rpx;
  margin-bottom: 18rpx;
  background: #fff;
  border-left: 8rpx solid #7567df;
  border-radius: 26rpx;
  box-shadow: 0 12rpx 30rpx rgb(70 55 110 / 7%);

  &--birthday { border-left-color: #e58fa3; }
  &--anniversary { border-left-color: #d5a75f; }
  &--festival { border-left-color: #63ad8a; }
  &--highlighted {
    box-shadow: 0 0 0 4rpx rgb(117 103 223 / 20%), 0 16rpx 38rpx rgb(70 55 110 / 13%);
  }
  &--completed {
    border-left-color: #c4becd;
    opacity: 0.66;
  }
}

.schedule-card__date {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 82rpx;
  height: 82rpx;
  color: #7567df;
  background: #f0edff;
  border-radius: 20rpx;
}

.schedule-card__day {
  font-size: 30rpx;
  font-weight: 700;
}

.schedule-card__month {
  font-size: 19rpx;
}

.schedule-card__body {
  flex: 1;
  min-width: 0;
}

.schedule-card__heading {
  display: flex;
  gap: 12rpx;
  align-items: center;
}

.schedule-card__title {
  overflow: hidden;
  font-size: 28rpx;
  font-weight: 650;
  color: #3e3848;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.schedule-card__type {
  flex-shrink: 0;
  padding: 4rpx 10rpx;
  font-size: 18rpx;
  color: #7667d8;
  background: #efecff;
  border-radius: 999rpx;
}

.schedule-card__time,
.schedule-card__desc {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #918a9c;
}

.schedule-card__actions {
  display: flex;
  flex-direction: column;
  gap: 22rpx;
  padding: 8rpx;
}

.editor-mask {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: flex;
  align-items: flex-end;
  background: rgb(25 20 35 / 46%);
}

.editor-panel {
  box-sizing: border-box;
  width: 100%;
  max-height: 88vh;
  padding: 34rpx 30rpx calc(36rpx + env(safe-area-inset-bottom));
  overflow-y: auto;
  background: #fff;
  border-radius: 38rpx 38rpx 0 0;
}

.editor-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 30rpx;
  font-size: 32rpx;
  font-weight: 700;
  color: #3e3848;
}

.form-item {
  flex: 1;
  margin-bottom: 24rpx;
}

.form-grid {
  display: flex;
  gap: 18rpx;
}

.form-label {
  display: block;
  margin-bottom: 10rpx;
  font-size: 23rpx;
  color: #6f6878;
}

.form-control,
.form-textarea {
  box-sizing: border-box;
  width: 100%;
  min-height: 78rpx;
  padding: 20rpx 22rpx;
  font-size: 26rpx;
  color: #40394c;
  background: #f7f5fb;
  border-radius: 18rpx;
}

.form-textarea {
  height: 130rpx;
}

.repeat-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18rpx 0 28rpx;
}

.repeat-row__hint {
  display: block;
  margin-top: 6rpx;
  font-size: 20rpx;
  color: #9a94a2;
}
</style>
