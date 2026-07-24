<template>
  <view class="publish-page">
    <custom-navbar title="发布家庭时刻" placeholder />

    <view class="publish-target" @click="chooseAlbum">
      <wd-icon name="home" size="16" color="#7567dc" />
      <text class="publish-target__label">发布到</text>
      <text class="publish-target__album">{{ albumName || "请选择相册" }}</text>
      <text class="publish-target__action">切换</text>
      <wd-icon name="right" size="15" color="#968eaa" />
    </view>

    <view class="publish-card publish-card--media">
      <view class="publish-title-row">
        <text class="publish-card__title">上传照片或视频</text>
        <text class="publish-title-row__hint">选择后自动识别类型</text>
      </view>

      <view v-if="!selectedMedia.length" class="upload-zone" @click="choosePhotoOrVideo">
        <view class="upload-zone__icon">
          <wd-icon name="add" size="34" color="#ffffff" />
        </view>
        <text class="upload-zone__title">点击批量上传照片或视频</text>
        <text class="upload-zone__desc">一次最多选择 {{ MAX_MEDIA_COUNT }} 个，可继续追加</text>
      </view>

      <view v-else class="selected-media-list">
        <view v-for="(media, index) in selectedMedia" :key="media.path" class="selected-media">
          <image
            v-if="media.type === 'IMAGE'"
            class="selected-media__preview"
            :src="media.path"
            mode="aspectFill"
          />
          <video
            v-else-if="media.type === 'VIDEO'"
            class="selected-media__preview"
            :src="media.path"
            controls
            object-fit="cover"
          />
          <view v-else class="selected-media__audio">
            <wd-icon name="voice" size="30" color="#7567dc" />
            <text class="selected-media__name">{{ media.name }}</text>
            <text class="selected-media__meta">{{ formatDuration(media.duration) }}</text>
          </view>
          <view class="selected-media__remove" @click.stop="removeMedia(index)">
            <wd-icon name="close" size="14" color="#ffffff" />
          </view>
        </view>
        <view
          v-if="selectedMedia.length < MAX_MEDIA_COUNT"
          class="selected-media-add"
          @click="choosePhotoOrVideo"
        >
          <wd-icon name="add" size="25" color="#7567dc" />
          <text>继续添加</text>
        </view>
      </view>
      <text v-if="selectedMedia.length" class="selected-media-count">
        已选择 {{ selectedMedia.length }}/{{ MAX_MEDIA_COUNT }} 个
      </text>

      <view class="audio-entry" @click="chooseAudio">
        <view class="audio-entry__icon">
          <wd-icon :name="recording ? 'pause-circle' : 'voice'" size="22" color="#7567dc" />
        </view>
        <view class="audio-entry__body">
          <text class="audio-entry__title">
            {{ recording ? "正在录音，点击结束" : "上传一段声音" }}
          </text>
          <text class="audio-entry__desc">支持录音或选择已有音频文件</text>
        </view>
        <wd-icon name="right" size="16" color="#aaa3b2" />
      </view>
    </view>

    <view class="publish-card">
      <text class="publish-card__title">这一刻想说什么</text>
      <textarea
        v-model="description"
        class="publish-textarea"
        placeholder="分享这一刻的故事、心情或想对家人说的话..."
        :maxlength="500"
      />
      <text class="publish-count">{{ description.length }}/500</text>
    </view>

    <view class="publish-card">
      <view class="tag-heading">
        <text class="publish-card__title">添加标签</text>
        <text class="tag-heading__hint">最多选择 10 个标签</text>
      </view>

      <view class="tag-create">
        <text class="tag-create__hash">#</text>
        <input
          v-model="customTag"
          class="tag-create__input"
          placeholder="创建自定义标签"
          :maxlength="30"
          @confirm="createCustomTag"
        />
        <text class="tag-create__button" @click="createCustomTag">添加</text>
      </view>

      <view v-if="tags.length" class="tag-list">
        <view
          v-for="tag in tags"
          :key="tag.id"
          class="tag-chip"
          :class="{ 'tag-chip--active': selectedTagIds.includes(tag.id) }"
          :style="getTagColorStyle(tag.color, selectedTagIds.includes(tag.id))"
          @click="toggleTag(tag.id)"
        >
          #{{ tag.name }}
        </view>
      </view>
      <text v-else class="tag-empty">还没有标签，输入一个名称创建吧</text>
    </view>

    <view class="publish-footer">
      <wd-button
        type="primary"
        block
        round
        :loading="submitting"
        :disabled="!albumId || !selectedMedia.length"
        custom-class="publish-button"
        @click="publish"
      >
        发布到家庭相册
      </wd-button>
    </view>

    <wd-action-sheet
      v-model="albumSheetVisible"
      title="选择发布相册"
      :actions="albumActions"
      cancel-text="取消"
      @select="handleAlbumSelect"
    />
    <view v-if="submitting" class="upload-mask">
      <view class="upload-mask__card">
        <wd-loading color="#7567dc" />
        <text>{{ uploadProgress || "正在上传并保存..." }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import dayjs from "dayjs";
import { onLoad } from "@dcloudio/uni-app";
import AlbumAPI, { type AlbumMediaType, type AlbumTag } from "@/api/album";
import FamilyAPI, { type FamilyAlbum } from "@/api/family";
import FileAPI from "@/api/file";
import { ALBUM_NAVIGATION_TARGET_KEY } from "@/constants";
import { Storage } from "@/utils/storage";
import { getTagColorStyle } from "@/utils/tagColor";

interface SelectedMedia {
  type: AlbumMediaType;
  path: string;
  name: string;
  mimeType: string;
  size?: number;
  duration?: number;
  width?: number;
  height?: number;
  /** H5 原生文件对象，用于浏览器端上传。 */
  rawFile?: File;
}

definePage({
  name: "album-publish",
  style: { navigationStyle: "custom", backgroundColor: "#f5f3fa" },
});

const familyId = ref(0);
const albumId = ref(0);
const albumName = ref("");
const albums = ref<FamilyAlbum[]>([]);
const albumSheetVisible = ref(false);
const albumActions = computed(() =>
  albums.value.map((album) => ({
    name: album.name,
    description: album.id === albumId.value ? "当前相册" : album.assetCount + " 项内容",
    color: album.id === albumId.value ? "#7567dc" : undefined,
  }))
);
const description = ref("");
const tags = ref<AlbumTag[]>([]);
const selectedTagIds = ref<number[]>([]);
const customTag = ref("");
const MAX_MEDIA_COUNT = 9;
const selectedMedia = ref<SelectedMedia[]>([]);
const submitting = ref(false);
const uploadProgress = ref("");
const recording = ref(false);
let recorder: ReturnType<typeof uni.getRecorderManager> | undefined;

// #ifndef H5
recorder = uni.getRecorderManager();
recorder.onStop((result) => {
  recording.value = false;
  addSelectedMedia({
    type: "AUDIO",
    path: result.tempFilePath,
    name: "家庭声音-" + Date.now() + ".mp3",
    mimeType: "audio/mpeg",
    duration: result.duration,
    size: result.fileSize,
  });
});

recorder.onError(() => {
  recording.value = false;
  uni.showToast({ title: "录音失败，请检查麦克风权限", icon: "none" });
});
// #endif

async function loadTags() {
  try {
    tags.value = await AlbumAPI.listTags();
  } catch (error) {
    console.error("读取标签失败", error);
  }
}

async function loadAlbums() {
  try {
    albums.value = await FamilyAPI.listAlbums(familyId.value);
    const selectedAlbum =
      albums.value.find((album) => album.id === albumId.value) || albums.value[0];
    if (!selectedAlbum) {
      albumId.value = 0;
      albumName.value = "";
      uni.showToast({ title: "当前家庭还没有相册", icon: "none" });
      return;
    }
    albumId.value = selectedAlbum.id;
    albumName.value = selectedAlbum.name;
  } catch (error: any) {
    console.error("读取家庭相册失败", error);
    uni.showToast({ title: error?.message || "相册加载失败", icon: "none" });
  }
}

function chooseAlbum() {
  if (!albums.value.length) {
    uni.showToast({ title: "暂无可选择的相册", icon: "none" });
    return;
  }
  albumSheetVisible.value = true;
}

function handleAlbumSelect({ index }: { index: number }) {
  const selectedAlbum = albums.value[index];
  if (!selectedAlbum) return;
  albumId.value = selectedAlbum.id;
  albumName.value = selectedAlbum.name;
  albumSheetVisible.value = false;
}

function choosePhotoOrVideo() {
  if (selectedMedia.value.length >= MAX_MEDIA_COUNT) {
    uni.showToast({ title: "一次最多选择9个文件", icon: "none" });
    return;
  }

  // #ifdef H5
  chooseH5PhotoOrVideo();
  return;
  // #endif

  // #ifdef APP-PLUS
  chooseAppPhotoOrVideo();
  return;
  // #endif

  const chooseMedia = (uni as any).chooseMedia;
  if (typeof chooseMedia === "function") {
    chooseMedia({
      count: MAX_MEDIA_COUNT - selectedMedia.value.length,
      mediaType: ["image", "video"],
      sourceType: ["album", "camera"],
      sizeType: ["original"],
      success: async (result: any) => {
        const files = result.tempFiles || [];
        for (const file of files) await applyPickedMedia(file);
      },
    });
    return;
  }
  choosePhotoOrVideoFile();
}

// #ifdef APP-PLUS
function chooseAppPhotoOrVideo() {
  const gallery = (globalThis as any).plus?.gallery;
  if (!gallery) {
    uni.showToast({ title: "无法打开系统相册", icon: "none" });
    return;
  }
  gallery.pick(
    async (result: { files?: string[] }) => {
      const paths = (result.files || []).slice(0, MAX_MEDIA_COUNT - selectedMedia.value.length);
      for (const path of paths) await applyPickedMedia({ path });
    },
    () => undefined,
    {
      filter: "none",
      multiple: true,
      maximum: MAX_MEDIA_COUNT - selectedMedia.value.length,
      system: true,
    }
  );
}
// #endif

// #ifdef H5
function chooseH5PhotoOrVideo() {
  const input = document.createElement("input");
  input.type = "file";
  input.accept = "image/*,video/*";
  input.multiple = true;
  input.onchange = async () => {
    const files = Array.from(input.files || []).slice(
      0,
      MAX_MEDIA_COUNT - selectedMedia.value.length
    );
    for (const file of files) {
      await applyPickedMedia({
        path: URL.createObjectURL(file),
        name: file.name,
        type: file.type,
        size: file.size,
        rawFile: file,
      });
    }
  };
  input.click();
}
// #endif
function choosePhotoOrVideoFile() {
  const chooseFile = (uni as any).chooseFile;
  if (typeof chooseFile !== "function") {
    uni.showToast({ title: "当前平台暂不支持混合选择器", icon: "none" });
    return;
  }
  chooseFile({
    count: MAX_MEDIA_COUNT - selectedMedia.value.length,
    extension: ["jpg", "jpeg", "png", "webp", "mp4", "mov"],
    success: async (result: any) => {
      const files = result.tempFiles || [];
      for (const file of files) await applyPickedMedia(file);
    },
  });
}

async function applyPickedMedia(file: any) {
  const path = file.tempFilePath || file.path;
  if (!path) return;
  const mimeType = file.type || getMimeType(path, "");
  const isVideo =
    file.fileType === "video" || mimeType.startsWith("video/") || /\.(mp4|mov|m4v)$/i.test(path);

  if (isVideo) {
    addSelectedMedia({
      type: "VIDEO",
      path,
      name: file.name || getFileName(path, "family-video.mp4"),
      mimeType: mimeType || "video/mp4",
      size: file.size,
      duration: file.duration ? Math.round(file.duration * 1000) : undefined,
      width: file.width,
      height: file.height,
      rawFile: file.rawFile,
    });
    return;
  }

  const meta = await getImageInfo(path);
  addSelectedMedia({
    type: "IMAGE",
    path,
    name: file.name || getFileName(path, "family-photo.jpg"),
    mimeType: mimeType || "image/jpeg",
    size: file.size,
    width: file.width || meta.width,
    height: file.height || meta.height,
    rawFile: file.rawFile,
  });
}

function chooseAudio() {
  // #ifdef H5
  chooseH5AudioFile();
  return;
  // #endif

  if (!recorder) return;
  if (recording.value) {
    recorder?.stop();
    return;
  }
  uni.showActionSheet({
    itemList: ["录制一段声音", "选择音频文件"],
    success: (result) => {
      if (result.tapIndex === 0) {
        recording.value = true;
        recorder?.start({ format: "mp3", duration: 600000 });
        uni.showToast({ title: "录音已开始，再次点击“声音”结束", icon: "none", duration: 2500 });
      } else {
        chooseAudioFile();
      }
    },
  });
}

// #ifdef H5
function chooseH5AudioFile() {
  const input = document.createElement("input");
  input.type = "file";
  input.accept = "audio/*";
  input.onchange = () => {
    const file = input.files?.[0];
    if (!file) return;
    const path = URL.createObjectURL(file);
    addSelectedMedia({
      type: "AUDIO",
      path,
      name: file.name || "family-audio.mp3",
      mimeType: file.type || getMimeType(file.name, "audio/mpeg"),
      size: file.size,
      rawFile: file,
    });
  };
  input.click();
}
// #endif

function chooseAudioFile() {
  const chooseFile = (uni as any).chooseFile;
  if (typeof chooseFile !== "function") {
    uni.showToast({ title: "当前平台请使用录音方式上传声音", icon: "none" });
    return;
  }
  chooseFile({
    count: 1,
    extension: ["mp3", "m4a", "aac", "wav", "ogg"],
    success: (result: any) => {
      const file = result.tempFiles?.[0];
      const path = file?.path || result.tempFilePaths?.[0];
      if (!path) return;
      addSelectedMedia({
        type: "AUDIO",
        path,
        name: file?.name || getFileName(path, "family-audio.mp3"),
        mimeType: file?.type || getMimeType(path, "audio/mpeg"),
        size: file?.size,
      });
    },
  });
}

function addSelectedMedia(media: SelectedMedia) {
  if (selectedMedia.value.length >= MAX_MEDIA_COUNT) {
    uni.showToast({ title: "一次最多选择9个文件", icon: "none" });
    return;
  }
  if (selectedMedia.value.some((item) => item.path === media.path)) return;
  selectedMedia.value.push(media);
}

function removeMedia(index: number) {
  const [media] = selectedMedia.value.splice(index, 1);
  // #ifdef H5
  if (media?.rawFile && media.path.startsWith("blob:")) URL.revokeObjectURL(media.path);
  // #endif
}

async function saveAndSelectCustomTag(name: string) {
  const existing = tags.value.find((item) => item.name === name);
  if (!existing && selectedTagIds.value.length >= 10) {
    throw new Error("最多选择10个标签");
  }

  const tag = existing || (await AlbumAPI.createTag(name));
  if (!tags.value.some((item) => item.id === tag.id)) tags.value.unshift(tag);
  if (!selectedTagIds.value.includes(tag.id)) {
    if (selectedTagIds.value.length >= 10) {
      throw new Error("最多选择10个标签");
    }
    selectedTagIds.value.push(tag.id);
  }
  customTag.value = "";
  return tag;
}

async function createCustomTag() {
  const name = customTag.value.trim().replace(/^#+/, "");
  if (!name) {
    uni.showToast({ title: "请输入标签名称", icon: "none" });
    return;
  }

  try {
    await saveAndSelectCustomTag(name);
  } catch (error: any) {
    uni.showToast({
      title: error?.message || "标签创建失败",
      icon: "none",
    });
  }
}

function toggleTag(id: number) {
  const index = selectedTagIds.value.indexOf(id);
  if (index >= 0) {
    selectedTagIds.value.splice(index, 1);
    return;
  }
  if (selectedTagIds.value.length >= 10) {
    uni.showToast({ title: "最多选择10个标签", icon: "none" });
    return;
  }
  selectedTagIds.value.push(id);
}

async function publish() {
  if (!selectedMedia.value.length || submitting.value) return;
  submitting.value = true;
  try {
    const pendingTagName = customTag.value.trim().replace(/^#+/, "");
    if (pendingTagName) {
      uploadProgress.value = "正在保存标签";
      await saveAndSelectCustomTag(pendingTagName);
    }

    const uploadedFiles: Array<{
      media: SelectedMedia;
      fileInfo: Awaited<ReturnType<typeof FileAPI.upload>>;
    }> = [];
    for (let index = 0; index < selectedMedia.value.length; index += 1) {
      const media = selectedMedia.value[index];
      uploadProgress.value = "正在上传 " + (index + 1) + "/" + selectedMedia.value.length;
      const fileInfo = await FileAPI.upload(media.path, media.rawFile);
      uploadedFiles.push({ media, fileInfo });
    }

    const fallbackCapturedAt = dayjs().format("YYYY-MM-DD HH:mm:ss");
    uploadProgress.value = "正在保存";
    await AlbumAPI.createMoment({
      familyId: familyId.value,
      albumId: albumId.value,
      resources: uploadedFiles.map(({ media, fileInfo }) => ({
        mediaType: media.type,
        url: fileInfo.url,
        originalName: fileInfo.name || media.name,
        mimeType: media.mimeType,
        fileSize: media.size,
        duration: media.duration,
        width: media.width,
        height: media.height,
        capturedAt: fileInfo.capturedAt || fallbackCapturedAt,
      })),
      tagIds: selectedTagIds.value,
      description: description.value.trim() || undefined,
      capturedAt: fallbackCapturedAt,
    });
    uni.showToast({ title: "成功发布" + uploadedFiles.length + "个文件", icon: "success" });
    Storage.set(ALBUM_NAVIGATION_TARGET_KEY, {
      familyId: familyId.value,
      albumId: albumId.value,
    });
    setTimeout(() => uni.switchTab({ url: "/pages/album/index" }), 700);
  } catch (error: any) {
    console.error("批量发布家庭时刻失败", error);
    uni.showToast({ title: error?.message || "发布失败，请稍后重试", icon: "none" });
  } finally {
    submitting.value = false;
    uploadProgress.value = "";
  }
}

function getImageInfo(path: string): Promise<{ width?: number; height?: number }> {
  return new Promise((resolve) => {
    uni.getImageInfo({
      src: path,
      success: (result) => resolve({ width: result.width, height: result.height }),
      fail: () => resolve({}),
    });
  });
}

function getFileName(path: string, fallback: string) {
  const clean = path.split("?")[0].replace(/\\/g, "/");
  return clean.split("/").pop() || fallback;
}

function getMimeType(path: string, fallback: string) {
  const extension = getFileName(path, "").split(".").pop()?.toLowerCase();
  const types: Record<string, string> = {
    jpg: "image/jpeg",
    jpeg: "image/jpeg",
    png: "image/png",
    webp: "image/webp",
    mp4: "video/mp4",
    mov: "video/quicktime",
    mp3: "audio/mpeg",
    m4a: "audio/mp4",
    aac: "audio/aac",
    wav: "audio/wav",
    ogg: "audio/ogg",
  };
  return types[extension || ""] || fallback;
}

function formatDuration(duration?: number) {
  if (!duration) return "声音文件";
  const seconds = Math.round(duration / 1000);
  return Math.floor(seconds / 60) + "分" + (seconds % 60) + "秒";
}

onLoad((options) => {
  familyId.value = Number(options?.familyId || 0);
  albumId.value = Number(options?.albumId || 0);
  albumName.value = decodeURIComponent(options?.albumName || "");
  if (!familyId.value) {
    uni.showToast({ title: "家庭参数缺失", icon: "none" });
    setTimeout(() => uni.navigateBack(), 800);
    return;
  }
  loadAlbums();
  loadTags();
});
</script>

<style lang="scss" scoped>
.publish-page {
  min-height: 100vh;
  padding: 24rpx 28rpx 180rpx;
  background: #f5f3fa;
}

.publish-target {
  display: flex;
  gap: 10rpx;
  align-items: center;
  padding: 18rpx 22rpx;
  margin-bottom: 18rpx;
  font-size: 22rpx;
  color: #8c8498;
  background: #ebe8fa;
  border-radius: 20rpx;
}

.publish-target__album {
  flex: 1;
  overflow: hidden;
  font-size: 24rpx;
  font-weight: 650;
  color: #5d51bd;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.publish-target__action {
  flex-shrink: 0;
  font-size: 21rpx;
  color: #8d84a0;
}

.publish-title-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.publish-title-row__hint {
  font-size: 19rpx;
  color: #a59eac;
}

.upload-zone {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 280rpx;
  margin-top: 22rpx;
  background: linear-gradient(145deg, #f8f6ff, #f2effc);
  border: 3rpx dashed #b7aef0;
  border-radius: 26rpx;
}

.upload-zone:active {
  background: #ece8ff;
}

.upload-zone__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 82rpx;
  height: 82rpx;
  background: linear-gradient(135deg, #7364dc, #a270d0);
  border-radius: 28rpx;
  box-shadow: 0 14rpx 28rpx rgb(94 72 188 / 24%);
}

.upload-zone__title {
  margin-top: 20rpx;
  font-size: 27rpx;
  font-weight: 650;
  color: #4a4257;
}

.upload-zone__desc {
  margin-top: 9rpx;
  font-size: 21rpx;
  color: #9c95a5;
}

.audio-entry {
  display: flex;
  gap: 16rpx;
  align-items: center;
  padding: 20rpx;
  margin-top: 18rpx;
  background: #f6f3fb;
  border-radius: 22rpx;
}

.audio-entry__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 66rpx;
  height: 66rpx;
  background: #e9e5fb;
  border-radius: 20rpx;
}

.audio-entry__body {
  flex: 1;
}

.audio-entry__title,
.audio-entry__desc {
  display: block;
}

.audio-entry__title {
  font-size: 24rpx;
  font-weight: 600;
  color: #4b4455;
}

.audio-entry__desc {
  margin-top: 6rpx;
  font-size: 19rpx;
  color: #9f98a8;
}

.selected-media__replace {
  position: absolute;
  right: 18rpx;
  bottom: 18rpx;
  padding: 10rpx 18rpx;
  font-size: 21rpx;
  color: #fff;
  background: rgb(33 26 44 / 62%);
  border-radius: 999rpx;
}
.publish-header {
  display: flex;
  align-items: center;
  padding: 24rpx 28rpx;
  margin-bottom: 22rpx;
  background: linear-gradient(135deg, #7364dc, #a571ce);
  border-radius: 28rpx;
  box-shadow: 0 16rpx 34rpx rgb(86 63 176 / 18%);
}

.publish-header__label {
  font-size: 23rpx;
  color: rgb(255 255 255 / 70%);
}

.publish-header__album {
  margin-left: 14rpx;
  font-size: 27rpx;
  font-weight: 650;
  color: #fff;
}

.publish-card {
  position: relative;
  padding: 30rpx;
  margin-top: 22rpx;
  background: #fff;
  border-radius: 30rpx;
  box-shadow: 0 12rpx 32rpx rgb(55 43 92 / 7%);
}

.publish-card__title {
  font-size: 28rpx;
  font-weight: 650;
  color: #40394b;
}

.media-types {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18rpx;
  margin-top: 26rpx;
}

.media-type {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  align-items: center;
  justify-content: center;
  height: 146rpx;
  font-size: 23rpx;
  color: #fff;
  border-radius: 25rpx;
}

.media-type--image {
  background: linear-gradient(145deg, #7188e8, #7770d9);
}

.media-type--video {
  background: linear-gradient(145deg, #e08aa0, #b870c8);
}

.media-type--audio {
  background: linear-gradient(145deg, #54b1a4, #6b8fd8);
}

.selected-media-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14rpx;
  margin-top: 24rpx;
}

.selected-media,
.selected-media-add {
  position: relative;
  height: 205rpx;
  overflow: hidden;
  background: #f1eef9;
  border-radius: 20rpx;
}

.selected-media__preview {
  width: 100%;
  height: 100%;
}

.selected-media__audio {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 18rpx;
  color: #4d4658;
  text-align: center;
  background: #f0edfc;
}

.selected-media__name,
.selected-media__meta {
  display: block;
  max-width: 100%;
}

.selected-media__name {
  overflow: hidden;
  font-size: 20rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.selected-media__meta {
  font-size: 18rpx;
  color: #91899d;
}

.selected-media__remove {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42rpx;
  height: 42rpx;
  background: rgb(25 20 35 / 62%);
  border-radius: 50%;
}

.selected-media-add {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  align-items: center;
  justify-content: center;
  font-size: 21rpx;
  color: #7567dc;
  background: #f7f5fd;
  border: 2rpx dashed #b8afe8;
}

.selected-media-count {
  display: block;
  margin-top: 14rpx;
  font-size: 20rpx;
  color: #928a9d;
  text-align: right;
}

.publish-textarea {
  box-sizing: border-box;
  width: 100%;
  height: 170rpx;
  padding: 24rpx;
  margin-top: 22rpx;
  font-size: 25rpx;
  line-height: 1.65;
  color: #40394b;
  background: #f7f5fa;
  border-radius: 22rpx;
}

.publish-count {
  display: block;
  margin-top: 10rpx;
  font-size: 20rpx;
  color: #aaa3b2;
  text-align: right;
}

.tag-heading {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.tag-heading__hint {
  font-size: 19rpx;
  color: #aaa3b2;
}

.tag-create {
  display: flex;
  align-items: center;
  height: 82rpx;
  padding: 0 20rpx;
  margin-top: 24rpx;
  background: #f5f2fb;
  border-radius: 21rpx;
}

.tag-create__hash {
  font-size: 31rpx;
  font-weight: 650;
  color: #7668dc;
}

.tag-create__input {
  flex: 1;
  height: 100%;
  margin-left: 10rpx;
  font-size: 24rpx;
}

.tag-create__button {
  padding: 10rpx 15rpx;
  font-size: 22rpx;
  color: #7668dc;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
  margin-top: 24rpx;
}

.tag-chip {
  padding: 12rpx 18rpx;
  font-size: 22rpx;
  color: #776f83;
  background: #f1eff5;
  border: 1rpx solid transparent;
  border-radius: 999rpx;
}

.tag-chip--active {
  color: #6f60d3;
  background: #ece8ff;
  border-color: #a99fed;
}

.tag-empty {
  display: block;
  padding: 30rpx 0 12rpx;
  font-size: 22rpx;
  color: #a59eac;
  text-align: center;
}

.publish-footer {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 10;
  padding: 22rpx 30rpx calc(22rpx + env(safe-area-inset-bottom));
  background: rgb(255 255 255 / 94%);
  box-shadow: 0 -10rpx 34rpx rgb(48 37 78 / 8%);
}

:deep(.publish-button) {
  height: 88rpx !important;
  font-size: 28rpx !important;
  background: linear-gradient(135deg, #7162dc, #a36fce) !important;
  border: 0 !important;
}

.upload-mask {
  position: fixed;
  inset: 0;
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgb(31 24 45 / 42%);
}

.upload-mask__card {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  align-items: center;
  padding: 48rpx;
  font-size: 24rpx;
  color: #5c5567;
  background: #fff;
  border-radius: 30rpx;
}
</style>
