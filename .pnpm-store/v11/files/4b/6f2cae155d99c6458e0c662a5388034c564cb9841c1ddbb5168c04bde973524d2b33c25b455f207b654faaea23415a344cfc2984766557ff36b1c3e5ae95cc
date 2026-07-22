import { env } from 'std-env';

const builtInPlatforms = [
  "app",
  "app-plus",
  "h5",
  "mp-360",
  "mp-alipay",
  "mp-baidu",
  "mp-jd",
  "mp-kuaishou",
  "mp-lark",
  "mp-qq",
  "mp-toutiao",
  "mp-weixin",
  "quickapp-webview",
  "quickapp-webview-huawei",
  "quickapp-webview-union"
];

const platform = env.UNI_PLATFORM;
const utsPlatform = env.UNI_UTS_PLATFORM;
const appPlatform = env.UNI_APP_PLATFORM;
const subPlatform = env.UNI_SUB_PLATFORM;
const isH5 = platform === "h5";
const isApp = platform === "app";
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

const hasDarkMode = toBoolean(env.VUE_APP_DARK_MODE);
const hasSourcemap = toBoolean(env.SOURCEMAP);
const isMpPlugin = toBoolean(env.UNI_MP_PLUGIN);
const isMinimize = toBoolean(env.UNI_MINIMIZE);
const isSSRClient = toBoolean(env.UNI_SSR_CLIENT);
const isSSRServer = toBoolean(env.UNI_SSR_SERVER);

const compiler = env.UNI_COMPILER;
const nvueCompiler = env.UNI_NVUE_COMPILER;
const nvueStyleCompiler = env.UNI_NVUE_STYLE_COMPILER;
const compilerVersion = env.UNI_COMPILER_VERSION;
const compilerVersionType = env.UNI_COMPILER_VERSION_TYPE;

const cliContext = env.UNI_CLI_CONTEXT;
const inputDir = env.UNI_INPUT_DIR;
const outputDir = env.UNI_OUTPUT_DIR;
const statTitleJson = parseJSON(
  env.STAT_TITLE_JSON
);
const customContext = env.UNI_CUSTOM_CONTEXT;
const customScript = env.UNI_CUSTOM_SCRIPT;
const customDefine = env.UNI_CUSTOM_DEFINE;
const subpackage = env.UNI_SUBPACKAGE;
const renderer = env.UNI_RENDERER;
const rendererNative = env.UNI_RENDERER_NATIVE;

export { appPlatform, builtInPlatforms, cliContext, compiler, compilerVersion, compilerVersionType, customContext, customDefine, customScript, hasDarkMode, hasSourcemap, inputDir, isApp, isAppAndroid, isAppIOS, isH5, isMinimize, isMp, isMpAlipay, isMpBaidu, isMpKuaishou, isMpPlugin, isMpQQ, isMpToutiao, isMpWeixin, isQuickapp, isQuickappHuawei, isQuickappUnion, isSSRClient, isSSRServer, nvueCompiler, nvueStyleCompiler, outputDir, platform, renderer, rendererNative, statTitleJson, subPlatform, subpackage, utsPlatform };
