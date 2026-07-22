package com.youlai.boot.family.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.youlai.boot.family.enums.FamilyCardSceneEnum;
import com.youlai.boot.family.enums.FamilyScheduleTypeEnum;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/** 家庭状态卡片与家庭日程的请求、响应模型。 */
public final class FamilyCardModels {
    private FamilyCardModels() {
    }

    /** 后台维护卡片展示规则的表单。 */
    @Data
    public static class FamilyCardConfigSaveRequest {
        @NotBlank(message = "标题模板不能为空")
        @Size(max = 100, message = "标题模板不能超过100个字符")
        private String titleTemplate;

        @NotBlank(message = "描述模板不能为空")
        @Size(max = 255, message = "描述模板不能超过255个字符")
        private String descriptionTemplate;

        @Size(max = 30, message = "标签不能超过30个字符")
        private String tagText;

        @Size(max = 50, message = "图标不能超过50个字符")
        private String icon;

        @Size(max = 1000, message = "背景地址不能超过1000个字符")
        private String backgroundUrl;

        @Size(max = 20, message = "按钮文案不能超过20个字符")
        private String actionText;

        @NotNull(message = "优先级不能为空")
        @Min(value = 0, message = "优先级不能小于0")
        @Max(value = 999, message = "优先级不能大于999")
        private Integer priority;

        @NotNull(message = "时间窗口不能为空")
        @Min(value = 1, message = "时间窗口至少为1天")
        @Max(value = 365, message = "时间窗口不能超过365天")
        private Integer windowDays;

        @NotNull(message = "状态不能为空")
        private Integer status;
    }

    /** 后台卡片配置视图。 */
    @Data
    public static class FamilyCardConfigVO {
        private Long id;
        private FamilyCardSceneEnum scene;
        private String sceneLabel;
        private String titleTemplate;
        private String descriptionTemplate;
        private String tagText;
        private String icon;
        private String backgroundUrl;
        private String actionText;
        private Integer priority;
        private Integer windowDays;
        private Integer status;
        private LocalDateTime updateTime;
    }

    /** 小程序个人中心动态卡片。 */
    @Data
    public static class FamilyStatusCardVO {
        private FamilyCardSceneEnum scene;
        private Long familyId;
        private String title;
        private String description;
        private String tagText;
        private String icon;
        private String backgroundUrl;
        private String actionText;
        private String actionPath;
        private Long referenceId;
        private Integer count;
    }

    /** 家庭日程保存参数。 */
    @Data
    public static class FamilyScheduleSaveRequest {
        @NotNull(message = "家庭ID不能为空")
        private Long familyId;

        @NotNull(message = "日程类型不能为空")
        private FamilyScheduleTypeEnum type;

        @NotBlank(message = "日程标题不能为空")
        @Size(max = 80, message = "日程标题不能超过80个字符")
        private String title;

        @Size(max = 255, message = "日程描述不能超过255个字符")
        private String description;

        @NotNull(message = "日程时间不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime eventTime;

        private Boolean repeatYearly;
    }

    /** 家庭日程展示视图。 */
    @Data
    public static class FamilyScheduleVO {
        private Long id;
        private Long familyId;
        private Long creatorId;
        private FamilyScheduleTypeEnum type;
        private String typeLabel;
        private String title;
        private String description;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime eventTime;
        private Boolean repeatYearly;
        private Integer status;
        private LocalDateTime createTime;
    }
}
