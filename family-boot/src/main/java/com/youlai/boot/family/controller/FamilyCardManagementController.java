package com.youlai.boot.family.controller;

import com.youlai.boot.common.result.Result;
import com.youlai.boot.family.model.FamilyCardModels.FamilyCardConfigSaveRequest;
import com.youlai.boot.family.model.FamilyCardModels.FamilyCardConfigVO;
import com.youlai.boot.family.service.FamilyCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 家庭状态卡片后台配置接口。 */
@Tag(name = "12.家庭卡片配置")
@RestController
@RequestMapping("/api/v1/family-card-configs")
@RequiredArgsConstructor
public class FamilyCardManagementController {
    private final FamilyCardService familyCardService;

    @Operation(summary = "卡片场景配置列表")
    @GetMapping
    @PreAuthorize("@ss.hasPerm('family:card:list')")
    public Result<List<FamilyCardConfigVO>> listConfigs() {
        return Result.success(familyCardService.listConfigs());
    }

    @Operation(summary = "获取卡片场景配置表单")
    @GetMapping("/{id}/form")
    @PreAuthorize("@ss.hasPerm('family:card:update')")
    public Result<FamilyCardConfigSaveRequest> getConfigForm(@PathVariable Long id) {
        return Result.success(familyCardService.getConfigForm(id));
    }

    @Operation(summary = "修改卡片场景配置")
    @PutMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('family:card:update')")
    public Result<Void> updateConfig(
            @PathVariable Long id,
            @Valid @RequestBody FamilyCardConfigSaveRequest request
    ) {
        return Result.judge(familyCardService.updateConfig(id, request));
    }
}
