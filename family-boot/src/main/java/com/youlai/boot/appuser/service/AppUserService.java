package com.youlai.boot.appuser.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.youlai.boot.appuser.model.entity.AppUser;
import com.youlai.boot.appuser.model.AppUserModels.AppUserInfoVO;
import com.youlai.boot.appuser.model.AppUserModels.AppUserRegisterRequest;
import com.youlai.boot.appuser.model.AppUserModels.AppUserProfileUpdateRequest;
import com.youlai.boot.appuser.model.AppUserModels.AppUserProfileVO;
import com.youlai.boot.framework.security.model.UserAuthInfo;

/** APP 用户领域服务。 */
public interface AppUserService extends IService<AppUser> {

    AppUser register(AppUserRegisterRequest request);

    UserAuthInfo getAuthInfoByUsername(String username);

    AppUserInfoVO getCurrentUserInfo();

    AppUserProfileVO getCurrentUserProfile();

    AppUserProfileVO updateCurrentUserProfile(AppUserProfileUpdateRequest request);

    AppUser findOrCreateByMobile(String mobile);

    AppUser createWechatUser();

    UserAuthInfo getAuthInfoByMobile(String mobile);

    UserAuthInfo getAuthInfoById(Long userId);
}
