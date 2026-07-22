-- 相册资源表：保存 APP 上传后的对象存储元数据，不保存文件二进制
CREATE TABLE IF NOT EXISTS `app_album_asset` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '资源ID',
  `uploader_id` BIGINT NOT NULL COMMENT '上传用户ID',
  `media_type` VARCHAR(16) NOT NULL COMMENT '资源类型：IMAGE/VIDEO/AUDIO',
  `url` VARCHAR(1000) NOT NULL COMMENT '资源访问地址',
  `thumbnail_url` VARCHAR(1000) DEFAULT NULL COMMENT '缩略图或视频封面地址',
  `original_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
  `mime_type` VARCHAR(100) DEFAULT NULL COMMENT 'MIME类型',
  `file_size` BIGINT DEFAULT NULL COMMENT '文件大小，单位字节',
  `duration` BIGINT DEFAULT NULL COMMENT '视频或音频时长，单位毫秒',
  `width` INT DEFAULT NULL COMMENT '图片或视频宽度',
  `height` INT DEFAULT NULL COMMENT '图片或视频高度',
  `group_id` BIGINT DEFAULT NULL COMMENT '分组ID',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '资源描述',
  `captured_at` DATETIME DEFAULT NULL COMMENT '拍摄或录制时间',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常，0-隐藏',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-否，1-是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_album_asset_uploader` (`uploader_id`, `create_time`),
  KEY `idx_album_asset_group` (`group_id`),
  KEY `idx_album_asset_type` (`media_type`, `status`),
  KEY `idx_album_asset_captured_at` (`captured_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='APP相册资源';

-- 分组和标签为全局数据，不绑定 APP 用户
CREATE TABLE IF NOT EXISTS `app_album_group` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分组ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分组名称',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '分组描述',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_album_group_name` (`name`),
  KEY `idx_album_group_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='APP相册分组';

CREATE TABLE IF NOT EXISTS `app_album_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name` VARCHAR(30) NOT NULL COMMENT '标签名称',
  `color` VARCHAR(20) DEFAULT NULL COMMENT '标签颜色',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序值',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_album_tag_name` (`name`),
  KEY `idx_album_tag_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='APP相册标签';

-- 资源上传后由后端按媒体类型自动匹配以下系统分组，名称请勿修改
INSERT INTO `app_album_group` (`name`, `description`, `sort`)
VALUES ('图片', '图片资源系统分组', 1),
       ('视频', '视频资源系统分组', 2),
       ('音频', '音频资源系统分组', 3)
ON DUPLICATE KEY UPDATE
  `description` = VALUES(`description`),
  `sort` = VALUES(`sort`);

CREATE TABLE IF NOT EXISTS `app_album_asset_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `asset_id` BIGINT NOT NULL COMMENT '资源ID',
  `tag_id` BIGINT NOT NULL COMMENT '标签ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_album_asset_tag` (`asset_id`, `tag_id`),
  KEY `idx_album_asset_tag_tag` (`tag_id`, `asset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='相册资源标签关系';
