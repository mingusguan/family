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
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
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

        @Schema(description = "描述关键字")
        private String contentKeyword;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @Schema(description = "查询开始日期")
        private LocalDate startDate;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @Schema(description = "查询结束日期")
        private LocalDate endDate;

        @Schema(description = "资源类型")
        private AlbumMediaTypeEnum mediaType;

        @Schema(description = "资源类型集合")
        private List<AlbumMediaTypeEnum> mediaTypes;

        @Schema(description = "年月集合，格式为yyyy-MM")
        private List<String> months;

        @Schema(description = "上传用户ID")
        private Long uploaderId;

        @Schema(description = "家庭ID")
        private Long familyId;

        @Schema(description = "家庭相册ID")
        private Long albumId;

        @Schema(description = "分组ID")
        private Long groupId;

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

        @Size(max = 100, message = "搜索关键字不能超过100个字符")
        @Schema(description = "描述关键字")
        private String keyword;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @Schema(description = "查询开始日期")
        private LocalDate startDate;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @Schema(description = "查询结束日期")
        private LocalDate endDate;

        @Size(max = 60, message = "最多选择60个月份")
        @Schema(description = "年月集合，格式为yyyy-MM")
        private List<@Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])", message = "年月格式必须为yyyy-MM") String> months;

        @Size(max = 3, message = "媒体类型最多选择3项")
        @Schema(description = "媒体类型集合")
        private List<AlbumMediaTypeEnum> mediaTypes;

        @NotNull(message = "请选择家庭")
        private Long familyId;

        @NotNull(message = "请选择相册")
        private Long albumId;
    }

    /** 小程序相册直传初始化参数。 */
    @Data
    public static class AlbumDirectUploadInitRequest {
        @NotNull(message = "请选择家庭")
        private Long familyId;
        @NotNull(message = "请选择相册")
        private Long albumId;
        @Size(max = 500, message = "描述长度不能超过500个字符")
        private String description;
        @Valid
        @NotEmpty(message = "请选择要上传的资源")
        @Size(max = 36, message = "一次最多上传36个资源")
        private List<AlbumDirectUploadFileRequest> files;
    }

    /** 直传文件的客户端可知元数据。 */
    @Data
    public static class AlbumDirectUploadFileRequest {
        @NotNull(message = "资源类型不能为空")
        private AlbumMediaTypeEnum mediaType;
        @NotBlank(message = "原始文件名不能为空")
        @Size(max = 255, message = "原始文件名长度不能超过255个字符")
        private String originalName;
        @Size(max = 100, message = "MIME类型长度不能超过100个字符")
        private String mimeType;
        @NotNull(message = "文件大小不能为空")
        @Min(value = 1, message = "文件不能为空")
        @Max(value = 104857600, message = "单个文件不能超过100MB")
        private Long fileSize;
        @Min(value = 0, message = "资源时长不能小于0")
        private Long duration;
        @Min(value = 0, message = "宽度不能小于0")
        private Integer width;
        @Min(value = 0, message = "高度不能小于0")
        private Integer height;
        private Boolean hasThumbnail = false;
    }

    /** 一次直传初始化结果。 */
    @Data
    public static class AlbumDirectUploadInitVO {
        private String batchId;
        private LocalDateTime expiresAt;
        private List<AlbumDirectUploadItemVO> uploads;
    }

    /** 一个原文件及可选封面的上传票据。 */
    @Data
    public static class AlbumDirectUploadItemVO {
        private Integer index;
        private AlbumDirectUploadTicketVO file;
        private AlbumDirectUploadTicketVO thumbnail;
    }

    /** COS PostObject 表单票据。 */
    @Data
    public static class AlbumDirectUploadTicketVO {
        private String uploadUrl;
        private String objectKey;
        private String policy;
        private String qSignAlgorithm;
        private String qAk;
        private String qKeyTime;
        private String qSignature;
        private String securityToken;
    }

    /** 客户端完成直传后的单次确认参数。 */
    @Data
    public static class AlbumDirectUploadConfirmRequest {
        @NotBlank(message = "上传批次不能为空")
        private String batchId;
        @NotNull(message = "请提交上传结果")
        @Size(max = 36, message = "上传结果不能超过36项")
        private List<@Min(0) @Max(35) Integer> uploadedIndexes;
        @Size(max = 36, message = "封面上传结果不能超过36项")
        private List<@Min(0) @Max(35) Integer> thumbnailUploadedIndexes;
    }

    /** 直传批次处理状态。 */
    @Data
    public static class AlbumDirectUploadStatusVO {
        private String batchId;
        private String status;
        private Integer total;
        private Integer processing;
        private Integer success;
        private Integer failed;
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
        @Size(max = 36, message = "一次最多发布36个资源")
        @Schema(description = "本次发布的资源列表", requiredMode = Schema.RequiredMode.REQUIRED)
        private List<AlbumMomentResourceRequest> resources;

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

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Schema(description = "当前资源的拍摄或录制时间")
        private LocalDateTime capturedAt;
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

    /** 相册资源管理视图。 */
    @Data
    @Schema(description = "相册资源管理视图")
    public static class AlbumAssetVO {
        private Long id;
        private String uploadBatchId;
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
        private String description;
        private LocalDateTime capturedAt;
        private Integer status;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
    }

    /** APP 相册批次列表视图。 */
    @Data
    @Schema(description = "APP相册批次列表视图")
    public static class AlbumMomentBatchVO {
        private String batchId;
        private Long uploaderId;
        private String uploaderName;
        private Long familyId;
        private Long albumId;
        private String description;
        private LocalDateTime capturedAt;
        private LocalDateTime createTime;
        private Long assetCount;
        private List<AlbumMomentCoverVO> covers;
    }

    /** APP 相册批次封面缩略图。 */
    @Data
    @Schema(description = "APP相册批次封面缩略图")
    public static class AlbumMomentCoverVO {
        private AlbumMediaTypeEnum mediaType;
        private String previewUrl;
    }

    /** APP 相册批次详情视图。 */
    @Data
    @Schema(description = "APP相册批次详情视图")
    public static class AlbumMomentDetailVO {
        private String batchId;
        private Long uploaderId;
        private String uploaderName;
        private Long familyId;
        private Long albumId;
        private String description;
        private LocalDateTime capturedAt;
        private LocalDateTime createTime;
        private List<AlbumAssetVO> assets;
    }

    /** Mapper 用于承载批次分页主查询结果。 */
    @Data
    public static class AlbumMomentBatchRow {
        private String batchId;
        private Long uploaderId;
        private Long familyId;
        private Long albumId;
        private String description;
        private LocalDateTime capturedAt;
        private LocalDateTime createTime;
        private Long assetCount;
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
}
