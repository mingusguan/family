package com.youlai.boot.album.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.youlai.boot.album.enums.AlbumMediaTypeEnum;
import com.youlai.boot.album.mapper.AlbumAssetMapper;
import com.youlai.boot.album.mapper.AlbumAssetTagMapper;
import com.youlai.boot.album.mapper.AlbumGroupMapper;
import com.youlai.boot.album.mapper.AlbumTagMapper;
import com.youlai.boot.album.model.AlbumModels.AlbumAssetQuery;
import com.youlai.boot.album.model.AlbumModels.AlbumAssetSaveRequest;
import com.youlai.boot.album.model.AlbumModels.AlbumAssetVO;
import com.youlai.boot.album.model.AlbumModels.AlbumMomentCreateRequest;
import com.youlai.boot.album.model.AlbumModels.AlbumMomentQuery;
import com.youlai.boot.album.model.AlbumModels.AlbumGroupSaveRequest;
import com.youlai.boot.album.model.AlbumModels.AlbumGroupVO;
import com.youlai.boot.album.model.AlbumModels.AlbumTagCreateRequest;
import com.youlai.boot.album.model.AlbumModels.AlbumTagSaveRequest;
import com.youlai.boot.album.model.AlbumModels.AlbumTagVO;
import com.youlai.boot.album.model.entity.AlbumAsset;
import com.youlai.boot.album.model.entity.AlbumAssetTag;
import com.youlai.boot.album.model.entity.AlbumGroup;
import com.youlai.boot.album.model.entity.AlbumTag;
import com.youlai.boot.album.service.AlbumManagementService;
import com.youlai.boot.appuser.model.entity.AppUser;
import com.youlai.boot.appuser.service.AppUserService;
import com.youlai.boot.common.exception.BusinessException;
import com.youlai.boot.family.service.FamilyService;
import com.youlai.boot.file.service.FileService;
import com.youlai.boot.framework.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 相册后台管理服务实现。
 */
@Service
@RequiredArgsConstructor
public class AlbumManagementServiceImpl implements AlbumManagementService {

    private final AlbumAssetMapper assetMapper;
    private final AlbumGroupMapper groupMapper;
    private final AlbumTagMapper tagMapper;
    private final AlbumAssetTagMapper assetTagMapper;
    private final AppUserService appUserService;
    private final FileService fileService;
    private final FamilyService familyService;

    @Override
    public IPage<AlbumAssetVO> getAssetPage(AlbumAssetQuery query) {
        List<Long> taggedAssetIds = resolveAssetIdsByTagId(query.getTagId());
        if (query.getTagId() != null && taggedAssetIds.isEmpty()) {
            return emptyAssetPage(query);
        }

        String contentKeyword = StrUtil.trim(query.getContentKeyword());
        List<Long> contentTagAssetIds = resolveAssetIdsByTagKeyword(contentKeyword);
        List<YearMonth> monthFilters = parseMonthFilters(query.getMonths());
        validateDateRange(query);
        LocalDateTime startAt = query.getStartDate() == null ? null : query.getStartDate().atStartOfDay();
        LocalDateTime endAt = query.getEndDate() == null ? null : query.getEndDate().plusDays(1).atStartOfDay();

        LambdaQueryWrapper<AlbumAsset> wrapper = new LambdaQueryWrapper<AlbumAsset>()
                .eq(query.getMediaType() != null, AlbumAsset::getMediaType, query.getMediaType())
                .in(CollectionUtil.isNotEmpty(query.getMediaTypes()), AlbumAsset::getMediaType, query.getMediaTypes())
                .eq(query.getUploaderId() != null, AlbumAsset::getUploaderId, query.getUploaderId())
                .eq(query.getFamilyId() != null, AlbumAsset::getFamilyId, query.getFamilyId())
                .eq(query.getAlbumId() != null, AlbumAsset::getAlbumId, query.getAlbumId())
                .eq(query.getGroupId() != null, AlbumAsset::getGroupId, query.getGroupId())
                .eq(query.getStatus() != null, AlbumAsset::getStatus, query.getStatus())
                .in(CollectionUtil.isNotEmpty(taggedAssetIds), AlbumAsset::getId, taggedAssetIds)
                .and(StrUtil.isNotBlank(query.getKeyword()), condition -> condition
                        .like(AlbumAsset::getOriginalName, query.getKeyword())
                        .or()
                        .like(AlbumAsset::getDescription, query.getKeyword()))
                .and(StrUtil.isNotBlank(contentKeyword), condition -> {
                    condition.like(AlbumAsset::getDescription, contentKeyword);
                    if (CollectionUtil.isNotEmpty(contentTagAssetIds)) {
                        condition.or().in(AlbumAsset::getId, contentTagAssetIds);
                    }
                })
                .and(startAt != null, condition -> condition
                        .ge(AlbumAsset::getCapturedAt, startAt)
                        .lt(AlbumAsset::getCapturedAt, endAt)
                        .or(fallback -> fallback
                                .isNull(AlbumAsset::getCapturedAt)
                                .ge(AlbumAsset::getCreateTime, startAt)
                                .lt(AlbumAsset::getCreateTime, endAt)))
                .and(CollectionUtil.isNotEmpty(monthFilters), monthConditions -> {
                    for (int index = 0; index < monthFilters.size(); index++) {
                        YearMonth month = monthFilters.get(index);
                        LocalDateTime monthStart = month.atDay(1).atStartOfDay();
                        LocalDateTime monthEnd = month.plusMonths(1).atDay(1).atStartOfDay();
                        monthConditions.or(index > 0).nested(condition -> condition
                                .ge(AlbumAsset::getCapturedAt, monthStart)
                                .lt(AlbumAsset::getCapturedAt, monthEnd)
                                .or(fallback -> fallback
                                        .isNull(AlbumAsset::getCapturedAt)
                                        .ge(AlbumAsset::getCreateTime, monthStart)
                                        .lt(AlbumAsset::getCreateTime, monthEnd)));
                    }
                })
                .orderByDesc(AlbumAsset::getCapturedAt)
                .orderByDesc(AlbumAsset::getCreateTime);

        Page<AlbumAsset> entityPage = assetMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()),
                wrapper
        );
        return assembleAssetPage(entityPage);
    }

    @Override
    public IPage<AlbumAssetVO> getMomentPage(AlbumMomentQuery query) {
        familyService.ensureCurrentUserMember(query.getFamilyId());
        familyService.ensureAlbumBelongsToFamily(query.getAlbumId(), query.getFamilyId());

        AlbumAssetQuery assetQuery = new AlbumAssetQuery();
        assetQuery.setPageNum(query.getPageNum());
        assetQuery.setPageSize(query.getPageSize());
        assetQuery.setFamilyId(query.getFamilyId());
        assetQuery.setAlbumId(query.getAlbumId());
        assetQuery.setStatus(1);
        assetQuery.setContentKeyword(normalizeContentKeyword(query.getKeyword()));
        assetQuery.setStartDate(query.getStartDate());
        assetQuery.setEndDate(query.getEndDate());
        assetQuery.setMonths(query.getMonths());
        assetQuery.setMediaTypes(query.getMediaTypes());
        if (Boolean.TRUE.equals(query.getMine())) {
            assetQuery.setUploaderId(requireCurrentUserId());
        }
        // 客户端只读取当前家庭相册中状态正常的资源，后台隐藏后会立即从 APP 消失。
        return getAssetPage(assetQuery);
    }

    private List<Long> resolveAssetIdsByTagId(Long tagId) {
        if (tagId == null) {
            return Collections.emptyList();
        }
        return assetTagMapper.selectList(
                        new LambdaQueryWrapper<AlbumAssetTag>()
                                .eq(AlbumAssetTag::getTagId, tagId)
                                .select(AlbumAssetTag::getAssetId)
                ).stream()
                .map(AlbumAssetTag::getAssetId)
                .distinct()
                .toList();
    }

    private List<Long> resolveAssetIdsByTagKeyword(String keyword) {
        if (StrUtil.isBlank(keyword)) {
            return Collections.emptyList();
        }
        List<Long> tagIds = tagMapper.selectList(
                        new LambdaQueryWrapper<AlbumTag>()
                                .like(AlbumTag::getName, keyword)
                                .select(AlbumTag::getId)
                ).stream()
                .map(AlbumTag::getId)
                .toList();
        if (tagIds.isEmpty()) {
            return Collections.emptyList();
        }
        // 标签和资源分表查询后一次性收集资源 ID，避免列表搜索引入多表 Join。
        return assetTagMapper.selectList(
                        new LambdaQueryWrapper<AlbumAssetTag>()
                                .in(AlbumAssetTag::getTagId, tagIds)
                                .select(AlbumAssetTag::getAssetId)
                ).stream()
                .map(AlbumAssetTag::getAssetId)
                .distinct()
                .toList();
    }

    private List<YearMonth> parseMonthFilters(List<String> months) {
        if (CollectionUtil.isEmpty(months)) {
            return Collections.emptyList();
        }
        try {
            return months.stream()
                    .filter(StrUtil::isNotBlank)
                    .map(String::trim)
                    .map(YearMonth::parse)
                    .distinct()
                    .toList();
        } catch (DateTimeParseException exception) {
            throw new BusinessException("年月格式必须为yyyy-MM");
        }
    }

    private void validateDateRange(AlbumAssetQuery query) {
        if ((query.getStartDate() == null) != (query.getEndDate() == null)) {
            throw new BusinessException("查询开始日期和结束日期必须同时填写");
        }
        if (query.getStartDate() != null && query.getStartDate().isAfter(query.getEndDate())) {
            throw new BusinessException("查询开始日期不能晚于结束日期");
        }
    }

    private String normalizeContentKeyword(String keyword) {
        String normalized = StrUtil.trim(keyword);
        return StrUtil.isBlank(normalized) ? null : normalized.replaceFirst("^#+", "");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveMoment(AlbumMomentCreateRequest request) {
        Long uploaderId = requireCurrentUserId();
        validateOwner(uploaderId);
        familyService.ensureCurrentUserMember(request.getFamilyId());
        familyService.ensureAlbumBelongsToFamily(request.getAlbumId(), request.getFamilyId());
        validateTags(request.getTagIds());
        // 同一批次资源共用家庭、相册、标签和描述，统一组装后使用 MyBatis-Plus 批量写入。
        List<AlbumAsset> assets = request.getResources().stream().map(resource -> {
            AlbumAsset asset = AlbumAsset.createMoment(uploaderId, request, resource);
            asset.changeGroup(resolveMediaGroupId(resource.getMediaType()));
            return asset;
        }).toList();
        boolean saved = Db.saveBatch(assets);
        if (saved) {
            replaceRelations(assets.stream().map(AlbumAsset::getId).toList(), request.getTagIds());
        }
        return saved;
    }

    @Override
    @Transactional
    public boolean deleteOwnMoment(Long id) {
        Long currentUserId = requireCurrentUserId();
        AlbumAsset asset = requireAsset(id);
        if (!Objects.equals(asset.getUploaderId(), currentUserId)) {
            throw new BusinessException("只能删除自己发布的精彩时刻");
        }
        assetTagMapper.delete(new LambdaQueryWrapper<AlbumAssetTag>().eq(AlbumAssetTag::getAssetId, id));
        // 客户端删除只移除相册元数据，对象存储文件保留，避免误删其他业务引用。
        return assetMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AlbumTagVO getOrCreateTag(AlbumTagCreateRequest request) {
        String name = request.getName().trim().replaceFirst("^#+", "");
        if (StrUtil.isBlank(name)) {
            throw new BusinessException("标签名称不能为空");
        }
        AlbumTag existing = tagMapper.selectOne(new LambdaQueryWrapper<AlbumTag>()
                .eq(AlbumTag::getName, name)
                .last("LIMIT 1"));
        if (existing != null) {
            return toTagVO(existing);
        }
        AlbumTagSaveRequest saveRequest = new AlbumTagSaveRequest();
        saveRequest.setName(name);
        saveRequest.setColor("#7C6FF6");
        saveRequest.setSort(0);
        AlbumTag tag = AlbumTag.create(saveRequest);
        tagMapper.insert(tag);
        return toTagVO(tag);
    }
    @Override
    public AlbumAssetSaveRequest getAssetForm(Long id) {
        AlbumAsset asset = requireAsset(id);
        AlbumAssetSaveRequest request = toAssetForm(asset);
        request.setTagIds(assetTagMapper.selectList(
                        new LambdaQueryWrapper<AlbumAssetTag>()
                                .eq(AlbumAssetTag::getAssetId, id)
                                .select(AlbumAssetTag::getTagId)
                ).stream()
                .map(AlbumAssetTag::getTagId)
                .toList());
        return request;
    }

    @Override
    @Transactional
    public boolean saveAsset(AlbumAssetSaveRequest request) {
        validateOwner(request.getUploaderId());
        validateTags(request.getTagIds());
        AlbumAsset asset = AlbumAsset.create(request);
        // 资源分组由媒体类型统一决定，避免不同入口提交不一致的分组。
        asset.changeGroup(resolveMediaGroupId(request.getMediaType()));
        boolean saved = assetMapper.insert(asset) > 0;
        if (saved) {
            replaceRelations(List.of(asset.getId()), request.getTagIds());
        }
        return saved;
    }

    @Override
    @Transactional
    public boolean updateAsset(Long id, AlbumAssetSaveRequest request) {
        AlbumAsset asset = requireAsset(id);
        validateOwner(request.getUploaderId());
        validateTags(request.getTagIds());
        asset.updateMetadata(request);
        // 更换资源文件后同步刷新系统分组。
        asset.changeGroup(resolveMediaGroupId(request.getMediaType()));
        boolean updated = assetMapper.updateById(asset) > 0;
        if (updated) {
            replaceRelations(List.of(id), request.getTagIds());
        }
        return updated;
    }

    @Override
    @Transactional
    public boolean deleteAssets(String ids) {
        List<Long> idList = parseIds(ids);
        assetTagMapper.delete(new LambdaQueryWrapper<AlbumAssetTag>().in(AlbumAssetTag::getAssetId, idList));
        // 后台删除仅移除资源元数据，云存储文件保留，避免误删被其他业务引用的对象
        return assetMapper.deleteByIds(idList) > 0;
    }

    @Override
    @Transactional
    public boolean changeAssetGroup(String ids, Long groupId) {
        List<Long> idList = parseIds(ids);
        if (groupId != null) {
            requireGroup(groupId);
        }

        // 归属校验通过后使用单条 SQL 批量变更，避免循环更新数据库。
        return assetMapper.updateGroupBatch(idList, groupId) == idList.size();
    }

    @Override
    @Transactional
    public boolean replaceAssetTags(String ids, List<Long> tagIds) {
        List<Long> idList = parseIds(ids);
        requireAssets(idList);
        validateTags(tagIds);
        replaceRelations(idList, tagIds);
        return true;
    }

    @Override
    public List<AlbumGroupVO> listGroups(String keyword) {
        List<AlbumGroup> groups = groupMapper.selectList(new LambdaQueryWrapper<AlbumGroup>()
                .like(StrUtil.isNotBlank(keyword), AlbumGroup::getName, keyword)
                .orderByAsc(AlbumGroup::getSort)
                .orderByDesc(AlbumGroup::getCreateTime));
        return groups.stream().map(this::toGroupVO).toList();
    }

    @Override
    public AlbumGroupSaveRequest getGroupForm(Long id) {
        AlbumGroup group = requireGroup(id);
        AlbumGroupSaveRequest request = new AlbumGroupSaveRequest();
        request.setId(group.getId());
        request.setName(group.getName());
        request.setDescription(group.getDescription());
        request.setSort(group.getSort());
        return request;
    }

    @Override
    public boolean saveGroup(AlbumGroupSaveRequest request) {
        validateGroupName(request.getName(), null);
        return groupMapper.insert(AlbumGroup.create(request)) > 0;
    }

    @Override
    public boolean updateGroup(Long id, AlbumGroupSaveRequest request) {
        AlbumGroup group = requireGroup(id);
        validateGroupName(request.getName(), id);
        group.updateMetadata(request);
        return groupMapper.updateById(group) > 0;
    }

    @Override
    @Transactional
    public boolean deleteGroups(String ids) {
        List<Long> idList = parseIds(ids);
        long boundAssets = assetMapper.selectCount(new LambdaQueryWrapper<AlbumAsset>().in(AlbumAsset::getGroupId, idList));
        if (boundAssets > 0) {
            throw new BusinessException("分组下仍有资源，请先移动资源后再删除");
        }
        return groupMapper.deleteByIds(idList) > 0;
    }

    @Override
    public List<AlbumTagVO> listTags(String keyword) {
        List<AlbumTag> tags = tagMapper.selectList(new LambdaQueryWrapper<AlbumTag>()
                .like(StrUtil.isNotBlank(keyword), AlbumTag::getName, keyword)
                .orderByAsc(AlbumTag::getSort)
                .orderByDesc(AlbumTag::getCreateTime));
        return tags.stream().map(this::toTagVO).toList();
    }

    @Override
    public AlbumTagSaveRequest getTagForm(Long id) {
        AlbumTag tag = requireTag(id);
        AlbumTagSaveRequest request = new AlbumTagSaveRequest();
        request.setId(tag.getId());
        request.setName(tag.getName());
        request.setColor(tag.getColor());
        request.setSort(tag.getSort());
        return request;
    }

    @Override
    public boolean saveTag(AlbumTagSaveRequest request) {
        validateTagName(request.getName(), null);
        return tagMapper.insert(AlbumTag.create(request)) > 0;
    }

    @Override
    public boolean updateTag(Long id, AlbumTagSaveRequest request) {
        AlbumTag tag = requireTag(id);
        validateTagName(request.getName(), id);
        tag.updateMetadata(request);
        return tagMapper.updateById(tag) > 0;
    }

    @Override
    @Transactional
    public boolean deleteTags(String ids) {
        List<Long> idList = parseIds(ids);
        assetTagMapper.delete(new LambdaQueryWrapper<AlbumAssetTag>().in(AlbumAssetTag::getTagId, idList));
        return tagMapper.deleteByIds(idList) > 0;
    }

    private IPage<AlbumAssetVO> assembleAssetPage(Page<AlbumAsset> entityPage) {
        Page<AlbumAssetVO> result = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<AlbumAsset> assets = entityPage.getRecords();
        if (assets.isEmpty()) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        Set<Long> assetIds = assets.stream().map(AlbumAsset::getId).collect(Collectors.toSet());
        Set<Long> groupIds = assets.stream().map(AlbumAsset::getGroupId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> groupNames = groupIds.isEmpty()
                ? Collections.emptyMap()
                : groupMapper.selectByIds(groupIds).stream().collect(Collectors.toMap(AlbumGroup::getId, AlbumGroup::getName));

        List<AlbumAssetTag> relations = assetTagMapper.selectList(
                new LambdaQueryWrapper<AlbumAssetTag>().in(AlbumAssetTag::getAssetId, assetIds)
        );
        Map<Long, List<Long>> tagIdsByAsset = relations.stream().collect(Collectors.groupingBy(
                AlbumAssetTag::getAssetId,
                LinkedHashMap::new,
                Collectors.mapping(AlbumAssetTag::getTagId, Collectors.toList())
        ));
        Set<Long> tagIds = relations.stream().map(AlbumAssetTag::getTagId).collect(Collectors.toSet());
        Map<Long, AlbumTag> tags = tagIds.isEmpty()
                ? Collections.emptyMap()
                : tagMapper.selectByIds(tagIds).stream().collect(Collectors.toMap(AlbumTag::getId, Function.identity()));
        Map<Long, String> uploaderNames = getUserNames(assets.stream().map(AlbumAsset::getUploaderId).collect(Collectors.toSet()));

        // 分页主表查询完成后，批量组装分组、标签和上传用户，避免多表 Join 与 N+1 查询
        result.setRecords(assets.stream().map(asset -> toAssetVO(
                asset,
                groupNames.get(asset.getGroupId()),
                tagIdsByAsset.getOrDefault(asset.getId(), Collections.emptyList()).stream()
                        .map(tags::get)
                        .filter(Objects::nonNull)
                        .map(this::toTagVO)
                        .toList(),
                uploaderNames.get(asset.getUploaderId())
        )).toList());
        return result;
    }

    private IPage<AlbumAssetVO> emptyAssetPage(AlbumAssetQuery query) {
        Page<AlbumAssetVO> page = new Page<>(query.getPageNum(), query.getPageSize(), 0);
        page.setRecords(Collections.emptyList());
        return page;
    }

    private void validateTags(List<Long> tagIds) {
        if (CollectionUtil.isEmpty(tagIds)) {
            return;
        }
        List<Long> distinctIds = tagIds.stream().filter(Objects::nonNull).distinct().toList();
        List<AlbumTag> tags = tagMapper.selectByIds(distinctIds);
        if (tags.size() != distinctIds.size()) {
            throw new BusinessException("部分相册标签不存在");
        }
    }

    private Long resolveMediaGroupId(AlbumMediaTypeEnum mediaType) {
        if (mediaType == null) {
            throw new BusinessException("资源类型不能为空");
        }
        AlbumGroup group = groupMapper.selectOne(new LambdaQueryWrapper<AlbumGroup>()
                .eq(AlbumGroup::getName, mediaType.getLabel())
                .last("LIMIT 1"));
        if (group == null) {
            throw new BusinessException("缺少“" + mediaType.getLabel() + "”系统分组，请先执行相册分组初始化SQL");
        }
        return group.getId();
    }

    private void replaceRelations(List<Long> assetIds, List<Long> tagIds) {
        assetTagMapper.delete(new LambdaQueryWrapper<AlbumAssetTag>().in(AlbumAssetTag::getAssetId, assetIds));
        if (CollectionUtil.isEmpty(tagIds)) {
            return;
        }
        List<Long> distinctTagIds = tagIds.stream().filter(Objects::nonNull).distinct().toList();
        List<AlbumAssetTag> relations = new ArrayList<>(assetIds.size() * distinctTagIds.size());
        for (Long assetId : assetIds) {
            for (Long tagId : distinctTagIds) {
                relations.add(new AlbumAssetTag(assetId, tagId));
            }
        }
        // 资源与标签是多对多关系，统一组装后执行单条批量 SQL
        assetTagMapper.insertBatch(relations);
    }

    private Long requireCurrentUserId() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            throw new BusinessException("请先登录后再操作相册");
        }
        return userId;
    }
    private void validateOwner(Long ownerId) {
        if (ownerId == null || appUserService.getById(ownerId) == null) {
            throw new BusinessException("所属用户不存在");
        }
    }

    private void validateGroupName(String name, Long excludeId) {
        long count = groupMapper.selectCount(new LambdaQueryWrapper<AlbumGroup>()
                .eq(AlbumGroup::getName, name)
                .ne(excludeId != null, AlbumGroup::getId, excludeId));
        if (count > 0) {
            throw new BusinessException("已存在同名分组");
        }
    }

    private void validateTagName(String name, Long excludeId) {
        long count = tagMapper.selectCount(new LambdaQueryWrapper<AlbumTag>()
                .eq(AlbumTag::getName, name)
                .ne(excludeId != null, AlbumTag::getId, excludeId));
        if (count > 0) {
            throw new BusinessException("已存在同名标签");
        }
    }

    private AlbumAsset requireAsset(Long id) {
        AlbumAsset asset = assetMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException("相册资源不存在");
        }
        return asset;
    }

    private List<AlbumAsset> requireAssets(List<Long> ids) {
        List<AlbumAsset> assets = assetMapper.selectByIds(ids);
        if (assets.size() != ids.size()) {
            throw new BusinessException("部分相册资源不存在");
        }
        return assets;
    }

    private AlbumGroup requireGroup(Long id) {
        AlbumGroup group = groupMapper.selectById(id);
        if (group == null) {
            throw new BusinessException("相册分组不存在");
        }
        return group;
    }

    private AlbumTag requireTag(Long id) {
        AlbumTag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException("相册标签不存在");
        }
        return tag;
    }

    private List<Long> parseIds(String ids) {
        if (StrUtil.isBlank(ids)) {
            throw new BusinessException("ID集合不能为空");
        }
        try {
            List<Long> parsedIds = Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .filter(StrUtil::isNotBlank)
                    .map(Long::valueOf)
                    .distinct()
                    .toList();
            if (parsedIds.isEmpty()) {
                throw new BusinessException("ID集合不能为空");
            }
            return parsedIds;
        } catch (NumberFormatException e) {
            throw new BusinessException("ID格式不正确");
        }
    }

    private Map<Long, String> getUserNames(Set<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return appUserService.listByIds(userIds).stream().collect(Collectors.toMap(
                AppUser::getId,
                user -> StrUtil.blankToDefault(user.getNickname(), user.getUsername())
        ));
    }

    private AlbumAssetSaveRequest toAssetForm(AlbumAsset asset) {
        AlbumAssetSaveRequest request = new AlbumAssetSaveRequest();
        request.setId(asset.getId());
        request.setUploaderId(asset.getUploaderId());
        request.setFamilyId(asset.getFamilyId());
        request.setAlbumId(asset.getAlbumId());
        request.setMediaType(asset.getMediaType());
        request.setUrl(asset.getUrl());
        request.setPreviewUrl(fileService.getAccessUrl(asset.getUrl()));
        request.setThumbnailUrl(asset.getThumbnailUrl());
        request.setThumbnailPreviewUrl(resolveThumbnailAccessUrl(asset));
        request.setOriginalName(asset.getOriginalName());
        request.setMimeType(asset.getMimeType());
        request.setFileSize(asset.getFileSize());
        request.setDuration(asset.getDuration());
        request.setWidth(asset.getWidth());
        request.setHeight(asset.getHeight());
        request.setGroupId(asset.getGroupId());
        request.setDescription(asset.getDescription());
        request.setCapturedAt(asset.getCapturedAt());
        request.setStatus(asset.getStatus());
        return request;
    }

    private AlbumAssetVO toAssetVO(AlbumAsset asset, String groupName, List<AlbumTagVO> tags, String uploaderName) {
        AlbumAssetVO vo = new AlbumAssetVO();
        vo.setId(asset.getId());
        vo.setUploaderId(asset.getUploaderId());
        vo.setUploaderName(uploaderName);
        vo.setFamilyId(asset.getFamilyId());
        vo.setAlbumId(asset.getAlbumId());
        vo.setMediaType(asset.getMediaType());
        vo.setMediaTypeLabel(asset.getMediaType() == null ? null : asset.getMediaType().getLabel());
        vo.setUrl(asset.getUrl());
        vo.setPreviewUrl(fileService.getAccessUrl(asset.getUrl()));
        vo.setThumbnailUrl(asset.getThumbnailUrl());
        vo.setThumbnailPreviewUrl(resolveThumbnailAccessUrl(asset));
        vo.setOriginalName(asset.getOriginalName());
        vo.setMimeType(asset.getMimeType());
        vo.setFileSize(asset.getFileSize());
        vo.setDuration(asset.getDuration());
        vo.setWidth(asset.getWidth());
        vo.setHeight(asset.getHeight());
        vo.setGroupId(asset.getGroupId());
        vo.setGroupName(groupName);
        vo.setTags(tags);
        vo.setDescription(asset.getDescription());
        vo.setCapturedAt(asset.getCapturedAt());
        vo.setStatus(asset.getStatus());
        vo.setCreateTime(asset.getCreateTime());
        vo.setUpdateTime(asset.getUpdateTime());
        return vo;
    }

    private String resolveThumbnailAccessUrl(AlbumAsset asset) {
        if (StrUtil.isBlank(asset.getThumbnailUrl())) {
            return null;
        }
        return fileService.getAccessUrl(asset.getThumbnailUrl());
    }

    private AlbumGroupVO toGroupVO(AlbumGroup group) {
        AlbumGroupVO vo = new AlbumGroupVO();
        vo.setId(group.getId());
        vo.setName(group.getName());
        vo.setDescription(group.getDescription());
        vo.setSort(group.getSort());
        vo.setCreateTime(group.getCreateTime());
        return vo;
    }

    private AlbumTagVO toTagVO(AlbumTag tag) {
        AlbumTagVO vo = new AlbumTagVO();
        vo.setId(tag.getId());
        vo.setName(tag.getName());
        vo.setColor(tag.getColor());
        vo.setSort(tag.getSort());
        vo.setCreateTime(tag.getCreateTime());
        return vo;
    }
}
