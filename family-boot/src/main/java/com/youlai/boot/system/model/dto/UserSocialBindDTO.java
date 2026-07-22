package com.youlai.boot.system.model.dto;

import com.youlai.boot.system.enums.SocialPlatformEnum;
import lombok.Builder;
import lombok.Getter;

/**
 * 第三方账号身份绑定参数。
 */
@Getter
@Builder
public class UserSocialBindDTO {

    /** 业务用户ID。 */
    private Long userId;

    /** 第三方平台。 */
    private SocialPlatformEnum platform;

    /** 平台用户唯一标识。 */
    private String openid;

    /** 微信开放平台统一用户标识。 */
    private String unionid;

    /** 微信当前会话密钥。 */
    private String sessionKey;
}
