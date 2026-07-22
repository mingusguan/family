-- 已部署旧版相册表的升级脚本，仅需执行一次。
-- 目标：移除分组/标签的用户归属，并将历史同名数据合并为全局数据。

-- 标签同名合并：先把资源关系复制到同名标签的最小 ID，再清理重复记录。
CREATE TEMPORARY TABLE `tmp_album_tag_mapping` AS
SELECT `tag`.`id` AS `old_id`, `canonical`.`canonical_id`
FROM `app_album_tag` `tag`
JOIN (
  SELECT `name`, MIN(`id`) AS `canonical_id`
  FROM `app_album_tag`
  GROUP BY `name`
) `canonical` ON `canonical`.`name` = `tag`.`name`;

INSERT IGNORE INTO `app_album_asset_tag` (`asset_id`, `tag_id`)
SELECT `relation`.`asset_id`, `mapping`.`canonical_id`
FROM `app_album_asset_tag` `relation`
JOIN `tmp_album_tag_mapping` `mapping` ON `mapping`.`old_id` = `relation`.`tag_id`;

DELETE `relation`
FROM `app_album_asset_tag` `relation`
JOIN `tmp_album_tag_mapping` `mapping` ON `mapping`.`old_id` = `relation`.`tag_id`
WHERE `mapping`.`old_id` <> `mapping`.`canonical_id`;

DELETE `tag`
FROM `app_album_tag` `tag`
JOIN `tmp_album_tag_mapping` `mapping` ON `mapping`.`old_id` = `tag`.`id`
WHERE `mapping`.`old_id` <> `mapping`.`canonical_id`;

DROP TEMPORARY TABLE `tmp_album_tag_mapping`;

-- 分组同名合并：历史资源统一指向同名分组的最小 ID。
CREATE TEMPORARY TABLE `tmp_album_group_mapping` AS
SELECT `album_group`.`id` AS `old_id`, `canonical`.`canonical_id`
FROM `app_album_group` `album_group`
JOIN (
  SELECT `name`, MIN(`id`) AS `canonical_id`
  FROM `app_album_group`
  GROUP BY `name`
) `canonical` ON `canonical`.`name` = `album_group`.`name`;

UPDATE `app_album_asset` `asset`
JOIN `tmp_album_group_mapping` `mapping` ON `mapping`.`old_id` = `asset`.`group_id`
SET `asset`.`group_id` = `mapping`.`canonical_id`;

DELETE `album_group`
FROM `app_album_group` `album_group`
JOIN `tmp_album_group_mapping` `mapping` ON `mapping`.`old_id` = `album_group`.`id`
WHERE `mapping`.`old_id` <> `mapping`.`canonical_id`;

DROP TEMPORARY TABLE `tmp_album_group_mapping`;

ALTER TABLE `app_album_group`
  DROP INDEX `uk_album_group_owner_name`,
  DROP INDEX `idx_album_group_owner_sort`,
  DROP COLUMN `owner_id`,
  ADD UNIQUE KEY `uk_album_group_name` (`name`),
  ADD KEY `idx_album_group_sort` (`sort`);

ALTER TABLE `app_album_tag`
  DROP INDEX `uk_album_tag_owner_name`,
  DROP INDEX `idx_album_tag_owner_sort`,
  DROP COLUMN `owner_id`,
  ADD UNIQUE KEY `uk_album_tag_name` (`name`),
  ADD KEY `idx_album_tag_sort` (`sort`);

INSERT INTO `app_album_group` (`name`, `description`, `sort`)
VALUES ('图片', '图片资源系统分组', 1),
       ('视频', '视频资源系统分组', 2),
       ('音频', '音频资源系统分组', 3)
ON DUPLICATE KEY UPDATE
  `description` = VALUES(`description`),
  `sort` = VALUES(`sort`);

-- 历史资源也按媒体类型回填到系统分组。
UPDATE `app_album_asset` `asset`
JOIN `app_album_group` `album_group`
  ON `album_group`.`name` = CASE `asset`.`media_type`
    WHEN 'IMAGE' THEN '图片'
    WHEN 'VIDEO' THEN '视频'
    WHEN 'AUDIO' THEN '音频'
  END
SET `asset`.`group_id` = `album_group`.`id`;
