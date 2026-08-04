package com.youlai.boot.file.service;

import java.io.InputStream;

/** 客户端直传对象存储能力。 */
public interface DirectUploadStorageService {
    UploadTicket createUploadTicket(String objectKey, long maxFileSize, long expirationSeconds);
    StoredObject headObject(String objectKey);
    InputStream openStream(String objectKey);
    String getObjectUrl(String objectKey);
    void deleteObject(String objectKey);

    record UploadTicket(String uploadUrl, String objectKey, String policy, String qSignAlgorithm,
                        String qAk, String qKeyTime, String qSignature, String securityToken) {
    }

    record StoredObject(long size, String contentType) {
    }
}
