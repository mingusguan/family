package com.youlai.boot.framework.security.provider;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.hutool.core.util.ObjectUtil;
import com.youlai.boot.framework.security.model.SysUserDetails;
import com.youlai.boot.framework.security.model.UserAuthInfo;
import com.youlai.boot.framework.security.model.WxMaAuthenticationToken;
import com.youlai.boot.framework.security.service.SysUserDetailsService;
import com.youlai.boot.system.model.entity.UserSocial;
import com.youlai.boot.system.service.UserSocialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * 微信小程序认证 Provider
 */
@Slf4j
@RequiredArgsConstructor
public class WxMaAuthenticationProvider implements AuthenticationProvider {

    private final WxMaService wxMaService;
    private final SysUserDetailsService sysUserDetailsService;
    private final UserSocialService userSocialService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String code = (String) authentication.getPrincipal();

        if (code == null || code.isEmpty()) {
            log.warn("微信小程序登录失败：code为空");
            throw new IllegalArgumentException("code不能为空");
        }

        try {
            // 1. 用 code 换取 openid
            WxMaJscode2SessionResult session = wxMaService.jsCode2SessionInfo(code);
            String openid = session.getOpenid();
            String sessionKey = session.getSessionKey();

            log.info("微信小程序登录：openid={}", openid);

            // 2. 根据 openid 查询绑定信息
            UserSocial userSocial = sysUserDetailsService.getWechatMiniBindInfo(openid);

            boolean newUser = false;
            if (userSocial == null) {
                // 首次微信登录时创建独立 APP 用户，并建立 openid 绑定关系。
                userSocial = userSocialService.createWechatUserBinding(openid, session.getUnionid(), sessionKey);
                newUser = true;
                log.info("微信小程序首次登录：已自动创建用户，userId={}, openid={}", userSocial.getUserId(), openid);
            } else {
                // 已绑定用户每次登录都刷新 session_key，保证后续微信能力调用使用最新会话。
                sysUserDetailsService.updateWechatSessionKey(userSocial.getId(), sessionKey);
            }

            // 3. 获取用户认证信息
            UserAuthInfo userAuthInfo = sysUserDetailsService.getAuthInfoByWechatOpenid(openid);

            if (userAuthInfo == null) {
                log.warn("微信小程序登录失败：用户不存在，openid={}", openid);
                throw new UsernameNotFoundException("用户不存在");
            }

            // 4. 检查用户状态
            if (ObjectUtil.notEqual(userAuthInfo.getStatus(), 1)) {
                log.warn("微信小程序登录失败：用户已禁用，username={}", userAuthInfo.getUsername());
                throw new DisabledException("用户已被禁用");
            }

            // 5. 构建已认证 Token
            SysUserDetails userDetails = new SysUserDetails(userAuthInfo);

            log.info("微信小程序登录成功：username={}, openid={}", userAuthInfo.getUsername(), openid);

            return WxMaAuthenticationToken.authenticated(userDetails, userDetails.getAuthorities(), newUser);

        } catch (WxErrorException e) {
            log.error("微信小程序登录失败：调用微信接口异常", e);
            throw new IllegalArgumentException("微信登录失败：" + e.getMessage());
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return WxMaAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
