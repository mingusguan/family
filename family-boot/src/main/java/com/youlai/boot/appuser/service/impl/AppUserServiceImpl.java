package com.youlai.boot.appuser.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.appuser.mapper.AppUserMapper;
import com.youlai.boot.appuser.model.AppUserModels.AppUserInfoVO;
import com.youlai.boot.appuser.model.AppUserModels.AppUserRegisterRequest;
import com.youlai.boot.appuser.model.AppUserModels.AppUserProfileUpdateRequest;
import com.youlai.boot.appuser.model.AppUserModels.AppUserProfileVO;
import com.youlai.boot.appuser.model.entity.AppUser;
import com.youlai.boot.appuser.service.AppUserService;
import com.youlai.boot.common.exception.BusinessException;
import com.youlai.boot.file.service.FileService;
import com.youlai.boot.framework.security.model.UserAuthInfo;
import com.youlai.boot.framework.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;

/** APP 用户领域服务实现。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppUserServiceImpl extends ServiceImpl<AppUserMapper, AppUser> implements AppUserService {

    private static final String APP_ROLE_CODE = "GUEST";

    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppUser register(AppUserRegisterRequest request) {
        String username = request.getUsername().trim();
        long usernameCount = count(new LambdaQueryWrapper<AppUser>().eq(AppUser::getUsername, username));
        if (usernameCount > 0) {
            throw new BusinessException("用户名已被使用");
        }

        AppUser user = AppUser.register(request, passwordEncoder.encode(request.getPassword()));
        save(user);
        log.info("APP 用户注册成功，username={}, userId={}", user.getUsername(), user.getId());
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppUser findOrCreateByMobile(String mobile) {
        AppUser user = getOne(new LambdaQueryWrapper<AppUser>().eq(AppUser::getMobile, mobile));
        if (user != null) {
            return user;
        }

        AppUser newUser = AppUser.createByMobile(mobile);
        save(newUser);
        log.info("微信小程序登录：创建 APP 用户，mobile={}, userId={}", mobile, newUser.getId());
        return newUser;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppUser createWechatUser() {
        AppUser user = AppUser.createByWechat();
        save(user);
        log.info("微信小程序登录：创建 APP 用户，userId={}", user.getId());
        return user;
    }

    @Override
    public UserAuthInfo getAuthInfoByUsername(String username) {
        AppUser user = getOne(new LambdaQueryWrapper<AppUser>().eq(AppUser::getUsername, username.trim()));
        return toAuthInfo(user);
    }

    @Override
    public UserAuthInfo getAuthInfoByMobile(String mobile) {
        AppUser user = getOne(new LambdaQueryWrapper<AppUser>().eq(AppUser::getMobile, mobile));
        return toAuthInfo(user);
    }

    @Override
    public UserAuthInfo getAuthInfoById(Long userId) {
        return toAuthInfo(getById(userId));
    }

    @Override
    public AppUserInfoVO getCurrentUserInfo() {
        return toUserInfoVO(requireCurrentUser());
    }

    @Override
    public AppUserProfileVO getCurrentUserProfile() {
        return toProfileVO(requireCurrentUser());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppUserProfileVO updateCurrentUserProfile(AppUserProfileUpdateRequest request) {
        AppUser user = requireCurrentUser();
        user.updateProfile(request.getNickname(), request.getAvatar());
        updateById(user);
        log.info("APP 用户修改个人资料成功，userId={}", user.getId());
        return toProfileVO(user);
    }

    private AppUser requireCurrentUser() {
        Long userId = SecurityUtils.getUserId();
        AppUser user = userId == null ? null : getById(userId);
        if (user == null) {
            throw new BusinessException("APP 用户不存在或登录已失效");
        }
        return user;
    }


    private UserAuthInfo toAuthInfo(AppUser user) {
        if (user == null) {
            return null;
        }
        UserAuthInfo authInfo = new UserAuthInfo();
        authInfo.setUserId(user.getId());
        authInfo.setUsername(user.getUsername());
        authInfo.setNickname(user.getNickname());
        authInfo.setPassword(user.getPassword());
        authInfo.setStatus(user.getStatus());
        authInfo.setRoles(Set.of(APP_ROLE_CODE));
        authInfo.setDataScopes(Collections.emptyList());
        return authInfo;
    }

    private AppUserInfoVO toUserInfoVO(AppUser user) {
        AppUserInfoVO vo = new AppUserInfoVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setMobile(user.getMobile());
        vo.setAvatar(resolveAvatarAccessUrl(user.getAvatar()));
        vo.setStatus(user.getStatus());
        return vo;
    }

    private AppUserProfileVO toProfileVO(AppUser user) {
        AppUserProfileVO vo = new AppUserProfileVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setMobile(user.getMobile());
        vo.setAvatar(resolveAvatarAccessUrl(user.getAvatar()));
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        return vo;
    }
    private String resolveAvatarAccessUrl(String avatar) {
        // 数据库只保存稳定地址，向客户端响应时为私有 COS 对象动态生成临时访问链接。
        return StrUtil.isBlank(avatar) ? avatar : fileService.getAccessUrl(avatar);
    }

}
