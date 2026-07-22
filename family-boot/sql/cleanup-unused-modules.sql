-- 清理现有数据库中的非系统菜单、菜单授权和代码生成表。
-- 首页由前端静态路由提供，不存储在 sys_menu 中。

START TRANSACTION;

DELETE role_menu
FROM sys_role_menu role_menu
INNER JOIN sys_menu menu ON menu.id = role_menu.menu_id
WHERE menu.id IN (2, 4, 5, 6, 7, 8, 9)
   OR menu.tree_path REGEXP '^0,(2|4|5|6|7|8|9)(,|$)';

DELETE FROM sys_menu
WHERE id IN (2, 4, 5, 6, 7, 8, 9)
   OR tree_path REGEXP '^0,(2|4|5|6|7|8|9)(,|$)';

COMMIT;

DROP TABLE IF EXISTS gen_table_column;
DROP TABLE IF EXISTS gen_table;
