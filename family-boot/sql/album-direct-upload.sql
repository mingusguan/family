-- 小程序相册直传批次：原文件由客户端直接上传 COS，后端只保存进度计数并异步审核。
CREATE TABLE IF NOT EXISTS app_album_upload_batch (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  batch_id VARCHAR(64) NOT NULL COMMENT '客户端直传批次ID',
  uploader_id BIGINT NOT NULL COMMENT '上传用户ID',
  family_id BIGINT NOT NULL COMMENT '家庭ID',
  album_id BIGINT NOT NULL COMMENT '相册ID',
  total_count INT NOT NULL DEFAULT 0 COMMENT '初始化文件数',
  processing_count INT NOT NULL DEFAULT 0 COMMENT '异步处理中数量',
  success_count INT NOT NULL DEFAULT 0 COMMENT '处理成功数量',
  failed_count INT NOT NULL DEFAULT 0 COMMENT '上传或处理失败数量',
  status VARCHAR(16) NOT NULL DEFAULT 'INIT' COMMENT 'INIT/PROCESSING/COMPLETED',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_album_upload_batch_id (batch_id),
  KEY idx_album_upload_batch_owner (uploader_id, create_time),
  KEY idx_album_upload_batch_status (status, update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小程序相册直传批次';

-- 原有资源表已具备 upload_batch_id、status、file_size、mime_type、captured_at 字段。
-- status=0 仅表示尚未完成 COS 对象核验；确认通过后立即切换为 status=1，元数据在后台继续补充。
