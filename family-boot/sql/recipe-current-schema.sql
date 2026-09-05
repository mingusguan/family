-- 食谱模块当前可运行脚本。
-- 用途：
-- 1. 新库：创建当前代码需要的食谱相关表。
-- 2. 已执行旧版 recipe-module.sql 的库：补齐后续新增列与索引。
-- 说明：依赖基础系统表 sys_menu/sys_role/sys_role_menu 已存在。

CREATE TABLE IF NOT EXISTS recipe_source (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  provider VARCHAR(32) NOT NULL COMMENT 'Source provider: JISU/JUMDATA/MANUAL',
  provider_recipe_id VARCHAR(128) DEFAULT NULL COMMENT 'Provider recipe id',
  category_id VARCHAR(128) DEFAULT NULL COMMENT '同步分类标识',
  category_name VARCHAR(255) DEFAULT NULL COMMENT '同步分类名称',
  title VARCHAR(255) NOT NULL COMMENT 'Source recipe title',
  cover_url VARCHAR(2048) DEFAULT NULL COMMENT 'Remote cover image URL',
  raw_payload LONGTEXT DEFAULT NULL COMMENT 'Original provider payload',
  sync_status VARCHAR(32) NOT NULL DEFAULT 'IMPORTED' COMMENT 'Sync status',
  synced_at DATETIME DEFAULT NULL COMMENT 'Last sync time',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_recipe_source_provider (provider, provider_recipe_id),
  KEY idx_recipe_source_title (title),
  KEY idx_recipe_source_category (provider, category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Recipe source pool';

CREATE TABLE IF NOT EXISTS baby_recipe (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  source_id BIGINT DEFAULT NULL COMMENT 'Source recipe id',
  category_id VARCHAR(128) DEFAULT NULL COMMENT '食谱分类标识',
  category_name VARCHAR(255) DEFAULT NULL COMMENT '食谱分类名称',
  title VARCHAR(255) NOT NULL COMMENT 'Recipe title',
  summary TEXT DEFAULT NULL COMMENT 'Recipe summary',
  cover_url VARCHAR(2048) DEFAULT NULL COMMENT 'Remote cover URL',
  audience_type VARCHAR(16) NOT NULL DEFAULT 'INFANT' COMMENT '适用人群：GENERAL/INFANT',
  min_month_age INT DEFAULT NULL COMMENT '婴幼儿最小月龄，普通食谱为空',
  max_month_age INT DEFAULT NULL COMMENT '婴幼儿最大月龄，普通食谱为空',
  texture_type VARCHAR(32) DEFAULT NULL COMMENT '婴幼儿食物性状，普通食谱可为空',
  meal_type VARCHAR(32) DEFAULT NULL COMMENT 'Meal type',
  difficulty VARCHAR(32) DEFAULT NULL COMMENT 'Difficulty',
  cook_time_minutes INT DEFAULT NULL COMMENT 'Cook time in minutes',
  serving_size VARCHAR(64) DEFAULT NULL COMMENT 'Serving size',
  ingredients_json LONGTEXT DEFAULT NULL COMMENT 'Ingredients JSON',
  steps_json LONGTEXT DEFAULT NULL COMMENT 'Cooking steps JSON',
  nutrition_json LONGTEXT DEFAULT NULL COMMENT 'Nutrition summary JSON',
  allergen_tags VARCHAR(500) DEFAULT NULL COMMENT 'Allergen tags',
  risk_tags VARCHAR(500) DEFAULT NULL COMMENT 'Risk tags',
  content_version INT NOT NULL DEFAULT 1 COMMENT '内容版本',
  screened_content_version INT DEFAULT NULL COMMENT '最近筛查的内容版本',
  screening_version VARCHAR(64) DEFAULT NULL COMMENT '最近筛查规则集版本',
  screened_at DATETIME DEFAULT NULL COMMENT '最近筛查时间',
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
  KEY idx_baby_recipe_category (category_id, category_name),
  KEY idx_baby_recipe_audience_status (audience_type, status)
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
  rule_version INT NOT NULL DEFAULT 1 COMMENT '规则版本',
  rule_name VARCHAR(128) NOT NULL COMMENT 'Rule name',
  min_month_age INT NOT NULL DEFAULT 0 COMMENT 'Minimum month age',
  max_month_age INT NOT NULL DEFAULT 36 COMMENT 'Maximum month age',
  match_type VARCHAR(32) NOT NULL COMMENT 'Match type',
  keywords VARCHAR(1000) NOT NULL COMMENT 'Comma separated keywords',
  severity VARCHAR(16) NOT NULL COMMENT 'BLOCK/REVIEW/WARN',
  suggestion VARCHAR(500) DEFAULT NULL COMMENT 'Review suggestion',
  evidence_source VARCHAR(255) DEFAULT NULL COMMENT 'Evidence source',
  evidence_url VARCHAR(1000) DEFAULT NULL COMMENT '权威依据链接',
  effective_date DATE DEFAULT NULL COMMENT '依据生效日期',
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
  rule_version INT NOT NULL DEFAULT 1 COMMENT '命中时规则版本',
  severity VARCHAR(16) NOT NULL COMMENT 'Rule severity',
  matched_text VARCHAR(255) DEFAULT NULL COMMENT 'Matched text',
  suggestion VARCHAR(500) DEFAULT NULL COMMENT 'Suggestion',
  evidence_source VARCHAR(255) DEFAULT NULL COMMENT '命中时依据名称',
  evidence_url VARCHAR(1000) DEFAULT NULL COMMENT '命中时依据链接',
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

CREATE TABLE IF NOT EXISTS recipe_sync_task (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  task_no VARCHAR(64) NOT NULL COMMENT 'Public task number',
  provider VARCHAR(32) NOT NULL COMMENT 'Source provider',
  mode VARCHAR(32) NOT NULL COMMENT 'CATEGORY_COUNT/SELECTED_RECIPES',
  status VARCHAR(24) NOT NULL COMMENT 'PENDING/RUNNING/SUCCEEDED/PARTIAL_FAILED/FAILED',
  request_payload LONGTEXT NOT NULL COMMENT 'Synchronization request snapshot',
  requested_count INT NOT NULL DEFAULT 0 COMMENT '用户请求同步数量',
  total_count INT NOT NULL DEFAULT 0 COMMENT 'Expected work count',
  processed_count INT NOT NULL DEFAULT 0 COMMENT 'Processed remote recipe count',
  imported_count INT NOT NULL DEFAULT 0 COMMENT 'Imported recipe count',
  duplicated_count INT NOT NULL DEFAULT 0 COMMENT 'Skipped local duplicate count',
  blocked_count INT NOT NULL DEFAULT 0 COMMENT 'BLOCK rule hit recipe count',
  warned_count INT NOT NULL DEFAULT 0 COMMENT 'WARN/REVIEW rule hit recipe count',
  failed_count INT NOT NULL DEFAULT 0 COMMENT 'Failed item count',
  error_message VARCHAR(1000) DEFAULT NULL COMMENT 'Task error message',
  failure_details_json LONGTEXT DEFAULT NULL COMMENT '失败菜谱及原因JSON',
  started_at DATETIME DEFAULT NULL COMMENT 'Start time',
  finished_at DATETIME DEFAULT NULL COMMENT 'Finish time',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  PRIMARY KEY (id),
  UNIQUE KEY uk_recipe_sync_task_no (task_no),
  KEY idx_recipe_sync_task_status (status, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Recipe synchronization task';

DROP PROCEDURE IF EXISTS recipe_add_column_if_missing;
DROP PROCEDURE IF EXISTS recipe_add_index_if_missing;

DELIMITER $$
CREATE PROCEDURE recipe_add_column_if_missing(
  IN p_table_name VARCHAR(64),
  IN p_column_name VARCHAR(64),
  IN p_sql TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = p_table_name
      AND column_name = p_column_name
  ) THEN
    SET @recipe_sql = p_sql;
    PREPARE recipe_stmt FROM @recipe_sql;
    EXECUTE recipe_stmt;
    DEALLOCATE PREPARE recipe_stmt;
  END IF;
END$$

CREATE PROCEDURE recipe_add_index_if_missing(
  IN p_table_name VARCHAR(64),
  IN p_index_name VARCHAR(64),
  IN p_sql TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = p_table_name
      AND index_name = p_index_name
  ) THEN
    SET @recipe_sql = p_sql;
    PREPARE recipe_stmt FROM @recipe_sql;
    EXECUTE recipe_stmt;
    DEALLOCATE PREPARE recipe_stmt;
  END IF;
END$$
DELIMITER ;

CALL recipe_add_column_if_missing('recipe_source', 'category_id',
  'ALTER TABLE recipe_source ADD COLUMN category_id VARCHAR(128) DEFAULT NULL COMMENT ''同步分类标识'' AFTER provider_recipe_id');
CALL recipe_add_column_if_missing('recipe_source', 'category_name',
  'ALTER TABLE recipe_source ADD COLUMN category_name VARCHAR(255) DEFAULT NULL COMMENT ''同步分类名称'' AFTER category_id');
CALL recipe_add_index_if_missing('recipe_source', 'idx_recipe_source_category',
  'ALTER TABLE recipe_source ADD KEY idx_recipe_source_category (provider, category_id)');
ALTER TABLE recipe_source MODIFY COLUMN category_name VARCHAR(255) DEFAULT NULL COMMENT '同步分类名称';
ALTER TABLE recipe_source MODIFY COLUMN title VARCHAR(255) NOT NULL COMMENT 'Source recipe title';
ALTER TABLE recipe_source MODIFY COLUMN cover_url VARCHAR(2048) DEFAULT NULL COMMENT 'Remote cover image URL';

CALL recipe_add_column_if_missing('baby_recipe', 'audience_type',
  'ALTER TABLE baby_recipe ADD COLUMN audience_type VARCHAR(16) NOT NULL DEFAULT ''INFANT'' COMMENT ''适用人群：GENERAL/INFANT'' AFTER cover_url');
CALL recipe_add_column_if_missing('baby_recipe', 'category_id',
  'ALTER TABLE baby_recipe ADD COLUMN category_id VARCHAR(128) DEFAULT NULL COMMENT ''食谱分类标识'' AFTER source_id');
CALL recipe_add_column_if_missing('baby_recipe', 'category_name',
  'ALTER TABLE baby_recipe ADD COLUMN category_name VARCHAR(255) DEFAULT NULL COMMENT ''食谱分类名称'' AFTER category_id');
CALL recipe_add_column_if_missing('baby_recipe', 'content_version',
  'ALTER TABLE baby_recipe ADD COLUMN content_version INT NOT NULL DEFAULT 1 COMMENT ''内容版本'' AFTER risk_tags');
CALL recipe_add_column_if_missing('baby_recipe', 'screened_content_version',
  'ALTER TABLE baby_recipe ADD COLUMN screened_content_version INT DEFAULT NULL COMMENT ''最近筛查的内容版本'' AFTER content_version');
CALL recipe_add_column_if_missing('baby_recipe', 'screening_version',
  'ALTER TABLE baby_recipe ADD COLUMN screening_version VARCHAR(64) DEFAULT NULL COMMENT ''最近筛查规则集版本'' AFTER screened_content_version');
CALL recipe_add_column_if_missing('baby_recipe', 'screened_at',
  'ALTER TABLE baby_recipe ADD COLUMN screened_at DATETIME DEFAULT NULL COMMENT ''最近筛查时间'' AFTER screening_version');
ALTER TABLE baby_recipe MODIFY COLUMN title VARCHAR(255) NOT NULL COMMENT 'Recipe title';
ALTER TABLE baby_recipe MODIFY COLUMN summary TEXT DEFAULT NULL COMMENT 'Recipe summary';
ALTER TABLE baby_recipe MODIFY COLUMN cover_url VARCHAR(2048) DEFAULT NULL COMMENT 'Remote cover URL';
ALTER TABLE baby_recipe MODIFY COLUMN category_name VARCHAR(255) DEFAULT NULL COMMENT '食谱分类名称';
ALTER TABLE baby_recipe MODIFY COLUMN min_month_age INT DEFAULT NULL COMMENT '婴幼儿最小月龄，普通食谱为空';
ALTER TABLE baby_recipe MODIFY COLUMN max_month_age INT DEFAULT NULL COMMENT '婴幼儿最大月龄，普通食谱为空';
ALTER TABLE baby_recipe MODIFY COLUMN texture_type VARCHAR(32) DEFAULT NULL COMMENT '婴幼儿食物性状，普通食谱可为空';
CALL recipe_add_index_if_missing('baby_recipe', 'idx_baby_recipe_category',
  'ALTER TABLE baby_recipe ADD KEY idx_baby_recipe_category (category_id, category_name)');
CALL recipe_add_index_if_missing('baby_recipe', 'idx_baby_recipe_audience_status',
  'ALTER TABLE baby_recipe ADD KEY idx_baby_recipe_audience_status (audience_type, status)');

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

UPDATE baby_recipe br
JOIN recipe_source rs ON br.source_id = rs.id
SET br.category_id = CASE
      WHEN br.category_id IS NULL OR br.category_id = '' THEN rs.category_id
      ELSE br.category_id
    END,
    br.category_name = CASE
      WHEN br.category_name IS NULL OR br.category_name = '' THEN rs.category_name
      ELSE br.category_name
    END
WHERE br.source_id IS NOT NULL
  AND (br.category_id IS NULL OR br.category_id = '' OR br.category_name IS NULL OR br.category_name = '');

INSERT IGNORE INTO baby_recipe_category (recipe_id, provider, category_id, category_name, create_time, update_time)
SELECT br.id,
       COALESCE(rs.provider, 'MANUAL'),
       COALESCE(NULLIF(br.category_id, ''), NULLIF(rs.category_id, ''), br.category_name),
       br.category_name,
       NOW(),
       NOW()
FROM baby_recipe br
LEFT JOIN recipe_source rs ON br.source_id = rs.id
WHERE br.category_name IS NOT NULL
  AND br.category_name <> '';

CALL recipe_add_column_if_missing('baby_recipe_rule', 'rule_version',
  'ALTER TABLE baby_recipe_rule ADD COLUMN rule_version INT NOT NULL DEFAULT 1 COMMENT ''规则版本'' AFTER rule_code');
CALL recipe_add_column_if_missing('baby_recipe_rule', 'evidence_url',
  'ALTER TABLE baby_recipe_rule ADD COLUMN evidence_url VARCHAR(1000) DEFAULT NULL COMMENT ''权威依据链接'' AFTER evidence_source');
CALL recipe_add_column_if_missing('baby_recipe_rule', 'effective_date',
  'ALTER TABLE baby_recipe_rule ADD COLUMN effective_date DATE DEFAULT NULL COMMENT ''依据生效日期'' AFTER evidence_url');

CALL recipe_add_column_if_missing('recipe_rule_hit', 'rule_version',
  'ALTER TABLE recipe_rule_hit ADD COLUMN rule_version INT NOT NULL DEFAULT 1 COMMENT ''命中时规则版本'' AFTER rule_code');
CALL recipe_add_column_if_missing('recipe_rule_hit', 'evidence_source',
  'ALTER TABLE recipe_rule_hit ADD COLUMN evidence_source VARCHAR(255) DEFAULT NULL COMMENT ''命中时依据名称'' AFTER suggestion');
CALL recipe_add_column_if_missing('recipe_rule_hit', 'evidence_url',
  'ALTER TABLE recipe_rule_hit ADD COLUMN evidence_url VARCHAR(1000) DEFAULT NULL COMMENT ''命中时依据链接'' AFTER evidence_source');

CALL recipe_add_column_if_missing('recipe_sync_task', 'requested_count',
  'ALTER TABLE recipe_sync_task ADD COLUMN requested_count INT NOT NULL DEFAULT 0 COMMENT ''用户请求同步数量'' AFTER request_payload');
CALL recipe_add_column_if_missing('recipe_sync_task', 'failure_details_json',
  'ALTER TABLE recipe_sync_task ADD COLUMN failure_details_json LONGTEXT DEFAULT NULL COMMENT ''失败菜谱及原因JSON'' AFTER error_message');
UPDATE recipe_sync_task SET requested_count = total_count WHERE requested_count = 0;

INSERT IGNORE INTO baby_recipe_rule
  (rule_code, rule_version, rule_name, min_month_age, max_month_age, match_type, keywords, severity, suggestion, evidence_source, evidence_url, enabled)
VALUES
  ('NO_COMPLEMENTARY_FOOD_BEFORE_6M', 1, '0-5月龄不推荐辅食', 0, 5, 'AGE', '*', 'BLOCK', '6月龄前不展示辅食菜谱，仅展示母乳/配方奶喂养科普。', '国家卫健委托育机构婴幼儿喂养与营养指南', 'https://www.nhc.gov.cn/rkjcyjtfzs/c100147/202201/a7d3fc17153f410ea97270814a3e662f.shtml', 1),
  ('NO_SALT_SUGAR_UNDER_12M', 1, '12月龄以下禁止盐糖调味', 0, 11, 'INGREDIENT', '盐,白糖,冰糖,红糖,酱油,生抽,老抽,蚝油,味精,鸡精', 'BLOCK', '1岁以内辅食应保持原味，不建议添加盐、糖或调味品。', '国家卫健委托育机构婴幼儿喂养与营养指南', 'https://www.nhc.gov.cn/rkjcyjtfzs/c100147/202201/a7d3fc17153f410ea97270814a3e662f.shtml', 1),
  ('NO_HONEY_UNDER_12M', 1, '12月龄以下禁止蜂蜜', 0, 11, 'INGREDIENT', '蜂蜜', 'BLOCK', '1岁以内不建议食用蜂蜜。', '婴幼儿喂养安全常识', NULL, 1),
  ('NO_ALCOHOL', 1, '婴幼儿食谱禁止酒类调味', 0, 36, 'INGREDIENT', '料酒,黄酒,白酒,啤酒,米酒,酒酿', 'BLOCK', '婴幼儿食谱不应使用酒类调味。', '婴幼儿辅食安全规则', NULL, 1),
  ('CHOKING_RISK', 1, '呛噎风险食材需要确认形态', 0, 36, 'INGREDIENT', '整粒坚果,花生,葡萄,小番茄,果冻,珍珠,硬糖', 'REVIEW', '需要确认是否已切碎、煮软或改良处理，避免呛噎风险。', '国家卫健委托育机构婴幼儿喂养与营养指南', 'https://www.nhc.gov.cn/rkjcyjtfzs/c100147/202201/a7d3fc17153f410ea97270814a3e662f.shtml', 1),
  ('ALLERGEN_NOTICE', 1, '常见过敏原提示', 6, 36, 'INGREDIENT', '鸡蛋,蛋清,牛奶,奶酪,小麦,鱼,虾,花生,坚果,大豆', 'WARN', '首次添加常见过敏原时应少量尝试并观察。', '中国居民膳食指南', 'https://dg.cnsoc.org/article/04/Qokq-WkDRZW3nhyFH0qF5A.html', 1);

UPDATE baby_recipe_rule
SET evidence_url = CASE rule_code
  WHEN 'NO_COMPLEMENTARY_FOOD_BEFORE_6M' THEN COALESCE(evidence_url, 'https://www.nhc.gov.cn/rkjcyjtfzs/c100147/202201/a7d3fc17153f410ea97270814a3e662f.shtml')
  WHEN 'NO_SALT_SUGAR_UNDER_12M' THEN COALESCE(evidence_url, 'https://www.nhc.gov.cn/rkjcyjtfzs/c100147/202201/a7d3fc17153f410ea97270814a3e662f.shtml')
  WHEN 'CHOKING_RISK' THEN COALESCE(evidence_url, 'https://www.nhc.gov.cn/rkjcyjtfzs/c100147/202201/a7d3fc17153f410ea97270814a3e662f.shtml')
  WHEN 'ALLERGEN_NOTICE' THEN COALESCE(evidence_url, 'https://dg.cnsoc.org/article/04/Qokq-WkDRZW3nhyFH0qF5A.html')
  ELSE evidence_url
END
WHERE rule_code IN ('NO_COMPLEMENTARY_FOOD_BEFORE_6M', 'NO_SALT_SUGAR_UNDER_12M', 'CHOKING_RISK', 'ALLERGEN_NOTICE');

INSERT IGNORE INTO sys_menu
  (id, parent_id, tree_path, name, type, route_name, route_path, component, perm, always_show, keep_alive, visible, sort, icon, redirect, create_time, update_time, params)
VALUES
  (400, 0, '0', 'APP管理', 'C', 'AppManagement', '/app', 'Layout', NULL, 1, 1, 1, 3, 'Cellphone', '/app/users', NOW(), NOW(), NULL),
  (430, 400, '0,400', '宝宝食谱管理', 'M', 'BabyRecipe', 'recipes', 'recipe/index', NULL, NULL, 1, 1, 3, 'Food', NULL, NOW(), NOW(), NULL),
  (4301, 430, '0,400,430', '食谱查询', 'B', NULL, '', NULL, 'recipe:list', NULL, NULL, 1, 1, '', NULL, NOW(), NOW(), NULL),
  (4302, 430, '0,400,430', '食谱新增', 'B', NULL, '', NULL, 'recipe:create', NULL, NULL, 1, 2, '', NULL, NOW(), NOW(), NULL),
  (4303, 430, '0,400,430', '食谱修改', 'B', NULL, '', NULL, 'recipe:update', NULL, NULL, 1, 3, '', NULL, NOW(), NOW(), NULL),
  (4304, 430, '0,400,430', '食谱审核', 'B', NULL, '', NULL, 'recipe:review', NULL, NULL, 1, 4, '', NULL, NOW(), NOW(), NULL),
  (4305, 430, '0,400,430', '食谱发布', 'B', NULL, '', NULL, 'recipe:publish', NULL, NULL, 1, 5, '', NULL, NOW(), NOW(), NULL),
  (4306, 430, '0,400,430', '极速同步', 'B', NULL, '', NULL, 'recipe:sync', NULL, NULL, 1, 6, '', NULL, NOW(), NOW(), NULL),
  (4307, 430, '0,400,430', '规则管理', 'B', NULL, '', NULL, 'recipe:rule', NULL, NULL, 1, 7, '', NULL, NOW(), NOW(), NULL);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT role.id, menu.id
FROM sys_role role
CROSS JOIN sys_menu menu
WHERE role.code = 'ADMIN'
  AND menu.id IN (400, 430, 4301, 4302, 4303, 4304, 4305, 4306, 4307);

DROP PROCEDURE IF EXISTS recipe_add_column_if_missing;
DROP PROCEDURE IF EXISTS recipe_add_index_if_missing;
