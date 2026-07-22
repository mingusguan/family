'use strict';

Object.defineProperty(exports, '__esModule', { value: true });

const vite = require('vite');
const chokidar = require('chokidar');
const uniEnv = require('@uni-helper/uni-env');
const astKit = require('ast-kit');
const MagicString = require('magic-string');
const scule = require('scule');
const path = require('node:path');
const process = require('node:process');
const fg = require('fast-glob');
const node_fs = require('node:fs');
const compilerSfc = require('@vue/compiler-sfc');
const jsoncParser = require('jsonc-parser');

function _interopDefaultCompat (e) { return e && typeof e === 'object' && 'default' in e ? e.default : e; }

const chokidar__default = /*#__PURE__*/_interopDefaultCompat(chokidar);
const MagicString__default = /*#__PURE__*/_interopDefaultCompat(MagicString);
const path__default = /*#__PURE__*/_interopDefaultCompat(path);
const process__default = /*#__PURE__*/_interopDefaultCompat(process);
const fg__default = /*#__PURE__*/_interopDefaultCompat(fg);

const virtualModuleId = "virtual:uni-layouts";

function scanLayouts(dir = "src/layouts", cwd = process__default.cwd()) {
  dir = path.resolve(cwd, dir);
  const files = fg__default.sync("**/*.vue", {
    onlyFiles: true,
    ignore: ["node_modules", ".git", "**/__*__/*"],
    cwd: dir
  });
  files.sort();
  const layouts = [];
  for (const file of files) {
    const filePath = vite.normalizePath(path.join(dir, file));
    const dirNameParts = scule.splitByCase(
      vite.normalizePath(path.relative(dir, path.dirname(filePath)))
    );
    let fileName = path.basename(filePath, path.extname(filePath));
    if (fileName.toLowerCase() === "index")
      fileName = path.basename(path.dirname(filePath));
    const fileNameParts = scule.splitByCase(fileName);
    const componentNameParts = [];
    while (dirNameParts.length && (dirNameParts[0] || "").toLowerCase() !== (fileNameParts[0] || "").toLowerCase())
      componentNameParts.push(dirNameParts.shift());
    const pascalName = scule.pascalCase(componentNameParts) + scule.pascalCase(fileNameParts);
    const camelName = scule.camelCase(pascalName);
    const kebabName = scule.kebabCase(pascalName);
    layouts.push({
      name: camelName,
      path: filePath,
      pascalName,
      kebabName
    });
  }
  return layouts;
}

function slash(str) {
  return str.replace(/\\|\//g, path.sep);
}
function getPageJsonPath(cwd = process__default.cwd()) {
  return node_fs.existsSync(vite.normalizePath(path.join(cwd, "src"))) ? "src/pages.json" : "pages.json";
}
function resolveOptions(userOptions = {}) {
  return {
    layout: "default",
    layoutDir: "src/layouts",
    cwd: process__default.cwd(),
    ...userOptions
  };
}
function loadPagesJson(path2 = "src/pages.json", cwd = process__default.cwd()) {
  const pagesJsonRaw = node_fs.readFileSync(path.resolve(cwd, path2), {
    encoding: "utf-8"
  });
  const { pages = [], subPackages = [] } = jsoncParser.parse(pagesJsonRaw);
  return [
    ...pages,
    ...subPackages.map(({ pages: pages2 = {}, root = "" }) => {
      return pages2.map((page) => ({
        ...page,
        path: vite.normalizePath(path.join(root, page.path))
      }));
    }).flat()
  ];
}
function getTarget(resolvePath, pages = [], layout = "default", cwd = process__default.cwd()) {
  if (!(resolvePath.endsWith(".vue") || resolvePath.endsWith(".nvue")))
    return false;
  const hasSrcDir = slash(resolvePath).includes(path.join(cwd, "src"));
  const relativePath = path.relative(path.join(cwd, hasSrcDir ? "src" : ""), resolvePath);
  const fileWithoutExt = path__default.basename(
    relativePath,
    path__default.extname(relativePath)
  );
  const pathWithoutExt = vite.normalizePath(
    path__default.join(path__default.dirname(relativePath), fileWithoutExt)
  );
  const page = pages.find((p) => vite.normalizePath(p.path) === pathWithoutExt);
  if (page) {
    return {
      layout,
      ...page
    };
  }
  return false;
}
async function parseSFC(code) {
  try {
    return compilerSfc.parse(code, {
      pad: "space"
    }).descriptor || compilerSfc.parse({
      source: code
    });
  } catch {
    throw new Error(
      `[vite-plugin-uni-layouts] Vue3's "@vue/compiler-sfc" is required.`
    );
  }
}

var __defProp = Object.defineProperty;
var __defNormalProp = (obj, key, value) => key in obj ? __defProp(obj, key, { enumerable: true, configurable: true, writable: true, value }) : obj[key] = value;
var __publicField = (obj, key, value) => {
  __defNormalProp(obj, typeof key !== "symbol" ? key + "" : key, value);
  return value;
};
class Context {
  constructor(options) {
    __publicField(this, "config");
    __publicField(this, "options");
    __publicField(this, "pages");
    __publicField(this, "layouts");
    __publicField(this, "pageJsonPath");
    __publicField(this, "_server");
    this.options = options;
    this.pages = [];
    this.layouts = scanLayouts(options.layoutDir, options.cwd);
    this.pageJsonPath = "src/pages.json";
  }
  setupViteServer(server) {
    if (this._server === server)
      return;
    this._server = server;
    this.setupWatcher(server.watcher);
  }
  async setupWatcher(watcher) {
    watcher.on("change", async (path) => {
      if (path.includes("pages.json"))
        this.pages = loadPagesJson(this.pageJsonPath, this.options.cwd);
    });
  }
  async transform(code, path) {
    if (!this.layouts.length)
      return;
    if (!this.pages?.length)
      this.pages = loadPagesJson(this.pageJsonPath, this.options.cwd);
    const page = getTarget(
      path,
      this.pages,
      this.options.layout,
      this.config?.root || this.options.cwd
    );
    if (!page)
      return;
    let pageLayoutName = page.layout;
    let pageLayout;
    const pageLayoutProps = [];
    if (typeof pageLayoutName === "boolean" && pageLayoutName)
      pageLayoutName = "default";
    if (typeof pageLayoutName === "string") {
      if (!pageLayoutName)
        return;
      pageLayout = this.layouts.find((l) => l.name === pageLayoutName);
      if (!pageLayout)
        return;
    }
    const disabled = typeof pageLayoutName === "boolean" && !pageLayoutName;
    const sfc = await parseSFC(code);
    const ms = new MagicString__default(code);
    const setupCode = sfc.scriptSetup?.loc.source;
    if (setupCode) {
      const setupAst = astKit.babelParse(setupCode, sfc.scriptSetup?.lang);
      astKit.walkAST(setupAst, {
        enter(node) {
          if (node.type === "VariableDeclarator") {
            const hasUniLayoutVar = node.id.type === "Identifier" && node.id.name === "uniLayout";
            const isRef = node.init?.type === "CallExpression" && node.init.callee.type === "Identifier" && node.init.callee.name === "ref";
            if (hasUniLayoutVar && isRef)
              pageLayoutProps.push('ref="uniLayout"');
          }
        }
      });
    }
    if (disabled) {
      const uniLayoutNode = sfc.template?.ast.children.find((v) => v.type === 1 && scule.kebabCase(v.tag) === "uni-layout");
      if (!uniLayoutNode)
        return;
      ms.overwrite(uniLayoutNode.loc.start.offset, uniLayoutNode.loc.end.offset, this.generateDynamicLayout(uniLayoutNode));
    } else {
      if (sfc.template?.loc.start.offset && sfc.template?.loc.end.offset)
        ms.overwrite(sfc.template?.loc.start.offset, sfc.template?.loc.end.offset, `
<layout-${pageLayout?.kebabName}-uni ${pageLayoutProps.join(" ")}>${sfc.template.content}</layout-${pageLayout?.kebabName}-uni>
`);
    }
    if (ms.hasChanged()) {
      const map = ms.generateMap({
        source: path,
        file: `${path}.map`,
        includeContent: true
      });
      return {
        code: ms.toString(),
        map
      };
    }
  }
  async virtualModule() {
    const imports = [];
    const components = [];
    const _exports = this.layouts.map((v) => {
      imports.push(
        `import Layout_${v.pascalName}_Uni from "${vite.normalizePath(v.path)}"`
      );
      components.push(
        `app.component("layout-${v.kebabName}-uni", Layout_${v.pascalName}_Uni)`
      );
      return `Layout_${v.pascalName}_Uni,`;
    });
    return `${imports.join("\n")}
export const layouts = {
  ${_exports.join("\n")}
}
export default {
  install(app) {
    ${components.join("\n")}
  }
}`;
  }
  generateDynamicLayout(node) {
    const staticLayoutNameBind = node.props.find(
      (v) => v.type === 6 && v.name === "name" && v.value?.content
    );
    const dynamicLayoutNameBind = node.props.find(
      (v) => v.type === 7 && v.name === "bind" && v.arg?.type === 4 && v.arg?.content === "name" && v.exp?.type === 4 && v.exp.content
    );
    const slotsSource = node.children.map((v) => v.loc.source).join("\n");
    const nodeProps = node.props.filter((prop) => !(prop === dynamicLayoutNameBind || prop === staticLayoutNameBind)).map((v) => v.loc.source);
    if (!(staticLayoutNameBind || dynamicLayoutNameBind))
      console.warn("[vite-plugin-uni-layouts] Dynamic layout not found name bind");
    if (uniEnv.isMp) {
      const props = [...nodeProps];
      if (staticLayoutNameBind) {
        const layout = staticLayoutNameBind.value?.content;
        return `<layout-${layout}-uni ${props.join(" ")}>${slotsSource}</layout-${layout}-uni>`;
      }
      const bind = dynamicLayoutNameBind.exp.content;
      const defaultSlot = node.children.filter((v) => {
        if (v.type === 1 && v.tagType === 3) {
          const slot = v.props.find((v2) => v2.type === 7 && v2.name === "slot" && v2.arg?.type === 4);
          if (slot)
            return slot.arg.content === "default";
        }
        return v;
      });
      const defaultSlotSource = defaultSlot.map((v) => v.loc.source).join("\n");
      const layouts = this.layouts.map((layout, index) => `<layout-${layout.kebabName}-uni v-${index === 0 ? "if" : "else-if"}="${bind} ==='${layout.kebabName}'" ${props.join(" ")}>${slotsSource}</layout-${layout.kebabName}-uni>`);
      layouts.push(`<template v-else>${defaultSlotSource}</template>`);
      return layouts.join("\n");
    } else {
      const props = [...nodeProps];
      if (staticLayoutNameBind)
        props.push(`is="layout-${staticLayoutNameBind.value?.content}-uni"`);
      else
        props.push(`:is="\`layout-\${${dynamicLayoutNameBind.exp.content}}-uni\`"`);
      return `<component ${props.join(" ")}>${slotsSource}</component>`;
    }
  }
  async importLayoutComponents(code, id) {
    const ms = new MagicString__default(code);
    const imports = [];
    const components = [];
    for (const v of this.layouts) {
      imports.push(
        `import Layout_${v.pascalName}_Uni from "${vite.normalizePath(v.path)}"`
      );
      components.push(
        `app.component("layout-${v.kebabName}-uni", Layout_${v.pascalName}_Uni);
`
      );
    }
    ms.append(imports.join("\n"));
    ms.replace(
      /(createApp[\s\S]*?)(return\s{\s*app)/,
      `$1${components.join("")}$2`
    );
    const map = ms.generateMap({
      source: id,
      file: `${id}.map`,
      includeContent: true
    });
    code = ms.toString();
    return {
      code,
      map
    };
  }
}

function VitePluginUniLayouts(userOptions = {}) {
  const options = resolveOptions(userOptions);
  const ctx = new Context(options);
  ctx.pageJsonPath = getPageJsonPath(options.cwd);
  return {
    name: "vite-plugin-uni-layouts",
    enforce: "pre",
    configResolved(config) {
      ctx.config = config;
      if (config.build.watch)
        ctx.setupWatcher(chokidar__default.watch(["src/pages.json", "pages.json"]));
    },
    configureServer(server) {
      ctx.setupViteServer(server);
    },
    resolveId(id) {
      if (id === virtualModuleId)
        return id;
    },
    load(id) {
      if (id === virtualModuleId)
        return ctx.virtualModule();
    },
    transform(code, id) {
      const filter = vite.createFilter(["src/main.(ts|js)", "main.(ts|js)"]);
      if (filter(id))
        return ctx.importLayoutComponents(code, id);
      return ctx.transform(code, id);
    }
  };
}

exports.VitePluginUniLayouts = VitePluginUniLayouts;
exports.default = VitePluginUniLayouts;
