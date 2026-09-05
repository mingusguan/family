package com.youlai.boot.file.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class MediaCaptureTimeExtractorTest {

    private static final byte[] BASE_JPEG = Base64.getDecoder().decode(
            "/9j/4AAQSkZJRgABAgAAAQABAAD//gAQTGF2YzYxLjExLjEwMAD/2wBDAAgEBAQEBAUFBQUFBQYGBgYGBgYGBgYGBgYGBgYHBwcICAgHBwcGBgcHCAgICAkJCQgICAgJCQoKCgwMCwsODg4RERT/xABLAAEBAAAAAAAAAAAAAAAAAAAABwEBAAAAAAAAAAAAAAAAAAAAABABAAAAAAAAAAAAAAAAAAAAABEBAAAAAAAAAAAAAAAAAAAAAP/AABEIAAIAAgMBIgACEQADEQD/2gAMAwEAAhEDEQA/AL+AD//Z"
    );

    private final MediaCaptureTimeExtractor extractor = new MediaCaptureTimeExtractor();

    @Test
    void shouldExtractExifOriginalDate() {
        byte[] jpeg = insertApp1Segment(BASE_JPEG, createExifPayload("2020:05:06 07:08:09"));
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", jpeg);

        assertThat(extractor.extract(file)).contains(LocalDateTime.of(2020, 5, 6, 7, 8, 9));
    }

    @Test
    void shouldExtractXmpCreateDateWhenExifIsMissing() {
        byte[] jpeg = insertApp1Segment(BASE_JPEG, createXmpPayload("2021-06-07T08:09:10+08:00"));
        MockMultipartFile file = new MockMultipartFile("file", "edited-photo.jpg", "image/jpeg", jpeg);

        assertThat(extractor.extract(file)).contains(LocalDateTime.of(2021, 6, 7, 8, 9, 10));
    }

    private byte[] createExifPayload(String capturedAt) {
        ByteBuffer tiff = ByteBuffer.allocate(64).order(ByteOrder.LITTLE_ENDIAN);
        tiff.put((byte) 'I').put((byte) 'I').putShort((short) 42).putInt(8);
        tiff.putShort((short) 1);
        tiff.putShort((short) 0x8769).putShort((short) 4).putInt(1).putInt(26);
        tiff.putInt(0);
        tiff.putShort((short) 1);
        tiff.putShort((short) 0x9003).putShort((short) 2).putInt(20).putInt(44);
        tiff.putInt(0);
        tiff.put(capturedAt.getBytes(StandardCharsets.US_ASCII)).put((byte) 0);

        byte[] payload = new byte[6 + tiff.array().length];
        System.arraycopy("Exif\0\0".getBytes(StandardCharsets.US_ASCII), 0, payload, 0, 6);
        System.arraycopy(tiff.array(), 0, payload, 6, tiff.array().length);
        return payload;
    }

    private byte[] createXmpPayload(String capturedAt) {
        String xml = "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\"><rdf:RDF "
                + "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"><rdf:Description "
                + "xmlns:xmp=\"http://ns.adobe.com/xap/1.0/\" xmp:CreateDate=\"" + capturedAt
                + "\"/></rdf:RDF></x:xmpmeta>";
        byte[] header = "http://ns.adobe.com/xap/1.0/\0".getBytes(StandardCharsets.US_ASCII);
        byte[] body = xml.getBytes(StandardCharsets.UTF_8);
        byte[] payload = new byte[header.length + body.length];
        System.arraycopy(header, 0, payload, 0, header.length);
        System.arraycopy(body, 0, payload, header.length, body.length);
        return payload;
    }

    private byte[] insertApp1Segment(byte[] jpeg, byte[] payload) {
        ByteBuffer segment = ByteBuffer.allocate(payload.length + 4).order(ByteOrder.BIG_ENDIAN);
        segment.put((byte) 0xff).put((byte) 0xe1).putShort((short) (payload.length + 2)).put(payload);

        byte[] result = new byte[jpeg.length + segment.array().length];
        System.arraycopy(jpeg, 0, result, 0, 2);
        System.arraycopy(segment.array(), 0, result, 2, segment.array().length);
        System.arraycopy(jpeg, 2, result, 2 + segment.array().length, jpeg.length - 2);
        return result;
    }
}
