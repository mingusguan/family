package com.youlai.boot.framework.security.model;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.io.Serial;
import java.util.Collection;

/**
 * 微信小程序认证 Token
 *
 * @author Ray.Hao
 * @since 4.0.0
 */
public class WxMaAuthenticationToken extends AbstractAuthenticationToken {

    @Serial
    private static final long serialVersionUID = 622L;

    /**
     * 认证信息
     * 未认证时：微信code
     * 已认证时：SysUserDetails 用户详情
     */
    private final Object principal;

    /**
     * 凭证信息
     * 未认证时：null
     * 已认证时：null
     */
    private final Object credentials;
    /**
     * 是否为本次微信登录自动创建的新用户
     */
    private boolean newUser;


    /**
     * 创建未认证的 Token
     *
     * @param code 微信小程序code
     */
    public WxMaAuthenticationToken(String code) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.principal = code;
        this.credentials = null;
        setAuthenticated(false);
    }

    /**
     * 创建已认证的 Token
     *
     * @param principal   用户详情（SysUserDetails）
     * @param authorities 授权信息
     */
    public WxMaAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = null;
        super.setAuthenticated(true);
    }

    /**
     * 创建已认证的 Token（静态工厂方法）
     */
    public static WxMaAuthenticationToken authenticated(Object principal, Collection<? extends GrantedAuthority> authorities) {
        return new WxMaAuthenticationToken(principal, authorities);
    }
    /**
     * 创建已认证的微信 Token，并携带首次注册标记
     *
     * @param principal 用户详情
     * @param authorities 授权信息
     * @param newUser 是否为本次登录自动创建的新用户
     * @return 已认证的微信 Token
     */
    public static WxMaAuthenticationToken authenticated(
            Object principal,
            Collection<? extends GrantedAuthority> authorities,
            boolean newUser
    ) {
        WxMaAuthenticationToken token = new WxMaAuthenticationToken(principal, authorities);
        token.newUser = newUser;
        return token;
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    /**
     * @return 是否为本次微信登录自动创建的新用户
     */
    public boolean isNewUser() {
        return newUser;
    }

}
