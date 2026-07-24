package com.youlai.boot.file.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 从照片、视频和音频文件中提取原始拍摄或录制时间。
 */
@Slf4j
@Component
public class MediaCaptureTimeExtractor {

    private static final List<String> CAPTURE_TIME_TAGS = List.of(
            "Date/Time Original",
            "Date/Time Digitized",
            "Creation Date",
            "Create Date",
            "Media Create Date",
            "Creation Time",
            "Date/Time"
    );

    private static final Pattern DATE_TIME_PATTERN = Pattern.compile(
            "(\\d{4})[:-](\\d{2})[:-](\\d{2})[ T](\\d{2}):(\\d{2}):(\\d{2})"
    );

    /**
     * 提取媒体文件中的原始时间。文件不包含相关元数据时返回空。
     */
    public Optional<LocalDateTime> extract(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Optional.empty();
        }
        try (InputStream inputStream = file.getInputStream()) {
            Metadata metadata = ImageMetadataReader.readMetadata(inputStream, file.getSize());
            for (String tagName : CAPTURE_TIME_TAGS) {
                Optional<LocalDateTime> capturedAt = findByTagName(metadata, tagName);
                if (capturedAt.isPresent()) {
                    return capturedAt;
                }
            }
        } catch (Exception exception) {
            // 元数据缺失或媒体格式不支持不能阻塞正常上传，调用方会回退为上传时间。
            log.debug("未能从上传文件提取拍摄时间，contentType={}", file.getContentType(), exception);
        }
        return Optional.empty();
    }

    private Optional<LocalDateTime> findByTagName(Metadata metadata, String expectedTagName) {
        for (Directory directory : metadata.getDirectories()) {
            for (Tag tag : directory.getTags()) {
                if (!expectedTagName.equalsIgnoreCase(tag.getTagName())) {
                    continue;
                }
                Optional<LocalDateTime> capturedAt = parseValue(directory.getObject(tag.getTagType()));
                if (capturedAt.isEmpty()) {
                    capturedAt = parseValue(directory.getString(tag.getTagType()));
                }
                if (capturedAt.filter(this::isReasonableCaptureTime).isPresent()) {
                    return capturedAt;
                }
            }
        }
        return Optional.empty();
    }

    private Optional<LocalDateTime> parseValue(Object value) {
        if (value instanceof Date date) {
            Instant instant = date.toInstant();
            return Optional.of(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
        }
        if (!(value instanceof String text)) {
            return Optional.empty();
        }
        Matcher matcher = DATE_TIME_PATTERN.matcher(text.trim());
        if (!matcher.find()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDateTime.of(
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2)),
                    Integer.parseInt(matcher.group(3)),
                    Integer.parseInt(matcher.group(4)),
                    Integer.parseInt(matcher.group(5)),
                    Integer.parseInt(matcher.group(6))
            ));
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private boolean isReasonableCaptureTime(LocalDateTime capturedAt) {
        return capturedAt.getYear() >= 1900 && !capturedAt.isAfter(LocalDateTime.now().plusDays(1));
    }
}
