package com.youlai.boot.appuser.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlai.boot.album.mapper.AlbumAssetMapper;
import com.youlai.boot.album.model.entity.AlbumAsset;
import com.youlai.boot.appuser.model.AppUserModels.AppUserQuery;
import com.youlai.boot.appuser.model.AppUserModels.AppUserVO;
import com.youlai.boot.appuser.model.entity.AppUser;
import com.youlai.boot.appuser.service.AppUserManagementService;
import com.youlai.boot.appuser.service.AppUserService;
import com.youlai.boot.common.exception.BusinessException;
import com.youlai.boot.common.model.Option;
import com.youlai.boot.framework.security.token.TokenManager;
import com.youlai.boot.system.enums.SocialPlatformEnum;
import com.youlai.boot.system.model.entity.UserSocial;
import com.youlai.boot.system.service.UserSocialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * APP 用户后台管理服务实现。
 */
@Service
@RequiredArgsConstructor
public class AppUserManagementServiceImpl implements AppUserManagementService {

    private final AppUserService appUserService;
    private final UserSocialService userSocialService;
    private final AlbumAssetMapper albumAssetMapper;
    private final TokenManager tokenManager;

    @Override
    public IPage<AppUserVO> getPage(AppUserQuery query) {
        LambdaQueryWrapper<AppUser> wrapper = new LambdaQueryWrapper<AppUser>()
                .eq(query.getStatus() != null, AppUser::getStatus, query.getStatus())
                .and(StrUtil.isNotBlank(query.getKeyword()), condition -> condition
                        .like(AppUser::getUsername, query.getKeyword())
                        .or().like(AppUser::getNickname, query.getKeyword())
                        .or().like(AppUser::getMobile, query.getKeyword()))
                .orderByDesc(AppUser::getCreateTime);

        IPage<AppUser> userPage = appUserService.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        List<AppUser> users = userPage.getRecords();
        Map<Long, UserSocial> wechatBindings = getWechatBindings(
                users.stream().map(AppUser::getId).collect(Collectors.toSet())
        );

        Page<AppUserVO> result = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        result.setRecords(users.stream().map(user -> toVO(user, wechatBindings.get(user.getId()))).toList());
        return result;
    }

    @Override
    public List<Option<String>> listOptions(String keyword) {
        // 下拉选项最多返回 100 条，避免 APP 用户量较大时拉取全表。
        return appUserService.list(new LambdaQueryWrapper<AppUser>()
                        .eq(AppUser::getStatus, 1)
                        .and(StrUtil.isNotBlank(keyword), condition -> condition
                                .like(AppUser::getNickname, keyword)
                                .or().like(AppUser::getMobile, keyword)
                                .or().like(AppUser::getUsername, keyword))
                        .orderByDesc(AppUser::getCreateTime)
                        .last("LIMIT 100"))
                .stream()
                .map(user -> new Option<>(String.valueOf(user.getId()), buildOptionLabel(user)))
                .toList();
    }

    @Override
    @Transactional
    public boolean updateStatus(Long userId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("用户状态只能为0或1");
        }
        AppUser user = requireAppUser(userId);
        user.changeStatus(status);
        boolean updated = appUserService.updateById(user);
        if (updated && status == 0) {
            tokenManager.invalidateUserSessions(userId);
        }
        return updated;
    }

    @Override
    @Transactional
    public boolean deleteUsers(String ids) {
        List<Long> idList = parseIds(ids);
        if (appUserService.listByIds(idList).size() != idList.size()) {
            throw new BusinessException("部分APP用户不存在");
        }
        Long assetCount = albumAssetMapper.selectCount(new LambdaQueryWrapper<AlbumAsset>()
                .in(AlbumAsset::getUploaderId, idList));
        if (assetCount > 0) {
            throw new BusinessException("用户已上传相册资源，请先处理资源后再删除");
        }

        // 用户、角色关系和第三方绑定在同一事务内清理，避免留下孤立数据。
        userSocialService.remove(new LambdaQueryWrapper<UserSocial>().in(UserSocial::getUserId, idList));
        boolean deleted = appUserService.removeByIds(idList);
        if (deleted) {
            idList.forEach(tokenManager::invalidateUserSessions);
        }
        return deleted;
    }

    private AppUser requireAppUser(Long userId) {
        AppUser user = appUserService.getById(userId);
        if (user == null) {
            throw new BusinessException("APP用户不存在");
        }
        return user;
    }

    private Map<Long, UserSocial> getWechatBindings(Set<Long> userIds) {
        if (CollectionUtil.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        return userSocialService.list(new LambdaQueryWrapper<UserSocial>()
                        .in(UserSocial::getUserId, userIds)
                        .eq(UserSocial::getPlatform, SocialPlatformEnum.WECHAT_MINI))
                .stream()
                .collect(Collectors.toMap(UserSocial::getUserId, Function.identity(), (left, right) -> left));
    }

    private AppUserVO toVO(AppUser user, UserSocial binding) {
        AppUserVO vo = new AppUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setMobile(user.getMobile());
        vo.setAvatar(appUserService.getAvatarAccessUrl(user.getAvatar()));
        vo.setStatus(user.getStatus());
        vo.setWechatBound(binding != null);
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        return vo;
    }

    private String buildOptionLabel(AppUser user) {
        String name = StrUtil.blankToDefault(user.getNickname(), user.getUsername());
        return StrUtil.isBlank(user.getMobile()) ? name : name + "（" + user.getMobile() + "）";
    }

    private List<Long> parseIds(String ids) {
        if (StrUtil.isBlank(ids)) {
            throw new BusinessException("用户ID不能为空");
        }
        try {
            List<Long> parsed = Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .filter(StrUtil::isNotBlank)
                    .map(Long::valueOf)
                    .distinct()
                    .toList();
            if (parsed.isEmpty()) {
                throw new BusinessException("用户ID不能为空");
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new BusinessException("用户ID格式不正确");
        }
    }
}
