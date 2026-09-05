package com.youlai.boot.framework.integration.wxma;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 微信小程序配置属性
 */
@Data
@ConfigurationProperties(prefix = "wx.miniapp")
public class WxMaProperties {

    /**
     * 小程序 AppID
     */
    private String appid;

    /**
     * 小程序 AppSecret
     */
    private String secret;

    /**
     * 本地开发微信登录降级配置。
     */
    private MockLogin mockLogin = new MockLogin();


    /**
     * 用户内容安全检测配置。
     */
    private ContentSecurity contentSecurity = new ContentSecurity();

    /**
     * 内容安全检测配置。
     */
    @Data
    public static class ContentSecurity {

        /**
         * 微信检测服务异常时是否降级放行。
         * 明确命中违规内容时始终拒绝，不受该配置影响。
         */
        private boolean failOpen = true;
    }

    /**
     * 本地开发 mock 登录配置。
     */
    @Data
    public static class MockLogin {

        /**
         * 是否启用 mock 登录。
         */
        private boolean enabled = false;

        /**
         * mock openid，固定后可复用同一个本地 APP 用户。
         */
        private String openid = "dev-wxma-openid";

        /**
         * mock session_key。
         */
        private String sessionKey = "dev-wxma-session-key";
    }
}
