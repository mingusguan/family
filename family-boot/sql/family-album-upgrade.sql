-- 家庭、家庭成员、用户相册以及资源家庭归属升级脚本。
CREATE TABLE IF NOT EXISTS `app_family` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '家庭ID',
  `name` VARCHAR(50) NOT NULL COMMENT '家庭名称',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '家庭介绍',
  `cover_url` VARCHAR(1000) DEFAULT NULL COMMENT '家庭封面',
  `creator_id` BIGINT NOT NULL COMMENT '创建用户ID',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_family_creator` (`creator_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='APP家庭';

CREATE TABLE IF NOT EXISTS `app_family_member` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '成员关系ID',
  `family_id` BIGINT NOT NULL COMMENT '家庭ID',
  `user_id` BIGINT NOT NULL COMMENT 'APP用户ID',
  `role` VARCHAR(16) NOT NULL DEFAULT 'MEMBER' COMMENT '角色：OWNER/MEMBER',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_family_member` (`family_id`, `user_id`),
  KEY `idx_family_member_user` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='APP家庭成员';

CREATE TABLE IF NOT EXISTS `app_family_album` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '相册ID',
  `family_id` BIGINT NOT NULL COMMENT '家庭ID',
  `name` VARCHAR(50) NOT NULL COMMENT '相册名称',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '相册介绍',
  `cover_url` VARCHAR(1000) DEFAULT NULL COMMENT '相册封面',
  `creator_id` BIGINT NOT NULL COMMENT '创建用户ID',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_family_album_name` (`family_id`, `name`),
  KEY `idx_family_album_family` (`family_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='APP家庭相册';

SET @asset_family_column = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'app_album_asset' AND COLUMN_NAME = 'family_id'
);
SET @asset_family_sql = IF(
  @asset_family_column = 0,
  'ALTER TABLE `app_album_asset` ADD COLUMN `family_id` BIGINT DEFAULT NULL COMMENT ''所属家庭ID'' AFTER `uploader_id`',
  'SELECT 1'
);
PREPARE asset_family_stmt FROM @asset_family_sql;
EXECUTE asset_family_stmt;
DEALLOCATE PREPARE asset_family_stmt;

SET @asset_album_column = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'app_album_asset' AND COLUMN_NAME = 'album_id'
);
SET @asset_album_sql = IF(
  @asset_album_column = 0,
  'ALTER TABLE `app_album_asset` ADD COLUMN `album_id` BIGINT DEFAULT NULL COMMENT ''所属相册ID'' AFTER `family_id`',
  'SELECT 1'
);
PREPARE asset_album_stmt FROM @asset_album_sql;
EXECUTE asset_album_stmt;
DEALLOCATE PREPARE asset_album_stmt;

SET @asset_family_album_index = (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'app_album_asset' AND INDEX_NAME = 'idx_album_asset_family_album'
);
SET @asset_family_album_sql = IF(
  @asset_family_album_index = 0,
  'CREATE INDEX `idx_album_asset_family_album` ON `app_album_asset` (`family_id`, `album_id`, `create_time`)',
  'SELECT 1'
);
PREPARE asset_family_album_stmt FROM @asset_family_album_sql;
EXECUTE asset_family_album_stmt;
DEALLOCATE PREPARE asset_family_album_stmt;
CREATE TABLE IF NOT EXISTS `app_family_invite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '家庭邀请ID',
  `family_id` BIGINT NOT NULL COMMENT '家庭ID',
  `inviter_id` BIGINT NOT NULL COMMENT '邀请人APP用户ID',
  `code` VARCHAR(64) NOT NULL COMMENT '随机邀请码',
  `expires_at` DATETIME NOT NULL COMMENT '邀请过期时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_family_invite_code` (`code`),
  KEY `idx_family_invite_owner` (`family_id`, `inviter_id`, `expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='APP家庭分享邀请';