package com.youlai.boot.family.model.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.youlai.boot.common.base.BaseEntity;
import com.youlai.boot.family.enums.FamilyScheduleTypeEnum;
import com.youlai.boot.family.model.FamilyCardModels.FamilyScheduleSaveRequest;
import lombok.Getter;
import lombok.Setter;

/** 家庭计划、生日、纪念日与节日。 */
@Getter
@Setter
@TableName("app_family_schedule")
public class FamilySchedule extends BaseEntity {
    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_COMPLETED = 2;

    private Long familyId;
    private Long creatorId;
    private FamilyScheduleTypeEnum type;
    private String title;
    private String description;
    private java.time.LocalDateTime eventTime;
    private Boolean repeatYearly;
    private Integer status;

    @TableLogic
    private Integer isDeleted;

    /** 创建家庭日程并应用类型对应的重复规则。 */
    @JsonIgnore
    public static FamilySchedule create(Long creatorId, FamilyScheduleSaveRequest request) {
        FamilySchedule schedule = new FamilySchedule();
        schedule.familyId = request.getFamilyId();
        schedule.creatorId = creatorId;
        schedule.status = STATUS_ACTIVE;
        schedule.isDeleted = 0;
        schedule.updateDetails(request);
        return schedule;
    }

    /** 修改日程内容，生日、纪念日和节日默认每年重复。 */
    public void updateDetails(FamilyScheduleSaveRequest request) {
        this.type = request.getType();
        this.title = request.getTitle().trim();
        this.description = request.getDescription();
        this.eventTime = request.getEventTime();
        this.repeatYearly = request.getRepeatYearly() != null
                ? request.getRepeatYearly()
                : request.getType().isSpecialDate();
    }

    /** 将普通计划标记为已完成。 */
    public void complete() {
        this.status = STATUS_COMPLETED;
    }
}
