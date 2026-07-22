'use strict';

const utilities = require('./shared/unocss-preset-weapp.a1c787c8.cjs');
const core = require('@unocss/core');
const utils = require('unplugin-transform-class/utils');
require('@unocss/rule-utils');

function variantMatcher(name, handler, transformRules = utils.defaultRules) {
  let re;
  return {
    name,
    match(input, ctx) {
      if (!re)
        re = new RegExp(`^${core.escapeRegExp(name)}(?:${ctx.generator.config.separators.join("|")})`);
      input = utils.cacheRestoreSelector(input, transformRules);
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
function variantParentMatcher(name, parent, transformRules = utils.defaultRules) {
  let re;
  return {
    name,
    match(input, ctx) {
      if (!re)
        re = new RegExp(`^${core.escapeRegExp(name)}(?:${ctx.generator.config.separators.join("|")})`);
      input = utils.cacheRestoreSelector(input, transformRules);
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

exports.CONTROL_MINI_NO_NEGATIVE = utilities.CONTROL_MINI_NO_NEGATIVE;
exports.colorResolver = utilities.colorResolver;
exports.colorableShadows = utilities.colorableShadows;
exports.cornerMap = utilities.cornerMap;
exports.cssMathFnRE = utilities.cssMathFnRE;
exports.cssVarFnRE = utilities.cssVarFnRE;
exports.directionMap = utilities.directionMap;
exports.directionSize = utilities.directionSize;
exports.globalKeywords = utilities.globalKeywords;
exports.h = utilities.h;
exports.handler = utilities.handler;
exports.hasParseableColor = utilities.hasParseableColor;
exports.insetMap = utilities.insetMap;
exports.isCSSMathFn = utilities.isCSSMathFn;
exports.isSize = utilities.isSize;
exports.makeGlobalStaticRules = utilities.makeGlobalStaticRules;
exports.parseColor = utilities.parseColor;
exports.positionMap = utilities.positionMap;
exports.resolveBreakpoints = utilities.resolveBreakpoints;
exports.resolveVerticalBreakpoints = utilities.resolveVerticalBreakpoints;
exports.splitShorthand = utilities.splitShorthand;
exports.transformXYZ = utilities.transformXYZ;
exports.valueHandlers = utilities.valueHandlers;
exports.xyzArray = utilities.xyzArray;
exports.xyzMap = utilities.xyzMap;
exports.variantMatcher = variantMatcher;
exports.variantParentMatcher = variantParentMatcher;
