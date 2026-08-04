package com.youlai.boot.album.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.youlai.boot.album.model.AlbumModels.AlbumAssetVO;
import com.youlai.boot.album.model.AlbumModels.AlbumMomentBatchVO;
import com.youlai.boot.album.model.AlbumModels.AlbumMomentDetailVO;
import com.youlai.boot.album.model.AlbumModels.AlbumMomentCreateRequest;
import com.youlai.boot.album.model.AlbumModels.AlbumMomentQuery;
import com.youlai.boot.album.model.AlbumModels.AlbumDirectUploadInitRequest;
import com.youlai.boot.album.model.AlbumModels.AlbumDirectUploadInitVO;
import com.youlai.boot.album.model.AlbumModels.AlbumDirectUploadConfirmRequest;
import com.youlai.boot.album.model.AlbumModels.AlbumDirectUploadStatusVO;
import com.youlai.boot.album.service.AlbumManagementService;
import com.youlai.boot.album.service.AlbumDirectUploadService;
import com.youlai.boot.common.annotation.RepeatSubmit;
import com.youlai.boot.common.result.PageResult;
import com.youlai.boot.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * APP 客户端精彩时刻接口。
 */
@Tag(name = "APP相册")
@RestController
@RequestMapping("/api/v1/app/album")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AlbumAppController {

    private final AlbumManagementService albumManagementService;
    private final AlbumDirectUploadService albumDirectUploadService;

    /**
     * 分页读取可在客户端展示的精彩时刻。
     */
    @Operation(summary = "精彩时刻分页列表")
    @GetMapping("/moments")
    public PageResult<AlbumAssetVO> getMomentPage(@Valid AlbumMomentQuery query) {
        IPage<AlbumAssetVO> page = albumManagementService.getMomentPage(query);
        return PageResult.success(page);
    }

    /**
     * 按上传批次分页读取合并后的精彩时刻。
     */
    @Operation(summary = "精彩时刻批次分页列表")
    @GetMapping("/moment-batches")
    public PageResult<AlbumMomentBatchVO> getMomentBatchPage(@Valid AlbumMomentQuery query) {
        IPage<AlbumMomentBatchVO> page = albumManagementService.getMomentBatchPage(query);
        return PageResult.success(page);
    }

    /**
     * 读取一次批量发布中的全部相册资源。
     */
    @Operation(summary = "精彩时刻批次详情")
    @GetMapping("/moments/batches/{batchId}")
    public Result<AlbumMomentDetailVO> getMomentDetail(
            @PathVariable String batchId,
            @RequestParam Long familyId,
            @RequestParam Long albumId
    ) {
        return Result.success(albumManagementService.getMomentDetail(batchId, familyId, albumId));
    }

    /**
     * 将已上传的一组资源批量登记为当前用户的精彩时刻。
     */
    @Operation(summary = "批量发布精彩时刻")
    @PostMapping("/moments")
    @RepeatSubmit
    public Result<Void> saveMoment(@Valid @RequestBody AlbumMomentCreateRequest request) {
        return Result.judge(albumManagementService.saveMoment(request));
    }
    /** 初始化小程序到 COS 的批量直传票据。 */
    @Operation(summary = "初始化相册直传")
    @PostMapping("/direct-uploads")
    @RepeatSubmit
    public Result<AlbumDirectUploadInitVO> initializeDirectUpload(
            @Valid @RequestBody AlbumDirectUploadInitRequest request
    ) {
        return Result.success(albumDirectUploadService.initialize(request));
    }

    /** 客户端直传结束后一次性确认成功对象并启动异步处理。 */
    @Operation(summary = "确认相册直传")
    @PostMapping("/direct-uploads/confirm")
    @RepeatSubmit
    public Result<AlbumDirectUploadStatusVO> confirmDirectUpload(
            @Valid @RequestBody AlbumDirectUploadConfirmRequest request
    ) {
        return Result.success(albumDirectUploadService.confirm(request));
    }

    /** 轮询直传批次的异步处理状态。 */
    @Operation(summary = "查询相册直传状态")
    @GetMapping("/direct-uploads/{batchId}")
    public Result<AlbumDirectUploadStatusVO> getDirectUploadStatus(@PathVariable String batchId) {
        return Result.success(albumDirectUploadService.getStatus(batchId));
    }

    /**
     * 删除当前用户自己发布的精彩时刻。
     */
    @Operation(summary = "删除自己的精彩时刻")
    @DeleteMapping("/moments/{id}")
    public Result<Void> deleteOwnMoment(@PathVariable Long id) {
        return Result.judge(albumManagementService.deleteOwnMoment(id));
    }
}
