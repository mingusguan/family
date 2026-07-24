<template>
  <div class="page-container">
    <el-card class="page-search" shadow="never">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item label="关键字" prop="keyword">
          <el-input
            v-model="queryParams.keyword"
            placeholder="文件名或描述"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="类型" prop="mediaType">
          <el-select
            v-model="queryParams.mediaType"
            placeholder="全部"
            clearable
            style="width: 110px"
          >
            <el-option label="图片" value="IMAGE" />
            <el-option label="视频" value="VIDEO" />
            <el-option label="音频" value="AUDIO" />
          </el-select>
        </el-form-item>
        <el-form-item label="上传用户" prop="uploaderId">
          <el-select
            v-model="queryParams.uploaderId"
            placeholder="全部用户"
            clearable
            filterable
            style="width: 170px"
          >
            <el-option
              v-for="item in userOptions"
              :key="item.value"
              :label="item.label"
              :value="String(item.value)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="分组" prop="groupId">
          <el-select
            v-model="queryParams.groupId"
            placeholder="全部"
            clearable
            filterable
            style="width: 150px"
          >
            <el-option v-for="item in groups" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签" prop="tagId">
          <el-select
            v-model="queryParams.tagId"
            placeholder="全部"
            clearable
            filterable
            style="width: 150px"
          >
            <el-option v-for="item in tags" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 100px">
            <el-option label="正常" :value="1" />
            <el-option label="隐藏" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="search" @click="handleQuery">搜索</el-button>
          <el-button icon="refresh" @click="handleResetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="page-content" shadow="never">
      <div class="page-toolbar">
        <div class="page-toolbar__left">
          <el-button
            v-hasPerm="['album:asset:create']"
            type="success"
            icon="plus"
            @click="openDialog()"
          >
            新增资源
          </el-button>
          <el-button
            v-hasPerm="['album:asset:delete']"
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
        <el-table-column label="预览" width="88" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.mediaType === 'IMAGE'"
              :src="row.thumbnailPreviewUrl || row.previewUrl || row.thumbnailUrl || row.url"
              :preview-src-list="[row.previewUrl || row.url]"
              preview-teleported
              fit="cover"
              class="media-preview"
            />
            <el-button
              v-else
              link
              type="primary"
              :aria-label="`预览${mediaLabel(row.mediaType)}`"
              @click="openMedia(row.mediaType, row.previewUrl || row.url, row.originalName)"
            >
              <el-icon :size="28">
                <VideoPlay v-if="row.mediaType === 'VIDEO'" />
                <Headset v-else />
              </el-icon>
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="originalName" label="文件名" min-width="180" show-overflow-tooltip />
        <el-table-column label="类型" width="86" align="center">
          <template #default="{ row }">
            <el-tag :type="mediaTagType(row.mediaType)">
              {{ row.mediaTypeLabel || mediaLabel(row.mediaType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="上传用户" min-width="130">
          <template #default="{ row }">{{ row.uploaderName || `用户 ${row.uploaderId}` }}</template>
        </el-table-column>
        <el-table-column prop="groupName" label="分组" min-width="110">
          <template #default="{ row }">{{ row.groupName || "未分组" }}</template>
        </el-table-column>
        <el-table-column label="标签" min-width="170">
          <template #default="{ row }">
            <span
              v-for="tag in row.tags || []"
              :key="tag.id"
              class="album-tag-badge tag-item"
              :style="{ '--album-tag-color': tag.color || '#6f60ce' }"
            >
              #{{ tag.name }}
            </span>
            <span v-if="!row.tags?.length" class="text-muted">无</span>
          </template>
        </el-table-column>
        <el-table-column label="大小/时长" width="130">
          <template #default="{ row }">
            <div>{{ formatSize(row.fileSize) }}</div>
            <div v-if="row.duration" class="text-muted">{{ formatDuration(row.duration) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="76" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? "正常" : "隐藏" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上传时间" width="170" />
        <el-table-column fixed="right" label="操作" width="150">
          <template #default="{ row }">
            <el-button
              v-hasPerm="['album:asset:update']"
              type="primary"
              link
              icon="edit"
              @click="openDialog(row.id)"
            >
              编辑
            </el-button>
            <el-button
              v-hasPerm="['album:asset:delete']"
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

    <el-dialog v-model="dialog.visible" :title="dialog.title" width="780px" @closed="resetForm">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="105px">
        <el-row :gutter="18">
          <el-col :span="24">
            <el-form-item label="所属用户" prop="uploaderId">
              <el-select
                v-model="formData.uploaderId"
                placeholder="请选择资源所属用户"
                filterable
                style="width: 100%"
              >
                <el-option
                  v-for="item in userOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="String(item.value)"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="资源文件" prop="url">
              <div class="resource-upload">
                <el-upload
                  :show-file-list="false"
                  :before-upload="beforeFileUpload"
                  :http-request="handleFileUpload"
                  accept="image/*,video/*,audio/*"
                >
                  <el-button type="primary" icon="upload" :loading="uploading">
                    {{ formData.url ? "重新上传" : "选择并上传文件" }}
                  </el-button>
                </el-upload>
                <div v-if="formData.url" class="uploaded-file">
                  <div class="uploaded-file__preview">
                    <el-image
                      v-if="formData.mediaType === 'IMAGE'"
                      :src="formData.previewUrl || formData.url"
                      :preview-src-list="[formData.previewUrl || formData.url]"
                      preview-teleported
                      fit="contain"
                      class="uploaded-media uploaded-media--image"
                    />
                    <video
                      v-else-if="formData.mediaType === 'VIDEO'"
                      :src="formData.previewUrl || formData.url"
                      controls
                      playsinline
                      preload="metadata"
                      class="uploaded-media uploaded-media--video"
                    >
                      当前浏览器不支持视频预览
                    </video>
                    <audio
                      v-else-if="formData.mediaType === 'AUDIO'"
                      :src="formData.previewUrl || formData.url"
                      controls
                      preload="metadata"
                      class="uploaded-media uploaded-media--audio"
                    >
                      当前浏览器不支持音频预览
                    </audio>
                  </div>
                  <div class="uploaded-file__info">
                    <el-link
                      type="primary"
                      :underline="false"
                      @click="
                        openMedia(
                          formData.mediaType!,
                          formData.previewUrl || formData.url!,
                          formData.originalName
                        )
                      "
                    >
                      {{ formData.originalName }}
                    </el-link>
                    <div class="text-muted">
                      {{ mediaLabel(formData.mediaType!) }} ·
                      {{ formData.mimeType || "未知类型" }} · {{ formatSize(formData.fileSize) }}
                      <template v-if="formData.width && formData.height">
                        · {{ formData.width }}×{{ formData.height }}
                      </template>
                      <template v-if="formData.duration">
                        · {{ formatDuration(formData.duration) }}
                      </template>
                    </div>
                  </div>
                </div>
                <div v-else class="upload-tip">
                  支持图片、视频和音频，最大 50MB；上传后自动识别文件信息。
                </div>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="识别分组">
              <el-input :model-value="recognizedGroupName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标签" prop="tagIds">
              <el-select
                v-model="formData.tagIds"
                multiple
                clearable
                filterable
                placeholder="请选择"
                style="width: 100%"
              >
                <el-option
                  v-for="item in tags"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="拍摄时间">
              <el-date-picker
                v-model="formData.capturedAt"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="formData.status">
                <el-radio :value="1">正常</el-radio>
                <el-radio :value="0">隐藏</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="描述">
              <el-input
                v-model="formData.description"
                type="textarea"
                :rows="3"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="previewDialog.visible"
      :title="previewDialog.title"
      width="min(760px, 90vw)"
      append-to-body
      destroy-on-close
    >
      <div class="media-viewer">
        <el-image
          v-if="previewDialog.mediaType === 'IMAGE'"
          :src="previewDialog.url"
          :preview-src-list="[previewDialog.url]"
          preview-teleported
          fit="contain"
          class="media-viewer__image"
        />
        <video
          v-else-if="previewDialog.mediaType === 'VIDEO'"
          :src="previewDialog.url"
          controls
          autoplay
          playsinline
          preload="metadata"
          class="media-viewer__video"
        >
          当前浏览器不支持视频预览
        </video>
        <audio
          v-else-if="previewDialog.mediaType === 'AUDIO'"
          :src="previewDialog.url"
          controls
          autoplay
          preload="metadata"
          class="media-viewer__audio"
        >
          当前浏览器不支持音频预览
        </audio>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { Headset, Picture, VideoPlay } from "@element-plus/icons-vue";
import {
  ElMessage,
  ElMessageBox,
  type FormInstance,
  type FormRules,
  type UploadRawFile,
  type UploadRequestOptions,
} from "element-plus";
import AlbumAPI from "@/api/album";
import FileAPI from "@/api/file";
import AppUserAPI from "@/api/app-user";
import type { OptionItem } from "@/api/common";
import type {
  AlbumAssetForm,
  AlbumAssetItem,
  AlbumAssetQuery,
  AlbumGroupItem,
  AlbumMediaType,
  AlbumTagItem,
} from "@/api/album";

defineOptions({ name: "AlbumAsset" });

const queryFormRef = ref<FormInstance>();
const formRef = ref<FormInstance>();
const loading = ref(false);
const submitting = ref(false);
const uploading = ref(false);
const selectedIds = ref<string[]>([]);
const groups = ref<AlbumGroupItem[]>([]);
const tags = ref<AlbumTagItem[]>([]);
const userOptions = ref<OptionItem[]>([]);

const queryParams = reactive<AlbumAssetQuery>({ pageNum: 1, pageSize: 10 });
const tableData = reactive({ list: [] as AlbumAssetItem[], total: 0 });
const dialog = reactive({ visible: false, title: "" });
const previewDialog = reactive<{
  visible: boolean;
  title: string;
  mediaType?: AlbumMediaType;
  url: string;
}>({
  visible: false,
  title: "资源预览",
  url: "",
});
const formData = reactive<AlbumAssetForm>({ status: 1, tagIds: [] });

const recognizedGroupName = computed(() => {
  const group = groups.value.find((item) => String(item.id) === String(formData.groupId));
  if (group) return group.name;
  return formData.mediaType ? mediaLabel(formData.mediaType) : "上传文件后自动识别";
});

const rules: FormRules = {
  uploaderId: [{ required: true, message: "请选择资源所属用户", trigger: "change" }],
  url: [{ required: true, message: "请先选择并上传资源文件", trigger: "change" }],
};

async function loadOptions() {
  const [groupList, tagList, users] = await Promise.all([
    AlbumAPI.getGroups(),
    AlbumAPI.getTags(),
    AppUserAPI.getOptions(),
  ]);
  groups.value = groupList || [];
  tags.value = tagList || [];
  userOptions.value = users || [];
}

function beforeFileUpload(file: UploadRawFile) {
  if (!resolveMediaType(file)) {
    ElMessage.warning("只能上传图片、视频或音频文件");
    return false;
  }
  if (file.size > 50 * 1024 * 1024) {
    ElMessage.warning("文件大小不能超过 50MB");
    return false;
  }
  return true;
}

async function handleFileUpload(options: UploadRequestOptions) {
  const file = options.file;
  const mediaType = resolveMediaType(file);
  if (!mediaType) return;
  uploading.value = true;
  try {
    const metadata = await readMediaMetadata(file, mediaType);
    const uploaded = await FileAPI.uploadFile(file);
    const mediaGroup = groups.value.find((item) => item.name === mediaLabel(mediaType));
    Object.assign(formData, {
      mediaType,
      groupId: mediaGroup?.id,
      url: uploaded.url,
      previewUrl: uploaded.previewUrl || uploaded.url,
      thumbnailUrl: mediaType === "IMAGE" ? uploaded.url : undefined,
      thumbnailPreviewUrl: mediaType === "IMAGE" ? uploaded.previewUrl || uploaded.url : undefined,
      originalName: file.name,
      mimeType: file.type || undefined,
      fileSize: file.size,
      duration: metadata.duration,
      width: metadata.width,
      height: metadata.height,
    });
    formRef.value?.clearValidate("url");
    ElMessage.success("文件已上传到腾讯云 COS");
    options.onSuccess?.(uploaded);
  } catch (error) {
    console.error("相册资源上传失败", error);
    ElMessage.error("文件上传失败");
  } finally {
    uploading.value = false;
  }
}

function resolveMediaType(file: File): AlbumMediaType | undefined {
  if (file.type.startsWith("image/")) return "IMAGE";
  if (file.type.startsWith("video/")) return "VIDEO";
  if (file.type.startsWith("audio/")) return "AUDIO";
}

function readMediaMetadata(
  file: File,
  mediaType: AlbumMediaType
): Promise<{ width?: number; height?: number; duration?: number }> {
  const objectUrl = URL.createObjectURL(file);
  return new Promise((resolve) => {
    if (mediaType === "IMAGE") {
      const image = new Image();
      image.onload = () => {
        URL.revokeObjectURL(objectUrl);
        resolve({ width: image.naturalWidth, height: image.naturalHeight });
      };
      image.onerror = () => {
        URL.revokeObjectURL(objectUrl);
        resolve({});
      };
      image.src = objectUrl;
      return;
    }

    const media = document.createElement(mediaType === "VIDEO" ? "video" : "audio");
    media.preload = "metadata";
    media.onloadedmetadata = () => {
      const video = media as HTMLVideoElement;
      URL.revokeObjectURL(objectUrl);
      resolve({
        width: mediaType === "VIDEO" ? video.videoWidth : undefined,
        height: mediaType === "VIDEO" ? video.videoHeight : undefined,
        duration: Number.isFinite(media.duration) ? Math.round(media.duration * 1000) : undefined,
      });
    };
    media.onerror = () => {
      URL.revokeObjectURL(objectUrl);
      resolve({});
    };
    media.src = objectUrl;
  });
}

async function fetchData() {
  loading.value = true;
  try {
    const data = await AlbumAPI.getAssetPage(queryParams);
    tableData.list = data.list || [];
    tableData.total = data.total || 0;
  } finally {
    loading.value = false;
  }
}
//test
function handleQuery() {
  queryParams.pageNum = 1;
  fetchData();
}

function handleResetQuery() {
  queryFormRef.value?.resetFields();
  queryParams.pageNum = 1;
  fetchData();
}

function handleSelectionChange(rows: AlbumAssetItem[]) {
  selectedIds.value = rows.map((item) => item.id);
}

async function openDialog(id?: string) {
  resetForm();
  dialog.visible = true;
  dialog.title = id ? "编辑相册资源" : "新增相册资源";
  if (id) Object.assign(formData, await AlbumAPI.getAssetForm(id));
}

function resetForm() {
  formRef.value?.resetFields();
  Object.assign(formData, {
    id: undefined,
    uploaderId: undefined,
    mediaType: undefined,
    url: undefined,
    previewUrl: undefined,
    thumbnailUrl: undefined,
    thumbnailPreviewUrl: undefined,
    originalName: undefined,
    mimeType: undefined,
    fileSize: undefined,
    duration: undefined,
    width: undefined,
    height: undefined,
    groupId: undefined,
    tagIds: [],
    description: undefined,
    capturedAt: undefined,
    status: 1,
  });
}

async function handleSubmit() {
  if (!(await formRef.value?.validate().catch(() => false))) return;
  submitting.value = true;
  try {
    if (formData.id) await AlbumAPI.updateAsset(formData.id, formData);
    else await AlbumAPI.createAsset(formData);
    ElMessage.success(formData.id ? "修改成功" : "新增成功");
    dialog.visible = false;
    await Promise.all([fetchData(), loadOptions()]);
  } finally {
    submitting.value = false;
  }
}

async function handleDelete(id?: string) {
  const ids = id || selectedIds.value.join(",");
  if (!ids) return;
  await ElMessageBox.confirm("删除后仅移除相册记录，不会删除COS对象，确认继续？", "删除确认", {
    type: "warning",
  });
  await AlbumAPI.deleteAssets(ids);
  ElMessage.success("删除成功");
  fetchData();
}

function openMedia(mediaType: AlbumMediaType, url: string, title?: string) {
  Object.assign(previewDialog, {
    visible: true,
    title: title || `${mediaLabel(mediaType)}预览`,
    mediaType,
    url,
  });
}

function mediaLabel(type: AlbumMediaType) {
  return ({ IMAGE: "图片", VIDEO: "视频", AUDIO: "音频" } as const)[type];
}

function mediaTagType(type: AlbumMediaType): "success" | "warning" | "primary" {
  return type === "IMAGE" ? "success" : type === "VIDEO" ? "primary" : "warning";
}

function formatSize(size?: number) {
  if (size == null) return "-";
  if (size < 1024) return `${size} B`;
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
  return `${(size / 1024 / 1024).toFixed(1)} MB`;
}

function formatDuration(duration?: number) {
  if (!duration) return "";
  const seconds = Math.floor(duration / 1000);
  return `${Math.floor(seconds / 60)}:${String(seconds % 60).padStart(2, "0")}`;
}

onMounted(async () => {
  await loadOptions();
  fetchData();
});
</script>

<style scoped>
.album-tag-badge {
  display: inline-flex;
  align-items: center;
  max-width: 100%;
  padding: 3px 8px;
  overflow: hidden;
  font-size: 12px;
  line-height: 1.4;
  color: var(--album-tag-color, #6f60ce);
  text-overflow: ellipsis;
  white-space: nowrap;
  background: #efecff;
  background: color-mix(in srgb, var(--album-tag-color, #6f60ce) 10%, transparent);
  border: 1px solid color-mix(in srgb, var(--album-tag-color, #6f60ce) 24%, transparent);
  border-radius: 999px;
}
.media-preview {
  width: 56px;
  height: 56px;
  border-radius: 6px;
}
.tag-item {
  margin: 2px 4px 2px 0;
}
.text-muted {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.resource-upload {
  width: 100%;
}
.uploaded-file {
  margin-top: 12px;
  padding: 12px;
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
  background: var(--el-fill-color-lighter);
}
.uploaded-file__preview {
  display: flex;
  width: 100%;
  min-height: 54px;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
  overflow: hidden;
  border-radius: 6px;
  background: var(--el-fill-color-dark);
}
.uploaded-media--image {
  width: 100%;
  height: 220px;
}
.uploaded-media--video {
  display: block;
  width: 100%;
  max-height: 320px;
  background: #000;
}
.uploaded-media--audio {
  width: min(100%, 560px);
  margin: 12px;
}
.uploaded-file__info {
  min-width: 0;
  line-height: 1.7;
}
.media-viewer {
  display: flex;
  min-height: 160px;
  align-items: center;
  justify-content: center;
}
.media-viewer__image {
  width: 100%;
  height: min(65vh, 620px);
}
.media-viewer__video {
  width: 100%;
  max-height: 65vh;
  background: #000;
}
.media-viewer__audio {
  width: min(100%, 560px);
}
.upload-tip {
  margin-top: 8px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>
