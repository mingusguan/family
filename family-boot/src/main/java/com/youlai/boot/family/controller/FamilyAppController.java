package com.youlai.boot.family.controller;

import com.youlai.boot.common.annotation.RepeatSubmit;
import com.youlai.boot.common.result.Result;
import com.youlai.boot.family.model.FamilyModels.FamilyAlbumCreateRequest;
import com.youlai.boot.family.model.FamilyModels.FamilyAlbumVO;
import com.youlai.boot.family.model.FamilyModels.FamilyCreateRequest;
import com.youlai.boot.family.model.FamilyModels.FamilyDetailVO;
import com.youlai.boot.family.model.FamilyModels.FamilyInviteVO;
import com.youlai.boot.family.model.FamilyModels.FamilyVO;
import com.youlai.boot.family.service.FamilyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** APP 家庭与相册接口。 */
@Tag(name = "APP家庭")
@RestController
@RequestMapping("/api/v1/app/families")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class FamilyAppController {

    private final FamilyService familyService;

    @Operation(summary = "我的家庭列表")
    @GetMapping
    public Result<List<FamilyVO>> listMyFamilies() {
        return Result.success(familyService.listMyFamilies());
    }

    @Operation(summary = "新建家庭")
    @PostMapping
    @RepeatSubmit
    public Result<FamilyVO> createFamily(@Valid @RequestBody FamilyCreateRequest request) {
        return Result.success(familyService.createFamily(request));
    }

    @Operation(summary = "家庭详情与成员列表")
    @GetMapping("/{familyId}")
    public Result<FamilyDetailVO> getFamilyDetail(@PathVariable Long familyId) {
        return Result.success(familyService.getFamilyDetail(familyId));
    }

    @Operation(summary = "创建或获取家庭分享邀请")
    @PostMapping("/{familyId}/invites")
    public Result<FamilyInviteVO> createInvite(@PathVariable Long familyId) {
        return Result.success(familyService.createInvite(familyId));
    }

    @Operation(summary = "获取家庭邀请信息")
    @GetMapping("/invites/{code}")
    public Result<FamilyInviteVO> getInvite(@PathVariable String code) {
        return Result.success(familyService.getInvite(code));
    }

    @Operation(summary = "接受家庭邀请")
    @PostMapping("/invites/{code}/accept")
    @RepeatSubmit
    public Result<FamilyVO> acceptInvite(@PathVariable String code) {
        return Result.success(familyService.acceptInvite(code));
    }

    @Operation(summary = "家庭相册列表")
    @GetMapping("/{familyId}/albums")
    public Result<List<FamilyAlbumVO>> listAlbums(@PathVariable Long familyId) {
        return Result.success(familyService.listAlbums(familyId));
    }

    @Operation(summary = "新建家庭相册")
    @PostMapping("/{familyId}/albums")
    @RepeatSubmit
    public Result<FamilyAlbumVO> createAlbum(
            @PathVariable Long familyId,
            @Valid @RequestBody FamilyAlbumCreateRequest request
    ) {
        return Result.success(familyService.createAlbum(familyId, request));
    }
}
