package com.youlai.boot.file.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.iptc.IptcDirectory;
import com.drew.metadata.xmp.XmpDirectory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
            "Date Created",
            "Digital Date Created",
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
            Optional<LocalDateTime> structuredCapturedAt = findStructuredCaptureTime(metadata);
            if (structuredCapturedAt.isPresent()) {
                return structuredCapturedAt;
            }
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

    private Optional<LocalDateTime> findStructuredCaptureTime(Metadata metadata) {
        // 相机照片优先读取 EXIF 标准字段，避免依赖不同格式下可能变化的展示标签名。
        for (ExifSubIFDDirectory directory : metadata.getDirectoriesOfType(ExifSubIFDDirectory.class)) {
            Optional<LocalDateTime> capturedAt = firstReasonableDate(
                    directory.getDateOriginal(),
                    directory.getDateDigitized(),
                    directory.getDateModified()
            );
            if (capturedAt.isPresent()) {
                return capturedAt;
            }
        }

        for (ExifIFD0Directory directory : metadata.getDirectoriesOfType(ExifIFD0Directory.class)) {
            Optional<LocalDateTime> capturedAt = parseValue(directory.getDate(ExifDirectoryBase.TAG_DATETIME))
                    .filter(this::isReasonableCaptureTime);
            if (capturedAt.isPresent()) {
                return capturedAt;
            }
        }

        // 编辑软件和部分手机会把原始日期写入 IPTC 或 XMP，而不是 EXIF 子目录。
        for (IptcDirectory directory : metadata.getDirectoriesOfType(IptcDirectory.class)) {
            Optional<LocalDateTime> capturedAt = firstReasonableDate(
                    directory.getDateCreated(),
                    directory.getDigitalDateCreated()
            );
            if (capturedAt.isPresent()) {
                return capturedAt;
            }
        }

        for (XmpDirectory directory : metadata.getDirectoriesOfType(XmpDirectory.class)) {
            Optional<LocalDateTime> capturedAt = findXmpCaptureTime(directory);
            if (capturedAt.isPresent()) {
                return capturedAt;
            }
        }
        return Optional.empty();
    }

    private Optional<LocalDateTime> firstReasonableDate(Date... dates) {
        for (Date date : dates) {
            Optional<LocalDateTime> capturedAt = parseValue(date).filter(this::isReasonableCaptureTime);
            if (capturedAt.isPresent()) {
                return capturedAt;
            }
        }
        return Optional.empty();
    }

    private Optional<LocalDateTime> findXmpCaptureTime(XmpDirectory directory) {
        List<String> propertyNames = List.of(
                "DateTimeOriginal",
                "DateTimeDigitized",
                "DateCreated",
                "CreateDate"
        );
        for (String propertyName : propertyNames) {
            for (var property : directory.getXmpProperties().entrySet()) {
                if (!property.getKey().toLowerCase(Locale.ROOT).endsWith(propertyName.toLowerCase(Locale.ROOT))) {
                    continue;
                }
                Optional<LocalDateTime> capturedAt = parseValue(property.getValue())
                        .filter(this::isReasonableCaptureTime);
                if (capturedAt.isPresent()) {
                    return capturedAt;
                }
            }
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
