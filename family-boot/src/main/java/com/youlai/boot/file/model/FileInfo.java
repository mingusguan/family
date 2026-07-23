package com.youlai.boot.file.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;


/**
 * 文件信息对象
 *
 * @author Ray.Hao
 * @since 1.0.0
 */
@Schema(description = "文件对象")
@Data
public class FileInfo {

    @Schema(description = "文件名称")
    private String name;

    @Schema(description = "文件URL")
    private String url;

    @Schema(description = "文件临时预览URL")
    private String previewUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "媒体文件中的原始拍摄或录制时间")
    private LocalDateTime capturedAt;

}
