package com.youlai.boot.family.service;

import com.youlai.boot.family.model.FamilyCardModels.FamilyCardConfigSaveRequest;
import com.youlai.boot.family.model.FamilyCardModels.FamilyCardConfigVO;
import com.youlai.boot.family.model.FamilyCardModels.FamilyScheduleSaveRequest;
import com.youlai.boot.family.model.FamilyCardModels.FamilyScheduleVO;
import com.youlai.boot.family.model.FamilyCardModels.FamilyStatusCardVO;

import java.util.List;

/** 家庭状态卡片与家庭日程服务。 */
public interface FamilyCardService {
    List<FamilyStatusCardVO> listCurrentCards(Long familyId);

    void markPhotoCardRead(Long familyId);

    List<FamilyCardConfigVO> listConfigs();

    FamilyCardConfigSaveRequest getConfigForm(Long id);

    boolean updateConfig(Long id, FamilyCardConfigSaveRequest request);

    List<FamilyScheduleVO> listSchedules(Long familyId);

    FamilyScheduleVO createSchedule(FamilyScheduleSaveRequest request);

    FamilyScheduleVO updateSchedule(Long id, FamilyScheduleSaveRequest request);

    boolean completeSchedule(Long id);

    boolean deleteSchedule(Long id);
}
