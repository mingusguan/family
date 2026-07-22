export { defaultAttributes, defaultIgnoreNonValuedAttributes } from 'unplugin-attributify-to-class/utils';
export { defaultRules } from 'unplugin-transform-class/utils';
import * as _unocss_core from '@unocss/core';
import { SourceCodeTransformer } from '@unocss/core';
import { Options as Options$1 } from 'unplugin-attributify-to-class/types';
import { T as Theme } from './shared/unocss-preset-weapp.d9544800.mjs';
import { FilterPattern } from '@rollup/pluginutils';

declare function export_default(options?: Options$1): SourceCodeTransformer;

declare function extractorAttributify(options?: Options$1): {
    transformerAttributify: () => _unocss_core.SourceCodeTransformer;
    presetWeappAttributify: () => _unocss_core.Preset<Theme>;
};
declare const transformerAttributify: typeof export_default;

interface Options {
    /**
     * 自定义转换规则
     * @default https://github.com/MellowCo/unplugin-transform-class#options
     */
    transformRules?: Record<string, string>;
    /**
     * 排除转换目标
     * @default [/[\\/]node_modules[\\/]/, /[\\/]\.git[\\/]/]
     */
    exclude?: FilterPattern;
    /**
     * 需要转换的目标
     * @default [/\.[jt]sx?$/, /\.vue$/,  /\.vue\?vue/]
     */
    include?: FilterPattern;
    /**
     * 是否生成 class 标签
     * 会在模板中生成 <!-- class --> 标签，用于 unocss vscode 插件识别
     * https://github.com/MellowCo/unocss-preset-weapp/issues/53
     * @default true
     */
    classTags?: boolean;
}
declare function transformerClass(options?: Options): SourceCodeTransformer;

export { extractorAttributify, transformerAttributify, transformerClass };
