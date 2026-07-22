import { g as globalKeywords, h, b as handler, m as makeGlobalStaticRules, p as positionMap, e as parseColor, i as isCSSMathFn, c as colorResolver, f as directionMap, d as hasParseableColor, j as cornerMap, k as isSize, l as insetMap, r as resolveBreakpoints, n as directionSize, a as colorableShadows, s as splitShorthand } from './unocss-preset-weapp.d4ee9f3f.mjs';
import { cacheRestoreSelector } from 'unplugin-transform-class/utils';
import { colorToString, colorOpacityToString } from '@unocss/rule-utils';
import { warnOnce, toArray } from '@unocss/core';
import { g as transforms, e as boxShadows, c as rings, a as filters } from './unocss-preset-weapp.1d3647ce.mjs';

const verticalAlignAlias = {
  "mid": "middle",
  "base": "baseline",
  "btm": "bottom",
  "baseline": "baseline",
  "top": "top",
  "start": "top",
  "middle": "middle",
  "bottom": "bottom",
  "end": "bottom",
  "text-top": "text-top",
  "text-bottom": "text-bottom",
  "sub": "sub",
  "super": "super",
  ...Object.fromEntries(globalKeywords.map((x) => [x, x]))
};
const verticalAligns = [
  [/^(?:vertical|align|v)-([-\w]+%?)$/, ([, v]) => ({ "vertical-align": verticalAlignAlias[v] ?? h.numberWithUnit(v) }), {
    autocomplete: [
      `(vertical|align|v)-(${Object.keys(verticalAlignAlias).join("|")})`,
      "(vertical|align|v)-<percentage>"
    ]
  }]
];
const textAlignValues = ["center", "left", "right", "justify", "start", "end"];
const textAligns = [
  ...textAlignValues.map((v) => [`text-${v}`, { "text-align": v }]),
  ...[
    ...globalKeywords,
    ...textAlignValues
  ].map((v) => [`text-align-${v}`, { "text-align": v }])
];

const animations = [
  [/^(?:animate-)?keyframes-(.+)$/, ([, name], { theme }) => {
    const kf = theme.animation?.keyframes?.[name];
    if (kf) {
      return [
        `@keyframes ${name}${kf}`,
        { animation: name }
      ];
    }
  }, { autocomplete: ["animate-keyframes-$animation.keyframes", "keyframes-$animation.keyframes"] }],
  [/^animate-(.+)$/, ([, name], { theme }) => {
    name = cacheRestoreSelector(name, theme.transformRules);
    const kf = theme.animation?.keyframes?.[name];
    if (kf) {
      const duration = theme.animation?.durations?.[name] ?? "1s";
      const timing = theme.animation?.timingFns?.[name] ?? "linear";
      const count = theme.animation?.counts?.[name] ?? 1;
      const props = theme.animation?.properties?.[name];
      return [
        `@keyframes ${name}${kf}`,
        {
          animation: `${name} ${duration} ${timing} ${count}`,
          ...props
        }
      ];
    }
    return { animation: handler.bracket.cssvar(name) };
  }, { autocomplete: "animate-$animation.keyframes" }],
  [/^animate-name-(.+)/, ([, d]) => ({ "animation-name": handler.bracket.cssvar(d) ?? d })],
  // timings
  [/^animate-duration-(.+)$/, ([, d], { theme }) => ({ "animation-duration": theme.duration?.[d || "DEFAULT"] ?? handler.bracket.cssvar.time(d) }), { autocomplete: ["animate-duration", "animate-duration-$duration"] }],
  [/^animate-delay-(.+)$/, ([, d], { theme }) => ({ "animation-delay": theme.duration?.[d || "DEFAULT"] ?? handler.bracket.cssvar.time(d) }), { autocomplete: ["animate-delay", "animate-delay-$duration"] }],
  [/^animate-ease(?:-(.+))?$/, ([, d], { theme }) => ({ "animation-timing-function": theme.easing?.[d || "DEFAULT"] ?? handler.bracket.cssvar(d) }), { autocomplete: ["animate-ease", "animate-ease-$easing"] }],
  // fill mode
  [/^animate-(fill-mode-|fill-|mode-)?(.+)$/, ([, t, d]) => ["none", "forwards", "backwards", "both", ...[t ? globalKeywords : []]].includes(d) ? { "animation-fill-mode": d } : void 0, {
    autocomplete: [
      "animate-(fill|mode|fill-mode)",
      "animate-(fill|mode|fill-mode)-(none|forwards|backwards|both|inherit|initial|revert|revert-layer|unset)",
      "animate-(none|forwards|backwards|both|inherit|initial|revert|revert-layer|unset)"
    ]
  }],
  // direction
  [/^animate-(direction-)?(.+)$/, ([, t, d]) => ["normal", "reverse", "alternate", "alternate-reverse", ...[t ? globalKeywords : []]].includes(d) ? { "animation-direction": d } : void 0, {
    autocomplete: [
      "animate-direction",
      "animate-direction-(normal|reverse|alternate|alternate-reverse|inherit|initial|revert|revert-layer|unset)",
      "animate-(normal|reverse|alternate|alternate-reverse|inherit|initial|revert|revert-layer|unset)"
    ]
  }],
  // others
  [/^animate-(?:iteration-count-|iteration-|count-)(.+)$/, ([, d]) => ({ "animation-iteration-count": handler.bracket.cssvar(d) ?? d.replace(/-/g, ",") }), { autocomplete: ["animate-(iteration|count|iteration-count)", "animate-(iteration|count|iteration-count)-<num>"] }],
  [/^animate-(play-state-|play-|state-)?(.+)$/, ([, t, d]) => ["paused", "running", ...[t ? globalKeywords : []]].includes(d) ? { "animation-play-state": d } : void 0, {
    autocomplete: [
      "animate-(play|state|play-state)",
      "animate-(play|state|play-state)-(paused|running|inherit|initial|revert|revert-layer|unset)",
      "animate-(paused|running|inherit|initial|revert|revert-layer|unset)"
    ]
  }],
  ["animate-none", { animation: "none" }],
  ...makeGlobalStaticRules("animate", "animation")
];

function bgGradientToValue(cssColor) {
  if (cssColor)
    return colorToString(cssColor, 0);
  return "rgb(255 255 255 / 0)";
}
function bgGradientColorValue(mode, cssColor, color, alpha) {
  if (cssColor) {
    if (alpha != null)
      return colorToString(cssColor, alpha);
    else
      return colorToString(cssColor, `var(--un-${mode}-opacity, ${colorOpacityToString(cssColor)})`);
  }
  return colorToString(color, alpha);
}
function bgGradientColorResolver() {
  return ([, mode, body], { theme }) => {
    const data = parseColor(body, theme, "backgroundColor");
    if (!data)
      return;
    const { alpha, color, cssColor } = data;
    if (!color)
      return;
    const colorString = bgGradientColorValue(mode, cssColor, color, alpha);
    switch (mode) {
      case "from":
        return {
          "--un-gradient-from-position": "0%",
          "--un-gradient-from": `${colorString} var(--un-gradient-from-position)`,
          "--un-gradient-to-position": "100%",
          "--un-gradient-to": `${bgGradientToValue(cssColor)} var(--un-gradient-to-position)`,
          "--un-gradient-stops": "var(--un-gradient-from), var(--un-gradient-to)"
        };
      case "via":
        return {
          "--un-gradient-via-position": "50%",
          "--un-gradient-to": bgGradientToValue(cssColor),
          "--un-gradient-stops": `var(--un-gradient-from), ${colorString} var(--un-gradient-via-position), var(--un-gradient-to)`
        };
      case "to":
        return {
          "--un-gradient-to-position": "100%",
          "--un-gradient-to": `${colorString} var(--un-gradient-to-position)`
        };
    }
  };
}
function bgGradientPositionResolver() {
  return ([, mode, body]) => {
    return {
      [`--un-gradient-${mode}-position`]: `${Number(handler.bracket.cssvar.percent(body)) * 100}%`
    };
  };
}
const backgroundStyles = [
  // gradients
  [/^bg-gradient-(.+)$/, ([, d]) => ({ "--un-gradient": handler.bracket(d) }), {
    autocomplete: ["bg-gradient", "bg-gradient-(from|to|via)", "bg-gradient-(from|to|via)-$colors", "bg-gradient-(from|to|via)-(op|opacity)", "bg-gradient-(from|to|via)-(op|opacity)-<percent>"]
  }],
  [/^(?:bg-gradient-)?stops-(\[.+\])$/, ([, s]) => ({ "--un-gradient-stops": handler.bracket(s) })],
  [/^(?:bg-gradient-)?(from|via|to)-(.+)$/, bgGradientColorResolver()],
  [/^(?:bg-gradient-)?(from|via|to)-op(?:acity)?-?(.+)$/, ([, position, opacity]) => ({ [`--un-${position}-opacity`]: handler.bracket.percent(opacity) })],
  [/^(from|via|to)-([\d.]+)%$/, bgGradientPositionResolver()],
  // images
  [/^bg-gradient-((?:repeating-)?(?:linear|radial|conic))$/, ([, s]) => ({
    "background-image": `${s}-gradient(var(--un-gradient, var(--un-gradient-stops, rgb(255 255 255 / 0))))`
  }), { autocomplete: ["bg-gradient-repeating", "bg-gradient-(linear|radial|conic)", "bg-gradient-repeating-(linear|radial|conic)"] }],
  // ignore any center position
  [/^bg-gradient-to-([rltb]{1,2})$/, ([, d]) => {
    if (d in positionMap) {
      return {
        "--un-gradient-shape": `to ${positionMap[d]}`,
        "--un-gradient": "var(--un-gradient-shape), var(--un-gradient-stops)",
        "background-image": "linear-gradient(var(--un-gradient))"
      };
    }
  }, { autocomplete: `bg-gradient-to-(${Object.keys(positionMap).filter((k) => k.length <= 2 && Array.from(k).every((c) => "rltb".includes(c))).join("|")})` }],
  [/^(?:bg-gradient-)?shape-(.+)$/, ([, d]) => {
    const v = d in positionMap ? `to ${positionMap[d]}` : handler.bracket(d);
    if (v != null) {
      return {
        "--un-gradient-shape": v,
        "--un-gradient": "var(--un-gradient-shape), var(--un-gradient-stops)"
      };
    }
  }, { autocomplete: ["bg-gradient-shape", `bg-gradient-shape-(${Object.keys(positionMap).join("|")})`, `shape-(${Object.keys(positionMap).join("|")})`] }],
  ["bg-none", { "background-image": "none" }],
  ["box-decoration-slice", { "box-decoration-break": "slice" }],
  ["box-decoration-clone", { "box-decoration-break": "clone" }],
  ...makeGlobalStaticRules("box-decoration", "box-decoration-break"),
  // size
  ["bg-auto", { "background-size": "auto" }],
  ["bg-cover", { "background-size": "cover" }],
  ["bg-contain", { "background-size": "contain" }],
  // attachments
  ["bg-fixed", { "background-attachment": "fixed" }],
  ["bg-local", { "background-attachment": "local" }],
  ["bg-scroll", { "background-attachment": "scroll" }],
  // clips
  ["bg-clip-border", { "-webkit-background-clip": "border-box", "background-clip": "border-box" }],
  ["bg-clip-content", { "-webkit-background-clip": "content-box", "background-clip": "content-box" }],
  ["bg-clip-padding", { "-webkit-background-clip": "padding-box", "background-clip": "padding-box" }],
  ["bg-clip-text", { "-webkit-background-clip": "text", "background-clip": "text" }],
  ...globalKeywords.map((keyword) => [`bg-clip-${keyword}`, {
    "-webkit-background-clip": keyword,
    "background-clip": keyword
  }]),
  // positions
  // skip 1 & 2 letters shortcut
  [/^bg-([-\w]{3,})$/, ([, s]) => ({ "background-position": positionMap[s] })],
  // repeats
  ["bg-repeat", { "background-repeat": "repeat" }],
  ["bg-no-repeat", { "background-repeat": "no-repeat" }],
  ["bg-repeat-x", { "background-repeat": "repeat-x" }],
  ["bg-repeat-y", { "background-repeat": "repeat-y" }],
  ["bg-repeat-round", { "background-repeat": "round" }],
  ["bg-repeat-space", { "background-repeat": "space" }],
  ...makeGlobalStaticRules("bg-repeat", "background-repeat"),
  // origins
  ["bg-origin-border", { "background-origin": "border-box" }],
  ["bg-origin-padding", { "background-origin": "padding-box" }],
  ["bg-origin-content", { "background-origin": "content-box" }],
  ...makeGlobalStaticRules("bg-origin", "background-origin")
];

const outline = [
  // size
  [/^outline-(?:width-|size-)?(.+)$/, handleWidth$2, { autocomplete: "outline-(width|size)-<num>" }],
  // color
  [/^outline-(?:color-)?(.+)$/, handleColorOrWidth$2, { autocomplete: "outline-$colors" }],
  // offset
  [/^outline-offset-(.+)$/, ([, d], { theme }) => ({ "outline-offset": theme.lineWidth?.[d] ?? handler.bracket.cssvar.global.px(d) }), { autocomplete: "outline-(offset)-<num>" }],
  // style
  ["outline", { "outline-style": "solid" }],
  ...["auto", "dashed", "dotted", "double", "hidden", "solid", "groove", "ridge", "inset", "outset", ...globalKeywords].map((v) => [`outline-${v}`, { "outline-style": v }]),
  ["outline-none", { "outline": "2px solid transparent", "outline-offset": "2px" }]
];
const appearance = [
  ["appearance-auto", { "-webkit-appearance": "auto", "appearance": "auto" }],
  ["appearance-none", { "-webkit-appearance": "none", "appearance": "none" }]
];
function willChangeProperty(prop) {
  return handler.properties.auto.global(prop) ?? {
    contents: "contents",
    scroll: "scroll-position"
  }[prop];
}
const willChange = [
  [/^will-change-(.+)/, ([, p]) => ({ "will-change": willChangeProperty(p) })]
];
function handleWidth$2([, b], { theme }) {
  return { "outline-width": theme.lineWidth?.[b] ?? handler.bracket.cssvar.global.px(b) };
}
function handleColorOrWidth$2(match, ctx) {
  if (isCSSMathFn(handler.bracket(match[1])))
    return handleWidth$2(match, ctx);
  return colorResolver("outline-color", "outline-color", "borderColor")(match, ctx);
}

const borderStyles = ["solid", "dashed", "dotted", "double", "hidden", "none", "groove", "ridge", "inset", "outset", ...globalKeywords];
const borders = [
  // compound
  [/^(?:border|b)()(?:-(.+))?$/, handlerBorderSize, { autocomplete: "(border|b)-<directions>" }],
  [/^(?:border|b)-([xy])(?:-(.+))?$/, handlerBorderSize],
  [/^(?:border|b)-([rltbse])(?:-(.+))?$/, handlerBorderSize],
  [/^(?:border|b)-(block|inline)(?:-(.+))?$/, handlerBorderSize],
  [/^(?:border|b)-([bi][se])(?:-(.+))?$/, handlerBorderSize],
  // size
  [/^(?:border|b)-()(?:width|size)-(.+)$/, handlerBorderSize, { autocomplete: ["(border|b)-<num>", "(border|b)-<directions>-<num>"] }],
  [/^(?:border|b)-([xy])-(?:width|size)-(.+)$/, handlerBorderSize],
  [/^(?:border|b)-([rltbse])-(?:width|size)-(.+)$/, handlerBorderSize],
  [/^(?:border|b)-(block|inline)-(?:width|size)-(.+)$/, handlerBorderSize],
  [/^(?:border|b)-([bi][se])-(?:width|size)-(.+)$/, handlerBorderSize],
  // colors
  [/^(?:border|b)-()(?:color-)?(.+)$/, handlerBorderColorOrSize, { autocomplete: ["(border|b)-$colors", "(border|b)-<directions>-$colors"] }],
  [/^(?:border|b)-([xy])-(?:color-)?(.+)$/, handlerBorderColorOrSize],
  [/^(?:border|b)-([rltbse])-(?:color-)?(.+)$/, handlerBorderColorOrSize],
  [/^(?:border|b)-(block|inline)-(?:color-)?(.+)$/, handlerBorderColorOrSize],
  [/^(?:border|b)-([bi][se])-(?:color-)?(.+)$/, handlerBorderColorOrSize],
  // opacity
  [/^(?:border|b)-()op(?:acity)?-?(.+)$/, handlerBorderOpacity, { autocomplete: "(border|b)-(op|opacity)-<percent>" }],
  [/^(?:border|b)-([xy])-op(?:acity)?-?(.+)$/, handlerBorderOpacity],
  [/^(?:border|b)-([rltbse])-op(?:acity)?-?(.+)$/, handlerBorderOpacity],
  [/^(?:border|b)-(block|inline)-op(?:acity)?-?(.+)$/, handlerBorderOpacity],
  [/^(?:border|b)-([bi][se])-op(?:acity)?-?(.+)$/, handlerBorderOpacity],
  // radius
  [/^(?:border-|b-)?(?:rounded|rd)()(?:-(.+))?$/, handlerRounded, { autocomplete: ["(border|b)-(rounded|rd)", "(border|b)-(rounded|rd)-$borderRadius", "(rounded|rd)", "(rounded|rd)-$borderRadius"] }],
  [/^(?:border-|b-)?(?:rounded|rd)-([rltbse])(?:-(.+))?$/, handlerRounded],
  [/^(?:border-|b-)?(?:rounded|rd)-([rltb]{2})(?:-(.+))?$/, handlerRounded],
  [/^(?:border-|b-)?(?:rounded|rd)-([bise][se])(?:-(.+))?$/, handlerRounded],
  [/^(?:border-|b-)?(?:rounded|rd)-([bi][se]-[bi][se])(?:-(.+))?$/, handlerRounded],
  // style
  [/^(?:border|b)-(?:style-)?()(.+)$/, handlerBorderStyle, { autocomplete: ["(border|b)-style", `(border|b)-(${borderStyles.join("|")})`, "(border|b)-<directions>-style", `(border|b)-<directions>-(${borderStyles.join("|")})`, `(border|b)-<directions>-style-(${borderStyles.join("|")})`, `(border|b)-style-(${borderStyles.join("|")})`] }],
  [/^(?:border|b)-([xy])-(?:style-)?(.+)$/, handlerBorderStyle],
  [/^(?:border|b)-([rltbse])-(?:style-)?(.+)$/, handlerBorderStyle],
  [/^(?:border|b)-(block|inline)-(?:style-)?(.+)$/, handlerBorderStyle],
  [/^(?:border|b)-([bi][se])-(?:style-)?(.+)$/, handlerBorderStyle]
];
function transformBorderColor(color, alpha, direction) {
  if (alpha != null) {
    return {
      [`border${direction}-color`]: colorToString(color, alpha)
    };
  }
  if (direction === "") {
    const object = {};
    const opacityVar = "--un-border-opacity";
    const result = colorToString(color, `var(${opacityVar})`);
    if (result.includes(opacityVar))
      object[opacityVar] = typeof color === "string" ? 1 : colorOpacityToString(color);
    object["border-color"] = result;
    return object;
  } else {
    const object = {};
    const opacityVar = "--un-border-opacity";
    const opacityDirectionVar = `--un-border${direction}-opacity`;
    const result = colorToString(color, `var(${opacityDirectionVar})`);
    if (result.includes(opacityDirectionVar)) {
      object[opacityVar] = typeof color === "string" ? 1 : colorOpacityToString(color);
      object[opacityDirectionVar] = `var(${opacityVar})`;
    }
    object[`border${direction}-color`] = result;
    return object;
  }
}
function borderColorResolver(direction) {
  return ([, body], theme) => {
    const data = parseColor(body, theme, "borderColor");
    if (!data)
      return;
    const { alpha, color, cssColor } = data;
    if (cssColor)
      return transformBorderColor(cssColor, alpha, direction);
    else if (color)
      return transformBorderColor(color, alpha, direction);
  };
}
function handlerBorderSize([, a = "", b], { theme }) {
  const v = theme.lineWidth?.[b || "DEFAULT"] ?? handler.bracket.cssvar.global.rpx(b || "1");
  if (a in directionMap && v != null)
    return directionMap[a].map((i) => [`border${i}-width`, v]);
}
function handlerBorderColorOrSize([, a = "", b], ctx) {
  if (a in directionMap) {
    if (isCSSMathFn(handler.bracket(b)))
      return handlerBorderSize(["", a, b], ctx);
    if (hasParseableColor(b, ctx.theme, "borderColor")) {
      return Object.assign(
        {},
        ...directionMap[a].map((i) => borderColorResolver(i)(["", b], ctx.theme))
      );
    }
  }
}
function handlerBorderOpacity([, a = "", opacity]) {
  const v = handler.bracket.percent.cssvar(opacity);
  if (a in directionMap && v != null)
    return directionMap[a].map((i) => [`--un-border${i}-opacity`, v]);
}
function handlerRounded([, a = "", s], { theme }) {
  s = cacheRestoreSelector(s, theme?.transformRules);
  const v = theme.borderRadius?.[s || "DEFAULT"] || handler.bracket.cssvar.global.fraction.remToRpx(s || "1");
  if (a in cornerMap && v != null)
    return cornerMap[a].map((i) => [`border${i}-radius`, v]);
}
function handlerBorderStyle([, a = "", s]) {
  if (borderStyles.includes(s) && a in directionMap)
    return directionMap[a].map((i) => [`border${i}-style`, s]);
}

const opacity = [
  [/^op(?:acity)?-?(.+)$/, ([, d]) => ({ opacity: handler.bracket.percent.cssvar(d) })]
];
const bgUrlRE = /^\[url\(.+\)\]$/;
const bgLengthRE = /^\[(?:length|size):.+\]$/;
const bgPositionRE = /^\[position:.+\]$/;
const bgGradientRE = /^\[(?:linear|conic|radial)-gradient\(.+\)\]$/;
const bgColors = [
  [/^bg-(.+)$/, (params, body) => {
    let [, d] = params;
    d = cacheRestoreSelector(d, body.theme?.transformRules);
    if (bgUrlRE.test(d))
      return { "--un-url": handler.bracket(d), "background-image": "var(--un-url)" };
    if (bgLengthRE.test(d) && handler.bracketOfLength(d) != null)
      return { "background-size": handler.bracketOfLength(d).split(" ").map((e) => handler.fraction.auto.px.cssvar(e) ?? e).join(" ") };
    if ((isSize(d) || bgPositionRE.test(d)) && handler.bracketOfPosition(d) != null)
      return { "background-position": handler.bracketOfPosition(d).split(" ").map((e) => handler.position.fraction.auto.px.cssvar(e) ?? e).join(" ") };
    if (bgGradientRE.test(d))
      return { "background-image": handler.bracket(d) };
    return colorResolver("background-color", "bg", "backgroundColor")(params, body);
  }, { autocomplete: "bg-$colors" }],
  [/^bg-op(?:acity)?-?(.+)$/, ([, opacity2]) => ({ "--un-bg-opacity": handler.bracket.percent.cssvar(opacity2) }), { autocomplete: "bg-(op|opacity)-<percent>" }]
];
const colorScheme = [
  [/^color-scheme-(\w+)$/, ([, v]) => ({ "color-scheme": v })]
];

const containerParent = [
  [/^@container(?:\/(\w+))?(?:-(normal))?$/, ([, l, v]) => {
    warnOnce("The container query rule is experimental and may not follow semver.");
    return {
      "container-type": v ?? "inline-size",
      "container-name": l
    };
  }]
];

const decorationStyles = ["solid", "double", "dotted", "dashed", "wavy", ...globalKeywords];
const textDecorations = [
  [/^(?:decoration-)?(underline|overline|line-through)$/, ([, s]) => ({ "text-decoration-line": s }), { autocomplete: "decoration-(underline|overline|line-through)" }],
  // size
  [/^(?:underline|decoration)-(?:size-)?(.+)$/, handleWidth$1, { autocomplete: "(underline|decoration)-<num>" }],
  [/^(?:underline|decoration)-(auto|from-font)$/, ([, s]) => ({ "text-decoration-thickness": s }), { autocomplete: "(underline|decoration)-(auto|from-font)" }],
  [/^(?:underline|decoration)-(.+)$/, handleColorOrWidth$1, { autocomplete: "(underline|decoration)-$colors" }],
  [/^(?:underline|decoration)-op(?:acity)?-?(.+)$/, ([, opacity]) => ({ "--un-line-opacity": handler.bracket.percent.cssvar(opacity) }), { autocomplete: "(underline|decoration)-(op|opacity)-<percent>" }],
  // offset
  [/^(?:underline|decoration)-offset-(.+)$/, ([, s], { theme }) => ({ "text-underline-offset": theme.lineWidth?.[s] ?? handler.auto.bracket.cssvar.global.px(s) }), { autocomplete: "(underline|decoration)-(offset)-<num>" }],
  // style
  ...decorationStyles.map((v) => [`underline-${v}`, { "text-decoration-style": v }]),
  ...decorationStyles.map((v) => [`decoration-${v}`, { "text-decoration-style": v }]),
  ["no-underline", { "text-decoration": "none" }],
  ["decoration-none", { "text-decoration": "none" }]
];
function handleWidth$1([, b], { theme }) {
  return { "text-decoration-thickness": theme.lineWidth?.[b] ?? handler.bracket.cssvar.global.px(b) };
}
function handleColorOrWidth$1(match, ctx) {
  if (isCSSMathFn(handler.bracket(match[1])))
    return handleWidth$1(match, ctx);
  const result = colorResolver("text-decoration-color", "line", "borderColor")(match, ctx);
  if (result) {
    return {
      "-webkit-text-decoration-color": result["text-decoration-color"],
      ...result
    };
  }
}

const divides = [
  // divides
  [/^divide-?([xy])$/, handlerDivide, { autocomplete: ["divide-(x|y|block|inline)", "divide-(x|y|block|inline)-reverse", "divide-(x|y|block|inline)-$lineWidth"] }],
  [/^divide-?([xy])-?(.+)$/, handlerDivide],
  [/^divide-?([xy])-reverse$/, ([, d]) => ({ [`--un-divide-${d}-reverse`]: 1 })],
  [/^divide-(block|inline)$/, handlerDivide],
  [/^divide-(block|inline)-(.+)$/, handlerDivide],
  [/^divide-(block|inline)-reverse$/, ([, d]) => ({ [`--un-divide-${d}-reverse`]: 1 })],
  // color & opacity
  [/^divide-(.+)$/, colorResolver("border-color", "divide", "borderColor"), { autocomplete: "divide-$colors" }],
  [/^divide-op(?:acity)?-?(.+)$/, ([, opacity]) => ({ "--un-divide-opacity": handler.bracket.percent(opacity) }), { autocomplete: ["divide-(op|opacity)", "divide-(op|opacity)-<percent>"] }],
  // styles
  ...borderStyles.map((style) => [`divide-${style}`, { "border-style": style }])
];
function handlerDivide([, d, s], { theme }) {
  let v = theme.lineWidth?.[s || "DEFAULT"] ?? handler.bracket.cssvar.rpx(s || "1");
  if (v != null) {
    if (v === "0")
      v = "0px";
    const results = directionMap[d].map((item) => {
      const key = `border${item}-width`;
      const value = item.endsWith("right") || item.endsWith("bottom") ? `calc(${v} * var(--un-divide-${d}-reverse))` : `calc(${v} * calc(1 - var(--un-divide-${d}-reverse)))`;
      return [key, value];
    });
    if (results) {
      return [
        [`--un-divide-${d}-reverse`, 0],
        ...results
      ];
    }
  }
}

const flex = [
  // display
  ["flex", { display: "flex" }],
  ["inline-flex", { display: "inline-flex" }],
  ["flex-inline", { display: "inline-flex" }],
  // flex
  [/^flex-(.*)$/, ([, d], { theme }) => {
    d = cacheRestoreSelector(d, theme?.transformRules);
    return { flex: handler.bracket(d) != null ? handler.bracket(d).split(" ").map((e) => handler.cssvar.fraction(e) ?? e).join(" ") : handler.cssvar.fraction(d) };
  }],
  ["flex-1", { flex: "1 1 0%" }],
  ["flex-auto", { flex: "1 1 auto" }],
  ["flex-initial", { flex: "0 1 auto" }],
  ["flex-none", { flex: "none" }],
  // shrink/grow/basis
  [/^(?:flex-)?shrink(?:-(.*))?$/, ([, d = ""]) => ({ "flex-shrink": handler.bracket.cssvar.number(d) ?? 1 }), { autocomplete: ["flex-shrink-<num>", "shrink-<num>"] }],
  [/^(?:flex-)?grow(?:-(.*))?$/, ([, d = ""]) => ({ "flex-grow": handler.bracket.cssvar.number(d) ?? 1 }), { autocomplete: ["flex-grow-<num>", "grow-<num>"] }],
  [/^(?:flex-)?basis-(.+)$/, ([, d], { theme }) => {
    d = cacheRestoreSelector(d, theme?.transformRules);
    return { "flex-basis": theme.spacing?.[d] ?? handler.bracket.cssvar.auto.fraction.remToRpx(d) };
  }, { autocomplete: ["flex-basis-$spacing", "basis-$spacing"] }],
  // directions
  ["flex-row", { "flex-direction": "row" }],
  ["flex-row-reverse", { "flex-direction": "row-reverse" }],
  ["flex-col", { "flex-direction": "column" }],
  ["flex-col-reverse", { "flex-direction": "column-reverse" }],
  // wraps
  ["flex-wrap", { "flex-wrap": "wrap" }],
  ["flex-wrap-reverse", { "flex-wrap": "wrap-reverse" }],
  ["flex-nowrap", { "flex-wrap": "nowrap" }]
];

const directions = {
  "": "",
  "x": "column-",
  "y": "row-",
  "col": "column-",
  "row": "row-"
};
function handleGap([, d = "", s], { theme }) {
  const v = theme.spacing?.[s] ?? handler.bracket.cssvar.global.remToRpx(s);
  if (v != null) {
    return {
      [`${directions[d]}gap`]: v
    };
  }
}
const gaps = [
  [/^(?:flex-|grid-)?gap-?()(.+)$/, handleGap, { autocomplete: ["gap-$spacing", "gap-<num>"] }],
  [/^(?:flex-|grid-)?gap-([xy])-?(.+)$/, handleGap, { autocomplete: ["gap-(x|y)-$spacing", "gap-(x|y)-<num>"] }],
  [/^(?:flex-|grid-)?gap-(col|row)-?(.+)$/, handleGap, { autocomplete: ["gap-(col|row)-$spacing", "gap-(col|row)-<num>"] }]
];

const rowCol = (s) => s.replace("col", "column");
const rowColTheme = (s) => s[0] === "r" ? "Row" : "Column";
function autoDirection(c, theme, prop) {
  const v = theme[`gridAuto${rowColTheme(c)}`]?.[prop];
  if (v != null)
    return v;
  switch (prop) {
    case "min":
      return "min-content";
    case "max":
      return "max-content";
    case "fr":
      return "minmax(0,1fr)";
  }
  return handler.bracket.cssvar.auto.remToRpx(prop);
}
const grids = [
  // displays
  ["grid", { display: "grid" }],
  ["inline-grid", { display: "inline-grid" }],
  // global
  [/^(?:grid-)?(row|col)-(.+)$/, ([, c, v], { theme }) => ({
    [`grid-${rowCol(c)}`]: theme[`grid${rowColTheme(c)}`]?.[v] ?? handler.bracket.cssvar.auto(v)
  })],
  // span
  [/^(?:grid-)?(row|col)-span-(.+)$/, ([, c, s]) => {
    if (s === "full")
      return { [`grid-${rowCol(c)}`]: "1/-1" };
    const v = handler.bracket.number(s);
    if (v != null)
      return { [`grid-${rowCol(c)}`]: `span ${v}/span ${v}` };
  }, { autocomplete: "(grid-row|grid-col|row|col)-span-<num>" }],
  // starts & ends
  [/^(?:grid-)?(row|col)-start-(.+)$/, ([, c, v]) => ({ [`grid-${rowCol(c)}-start`]: handler.bracket.cssvar(v) ?? v })],
  [/^(?:grid-)?(row|col)-end-(.+)$/, ([, c, v]) => ({ [`grid-${rowCol(c)}-end`]: handler.bracket.cssvar(v) ?? v }), { autocomplete: "(grid-row|grid-col|row|col)-(start|end)-<num>" }],
  // auto flows
  [/^(?:grid-)?auto-(rows|cols)-(.+)$/, ([, c, v], { theme }) => ({ [`grid-auto-${rowCol(c)}`]: autoDirection(c, theme, v) }), { autocomplete: "(grid-auto|auto)-(rows|cols)-<num>" }],
  // grid-auto-flow, auto-flow: uno
  // grid-flow: wind
  [/^(?:grid-auto-flow|auto-flow|grid-flow)-(.+)$/, ([, v]) => ({ "grid-auto-flow": handler.bracket.cssvar(v) })],
  [/^(?:grid-auto-flow|auto-flow|grid-flow)-(row|col|dense|row-dense|col-dense)$/, ([, v]) => ({ "grid-auto-flow": rowCol(v).replace("-", " ") }), { autocomplete: ["(grid-auto-flow|auto-flow|grid-flow)-(row|col|dense|row-dense|col-dense)"] }],
  // templates
  [/^(?:grid-)?(rows|cols)-(.+)$/, ([, c, v], { theme }) => ({
    [`grid-template-${rowCol(c)}`]: theme[`gridTemplate${rowColTheme(c)}`]?.[v] ?? handler.bracket.cssvar(v)
  })],
  [/^(?:grid-)?(rows|cols)-minmax-([\w.-]+)$/, ([, c, d]) => ({ [`grid-template-${rowCol(c)}`]: `repeat(auto-fill,minmax(${d},1fr))` })],
  [/^(?:grid-)?(rows|cols)-(\d+)$/, ([, c, d]) => ({ [`grid-template-${rowCol(c)}`]: `repeat(${d},minmax(0,1fr))` }), { autocomplete: "(grid-rows|grid-cols|rows|cols)-<num>" }],
  // areas
  [/^grid-area(s)?-(.+)$/, ([, s, v]) => {
    if (s != null)
      return { "grid-template-areas": handler.cssvar(v) ?? v.split("-").map((s2) => `"${handler.bracket(s2)}"`).join(" ") };
    return { "grid-area": handler.bracket.cssvar(v) };
  }],
  // template none
  ["grid-rows-none", { "grid-template-rows": "none" }],
  ["grid-cols-none", { "grid-template-columns": "none" }],
  // template subgrid
  ["grid-rows-subgrid", { "grid-template-rows": "subgrid" }],
  ["grid-cols-subgrid", { "grid-template-columns": "subgrid" }]
];

const overflowValues = [
  "auto",
  "hidden",
  "clip",
  "visible",
  "scroll",
  "overlay",
  ...globalKeywords
];
const overflows = [
  [/^(?:overflow|of)-(.+)$/, ([, v]) => overflowValues.includes(v) ? { overflow: v } : void 0, { autocomplete: [`(overflow|of)-(${overflowValues.join("|")})`, `(overflow|of)-(x|y)-(${overflowValues.join("|")})`] }],
  [/^(?:overflow|of)-([xy])-(.+)$/, ([, d, v]) => overflowValues.includes(v) ? { [`overflow-${d}`]: v } : void 0]
];

const lineClamps = [
  [/^line-clamp-(\d+)$/, ([, v]) => ({
    "overflow": "hidden",
    "display": "-webkit-box",
    "-webkit-box-orient": "vertical",
    "-webkit-line-clamp": v,
    "line-clamp": v
  }), { autocomplete: ["line-clamp", "line-clamp-<num>"] }],
  ...["none", ...globalKeywords].map((keyword) => [`line-clamp-${keyword}`, {
    "overflow": "visible",
    "display": "block",
    "-webkit-box-orient": "horizontal",
    "-webkit-line-clamp": keyword,
    "line-clamp": keyword
  }])
];

const positions = [
  [/^(?:position-|pos-)?(relative|absolute|fixed|sticky)$/, ([, v]) => ({ position: v }), {
    autocomplete: [
      "(position|pos)-<position>",
      "(position|pos)-<globalKeyword>",
      "<position>"
    ]
  }],
  [/^(?:position-|pos-)([-\w]+)$/, ([, v]) => globalKeywords.includes(v) ? { position: v } : void 0],
  [/^(?:position-|pos-)?(static)$/, ([, v]) => ({ position: v })]
];
const justifies = [
  // contents
  ["justify-start", { "justify-content": "flex-start" }],
  ["justify-end", { "justify-content": "flex-end" }],
  ["justify-center", { "justify-content": "center" }],
  ["justify-between", { "justify-content": "space-between" }],
  ["justify-around", { "justify-content": "space-around" }],
  ["justify-evenly", { "justify-content": "space-evenly" }],
  ["justify-stretch", { "justify-content": "stretch" }],
  ["justify-left", { "justify-content": "left" }],
  ["justify-right", { "justify-content": "right" }],
  ...makeGlobalStaticRules("justify", "justify-content"),
  // items
  ["justify-items-start", { "justify-items": "start" }],
  ["justify-items-end", { "justify-items": "end" }],
  ["justify-items-center", { "justify-items": "center" }],
  ["justify-items-stretch", { "justify-items": "stretch" }],
  ...makeGlobalStaticRules("justify-items"),
  // selfs
  ["justify-self-auto", { "justify-self": "auto" }],
  ["justify-self-start", { "justify-self": "start" }],
  ["justify-self-end", { "justify-self": "end" }],
  ["justify-self-center", { "justify-self": "center" }],
  ["justify-self-stretch", { "justify-self": "stretch" }],
  ...makeGlobalStaticRules("justify-self")
];
const orders = [
  [/^order-(.+)$/, ([, v]) => ({ order: handler.bracket.cssvar.number(v) })],
  ["order-first", { order: "-9999" }],
  ["order-last", { order: "9999" }],
  ["order-none", { order: "0" }]
];
const alignments = [
  // contents
  ["content-center", { "align-content": "center" }],
  ["content-start", { "align-content": "flex-start" }],
  ["content-end", { "align-content": "flex-end" }],
  ["content-between", { "align-content": "space-between" }],
  ["content-around", { "align-content": "space-around" }],
  ["content-evenly", { "align-content": "space-evenly" }],
  ...makeGlobalStaticRules("content", "align-content"),
  // items
  ["items-start", { "align-items": "flex-start" }],
  ["items-end", { "align-items": "flex-end" }],
  ["items-center", { "align-items": "center" }],
  ["items-baseline", { "align-items": "baseline" }],
  ["items-stretch", { "align-items": "stretch" }],
  ...makeGlobalStaticRules("items", "align-items"),
  // selfs
  ["self-auto", { "align-self": "auto" }],
  ["self-start", { "align-self": "flex-start" }],
  ["self-end", { "align-self": "flex-end" }],
  ["self-center", { "align-self": "center" }],
  ["self-stretch", { "align-self": "stretch" }],
  ["self-baseline", { "align-self": "baseline" }],
  ...makeGlobalStaticRules("self", "align-self")
];
const placements = [
  // contents
  ["place-content-center", { "place-content": "center" }],
  ["place-content-start", { "place-content": "start" }],
  ["place-content-end", { "place-content": "end" }],
  ["place-content-between", { "place-content": "space-between" }],
  ["place-content-around", { "place-content": "space-around" }],
  ["place-content-evenly", { "place-content": "space-evenly" }],
  ["place-content-stretch", { "place-content": "stretch" }],
  ...makeGlobalStaticRules("place-content"),
  // items
  ["place-items-start", { "place-items": "start" }],
  ["place-items-end", { "place-items": "end" }],
  ["place-items-center", { "place-items": "center" }],
  ["place-items-stretch", { "place-items": "stretch" }],
  ...makeGlobalStaticRules("place-items"),
  // selfs
  ["place-self-auto", { "place-self": "auto" }],
  ["place-self-start", { "place-self": "start" }],
  ["place-self-end", { "place-self": "end" }],
  ["place-self-center", { "place-self": "center" }],
  ["place-self-stretch", { "place-self": "stretch" }],
  ...makeGlobalStaticRules("place-self")
];
const flexGridJustifiesAlignments = [...justifies, ...alignments, ...placements].flatMap(([k, v]) => [
  [`flex-${k}`, v],
  [`grid-${k}`, v]
]);
function handleInsetValue(v, { theme }) {
  v = cacheRestoreSelector(v, theme?.transformRules);
  return theme.spacing?.[v] ?? (theme?.whRpx ? handler.bracket.cssvar.global.auto.fraction.rpx(v) : handler.bracket.cssvar.global.auto.fraction.remToRpx(v));
}
function handleInsetValues([, d, v], ctx) {
  const r = handleInsetValue(v, ctx);
  if (r != null && d in insetMap)
    return insetMap[d].map((i) => [i.slice(1), r]);
}
const insets = [
  [/^(?:position-|pos-)?inset-(.+)$/, ([, v], ctx) => ({ inset: handleInsetValue(v, ctx) }), {
    autocomplete: [
      "(position|pos)-inset-<directions>-$spacing",
      "(position|pos)-inset-(block|inline)-$spacing",
      "(position|pos)-inset-(bs|be|is|ie)-$spacing",
      "(position|pos)-(top|left|right|bottom)-$spacing"
    ]
  }],
  [/^(?:position-|pos-)?(start|end)-(.+)$/, handleInsetValues],
  [/^(?:position-|pos-)?inset-([xy])-(.+)$/, handleInsetValues],
  [/^(?:position-|pos-)?inset-([rltbse])-(.+)$/, handleInsetValues],
  [/^(?:position-|pos-)?inset-(block|inline)-(.+)$/, handleInsetValues],
  [/^(?:position-|pos-)?inset-([bi][se])-(.+)$/, handleInsetValues],
  [/^(?:position-|pos-)?(top|left|right|bottom)-(.+)$/, ([, d, v], ctx) => ({ [d]: handleInsetValue(v, ctx) })]
];
const floats = [
  // floats
  ["float-left", { float: "left" }],
  ["float-right", { float: "right" }],
  ["float-start", { float: "inline-start" }],
  ["float-end", { float: "inline-end" }],
  ["float-none", { float: "none" }],
  ...makeGlobalStaticRules("float"),
  // clears
  ["clear-left", { clear: "left" }],
  ["clear-right", { clear: "right" }],
  ["clear-both", { clear: "both" }],
  ["clear-start", { clear: "inline-start" }],
  ["clear-end", { clear: "inline-end" }],
  ["clear-none", { clear: "none" }],
  ...makeGlobalStaticRules("clear")
];
const zIndexes = [
  [/^(?:position-|pos-)?z([\d.]+)$/, ([, v]) => ({ "z-index": handler.number(v) })],
  [/^(?:position-|pos-)?z-(.+)$/, ([, v], { theme }) => ({ "z-index": theme.zIndex?.[v] ?? handler.bracket.cssvar.global.auto.number(v) }), { autocomplete: "z-<num>" }]
];
const boxSizing = [
  ["box-border", { "box-sizing": "border-box" }],
  ["box-content", { "box-sizing": "content-box" }],
  ...makeGlobalStaticRules("box", "box-sizing")
];

const safeArea = [
  ["p-safe", { padding: "env(safe-area-inset-top) env(safe-area-inset-right) env(safe-area-inset-bottom) env(safe-area-inset-left)" }],
  ["pt-safe", { "padding-top": "env(safe-area-inset-top)" }],
  ["pb-safe", { "padding-bottom": "env(safe-area-inset-bottom)" }],
  ["pl-safe", { "padding-left": "env(safe-area-inset-left)" }],
  ["pr-safe", { "padding-right": "env(safe-area-inset-right)" }]
];

const sizeMapping = {
  h: "height",
  w: "width",
  inline: "inline-size",
  block: "block-size"
};
function getPropName(minmax, hw) {
  return `${minmax || ""}${sizeMapping[hw]}`;
}
function getSizeValue(minmax, hw, theme, prop) {
  prop = cacheRestoreSelector(prop, theme?.transformRules);
  const str = getPropName(minmax, hw).replace(/-(\w)/g, (_, p) => p.toUpperCase());
  const v = theme[str]?.[prop];
  if (v != null)
    return v;
  switch (prop) {
    case "fit":
    case "max":
    case "min":
      return `${prop}-content`;
  }
  if (theme.whRpx)
    return handler.bracket.cssvar.global.auto.fraction.rpx(prop);
  else
    return handler.bracket.cssvar.global.auto.fraction.remToRpx(prop);
}
const sizes = [
  [/^size-(min-|max-)?(.+)$/, ([, m, s], { theme }) => ({ [getPropName(m, "w")]: getSizeValue(m, "w", theme, s), [getPropName(m, "h")]: getSizeValue(m, "h", theme, s) })],
  [/^(?:size-)?(min-|max-)?([wh])-?(.+)$/, ([, m, w, s], { theme }) => ({ [getPropName(m, w)]: getSizeValue(m, w, theme, s) })],
  [/^(?:size-)?(min-|max-)?(block|inline)-(.+)$/, ([, m, w, s], { theme }) => ({ [getPropName(m, w)]: getSizeValue(m, w, theme, s) }), {
    autocomplete: [
      "(w|h)-$width|height|maxWidth|maxHeight|minWidth|minHeight|inlineSize|blockSize|maxInlineSize|maxBlockSize|minInlineSize|minBlockSize",
      "(block|inline)-$width|height|maxWidth|maxHeight|minWidth|minHeight|inlineSize|blockSize|maxInlineSize|maxBlockSize|minInlineSize|minBlockSize",
      "(max|min)-(w|h|block|inline)",
      "(max|min)-(w|h|block|inline)-$width|height|maxWidth|maxHeight|minWidth|minHeight|inlineSize|blockSize|maxInlineSize|maxBlockSize|minInlineSize|minBlockSize",
      "(w|h)-full",
      "(max|min)-(w|h)-full"
    ]
  }],
  [/^(?:size-)?(min-|max-)?(h)-screen-(.+)$/, ([, m, h2, p], context) => ({ [getPropName(m, h2)]: handleBreakpoint(context, p, "verticalBreakpoints") })],
  [/^(?:size-)?(min-|max-)?(w)-screen-(.+)$/, ([, m, w, p], context) => ({ [getPropName(m, w)]: handleBreakpoint(context, p) }), {
    autocomplete: [
      "(w|h)-screen",
      "(min|max)-(w|h)-screen",
      "h-screen-$verticalBreakpoints",
      "(min|max)-h-screen-$verticalBreakpoints",
      "w-screen-$breakpoints",
      "(min|max)-w-screen-$breakpoints"
    ]
  }]
];
function handleBreakpoint(context, point, key = "breakpoints") {
  const bp = resolveBreakpoints(context, key);
  if (bp)
    return bp.find((i) => i.point === point)?.size;
}
function getAspectRatio(prop) {
  if (/^\d+\/\d+$/.test(prop))
    return prop;
  switch (prop) {
    case "square":
      return "1/1";
    case "video":
      return "16/9";
  }
  return handler.bracket.cssvar.global.auto.number(prop);
}
const aspectRatio = [
  [/^(?:size-)?aspect-(?:ratio-)?(.+)$/, ([, d]) => ({ "aspect-ratio": getAspectRatio(d) }), { autocomplete: ["aspect-(square|video|ratio)", "aspect-ratio-(square|video)"] }]
];

const paddings = [
  [/^pa?()-?(.+)$/, directionSize("padding"), { autocomplete: ["(m|p)<num>", "(m|p)-<num>"] }],
  [/^p-?xy()()$/, directionSize("padding"), { autocomplete: "(m|p)-(xy)" }],
  [/^p-?([xy])(?:-?(.+))?$/, directionSize("padding")],
  [/^p-?([rltbse])(?:-?(.+))?$/, directionSize("padding"), { autocomplete: "(m|p)<directions>-<num>" }],
  [/^p-(block|inline)(?:-(.+))?$/, directionSize("padding"), { autocomplete: "(m|p)-(block|inline)-<num>" }],
  [/^p-?([bi][se])(?:-?(.+))?$/, directionSize("padding"), { autocomplete: "(m|p)-(bs|be|is|ie)-<num>" }]
];
const margins = [
  [/^ma?()-?(.+)$/, directionSize("margin")],
  [/^m-?xy()()$/, directionSize("margin")],
  [/^m-?([xy])(?:-?(.+))?$/, directionSize("margin")],
  [/^m-?([rltbse])(?:-?(.+))?$/, directionSize("margin")],
  [/^m-(block|inline)(?:-(.+))?$/, directionSize("margin")],
  [/^m-?([bi][se])(?:-?(.+))?$/, directionSize("margin")]
];
const spaces = [
  [/^space-([xy])-(.+)$/, handlerSpace, { autocomplete: ["space-(x|y|block|inline)", "space-(x|y|block|inline)-reverse", "space-(x|y|block|inline)-$spacing"] }],
  [/^space-([xy])-reverse$/, ([, d]) => ({ [`--un-space-${d}-reverse`]: 1 })],
  [/^space-(block|inline)-(.+)$/, handlerSpace],
  [/^space-(block|inline)-reverse$/, ([, d]) => ({ [`--un-space-${d}-reverse`]: 1 })]
];
function handlerSpace([, d, s], { theme }) {
  let v = theme.spacing?.[s || "DEFAULT"] ?? handler.bracket.cssvar.auto.fraction.remToRpx(s || "1");
  if (v != null) {
    if (v === "0")
      v = "0px";
    const results = directionMap[d].map((item) => {
      const key = `margin${item}`;
      const value = item.endsWith("right") || item.endsWith("bottom") ? `calc(${v} * var(--un-space-${d}-reverse))` : `calc(${v} * calc(1 - var(--un-space-${d}-reverse)))`;
      return [key, value];
    });
    if (results) {
      return [
        [`--un-space-${d}-reverse`, 0],
        ...results
      ];
    }
  }
}

const cursorValues = ["auto", "default", "none", "context-menu", "help", "pointer", "progress", "wait", "cell", "crosshair", "text", "vertical-text", "alias", "copy", "move", "no-drop", "not-allowed", "grab", "grabbing", "all-scroll", "col-resize", "row-resize", "n-resize", "e-resize", "s-resize", "w-resize", "ne-resize", "nw-resize", "se-resize", "sw-resize", "ew-resize", "ns-resize", "nesw-resize", "nwse-resize", "zoom-in", "zoom-out"];
const containValues = ["none", "strict", "content", "size", "inline-size", "layout", "style", "paint"];
const displays = [
  ["inline", { display: "inline" }],
  ["block", { display: "block" }],
  ["inline-block", { display: "inline-block" }],
  ["contents", { display: "contents" }],
  ["flow-root", { display: "flow-root" }],
  ["list-item", { display: "list-item" }],
  ["hidden", { display: "none" }],
  [/^display-(.+)$/, ([, c]) => ({ display: handler.bracket.cssvar.global(c) })]
];
const appearances = [
  ["visible", { visibility: "visible" }],
  ["invisible", { visibility: "hidden" }],
  ["backface-visible", { "backface-visibility": "visible" }],
  ["backface-hidden", { "backface-visibility": "hidden" }],
  ...makeGlobalStaticRules("backface", "backface-visibility")
];
const cursors = [
  [/^cursor-(.+)$/, ([, c]) => ({ cursor: handler.bracket.cssvar.global(c) })],
  ...cursorValues.map((v) => [`cursor-${v}`, { cursor: v }])
];
const contains = [
  [/^contain-(.*)$/, ([, d]) => {
    if (handler.bracket(d) != null) {
      return {
        contain: handler.bracket(d).split(" ").map((e) => handler.cssvar.fraction(e) ?? e).join(" ")
      };
    }
    return containValues.includes(d) ? { contain: d } : void 0;
  }]
];
const pointerEvents = [
  ["pointer-events-auto", { "pointer-events": "auto" }],
  ["pointer-events-none", { "pointer-events": "none" }],
  ...makeGlobalStaticRules("pointer-events")
];
const resizes = [
  ["resize-x", { resize: "horizontal" }],
  ["resize-y", { resize: "vertical" }],
  ["resize", { resize: "both" }],
  ["resize-none", { resize: "none" }],
  ...makeGlobalStaticRules("resize")
];
const userSelects = [
  ["select-auto", { "-webkit-user-select": "auto", "user-select": "auto" }],
  ["select-all", { "-webkit-user-select": "all", "user-select": "all" }],
  ["select-text", { "-webkit-user-select": "text", "user-select": "text" }],
  ["select-none", { "-webkit-user-select": "none", "user-select": "none" }],
  ...makeGlobalStaticRules("select", "user-select")
];
const whitespaces = [
  [
    /^(?:whitespace-|ws-)([-\w]+)$/,
    ([, v]) => ["normal", "nowrap", "pre", "pre-line", "pre-wrap", "break-spaces", ...globalKeywords].includes(v) ? { "white-space": v } : void 0,
    { autocomplete: "(whitespace|ws)-(normal|nowrap|pre|pre-line|pre-wrap|break-spaces)" }
  ]
];
const contentVisibility = [
  [/^intrinsic-size-(.+)$/, ([, d], { theme }) => {
    d = cacheRestoreSelector(d, theme?.transformRules);
    return { "contain-intrinsic-size": handler.bracket.cssvar.global.fraction.rem(d) };
  }, { autocomplete: "intrinsic-size-<num>" }],
  ["content-visibility-visible", { "content-visibility": "visible" }],
  ["content-visibility-hidden", { "content-visibility": "hidden" }],
  ["content-visibility-auto", { "content-visibility": "auto" }],
  ...makeGlobalStaticRules("content-visibility")
];
const contents = [
  [/^content-(.+)$/, ([, v]) => ({ content: handler.bracket.cssvar(v) })],
  ["content-empty", { content: '""' }],
  ["content-none", { content: "none" }]
];
const breaks = [
  ["break-normal", { "overflow-wrap": "normal", "word-break": "normal" }],
  ["break-words", { "overflow-wrap": "break-word" }],
  ["break-all", { "word-break": "break-all" }],
  ["break-keep", { "word-break": "keep-all" }],
  ["break-anywhere", { "overflow-wrap": "anywhere" }]
];
const textWraps = [
  ["text-wrap", { "text-wrap": "wrap" }],
  ["text-nowrap", { "text-wrap": "nowrap" }],
  ["text-balance", { "text-wrap": "balance" }],
  ["text-pretty", { "text-wrap": "pretty" }]
];
const textOverflows = [
  ["truncate", { "overflow": "hidden", "text-overflow": "ellipsis", "white-space": "nowrap" }],
  ["text-truncate", { "overflow": "hidden", "text-overflow": "ellipsis", "white-space": "nowrap" }],
  ["text-ellipsis", { "text-overflow": "ellipsis" }],
  ["text-clip", { "text-overflow": "clip" }]
];
const textTransforms = [
  ["case-upper", { "text-transform": "uppercase" }],
  ["case-lower", { "text-transform": "lowercase" }],
  ["case-capital", { "text-transform": "capitalize" }],
  ["case-normal", { "text-transform": "none" }],
  ...makeGlobalStaticRules("case", "text-transform"),
  // tailwind compat
  ["uppercase", { "text-transform": "uppercase" }],
  ["lowercase", { "text-transform": "lowercase" }],
  ["capitalize", { "text-transform": "capitalize" }],
  ["normal-case", { "text-transform": "none" }]
];
const fontStyles = [
  ["italic", { "font-style": "italic" }],
  ["not-italic", { "font-style": "normal" }],
  ["font-italic", { "font-style": "italic" }],
  ["font-not-italic", { "font-style": "normal" }],
  ["oblique", { "font-style": "oblique" }],
  ["not-oblique", { "font-style": "normal" }],
  ["font-oblique", { "font-style": "oblique" }],
  ["font-not-oblique", { "font-style": "normal" }]
];
const fontSmoothings = [
  ["antialiased", {
    "-webkit-font-smoothing": "antialiased",
    "-moz-osx-font-smoothing": "grayscale"
  }],
  ["subpixel-antialiased", {
    "-webkit-font-smoothing": "auto",
    "-moz-osx-font-smoothing": "auto"
  }]
];
const hyphens = [
  ...["manual", "auto", "none", ...globalKeywords].map((keyword) => [`hyphens-${keyword}`, {
    "-webkit-hyphens": keyword,
    "-ms-hyphens": keyword,
    "hyphens": keyword
  }])
];
const writingModes = [
  ["write-vertical-right", { "writing-mode": "vertical-rl" }],
  ["write-vertical-left", { "writing-mode": "vertical-lr" }],
  ["write-normal", { "writing-mode": "horizontal-tb" }],
  ...makeGlobalStaticRules("write", "writing-mode")
];
const writingOrientations = [
  ["write-orient-mixed", { "text-orientation": "mixed" }],
  ["write-orient-sideways", { "text-orientation": "sideways" }],
  ["write-orient-upright", { "text-orientation": "upright" }],
  ...makeGlobalStaticRules("write-orient", "text-orientation")
];
const screenReadersAccess = [
  [
    "sr-only",
    {
      "position": "absolute",
      "width": "1px",
      "height": "1px",
      "padding": "0",
      "margin": "-1px",
      "overflow": "hidden",
      "clip": "rect(0,0,0,0)",
      "white-space": "nowrap",
      "border-width": 0
    }
  ],
  [
    "not-sr-only",
    {
      "position": "static",
      "width": "auto",
      "height": "auto",
      "padding": "0",
      "margin": "0",
      "overflow": "visible",
      "clip": "auto",
      "white-space": "normal"
    }
  ]
];
const isolations = [
  ["isolate", { isolation: "isolate" }],
  ["isolate-auto", { isolation: "auto" }],
  ["isolation-auto", { isolation: "auto" }]
];
const objectPositions = [
  // object fit
  ["object-cover", { "object-fit": "cover" }],
  ["object-contain", { "object-fit": "contain" }],
  ["object-fill", { "object-fit": "fill" }],
  ["object-scale-down", { "object-fit": "scale-down" }],
  ["object-none", { "object-fit": "none" }],
  // object position
  [/^object-(.+)$/, ([, d], { theme }) => {
    d = cacheRestoreSelector(d, theme?.transformRules);
    if (positionMap[d])
      return { "object-position": positionMap[d] };
    if (handler.bracketOfPosition(d) != null)
      return { "object-position": handler.bracketOfPosition(d).split(" ").map((e) => handler.position.fraction.auto.px.cssvar(e) ?? e).join(" ") };
  }, { autocomplete: `object-(${Object.keys(positionMap).join("|")})` }]
];
const backgroundBlendModes = [
  ["bg-blend-multiply", { "background-blend-mode": "multiply" }],
  ["bg-blend-screen", { "background-blend-mode": "screen" }],
  ["bg-blend-overlay", { "background-blend-mode": "overlay" }],
  ["bg-blend-darken", { "background-blend-mode": "darken" }],
  ["bg-blend-lighten", { "background-blend-mode": "lighten" }],
  ["bg-blend-color-dodge", { "background-blend-mode": "color-dodge" }],
  ["bg-blend-color-burn", { "background-blend-mode": "color-burn" }],
  ["bg-blend-hard-light", { "background-blend-mode": "hard-light" }],
  ["bg-blend-soft-light", { "background-blend-mode": "soft-light" }],
  ["bg-blend-difference", { "background-blend-mode": "difference" }],
  ["bg-blend-exclusion", { "background-blend-mode": "exclusion" }],
  ["bg-blend-hue", { "background-blend-mode": "hue" }],
  ["bg-blend-saturation", { "background-blend-mode": "saturation" }],
  ["bg-blend-color", { "background-blend-mode": "color" }],
  ["bg-blend-luminosity", { "background-blend-mode": "luminosity" }],
  ["bg-blend-normal", { "background-blend-mode": "normal" }],
  ...makeGlobalStaticRules("bg-blend", "background-blend")
];
const mixBlendModes = [
  ["mix-blend-multiply", { "mix-blend-mode": "multiply" }],
  ["mix-blend-screen", { "mix-blend-mode": "screen" }],
  ["mix-blend-overlay", { "mix-blend-mode": "overlay" }],
  ["mix-blend-darken", { "mix-blend-mode": "darken" }],
  ["mix-blend-lighten", { "mix-blend-mode": "lighten" }],
  ["mix-blend-color-dodge", { "mix-blend-mode": "color-dodge" }],
  ["mix-blend-color-burn", { "mix-blend-mode": "color-burn" }],
  ["mix-blend-hard-light", { "mix-blend-mode": "hard-light" }],
  ["mix-blend-soft-light", { "mix-blend-mode": "soft-light" }],
  ["mix-blend-difference", { "mix-blend-mode": "difference" }],
  ["mix-blend-exclusion", { "mix-blend-mode": "exclusion" }],
  ["mix-blend-hue", { "mix-blend-mode": "hue" }],
  ["mix-blend-saturation", { "mix-blend-mode": "saturation" }],
  ["mix-blend-color", { "mix-blend-mode": "color" }],
  ["mix-blend-luminosity", { "mix-blend-mode": "luminosity" }],
  ["mix-blend-plus-lighter", { "mix-blend-mode": "plus-lighter" }],
  ["mix-blend-normal", { "mix-blend-mode": "normal" }],
  ...makeGlobalStaticRules("mix-blend")
];
const dynamicViewportHeight = [
  ["min-h-dvh", { "min-height": "100dvh" }],
  ["min-h-svh", { "min-height": "100svh" }],
  ["min-h-lvh", { "min-height": "100lvh" }],
  ["h-dvh", { height: "100dvh" }],
  ["h-svh", { height: "100svh" }],
  ["h-lvh", { height: "100lvh" }],
  ["max-h-dvh", { "max-height": "100dvh" }],
  ["max-h-svh", { "max-height": "100svh" }],
  ["max-h-lvh", { "max-height": "100lvh" }]
];

const svgUtilities = [
  // fills
  [/^fill-(.+)$/, colorResolver("fill", "fill", "backgroundColor"), { autocomplete: "fill-$colors" }],
  [/^fill-op(?:acity)?-?(.+)$/, ([, opacity]) => ({ "--un-fill-opacity": handler.bracket.percent.cssvar(opacity) }), { autocomplete: "fill-(op|opacity)-<percent>" }],
  ["fill-none", { fill: "none" }],
  // stroke size
  [/^stroke-(?:width-|size-)?(.+)$/, handleWidth, { autocomplete: ["stroke-width-$lineWidth", "stroke-size-$lineWidth"] }],
  // stroke dash
  [/^stroke-dash-(.+)$/, ([, s]) => ({ "stroke-dasharray": handler.bracket.cssvar.number(s) }), { autocomplete: "stroke-dash-<num>" }],
  [/^stroke-offset-(.+)$/, ([, s], { theme }) => ({ "stroke-dashoffset": theme.lineWidth?.[s] ?? handler.bracket.cssvar.px.numberWithUnit(s) }), { autocomplete: "stroke-offset-$lineWidth" }],
  // stroke colors
  [/^stroke-(.+)$/, handleColorOrWidth, { autocomplete: "stroke-$colors" }],
  [/^stroke-op(?:acity)?-?(.+)$/, ([, opacity]) => ({ "--un-stroke-opacity": handler.bracket.percent.cssvar(opacity) }), { autocomplete: "stroke-(op|opacity)-<percent>" }],
  // line cap
  ["stroke-cap-square", { "stroke-linecap": "square" }],
  ["stroke-cap-round", { "stroke-linecap": "round" }],
  ["stroke-cap-auto", { "stroke-linecap": "butt" }],
  // line join
  ["stroke-join-arcs", { "stroke-linejoin": "arcs" }],
  ["stroke-join-bevel", { "stroke-linejoin": "bevel" }],
  ["stroke-join-clip", { "stroke-linejoin": "miter-clip" }],
  ["stroke-join-round", { "stroke-linejoin": "round" }],
  ["stroke-join-auto", { "stroke-linejoin": "miter" }],
  // none
  ["stroke-none", { stroke: "none" }]
];
function handleWidth([, b], { theme }) {
  b = cacheRestoreSelector(b, theme?.transformRules);
  return { "stroke-width": theme.lineWidth?.[b] ?? handler.bracket.cssvar.fraction.px.number(b) };
}
function handleColorOrWidth(match, ctx) {
  if (isCSSMathFn(handler.bracket(match[1])))
    return handleWidth(match, ctx);
  return colorResolver("stroke", "stroke", "borderColor")(match, ctx);
}

function resolveTransitionProperty(prop, theme) {
  let p;
  if (h.cssvar(prop) != null) {
    p = h.cssvar(prop);
  } else {
    if (prop.startsWith("[") && prop.endsWith("]")) {
      prop = prop.slice(1, -1);
    }
    const props = prop.split(",").map((p2) => theme.transitionProperty?.[p2] ?? h.properties(p2));
    if (props.every(Boolean)) {
      p = props.join(",");
    }
  }
  return p;
}
const transitions = [
  // transition
  [
    /^transition(?:-(\D+?))?(?:-(\d+))?$/,
    ([, prop, d], { theme }) => {
      if (!prop && !d) {
        return {
          "transition-property": theme.transitionProperty?.DEFAULT,
          "transition-timing-function": theme.easing?.DEFAULT,
          "transition-duration": theme.duration?.DEFAULT ?? h.time("150")
        };
      } else if (prop != null) {
        const p = resolveTransitionProperty(prop, theme);
        const duration = theme.duration?.[d || "DEFAULT"] ?? h.time(d || "150");
        if (p) {
          return {
            "transition-property": p,
            "transition-timing-function": theme.easing?.DEFAULT,
            "transition-duration": duration
          };
        }
      } else if (d != null) {
        return {
          "transition-property": theme.transitionProperty?.DEFAULT,
          "transition-timing-function": theme.easing?.DEFAULT,
          "transition-duration": theme.duration?.[d] ?? h.time(d)
        };
      }
    },
    {
      autocomplete: "transition-$transitionProperty-$duration"
    }
  ],
  // timings
  [
    /^(?:transition-)?duration-(.+)$/,
    ([, d], { theme }) => ({ "transition-duration": theme.duration?.[d || "DEFAULT"] ?? h.bracket.cssvar.time(d) }),
    { autocomplete: ["transition-duration-$duration", "duration-$duration"] }
  ],
  [
    /^(?:transition-)?delay-(.+)$/,
    ([, d], { theme }) => ({ "transition-delay": theme.duration?.[d || "DEFAULT"] ?? h.bracket.cssvar.time(d) }),
    { autocomplete: ["transition-delay-$duration", "delay-$duration"] }
  ],
  [
    /^(?:transition-)?ease(?:-(.+))?$/,
    ([, d], { theme }) => ({ "transition-timing-function": theme.easing?.[d || "DEFAULT"] ?? h.bracket.cssvar(d) }),
    { autocomplete: ["transition-ease-(linear|in|out|in-out|DEFAULT)", "ease-(linear|in|out|in-out|DEFAULT)"] }
  ],
  // props
  [
    /^(?:transition-)?property-(.+)$/,
    ([, v], { theme }) => {
      const p = h.global(v) || resolveTransitionProperty(v, theme);
      if (p)
        return { "transition-property": p };
    },
    { autocomplete: [
      `transition-property-(${[...globalKeywords].join("|")})`,
      "transition-property-$transitionProperty",
      "property-$transitionProperty"
    ] }
  ],
  // none
  ["transition-none", { transition: "none" }],
  ...makeGlobalStaticRules("transition"),
  // behavior
  ["transition-discrete", { "transition-behavior": "allow-discrete" }],
  ["transition-normal", { "transition-behavior": "normal" }]
];

const fonts = [
  // size
  [
    /^text-(.+)$/,
    handleText,
    { autocomplete: "text-$fontSize" }
  ],
  [/^(?:text|font)-size-(.+)$/, handleSize, { autocomplete: "text-size-$fontSize" }],
  // text colors
  [/^text-(?:color-)?(.+)$/, handlerColorOrSize, { autocomplete: "text-$colors" }],
  // colors
  [/^(?:color|c)-(.+)$/, colorResolver("color", "text", "textColor"), { autocomplete: "(color|c)-$colors" }],
  // style
  [/^(?:text|color|c)-(.+)$/, ([, v]) => globalKeywords.includes(v) ? { color: v } : void 0, { autocomplete: `(text|color|c)-(${globalKeywords.join("|")})` }],
  // opacity
  [/^(?:text|color|c)-op(?:acity)?-?(.+)$/, ([, opacity]) => ({ "--un-text-opacity": handler.bracket.percent.cssvar(opacity) }), { autocomplete: "(text|color|c)-(op|opacity)-<percent>" }],
  // weights
  [
    /^(?:font|fw)-?([^-]+)$/,
    ([, s], { theme }) => ({ "font-weight": theme.fontWeight?.[s] || handler.bracket.global.number(s) }),
    {
      autocomplete: [
        "(font|fw)-(100|200|300|400|500|600|700|800|900)",
        "(font|fw)-$fontWeight"
      ]
    }
  ],
  // leadings
  [
    /^(?:font-)?(?:leading|lh|line-height)-(.+)$/,
    ([, s], { theme }) => {
      s = cacheRestoreSelector(s, theme.transformRules);
      return { "line-height": handleThemeByKey(s, theme, "lineHeight") };
    },
    { autocomplete: "(leading|lh|line-height)-$lineHeight" }
  ],
  // synthesis
  ["font-synthesis-weight", { "font-synthesis": "weight" }],
  ["font-synthesis-style", { "font-synthesis": "style" }],
  ["font-synthesis-small-caps", { "font-synthesis": "small-caps" }],
  ["font-synthesis-none", { "font-synthesis": "none" }],
  [/^font-synthesis-(.+)$/, ([, s]) => ({ "font-synthesis": handler.bracket.cssvar.global(s) })],
  // tracking
  [
    /^(?:font-)?tracking-(.+)$/,
    ([, s], { theme }) => ({ "letter-spacing": theme.letterSpacing?.[s] || handler.bracket.cssvar.global.remToRpx(s) }),
    { autocomplete: "tracking-$letterSpacing" }
  ],
  // word-spacing
  [
    /^(?:font-)?word-spacing-(.+)$/,
    ([, s], { theme }) => ({ "word-spacing": theme.wordSpacing?.[s] || handler.bracket.cssvar.global.remToRpx(s) }),
    { autocomplete: "word-spacing-$wordSpacing" }
  ],
  // stretch
  ["font-stretch-normal", { "font-stretch": "normal" }],
  ["font-stretch-ultra-condensed", { "font-stretch": "ultra-condensed" }],
  ["font-stretch-extra-condensed", { "font-stretch": "extra-condensed" }],
  ["font-stretch-condensed", { "font-stretch": "condensed" }],
  ["font-stretch-semi-condensed", { "font-stretch": "semi-condensed" }],
  ["font-stretch-semi-expanded", { "font-stretch": "semi-expanded" }],
  ["font-stretch-expanded", { "font-stretch": "expanded" }],
  ["font-stretch-extra-expanded", { "font-stretch": "extra-expanded" }],
  ["font-stretch-ultra-expanded", { "font-stretch": "ultra-expanded" }],
  [
    /^font-stretch-(.+)$/,
    ([, s]) => ({ "font-stretch": handler.bracket.cssvar.fraction.global(s) }),
    { autocomplete: "font-stretch-<percentage>" }
  ],
  // family
  [
    /^font-(.+)$/,
    ([, d], { theme }) => ({ "font-family": theme.fontFamily?.[d] || handler.bracket.cssvar.global(d) }),
    { autocomplete: "font-$fontFamily" }
  ]
];
const tabSizes = [
  [/^tab(?:-(.+))?$/, ([, s]) => {
    const v = handler.bracket.cssvar.global.number(s || "4");
    if (v != null) {
      return {
        "-moz-tab-size": v,
        "-o-tab-size": v,
        "tab-size": v
      };
    }
  }]
];
const textIndents = [
  [/^indent(?:-(.+))?$/, ([, s], { theme }) => {
    s = cacheRestoreSelector(s, theme?.transformRules);
    return { "text-indent": theme.textIndent?.[s || "DEFAULT"] || handler.bracket.cssvar.global.fraction.remToRpx(s) };
  }, { autocomplete: "indent-$textIndent" }]
];
const textStrokes = [
  // widths
  [/^text-stroke(?:-(.+))?$/, ([, s], { theme }) => ({ "-webkit-text-stroke-width": theme.textStrokeWidth?.[s || "DEFAULT"] || handler.bracket.cssvar.px(s) }), { autocomplete: "text-stroke-$textStrokeWidth" }],
  // colors
  [/^text-stroke-(.+)$/, colorResolver("-webkit-text-stroke-color", "text-stroke", "borderColor"), { autocomplete: "text-stroke-$colors" }],
  [/^text-stroke-op(?:acity)?-?(.+)$/, ([, opacity]) => ({ "--un-text-stroke-opacity": handler.bracket.percent.cssvar(opacity) }), { autocomplete: "text-stroke-(op|opacity)-<percent>" }]
];
const textShadows = [
  [/^text-shadow(?:-(.+))?$/, ([, s], { theme }) => {
    const v = theme.textShadow?.[s || "DEFAULT"];
    if (v != null) {
      return {
        "--un-text-shadow": colorableShadows(v, "--un-text-shadow-color").join(","),
        "text-shadow": "var(--un-text-shadow)"
      };
    }
    return { "text-shadow": handler.bracket.cssvar.global(s) };
  }, { autocomplete: "text-shadow-$textShadow" }],
  // colors
  [/^text-shadow-color-(.+)$/, colorResolver("--un-text-shadow-color", "text-shadow", "shadowColor"), { autocomplete: "text-shadow-color-$colors" }],
  [/^text-shadow-color-op(?:acity)?-?(.+)$/, ([, opacity]) => ({ "--un-text-shadow-opacity": handler.bracket.percent.cssvar(opacity) }), { autocomplete: "text-shadow-color-(op|opacity)-<percent>" }]
];
function handleThemeByKey(s, theme, key) {
  return theme[key]?.[s] || handler.bracket.cssvar.global.remToRpx(s);
}
function handleSize([, s], { theme }) {
  const themed = toArray(theme.fontSize?.[s]);
  const size = themed?.[0] ?? (theme.whRpx ? handler.bracket.cssvar.global.rpx(s) : handler.bracket.cssvar.global.remToRpx(s));
  if (size != null)
    return { "font-size": size };
}
function handlerColorOrSize(match, ctx) {
  if (isCSSMathFn(handler.bracket(match[1])))
    return handleSize(match, ctx);
  return colorResolver("color", "text", "textColor")(match, ctx);
}
function handleText([, s = "base"], { theme }) {
  const split = splitShorthand(s, "length", theme);
  if (!split)
    return;
  const [size, leading] = split;
  const sizePairs = toArray(theme.fontSize?.[size]);
  const lineHeight = leading ? handleThemeByKey(leading, theme, "lineHeight") : void 0;
  if (sizePairs?.[0]) {
    const [fontSize2, height, letterSpacing] = sizePairs;
    if (typeof height === "object") {
      return {
        "font-size": fontSize2,
        ...height
      };
    }
    return {
      "font-size": fontSize2,
      "line-height": lineHeight ?? height ?? "1",
      "letter-spacing": letterSpacing ? handleThemeByKey(letterSpacing, theme, "letterSpacing") : void 0
    };
  }
  const fontSize = handler.bracketOfLength.remToRpx(size);
  if (lineHeight && fontSize) {
    return {
      "font-size": fontSize,
      "line-height": lineHeight
    };
  }
  if (theme.whRpx)
    return { "font-size": handler.bracketOfLength.rpx(s) };
  else
    return { "font-size": handler.bracketOfLength.remToRpx(s) };
}

const variablesAbbrMap = {
  backface: "backface-visibility",
  break: "word-break",
  case: "text-transform",
  content: "align-content",
  fw: "font-weight",
  items: "align-items",
  justify: "justify-content",
  select: "user-select",
  self: "align-self",
  vertical: "vertical-align",
  visible: "visibility",
  whitespace: "white-space",
  ws: "white-space"
};
const cssVariables = [
  [/^(.+?)-(\$.+)$/, ([, name, varname]) => {
    const prop = variablesAbbrMap[name];
    if (prop)
      return { [prop]: h.cssvar(varname) };
  }]
];
const cssProperty = [
  [/^\[(.*)\]$/, ([_, body]) => {
    if (!body.includes(":"))
      return;
    const [prop, ...rest] = body.split(":");
    const value = rest.join(":");
    if (!isURI(body) && /^[a-z-]+$/.test(prop) && isValidCSSBody(value)) {
      const parsed = h.bracket(`[${value}]`);
      if (parsed)
        return { [prop]: parsed };
    }
  }]
];
function isValidCSSBody(body) {
  let i = 0;
  function findUntil(c) {
    while (i < body.length) {
      i += 1;
      const char = body[i];
      if (char === c)
        return true;
    }
    return false;
  }
  for (i = 0; i < body.length; i++) {
    const c = body[i];
    if ("\"`'".includes(c)) {
      if (!findUntil(c))
        return false;
    } else if (c === "(") {
      if (!findUntil(")"))
        return false;
    } else if ("[]{}:".includes(c)) {
      return false;
    }
  }
  return true;
}
function isURI(declaration) {
  if (!declaration.includes("://"))
    return false;
  try {
    return new URL(declaration).host !== "";
  } catch {
    return false;
  }
}

const rules = [
  cssVariables,
  cssProperty,
  contains,
  screenReadersAccess,
  pointerEvents,
  appearances,
  positions,
  insets,
  lineClamps,
  isolations,
  zIndexes,
  orders,
  grids,
  floats,
  margins,
  boxSizing,
  displays,
  aspectRatio,
  sizes,
  flex,
  transforms,
  animations,
  cursors,
  userSelects,
  resizes,
  appearance,
  placements,
  alignments,
  justifies,
  gaps,
  flexGridJustifiesAlignments,
  spaces,
  divides,
  overflows,
  textOverflows,
  whitespaces,
  breaks,
  borders,
  bgColors,
  backgroundStyles,
  colorScheme,
  svgUtilities,
  objectPositions,
  paddings,
  safeArea,
  textAligns,
  textIndents,
  textWraps,
  verticalAligns,
  fonts,
  textTransforms,
  fontStyles,
  textDecorations,
  fontSmoothings,
  tabSizes,
  textStrokes,
  textShadows,
  hyphens,
  writingModes,
  writingOrientations,
  opacity,
  backgroundBlendModes,
  mixBlendModes,
  boxShadows,
  outline,
  rings,
  filters,
  transitions,
  willChange,
  contentVisibility,
  contents,
  containerParent,
  dynamicViewportHeight
].flat(1);

export { fontSmoothings as $, placements as A, flexGridJustifiesAlignments as B, insets as C, floats as D, zIndexes as E, boxSizing as F, safeArea as G, sizes as H, aspectRatio as I, paddings as J, margins as K, spaces as L, displays as M, appearances as N, cursors as O, contains as P, pointerEvents as Q, resizes as R, userSelects as S, whitespaces as T, contentVisibility as U, contents as V, breaks as W, textWraps as X, textOverflows as Y, textTransforms as Z, fontStyles as _, animations as a, hyphens as a0, writingModes as a1, writingOrientations as a2, screenReadersAccess as a3, isolations as a4, objectPositions as a5, backgroundBlendModes as a6, mixBlendModes as a7, dynamicViewportHeight as a8, svgUtilities as a9, transitions as aa, fonts as ab, tabSizes as ac, textIndents as ad, textStrokes as ae, textShadows as af, cssVariables as ag, cssProperty as ah, backgroundStyles as b, appearance as c, borderStyles as d, borders as e, opacity as f, bgColors as g, handlerBorderStyle as h, colorScheme as i, containerParent as j, textDecorations as k, divides as l, flex as m, gaps as n, outline as o, grids as p, overflows as q, rules as r, lineClamps as s, textAligns as t, positions as u, verticalAligns as v, willChange as w, justifies as x, orders as y, alignments as z };
