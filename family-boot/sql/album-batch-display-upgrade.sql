-- 相册批次聚合展示升级脚本。
SET @asset_batch_column = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'app_album_asset'
    AND COLUMN_NAME = 'upload_batch_id'
);
SET @asset_batch_column_sql = IF(
  @asset_batch_column = 0,
  'ALTER TABLE app_album_asset ADD COLUMN upload_batch_id VARCHAR(64) DEFAULT NULL COMMENT ''客户端批量发布批次ID'' AFTER album_id',
  'SELECT 1'
);
PREPARE asset_batch_column_stmt FROM @asset_batch_column_sql;
EXECUTE asset_batch_column_stmt;
DEALLOCATE PREPARE asset_batch_column_stmt;

-- 历史批量上传缺少显式批次号，按家庭、相册、上传人、创建秒和描述生成稳定批次。
UPDATE app_album_asset
SET upload_batch_id = SHA2(
  CONCAT_WS(
    '|',
    COALESCE(family_id, 0),
    COALESCE(album_id, 0),
    uploader_id,
    DATE_FORMAT(create_time, '%Y-%m-%d %H:%i:%s'),
    COALESCE(description, '')
  ),
  256
)
WHERE upload_batch_id IS NULL;

SET @asset_batch_index = (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'app_album_asset'
    AND INDEX_NAME = 'idx_album_asset_batch'
);
SET @asset_batch_index_sql = IF(
  @asset_batch_index = 0,
  'CREATE INDEX idx_album_asset_batch ON app_album_asset (upload_batch_id, id)',
  'SELECT 1'
);
PREPARE asset_batch_index_stmt FROM @asset_batch_index_sql;
EXECUTE asset_batch_index_stmt;
DEALLOCATE PREPARE asset_batch_index_stmt;
