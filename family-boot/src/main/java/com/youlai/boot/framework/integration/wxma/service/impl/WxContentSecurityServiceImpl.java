package com.youlai.boot.framework.integration.wxma.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.security.WxMaMsgSecCheckCheckRequest;
import cn.binarywang.wx.miniapp.bean.security.WxMaMsgSecCheckCheckResponse;
import cn.hutool.core.util.StrUtil;
import com.youlai.boot.common.exception.BusinessException;
import com.youlai.boot.common.result.ResultCode;
import com.youlai.boot.framework.integration.wxma.service.WxContentSecurityService;
import com.youlai.boot.system.enums.SocialPlatformEnum;
import com.youlai.boot.system.model.entity.UserSocial;
import com.youlai.boot.system.service.UserSocialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 微信小程序用户发布内容安全检测服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WxContentSecurityServiceImpl implements WxContentSecurityService {

    private static final int CONTENT_RISK_ERROR_CODE = 87014;
    private static final int USER_CONTENT_SCENE = 2;
    private static final String TEXT_CHECK_VERSION = "2";
    private static final String PASS_SUGGESTION = "pass";
    private static final String CONTENT_REJECTED_MESSAGE = "内容可能存在违规信息，请修改或重新选择后再提交";

    private final WxMaService wxMaService;
    private final UserSocialService userSocialService;

    @Override
    public void checkImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }
        Path tempFile = null;
        try {
            // 微信同步图片检测接口只接受文件，因此先写入独立临时文件，检测完成后立即删除。
            tempFile = Files.createTempFile("wx-image-security-", resolveSuffix(file.getOriginalFilename()));
            file.transferTo(tempFile);
            if (!wxMaService.getSecurityService().checkImage(tempFile.toFile())) {
                throw contentRejectedException();
            }
        } catch (WxErrorException exception) {
            handleWxException("image", exception);
        } catch (IOException exception) {
            log.error("创建微信图片安全检测临时文件失败", exception);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "内容安全检测暂时不可用，请稍后再试");
        } finally {
            deleteTempFile(tempFile);
        }
    }

    @Override
    public void checkText(Long userId, Collection<String> contents) {
        String content = contents == null ? "" : contents.stream()
                .filter(StrUtil::isNotBlank)
                .map(String::trim)
                .distinct()
                .collect(Collectors.joining("\n"));
        if (StrUtil.isBlank(content)) {
            return;
        }

        UserSocial binding = userSocialService.lambdaQuery()
                .eq(UserSocial::getUserId, userId)
                .eq(UserSocial::getPlatform, SocialPlatformEnum.WECHAT_MINI)
                .one();
        if (binding == null || StrUtil.isBlank(binding.getOpenid())) {
            throw new BusinessException(ResultCode.ACCESS_TOKEN_INVALID, "微信登录状态已失效，请重新登录后再提交");
        }

        WxMaMsgSecCheckCheckRequest request = WxMaMsgSecCheckCheckRequest.builder()
                .version(TEXT_CHECK_VERSION)
                .openid(binding.getOpenid())
                .scene(USER_CONTENT_SCENE)
                .content(content)
                .build();
        try {
            WxMaMsgSecCheckCheckResponse response = wxMaService.getSecurityService().checkMessage(request);
            String suggestion = response == null || response.getResult() == null
                    ? null
                    : response.getResult().getSuggest();
            // review 和 risky 都不能发布，防止待复核内容先在相册中对其他成员可见。
            if (!PASS_SUGGESTION.equalsIgnoreCase(suggestion)) {
                log.warn("微信文字内容安全检测未通过，userId={}, traceId={}, suggestion={}",
                        userId, response == null ? null : response.getTraceId(), suggestion);
                throw contentRejectedException();
            }
        } catch (WxErrorException exception) {
            handleWxException("text", exception);
        }
    }

    private void handleWxException(String contentType, WxErrorException exception) {
        Integer errorCode = exception.getError() == null ? null : exception.getError().getErrorCode();
        if (Objects.equals(errorCode, CONTENT_RISK_ERROR_CODE)) {
            throw contentRejectedException();
        }
        log.error("微信内容安全检测调用失败，contentType={}, errorCode={}", contentType, errorCode, exception);
        throw new BusinessException(ResultCode.THIRD_PARTY_SERVICE_ERROR, "内容安全检测暂时不可用，请稍后再试");
    }

    private BusinessException contentRejectedException() {
        return new BusinessException(ResultCode.CONTENT_SECURITY_REJECTED, CONTENT_REJECTED_MESSAGE);
    }

    private String resolveSuffix(String originalFilename) {
        String suffix = StrUtil.subAfter(StrUtil.blankToDefault(originalFilename, "image.tmp"), '.', true);
        return StrUtil.isBlank(suffix) ? ".tmp" : "." + suffix;
    }

    private void deleteTempFile(Path tempFile) {
        if (tempFile == null) {
            return;
        }
        try {
            Files.deleteIfExists(tempFile);
        } catch (IOException exception) {
            log.warn("删除微信图片安全检测临时文件失败，path={}", tempFile, exception);
        }
    }
}
