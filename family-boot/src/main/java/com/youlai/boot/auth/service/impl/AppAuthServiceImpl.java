package com.youlai.boot.auth.service.impl;

import com.youlai.boot.appuser.model.AppUserModels.AppUserLoginRequest;
import com.youlai.boot.appuser.model.AppUserModels.AppUserRegisterRequest;
import com.youlai.boot.appuser.model.entity.AppUser;
import com.youlai.boot.appuser.service.AppUserService;
import com.youlai.boot.auth.service.AppAuthService;
import com.youlai.boot.common.exception.BusinessException;
import com.youlai.boot.framework.security.model.AuthenticationToken;
import com.youlai.boot.framework.security.model.SysUserDetails;
import com.youlai.boot.framework.security.model.UserAuthInfo;
import com.youlai.boot.framework.security.token.TokenManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** APP 客户端账号认证服务实现。 */
@Service
@RequiredArgsConstructor
public class AppAuthServiceImpl implements AppAuthService {

    private final AppUserService appUserService;
    private final PasswordEncoder passwordEncoder;
    private final TokenManager tokenManager;

    @Override
    public AuthenticationToken register(AppUserRegisterRequest request) {
        AppUser user = appUserService.register(request);
        UserAuthInfo authInfo = appUserService.getAuthInfoById(user.getId());
        // 注册成功后直接签发令牌，让客户端无需再次输入账号密码。
        return issueToken(authInfo);
    }

    @Override
    public AuthenticationToken login(AppUserLoginRequest request) {
        UserAuthInfo authInfo = appUserService.getAuthInfoByUsername(request.getUsername());
        if (authInfo == null || authInfo.getPassword() == null
                || !passwordEncoder.matches(request.getPassword(), authInfo.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!Integer.valueOf(1).equals(authInfo.getStatus())) {
            throw new BusinessException("账号已被禁用");
        }
        return issueToken(authInfo);
    }

    private AuthenticationToken issueToken(UserAuthInfo authInfo) {
        SysUserDetails userDetails = new SysUserDetails(authInfo);
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return tokenManager.generateToken(authentication);
    }
}
