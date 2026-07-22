package com.youlai.boot.appuser.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.youlai.boot.appuser.model.AppUserModels.AppUserQuery;
import com.youlai.boot.appuser.model.AppUserModels.AppUserVO;
import com.youlai.boot.common.model.Option;

import java.util.List;

/**
 * APP 用户后台管理服务。
 */
public interface AppUserManagementService {

    IPage<AppUserVO> getPage(AppUserQuery query);

    List<Option<String>> listOptions(String keyword);

    boolean updateStatus(Long userId, Integer status);

    boolean deleteUsers(String ids);
}

