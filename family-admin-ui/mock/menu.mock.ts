import { defineMock } from "./base";

const systemChildren = [
  ["210", "用户管理", "User", "user", "system/user/index", "el-icon-User"],
  ["220", "角色管理", "Role", "role", "system/role/index", "role"],
  ["230", "菜单管理", "SysMenu", "menu", "system/menu/index", "menu"],
  ["240", "部门管理", "Dept", "dept", "system/dept/index", "tree"],
  ["250", "字典管理", "Dict", "dict", "system/dict/index", "dict"],
  ["251", "字典项", "DictItem", "dict-item", "system/dict/dict-item", ""],
  ["260", "系统日志", "Log", "log", "system/log/index", "document"],
  ["270", "系统配置", "Config", "config", "system/config/index", "setting"],
  ["280", "通知公告", "Notice", "notice", "system/notice/index", ""],
] as const;

const systemRoutes = [
  {
    path: "/system",
    component: "Layout",
    redirect: "/system/user",
    name: "/system",
    meta: {
      title: "系统管理",
      icon: "system",
      hidden: false,
      alwaysShow: false,
      params: null,
    },
    children: systemChildren.map(([, title, name, path, component, icon]) => ({
      path,
      component,
      name,
      meta: {
        title,
        icon,
        hidden: name === "DictItem",
        keepAlive: true,
        alwaysShow: false,
        params: null,
      },
    })),
  },
];

const systemMenus = [
  {
    id: "1",
    parentId: "0",
    name: "系统管理",
    type: "C",
    routeName: "",
    routePath: "/system",
    component: "Layout",
    perm: null,
    visible: 1,
    sort: 1,
    icon: "system",
    redirect: "/system/user",
    keepAlive: null,
    alwaysShow: null,
    params: null,
    children: systemChildren.map(([id, name, routeName, routePath, component, icon], index) => ({
      id,
      parentId: "1",
      name,
      type: "M",
      routeName,
      routePath,
      component,
      perm: null,
      visible: routeName === "DictItem" ? 0 : 1,
      sort: index + 1,
      icon,
      redirect: null,
      keepAlive: 1,
      alwaysShow: null,
      params: null,
    })),
  },
];

const menuMap = Object.fromEntries([
  [systemMenus[0].id, systemMenus[0]],
  ...systemMenus[0].children.map((menu) => [menu.id, menu]),
]);

export default defineMock([
  {
    url: "menus/routes",
    method: ["GET"],
    body: {
      code: "00000",
      data: systemRoutes,
      msg: "一切ok",
    },
  },
  {
    url: "menus",
    method: ["GET"],
    body: {
      code: "00000",
      data: systemMenus,
      msg: "一切ok",
    },
  },
  {
    url: "menus/options",
    method: ["GET"],
    body: {
      code: "00000",
      data: [
        {
          value: "1",
          label: "系统管理",
          children: systemChildren.map(([id, name]) => ({ value: id, label: name })),
        },
      ],
      msg: "一切ok",
    },
  },
  {
    url: "menus",
    method: ["POST"],
    body({ body }) {
      return {
        code: "00000",
        data: null,
        msg: `新增菜单${body.name}成功`,
      };
    },
  },
  {
    url: "menus/:id/form",
    method: ["GET"],
    body({ params }) {
      return {
        code: "00000",
        data: menuMap[params.id] ?? null,
        msg: "一切ok",
      };
    },
  },
  {
    url: "menus/:id",
    method: ["PUT"],
    body({ body }) {
      return {
        code: "00000",
        data: null,
        msg: `修改菜单${body.name}成功`,
      };
    },
  },
  {
    url: "menus/:id",
    method: ["DELETE"],
    body({ params }) {
      return {
        code: "00000",
        data: null,
        msg: `删除菜单${params.id}成功`,
      };
    },
  },
]);
