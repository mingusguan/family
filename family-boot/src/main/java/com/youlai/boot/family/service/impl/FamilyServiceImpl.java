package com.youlai.boot.family.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youlai.boot.album.mapper.AlbumAssetMapper;
import com.youlai.boot.album.model.entity.AlbumAsset;
import com.youlai.boot.appuser.mapper.AppUserMapper;
import com.youlai.boot.appuser.model.entity.AppUser;
import com.youlai.boot.common.exception.BusinessException;
import com.youlai.boot.family.mapper.FamilyAlbumMapper;
import com.youlai.boot.family.mapper.FamilyInviteMapper;
import com.youlai.boot.family.mapper.FamilyMapper;
import com.youlai.boot.family.mapper.FamilyMemberMapper;
import com.youlai.boot.family.model.FamilyModels.FamilyAlbumCreateRequest;
import com.youlai.boot.family.model.FamilyModels.FamilyAlbumVO;
import com.youlai.boot.family.model.FamilyModels.FamilyCreateRequest;
import com.youlai.boot.family.model.FamilyModels.FamilyDetailVO;
import com.youlai.boot.family.model.FamilyModels.FamilyInviteVO;
import com.youlai.boot.family.model.FamilyModels.FamilyMemberVO;
import com.youlai.boot.family.model.FamilyModels.FamilyVO;
import com.youlai.boot.family.model.entity.Family;
import com.youlai.boot.family.model.entity.FamilyAlbum;
import com.youlai.boot.family.model.entity.FamilyInvite;
import com.youlai.boot.family.model.entity.FamilyMember;
import com.youlai.boot.family.service.FamilyService;
import com.youlai.boot.file.service.FileService;
import com.youlai.boot.framework.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** APP 家庭与家庭相册服务实现。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FamilyServiceImpl implements FamilyService {

    private static final long INVITE_VALID_DAYS = 7;

    private final FamilyMapper familyMapper;
    private final FamilyMemberMapper memberMapper;
    private final FamilyInviteMapper inviteMapper;
    private final AppUserMapper appUserMapper;
    private final FamilyAlbumMapper albumMapper;
    private final AlbumAssetMapper assetMapper;
    private final FileService fileService;

    @Override
    public List<FamilyVO> listMyFamilies() {
        Long userId = requireCurrentUserId();
        List<FamilyMember> memberships = memberMapper.selectList(new LambdaQueryWrapper<FamilyMember>()
                .eq(FamilyMember::getUserId, userId)
                .orderByDesc(FamilyMember::getCreateTime));
        if (memberships.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> familyIds = memberships.stream().map(FamilyMember::getFamilyId).collect(Collectors.toSet());
        Map<Long, Family> families = familyMapper.selectByIds(familyIds).stream()
                .collect(Collectors.toMap(Family::getId, Function.identity()));
        Map<Long, Long> memberCounts = memberMapper.selectList(new LambdaQueryWrapper<FamilyMember>()
                        .in(FamilyMember::getFamilyId, familyIds))
                .stream().collect(Collectors.groupingBy(FamilyMember::getFamilyId, Collectors.counting()));
        Map<Long, Long> albumCounts = albumMapper.selectList(new LambdaQueryWrapper<FamilyAlbum>()
                        .in(FamilyAlbum::getFamilyId, familyIds))
                .stream().collect(Collectors.groupingBy(FamilyAlbum::getFamilyId, Collectors.counting()));

        // 按用户加入家庭的顺序组装，不使用多表 Join，确保成员角色能够准确对应。
        return memberships.stream()
                .map(member -> {
                    Family family = families.get(member.getFamilyId());
                    return family == null ? null : toFamilyVO(
                            family,
                            member.getRole(),
                            memberCounts.getOrDefault(family.getId(), 0L),
                            albumCounts.getOrDefault(family.getId(), 0L)
                    );
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FamilyVO createFamily(FamilyCreateRequest request) {
        Long userId = requireCurrentUserId();
        Family family = Family.create(userId, request);
        familyMapper.insert(family);
        memberMapper.insert(FamilyMember.owner(family.getId(), userId));
        log.info("APP 用户创建家庭，userId={}, familyId={}", userId, family.getId());
        return toFamilyVO(family, FamilyMember.ROLE_OWNER, 1L, 0L);
    }

    @Override
    public FamilyDetailVO getFamilyDetail(Long familyId) {
        Long userId = requireCurrentUserId();
        Family family = requireFamily(familyId);
        FamilyMember currentMember = requireMembership(familyId, userId);
        List<FamilyMember> members = memberMapper.selectList(new LambdaQueryWrapper<FamilyMember>()
                .eq(FamilyMember::getFamilyId, familyId)
                .orderByAsc(FamilyMember::getCreateTime));
        Set<Long> userIds = members.stream().map(FamilyMember::getUserId).collect(Collectors.toSet());
        Map<Long, AppUser> users = appUserMapper.selectByIds(userIds).stream()
                .collect(Collectors.toMap(AppUser::getId, Function.identity()));
        long albumCount = albumMapper.selectCount(new LambdaQueryWrapper<FamilyAlbum>()
                .eq(FamilyAlbum::getFamilyId, familyId));

        // 成员关系和 APP 用户分表查询后组装，避免跨用户域使用多表 Join。
        List<FamilyMemberVO> memberViews = members.stream()
                .map(member -> toFamilyMemberVO(member, users.get(member.getUserId())))
                .toList();
        return toFamilyDetailVO(family, currentMember.getRole(), (long) members.size(), albumCount, memberViews);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FamilyInviteVO createInvite(Long familyId) {
        Long userId = requireCurrentUserId();
        Family family = requireFamily(familyId);
        requireMembership(familyId, userId);
        LocalDateTime now = LocalDateTime.now();
        FamilyInvite invite = inviteMapper.selectOne(new LambdaQueryWrapper<FamilyInvite>()
                .eq(FamilyInvite::getFamilyId, familyId)
                .eq(FamilyInvite::getInviterId, userId)
                .gt(FamilyInvite::getExpiresAt, now)
                .orderByDesc(FamilyInvite::getCreateTime)
                .last("LIMIT 1"));
        if (invite == null) {
            invite = FamilyInvite.create(familyId, userId, now.plusDays(INVITE_VALID_DAYS));
            inviteMapper.insert(invite);
            log.info("APP 用户创建家庭邀请，userId={}, familyId={}, inviteId={}", userId, familyId, invite.getId());
        }
        return toFamilyInviteVO(invite, family, appUserMapper.selectById(userId), true);
    }

    @Override
    public FamilyInviteVO getInvite(String code) {
        Long userId = requireCurrentUserId();
        FamilyInvite invite = requireValidInvite(code);
        Family family = requireFamily(invite.getFamilyId());
        boolean alreadyMember = memberMapper.selectCount(new LambdaQueryWrapper<FamilyMember>()
                .eq(FamilyMember::getFamilyId, family.getId())
                .eq(FamilyMember::getUserId, userId)) > 0;
        AppUser inviter = appUserMapper.selectById(invite.getInviterId());
        return toFamilyInviteVO(invite, family, inviter, alreadyMember);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FamilyVO acceptInvite(String code) {
        Long userId = requireCurrentUserId();
        FamilyInvite invite = requireValidInvite(code);
        Family family = requireFamily(invite.getFamilyId());
        FamilyMember membership = memberMapper.selectOne(new LambdaQueryWrapper<FamilyMember>()
                .eq(FamilyMember::getFamilyId, family.getId())
                .eq(FamilyMember::getUserId, userId)
                .last("LIMIT 1"));
        if (membership == null) {
            membership = FamilyMember.member(family.getId(), userId);
            try {
                memberMapper.insert(membership);
                log.info("APP 用户接受家庭邀请，userId={}, familyId={}, inviteId={}", userId, family.getId(), invite.getId());
            } catch (DuplicateKeyException ignored) {
                // 唯一索引负责兜底并发确认，重复请求按已加入处理，保证邀请接受幂等。
                membership = requireMembership(family.getId(), userId);
            }
        }
        long memberCount = memberMapper.selectCount(new LambdaQueryWrapper<FamilyMember>()
                .eq(FamilyMember::getFamilyId, family.getId()));
        long albumCount = albumMapper.selectCount(new LambdaQueryWrapper<FamilyAlbum>()
                .eq(FamilyAlbum::getFamilyId, family.getId()));
        return toFamilyVO(family, membership.getRole(), memberCount, albumCount);
    }

    @Override
    public List<FamilyAlbumVO> listAlbums(Long familyId) {
        ensureCurrentUserMember(familyId);
        List<FamilyAlbum> albums = albumMapper.selectList(new LambdaQueryWrapper<FamilyAlbum>()
                .eq(FamilyAlbum::getFamilyId, familyId)
                .orderByDesc(FamilyAlbum::getCreateTime));
        if (albums.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> albumIds = albums.stream().map(FamilyAlbum::getId).collect(Collectors.toSet());
        List<AlbumAsset> assets = assetMapper.selectList(new LambdaQueryWrapper<AlbumAsset>()
                .in(AlbumAsset::getAlbumId, albumIds)
                .eq(AlbumAsset::getStatus, 1)
                .select(AlbumAsset::getId, AlbumAsset::getAlbumId, AlbumAsset::getUrl,
                        AlbumAsset::getThumbnailUrl, AlbumAsset::getCreateTime)
                .orderByDesc(AlbumAsset::getCreateTime));
        Map<Long, Long> counts = assets.stream()
                .collect(Collectors.groupingBy(AlbumAsset::getAlbumId, Collectors.counting()));
        Map<Long, AlbumAsset> covers = assets.stream().collect(Collectors.toMap(
                AlbumAsset::getAlbumId,
                Function.identity(),
                (first, ignored) -> first,
                LinkedHashMap::new
        ));

        // 相册封面优先使用手动封面，否则使用相册内最新资源的缩略图或原图。
        return albums.stream().map(album -> {
            AlbumAsset coverAsset = covers.get(album.getId());
            String coverUrl = StrUtil.isNotBlank(album.getCoverUrl())
                    ? fileService.getAccessUrl(album.getCoverUrl())
                    : resolveAssetCover(coverAsset);
            return toAlbumVO(album, counts.getOrDefault(album.getId(), 0L), coverUrl);
        }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FamilyAlbumVO createAlbum(Long familyId, FamilyAlbumCreateRequest request) {
        Long userId = requireCurrentUserId();
        ensureCurrentUserMember(familyId);
        long duplicateCount = albumMapper.selectCount(new LambdaQueryWrapper<FamilyAlbum>()
                .eq(FamilyAlbum::getFamilyId, familyId)
                .eq(FamilyAlbum::getName, request.getName().trim()));
        if (duplicateCount > 0) {
            throw new BusinessException("当前家庭中已存在同名相册");
        }
        FamilyAlbum album = FamilyAlbum.create(familyId, userId, request);
        albumMapper.insert(album);
        log.info("APP 用户创建家庭相册，userId={}, familyId={}, albumId={}", userId, familyId, album.getId());
        return toAlbumVO(album, 0L, null);
    }

    @Override
    public void ensureCurrentUserMember(Long familyId) {
        requireFamily(familyId);
        requireMembership(familyId, requireCurrentUserId());
    }

    @Override
    public void ensureAlbumBelongsToFamily(Long albumId, Long familyId) {
        FamilyAlbum album = albumId == null ? null : albumMapper.selectById(albumId);
        if (album == null || !Objects.equals(album.getFamilyId(), familyId)) {
            throw new BusinessException("相册不存在或不属于所选家庭");
        }
    }

    private Family requireFamily(Long familyId) {
        Family family = familyId == null ? null : familyMapper.selectById(familyId);
        if (family == null) {
            throw new BusinessException("家庭不存在");
        }
        return family;
    }

    private FamilyMember requireMembership(Long familyId, Long userId) {
        FamilyMember membership = memberMapper.selectOne(new LambdaQueryWrapper<FamilyMember>()
                .eq(FamilyMember::getFamilyId, familyId)
                .eq(FamilyMember::getUserId, userId)
                .last("LIMIT 1"));
        if (membership == null) {
            throw new BusinessException("你不是该家庭成员");
        }
        return membership;
    }

    private FamilyInvite requireValidInvite(String code) {
        FamilyInvite invite = StrUtil.isBlank(code) ? null : inviteMapper.selectOne(
                new LambdaQueryWrapper<FamilyInvite>()
                        .eq(FamilyInvite::getCode, code.trim())
                        .last("LIMIT 1")
        );
        if (invite == null) {
            throw new BusinessException("家庭邀请不存在");
        }
        if (!invite.isValidAt(LocalDateTime.now())) {
            throw new BusinessException("家庭邀请已过期，请联系家人重新分享");
        }
        return invite;
    }

    private Long requireCurrentUserId() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        return userId;
    }

    private String resolveAssetCover(AlbumAsset asset) {
        if (asset == null) {
            return null;
        }
        return fileService.getAccessUrl(StrUtil.blankToDefault(asset.getThumbnailUrl(), asset.getUrl()));
    }

    private FamilyDetailVO toFamilyDetailVO(
            Family family,
            String role,
            Long memberCount,
            Long albumCount,
            List<FamilyMemberVO> members
    ) {
        FamilyDetailVO vo = new FamilyDetailVO();
        applyFamilyView(vo, family, role, memberCount, albumCount);
        vo.setMembers(members);
        return vo;
    }

    private FamilyMemberVO toFamilyMemberVO(FamilyMember member, AppUser user) {
        FamilyMemberVO vo = new FamilyMemberVO();
        vo.setUserId(member.getUserId());
        vo.setNickname(user == null
                ? "家庭成员" : StrUtil.blankToDefault(user.getNickname(), user.getUsername()));
        vo.setAvatar(user == null || StrUtil.isBlank(user.getAvatar())
                ? null : fileService.getAccessUrl(user.getAvatar()));
        vo.setRole(member.getRole());
        vo.setJoinedAt(member.getCreateTime());
        return vo;
    }

    private FamilyInviteVO toFamilyInviteVO(
            FamilyInvite invite,
            Family family,
            AppUser inviter,
            boolean alreadyMember
    ) {
        FamilyInviteVO vo = new FamilyInviteVO();
        vo.setCode(invite.getCode());
        vo.setFamilyId(family.getId());
        vo.setFamilyName(family.getName());
        vo.setInviterName(inviter == null
                ? "家庭成员"
                : StrUtil.blankToDefault(inviter.getNickname(), inviter.getUsername()));
        vo.setExpiresAt(invite.getExpiresAt());
        vo.setAlreadyMember(alreadyMember);
        return vo;
    }

    private FamilyVO toFamilyVO(Family family, String role, Long memberCount, Long albumCount) {
        FamilyVO vo = new FamilyVO();
        applyFamilyView(vo, family, role, memberCount, albumCount);
        return vo;
    }

    private void applyFamilyView(
            FamilyVO vo,
            Family family,
            String role,
            Long memberCount,
            Long albumCount
    ) {
        vo.setId(family.getId());
        vo.setName(family.getName());
        vo.setDescription(family.getDescription());
        vo.setCoverUrl(StrUtil.isBlank(family.getCoverUrl()) ? null : fileService.getAccessUrl(family.getCoverUrl()));
        vo.setCreatorId(family.getCreatorId());
        vo.setRole(role);
        vo.setMemberCount(memberCount);
        vo.setAlbumCount(albumCount);
        vo.setCreateTime(family.getCreateTime());
    }

    private FamilyAlbumVO toAlbumVO(FamilyAlbum album, Long assetCount, String coverUrl) {
        FamilyAlbumVO vo = new FamilyAlbumVO();
        vo.setId(album.getId());
        vo.setFamilyId(album.getFamilyId());
        vo.setName(album.getName());
        vo.setDescription(album.getDescription());
        vo.setCoverUrl(coverUrl);
        vo.setCreatorId(album.getCreatorId());
        vo.setAssetCount(assetCount);
        vo.setCreateTime(album.getCreateTime());
        return vo;
    }
}
