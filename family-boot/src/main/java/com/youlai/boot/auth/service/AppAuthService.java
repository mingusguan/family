package com.youlai.boot.auth.service;

import com.youlai.boot.appuser.model.AppUserModels.AppUserLoginRequest;
import com.youlai.boot.appuser.model.AppUserModels.AppUserRegisterRequest;
import com.youlai.boot.framework.security.model.AuthenticationToken;

/** APP 客户端账号认证服务。 */
public interface AppAuthService {

    AuthenticationToken register(AppUserRegisterRequest request);

    AuthenticationToken login(AppUserLoginRequest request);
}
