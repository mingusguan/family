<template>
  <div class="page-container">
    <el-card class="page-search" shadow="never">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item label="关键字" prop="keyword">
          <el-input v-model="queryParams.keyword" placeholder="分组名称" clearable @keyup.enter="fetchData" />
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
          <el-button v-hasPerm="['album:group:create']" type="success" icon="plus" @click="openDialog()">新增分组</el-button>
          <el-button
            v-hasPerm="['album:group:delete']"
            type="danger"
            icon="delete"
            :disabled="selectedIds.length === 0"
            @click="handleDelete()"
          >批量删除</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="list" border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column prop="name" label="分组名称" min-width="160" />
        <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="90" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column fixed="right" label="操作" width="150">
          <template #default="{ row }">
            <el-button v-hasPerm="['album:group:update']" type="primary" link icon="edit" @click="openDialog(row.id)">编辑</el-button>
            <el-button v-hasPerm="['album:group:delete']" type="danger" link icon="delete" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.title" width="520px" @closed="resetForm">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="90px">
        <el-form-item label="分组名称" prop="name">
          <el-input v-model="formData.name" maxlength="50" placeholder="请输入分组名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="4" maxlength="255" show-word-limit />
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
import type { AlbumGroupForm, AlbumGroupItem } from "@/api/album";

defineOptions({ name: "AlbumGroup" });

const queryFormRef = ref<FormInstance>();
const formRef = ref<FormInstance>();
const queryParams = reactive<{ keyword?: string }>({});
const list = ref<AlbumGroupItem[]>([]);
const selectedIds = ref<string[]>([]);
const loading = ref(false);
const submitting = ref(false);
const dialog = reactive({ visible: false, title: "" });
const formData = reactive<AlbumGroupForm>({ sort: 0 });
const rules: FormRules = {
  name: [{ required: true, message: "请输入分组名称", trigger: "blur" }],
};

async function fetchData() {
  loading.value = true;
  try {
    list.value = (await AlbumAPI.getGroups(queryParams)) || [];
  } finally {
    loading.value = false;
  }
}

function resetQuery() {
  queryFormRef.value?.resetFields();
  fetchData();
}

function handleSelectionChange(rows: AlbumGroupItem[]) {
  selectedIds.value = rows.map((item) => item.id);
}

async function openDialog(id?: string) {
  resetForm();
  dialog.visible = true;
  dialog.title = id ? "编辑相册分组" : "新增相册分组";
  if (id) Object.assign(formData, await AlbumAPI.getGroupForm(id));
}

function resetForm() {
  formRef.value?.resetFields();
  Object.assign(formData, { id: undefined, name: undefined, description: undefined, sort: 0 });
}

async function handleSubmit() {
  if (!(await formRef.value?.validate().catch(() => false))) return;
  submitting.value = true;
  try {
    if (formData.id) await AlbumAPI.updateGroup(formData.id, formData);
    else await AlbumAPI.createGroup(formData);
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
  await ElMessageBox.confirm("分组下存在资源时不能删除，确认继续？", "删除确认", { type: "warning" });
  await AlbumAPI.deleteGroups(ids);
  ElMessage.success("删除成功");
  fetchData();
}

onMounted(fetchData);
</script>
