package com.youlai.boot.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.youlai.boot.appuser.model.entity.AppUser;
import com.youlai.boot.appuser.service.AppUserService;
import com.youlai.boot.framework.security.model.UserAuthInfo;
import com.youlai.boot.system.enums.SocialPlatformEnum;
import com.youlai.boot.system.mapper.UserSocialMapper;
import com.youlai.boot.system.model.dto.UserSocialBindDTO;
import com.youlai.boot.system.model.entity.UserSocial;
import com.youlai.boot.system.service.UserSocialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户第三方账号绑定业务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserSocialServiceImpl extends ServiceImpl<UserSocialMapper, UserSocial> implements UserSocialService {

    private final AppUserService appUserService;

    @Override
    public UserSocial getByPlatformAndOpenid(SocialPlatformEnum platform, String openid) {
        return getOne(new LambdaQueryWrapper<UserSocial>()
                .eq(UserSocial::getPlatform, platform)
                .eq(UserSocial::getOpenid, openid));
    }

    @Override
    public UserSocial getByUnionid(String unionid) {
        if (StrUtil.isBlank(unionid)) {
            return null;
        }
        return getOne(new LambdaQueryWrapper<UserSocial>()
                .eq(UserSocial::getUnionid, unionid));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserSocial bindOrUpdate(UserSocialBindDTO command) {
        UserSocial userSocial = getByPlatformAndOpenid(command.getPlatform(), command.getOpenid());
        LocalDateTime now = LocalDateTime.now();

        if (userSocial == null) {
            userSocial = new UserSocial();
            userSocial.setUserId(command.getUserId());
            userSocial.setPlatform(command.getPlatform());
            userSocial.setOpenid(command.getOpenid());
            userSocial.setUnionid(command.getUnionid());
            userSocial.setSessionKey(command.getSessionKey());
            userSocial.setVerified(1);
            userSocial.setCreateTime(now);
            userSocial.setUpdateTime(now);
            save(userSocial);
            log.info("第三方账号绑定成功：userId={}, platform={}, openid={}",
                    command.getUserId(), command.getPlatform(), command.getOpenid());
        } else {
            userSocial.setUserId(command.getUserId());
            userSocial.setUnionid(command.getUnionid());
            userSocial.setSessionKey(command.getSessionKey());
            userSocial.setUpdateTime(now);
            updateById(userSocial);
            log.info("第三方账号更新成功：userId={}, platform={}, openid={}",
                    command.getUserId(), command.getPlatform(), command.getOpenid());
        }

        return userSocial;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserSocial createWechatUserBinding(String openid, String unionid, String sessionKey) {
        // 绑定表只保存第三方身份信息，用户主动维护的昵称和头像统一归属 APP 用户表。
        // APP 用户与微信绑定必须在同一事务中创建，避免任一步骤失败后遗留孤立数据。
        AppUser user = appUserService.createWechatUser();
        return bindOrUpdate(UserSocialBindDTO.builder()
                .userId(user.getId())
                .platform(SocialPlatformEnum.WECHAT_MINI)
                .openid(openid)
                .unionid(unionid)
                .sessionKey(sessionKey)
                .build());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unbind(Long userId, SocialPlatformEnum platform) {
        boolean removed = remove(new LambdaQueryWrapper<UserSocial>()
                .eq(UserSocial::getUserId, userId)
                .eq(UserSocial::getPlatform, platform));
        if (removed) {
            log.info("第三方账号解绑成功：userId={}, platform={}", userId, platform);
        }
        return removed;
    }

    @Override
    public UserAuthInfo getAuthInfoByOpenid(SocialPlatformEnum platform, String openid) {
        UserSocial userSocial = getByPlatformAndOpenid(platform, openid);
        if (userSocial == null) {
            return null;
        }
        // 微信绑定中的 user_id 现在指向独立的 APP 用户表。
        return appUserService.getAuthInfoById(userSocial.getUserId());
    }

    @Override
    public void updateSessionKey(Long id, String sessionKey) {
        UserSocial userSocial = getById(id);
        if (userSocial != null) {
            userSocial.setSessionKey(sessionKey);
            userSocial.setUpdateTime(LocalDateTime.now());
            updateById(userSocial);
        }
    }

}
