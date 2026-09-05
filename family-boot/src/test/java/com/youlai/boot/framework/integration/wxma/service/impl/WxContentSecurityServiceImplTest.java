package com.youlai.boot.framework.integration.wxma.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaSecurityService;
import cn.binarywang.wx.miniapp.api.WxMaService;
import com.youlai.boot.common.exception.BusinessException;
import com.youlai.boot.framework.integration.wxma.WxMaProperties;
import com.youlai.boot.system.service.UserSocialService;
import me.chanjar.weixin.common.error.WxError;
import me.chanjar.weixin.common.error.WxErrorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 微信内容安全检测降级策略测试。
 */
@ExtendWith(MockitoExtension.class)
class WxContentSecurityServiceImplTest {

    @Mock
    private WxMaService wxMaService;

    @Mock
    private WxMaSecurityService wxMaSecurityService;

    @Mock
    private UserSocialService userSocialService;

    private WxContentSecurityServiceImpl contentSecurityService;

    @BeforeEach
    void setUp() {
        WxMaProperties properties = new WxMaProperties();
        properties.getContentSecurity().setFailOpen(true);
        contentSecurityService = new WxContentSecurityServiceImpl(wxMaService, userSocialService, properties);
        when(wxMaService.getSecurityService()).thenReturn(wxMaSecurityService);
    }

    @Test
    void shouldAllowPublishWhenWechatSecurityServiceIsUnavailable() throws WxErrorException {
        when(wxMaSecurityService.checkImage(any(File.class)))
                .thenThrow(new WxErrorException(new WxError(50001, "service unavailable")));

        assertThatCode(() -> contentSecurityService.checkImage(testImage()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectPublishWhenWechatReportsRiskyContent() throws WxErrorException {
        when(wxMaSecurityService.checkImage(any(File.class)))
                .thenThrow(new WxErrorException(new WxError(87014, "risky content")));

        assertThatThrownBy(() -> contentSecurityService.checkImage(testImage()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("内容可能存在违规信息");
    }

    @Test
    void shouldRejectUnavailableServiceWhenFailOpenIsDisabled() throws WxErrorException {
        WxMaProperties properties = new WxMaProperties();
        properties.getContentSecurity().setFailOpen(false);
        contentSecurityService = new WxContentSecurityServiceImpl(wxMaService, userSocialService, properties);
        when(wxMaSecurityService.checkImage(any(File.class)))
                .thenThrow(new WxErrorException(new WxError(50001, "service unavailable")));

        assertThatThrownBy(() -> contentSecurityService.checkImage(testImage()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("内容安全检测暂时不可用");
    }

    private MockMultipartFile testImage() {
        return new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3});
    }

}
