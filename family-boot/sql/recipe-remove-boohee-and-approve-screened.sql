-- Remove the deprecated Boohee nutrition-analysis permission and approve recipes
-- that were only waiting because of automatic rule screening.

START TRANSACTION;

UPDATE baby_recipe
SET status = 'APPROVED',
    reviewed_at = COALESCE(reviewed_at, NOW()),
    update_time = NOW()
WHERE status IN ('PENDING_REVIEW', 'AUTO_REJECTED')
  AND is_deleted = 0;

DELETE FROM sys_role_menu
WHERE menu_id IN (
  SELECT id FROM sys_menu WHERE id = 4308 OR perm = 'recipe:nutrition'
);

DELETE FROM sys_menu
WHERE id = 4308
   OR perm = 'recipe:nutrition';

COMMIT;
