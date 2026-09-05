-- 修复历史直传资源：COS 对象已由确认接口校验，但因异步元数据处理异常而一直停留在 status=0。
-- 可重复执行：仅更新尚未展示的资源；INIT 批次未完成对象校验，必须排除。
START TRANSACTION;

UPDATE app_album_asset AS asset
INNER JOIN app_album_upload_batch AS batch
        ON batch.batch_id = asset.upload_batch_id
       AND batch.family_id = asset.family_id
       AND batch.album_id = asset.album_id
       AND batch.uploader_id = asset.uploader_id
SET asset.status = 1,
    asset.captured_at = COALESCE(asset.captured_at, asset.create_time),
    asset.update_time = CURRENT_TIMESTAMP
WHERE asset.is_deleted = 0
  AND asset.status = 0
  AND batch.status IN ('PROCESSING', 'COMPLETED');

SELECT ROW_COUNT() AS repaired_asset_count;

COMMIT;
