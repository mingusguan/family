-- 删除第三方身份绑定表中与 app_user 重复的资料字段。
-- 用户昵称和头像统一由 app_user 维护，sys_user_social 只保存第三方身份标识。
SET @social_nickname_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sys_user_social'
    AND COLUMN_NAME = 'nickname'
);
SET @drop_social_nickname_sql = IF(
  @social_nickname_exists > 0,
  'ALTER TABLE `sys_user_social` DROP COLUMN `nickname`',
  'SELECT 1'
);
PREPARE drop_social_nickname_stmt FROM @drop_social_nickname_sql;
EXECUTE drop_social_nickname_stmt;
DEALLOCATE PREPARE drop_social_nickname_stmt;

SET @social_avatar_exists = (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sys_user_social'
    AND COLUMN_NAME = 'avatar'
);
SET @drop_social_avatar_sql = IF(
  @social_avatar_exists > 0,
  'ALTER TABLE `sys_user_social` DROP COLUMN `avatar`',
  'SELECT 1'
);
PREPARE drop_social_avatar_stmt FROM @drop_social_avatar_sql;
EXECUTE drop_social_avatar_stmt;
DEALLOCATE PREPARE drop_social_avatar_stmt;
