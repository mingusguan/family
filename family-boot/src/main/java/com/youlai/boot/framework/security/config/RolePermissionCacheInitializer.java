package com.youlai.boot.framework.security.config;

import com.youlai.boot.system.service.RoleMenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 角色权限缓存初始化器。
 *
 * <p>菜单和角色授权可能通过 SQL 脚本直接写入数据库，此时不会触发业务层的缓存刷新。
 * 应用启动完成后以数据库为准重建角色权限缓存，防止 Redis 中的旧权限导致误判。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RolePermissionCacheInitializer {

    private final RoleMenuService roleMenuService;

    @EventListener(ApplicationReadyEvent.class)
    public void refreshRolePermissionCache() {
        roleMenuService.refreshRolePermsCache();
        log.info("应用启动完成，角色权限缓存已与数据库同步");
    }
}
