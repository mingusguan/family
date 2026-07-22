package com.youlai.boot.auth.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.hutool.core.util.StrUtil;
import com.youlai.boot.appuser.model.entity.AppUser;
import com.youlai.boot.appuser.service.AppUserService;
import com.youlai.boot.auth.model.WxMaLoginResp;
import com.youlai.boot.auth.service.WxMaAuthService;
import com.youlai.boot.common.constant.RedisConstants;
import com.youlai.boot.framework.security.model.AuthenticationToken;
import com.youlai.boot.framework.security.model.SysUserDetails;
import com.youlai.boot.framework.security.model.WxMaAuthenticationToken;
import com.youlai.boot.framework.security.token.TokenManager;
import com.youlai.boot.system.enums.SocialPlatformEnum;
import com.youlai.boot.system.model.dto.UserSocialBindDTO;
import com.youlai.boot.system.service.UserSocialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 微信小程序认证服务实现
 *
 * @author Ray.Hao
 * @since 4.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WxMaAuthServiceImpl implements WxMaAuthService {

    private final WxMaService wxMaService;
    private final AuthenticationManager authenticationManager;
    private final TokenManager tokenManager;
    private final AppUserService appUserService;
    private final UserSocialService userSocialService;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 静默登录
     */
    @Override
    public WxMaLoginResp silentLogin(String code) {
        WxMaAuthenticationToken token = new WxMaAuthenticationToken(code);

        WxMaAuthenticationToken authentication =
                (WxMaAuthenticationToken) authenticationManager.authenticate(token);
        AuthenticationToken authToken = tokenManager.generateToken(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return WxMaLoginResp.builder()
                .isNewUser(authentication.isNewUser())
                .accessToken(authToken.getAccessToken())
                .refreshToken(authToken.getRefreshToken())
                .tokenType(authToken.getTokenType())
                .expiresIn(authToken.getExpiresIn())
                .build();
    }

    /**
     * 手机号快捷登录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthenticationToken phoneLogin(String loginCode, String phoneCode) {
        // 1. 解析微信登录凭证，获取会话信息
        WxMaJscode2SessionResult session = resolveSession(loginCode);
        String openid = session.getOpenid();

        // 2. 解析手机号授权凭证，获取手机号
        String mobile = resolvePhoneNumber(phoneCode);

        log.info("微信小程序手机号快捷登录：openid={}, mobile={}", openid, mobile);

        // 3. 查询或创建用户
        AppUser user = appUserService.findOrCreateByMobile(mobile);

        // 4. 绑定微信 openid
        bindWechatOpenid(user, session);

        // 5. 生成认证令牌
        return generateAuthToken(mobile);
    }

    /**
     * 绑定手机号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthenticationToken bindMobile(String openid, String mobile, String smsCode) {
        // 1. 验证短信验证码
        validateSmsCode(mobile, smsCode);

        // 2. 查询或创建用户
        AppUser user = appUserService.findOrCreateByMobile(mobile);

        // 3. 绑定微信 openid
        userSocialService.bindOrUpdate(UserSocialBindDTO.builder()
                .userId(user.getId())
                .platform(SocialPlatformEnum.WECHAT_MINI)
                .openid(openid)
                .build());

        log.info("微信小程序绑定手机号成功：mobile={}, openid={}", mobile, openid);

        // 4. 生成认证令牌
        return generateAuthToken(mobile);
    }

    // ==================== 私有方法 ====================

    /**
     * 解析微信登录凭证，获取会话信息
     */
    private WxMaJscode2SessionResult resolveSession(String loginCode) {
        try {
            return wxMaService.jsCode2SessionInfo(loginCode);
        } catch (Exception e) {
            log.error("获取微信会话信息失败，loginCode={}", loginCode, e);
            throw new IllegalArgumentException("微信登录失败：" + e.getMessage());
        }
    }

    /**
     * 解析手机号授权凭证，获取手机号
     */
    private String resolvePhoneNumber(String phoneCode) {
        try {
            WxMaPhoneNumberInfo phoneInfo = wxMaService.getUserService().getPhoneNoInfo(phoneCode);
            return phoneInfo.getPhoneNumber();
        } catch (Exception e) {
            log.error("获取微信手机号失败，phoneCode={}", phoneCode, e);
            throw new IllegalArgumentException("获取手机号失败：" + e.getMessage());
        }
    }

    /**
     * 绑定微信 openid
     */
    private void bindWechatOpenid(AppUser user, WxMaJscode2SessionResult session) {
        try {
            userSocialService.bindOrUpdate(UserSocialBindDTO.builder()
                    .userId(user.getId())
                    .platform(SocialPlatformEnum.WECHAT_MINI)
                    .openid(session.getOpenid())
                    .unionid(session.getUnionid())
                    .sessionKey(session.getSessionKey())
                    .build());
        } catch (Exception e) {
            // 绑定失败不影响登录
            log.warn("绑定微信 openid 失败，userId={}, openid={}", user.getId(), session.getOpenid(), e);
        }
    }

    /**
     * 验证短信验证码
     */
    private void validateSmsCode(String mobile, String smsCode) {
        String cacheKey = StrUtil.format(RedisConstants.Captcha.SMS_LOGIN_CODE, mobile);
        String cachedCode = (String) redisTemplate.opsForValue().get(cacheKey);

        if (!StrUtil.equals(smsCode, cachedCode)) {
            throw new IllegalArgumentException("验证码错误");
        }

        // 验证成功后删除验证码
        redisTemplate.delete(cacheKey);
    }

    /**
     * 生成认证令牌
     */
    private AuthenticationToken generateAuthToken(String mobile) {
        SysUserDetails userDetails = new SysUserDetails(appUserService.getAuthInfoByMobile(mobile));
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        AuthenticationToken authToken = tokenManager.generateToken(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authToken;
    }
}
