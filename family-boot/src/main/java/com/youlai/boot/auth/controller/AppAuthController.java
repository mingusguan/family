package com.youlai.boot.auth.controller;

import com.youlai.boot.appuser.model.AppUserModels.AppUserLoginRequest;
import com.youlai.boot.appuser.model.AppUserModels.AppUserRegisterRequest;
import com.youlai.boot.auth.service.AppAuthService;
import com.youlai.boot.common.annotation.RepeatSubmit;
import com.youlai.boot.common.result.Result;
import com.youlai.boot.framework.security.model.AuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** APP 客户端公开注册与登录接口。 */
@Tag(name = "APP账号认证")
@RestController
@RequestMapping("/api/v1/auth/app")
@RequiredArgsConstructor
public class AppAuthController {

    private final AppAuthService appAuthService;

    @Operation(summary = "APP用户注册")
    @PostMapping("/register")
    @RepeatSubmit
    public Result<AuthenticationToken> register(@Valid @RequestBody AppUserRegisterRequest request) {
        return Result.success(appAuthService.register(request));
    }

    @Operation(summary = "APP账号密码登录")
    @PostMapping("/login")
    public Result<AuthenticationToken> login(@Valid @RequestBody AppUserLoginRequest request) {
        return Result.success(appAuthService.login(request));
    }
}
