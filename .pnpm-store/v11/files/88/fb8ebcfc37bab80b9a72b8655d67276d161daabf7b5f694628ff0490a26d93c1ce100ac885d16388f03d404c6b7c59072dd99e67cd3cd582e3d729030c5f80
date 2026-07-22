import { env } from 'std-env';

function toBoolean(val) {
  return val ? val !== "false" : false;
}
function parseJSON(val) {
  let obj;
  try {
    obj = JSON.parse(val || "{}");
  } catch (error) {
    obj = {};
  }
  return obj;
}

const builtInPlatforms = ["h5", "web", "app", "app-plus", "app-harmony", "mp-360", "mp-alipay", "mp-baidu", "mp-qq", "mp-toutiao", "mp-weixin", "mp-kuaishou", "mp-lark", "mp-jd", "mp-xhs", "quickapp-webview", "quickapp-webview-huawei", "quickapp-webview-union"];
const platform = env.UNI_PLATFORM;
const appPlatform = env.UNI_APP_PLATFORM;
const subPlatform = env.UNI_SUB_PLATFORM;
const utsPlatform = env.UNI_UTS_PLATFORM;
const isH5 = platform === "h5";
const isWeb = platform === "web";
const isApp = platform === "app";
const isAppPlus = platform === "app-plus";
const isAppHarmony = platform === "app-harmony";
const isAppAndroid = appPlatform === "android" || utsPlatform === "app-android";
const isAppIOS = appPlatform === "ios" || utsPlatform === "app-ios";
const isMp = /^mp-/i.test(platform);
const isMpWeixin = platform === "mp-weixin";
const isMpAlipay = platform === "mp-alipay";
const isMpBaidu = platform === "mp-baidu";
const isMpKuaishou = platform === "mp-kuaishou";
const isMpQQ = platform === "mp-qq";
const isMpToutiao = platform === "mp-toutiao";
const isQuickapp = /^quickapp-webview/i.test(platform);
const isQuickappUnion = platform === "quickapp-webview-union";
const isQuickappHuawei = platform === "quickapp-webview-huawei";
const utsJsCodeFormat = env.UNI_UTS_JS_CODE_FORMAT;
const utsModuleType = env.UNI_UTS_MODULE_TYPE;
const utsModulePrefix = env.UNI_UTS_MODULE_PREFIX;
const utsTargetLanguage = env.UNI_UTS_TARGET_LANGUAGE;
const inputDir = env.UNI_INPUT_DIR;
const outputDir = env.UNI_OUTPUT_DIR;
const cliContext = env.UNI_CLI_CONTEXT;
const subpackage = env.UNI_SUBPACKAGE;
const mpPlugin = env.UNI_MP_PLUGIN;
const isMpPlugin = toBoolean(env.UNI_MP_PLUGIN);
const compilerVersion = env.UNI_COMPILER_VERSION;
const compilerVersionType = env.UNI_COMPILER_VERSION_TYPE;
const hbuilderxPlugins = env.UNI_HBUILDERX_PLUGINS;
const renderer = env.UNI_RENDERER;
const nvueCompiler = env.UNI_NVUE_COMPILER;
const nvueStyleCompiler = env.UNI_NVUE_STYLE_COMPILER;
const appCodeSpliting = env.UNI_APP_CODE_SPLITING;
const automatorWsEndpoint = env.UNI_AUTOMATOR_WS_ENDPOINT;
const automatorAppWebview = env.UNI_AUTOMATOR_APP_WEBVIEW;
const automatorAppWebviewSrc = env.UNI_AUTOMATOR_APP_WEBVIEW_SRC;
const h5Base = env.UNI_H5_BASE;
const h5Browser = env.UNI_H5_BROWSER;
const customScript = env.UNI_CUSTOM_SCRIPT;
const customDefine = env.UNI_CUSTOM_DEFINE;
const customContext = env.UNI_CUSTOM_CONTEXT;
const minimize = env.UNI_MINIMIZE;
const isMinimize = toBoolean(env.UNI_MINIMIZE);
const uvue = env.UNI_UVUE;
const isUVue = toBoolean(env.UNI_MINIMIZE);
const uvueTargetLanguage = env.UNI_UVUE_TARGET_LANGUAGE;
const compiler = env.UNI_COMPILER;
const rendererNative = env.UNI_RENDERER_NATIVE;
const nvueAppStyles = env.UNI_NVUE_APP_STYLES;
const appChangedFiles = env.UNI_APP_CHANGED_FILES;
const appChangedPages = env.UNI_APP_CHANGED_PAGES;
const darkMode = env.VUE_APP_DARK_MODE;
const hasDarkMode = toBoolean(env.VUE_APP_DARK_MODE);
const hxUseBaseType = env.HX_USE_BASE_TYPE;
const hxDependenciesDir = env.HX_DEPENDENCIES_DIR;
const appX = env.UNI_APP_X;
const isAppX = toBoolean(env.UNI_APP_X);
const appXCacheDir = env.UNI_APP_X_CACHE_DIR;
const hxVersion = env.HX_VERSION;
const appXPageCount = env.UNI_APP_X_PAGE_COUNT;
const appXTsc = env.UNI_APP_X_TSC;
const appXSingleThread = env.UNI_APP_X_SINGLE_THREAD;
const appXSetup = env.UNI_APP_X_SETUP;
const appXUVueScriptEngine = env.UNI_APP_X_UVUE_SCRIPT_ENGINE;
const compileTarget = env.UNI_COMPILE_TARGET;
const compileCloudDir = env.UNI_COMPILE_CLOUD_DIR;
const modulesEncryptCacheDir = env.UNI_MODULES_ENCRYPT_CACHE_DIR;
const appPackType = env.UNI_APP_PACK_TYPE;
const appHarmonyProjectPath = env.UNI_APP_HARMONY_PROJECT_PATH;
const statTitleJson = parseJSON(env.STAT_TITLE_JSON);
const sourcemap = env.SOURCEMAP;
const hasSourcemap = toBoolean(env.SOURCEMAP);
const ssrClient = env.UNI_SSR_CLIENT;
const isSSRClient = toBoolean(env.UNI_SSR_CLIENT);
const ssrServer = env.UNI_SSR_SERVER;
const isSSRServer = toBoolean(env.UNI_SSR_SERVER);

export { appChangedFiles, appChangedPages, appCodeSpliting, appHarmonyProjectPath, appPackType, appPlatform, appX, appXCacheDir, appXPageCount, appXSetup, appXSingleThread, appXTsc, appXUVueScriptEngine, automatorAppWebview, automatorAppWebviewSrc, automatorWsEndpoint, builtInPlatforms, cliContext, compileCloudDir, compileTarget, compiler, compilerVersion, compilerVersionType, customContext, customDefine, customScript, darkMode, h5Base, h5Browser, hasDarkMode, hasSourcemap, hbuilderxPlugins, hxDependenciesDir, hxUseBaseType, hxVersion, inputDir, isApp, isAppAndroid, isAppHarmony, isAppIOS, isAppPlus, isAppX, isH5, isMinimize, isMp, isMpAlipay, isMpBaidu, isMpKuaishou, isMpPlugin, isMpQQ, isMpToutiao, isMpWeixin, isQuickapp, isQuickappHuawei, isQuickappUnion, isSSRClient, isSSRServer, isUVue, isWeb, minimize, modulesEncryptCacheDir, mpPlugin, nvueAppStyles, nvueCompiler, nvueStyleCompiler, outputDir, platform, renderer, rendererNative, sourcemap, ssrClient, ssrServer, statTitleJson, subPlatform, subpackage, utsJsCodeFormat, utsModulePrefix, utsModuleType, utsPlatform, utsTargetLanguage, uvue, uvueTargetLanguage };
