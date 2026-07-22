<template>
  <div class="page-container">
    <el-card class="guide-card" shadow="never">
      <template #header>
        <div class="guide-card__title">家庭状态卡片</div>
      </template>
      <div class="guide-card__content">
        小程序会按优先级从高到低判断场景，只展示第一个满足条件的卡片。模板支持
        <el-tag size="small">{familyName}</el-tag>
        <el-tag size="small">{title}</el-tag>
        <el-tag size="small">{date}</el-tag>
        <el-tag size="small">{days}</el-tag>
        <el-tag size="small">{count}</el-tag>
        等变量。
      </div>
    </el-card>

    <el-card class="page-content" shadow="never">
      <el-table v-loading="loading" :data="list" border>
        <el-table-column prop="sceneLabel" label="场景" width="130">
          <template #default="{ row }">
            <div class="scene-cell">
              <span>{{ row.sceneLabel }}</span>
              <el-tag size="small" type="info">{{ row.scene }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="titleTemplate" label="标题模板" min-width="180" />
        <el-table-column prop="descriptionTemplate" label="描述模板" min-width="280" show-overflow-tooltip />
        <el-table-column prop="tagText" label="角标" width="100" />
        <el-table-column prop="priority" label="优先级" width="85" align="center" />
        <el-table-column prop="windowDays" label="时间窗口" width="100" align="center">
          <template #default="{ row }">{{ row.windowDays }} 天</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="85" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? "启用" : "停用" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="90" align="center">
          <template #default="{ row }">
            <el-button
              v-hasPerm="['family:card:update']"
              type="primary"
              link
              icon="edit"
              @click="openDialog(row)"
            >
              编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialog.visible" :title="`编辑${dialog.sceneLabel}卡片`" width="660px" @closed="resetForm">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="标题模板" prop="titleTemplate">
          <el-input v-model="formData.titleTemplate" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="描述模板" prop="descriptionTemplate">
          <el-input
            v-model="formData.descriptionTemplate"
            type="textarea"
            :rows="3"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>
        <el-row :gutter="18">
          <el-col :span="12">
            <el-form-item label="角标文案" prop="tagText">
              <el-input v-model="formData.tagText" maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="按钮文案" prop="actionText">
              <el-input v-model="formData.actionText" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="图标名称" prop="icon">
              <el-input v-model="formData.icon" placeholder="例如 calendar" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-input-number v-model="formData.priority" :min="0" :max="999" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="时间窗口" prop="windowDays">
              <el-input-number v-model="formData.windowDays" :min="1" :max="365" controls-position="right" />
              <span class="unit-text">天</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="背景图片" prop="backgroundUrl">
          <el-input v-model="formData.backgroundUrl" placeholder="对象存储路径或完整图片地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ElMessage, type FormInstance, type FormRules } from "element-plus";
import FamilyCardAPI, {
  type FamilyCardConfigForm,
  type FamilyCardConfigItem,
} from "@/api/family-card";

defineOptions({ name: "FamilyCardConfig" });

const loading = ref(false);
const submitting = ref(false);
const list = ref<FamilyCardConfigItem[]>([]);
const formRef = ref<FormInstance>();
const currentId = ref<string>();
const dialog = reactive({ visible: false, sceneLabel: "" });
const formData = reactive<FamilyCardConfigForm>({
  priority: 0,
  windowDays: 7,
  status: 1,
});
const rules: FormRules = {
  titleTemplate: [{ required: true, message: "请输入标题模板", trigger: "blur" }],
  descriptionTemplate: [{ required: true, message: "请输入描述模板", trigger: "blur" }],
  priority: [{ required: true, message: "请输入优先级", trigger: "change" }],
  windowDays: [{ required: true, message: "请输入时间窗口", trigger: "change" }],
};

async function fetchData() {
  loading.value = true;
  try {
    list.value = (await FamilyCardAPI.listConfigs()) || [];
  } finally {
    loading.value = false;
  }
}

async function openDialog(row: FamilyCardConfigItem) {
  resetForm();
  currentId.value = row.id;
  dialog.sceneLabel = row.sceneLabel;
  dialog.visible = true;
  Object.assign(formData, await FamilyCardAPI.getConfigForm(row.id));
}

function resetForm() {
  formRef.value?.resetFields();
  currentId.value = undefined;
  Object.assign(formData, {
    titleTemplate: undefined,
    descriptionTemplate: undefined,
    tagText: undefined,
    icon: undefined,
    backgroundUrl: undefined,
    actionText: undefined,
    priority: 0,
    windowDays: 7,
    status: 1,
  });
}

async function handleSubmit() {
  if (!currentId.value || !(await formRef.value?.validate().catch(() => false))) return;
  submitting.value = true;
  try {
    await FamilyCardAPI.updateConfig(currentId.value, formData);
    ElMessage.success("保存成功");
    dialog.visible = false;
    await fetchData();
  } finally {
    submitting.value = false;
  }
}

onMounted(fetchData);
</script>

<style scoped>
.guide-card {
  margin-bottom: 16px;
}

.guide-card__title {
  font-size: 16px;
  font-weight: 600;
}

.guide-card__content {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  color: var(--el-text-color-secondary);
}

.scene-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: flex-start;
}

.unit-text {
  margin-left: 8px;
  color: var(--el-text-color-secondary);
}
</style>
