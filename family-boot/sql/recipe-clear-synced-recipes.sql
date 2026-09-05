-- Clear recipes imported from third-party sync providers before re-syncing.
--
-- Default scope: JUMDATA + JISU.
-- Manual recipes are retained.
-- To clear only old Jumdata data, change the two provider filters to:
--   provider IN ('JUMDATA')

START TRANSACTION;

DROP TEMPORARY TABLE IF EXISTS recipe_sync_cleanup_source_ids;
DROP TEMPORARY TABLE IF EXISTS recipe_sync_cleanup_recipe_ids;

CREATE TEMPORARY TABLE recipe_sync_cleanup_source_ids (
  source_id BIGINT PRIMARY KEY
) ENGINE=MEMORY;

CREATE TEMPORARY TABLE recipe_sync_cleanup_recipe_ids (
  recipe_id BIGINT PRIMARY KEY
) ENGINE=MEMORY;

INSERT IGNORE INTO recipe_sync_cleanup_source_ids (source_id)
SELECT id
FROM recipe_source
WHERE provider IN ('JUMDATA', 'JISU');

INSERT IGNORE INTO recipe_sync_cleanup_recipe_ids (recipe_id)
SELECT br.id
FROM baby_recipe br
JOIN recipe_sync_cleanup_source_ids source_ids ON source_ids.source_id = br.source_id;

SELECT
  (SELECT COUNT(*) FROM recipe_sync_cleanup_source_ids) AS source_count,
  (SELECT COUNT(*) FROM recipe_sync_cleanup_recipe_ids) AS recipe_count;

DELETE FROM recipe_favorite
WHERE recipe_id IN (
  SELECT recipe_id FROM recipe_sync_cleanup_recipe_ids
);

DELETE FROM recipe_cooked_record
WHERE recipe_id IN (
  SELECT recipe_id FROM recipe_sync_cleanup_recipe_ids
);

DELETE FROM recipe_rule_hit
WHERE recipe_id IN (
  SELECT recipe_id FROM recipe_sync_cleanup_recipe_ids
)
OR source_id IN (
  SELECT source_id FROM recipe_sync_cleanup_source_ids
);

DELETE FROM baby_recipe
WHERE id IN (
  SELECT recipe_id FROM recipe_sync_cleanup_recipe_ids
);

DELETE FROM recipe_source
WHERE id IN (
  SELECT source_id FROM recipe_sync_cleanup_source_ids
);

DELETE FROM recipe_sync_task
WHERE provider IN ('JUMDATA', 'JISU');

DROP TEMPORARY TABLE IF EXISTS recipe_sync_cleanup_recipe_ids;
DROP TEMPORARY TABLE IF EXISTS recipe_sync_cleanup_source_ids;

COMMIT;
