-- 食谱分类字段升级脚本。
-- 用途：给 baby_recipe 增加系统内分类字段，并从 recipe_source 回填已同步菜谱分类。

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

CALL recipe_add_column_if_missing('baby_recipe', 'category_id',
  'ALTER TABLE baby_recipe ADD COLUMN category_id VARCHAR(128) DEFAULT NULL COMMENT ''食谱分类标识'' AFTER source_id');
CALL recipe_add_column_if_missing('baby_recipe', 'category_name',
  'ALTER TABLE baby_recipe ADD COLUMN category_name VARCHAR(255) DEFAULT NULL COMMENT ''食谱分类名称'' AFTER category_id');
ALTER TABLE baby_recipe MODIFY COLUMN category_name VARCHAR(255) DEFAULT NULL COMMENT '食谱分类名称';
CALL recipe_add_index_if_missing('baby_recipe', 'idx_baby_recipe_category',
  'ALTER TABLE baby_recipe ADD KEY idx_baby_recipe_category (category_id, category_name)');

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

DROP PROCEDURE IF EXISTS recipe_add_column_if_missing;
DROP PROCEDURE IF EXISTS recipe_add_index_if_missing;
