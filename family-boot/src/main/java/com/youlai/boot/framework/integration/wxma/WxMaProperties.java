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
}
