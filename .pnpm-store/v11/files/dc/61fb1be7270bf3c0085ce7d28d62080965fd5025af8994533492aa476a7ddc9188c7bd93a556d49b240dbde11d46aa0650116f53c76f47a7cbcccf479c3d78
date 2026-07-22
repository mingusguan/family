export { z as alignments, a as animations, c as appearance, N as appearances, I as aspectRatio, a6 as backgroundBlendModes, b as backgroundStyles, g as bgColors, d as borderStyles, e as borders, F as boxSizing, W as breaks, i as colorScheme, j as containerParent, P as contains, U as contentVisibility, V as contents, ah as cssProperty, ag as cssVariables, O as cursors, M as displays, l as divides, a8 as dynamicViewportHeight, m as flex, B as flexGridJustifiesAlignments, D as floats, $ as fontSmoothings, _ as fontStyles, ab as fonts, n as gaps, p as grids, h as handlerBorderStyle, a0 as hyphens, C as insets, a4 as isolations, x as justifies, s as lineClamps, K as margins, a7 as mixBlendModes, a5 as objectPositions, f as opacity, y as orders, o as outline, q as overflows, J as paddings, A as placements, Q as pointerEvents, u as positions, R as resizes, r as rules, G as safeArea, a3 as screenReadersAccess, H as sizes, L as spaces, a9 as svgUtilities, ac as tabSizes, t as textAligns, k as textDecorations, ad as textIndents, Y as textOverflows, af as textShadows, ae as textStrokes, Z as textTransforms, X as textWraps, aa as transitions, S as userSelects, v as verticalAligns, T as whitespaces, w as willChange, a1 as writingModes, a2 as writingOrientations, E as zIndexes } from './shared/unocss-preset-weapp.7b88246f.mjs';
export { b as backdropFilterBase, e as boxShadows, d as boxShadowsBase, f as filterBase, a as filters, r as ringBase, c as rings, t as transformBase, g as transforms } from './shared/unocss-preset-weapp.1d3647ce.mjs';
import './shared/unocss-preset-weapp.d4ee9f3f.mjs';
import '@unocss/core';
import '@unocss/rule-utils';
import 'unplugin-transform-class/utils';

const questionMark = [
  [
    /^(where|\?)$/,
    (_, { constructCSS, generator }) => {
      if (generator.userConfig.envMode === "dev")
        return `@keyframes __un_qm{0%{box-shadow:inset 4px 4px #ff1e90, inset -4px -4px #ff1e90}100%{box-shadow:inset 8px 8px #3399ff, inset -8px -8px #3399ff}} ${constructCSS({ animation: "__un_qm 0.5s ease-in-out alternate infinite" })}`;
    }
  ]
];

export { questionMark };
