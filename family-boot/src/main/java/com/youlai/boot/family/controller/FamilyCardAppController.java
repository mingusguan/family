package com.youlai.boot.family.controller;

import com.youlai.boot.common.annotation.RepeatSubmit;
import com.youlai.boot.common.result.Result;
import com.youlai.boot.family.model.FamilyCardModels.FamilyScheduleSaveRequest;
import com.youlai.boot.family.model.FamilyCardModels.FamilyScheduleVO;
import com.youlai.boot.family.model.FamilyCardModels.FamilyStatusCardVO;
import com.youlai.boot.family.service.FamilyCardService;
import io.swagger.v3.oas.annotations.Operation;
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

/** 小程序家庭状态卡片与家庭日程接口。 */
@Tag(name = "APP家庭状态卡片")
@RestController
@RequestMapping("/api/v1/app")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class FamilyCardAppController {
    private final FamilyCardService familyCardService;

    @Operation(summary = "获取个人中心当前家庭状态卡片列表")
    @GetMapping("/family-card")
    public Result<List<FamilyStatusCardVO>> listCurrentCards(@RequestParam(required = false) Long familyId) {
        return Result.success(familyCardService.listCurrentCards(familyId));
    }

    @Operation(summary = "标记家庭照片动态为已读")
    @PostMapping("/family-card/{familyId}/photo-read")
    public Result<Void> markPhotoCardRead(@PathVariable Long familyId) {
        familyCardService.markPhotoCardRead(familyId);
        return Result.success();
    }

    @Operation(summary = "家庭日程列表")
    @GetMapping("/family-schedules")
    public Result<List<FamilyScheduleVO>> listSchedules(@RequestParam Long familyId) {
        return Result.success(familyCardService.listSchedules(familyId));
    }

    @Operation(summary = "新建家庭日程")
    @PostMapping("/family-schedules")
    @RepeatSubmit
    public Result<FamilyScheduleVO> createSchedule(@Valid @RequestBody FamilyScheduleSaveRequest request) {
        return Result.success(familyCardService.createSchedule(request));
    }

    @Operation(summary = "修改家庭日程")
    @PutMapping("/family-schedules/{id}")
    public Result<FamilyScheduleVO> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody FamilyScheduleSaveRequest request
    ) {
        return Result.success(familyCardService.updateSchedule(id, request));
    }

    @Operation(summary = "完成家庭计划")
    @PutMapping("/family-schedules/{id}/complete")
    public Result<Void> completeSchedule(@PathVariable Long id) {
        return Result.judge(familyCardService.completeSchedule(id));
    }

    @Operation(summary = "删除家庭日程")
    @DeleteMapping("/family-schedules/{id}")
    public Result<Void> deleteSchedule(@PathVariable Long id) {
        return Result.judge(familyCardService.deleteSchedule(id));
    }
}
