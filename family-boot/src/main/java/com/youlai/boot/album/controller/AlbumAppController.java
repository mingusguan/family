package com.youlai.boot.album.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.youlai.boot.album.model.AlbumModels.AlbumAssetVO;
import com.youlai.boot.album.model.AlbumModels.AlbumMomentBatchVO;
import com.youlai.boot.album.model.AlbumModels.AlbumMomentDetailVO;
import com.youlai.boot.album.model.AlbumModels.AlbumMomentCreateRequest;
import com.youlai.boot.album.model.AlbumModels.AlbumMomentQuery;
import com.youlai.boot.album.service.AlbumManagementService;
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
    /**
     * 删除当前用户自己发布的精彩时刻。
     */
    @Operation(summary = "删除自己的精彩时刻")
    @DeleteMapping("/moments/{id}")
    public Result<Void> deleteOwnMoment(@PathVariable Long id) {
        return Result.judge(albumManagementService.deleteOwnMoment(id));
    }
}
