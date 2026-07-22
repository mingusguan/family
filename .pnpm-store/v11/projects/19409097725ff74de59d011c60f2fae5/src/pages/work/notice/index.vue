<template>
  <view class="page page--padding">
    <view>
      <wd-search v-model="queryParams.title" placeholder="搜索通知标题" hide-cancel @search="handleSearch" />
    </view>

    <!-- 通知列表 -->
    <view class="mt-16rpx">
      <wd-card v-for="item in pageData" :key="item.id" custom-class="item-card" @click="openNoticeDetail(item)">
        <!-- 主信息行 -->
        <view class="flex-start">
          <view class="notice-card__main">
            <view class="flex-start mt-12rpx">
              <text class="notice-card__title">{{ item.title }}</text>
            </view>
            <text class="notice-card__publisher">
              {{ item.publisherName || "系统管理员" }}
            </text>
          </view>
          <wd-tag :type="getStatusType(item.publishStatus)" variant="plain">
            {{ getStatusText(item.publishStatus) }}
          </wd-tag>
        </view>

        <!-- 辅助信息行 -->
        <view class="notice-card__meta">
          <view class="notice-card__detail">
            <wd-icon name="user" size="16" class="color-text-secondary" />
            <text class="notice-card__detail-text">
              {{ item.targetType === 1 ? "全体" : "指定用户" }}
            </text>
          </view>
          <view class="notice-card__detail">
            <wd-icon name="warning" size="16" class="color-text-secondary" />
            <text class="notice-card__detail-text">
              {{ getLevelText(item.level) }}
            </text>
          </view>
        </view>

        <!-- 元信息行 -->
        <view class="notice-card__footer">
          <text class="notice-card__time">{{ formatTime(item) }}</text>
          <view class="notice-card__action" hover-class="notice-card__action--hover" @click.stop="showNoticeActions(item)">
            <wd-icon name="more" size="16" class="color-text-secondary" />
          </view>
        </view>
      </wd-card>

      <wd-loadmore v-if="total > 0" :state="loadMoreState" @reload="fetchNoticeList" />
      <wd-empty v-else-if="total === 0" icon="search" tip="暂无数据" />
    </view>

    <!-- 详情弹窗 -->
    <wd-popup v-model="detailDialog.visible" position="bottom" custom-class="popup-bottom" @close="closeNoticeDetail">
      <view class="p-4">
        <view class="popup-title">通知详情</view>
        <wd-cell-group border>
          <wd-cell title="标题" :value="noticeDetail.title" />
          <wd-cell title="发布状态">
            <template #default>
              <wd-tag :type="getStatusType(noticeDetail.publishStatus)" size="small">
                {{ getStatusText(noticeDetail.publishStatus) }}
              </wd-tag>
            </template>
          </wd-cell>
          <wd-cell title="发布人" :value="noticeDetail.publisherName" />
          <wd-cell title="发布时间" :value="String(noticeDetail.publishTime || '-')" />
        </wd-cell-group>
        <view class="mt-4 p-4 bg-[var(--color-bg-secondary)] rounded-lg">
          <rich-text :nodes="noticeDetail.content" class="text-28rpx" />
        </view>
        <view class="popup-actions">
          <wd-button type="info" variant="plain" block @click="closeNoticeDetail">关闭</wd-button>
        </view>
      </view>
    </wd-popup>

    <!-- 新增/编辑弹窗 -->
    <wd-popup v-model="formDialog.visible" position="bottom" custom-class="popup-bottom" @close="closeNoticeForm">
      <view class="p-4">
        <view class="popup-title">
          {{ formData.id ? "编辑通知" : "新增通知" }}
        </view>
        <wd-form ref="formRef" :model="formData" :schema="formRules">
          <wd-form-item prop="title" title="标题" required>
            <wd-input v-model="formData.title" placeholder="请输入通知标题" />
          </wd-form-item>
          <wd-form-item prop="level" title="优先级">
            <wd-radio-group v-model="formData.level" type="button">
              <wd-radio value="L">低</wd-radio>
              <wd-radio value="M">中</wd-radio>
              <wd-radio value="H">高</wd-radio>
            </wd-radio-group>
          </wd-form-item>
          <wd-form-item prop="targetType" title="目标类型">
            <wd-radio-group v-model="formData.targetType" type="button">
              <wd-radio :value="1">全体</wd-radio>
              <wd-radio :value="2">指定用户</wd-radio>
            </wd-radio-group>
          </wd-form-item>
          <wd-form-item prop="content" title="内容">
            <wd-textarea v-model="formData.content" placeholder="请输入通知内容" :maxlength="500" show-word-limit />
          </wd-form-item>
        </wd-form>
        <view class="popup-actions">
          <wd-button type="info" variant="plain" @click="closeNoticeForm">取消</wd-button>
          <wd-button type="primary" :loading="isSubmitting" @click="submitNoticeForm">
            保存
          </wd-button>
        </view>
      </view>
    </wd-popup>

    <wd-action-sheet v-model="actionSheetVisible" :actions="actionSheetActions" cancel-text="取消" @select="handleActionSelect" />

    <wd-fab v-if="hasPermission('sys:notice:create') && !formDialog.visible && !detailDialog.visible" :expandable="false" :gap="{ bottom: 32 }"
      @click="openNoticeForm()" />
  </view>
</template>

<script lang="ts" setup>

  import { onLoad, onReachBottom } from "@dcloudio/uni-app";
  import { LoadMoreState } from "@wot-ui/ui/components/wd-loadmore/types";
  import { toFormSchema } from "@/utils/form";
  import { useToast, useDialog } from "@wot-ui/ui";
  import NoticeAPI, {
    type NoticePageQuery,
    NoticeItem,
    NoticeDetail,
    NoticeForm,
  } from "@/api/notice";
  import { hasPermission } from "@/utils/permission";


  definePage({
    name: "notice",
    style: { navigationBarTitleText: "通知公告" },
  });

  const toast = useToast();
  const { confirm } = useDialog();
  const loadMoreState = ref<LoadMoreState>("loading");
  const formRef = ref();
  const isSubmitting = ref(false);

  const queryParams = reactive<NoticePageQuery>({ pageNum: 1, pageSize: 10 });
  const total = ref(0);
  const pageData = ref<NoticeItem[]>([]);

  const noticeDetail = ref<NoticeDetail>({});
  const detailDialog = reactive({ visible: false });

  const formDialog = reactive({ visible: false });
  const initialFormData: NoticeForm = {
    id: undefined,
    title: undefined,
    content: undefined,
    type: undefined,
    level: "M",
    targetType: 1,
    targetUserIds: undefined,
  };
  const formData = reactive<NoticeForm>({ ...initialFormData });

  const formRules = toFormSchema({
    title: [{ required: true, message: "请输入通知标题" }],
  });

  // 获取状态样式
  const getStatusType = (
    status?: number
  ): "default" | "primary" | "danger" | "warning" | "success" => {
    const map: Record<number, "default" | "primary" | "danger" | "warning" | "success"> = {
      0: "primary",
      1: "success",
      [-1]: "warning",
    };
    return status !== undefined ? map[status] || "default" : "default";
  };

  // 获取状态文本
  const getStatusText = (status?: number): string => {
    const map: Record<number, string> = { 0: "未发布", 1: "已发布", [-1]: "已撤回" };
    return status !== undefined ? map[status] || "未知" : "-";
  };

  // 获取级别文本
  const getLevelText = (level?: string | number): string => {
    const map: Record<string, string> = { L: "低", M: "中", H: "高" };
    return level ? map[String(level)] || String(level) : "-";
  };

  // 格式化时间
  const formatTime = (item: NoticeItem): string => {
    if (item.publishStatus === 1 && item.publishTime) {
      return String(item.publishTime);
    }
    if (item.publishStatus === -1 && item.revokeTime) {
      return String(item.revokeTime);
    }
    return "-";
  };

  // 搜索触发
  const handleSearch = () => loadNoticeList();

  // 加载列表
  function loadNoticeList() {
    queryParams.pageNum = 1;
    fetchNoticeList();
  }

  // 分页加载列表
  function fetchNoticeList() {
    loadMoreState.value = "loading";
    NoticeAPI.getPage(queryParams)
      .then((data) => {
        pageData.value = data.list;
        total.value = data.total;
        queryParams.pageNum++;
      })
      .catch(() => {
        pageData.value = [];
      })
      .finally(() => {
        loadMoreState.value = "finished";
      });
  }

  // 打开详情弹窗
  async function openNoticeDetail(item: NoticeItem) {
    const detail = await NoticeAPI.getDetail(item.id);
    noticeDetail.value = detail;
    detailDialog.visible = true;
  }

  // 关闭详情弹窗
  function closeNoticeDetail() {
    detailDialog.visible = false;
  }

  // 打开表单弹窗
  async function openNoticeForm(id?: number) {
    formRef.value?.reset();
    Object.assign(formData, initialFormData);
    formDialog.visible = true;
    if (id) {
      formData.id = id;
      const data = await NoticeAPI.getFormData(id);
      Object.assign(formData, data, { id });
    }
  }

  // 关闭表单弹窗
  function closeNoticeForm() {
    formDialog.visible = false;
    formRef.value?.reset();
    Object.assign(formData, initialFormData);
  }

  // 提交表单
  function submitNoticeForm() {
    formRef.value.validate().then(({ valid }: { valid: boolean }) => {
      if (!valid) return;
      isSubmitting.value = true;
      const id = formData.id;
      const action = id ? NoticeAPI.update(id, formData) : NoticeAPI.add(formData);
      action
        .then(() => {
          toast.success("操作成功");
          closeNoticeForm();
          loadNoticeList();
        })
        .finally(() => {
          isSubmitting.value = false;
        });
    });
  }

  // 更多操作
  const actionSheetVisible = ref(false);
  const actionSheetActions = ref<{ name: string; color?: string }[]>([]);
  const pendingAction = ref<Record<string, () => void>>({});

  function showNoticeActions(item: NoticeItem) {
    const actions: { name: string; color?: string }[] = [{ name: "查看" }];
    const actionMap: Record<string, () => void> = {
      查看: () => openNoticeDetail(item),
    };

    if (item.publishStatus !== 1) {
      if (hasPermission("sys:notice:update")) {
        actions.push({ name: "编辑" });
        actionMap["编辑"] = () => openNoticeForm(Number(item.id));
      }
      if (hasPermission("sys:notice:delete")) {
        actions.push({ name: "删除", color: "var(--color-danger)" });
        actionMap["删除"] = async () => {
          try {
            await confirm({
              title: "确认删除",
              msg: `确定要删除通知「${item.title}」吗？`,
              headerImage: "warning",
            });
            await NoticeAPI.deleteByIds(item.id);
            toast.success("删除成功");
            loadNoticeList();
          } catch {
            // 用户取消操作
          }
        };
      }
      if (hasPermission("sys:notice:publish")) {
        actions.push({ name: "发布" });
        actionMap["发布"] = async () => {
          try {
            await confirm({
              title: "确认发布",
              msg: `确定要发布通知「${item.title}」吗？`,
              headerImage: "warning",
            });
            await NoticeAPI.publish(Number(item.id));
            toast.success("发布成功");
            loadNoticeList();
          } catch {
            // 用户取消操作
          }
        };
      }
    } else {
      if (hasPermission("sys:notice:revoke")) {
        actions.push({ name: "撤回", color: "var(--color-warning)" });
        actionMap["撤回"] = async () => {
          try {
            await confirm({
              title: "确认撤回",
              msg: `确定要撤回通知「${item.title}」吗？`,
              headerImage: "warning",
            });
            await NoticeAPI.revoke(Number(item.id));
            toast.success("撤回成功");
            loadNoticeList();
          } catch {
            // 用户取消操作
          }
        };
      }
    }

    actionSheetActions.value = actions;
    pendingAction.value = actionMap;
    actionSheetVisible.value = true;
  }

  function handleActionSelect({ item }: { item: any }) {
    pendingAction.value[item.name]?.();
  }

  onReachBottom(() => {
    if (queryParams.pageNum * queryParams.pageSize < total.value) {
      fetchNoticeList();
    } else {
      loadMoreState.value = "finished";
    }
  });

  onLoad(() => {
    loadNoticeList();
  });
</script>

<script lang="ts">
  export default { options: { styleIsolation: "shared" } };
</script>



<style lang="scss" scoped>
  .notice-card__main {
    flex: 1;
  }

  .notice-card__title {
    font-weight: 700;
    font-size: 32rpx;
  }

  .notice-card__publisher {
    font-size: 24rpx;
    color: var(--color-text-secondary);
  }

  .notice-card__meta {
    display: flex;
    gap: 24rpx;
    margin-top: 12rpx;
  }

  .notice-card__detail {
    display: flex;
    align-items: flex-start;
    min-width: 0;
  }

  .notice-card__detail-text {
    margin-left: 8rpx;
    font-size: 24rpx;
    color: var(--color-text-secondary);
  }

  .notice-card__footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 16rpx;
  }

  .notice-card__time {
    font-size: 24rpx;
    color: var(--color-text-placeholder);
  }

  .notice-card__action {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 64rpx;
    height: 64rpx;
    border-radius: 50%;
  }

  .notice-card__action--hover {
    background: rgba(var(--color-text-placeholder-rgb, 148, 163, 184), 0.16);
  }
</style>
