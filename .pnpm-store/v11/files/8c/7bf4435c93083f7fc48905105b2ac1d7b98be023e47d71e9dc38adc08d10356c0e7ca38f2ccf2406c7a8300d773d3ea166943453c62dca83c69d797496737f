export { C as CONTROL_MINI_NO_NEGATIVE, c as colorResolver, a as colorableShadows, j as cornerMap, q as cssMathFnRE, u as cssVarFnRE, f as directionMap, n as directionSize, g as globalKeywords, h, b as handler, d as hasParseableColor, l as insetMap, i as isCSSMathFn, k as isSize, m as makeGlobalStaticRules, e as parseColor, p as positionMap, r as resolveBreakpoints, w as resolveVerticalBreakpoints, s as splitShorthand, t as transformXYZ, v as valueHandlers, o as xyzArray, x as xyzMap } from './shared/unocss-preset-weapp.d4ee9f3f.mjs';
import { escapeRegExp } from '@unocss/core';
import { cacheRestoreSelector, defaultRules } from 'unplugin-transform-class/utils';
import '@unocss/rule-utils';

function variantMatcher(name, handler, transformRules = defaultRules) {
  let re;
  return {
    name,
    match(input, ctx) {
      if (!re)
        re = new RegExp(`^${escapeRegExp(name)}(?:${ctx.generator.config.separators.join("|")})`);
      input = cacheRestoreSelector(input, transformRules);
      const match = input.match(re);
      if (match) {
        return {
          matcher: input.slice(match[0].length),
          handle: (input2, next) => next({
            ...input2,
            ...handler(input2)
          })
        };
      }
    },
    autocomplete: `${name}:`
  };
}
function variantParentMatcher(name, parent, transformRules = defaultRules) {
  let re;
  return {
    name,
    match(input, ctx) {
      if (!re)
        re = new RegExp(`^${escapeRegExp(name)}(?:${ctx.generator.config.separators.join("|")})`);
      input = cacheRestoreSelector(input, transformRules);
      const match = input.match(re);
      if (match) {
        return {
          matcher: input.slice(match[0].length),
          handle: (input2, next) => next({
            ...input2,
            parent: `${input2.parent ? `${input2.parent} $$ ` : ""}${parent}`
          })
        };
      }
    },
    autocomplete: `${name}:`
  };
}

export { variantMatcher, variantParentMatcher };
