CREATE TABLE IF NOT EXISTS recipe_source (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  provider VARCHAR(32) NOT NULL COMMENT 'Source provider: JISU/JUMDATA/MANUAL',
  provider_recipe_id VARCHAR(128) DEFAULT NULL COMMENT 'Provider recipe id',
  title VARCHAR(255) NOT NULL COMMENT 'Source recipe title',
  cover_url VARCHAR(2048) DEFAULT NULL COMMENT 'Remote cover image URL',
  raw_payload LONGTEXT DEFAULT NULL COMMENT 'Original provider payload',
  sync_status VARCHAR(32) NOT NULL DEFAULT 'IMPORTED' COMMENT 'Sync status',
  synced_at DATETIME DEFAULT NULL COMMENT 'Last sync time',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_recipe_source_provider (provider, provider_recipe_id),
  KEY idx_recipe_source_title (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Recipe source pool';

CREATE TABLE IF NOT EXISTS baby_recipe (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  source_id BIGINT DEFAULT NULL COMMENT 'Source recipe id',
  category_id VARCHAR(128) DEFAULT NULL COMMENT '食谱分类标识',
  category_name VARCHAR(255) DEFAULT NULL COMMENT '食谱分类名称',
  title VARCHAR(255) NOT NULL COMMENT 'Recipe title',
  summary TEXT DEFAULT NULL COMMENT 'Recipe summary',
  cover_url VARCHAR(2048) DEFAULT NULL COMMENT 'Remote cover URL',
  min_month_age INT NOT NULL COMMENT 'Minimum month age',
  max_month_age INT NOT NULL COMMENT 'Maximum month age',
  texture_type VARCHAR(32) NOT NULL COMMENT 'Food texture type',
  meal_type VARCHAR(32) DEFAULT NULL COMMENT 'Meal type',
  difficulty VARCHAR(32) DEFAULT NULL COMMENT 'Difficulty',
  cook_time_minutes INT DEFAULT NULL COMMENT 'Cook time in minutes',
  serving_size VARCHAR(64) DEFAULT NULL COMMENT 'Serving size',
  ingredients_json LONGTEXT DEFAULT NULL COMMENT 'Ingredients JSON',
  steps_json LONGTEXT DEFAULT NULL COMMENT 'Cooking steps JSON',
  nutrition_json LONGTEXT DEFAULT NULL COMMENT 'Nutrition summary JSON',
  allergen_tags VARCHAR(500) DEFAULT NULL COMMENT 'Allergen tags',
  risk_tags VARCHAR(500) DEFAULT NULL COMMENT 'Risk tags',
  status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT 'Publish status',
  reviewer_id BIGINT DEFAULT NULL COMMENT 'Reviewer user id',
  reviewed_at DATETIME DEFAULT NULL COMMENT 'Review time',
  published_at DATETIME DEFAULT NULL COMMENT 'Publish time',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete flag',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (id),
  KEY idx_baby_recipe_status_age (status, min_month_age, max_month_age),
  KEY idx_baby_recipe_source (source_id),
  KEY idx_baby_recipe_category (category_id, category_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Reviewed baby recipe';

CREATE TABLE IF NOT EXISTS baby_recipe_category (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  recipe_id BIGINT NOT NULL COMMENT 'Recipe id',
  provider VARCHAR(32) NOT NULL COMMENT 'Source provider: JISU/JUMDATA/MANUAL',
  category_id VARCHAR(128) DEFAULT NULL COMMENT '分类标识',
  category_name VARCHAR(255) NOT NULL COMMENT '分类名称',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_baby_recipe_category (recipe_id, provider, category_id),
  KEY idx_baby_recipe_category_name (category_name),
  KEY idx_baby_recipe_category_recipe (recipe_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Baby recipe category relation';

CREATE TABLE IF NOT EXISTS baby_recipe_rule (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  rule_code VARCHAR(64) NOT NULL COMMENT 'Rule code',
  rule_name VARCHAR(128) NOT NULL COMMENT 'Rule name',
  min_month_age INT NOT NULL DEFAULT 0 COMMENT 'Minimum month age',
  max_month_age INT NOT NULL DEFAULT 36 COMMENT 'Maximum month age',
  match_type VARCHAR(32) NOT NULL COMMENT 'Match type',
  keywords VARCHAR(1000) NOT NULL COMMENT 'Comma separated keywords',
  severity VARCHAR(16) NOT NULL COMMENT 'BLOCK/REVIEW/WARN',
  suggestion VARCHAR(500) DEFAULT NULL COMMENT 'Review suggestion',
  evidence_source VARCHAR(255) DEFAULT NULL COMMENT 'Evidence source',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT 'Enabled flag',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_baby_recipe_rule_code (rule_code),
  KEY idx_baby_recipe_rule_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Baby recipe safety rule';

CREATE TABLE IF NOT EXISTS recipe_rule_hit (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  source_id BIGINT DEFAULT NULL COMMENT 'Source recipe id',
  recipe_id BIGINT DEFAULT NULL COMMENT 'Reviewed recipe id',
  rule_code VARCHAR(64) NOT NULL COMMENT 'Rule code',
  severity VARCHAR(16) NOT NULL COMMENT 'Rule severity',
  matched_text VARCHAR(255) DEFAULT NULL COMMENT 'Matched text',
  suggestion VARCHAR(500) DEFAULT NULL COMMENT 'Suggestion',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (id),
  KEY idx_recipe_rule_hit_source (source_id),
  KEY idx_recipe_rule_hit_recipe (recipe_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Recipe rule hit record';

CREATE TABLE IF NOT EXISTS recipe_favorite (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  user_id BIGINT NOT NULL COMMENT 'App user id',
  recipe_id BIGINT NOT NULL COMMENT 'Recipe id',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_recipe_favorite_user_recipe (user_id, recipe_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Recipe favorite';

CREATE TABLE IF NOT EXISTS recipe_cooked_record (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  user_id BIGINT NOT NULL COMMENT 'App user id',
  recipe_id BIGINT NOT NULL COMMENT 'Recipe id',
  cooked_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Cooked time',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (id),
  KEY idx_recipe_cooked_user_recipe (user_id, recipe_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Recipe cooked record';

INSERT INTO baby_recipe_rule (rule_code, rule_name, min_month_age, max_month_age, match_type, keywords, severity, suggestion, evidence_source)
VALUES
('NO_COMPLEMENTARY_FOOD_BEFORE_6M', '0-5月龄不推荐辅食', 0, 5, 'AGE', '*', 'BLOCK', '6月龄前不展示辅食菜谱，仅展示母乳/配方奶喂养科普。', '国家卫健委托育机构婴幼儿喂养与营养指南'),
('NO_SALT_SUGAR_UNDER_12M', '12月龄以下禁止盐糖调味', 0, 11, 'INGREDIENT', '盐,白糖,冰糖,红糖,酱油,生抽,老抽,蚝油,味精,鸡精', 'BLOCK', '1岁以内辅食应保持原味，不建议添加盐、糖或调味品。', '国家卫健委托育机构婴幼儿喂养与营养指南'),
('NO_HONEY_UNDER_12M', '12月龄以下禁止蜂蜜', 0, 11, 'INGREDIENT', '蜂蜜', 'BLOCK', '1岁以内不建议食用蜂蜜。', '婴幼儿喂养安全常识'),
('NO_ALCOHOL', '婴幼儿食谱禁止酒类调味', 0, 36, 'INGREDIENT', '料酒,黄酒,白酒,啤酒,米酒,酒酿', 'BLOCK', '婴幼儿食谱不应使用酒类调味。', '婴幼儿辅食安全规则'),
('CHOKING_RISK', '呛噎风险食材需要确认形态', 0, 36, 'INGREDIENT', '整粒坚果,花生,葡萄,小番茄,果冻,珍珠,硬糖', 'REVIEW', '需要确认是否已切碎、煮软或改良处理，避免呛噎风险。', '国家卫健委托育机构婴幼儿喂养与营养指南'),
('ALLERGEN_NOTICE', '常见过敏原提示', 6, 36, 'INGREDIENT', '鸡蛋,蛋清,牛奶,奶酪,小麦,鱼,虾,花生,坚果,大豆', 'WARN', '首次添加常见过敏原时应少量尝试并观察。', '中国居民膳食指南')
ON DUPLICATE KEY UPDATE
  rule_name = VALUES(rule_name),
  min_month_age = VALUES(min_month_age),
  max_month_age = VALUES(max_month_age),
  match_type = VALUES(match_type),
  keywords = VALUES(keywords),
  severity = VALUES(severity),
  suggestion = VALUES(suggestion),
  evidence_source = VALUES(evidence_source),
  enabled = 1;

START TRANSACTION;

-- APP 管理下增加宝宝食谱管理菜单与按钮权限。
INSERT IGNORE INTO `sys_menu` VALUES
(400, 0, '0', 'APP管理', 'C', 'AppManagement', '/app', 'Layout', NULL, 1, 1, 1, 3, 'Cellphone', '/app/users', NOW(), NOW(), NULL);

INSERT IGNORE INTO `sys_menu` VALUES
(430, 400, '0,400', '宝宝食谱管理', 'M', 'BabyRecipe', 'recipes', 'recipe/index', NULL, NULL, 1, 1, 3, 'Food', NULL, NOW(), NOW(), NULL),
(4301, 430, '0,400,430', '食谱查询', 'B', NULL, '', NULL, 'recipe:list', NULL, NULL, 1, 1, '', NULL, NOW(), NOW(), NULL),
(4302, 430, '0,400,430', '食谱新增', 'B', NULL, '', NULL, 'recipe:create', NULL, NULL, 1, 2, '', NULL, NOW(), NOW(), NULL),
(4303, 430, '0,400,430', '食谱修改', 'B', NULL, '', NULL, 'recipe:update', NULL, NULL, 1, 3, '', NULL, NOW(), NOW(), NULL),
(4304, 430, '0,400,430', '食谱审核', 'B', NULL, '', NULL, 'recipe:review', NULL, NULL, 1, 4, '', NULL, NOW(), NOW(), NULL),
(4305, 430, '0,400,430', '食谱发布', 'B', NULL, '', NULL, 'recipe:publish', NULL, NULL, 1, 5, '', NULL, NOW(), NOW(), NULL),
(4306, 430, '0,400,430', '极速同步', 'B', NULL, '', NULL, 'recipe:sync', NULL, NULL, 1, 6, '', NULL, NOW(), NOW(), NULL);

INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT role.id, menu.id
FROM `sys_role` role
CROSS JOIN `sys_menu` menu
WHERE role.code = 'ADMIN'
  AND menu.id IN (400, 430, 4301, 4302, 4303, 4304, 4305, 4306);


CREATE TABLE IF NOT EXISTS recipe_sync_task (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  task_no VARCHAR(64) NOT NULL COMMENT 'Public task number',
  provider VARCHAR(32) NOT NULL COMMENT 'Source provider',
  mode VARCHAR(32) NOT NULL COMMENT 'CATEGORY_COUNT/SELECTED_RECIPES',
  status VARCHAR(24) NOT NULL COMMENT 'PENDING/RUNNING/SUCCEEDED/PARTIAL_FAILED/FAILED',
  request_payload LONGTEXT NOT NULL COMMENT 'Synchronization request snapshot',
  total_count INT NOT NULL DEFAULT 0 COMMENT 'Expected work count',
  processed_count INT NOT NULL DEFAULT 0 COMMENT 'Processed remote recipe count',
  imported_count INT NOT NULL DEFAULT 0 COMMENT 'Imported recipe count',
  duplicated_count INT NOT NULL DEFAULT 0 COMMENT 'Skipped local duplicate count',
  blocked_count INT NOT NULL DEFAULT 0 COMMENT 'BLOCK rule hit recipe count',
  warned_count INT NOT NULL DEFAULT 0 COMMENT 'WARN/REVIEW rule hit recipe count',
  failed_count INT NOT NULL DEFAULT 0 COMMENT 'Failed item count',
  error_message VARCHAR(1000) DEFAULT NULL COMMENT 'Task error message',
  started_at DATETIME DEFAULT NULL COMMENT 'Start time',
  finished_at DATETIME DEFAULT NULL COMMENT 'Finish time',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_recipe_sync_task_no (task_no),
  KEY idx_recipe_sync_task_status (status, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Recipe synchronization task';
COMMIT;
