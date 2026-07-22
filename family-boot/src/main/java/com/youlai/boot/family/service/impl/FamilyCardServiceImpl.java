package com.youlai.boot.family.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.youlai.boot.album.mapper.AlbumAssetMapper;
import com.youlai.boot.album.model.entity.AlbumAsset;
import com.youlai.boot.common.exception.BusinessException;
import com.youlai.boot.family.enums.FamilyCardSceneEnum;
import com.youlai.boot.family.enums.FamilyScheduleTypeEnum;
import com.youlai.boot.family.mapper.FamilyCardConfigMapper;
import com.youlai.boot.family.mapper.FamilyCardReadStateMapper;
import com.youlai.boot.family.mapper.FamilyMapper;
import com.youlai.boot.family.mapper.FamilyMemberMapper;
import com.youlai.boot.family.mapper.FamilyScheduleMapper;
import com.youlai.boot.family.model.FamilyCardModels.FamilyCardConfigSaveRequest;
import com.youlai.boot.family.model.FamilyCardModels.FamilyCardConfigVO;
import com.youlai.boot.family.model.FamilyCardModels.FamilyScheduleSaveRequest;
import com.youlai.boot.family.model.FamilyCardModels.FamilyScheduleVO;
import com.youlai.boot.family.model.FamilyCardModels.FamilyStatusCardVO;
import com.youlai.boot.family.model.entity.Family;
import com.youlai.boot.family.model.entity.FamilyCardConfig;
import com.youlai.boot.family.model.entity.FamilyCardReadState;
import com.youlai.boot.family.model.entity.FamilyMember;
import com.youlai.boot.family.model.entity.FamilySchedule;
import com.youlai.boot.family.service.FamilyCardService;
import com.youlai.boot.family.service.FamilyService;
import com.youlai.boot.file.service.FileService;
import com.youlai.boot.framework.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/** 家庭状态卡片与家庭日程服务实现。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FamilyCardServiceImpl implements FamilyCardService {
    private static final int ENABLED = 1;

    private final FamilyCardConfigMapper configMapper;
    private final FamilyScheduleMapper scheduleMapper;
    private final FamilyCardReadStateMapper readStateMapper;
    private final FamilyMemberMapper memberMapper;
    private final FamilyMapper familyMapper;
    private final AlbumAssetMapper assetMapper;
    private final FamilyService familyService;
    private final FileService fileService;

    @Override
    public List<FamilyStatusCardVO> listCurrentCards(Long familyId) {
        Long userId = requireCurrentUserId();
        CardContext context = buildContext(userId, familyId);
        List<FamilyCardConfig> configs = configMapper.selectList(new LambdaQueryWrapper<FamilyCardConfig>()
                .eq(FamilyCardConfig::getStatus, ENABLED)
                .orderByDesc(FamilyCardConfig::getPriority)
                .orderByAsc(FamilyCardConfig::getId));
        Map<FamilyCardSceneEnum, BiFunction<FamilyCardConfig, CardContext, FamilyStatusCardVO>> resolvers =
                createResolvers();

        List<FamilyStatusCardVO> cards = new ArrayList<>();
        // 后台优先级决定轮播顺序，每个策略只负责判断自身是否满足展示条件。
        for (FamilyCardConfig config : configs) {
            BiFunction<FamilyCardConfig, CardContext, FamilyStatusCardVO> resolver = resolvers.get(config.getScene());
            if (resolver == null) {
                continue;
            }
            FamilyStatusCardVO card = resolver.apply(config, context);
            if (card != null) {
                cards.add(card);
            }
        }
        return cards;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markPhotoCardRead(Long familyId) {
        Long userId = requireCurrentUserId();
        familyService.ensureCurrentUserMember(familyId);
        LocalDateTime now = LocalDateTime.now();
        FamilyCardReadState state = findReadState(userId, familyId);
        if (state != null) {
            state.markPhotoRead(now);
            readStateMapper.updateById(state);
            return;
        }
        try {
            readStateMapper.insert(FamilyCardReadState.create(userId, familyId, now));
        } catch (DuplicateKeyException ignored) {
            // 唯一索引兜底并发首次点击，后到请求只更新时间游标。
            readStateMapper.update(null, new LambdaUpdateWrapper<FamilyCardReadState>()
                    .eq(FamilyCardReadState::getUserId, userId)
                    .eq(FamilyCardReadState::getFamilyId, familyId)
                    .set(FamilyCardReadState::getLastPhotoReadAt, now));
        }
    }

    @Override
    public List<FamilyCardConfigVO> listConfigs() {
        return configMapper.selectList(new LambdaQueryWrapper<FamilyCardConfig>()
                        .orderByDesc(FamilyCardConfig::getPriority)
                        .orderByAsc(FamilyCardConfig::getId))
                .stream().map(this::toConfigVO).toList();
    }

    @Override
    public FamilyCardConfigSaveRequest getConfigForm(Long id) {
        FamilyCardConfig config = requireConfig(id);
        FamilyCardConfigSaveRequest request = new FamilyCardConfigSaveRequest();
        request.setTitleTemplate(config.getTitleTemplate());
        request.setDescriptionTemplate(config.getDescriptionTemplate());
        request.setTagText(config.getTagText());
        request.setIcon(config.getIcon());
        request.setBackgroundUrl(config.getBackgroundUrl());
        request.setActionText(config.getActionText());
        request.setPriority(config.getPriority());
        request.setWindowDays(config.getWindowDays());
        request.setStatus(config.getStatus());
        return request;
    }

    @Override
    public boolean updateConfig(Long id, FamilyCardConfigSaveRequest request) {
        FamilyCardConfig config = requireConfig(id);
        config.updateConfig(request);
        return configMapper.updateById(config) > 0;
    }

    @Override
    public List<FamilyScheduleVO> listSchedules(Long familyId) {
        familyService.ensureCurrentUserMember(familyId);
        return scheduleMapper.selectList(new LambdaQueryWrapper<FamilySchedule>()
                        .eq(FamilySchedule::getFamilyId, familyId)
                        .orderByAsc(FamilySchedule::getStatus)
                        .orderByAsc(FamilySchedule::getEventTime))
                .stream().map(this::toScheduleVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FamilyScheduleVO createSchedule(FamilyScheduleSaveRequest request) {
        Long userId = requireCurrentUserId();
        familyService.ensureCurrentUserMember(request.getFamilyId());
        FamilySchedule schedule = FamilySchedule.create(userId, request);
        scheduleMapper.insert(schedule);
        log.info("创建家庭日程，userId={}, familyId={}, scheduleId={}, type={}",
                userId, request.getFamilyId(), schedule.getId(), schedule.getType());
        return toScheduleVO(schedule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FamilyScheduleVO updateSchedule(Long id, FamilyScheduleSaveRequest request) {
        Long userId = requireCurrentUserId();
        FamilySchedule schedule = requireSchedule(id);
        ensureScheduleOwner(schedule, userId);
        if (!schedule.getFamilyId().equals(request.getFamilyId())) {
            throw new BusinessException("不能修改日程所属家庭");
        }
        familyService.ensureCurrentUserMember(schedule.getFamilyId());
        schedule.updateDetails(request);
        scheduleMapper.updateById(schedule);
        return toScheduleVO(schedule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeSchedule(Long id) {
        Long userId = requireCurrentUserId();
        FamilySchedule schedule = requireSchedule(id);
        ensureScheduleOwner(schedule, userId);
        familyService.ensureCurrentUserMember(schedule.getFamilyId());
        if (schedule.getType() != FamilyScheduleTypeEnum.PLAN) {
            throw new BusinessException("生日、纪念日和节日无需标记完成");
        }
        schedule.complete();
        return scheduleMapper.updateById(schedule) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSchedule(Long id) {
        Long userId = requireCurrentUserId();
        FamilySchedule schedule = requireSchedule(id);
        ensureScheduleOwner(schedule, userId);
        familyService.ensureCurrentUserMember(schedule.getFamilyId());
        return scheduleMapper.deleteById(id) > 0;
    }

    private Map<FamilyCardSceneEnum, BiFunction<FamilyCardConfig, CardContext, FamilyStatusCardVO>>
    createResolvers() {
        Map<FamilyCardSceneEnum, BiFunction<FamilyCardConfig, CardContext, FamilyStatusCardVO>> resolvers =
                new EnumMap<>(FamilyCardSceneEnum.class);
        resolvers.put(FamilyCardSceneEnum.CREATE_FAMILY, this::resolveCreateFamily);
        resolvers.put(FamilyCardSceneEnum.INVITE_MEMBER, this::resolveInviteMember);
        resolvers.put(FamilyCardSceneEnum.UPCOMING_SCHEDULE, this::resolveUpcomingSchedule);
        resolvers.put(FamilyCardSceneEnum.RECENT_PHOTO, this::resolveRecentPhoto);
        resolvers.put(FamilyCardSceneEnum.TODAY_MEMORY, this::resolveTodayMemory);
        resolvers.put(FamilyCardSceneEnum.SPECIAL_DATE, this::resolveSpecialDate);
        return resolvers;
    }

    private FamilyStatusCardVO resolveCreateFamily(FamilyCardConfig config, CardContext context) {
        if (context.family() != null) {
            return null;
        }
        return buildCard(config, context, Map.of(), "/pages/mine/family/index", null, null, null);
    }

    private FamilyStatusCardVO resolveInviteMember(FamilyCardConfig config, CardContext context) {
        if (context.family() == null || context.memberCount() != 1) {
            return null;
        }
        Map<String, String> variables = Map.of("familyName", context.family().getName());
        String path = "/pages/mine/family/detail/index?familyId=" + context.family().getId();
        return buildCard(config, context, variables, path, null, 1, null);
    }

    private FamilyStatusCardVO resolveUpcomingSchedule(FamilyCardConfig config, CardContext context) {
        if (context.family() == null) {
            return null;
        }
        LocalDateTime deadline = context.now().plusDays(config.getWindowDays());
        FamilySchedule schedule = scheduleMapper.selectOne(new LambdaQueryWrapper<FamilySchedule>()
                .eq(FamilySchedule::getFamilyId, context.family().getId())
                .eq(FamilySchedule::getType, FamilyScheduleTypeEnum.PLAN)
                .eq(FamilySchedule::getStatus, FamilySchedule.STATUS_ACTIVE)
                .ge(FamilySchedule::getEventTime, context.now())
                .le(FamilySchedule::getEventTime, deadline)
                .orderByAsc(FamilySchedule::getEventTime)
                .last("LIMIT 1"));
        if (schedule == null) {
            return null;
        }
        Map<String, String> variables = new LinkedHashMap<>();
        variables.put("title", schedule.getTitle());
        variables.put("date", formatDate(schedule.getEventTime()));
        variables.put("days", String.valueOf(daysUntil(context.now(), schedule.getEventTime())));
        String path = "/pages/plan/index?familyId=" + context.family().getId() + "&scheduleId=" + schedule.getId();
        return buildCard(config, context, variables, path, schedule.getId(), 1, null);
    }

    private FamilyStatusCardVO resolveRecentPhoto(FamilyCardConfig config, CardContext context) {
        if (context.family() == null) {
            return null;
        }
        FamilyCardReadState state = findReadState(context.userId(), context.family().getId());
        LocalDateTime threshold = context.now().minusDays(config.getWindowDays());
        if (state != null && state.getLastPhotoReadAt() != null && state.getLastPhotoReadAt().isAfter(threshold)) {
            threshold = state.getLastPhotoReadAt();
        }
        LambdaQueryWrapper<AlbumAsset> condition = new LambdaQueryWrapper<AlbumAsset>()
                .eq(AlbumAsset::getFamilyId, context.family().getId())
                .eq(AlbumAsset::getStatus, ENABLED)
                .gt(AlbumAsset::getCreateTime, threshold);
        long count = assetMapper.selectCount(condition);
        if (count == 0) {
            return null;
        }
        AlbumAsset newest = assetMapper.selectOne(new LambdaQueryWrapper<AlbumAsset>()
                .eq(AlbumAsset::getFamilyId, context.family().getId())
                .eq(AlbumAsset::getStatus, ENABLED)
                .gt(AlbumAsset::getCreateTime, threshold)
                .orderByDesc(AlbumAsset::getCreateTime)
                .last("LIMIT 1"));
        Map<String, String> variables = Map.of("count", String.valueOf(count));
        String path = "/pages/album/index?familyId=" + context.family().getId();
        return buildCard(config, context, variables, path, newest == null ? null : newest.getId(),
                Math.toIntExact(count), resolveAssetBackground(newest));
    }

    private FamilyStatusCardVO resolveTodayMemory(FamilyCardConfig config, CardContext context) {
        if (context.family() == null) {
            return null;
        }
        int month = context.now().getMonthValue();
        int day = context.now().getDayOfMonth();
        int year = context.now().getYear();
        LambdaQueryWrapper<AlbumAsset> condition = new LambdaQueryWrapper<AlbumAsset>()
                .eq(AlbumAsset::getFamilyId, context.family().getId())
                .eq(AlbumAsset::getStatus, ENABLED)
                .isNotNull(AlbumAsset::getCapturedAt)
                .apply("MONTH(captured_at) = {0}", month)
                .apply("DAY(captured_at) = {0}", day)
                .apply("YEAR(captured_at) < {0}", year);
        long count = assetMapper.selectCount(condition);
        AlbumAsset memory = count == 0 ? null : assetMapper.selectOne(new LambdaQueryWrapper<AlbumAsset>()
                .eq(AlbumAsset::getFamilyId, context.family().getId())
                .eq(AlbumAsset::getStatus, ENABLED)
                .isNotNull(AlbumAsset::getCapturedAt)
                .apply("MONTH(captured_at) = {0}", month)
                .apply("DAY(captured_at) = {0}", day)
                .apply("YEAR(captured_at) < {0}", year)
                .orderByDesc(AlbumAsset::getCapturedAt)
                .last("LIMIT 1"));
        Map<String, String> variables = new LinkedHashMap<>();
        variables.put("familyName", context.family().getName());
        variables.put("count", String.valueOf(count));
        String path = count > 0
                ? "/pages/album/index?familyId=" + context.family().getId()
                : "/pages/album/publish?familyId=" + context.family().getId();
        FamilyStatusCardVO card = buildCard(config, context, variables, path,
                memory == null ? null : memory.getId(), Math.toIntExact(count), resolveAssetBackground(memory));
        if (count == 0) {
            // 没有历史照片时仍保留家庭时光入口，引导用户为未来留下今天的回忆。
            card.setDescription("今天还没有往年回忆，去留下属于你们的第一份今日故事吧");
            card.setActionText("留下回忆");
        }
        return card;
    }

    private FamilyStatusCardVO resolveSpecialDate(FamilyCardConfig config, CardContext context) {
        if (context.family() == null) {
            return null;
        }
        List<FamilySchedule> schedules = scheduleMapper.selectList(new LambdaQueryWrapper<FamilySchedule>()
                .eq(FamilySchedule::getFamilyId, context.family().getId())
                .eq(FamilySchedule::getStatus, FamilySchedule.STATUS_ACTIVE)
                .in(FamilySchedule::getType, FamilyScheduleTypeEnum.BIRTHDAY,
                        FamilyScheduleTypeEnum.ANNIVERSARY, FamilyScheduleTypeEnum.FESTIVAL));
        FamilySchedule upcoming = schedules.stream()
                .filter(schedule -> {
                    LocalDateTime next = nextOccurrence(schedule, context.now());
                    return !next.isBefore(context.now()) && !next.isAfter(context.now().plusDays(config.getWindowDays()));
                })
                .min(Comparator.comparing(schedule -> nextOccurrence(schedule, context.now())))
                .orElse(null);
        if (upcoming == null) {
            return null;
        }
        LocalDateTime next = nextOccurrence(upcoming, context.now());
        Map<String, String> variables = new LinkedHashMap<>();
        variables.put("title", upcoming.getTitle());
        variables.put("date", formatDate(next));
        variables.put("days", String.valueOf(daysUntil(context.now(), next)));
        String path = "/pages/plan/index?familyId=" + context.family().getId() + "&scheduleId=" + upcoming.getId();
        return buildCard(config, context, variables, path, upcoming.getId(), 1, null);
    }

    private CardContext buildContext(Long userId, Long requestedFamilyId) {
        FamilyMember membership;
        if (requestedFamilyId == null) {
            membership = memberMapper.selectOne(new LambdaQueryWrapper<FamilyMember>()
                    .eq(FamilyMember::getUserId, userId)
                    .orderByDesc(FamilyMember::getCreateTime)
                    .last("LIMIT 1"));
        } else {
            membership = memberMapper.selectOne(new LambdaQueryWrapper<FamilyMember>()
                    .eq(FamilyMember::getUserId, userId)
                    .eq(FamilyMember::getFamilyId, requestedFamilyId)
                    .last("LIMIT 1"));
            if (membership == null) {
                throw new BusinessException("您不是该家庭成员");
            }
        }
        if (membership == null) {
            return new CardContext(userId, null, 0, LocalDateTime.now());
        }
        Family family = familyMapper.selectById(membership.getFamilyId());
        if (family == null) {
            return new CardContext(userId, null, 0, LocalDateTime.now());
        }
        long memberCount = memberMapper.selectCount(new LambdaQueryWrapper<FamilyMember>()
                .eq(FamilyMember::getFamilyId, family.getId()));
        return new CardContext(userId, family, memberCount, LocalDateTime.now());
    }

    private FamilyStatusCardVO buildCard(
            FamilyCardConfig config,
            CardContext context,
            Map<String, String> variables,
            String actionPath,
            Long referenceId,
            Integer count,
            String dynamicBackground
    ) {
        Map<String, String> allVariables = new LinkedHashMap<>(variables);
        if (context.family() != null) {
            allVariables.putIfAbsent("familyName", context.family().getName());
        }
        FamilyStatusCardVO card = new FamilyStatusCardVO();
        card.setScene(config.getScene());
        card.setFamilyId(context.family() == null ? null : context.family().getId());
        card.setTitle(renderTemplate(config.getTitleTemplate(), allVariables));
        card.setDescription(renderTemplate(config.getDescriptionTemplate(), allVariables));
        card.setTagText(config.getTagText());
        card.setIcon(config.getIcon());
        card.setBackgroundUrl(StrUtil.isNotBlank(dynamicBackground)
                ? dynamicBackground
                : resolveConfigBackground(config.getBackgroundUrl()));
        card.setActionText(config.getActionText());
        card.setActionPath(actionPath);
        card.setReferenceId(referenceId);
        card.setCount(count);
        return card;
    }

    private String renderTemplate(String template, Map<String, String> variables) {
        String result = StrUtil.blankToDefault(template, "");
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", StrUtil.nullToEmpty(entry.getValue()));
        }
        return result;
    }

    private LocalDateTime nextOccurrence(FamilySchedule schedule, LocalDateTime now) {
        if (!Boolean.TRUE.equals(schedule.getRepeatYearly())) {
            return schedule.getEventTime();
        }
        MonthDay monthDay = MonthDay.from(schedule.getEventTime());
        LocalDateTime candidate = monthDay.atYear(now.getYear()).atTime(schedule.getEventTime().toLocalTime());
        return candidate.isBefore(now)
                ? monthDay.atYear(now.getYear() + 1).atTime(schedule.getEventTime().toLocalTime())
                : candidate;
    }

    private long daysUntil(LocalDateTime now, LocalDateTime target) {
        long hours = Math.max(0, Duration.between(now, target).toHours());
        return (hours + 23) / 24;
    }

    private String formatDate(LocalDateTime value) {
        return value.getMonthValue() + "月" + value.getDayOfMonth() + "日 "
                + String.format("%02d:%02d", value.getHour(), value.getMinute());
    }

    private String resolveConfigBackground(String backgroundUrl) {
        return StrUtil.isBlank(backgroundUrl) ? null : fileService.getAccessUrl(backgroundUrl);
    }

    private String resolveAssetBackground(AlbumAsset asset) {
        if (asset == null) {
            return null;
        }
        String path = StrUtil.isNotBlank(asset.getThumbnailUrl()) ? asset.getThumbnailUrl() : asset.getUrl();
        return fileService.getAccessUrl(path);
    }

    private FamilyCardReadState findReadState(Long userId, Long familyId) {
        return readStateMapper.selectOne(new LambdaQueryWrapper<FamilyCardReadState>()
                .eq(FamilyCardReadState::getUserId, userId)
                .eq(FamilyCardReadState::getFamilyId, familyId)
                .last("LIMIT 1"));
    }

    private FamilyCardConfig requireConfig(Long id) {
        FamilyCardConfig config = configMapper.selectById(id);
        if (config == null) {
            throw new BusinessException("家庭卡片配置不存在");
        }
        return config;
    }

    private FamilySchedule requireSchedule(Long id) {
        FamilySchedule schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
            throw new BusinessException("家庭日程不存在");
        }
        return schedule;
    }

    private void ensureScheduleOwner(FamilySchedule schedule, Long userId) {
        if (!userId.equals(schedule.getCreatorId())) {
            throw new BusinessException("只能修改自己创建的家庭日程");
        }
    }

    private Long requireCurrentUserId() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        return userId;
    }

    private FamilyCardConfigVO toConfigVO(FamilyCardConfig config) {
        FamilyCardConfigVO vo = new FamilyCardConfigVO();
        vo.setId(config.getId());
        vo.setScene(config.getScene());
        vo.setSceneLabel(config.getScene() == null ? null : config.getScene().getLabel());
        vo.setTitleTemplate(config.getTitleTemplate());
        vo.setDescriptionTemplate(config.getDescriptionTemplate());
        vo.setTagText(config.getTagText());
        vo.setIcon(config.getIcon());
        vo.setBackgroundUrl(config.getBackgroundUrl());
        vo.setActionText(config.getActionText());
        vo.setPriority(config.getPriority());
        vo.setWindowDays(config.getWindowDays());
        vo.setStatus(config.getStatus());
        vo.setUpdateTime(config.getUpdateTime());
        return vo;
    }

    private FamilyScheduleVO toScheduleVO(FamilySchedule schedule) {
        FamilyScheduleVO vo = new FamilyScheduleVO();
        vo.setId(schedule.getId());
        vo.setFamilyId(schedule.getFamilyId());
        vo.setCreatorId(schedule.getCreatorId());
        vo.setType(schedule.getType());
        vo.setTypeLabel(schedule.getType() == null ? null : schedule.getType().getLabel());
        vo.setTitle(schedule.getTitle());
        vo.setDescription(schedule.getDescription());
        vo.setEventTime(schedule.getEventTime());
        vo.setRepeatYearly(schedule.getRepeatYearly());
        vo.setStatus(schedule.getStatus());
        vo.setCreateTime(schedule.getCreateTime());
        return vo;
    }

    private record CardContext(Long userId, Family family, long memberCount, LocalDateTime now) {
    }
}
