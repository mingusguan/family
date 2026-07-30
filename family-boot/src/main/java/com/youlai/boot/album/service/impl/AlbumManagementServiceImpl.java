package com.youlai.boot.album.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.youlai.boot.album.enums.AlbumMediaTypeEnum;
import com.youlai.boot.album.mapper.AlbumAssetMapper;
import com.youlai.boot.album.mapper.AlbumGroupMapper;
import com.youlai.boot.album.model.AlbumModels.AlbumAssetQuery;
import com.youlai.boot.album.model.AlbumModels.AlbumAssetSaveRequest;
import com.youlai.boot.album.model.AlbumModels.AlbumAssetVO;
import com.youlai.boot.album.model.AlbumModels.AlbumMomentBatchRow;
import com.youlai.boot.album.model.AlbumModels.AlbumMomentBatchVO;
import com.youlai.boot.album.model.AlbumModels.AlbumMomentCoverVO;
import com.youlai.boot.album.model.AlbumModels.AlbumMomentDetailVO;
import com.youlai.boot.album.model.AlbumModels.AlbumMomentCreateRequest;
import com.youlai.boot.album.model.AlbumModels.AlbumMomentQuery;
import com.youlai.boot.album.model.AlbumModels.AlbumGroupSaveRequest;
import com.youlai.boot.album.model.AlbumModels.AlbumGroupVO;
import com.youlai.boot.album.model.entity.AlbumAsset;
import com.youlai.boot.album.model.entity.AlbumGroup;
import com.youlai.boot.album.service.AlbumManagementService;
import com.youlai.boot.appuser.model.entity.AppUser;
import com.youlai.boot.appuser.service.AppUserService;
import com.youlai.boot.common.exception.BusinessException;
import com.youlai.boot.family.service.FamilyService;
import com.youlai.boot.file.service.FileService;
import com.youlai.boot.framework.security.util.SecurityUtils;
import com.youlai.boot.framework.integration.wxma.service.WxContentSecurityService;
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
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 相册后台管理服务实现。
 */
@Service
@RequiredArgsConstructor
public class AlbumManagementServiceImpl implements AlbumManagementService {

    private final AlbumAssetMapper assetMapper;
    private final AlbumGroupMapper groupMapper;
    private final AppUserService appUserService;
    private final FileService fileService;
    private final FamilyService familyService;
    private final WxContentSecurityService wxContentSecurityService;

    @Override
    public IPage<AlbumAssetVO> getAssetPage(AlbumAssetQuery query) {

        String contentKeyword = StrUtil.trim(query.getContentKeyword());
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
                .and(StrUtil.isNotBlank(query.getKeyword()), condition -> condition
                        .like(AlbumAsset::getOriginalName, query.getKeyword())
                        .or()
                        .like(AlbumAsset::getDescription, query.getKeyword()))
                .like(StrUtil.isNotBlank(contentKeyword), AlbumAsset::getDescription, contentKeyword)
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
        return getAssetPage(assetQuery);
    }

    @Override
    public IPage<AlbumMomentBatchVO> getMomentBatchPage(AlbumMomentQuery query) {
        familyService.ensureCurrentUserMember(query.getFamilyId());
        familyService.ensureAlbumBelongsToFamily(query.getAlbumId(), query.getFamilyId());
        validateMomentQuery(query);

        String keyword = normalizeContentKeyword(query.getKeyword());
        query.setKeyword(keyword);
        Long currentUserId = Boolean.TRUE.equals(query.getMine()) ? requireCurrentUserId() : null;
        Page<AlbumMomentBatchRow> batchPage = assetMapper.selectMomentBatchPage(
                new Page<>(query.getPageNum(), query.getPageSize()),
                query,
                currentUserId
        );
        Page<AlbumMomentBatchVO> result = new Page<>(
                batchPage.getCurrent(),
                batchPage.getSize(),
                batchPage.getTotal()
        );
        if (batchPage.getRecords().isEmpty()) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        List<String> batchIds = batchPage.getRecords().stream()
                .map(AlbumMomentBatchRow::getBatchId)
                .toList();
        Map<String, List<AlbumAssetVO>> assetsByBatch = assembleAssetVOs(
                listAssetsByBatchIds(batchIds, query.getFamilyId(), query.getAlbumId())
        ).stream().collect(Collectors.groupingBy(
                this::resolveBatchId,
                LinkedHashMap::new,
                Collectors.toList()
        ));
        Map<Long, String> uploaderNames = getUserNames(batchPage.getRecords().stream()
                .map(AlbumMomentBatchRow::getUploaderId)
                .collect(Collectors.toSet()));
        result.setRecords(batchPage.getRecords().stream()
                .map(row -> toMomentBatchVO(
                        row,
                        assetsByBatch.getOrDefault(row.getBatchId(), Collections.emptyList()),
                        uploaderNames.get(row.getUploaderId())
                ))
                .toList());
        // 客户端按上传批次分页，后台隐藏的资源不会进入批次封面或详情。
        return result;
    }

    @Override
    public AlbumMomentDetailVO getMomentDetail(String batchId, Long familyId, Long albumId) {
        familyService.ensureCurrentUserMember(familyId);
        familyService.ensureAlbumBelongsToFamily(albumId, familyId);
        List<AlbumAssetVO> assets = assembleAssetVOs(listAssetsByBatchIds(List.of(batchId), familyId, albumId));
        if (assets.isEmpty()) {
            throw new BusinessException("相册批次不存在");
        }
        AlbumAssetVO first = assets.get(0);
        AlbumMomentDetailVO detail = new AlbumMomentDetailVO();
        detail.setBatchId(batchId);
        detail.setUploaderId(first.getUploaderId());
        detail.setUploaderName(first.getUploaderName());
        detail.setFamilyId(familyId);
        detail.setAlbumId(albumId);
        detail.setDescription(first.getDescription());
        detail.setCapturedAt(first.getCapturedAt());
        detail.setCreateTime(first.getCreateTime());
        detail.setAssets(assets);
        return detail;
    }

    private void validateMomentQuery(AlbumMomentQuery query) {
        AlbumAssetQuery assetQuery = new AlbumAssetQuery();
        assetQuery.setStartDate(query.getStartDate());
        assetQuery.setEndDate(query.getEndDate());
        validateDateRange(assetQuery);
        parseMonthFilters(query.getMonths());
    }

    private List<AlbumAsset> listAssetsByBatchIds(List<String> batchIds, Long familyId, Long albumId) {
        if (CollectionUtil.isEmpty(batchIds)) {
            return Collections.emptyList();
        }
        List<String> actualBatchIds = batchIds.stream()
                .filter(batchId -> batchId != null && !batchId.startsWith("legacy-"))
                .peek(this::validateBatchId)
                .toList();
        List<Long> legacyAssetIds = batchIds.stream()
                .filter(batchId -> batchId != null && batchId.startsWith("legacy-"))
                .map(this::parseLegacyAssetId)
                .toList();

        LambdaQueryWrapper<AlbumAsset> wrapper = new LambdaQueryWrapper<AlbumAsset>()
                .eq(AlbumAsset::getFamilyId, familyId)
                .eq(AlbumAsset::getAlbumId, albumId)
                .eq(AlbumAsset::getStatus, 1)
                .and(condition -> {
                    if (CollectionUtil.isNotEmpty(actualBatchIds)) {
                        condition.in(AlbumAsset::getUploadBatchId, actualBatchIds);
                    }
                    if (CollectionUtil.isNotEmpty(legacyAssetIds)) {
                        condition.or(CollectionUtil.isNotEmpty(actualBatchIds))
                                .in(AlbumAsset::getId, legacyAssetIds);
                    }
                })
                .orderByAsc(AlbumAsset::getId);
        return assetMapper.selectList(wrapper);
    }

    private void validateBatchId(String batchId) {
        if (!batchId.matches("[a-fA-F0-9]{32,64}")) {
            throw new BusinessException("相册批次格式不正确");
        }
    }

    private Long parseLegacyAssetId(String batchId) {
        try {
            return Long.valueOf(batchId.substring("legacy-".length()));
        } catch (RuntimeException exception) {
            throw new BusinessException("相册批次格式不正确");
        }
    }

    private List<AlbumAssetVO> assembleAssetVOs(List<AlbumAsset> assets) {
        if (assets.isEmpty()) {
            return Collections.emptyList();
        }
        Page<AlbumAsset> assetPage = new Page<>(1, assets.size(), assets.size());
        assetPage.setRecords(assets);
        return assembleAssetPage(assetPage).getRecords();
    }

    private String resolveBatchId(AlbumAssetVO asset) {
        return StrUtil.isBlank(asset.getUploadBatchId())
                ? "legacy-" + asset.getId()
                : asset.getUploadBatchId();
    }

    private AlbumMomentBatchVO toMomentBatchVO(
            AlbumMomentBatchRow row,
            List<AlbumAssetVO> assets,
            String uploaderName
    ) {
        AlbumMomentBatchVO vo = new AlbumMomentBatchVO();
        vo.setBatchId(row.getBatchId());
        vo.setUploaderId(row.getUploaderId());
        vo.setUploaderName(uploaderName);
        vo.setFamilyId(row.getFamilyId());
        vo.setAlbumId(row.getAlbumId());
        vo.setDescription(row.getDescription());
        vo.setCapturedAt(row.getCapturedAt());
        vo.setCreateTime(row.getCreateTime());
        vo.setAssetCount(row.getAssetCount());
        // 列表最多返回四张轻量封面，完整资源只在详情页加载。
        vo.setCovers(assets.stream().limit(4).map(asset -> {
            AlbumMomentCoverVO cover = new AlbumMomentCoverVO();
            cover.setMediaType(asset.getMediaType());
            cover.setPreviewUrl(StrUtil.blankToDefault(
                    asset.getThumbnailPreviewUrl(),
                    asset.getPreviewUrl()
            ));
            return cover;
        }).toList());
        return vo;
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
        return StrUtil.isBlank(normalized) ? null : normalized;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveMoment(AlbumMomentCreateRequest request) {
        Long uploaderId = requireCurrentUserId();
        validateOwner(uploaderId);
        familyService.ensureCurrentUserMember(request.getFamilyId());
        familyService.ensureAlbumBelongsToFamily(request.getAlbumId(), request.getFamilyId());
        wxContentSecurityService.checkText(uploaderId, collectPublishedText(request));
        // 同一批次资源共用家庭、相册和描述，统一组装后使用 MyBatis-Plus 批量写入。
        String uploadBatchId = UUID.randomUUID().toString().replace("-", "");
        List<AlbumAsset> assets = request.getResources().stream().map(resource -> {
            AlbumAsset asset = AlbumAsset.createMoment(uploaderId, request, resource);
            asset.assignUploadBatch(uploadBatchId);
            asset.changeGroup(resolveMediaGroupId(resource.getMediaType()));
            return asset;
        }).toList();
        return Db.saveBatch(assets);
    }

    @Override
    @Transactional
    public boolean deleteOwnMoment(Long id) {
        Long currentUserId = requireCurrentUserId();
        AlbumAsset asset = requireAsset(id);
        if (!Objects.equals(asset.getUploaderId(), currentUserId)) {
            throw new BusinessException("只能删除自己发布的精彩时刻");
        }
        // 客户端删除只移除相册元数据，对象存储文件保留，避免误删其他业务引用。
        return assetMapper.deleteById(id) > 0;
    }
    @Override
    public AlbumAssetSaveRequest getAssetForm(Long id) {
        AlbumAsset asset = requireAsset(id);
        AlbumAssetSaveRequest request = toAssetForm(asset);
        return request;
    }

    @Override
    @Transactional
    public boolean saveAsset(AlbumAssetSaveRequest request) {
        validateOwner(request.getUploaderId());
        AlbumAsset asset = AlbumAsset.create(request);
        // 资源分组由媒体类型统一决定，避免不同入口提交不一致的分组。
        asset.changeGroup(resolveMediaGroupId(request.getMediaType()));
        return assetMapper.insert(asset) > 0;
    }

    @Override
    @Transactional
    public boolean updateAsset(Long id, AlbumAssetSaveRequest request) {
        AlbumAsset asset = requireAsset(id);
        validateOwner(request.getUploaderId());
        asset.updateMetadata(request);
        // 更换资源文件后同步刷新系统分组。
        asset.changeGroup(resolveMediaGroupId(request.getMediaType()));
        return assetMapper.updateById(asset) > 0;
    }

    @Override
    @Transactional
    public boolean deleteAssets(String ids) {
        List<Long> idList = parseIds(ids);
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

    private IPage<AlbumAssetVO> assembleAssetPage(Page<AlbumAsset> entityPage) {
        Page<AlbumAssetVO> result = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<AlbumAsset> assets = entityPage.getRecords();
        if (assets.isEmpty()) {
            result.setRecords(Collections.emptyList());
            return result;
        }
        Set<Long> groupIds = assets.stream().map(AlbumAsset::getGroupId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> groupNames = groupIds.isEmpty()
                ? Collections.emptyMap()
                : groupMapper.selectByIds(groupIds).stream().collect(Collectors.toMap(AlbumGroup::getId, AlbumGroup::getName));
        Map<Long, String> uploaderNames = getUserNames(assets.stream().map(AlbumAsset::getUploaderId).collect(Collectors.toSet()));

        // 分页主表查询完成后，批量组装分组和上传用户，避免多表 Join 与 N+1 查询
        result.setRecords(assets.stream().map(asset -> toAssetVO(
                asset,
                groupNames.get(asset.getGroupId()),
                uploaderNames.get(asset.getUploaderId())
        )).toList());
        return result;
    }

    private List<String> collectPublishedText(AlbumMomentCreateRequest request) {
        List<String> contents = new ArrayList<>();
        if (StrUtil.isNotBlank(request.getDescription())) {
            contents.add(request.getDescription());
        }
        return contents;
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

    private AlbumAsset requireAsset(Long id) {
        AlbumAsset asset = assetMapper.selectById(id);
        if (asset == null) {
            throw new BusinessException("相册资源不存在");
        }
        return asset;
    }

    private AlbumGroup requireGroup(Long id) {
        AlbumGroup group = groupMapper.selectById(id);
        if (group == null) {
            throw new BusinessException("相册分组不存在");
        }
        return group;
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

    private AlbumAssetVO toAssetVO(AlbumAsset asset, String groupName, String uploaderName) {
        AlbumAssetVO vo = new AlbumAssetVO();
        vo.setId(asset.getId());
        vo.setUploadBatchId(asset.getUploadBatchId());
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
}
