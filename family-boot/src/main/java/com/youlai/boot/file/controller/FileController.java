package com.youlai.boot.file.controller;

import cn.hutool.core.util.StrUtil;
import com.youlai.boot.framework.integration.wxma.service.WxContentSecurityService;
import com.youlai.boot.common.result.Result;
import com.youlai.boot.file.service.FileService;
import com.youlai.boot.file.service.MediaCaptureTimeExtractor;
import com.youlai.boot.file.model.FileInfo;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件控制层
 *
 * @author Ray.Hao
 * @since 2022/10/16
 */
@Tag(name = "10.文件接口")
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final MediaCaptureTimeExtractor mediaCaptureTimeExtractor;
    private final WxContentSecurityService wxContentSecurityService;

    @PostMapping
    @Operation(summary = "文件上传")
    public Result<FileInfo> uploadFile(
            @Parameter(
                    name = "file",
                    description = "表单文件对象",
                    required = true,
                    in = ParameterIn.DEFAULT,
                    schema = @Schema(name = "file", format = "binary")
            )
            @RequestPart(value = "file") MultipartFile file
    ) {
        // 图片在写入对象存储前同步完成微信内容安全检测，所有上传入口都会经过此处。
        if (isImage(file)) {
            wxContentSecurityService.checkImage(file);
        }
        FileInfo fileInfo = fileService.uploadFile(file);
        fileInfo.setCapturedAt(mediaCaptureTimeExtractor.extract(file).orElse(null));
        return Result.success(fileInfo);
    }

    private boolean isImage(MultipartFile file) {
        String contentType = file.getContentType();
        if (StrUtil.startWithIgnoreCase(contentType, "image/")) {
            return true;
        }
        String filename = StrUtil.blankToDefault(file.getOriginalFilename(), "").toLowerCase();
        return filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png")
                || filename.endsWith(".gif") || filename.endsWith(".webp") || filename.endsWith(".bmp");
    }

    @DeleteMapping
    @Operation(summary = "文件删除")
    @SneakyThrows
    public Result<?> deleteFile(
            @Parameter(description = "文件路径") @RequestParam String filePath
    ) {
        boolean result = fileService.deleteFile(filePath);
        return Result.judge(result);
    }
}
