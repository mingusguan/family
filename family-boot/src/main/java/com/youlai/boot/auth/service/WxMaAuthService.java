package com.youlai.boot.auth.service;

import com.youlai.boot.auth.model.WxMaLoginResp;
import com.youlai.boot.framework.security.model.AuthenticationToken;

/**
 * 微信小程序认证服务接口
 *
 * @author Ray.Hao
 * @since 2.4.0
 */
public interface WxMaAuthService {

    /**
     * 静默登录
     * <p>
     * 通过微信登录凭证（code）获取用户唯一标识（openid）。
     * 首次登录时自动创建 APP 用户，后续直接使用 openid 登录。
     * </p>
     *
     * @param code 微信登录凭证（wx.login 获取）
     * @return 微信登录结果
     */
    WxMaLoginResp silentLogin(String code);

    /**
     * 手机号快捷登录
     * <p>
     * 同时使用微信登录凭证和手机号授权凭证，
     * 一步完成用户注册/登录，无需额外绑定流程。
     * 适用于企业认证的小程序（已开通手机号快捷登录权限）。
     * </p>
     *
     * @param loginCode 微信登录凭证（wx.login 获取）
     * @param phoneCode 手机号授权凭证（getPhoneNumber 事件获取）
     * @return 认证令牌
     */
    AuthenticationToken phoneLogin(String loginCode, String phoneCode);

    /**
     * 绑定手机号
     * <p>
     * 为已静默登录但未绑定手机号的用户绑定手机号，
     * 绑定成功后自动完成登录。
     * </p>
     *
     * @param openid   微信用户唯一标识
     * @param mobile   手机号码
     * @param smsCode  短信验证码
     * @return 认证令牌
     */
    AuthenticationToken bindMobile(String openid, String mobile, String smsCode);
}
