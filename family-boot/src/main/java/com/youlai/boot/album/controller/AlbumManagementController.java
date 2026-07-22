package com.youlai.boot.album.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.youlai.boot.album.model.AlbumModels.AlbumAssetQuery;
import com.youlai.boot.album.model.AlbumModels.AlbumAssetSaveRequest;
import com.youlai.boot.album.model.AlbumModels.AlbumAssetVO;
import com.youlai.boot.album.model.AlbumModels.AlbumBatchGroupRequest;
import com.youlai.boot.album.model.AlbumModels.AlbumBatchTagRequest;
import com.youlai.boot.album.model.AlbumModels.AlbumGroupSaveRequest;
import com.youlai.boot.album.model.AlbumModels.AlbumGroupVO;
import com.youlai.boot.album.model.AlbumModels.AlbumTagSaveRequest;
import com.youlai.boot.album.model.AlbumModels.AlbumTagVO;
import com.youlai.boot.album.service.AlbumManagementService;
import com.youlai.boot.common.annotation.RepeatSubmit;
import com.youlai.boot.common.result.PageResult;
import com.youlai.boot.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 相册后台管理接口。
 */
@Tag(name = "11.相册管理")
@RestController
@RequestMapping("/api/v1/album")
@RequiredArgsConstructor
public class AlbumManagementController {

    private final AlbumManagementService albumManagementService;

    @Operation(summary = "相册资源分页列表")
    @GetMapping("/assets")
    @PreAuthorize("@ss.hasPerm('album:asset:list')")
    public PageResult<AlbumAssetVO> getAssetPage(AlbumAssetQuery query) {
        IPage<AlbumAssetVO> page = albumManagementService.getAssetPage(query);
        return PageResult.success(page);
    }

    @Operation(summary = "新增相册资源记录")
    @PostMapping("/assets")
    @RepeatSubmit
    @PreAuthorize("@ss.hasPerm('album:asset:create')")
    public Result<Void> saveAsset(@Valid @RequestBody AlbumAssetSaveRequest request) {
        return Result.judge(albumManagementService.saveAsset(request));
    }

    @Operation(summary = "获取相册资源表单")
    @GetMapping("/assets/{id}/form")
    @PreAuthorize("@ss.hasPerm('album:asset:update')")
    public Result<AlbumAssetSaveRequest> getAssetForm(@PathVariable Long id) {
        return Result.success(albumManagementService.getAssetForm(id));
    }

    @Operation(summary = "修改相册资源")
    @PutMapping("/assets/{id}")
    @PreAuthorize("@ss.hasPerm('album:asset:update')")
    public Result<Void> updateAsset(@PathVariable Long id, @Valid @RequestBody AlbumAssetSaveRequest request) {
        return Result.judge(albumManagementService.updateAsset(id, request));
    }

    @Operation(summary = "删除相册资源记录")
    @DeleteMapping("/assets/{ids}")
    @PreAuthorize("@ss.hasPerm('album:asset:delete')")
    public Result<Void> deleteAssets(
            @Parameter(description = "资源ID，多个以英文逗号分隔") @PathVariable String ids
    ) {
        return Result.judge(albumManagementService.deleteAssets(ids));
    }

    @Operation(summary = "批量调整资源分组")
    @PutMapping("/assets/{ids}/group")
    @PreAuthorize("@ss.hasPerm('album:asset:update')")
    public Result<Void> changeAssetGroup(
            @PathVariable String ids,
            @Valid @RequestBody AlbumBatchGroupRequest request
    ) {
        return Result.judge(albumManagementService.changeAssetGroup(ids, request.getGroupId()));
    }

    @Operation(summary = "批量设置资源标签")
    @PutMapping("/assets/{ids}/tags")
    @PreAuthorize("@ss.hasPerm('album:asset:update')")
    public Result<Void> replaceAssetTags(
            @PathVariable String ids,
            @Valid @RequestBody AlbumBatchTagRequest request
    ) {
        return Result.judge(albumManagementService.replaceAssetTags(ids, request.getTagIds()));
    }

    @Operation(summary = "相册分组列表")
    @GetMapping("/groups")
    @PreAuthorize("@ss.hasPerm('album:group:list')")
    public Result<List<AlbumGroupVO>> listGroups(@RequestParam(required = false) String keyword) {
        return Result.success(albumManagementService.listGroups(keyword));
    }

    @Operation(summary = "新增相册分组")
    @PostMapping("/groups")
    @RepeatSubmit
    @PreAuthorize("@ss.hasPerm('album:group:create')")
    public Result<Void> saveGroup(@Valid @RequestBody AlbumGroupSaveRequest request) {
        return Result.judge(albumManagementService.saveGroup(request));
    }

    @Operation(summary = "获取相册分组表单")
    @GetMapping("/groups/{id}/form")
    @PreAuthorize("@ss.hasPerm('album:group:update')")
    public Result<AlbumGroupSaveRequest> getGroupForm(@PathVariable Long id) {
        return Result.success(albumManagementService.getGroupForm(id));
    }

    @Operation(summary = "修改相册分组")
    @PutMapping("/groups/{id}")
    @PreAuthorize("@ss.hasPerm('album:group:update')")
    public Result<Void> updateGroup(@PathVariable Long id, @Valid @RequestBody AlbumGroupSaveRequest request) {
        return Result.judge(albumManagementService.updateGroup(id, request));
    }

    @Operation(summary = "删除相册分组")
    @DeleteMapping("/groups/{ids}")
    @PreAuthorize("@ss.hasPerm('album:group:delete')")
    public Result<Void> deleteGroups(@PathVariable String ids) {
        return Result.judge(albumManagementService.deleteGroups(ids));
    }

    @Operation(summary = "相册标签列表")
    @GetMapping("/tags")
    @PreAuthorize("@ss.hasPerm('album:tag:list')")
    public Result<List<AlbumTagVO>> listTags(@RequestParam(required = false) String keyword) {
        return Result.success(albumManagementService.listTags(keyword));
    }

    @Operation(summary = "新增相册标签")
    @PostMapping("/tags")
    @RepeatSubmit
    @PreAuthorize("@ss.hasPerm('album:tag:create')")
    public Result<Void> saveTag(@Valid @RequestBody AlbumTagSaveRequest request) {
        return Result.judge(albumManagementService.saveTag(request));
    }

    @Operation(summary = "获取相册标签表单")
    @GetMapping("/tags/{id}/form")
    @PreAuthorize("@ss.hasPerm('album:tag:update')")
    public Result<AlbumTagSaveRequest> getTagForm(@PathVariable Long id) {
        return Result.success(albumManagementService.getTagForm(id));
    }

    @Operation(summary = "修改相册标签")
    @PutMapping("/tags/{id}")
    @PreAuthorize("@ss.hasPerm('album:tag:update')")
    public Result<Void> updateTag(@PathVariable Long id, @Valid @RequestBody AlbumTagSaveRequest request) {
        return Result.judge(albumManagementService.updateTag(id, request));
    }

    @Operation(summary = "删除相册标签")
    @DeleteMapping("/tags/{ids}")
    @PreAuthorize("@ss.hasPerm('album:tag:delete')")
    public Result<Void> deleteTags(@PathVariable String ids) {
        return Result.judge(albumManagementService.deleteTags(ids));
    }
}
