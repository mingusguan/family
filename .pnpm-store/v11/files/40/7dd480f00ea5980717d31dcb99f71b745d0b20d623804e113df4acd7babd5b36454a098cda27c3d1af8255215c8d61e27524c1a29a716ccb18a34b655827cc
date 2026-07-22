import { normalizePath, createFilter } from 'vite';
import chokidar from 'chokidar';
import { isMp } from '@uni-helper/uni-env';
import { babelParse, walkAST } from 'ast-kit';
import MagicString from 'magic-string';
import { splitByCase, pascalCase, camelCase, kebabCase } from 'scule';
import path, { resolve, join, relative, dirname, basename, extname, sep } from 'node:path';
import process from 'node:process';
import fg from 'fast-glob';
import { existsSync, readFileSync } from 'node:fs';
import { parse as parse$1 } from '@vue/compiler-sfc';
import { parse } from 'jsonc-parser';

const virtualModuleId = "virtual:uni-layouts";

function scanLayouts(dir = "src/layouts", cwd = process.cwd()) {
  dir = resolve(cwd, dir);
  const files = fg.sync("**/*.vue", {
    onlyFiles: true,
    ignore: ["node_modules", ".git", "**/__*__/*"],
    cwd: dir
  });
  files.sort();
  const layouts = [];
  for (const file of files) {
    const filePath = normalizePath(join(dir, file));
    const dirNameParts = splitByCase(
      normalizePath(relative(dir, dirname(filePath)))
    );
    let fileName = basename(filePath, extname(filePath));
    if (fileName.toLowerCase() === "index")
      fileName = basename(dirname(filePath));
    const fileNameParts = splitByCase(fileName);
    const componentNameParts = [];
    while (dirNameParts.length && (dirNameParts[0] || "").toLowerCase() !== (fileNameParts[0] || "").toLowerCase())
      componentNameParts.push(dirNameParts.shift());
    const pascalName = pascalCase(componentNameParts) + pascalCase(fileNameParts);
    const camelName = camelCase(pascalName);
    const kebabName = kebabCase(pascalName);
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
  return str.replace(/\\|\//g, sep);
}
function getPageJsonPath(cwd = process.cwd()) {
  return existsSync(normalizePath(join(cwd, "src"))) ? "src/pages.json" : "pages.json";
}
function resolveOptions(userOptions = {}) {
  return {
    layout: "default",
    layoutDir: "src/layouts",
    cwd: process.cwd(),
    ...userOptions
  };
}
function loadPagesJson(path2 = "src/pages.json", cwd = process.cwd()) {
  const pagesJsonRaw = readFileSync(resolve(cwd, path2), {
    encoding: "utf-8"
  });
  const { pages = [], subPackages = [] } = parse(pagesJsonRaw);
  return [
    ...pages,
    ...subPackages.map(({ pages: pages2 = {}, root = "" }) => {
      return pages2.map((page) => ({
        ...page,
        path: normalizePath(join(root, page.path))
      }));
    }).flat()
  ];
}
function getTarget(resolvePath, pages = [], layout = "default", cwd = process.cwd()) {
  if (!(resolvePath.endsWith(".vue") || resolvePath.endsWith(".nvue")))
    return false;
  const hasSrcDir = slash(resolvePath).includes(join(cwd, "src"));
  const relativePath = relative(join(cwd, hasSrcDir ? "src" : ""), resolvePath);
  const fileWithoutExt = path.basename(
    relativePath,
    path.extname(relativePath)
  );
  const pathWithoutExt = normalizePath(
    path.join(path.dirname(relativePath), fileWithoutExt)
  );
  const page = pages.find((p) => normalizePath(p.path) === pathWithoutExt);
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
    return parse$1(code, {
      pad: "space"
    }).descriptor || parse$1({
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
    const ms = new MagicString(code);
    const setupCode = sfc.scriptSetup?.loc.source;
    if (setupCode) {
      const setupAst = babelParse(setupCode, sfc.scriptSetup?.lang);
      walkAST(setupAst, {
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
      const uniLayoutNode = sfc.template?.ast.children.find((v) => v.type === 1 && kebabCase(v.tag) === "uni-layout");
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
        `import Layout_${v.pascalName}_Uni from "${normalizePath(v.path)}"`
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
    if (isMp) {
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
    const ms = new MagicString(code);
    const imports = [];
    const components = [];
    for (const v of this.layouts) {
      imports.push(
        `import Layout_${v.pascalName}_Uni from "${normalizePath(v.path)}"`
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
        ctx.setupWatcher(chokidar.watch(["src/pages.json", "pages.json"]));
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
      const filter = createFilter(["src/main.(ts|js)", "main.(ts|js)"]);
      if (filter(id))
        return ctx.importLayoutComponents(code, id);
      return ctx.transform(code, id);
    }
  };
}

export { VitePluginUniLayouts, VitePluginUniLayouts as default };
