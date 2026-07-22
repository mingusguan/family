-- 家庭状态卡片、家庭日程及后台菜单。
-- 请在已执行 family-album-upgrade.sql 与 album-module.sql 的数据库中执行。

START TRANSACTION;

CREATE TABLE IF NOT EXISTS `app_family_card_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `scene` VARCHAR(32) NOT NULL COMMENT '场景编码',
  `title_template` VARCHAR(100) NOT NULL COMMENT '标题模板',
  `description_template` VARCHAR(255) NOT NULL COMMENT '描述模板',
  `tag_text` VARCHAR(30) DEFAULT NULL COMMENT '角标文案',
  `icon` VARCHAR(50) DEFAULT NULL COMMENT 'Wot Design图标名',
  `background_url` VARCHAR(1000) DEFAULT NULL COMMENT '默认背景图',
  `action_text` VARCHAR(20) DEFAULT NULL COMMENT '行动文案',
  `priority` INT NOT NULL DEFAULT 0 COMMENT '优先级，越大越优先',
  `window_days` INT NOT NULL DEFAULT 7 COMMENT '场景查询时间窗口（天）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0停用',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_family_card_scene` (`scene`),
  KEY `idx_family_card_priority` (`status`, `priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='家庭状态卡片配置';

CREATE TABLE IF NOT EXISTS `app_family_schedule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日程ID',
  `family_id` BIGINT NOT NULL COMMENT '家庭ID',
  `creator_id` BIGINT NOT NULL COMMENT '创建用户ID',
  `type` VARCHAR(20) NOT NULL COMMENT '类型：PLAN/BIRTHDAY/ANNIVERSARY/FESTIVAL',
  `title` VARCHAR(80) NOT NULL COMMENT '日程标题',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '日程说明',
  `event_time` DATETIME NOT NULL COMMENT '发生时间',
  `repeat_yearly` TINYINT NOT NULL DEFAULT 0 COMMENT '是否每年重复',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1进行中，2已完成',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_family_schedule_active` (`family_id`, `status`, `event_time`),
  KEY `idx_family_schedule_creator` (`creator_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='家庭计划与特殊日期';

CREATE TABLE IF NOT EXISTS `app_family_card_read_state` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '状态ID',
  `user_id` BIGINT NOT NULL COMMENT 'APP用户ID',
  `family_id` BIGINT NOT NULL COMMENT '家庭ID',
  `last_photo_read_at` DATETIME DEFAULT NULL COMMENT '最近查看照片动态时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_family_card_read_user` (`user_id`, `family_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='家庭卡片用户已读状态';

INSERT INTO `app_family_card_config`
(`scene`, `title_template`, `description_template`, `tag_text`, `icon`, `action_text`, `priority`, `window_days`, `status`)
VALUES
('CREATE_FAMILY', '创建我们的家', '一起记录照片、计划和生活点滴', '开始使用', 'home', '立即创建', 100, 7, 1),
('INVITE_MEMBER', '邀请家人加入{familyName}', '家人到齐后，就能一起共享相册和家庭计划', '家庭邀请', 'user', '邀请家人', 90, 7, 1),
('SPECIAL_DATE', '{title}快到了', '距离{date}还有{days}天，别忘了准备一份心意', '重要日子', 'gift', '查看日程', 80, 30, 1),
('UPCOMING_SCHEDULE', '{title}', '安排在{date}，距离现在还有{days}天', '家庭计划', 'calendar', '查看计划', 70, 14, 1),
('RECENT_PHOTO', '家人新增了{count}张照片', '共同的生活正在被好好记录，去看看新回忆吧', '家庭动态', 'image', '去看看', 60, 7, 1),
('TODAY_MEMORY', '家庭时光', '那年今天留下了{count}张照片，再看一次当时的故事', '那年今日', 'time', '查看回忆', 10, 365, 1)
ON DUPLICATE KEY UPDATE
  `title_template` = VALUES(`title_template`),
  `description_template` = VALUES(`description_template`),
  `tag_text` = VALUES(`tag_text`),
  `icon` = VALUES(`icon`),
  `action_text` = VALUES(`action_text`),
  `priority` = VALUES(`priority`),
  `window_days` = VALUES(`window_days`);

-- APP 管理下增加家庭卡片配置菜单。
INSERT IGNORE INTO `sys_menu` VALUES
(400, 0, '0', 'APP管理', 'C', 'AppManagement', '/app', 'Layout', NULL, 1, 1, 1, 3, 'Cellphone', '/app/users', NOW(), NOW(), NULL);

INSERT IGNORE INTO `sys_menu` VALUES
(420, 400, '0,400', '家庭卡片配置', 'M', 'FamilyCardConfig', 'family-cards', 'family/cards/index', NULL, NULL, 1, 1, 2, 'Postcard', NULL, NOW(), NOW(), NULL),
(4201, 420, '0,400,420', '卡片配置查询', 'B', NULL, '', NULL, 'family:card:list', NULL, NULL, 1, 1, '', NULL, NOW(), NOW(), NULL),
(4202, 420, '0,400,420', '卡片配置修改', 'B', NULL, '', NULL, 'family:card:update', NULL, NULL, 1, 2, '', NULL, NOW(), NOW(), NULL);

INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT role.id, menu.id
FROM `sys_role` role
CROSS JOIN `sys_menu` menu
WHERE role.code = 'ADMIN'
  AND menu.id IN (400, 420, 4201, 4202);

COMMIT;
