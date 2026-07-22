package com.youlai.boot.appuser.controller;

import com.youlai.boot.appuser.model.AppUserModels.AppUserInfoVO;
import com.youlai.boot.appuser.model.AppUserModels.AppUserProfileUpdateRequest;
import com.youlai.boot.appuser.model.AppUserModels.AppUserProfileVO;
import com.youlai.boot.appuser.service.AppUserService;
import com.youlai.boot.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** APP 客户端用户接口。 */
@Tag(name = "APP用户")
@RestController
@RequestMapping("/api/v1/app/users")
@RequiredArgsConstructor
public class AppUserController {

    private final AppUserService appUserService;

    @Operation(summary = "获取当前APP用户信息")
    @GetMapping("/me")
    public Result<AppUserInfoVO> getCurrentUserInfo() {
        return Result.success(appUserService.getCurrentUserInfo());
    }

    @Operation(summary = "获取当前APP用户资料")
    @GetMapping("/profile")
    public Result<AppUserProfileVO> getCurrentUserProfile() {
        return Result.success(appUserService.getCurrentUserProfile());
    }

    @Operation(summary = "修改当前APP用户资料")
    @PutMapping("/profile")
    public Result<AppUserProfileVO> updateCurrentUserProfile(
            @Valid @RequestBody AppUserProfileUpdateRequest request
    ) {
        return Result.success(appUserService.updateCurrentUserProfile(request));
    }
}