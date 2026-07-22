package com.youlai.boot.album.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.youlai.boot.common.base.IBaseEnum;
import lombok.Getter;

/**
 * 相册资源类型。
 */
@Getter
public enum AlbumMediaTypeEnum implements IBaseEnum<String> {

    IMAGE("IMAGE", "图片"),
    VIDEO("VIDEO", "视频"),
    AUDIO("AUDIO", "音频");

    @EnumValue
    private final String value;

    private final String label;

    AlbumMediaTypeEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }
}
