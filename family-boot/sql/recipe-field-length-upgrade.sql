-- 食谱字段长度升级脚本。
-- 用途：已建库环境执行一次，避免第三方菜谱摘要、封面地址等字段过长导致同步失败。

ALTER TABLE recipe_source
  MODIFY COLUMN category_name VARCHAR(255) DEFAULT NULL COMMENT '同步分类名称',
  MODIFY COLUMN title VARCHAR(255) NOT NULL COMMENT 'Source recipe title',
  MODIFY COLUMN cover_url VARCHAR(2048) DEFAULT NULL COMMENT 'Remote cover image URL';

ALTER TABLE baby_recipe
  MODIFY COLUMN title VARCHAR(255) NOT NULL COMMENT 'Recipe title',
  MODIFY COLUMN summary TEXT DEFAULT NULL COMMENT 'Recipe summary',
  MODIFY COLUMN cover_url VARCHAR(2048) DEFAULT NULL COMMENT 'Remote cover URL';
