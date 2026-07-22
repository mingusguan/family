package com.youlai.boot.family.model.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.youlai.boot.common.base.BaseEntity;
import com.youlai.boot.family.enums.FamilyCardSceneEnum;
import com.youlai.boot.family.model.FamilyCardModels.FamilyCardConfigSaveRequest;
import lombok.Getter;
import lombok.Setter;

/** 家庭状态卡片运营配置。 */
@Getter
@Setter
@TableName("app_family_card_config")
public class FamilyCardConfig extends BaseEntity {
    private FamilyCardSceneEnum scene;
    private String titleTemplate;
    private String descriptionTemplate;
    private String tagText;
    private String icon;
    private String backgroundUrl;
    private String actionText;
    private Integer priority;
    private Integer windowDays;
    private Integer status;

    @TableLogic
    private Integer isDeleted;

    /** 更新后台允许维护的卡片展示规则。 */
    public void updateConfig(FamilyCardConfigSaveRequest request) {
        this.titleTemplate = request.getTitleTemplate().trim();
        this.descriptionTemplate = request.getDescriptionTemplate().trim();
        this.tagText = request.getTagText();
        this.icon = request.getIcon();
        this.backgroundUrl = request.getBackgroundUrl();
        this.actionText = request.getActionText();
        this.priority = request.getPriority();
        this.windowDays = request.getWindowDays();
        this.status = request.getStatus();
    }
}
