<template>
  <div class="page-container">
    <el-card class="page-search" shadow="never">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item label="关键字" prop="keyword">
          <el-input v-model="queryParams.keyword" placeholder="标签名称" clearable @keyup.enter="fetchData" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="search" @click="fetchData">搜索</el-button>
          <el-button icon="refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="page-content" shadow="never">
      <div class="page-toolbar">
        <div class="page-toolbar__left">
          <el-button v-hasPerm="['album:tag:create']" type="success" icon="plus" @click="openDialog()">新增标签</el-button>
          <el-button
            v-hasPerm="['album:tag:delete']"
            type="danger"
            icon="delete"
            :disabled="selectedIds.length === 0"
            @click="handleDelete()"
          >批量删除</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="list" border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column label="标签" min-width="170">
          <template #default="{ row }">
            <el-tag :color="row.color || undefined" effect="plain">{{ row.name }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="color" label="颜色" width="140">
          <template #default="{ row }">
            <span v-if="row.color" class="color-dot" :style="{ backgroundColor: row.color }" />{{ row.color || "默认" }}
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="90" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column fixed="right" label="操作" width="150">
          <template #default="{ row }">
            <el-button v-hasPerm="['album:tag:update']" type="primary" link icon="edit" @click="openDialog(row.id)">编辑</el-button>
            <el-button v-hasPerm="['album:tag:delete']" type="danger" link icon="delete" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.title" width="500px" @closed="resetForm">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="90px">
        <el-form-item label="标签名称" prop="name">
          <el-input v-model="formData.name" maxlength="30" placeholder="请输入标签名称" />
        </el-form-item>
        <el-form-item label="标签颜色" prop="color">
          <el-color-picker v-model="formData.color" show-alpha />
          <el-input v-model="formData.color" placeholder="#1677FF" clearable class="color-input" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" controls-position="right" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from "element-plus";
import AlbumAPI from "@/api/album";
import type { AlbumTagForm, AlbumTagItem } from "@/api/album";

defineOptions({ name: "AlbumTag" });

const queryFormRef = ref<FormInstance>();
const formRef = ref<FormInstance>();
const queryParams = reactive<{ keyword?: string }>({});
const list = ref<AlbumTagItem[]>([]);
const selectedIds = ref<string[]>([]);
const loading = ref(false);
const submitting = ref(false);
const dialog = reactive({ visible: false, title: "" });
const formData = reactive<AlbumTagForm>({ color: "#1677FF", sort: 0 });
const rules: FormRules = {
  name: [{ required: true, message: "请输入标签名称", trigger: "blur" }],
};

async function fetchData() {
  loading.value = true;
  try {
    list.value = (await AlbumAPI.getTags(queryParams)) || [];
  } finally {
    loading.value = false;
  }
}

function resetQuery() {
  queryFormRef.value?.resetFields();
  fetchData();
}

function handleSelectionChange(rows: AlbumTagItem[]) {
  selectedIds.value = rows.map((item) => item.id);
}

async function openDialog(id?: string) {
  resetForm();
  dialog.visible = true;
  dialog.title = id ? "编辑相册标签" : "新增相册标签";
  if (id) Object.assign(formData, await AlbumAPI.getTagForm(id));
}

function resetForm() {
  formRef.value?.resetFields();
  Object.assign(formData, { id: undefined, name: undefined, color: "#1677FF", sort: 0 });
}

async function handleSubmit() {
  if (!(await formRef.value?.validate().catch(() => false))) return;
  submitting.value = true;
  try {
    if (formData.id) await AlbumAPI.updateTag(formData.id, formData);
    else await AlbumAPI.createTag(formData);
    ElMessage.success(formData.id ? "修改成功" : "新增成功");
    dialog.visible = false;
    fetchData();
  } finally {
    submitting.value = false;
  }
}

async function handleDelete(id?: string) {
  const ids = id || selectedIds.value.join(",");
  if (!ids) return;
  await ElMessageBox.confirm("删除标签会同步清理资源关联，确认继续？", "删除确认", { type: "warning" });
  await AlbumAPI.deleteTags(ids);
  ElMessage.success("删除成功");
  fetchData();
}

onMounted(fetchData);
</script>

<style scoped>
.color-dot { display: inline-block; width: 12px; height: 12px; margin-right: 8px; border-radius: 50%; vertical-align: -1px; }
.color-input { width: 160px; margin-left: 12px; }
</style>
