<template>
  <div class="page-container">
    <el-card class="page-search" shadow="never">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item label="关键字" prop="keyword">
          <el-input
            v-model="queryParams.keyword"
            placeholder="昵称、用户名或手机号"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 110px">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="search" @click="handleQuery">搜索</el-button>
          <el-button icon="refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="page-content" shadow="never">
      <div class="page-toolbar">
        <div class="page-toolbar__left">
          <el-button
            v-hasPerm="['app:user:delete']"
            type="danger"
            icon="delete"
            :disabled="selectedIds.length === 0"
            @click="handleDelete()"
          >
            批量删除
          </el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData.list"
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column label="用户" min-width="210">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="38" :src="row.avatar">
                {{ (row.nickname || row.username || "用").slice(0, 1) }}
              </el-avatar>
              <div>
                <div class="user-name">{{ row.nickname || "未设置昵称" }}</div>
                <div class="text-muted">{{ row.username }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="mobile" label="手机号" width="150">
          <template #default="{ row }">{{ row.mobile || "未绑定" }}</template>
        </el-table-column>
        <el-table-column label="微信小程序" min-width="150">
          <template #default="{ row }">
            <el-tag :type="row.wechatBound ? 'success' : 'info'">
              {{ row.wechatBound ? "已绑定" : "未绑定" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-hasPerm="['app:user:update']"
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column fixed="right" label="操作" width="90">
          <template #default="{ row }">
            <el-button
              v-hasPerm="['app:user:delete']"
              type="danger"
              link
              icon="delete"
              @click="handleDelete(row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-if="tableData.total > 0"
        v-model:total="tableData.total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="fetchData"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox, type FormInstance } from "element-plus";
import AppUserAPI from "@/api/app-user";
import type { AppUserItem, AppUserQuery } from "@/api/app-user";

defineOptions({ name: "AppUser" });

const queryFormRef = ref<FormInstance>();
const queryParams = reactive<AppUserQuery>({ pageNum: 1, pageSize: 10 });
const tableData = reactive({ list: [] as AppUserItem[], total: 0 });
const selectedIds = ref<string[]>([]);
const loading = ref(false);

async function fetchData() {
  loading.value = true;
  try {
    const data = await AppUserAPI.getPage(queryParams);
    tableData.list = data.list || [];
    tableData.total = data.total || 0;
  } finally {
    loading.value = false;
  }
}

function handleQuery() {
  queryParams.pageNum = 1;
  fetchData();
}

function resetQuery() {
  queryFormRef.value?.resetFields();
  handleQuery();
}

function handleSelectionChange(rows: AppUserItem[]) {
  selectedIds.value = rows.map((item) => item.id);
}

async function handleStatusChange(row: AppUserItem) {
  const nextStatus = row.status;
  try {
    await AppUserAPI.updateStatus(row.id, nextStatus);
    ElMessage.success(nextStatus === 1 ? "用户已启用" : "用户已禁用");
  } catch (error) {
    row.status = nextStatus === 1 ? 0 : 1;
    throw error;
  }
}

async function handleDelete(id?: string) {
  const ids = id || selectedIds.value.join(",");
  if (!ids) return;
  await ElMessageBox.confirm("已上传相册资源的用户不能删除，确认继续？", "删除APP用户", {
    type: "warning",
  });
  await AppUserAPI.deleteUsers(ids);
  ElMessage.success("删除成功");
  fetchData();
}

onMounted(fetchData);
</script>

<style scoped>
.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}
.user-name {
  color: var(--el-text-color-primary);
}
.text-muted {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>
