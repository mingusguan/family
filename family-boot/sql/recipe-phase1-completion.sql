-- 食谱一期完整化增量迁移，适用于已执行 recipe-module.sql 的数据库，仅执行一次。
ALTER TABLE recipe_source
  ADD COLUMN category_id VARCHAR(128) DEFAULT NULL COMMENT '同步分类标识' AFTER provider_recipe_id,
  ADD COLUMN category_name VARCHAR(255) DEFAULT NULL COMMENT '同步分类名称' AFTER category_id,
  ADD KEY idx_recipe_source_category (provider, category_id);

ALTER TABLE baby_recipe
  MODIFY COLUMN min_month_age INT DEFAULT NULL COMMENT '婴幼儿最小月龄，普通食谱为空',
  MODIFY COLUMN max_month_age INT DEFAULT NULL COMMENT '婴幼儿最大月龄，普通食谱为空',
  MODIFY COLUMN texture_type VARCHAR(32) DEFAULT NULL COMMENT '婴幼儿食物性状，普通食谱可为空',
  ADD COLUMN audience_type VARCHAR(16) NOT NULL DEFAULT 'INFANT' COMMENT '适用人群：GENERAL/INFANT' AFTER cover_url,
  ADD COLUMN content_version INT NOT NULL DEFAULT 1 COMMENT '内容版本' AFTER risk_tags,
  ADD COLUMN screened_content_version INT DEFAULT NULL COMMENT '最近筛查的内容版本' AFTER content_version,
  ADD COLUMN screening_version VARCHAR(64) DEFAULT NULL COMMENT '最近筛查规则集版本' AFTER screened_content_version,
  ADD COLUMN screened_at DATETIME DEFAULT NULL COMMENT '最近筛查时间' AFTER screening_version,
  ADD KEY idx_baby_recipe_audience_status (audience_type, status);

ALTER TABLE baby_recipe_rule
  ADD COLUMN rule_version INT NOT NULL DEFAULT 1 COMMENT '规则版本' AFTER rule_code,
  ADD COLUMN evidence_url VARCHAR(1000) DEFAULT NULL COMMENT '权威依据链接' AFTER evidence_source,
  ADD COLUMN effective_date DATE DEFAULT NULL COMMENT '依据生效日期' AFTER evidence_url;

ALTER TABLE recipe_rule_hit
  ADD COLUMN rule_version INT NOT NULL DEFAULT 1 COMMENT '命中时规则版本' AFTER rule_code,
  ADD COLUMN evidence_source VARCHAR(255) DEFAULT NULL COMMENT '命中时依据名称' AFTER suggestion,
  ADD COLUMN evidence_url VARCHAR(1000) DEFAULT NULL COMMENT '命中时依据链接' AFTER evidence_source;

ALTER TABLE recipe_sync_task
  ADD COLUMN requested_count INT NOT NULL DEFAULT 0 COMMENT '用户请求同步数量' AFTER request_payload,
  ADD COLUMN failure_details_json LONGTEXT DEFAULT NULL COMMENT '失败菜谱及原因JSON' AFTER error_message;
UPDATE recipe_sync_task SET requested_count = total_count WHERE requested_count = 0;

INSERT IGNORE INTO sys_menu
  (id, parent_id, tree_path, name, type, route_name, route_path, component, perm, always_show, keep_alive, visible, sort, icon, redirect, create_time, update_time, params)
VALUES
  (4307, 430, '0,400,430', '规则管理', 'B', NULL, '', NULL, 'recipe:rule', NULL, NULL, 1, 7, '', NULL, NOW(), NOW(), NULL);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT role.id, menu.id FROM sys_role role CROSS JOIN sys_menu menu
WHERE role.code = 'ADMIN' AND menu.id IN (4307);

UPDATE baby_recipe_rule SET evidence_url = CASE rule_code
  WHEN 'NO_COMPLEMENTARY_FOOD_BEFORE_6M' THEN 'https://www.nhc.gov.cn/rkjcyjtfzs/c100147/202201/a7d3fc17153f410ea97270814a3e662f.shtml'
  WHEN 'NO_SALT_SUGAR_UNDER_12M' THEN 'https://www.nhc.gov.cn/rkjcyjtfzs/c100147/202201/a7d3fc17153f410ea97270814a3e662f.shtml'
  WHEN 'CHOKING_RISK' THEN 'https://www.nhc.gov.cn/rkjcyjtfzs/c100147/202201/a7d3fc17153f410ea97270814a3e662f.shtml'
  WHEN 'ALLERGEN_NOTICE' THEN 'https://dg.cnsoc.org/article/04/Qokq-WkDRZW3nhyFH0qF5A.html'
  ELSE evidence_url END;
