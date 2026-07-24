package com.youlai.boot.framework.integration.wxma.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;

/**
 * 微信小程序用户发布内容安全检测服务。
 */
public interface WxContentSecurityService {

    /**
     * 检测用户即将上传的图片，检测不通过时直接阻止文件落盘。
     *
     * @param file 待上传文件
     */
    void checkImage(MultipartFile file);

    /**
     * 检测用户发布的描述和标签文本。
     *
     * @param userId 当前 APP 用户 ID
     * @param contents 待检测文本集合
     */
    void checkText(Long userId, Collection<String> contents);
}
