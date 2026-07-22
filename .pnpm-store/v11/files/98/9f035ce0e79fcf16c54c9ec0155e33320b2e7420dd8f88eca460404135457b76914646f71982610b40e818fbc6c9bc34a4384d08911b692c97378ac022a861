'use strict';

const _default = require('./shared/unocss-preset-weapp.0eb057b4.cjs');
const transform = require('./shared/unocss-preset-weapp.9b4df821.cjs');
require('./shared/unocss-preset-weapp.a1c787c8.cjs');
require('@unocss/core');
require('@unocss/rule-utils');
require('unplugin-transform-class/utils');

const questionMark = [
  [
    /^(where|\?)$/,
    (_, { constructCSS, generator }) => {
      if (generator.userConfig.envMode === "dev")
        return `@keyframes __un_qm{0%{box-shadow:inset 4px 4px #ff1e90, inset -4px -4px #ff1e90}100%{box-shadow:inset 8px 8px #3399ff, inset -8px -8px #3399ff}} ${constructCSS({ animation: "__un_qm 0.5s ease-in-out alternate infinite" })}`;
    }
  ]
];

exports.alignments = _default.alignments;
exports.animations = _default.animations;
exports.appearance = _default.appearance;
exports.appearances = _default.appearances;
exports.aspectRatio = _default.aspectRatio;
exports.backgroundBlendModes = _default.backgroundBlendModes;
exports.backgroundStyles = _default.backgroundStyles;
exports.bgColors = _default.bgColors;
exports.borderStyles = _default.borderStyles;
exports.borders = _default.borders;
exports.boxSizing = _default.boxSizing;
exports.breaks = _default.breaks;
exports.colorScheme = _default.colorScheme;
exports.containerParent = _default.containerParent;
exports.contains = _default.contains;
exports.contentVisibility = _default.contentVisibility;
exports.contents = _default.contents;
exports.cssProperty = _default.cssProperty;
exports.cssVariables = _default.cssVariables;
exports.cursors = _default.cursors;
exports.displays = _default.displays;
exports.divides = _default.divides;
exports.dynamicViewportHeight = _default.dynamicViewportHeight;
exports.flex = _default.flex;
exports.flexGridJustifiesAlignments = _default.flexGridJustifiesAlignments;
exports.floats = _default.floats;
exports.fontSmoothings = _default.fontSmoothings;
exports.fontStyles = _default.fontStyles;
exports.fonts = _default.fonts;
exports.gaps = _default.gaps;
exports.grids = _default.grids;
exports.handlerBorderStyle = _default.handlerBorderStyle;
exports.hyphens = _default.hyphens;
exports.insets = _default.insets;
exports.isolations = _default.isolations;
exports.justifies = _default.justifies;
exports.lineClamps = _default.lineClamps;
exports.margins = _default.margins;
exports.mixBlendModes = _default.mixBlendModes;
exports.objectPositions = _default.objectPositions;
exports.opacity = _default.opacity;
exports.orders = _default.orders;
exports.outline = _default.outline;
exports.overflows = _default.overflows;
exports.paddings = _default.paddings;
exports.placements = _default.placements;
exports.pointerEvents = _default.pointerEvents;
exports.positions = _default.positions;
exports.resizes = _default.resizes;
exports.rules = _default.rules;
exports.safeArea = _default.safeArea;
exports.screenReadersAccess = _default.screenReadersAccess;
exports.sizes = _default.sizes;
exports.spaces = _default.spaces;
exports.svgUtilities = _default.svgUtilities;
exports.tabSizes = _default.tabSizes;
exports.textAligns = _default.textAligns;
exports.textDecorations = _default.textDecorations;
exports.textIndents = _default.textIndents;
exports.textOverflows = _default.textOverflows;
exports.textShadows = _default.textShadows;
exports.textStrokes = _default.textStrokes;
exports.textTransforms = _default.textTransforms;
exports.textWraps = _default.textWraps;
exports.transitions = _default.transitions;
exports.userSelects = _default.userSelects;
exports.verticalAligns = _default.verticalAligns;
exports.whitespaces = _default.whitespaces;
exports.willChange = _default.willChange;
exports.writingModes = _default.writingModes;
exports.writingOrientations = _default.writingOrientations;
exports.zIndexes = _default.zIndexes;
exports.backdropFilterBase = transform.backdropFilterBase;
exports.boxShadows = transform.boxShadows;
exports.boxShadowsBase = transform.boxShadowsBase;
exports.filterBase = transform.filterBase;
exports.filters = transform.filters;
exports.ringBase = transform.ringBase;
exports.rings = transform.rings;
exports.transformBase = transform.transformBase;
exports.transforms = transform.transforms;
exports.questionMark = questionMark;
