package com.youlai.boot.appuser.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.youlai.boot.appuser.model.AppUserModels.AppUserQuery;
import com.youlai.boot.appuser.model.AppUserModels.AppUserVO;
import com.youlai.boot.appuser.service.AppUserManagementService;
import com.youlai.boot.common.model.Option;
import com.youlai.boot.common.result.PageResult;
import com.youlai.boot.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * APP 用户后台管理接口。
 */
@Tag(name = "12.APP用户管理")
@RestController
@RequestMapping("/api/v1/app-users")
@RequiredArgsConstructor
public class AppUserManagementController {

    private final AppUserManagementService appUserManagementService;

    @Operation(summary = "APP用户分页列表")
    @GetMapping
    @PreAuthorize("@ss.hasPerm('app:user:list')")
    public PageResult<AppUserVO> getPage(@Valid AppUserQuery query) {
        IPage<AppUserVO> page = appUserManagementService.getPage(query);
        return PageResult.success(page);
    }

    @Operation(summary = "APP用户下拉选项")
    @GetMapping("/options")
    public Result<List<Option<String>>> listOptions(@RequestParam(required = false) String keyword) {
        return Result.success(appUserManagementService.listOptions(keyword));
    }

    @Operation(summary = "修改APP用户状态")
    @PatchMapping("/{userId}/status")
    @PreAuthorize("@ss.hasPerm('app:user:update')")
    public Result<Void> updateStatus(@PathVariable Long userId, @RequestParam Integer status) {
        return Result.judge(appUserManagementService.updateStatus(userId, status));
    }

    @Operation(summary = "删除APP用户")
    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPerm('app:user:delete')")
    public Result<Void> deleteUsers(@PathVariable String ids) {
        return Result.judge(appUserManagementService.deleteUsers(ids));
    }
}

