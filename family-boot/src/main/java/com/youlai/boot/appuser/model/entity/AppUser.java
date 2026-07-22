package com.youlai.boot.appuser.model.entity;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.youlai.boot.appuser.model.AppUserModels.AppUserRegisterRequest;
import com.youlai.boot.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * APP 用户领域实体，独立于后台系统用户。
 */
@Getter
@Setter
@TableName("app_user")
public class AppUser extends BaseEntity {

    private String username;

    private String password;

    private String nickname;

    private String mobile;

    private String avatar;

    private Integer status;

    @TableLogic
    private Integer isDeleted;

    /** 创建使用账号密码注册的 APP 用户。 */
    @JsonIgnore
    public static AppUser register(AppUserRegisterRequest request, String encodedPassword) {
        AppUser user = new AppUser();
        user.username = request.getUsername().trim();
        user.nickname = request.getNickname().trim();
        user.password = encodedPassword;
        user.status = 1;
        user.isDeleted = 0;
        return user;
    }

    /** 根据手机号创建一个可登录的 APP 用户。 */
    @JsonIgnore
    public static AppUser createByMobile(String mobile) {
        AppUser user = new AppUser();
        user.mobile = mobile;
        user.username = "wx_" + IdUtil.fastSimpleUUID().substring(0, 8);
        user.nickname = "微信用户";
        user.status = 1;
        user.isDeleted = 0;
        return user;
    }

    /** 创建仅使用微信身份登录的 APP 用户。 */
    @JsonIgnore
    public static AppUser createByWechat() {
        AppUser user = new AppUser();
        user.username = "wx_" + IdUtil.fastSimpleUUID().substring(0, 8);
        user.nickname = "微信用户";
        user.status = 1;
        user.isDeleted = 0;
        return user;
    }

    /** 修改用户可自主维护的个人资料。 */
    public void updateProfile(String nickname, String avatar) {
        if (nickname != null) {
            this.nickname = nickname.trim();
        }
        if (avatar != null) {
            this.avatar = avatar.trim();
        }
    }

    /** 修改账号启用状态。 */
    public void changeStatus(Integer status) {
        this.status = status;
    }
}
