'use strict';

const utilities = require('./unocss-preset-weapp.a1c787c8.cjs');
const utils = require('unplugin-transform-class/utils');

const varEmpty = "var(--un-empty,/*!*/ /*!*/)";

const filterBase = {
  "--un-blur": varEmpty,
  "--un-brightness": varEmpty,
  "--un-contrast": varEmpty,
  "--un-drop-shadow": varEmpty,
  "--un-grayscale": varEmpty,
  "--un-hue-rotate": varEmpty,
  "--un-invert": varEmpty,
  "--un-saturate": varEmpty,
  "--un-sepia": varEmpty
};
const filterProperty = "var(--un-blur) var(--un-brightness) var(--un-contrast) var(--un-drop-shadow) var(--un-grayscale) var(--un-hue-rotate) var(--un-invert) var(--un-saturate) var(--un-sepia)";
const backdropFilterBase = {
  "--un-backdrop-blur": varEmpty,
  "--un-backdrop-brightness": varEmpty,
  "--un-backdrop-contrast": varEmpty,
  "--un-backdrop-grayscale": varEmpty,
  "--un-backdrop-hue-rotate": varEmpty,
  "--un-backdrop-invert": varEmpty,
  "--un-backdrop-opacity": varEmpty,
  "--un-backdrop-saturate": varEmpty,
  "--un-backdrop-sepia": varEmpty
};
const backdropFilterProperty = "var(--un-backdrop-blur) var(--un-backdrop-brightness) var(--un-backdrop-contrast) var(--un-backdrop-grayscale) var(--un-backdrop-hue-rotate) var(--un-backdrop-invert) var(--un-backdrop-opacity) var(--un-backdrop-saturate) var(--un-backdrop-sepia)";
function percentWithDefault(str) {
  let v = utilities.h.bracket.cssvar(str || "");
  if (v != null)
    return v;
  v = str ? utilities.h.percent(str) : "1";
  if (v != null && Number.parseFloat(v) <= 1)
    return v;
}
function toFilter(varName, resolver) {
  return ([, b, s], { theme }) => {
    const value = resolver(s, theme) ?? (s === "none" ? "0" : "");
    if (value !== "") {
      if (b) {
        return {
          [`--un-${b}${varName}`]: `${varName}(${value})`,
          "-webkit-backdrop-filter": backdropFilterProperty,
          "backdrop-filter": backdropFilterProperty
        };
      } else {
        return {
          [`--un-${varName}`]: `${varName}(${value})`,
          filter: filterProperty
        };
      }
    }
  };
}
function dropShadowResolver([, s], { theme }) {
  let v = theme.dropShadow?.[s || "DEFAULT"];
  if (v != null) {
    const shadows = utilities.colorableShadows(v, "--un-drop-shadow-color");
    return {
      "--un-drop-shadow": `drop-shadow(${shadows.join(") drop-shadow(")})`,
      "filter": filterProperty
    };
  }
  v = utilities.h.bracket.cssvar(s);
  if (v != null) {
    return {
      "--un-drop-shadow": `drop-shadow(${v})`,
      "filter": filterProperty
    };
  }
}
const filters = [
  // filters
  [/^(?:(backdrop-)|filter-)?blur(?:-(.+))?$/, toFilter("blur", (s, theme) => theme.blur?.[s || "DEFAULT"] || utilities.h.bracket.cssvar.px(s)), { autocomplete: ["(backdrop|filter)-blur-$blur", "blur-$blur", "filter-blur"] }],
  [/^(?:(backdrop-)|filter-)?brightness-(.+)$/, toFilter("brightness", (s) => utilities.h.bracket.cssvar.percent(s)), { autocomplete: ["(backdrop|filter)-brightness-<percent>", "brightness-<percent>"] }],
  [/^(?:(backdrop-)|filter-)?contrast-(.+)$/, toFilter("contrast", (s) => utilities.h.bracket.cssvar.percent(s)), { autocomplete: ["(backdrop|filter)-contrast-<percent>", "contrast-<percent>"] }],
  // drop-shadow only on filter
  [/^(?:filter-)?drop-shadow(?:-(.+))?$/, dropShadowResolver, {
    autocomplete: [
      "filter-drop",
      "filter-drop-shadow",
      "filter-drop-shadow-color",
      "drop-shadow",
      "drop-shadow-color",
      "filter-drop-shadow-$dropShadow",
      "drop-shadow-$dropShadow",
      "filter-drop-shadow-color-$colors",
      "drop-shadow-color-$colors",
      "filter-drop-shadow-color-(op|opacity)",
      "drop-shadow-color-(op|opacity)",
      "filter-drop-shadow-color-(op|opacity)-<percent>",
      "drop-shadow-color-(op|opacity)-<percent>"
    ]
  }],
  [/^(?:filter-)?drop-shadow-color-(.+)$/, utilities.colorResolver("--un-drop-shadow-color", "drop-shadow", "shadowColor")],
  [/^(?:filter-)?drop-shadow-color-op(?:acity)?-?(.+)$/, ([, opacity]) => ({ "--un-drop-shadow-opacity": utilities.h.bracket.percent(opacity) })],
  [/^(?:(backdrop-)|filter-)?grayscale(?:-(.+))?$/, toFilter("grayscale", percentWithDefault), { autocomplete: ["(backdrop|filter)-grayscale", "(backdrop|filter)-grayscale-<percent>", "grayscale-<percent>"] }],
  [/^(?:(backdrop-)|filter-)?hue-rotate-(.+)$/, toFilter("hue-rotate", (s) => utilities.h.bracket.cssvar.degree(s))],
  [/^(?:(backdrop-)|filter-)?invert(?:-(.+))?$/, toFilter("invert", percentWithDefault), { autocomplete: ["(backdrop|filter)-invert", "(backdrop|filter)-invert-<percent>", "invert-<percent>"] }],
  // opacity only on backdrop-filter
  [/^(backdrop-)opacity-(.+)$/, toFilter("opacity", (s) => utilities.h.bracket.cssvar.percent(s)), { autocomplete: ["backdrop-(op|opacity)", "backdrop-(op|opacity)-<percent>"] }],
  [/^(?:(backdrop-)|filter-)?saturate-(.+)$/, toFilter("saturate", (s) => utilities.h.bracket.cssvar.percent(s)), { autocomplete: ["(backdrop|filter)-saturate", "(backdrop|filter)-saturate-<percent>", "saturate-<percent>"] }],
  [/^(?:(backdrop-)|filter-)?sepia(?:-(.+))?$/, toFilter("sepia", percentWithDefault), { autocomplete: ["(backdrop|filter)-sepia", "(backdrop|filter)-sepia-<percent>", "sepia-<percent>"] }],
  // base
  ["filter", { filter: filterProperty }],
  ["backdrop-filter", {
    "-webkit-backdrop-filter": backdropFilterProperty,
    "backdrop-filter": backdropFilterProperty
  }],
  // nones
  ["filter-none", { filter: "none" }],
  ["backdrop-filter-none", {
    "-webkit-backdrop-filter": "none",
    "backdrop-filter": "none"
  }],
  ...utilities.globalKeywords.map((keyword) => [`filter-${keyword}`, { filter: keyword }]),
  ...utilities.globalKeywords.map((keyword) => [`backdrop-filter-${keyword}`, {
    "-webkit-backdrop-filter": keyword,
    "backdrop-filter": keyword
  }])
];

const ringBase = {
  "--un-ring-inset": varEmpty,
  "--un-ring-offset-width": "0px",
  "--un-ring-offset-color": "#fff",
  "--un-ring-width": "0px",
  "--un-ring-color": "rgb(147 197 253 / 0.5)",
  "--un-shadow": "0 0 rgb(0 0 0 / 0)"
};
const rings = [
  // size
  [/^ring(?:-(.+))?$/, ([, d], { theme }) => {
    const value = theme.ringWidth?.[d || "DEFAULT"] ?? utilities.handler.px(d || "1");
    if (value) {
      return {
        "--un-ring-width": value,
        "--un-ring-offset-shadow": "var(--un-ring-inset) 0 0 0 var(--un-ring-offset-width) var(--un-ring-offset-color)",
        "--un-ring-shadow": "var(--un-ring-inset) 0 0 0 calc(var(--un-ring-width) + var(--un-ring-offset-width)) var(--un-ring-color)",
        "box-shadow": "var(--un-ring-offset-shadow), var(--un-ring-shadow), var(--un-shadow)"
      };
    }
  }, { autocomplete: "ring-$ringWidth" }],
  // size
  [/^ring-(?:width-|size-)(.+)$/, handleWidth, { autocomplete: "ring-(width|size)-$lineWidth" }],
  // offset size
  ["ring-offset", { "--un-ring-offset-width": "1px" }],
  [/^ring-offset-(?:width-|size-)?(.+)$/, ([, d], { theme }) => ({ "--un-ring-offset-width": theme.lineWidth?.[d] ?? utilities.handler.bracket.cssvar.px(d) }), { autocomplete: "ring-offset-(width|size)-$lineWidth" }],
  // colors
  [/^ring-(.+)$/, handleColorOrWidth, { autocomplete: "ring-$colors" }],
  [/^ring-op(?:acity)?-?(.+)$/, ([, opacity]) => ({ "--un-ring-opacity": utilities.handler.bracket.percent.cssvar(opacity) }), { autocomplete: "ring-(op|opacity)-<percent>" }],
  // offset color
  [/^ring-offset-(.+)$/, utilities.colorResolver("--un-ring-offset-color", "ring-offset", "borderColor"), { autocomplete: "ring-offset-$colors" }],
  [/^ring-offset-op(?:acity)?-?(.+)$/, ([, opacity]) => ({ "--un-ring-offset-opacity": utilities.handler.bracket.percent.cssvar(opacity) }), { autocomplete: "ring-offset-(op|opacity)-<percent>" }],
  // style
  ["ring-inset", { "--un-ring-inset": "inset" }]
];
function handleWidth([, b], { theme }) {
  return { "--un-ring-width": theme.ringWidth?.[b] ?? utilities.handler.bracket.cssvar.px(b) };
}
function handleColorOrWidth(match, ctx) {
  if (utilities.isCSSMathFn(utilities.handler.bracket(match[1])))
    return handleWidth(match, ctx);
  return utilities.colorResolver("--un-ring-color", "ring", "borderColor")(match, ctx);
}

const boxShadowsBase = {
  "--un-ring-offset-shadow": "0 0 rgb(0 0 0 / 0)",
  "--un-ring-shadow": "0 0 rgb(0 0 0 / 0)",
  "--un-shadow-inset": varEmpty,
  "--un-shadow": "0 0 rgb(0 0 0 / 0)"
};
const boxShadows = [
  [/^shadow(?:-(.+))?$/, (match, context) => {
    let [, d] = match;
    const { theme } = context;
    d = utils.cacheRestoreSelector(d, theme.transformRules);
    const v = theme.boxShadow?.[d || "DEFAULT"];
    const c = d ? utilities.handler.bracket.cssvar(d) : void 0;
    if ((v != null || c != null) && !utilities.hasParseableColor(c, theme, "shadowColor")) {
      return {
        "--un-shadow": utilities.colorableShadows(v || c, "--un-shadow-color").join(","),
        "box-shadow": "var(--un-ring-offset-shadow), var(--un-ring-shadow), var(--un-shadow)"
      };
    }
    return utilities.colorResolver("--un-shadow-color", "shadow", "shadowColor")(match, context);
  }, { autocomplete: ["shadow-$colors", "shadow-$boxShadow"] }],
  [/^shadow-op(?:acity)?-?(.+)$/, ([, opacity]) => ({ "--un-shadow-opacity": utilities.handler.bracket.percent.cssvar(opacity) }), { autocomplete: "shadow-(op|opacity)-<percent>" }],
  // inset
  ["shadow-inset", { "--un-shadow-inset": "inset" }]
];

const transformValues = [
  "translate",
  "rotate",
  "scale"
];
const transformCpu = [
  "translateX(var(--un-translate-x))",
  "translateY(var(--un-translate-y))",
  // 'translateZ(var(--un-translate-z))',
  "rotate(var(--un-rotate))",
  // 'rotateX(var(--un-rotate-x))',
  // 'rotateY(var(--un-rotate-y))',
  "rotateZ(var(--un-rotate-z))",
  "skewX(var(--un-skew-x))",
  "skewY(var(--un-skew-y))",
  "scaleX(var(--un-scale-x))",
  "scaleY(var(--un-scale-y))"
  // 'scaleZ(var(--un-scale-z))',
].join(" ");
const transform = [
  "translateX(var(--un-translate-x))",
  "translateY(var(--un-translate-y))",
  "translateZ(var(--un-translate-z))",
  "rotate(var(--un-rotate))",
  "rotateX(var(--un-rotate-x))",
  "rotateY(var(--un-rotate-y))",
  "rotateZ(var(--un-rotate-z))",
  "skewX(var(--un-skew-x))",
  "skewY(var(--un-skew-y))",
  "scaleX(var(--un-scale-x))",
  "scaleY(var(--un-scale-y))",
  "scaleZ(var(--un-scale-z))"
].join(" ");
const transformGpu = [
  "translate3d(var(--un-translate-x), var(--un-translate-y), var(--un-translate-z))",
  "rotate(var(--un-rotate))",
  "rotateX(var(--un-rotate-x))",
  "rotateY(var(--un-rotate-y))",
  "rotateZ(var(--un-rotate-z))",
  "skewX(var(--un-skew-x))",
  "skewY(var(--un-skew-y))",
  "scaleX(var(--un-scale-x))",
  "scaleY(var(--un-scale-y))",
  "scaleZ(var(--un-scale-z))"
].join(" ");
const transformBase = {
  // transform
  "--un-rotate": 0,
  "--un-rotate-x": 0,
  "--un-rotate-y": 0,
  "--un-rotate-z": 0,
  "--un-scale-x": 1,
  "--un-scale-y": 1,
  "--un-scale-z": 1,
  "--un-skew-x": 0,
  "--un-skew-y": 0,
  "--un-translate-x": 0,
  "--un-translate-y": 0,
  "--un-translate-z": 0
};
const transforms = [
  // origins
  [/^(?:transform-)?origin-(.+)$/, ([, s]) => ({ "transform-origin": utilities.positionMap[s] ?? utilities.handler.bracket.cssvar(s) }), { autocomplete: [`transform-origin-(${Object.keys(utilities.positionMap).join("|")})`, `origin-(${Object.keys(utilities.positionMap).join("|")})`] }],
  // perspectives
  [/^(?:transform-)?perspect(?:ive)?-(.+)$/, ([, s]) => {
    const v = utilities.handler.bracket.cssvar.px.numberWithUnit(s);
    if (v != null) {
      return {
        "-webkit-perspective": v,
        "perspective": v
      };
    }
  }],
  // skip 1 & 2 letters shortcut
  [/^(?:transform-)?perspect(?:ive)?-origin-(.+)$/, ([, s]) => {
    const v = utilities.handler.bracket.cssvar(s) ?? (s.length >= 3 ? utilities.positionMap[s] : void 0);
    if (v != null) {
      return {
        "-webkit-perspective-origin": v,
        "perspective-origin": v
      };
    }
  }],
  // modifiers
  [/^(?:transform-)?translate-()(.+)$/, handleTranslate],
  [/^(?:transform-)?translate-([xyz])-(.+)$/, handleTranslate],
  [/^(?:transform-)?rotate-()(.+)$/, handleRotate],
  [/^(?:transform-)?rotate-([xyz])-(.+)$/, handleRotate],
  [/^(?:transform-)?skew-()(.+)$/, handleSkew],
  [/^(?:transform-)?skew-([xy])-(.+)$/, handleSkew, { autocomplete: ["transform-skew-(x|y)-<percent>", "skew-(x|y)-<percent>"] }],
  [/^(?:transform-)?scale-()(.+)$/, handleScale],
  [/^(?:transform-)?scale-([xyz])-(.+)$/, handleScale, { autocomplete: [`transform-(${transformValues.join("|")})-<percent>`, `transform-(${transformValues.join("|")})-(x|y|z)-<percent>`, `(${transformValues.join("|")})-<percent>`, `(${transformValues.join("|")})-(x|y|z)-<percent>`] }],
  // style
  [/^(?:transform-)?preserve-3d$/, () => ({ "transform-style": "preserve-3d" })],
  [/^(?:transform-)?preserve-flat$/, () => ({ "transform-style": "flat" })],
  // base
  ["transform", { transform }],
  ["transform-cpu", { transform: transformCpu }],
  ["transform-gpu", { transform: transformGpu }],
  ["transform-none", { transform: "none" }],
  ...utilities.makeGlobalStaticRules("transform")
];
function handleTranslate([, d, b], { theme }) {
  b = utils.cacheRestoreSelector(b, theme?.transformRules);
  const v = theme.spacing?.[b] ?? utilities.handler.bracket.cssvar.fraction.remToRpx(b);
  if (v != null) {
    return [
      ...utilities.transformXYZ(d, v, "translate"),
      ["transform", transform]
    ];
  }
}
function handleScale([, d, b], { theme }) {
  b = utils.cacheRestoreSelector(b, theme?.transformRules);
  const v = utilities.handler.bracket.cssvar.fraction.percent(b);
  if (v != null) {
    return [
      ...utilities.transformXYZ(d, v, "scale"),
      ["transform", transform]
    ];
  }
}
function handleRotate([, d = "", b]) {
  const v = utilities.handler.bracket.cssvar.degree(b);
  if (v != null) {
    if (d) {
      return {
        "--un-rotate": 0,
        [`--un-rotate-${d}`]: v,
        "transform": transform
      };
    } else {
      return {
        "--un-rotate-x": 0,
        "--un-rotate-y": 0,
        "--un-rotate-z": 0,
        "--un-rotate": v,
        "transform": transform
      };
    }
  }
}
function handleSkew([, d, b]) {
  const v = utilities.handler.bracket.cssvar.degree(b);
  if (v != null) {
    return [
      ...utilities.transformXYZ(d, v, "skew"),
      ["transform", transform]
    ];
  }
}

exports.backdropFilterBase = backdropFilterBase;
exports.boxShadows = boxShadows;
exports.boxShadowsBase = boxShadowsBase;
exports.filterBase = filterBase;
exports.filters = filters;
exports.ringBase = ringBase;
exports.rings = rings;
exports.transformBase = transformBase;
exports.transforms = transforms;
