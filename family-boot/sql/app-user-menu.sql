-- APP 用户管理菜单与系统管理员授权
START TRANSACTION;

INSERT INTO `sys_menu` VALUES
(400, 0, '0', 'APP管理', 'C', 'AppManagement', '/app', 'Layout', NULL, 1, 1, 1, 3, 'Cellphone', '/app/users', NOW(), NOW(), NULL),
(410, 400, '0,400', 'APP用户管理', 'M', 'AppUser', 'users', 'app/user/index', NULL, NULL, 1, 1, 1, 'UserFilled', NULL, NOW(), NOW(), NULL),
(4101, 410, '0,400,410', '用户查询', 'B', NULL, '', NULL, 'app:user:list', NULL, NULL, 1, 1, '', NULL, NOW(), NOW(), NULL),
(4102, 410, '0,400,410', '用户状态修改', 'B', NULL, '', NULL, 'app:user:update', NULL, NULL, 1, 2, '', NULL, NOW(), NOW(), NULL),
(4103, 410, '0,400,410', '用户删除', 'B', NULL, '', NULL, 'app:user:delete', NULL, NULL, 1, 3, '', NULL, NOW(), NOW(), NULL);

-- 按角色编码授权，避免不同环境中的角色主键不一致
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT role.id, menu.id
FROM `sys_role` role
CROSS JOIN `sys_menu` menu
WHERE role.code = 'ADMIN'
  AND menu.id IN (400, 410, 4101, 4102, 4103);

COMMIT;
