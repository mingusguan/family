-- Switch recipe sync menu label from Jumdata to Jisu.
-- Existing JUMDATA recipe_source rows are retained for historical data.

UPDATE sys_menu
SET name = '极速同步',
    update_time = NOW()
WHERE id = 4306
  AND perm = 'recipe:sync';
