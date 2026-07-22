package com.youlai.boot.album.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.youlai.boot.album.enums.AlbumMediaTypeEnum;
import com.youlai.boot.common.base.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 相册模块请求与响应模型集合。
 */
public final class AlbumModels {

    private AlbumModels() {
    }

    /** 相册资源分页查询参数。 */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "相册资源分页查询参数")
    public static class AlbumAssetQuery extends BaseQuery {

        @Schema(description = "文件名或描述关键字")
        private String keyword;

        @Schema(description = "资源类型")
        private AlbumMediaTypeEnum mediaType;

        @Schema(description = "上传用户ID")
        private Long uploaderId;

        @Schema(description = "家庭ID")
        private Long familyId;

        @Schema(description = "家庭相册ID")
        private Long albumId;

        @Schema(description = "分组ID")
        private Long groupId;

        @Schema(description = "标签ID")
        private Long tagId;

        @Schema(description = "状态：1-正常，0-隐藏")
        private Integer status;
    }

    /** APP 精彩时刻分页查询参数。 */
    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "APP精彩时刻分页查询参数")
    public static class AlbumMomentQuery extends BaseQuery {

        @Schema(description = "是否只查看当前用户上传的内容")
        private Boolean mine = false;

        @NotNull(message = "请选择家庭")
        private Long familyId;

        @NotNull(message = "请选择相册")
        private Long albumId;
    }

    /** APP 精彩时刻批量创建参数，上传者身份由服务端登录态决定。 */
    @Data
    @Schema(description = "APP精彩时刻批量创建参数")
    public static class AlbumMomentCreateRequest {

        @NotNull(message = "请选择家庭")
        private Long familyId;

        @NotNull(message = "请选择相册")
        private Long albumId;

        @Valid
        @NotEmpty(message = "请选择要发布的资源")
        @Size(max = 9, message = "一次最多发布9个资源")
        @Schema(description = "本次发布的资源列表", requiredMode = Schema.RequiredMode.REQUIRED)
        private List<AlbumMomentResourceRequest> resources;

        @Size(max = 10, message = "一次最多选择10个标签")
        private List<Long> tagIds;

        @Size(max = 500, message = "描述长度不能超过500个字符")
        private String description;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "拍摄时间")
        private LocalDateTime capturedAt;
    }

    /** APP 精彩时刻中的单个资源参数。 */
    @Data
    @Schema(description = "APP精彩时刻资源参数")
    public static class AlbumMomentResourceRequest {

        @NotNull(message = "资源类型不能为空")
        private AlbumMediaTypeEnum mediaType;

        @NotBlank(message = "资源地址不能为空")
        @Size(max = 1000, message = "资源地址长度不能超过1000个字符")
        @Schema(description = "对象存储资源地址", requiredMode = Schema.RequiredMode.REQUIRED)
        private String url;

        @Size(max = 1000, message = "缩略图地址长度不能超过1000个字符")
        private String thumbnailUrl;

        @NotBlank(message = "原始文件名不能为空")
        @Size(max = 255, message = "原始文件名长度不能超过255个字符")
        @Schema(description = "原始文件名", requiredMode = Schema.RequiredMode.REQUIRED)
        private String originalName;

        @Size(max = 100, message = "MIME类型长度不能超过100个字符")
        @Schema(description = "MIME类型")
        private String mimeType;

        @Min(value = 0, message = "文件大小不能小于0")
        @Schema(description = "文件大小，单位字节")
        private Long fileSize;

        @Min(value = 0, message = "资源时长不能小于0")
        private Long duration;

        @Min(value = 0, message = "宽度不能小于0")
        private Integer width;

        @Min(value = 0, message = "高度不能小于0")
        private Integer height;
    }
    /** 相册资源保存参数。 */
    @Data
    @Schema(description = "相册资源保存参数")
    public static class AlbumAssetSaveRequest {

        @Schema(description = "资源ID")
        private Long id;

        @NotNull(message = "上传用户不能为空")
        @Schema(description = "上传用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long uploaderId;

        @Schema(description = "所属家庭ID")
        private Long familyId;

        @Schema(description = "所属家庭相册ID")
        private Long albumId;

        @NotNull(message = "资源类型不能为空")
        @Schema(description = "资源类型", requiredMode = Schema.RequiredMode.REQUIRED)
        private AlbumMediaTypeEnum mediaType;

        @NotBlank(message = "资源地址不能为空")
        @Size(max = 1000, message = "资源地址长度不能超过1000个字符")
        @Schema(description = "资源访问地址", requiredMode = Schema.RequiredMode.REQUIRED)
        private String url;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        @Schema(description = "资源临时预览地址", accessMode = Schema.AccessMode.READ_ONLY)
        private String previewUrl;

        @Size(max = 1000, message = "缩略图地址长度不能超过1000个字符")
        @Schema(description = "视频封面或资源缩略图地址")
        private String thumbnailUrl;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        @Schema(description = "缩略图临时预览地址", accessMode = Schema.AccessMode.READ_ONLY)
        private String thumbnailPreviewUrl;

        @NotBlank(message = "原始文件名不能为空")
        @Size(max = 255, message = "原始文件名长度不能超过255个字符")
        @Schema(description = "原始文件名", requiredMode = Schema.RequiredMode.REQUIRED)
        private String originalName;

        @Size(max = 100, message = "MIME类型长度不能超过100个字符")
        @Schema(description = "MIME类型")
        private String mimeType;

        @Min(value = 0, message = "文件大小不能小于0")
        @Schema(description = "文件大小，单位字节")
        private Long fileSize;

        @Min(value = 0, message = "资源时长不能小于0")
        @Schema(description = "视频或音频时长，单位毫秒")
        private Long duration;

        @Min(value = 0, message = "宽度不能小于0")
        @Schema(description = "图片或视频宽度")
        private Integer width;

        @Min(value = 0, message = "高度不能小于0")
        @Schema(description = "图片或视频高度")
        private Integer height;

        @Schema(description = "分组ID")
        private Long groupId;

        @Schema(description = "标签ID集合")
        private List<Long> tagIds;

        @Size(max = 500, message = "描述长度不能超过500个字符")
        @Schema(description = "资源描述")
        private String description;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "拍摄或录制时间")
        private LocalDateTime capturedAt;

        @Min(value = 0, message = "状态只能为0或1")
        @Max(value = 1, message = "状态只能为0或1")
        @Schema(description = "状态：1-正常，0-隐藏")
        private Integer status = 1;
    }

    /** 批量调整资源分组参数。 */
    @Data
    @Schema(description = "批量调整资源分组参数")
    public static class AlbumBatchGroupRequest {
        @Schema(description = "分组ID，为空表示移出分组")
        private Long groupId;
    }

    /** 批量设置资源标签参数。 */
    @Data
    @Schema(description = "批量设置资源标签参数")
    public static class AlbumBatchTagRequest {
        @NotNull(message = "标签集合不能为空")
        @Schema(description = "标签ID集合，空数组表示清空标签", requiredMode = Schema.RequiredMode.REQUIRED)
        private List<Long> tagIds;
    }

    /** 相册分组保存参数。 */
    @Data
    @Schema(description = "相册分组保存参数")
    public static class AlbumGroupSaveRequest {
        @Schema(description = "分组ID")
        private Long id;

        @NotBlank(message = "分组名称不能为空")
        @Size(max = 50, message = "分组名称长度不能超过50个字符")
        @Schema(description = "分组名称", requiredMode = Schema.RequiredMode.REQUIRED)
        private String name;

        @Size(max = 255, message = "分组描述长度不能超过255个字符")
        @Schema(description = "分组描述")
        private String description;

        @Schema(description = "排序值")
        private Integer sort = 0;
    }

    /** APP 自定义话题标签参数。 */
    @Data
    public static class AlbumTagCreateRequest {
        @NotBlank(message = "标签名称不能为空")
        @Size(max = 30, message = "标签名称不能超过30个字符")
        private String name;
    }

    /** 相册标签保存参数。 */
    @Data
    @Schema(description = "相册标签保存参数")
    public static class AlbumTagSaveRequest {
        @Schema(description = "标签ID")
        private Long id;

        @NotBlank(message = "标签名称不能为空")
        @Size(max = 30, message = "标签名称长度不能超过30个字符")
        @Schema(description = "标签名称", requiredMode = Schema.RequiredMode.REQUIRED)
        private String name;

        @Size(max = 20, message = "标签颜色长度不能超过20个字符")
        @Schema(description = "标签颜色，例如#1677FF")
        private String color;

        @Schema(description = "排序值")
        private Integer sort = 0;
    }

    /** 相册资源管理视图。 */
    @Data
    @Schema(description = "相册资源管理视图")
    public static class AlbumAssetVO {
        private Long id;
        private Long uploaderId;
        private String uploaderName;
        private Long familyId;
        private Long albumId;
        private AlbumMediaTypeEnum mediaType;
        private String mediaTypeLabel;
        private String url;
        private String previewUrl;
        private String thumbnailUrl;
        private String thumbnailPreviewUrl;
        private String originalName;
        private String mimeType;
        private Long fileSize;
        private Long duration;
        private Integer width;
        private Integer height;
        private Long groupId;
        private String groupName;
        private List<AlbumTagVO> tags;
        private String description;
        private LocalDateTime capturedAt;
        private Integer status;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
    }

    /** 相册分组视图。 */
    @Data
    @Schema(description = "相册分组视图")
    public static class AlbumGroupVO {
        private Long id;
        private String name;
        private String description;
        private Integer sort;
        private LocalDateTime createTime;
    }

    /** 相册标签视图。 */
    @Data
    @Schema(description = "相册标签视图")
    public static class AlbumTagVO {
        private Long id;
        private String name;
        private String color;
        private Integer sort;
        private LocalDateTime createTime;
    }
}
