-- APP 用户独立表及存量数据迁移。
-- 新用户从 100000000 起分配 ID，避免与后台 sys_user 的 ID 空间发生碰撞。
CREATE TABLE IF NOT EXISTS `app_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'APP用户ID',
  `username` VARCHAR(64) NOT NULL COMMENT 'APP用户名',
  `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
  `password` VARCHAR(100) DEFAULT NULL COMMENT 'BCrypt加密密码',
  `mobile` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像地址',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-否，1-是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_user_username` (`username`),
  UNIQUE KEY `uk_app_user_mobile` (`mobile`),
  KEY `idx_app_user_status_create_time` (`status`, `create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=100000000 DEFAULT CHARSET=utf8mb4 COMMENT='APP用户表';
-- 兼容已创建 app_user 表的环境，为账号密码注册补充加密密码列。
SET @app_user_password_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'app_user'
    AND COLUMN_NAME = 'password'
);
SET @app_user_password_sql = IF(
  @app_user_password_exists = 0,
  'ALTER TABLE `app_user` ADD COLUMN `password` VARCHAR(100) DEFAULT NULL COMMENT ''BCrypt加密密码'' AFTER `username`',
  'SELECT 1'
);
PREPARE app_user_password_stmt FROM @app_user_password_sql;
EXECUTE app_user_password_stmt;
DEALLOCATE PREPARE app_user_password_stmt;

-- 保留原用户 ID，确保相册资源、分组、标签和微信绑定记录无需重写关联 ID。
INSERT IGNORE INTO `app_user`
  (`id`, `username`, `password`, `nickname`, `mobile`, `avatar`, `status`, `is_deleted`, `create_time`, `update_time`)
SELECT
  u.`id`, u.`username`, u.`password`, u.`nickname`, u.`mobile`, u.`avatar`, u.`status`, u.`is_deleted`,
  COALESCE(u.`create_time`, CURRENT_TIMESTAMP), COALESCE(u.`update_time`, CURRENT_TIMESTAMP)
FROM `sys_user` u
INNER JOIN `sys_user_role` ur ON ur.`user_id` = u.`id`
INNER JOIN `sys_role` r ON r.`id` = ur.`role_id`
WHERE r.`code` = 'GUEST';

-- 只删除迁移成功且仍具有 GUEST 角色的系统用户，避免重复执行脚本时误删同 ID 管理员。
DELETE u
FROM `sys_user` u
INNER JOIN `sys_user_role` ur ON ur.`user_id` = u.`id`
INNER JOIN `sys_role` r ON r.`id` = ur.`role_id` AND r.`code` = 'GUEST'
INNER JOIN `app_user` au ON au.`id` = u.`id`;

-- APP 用户迁移完成后移除旧角色关系，实现两个用户域的数据隔离。
DELETE ur
FROM `sys_user_role` ur
INNER JOIN `sys_role` r ON r.`id` = ur.`role_id`
INNER JOIN `app_user` au ON au.`id` = ur.`user_id`
WHERE r.`code` = 'GUEST';

ALTER TABLE `app_user` AUTO_INCREMENT = 100000000;
