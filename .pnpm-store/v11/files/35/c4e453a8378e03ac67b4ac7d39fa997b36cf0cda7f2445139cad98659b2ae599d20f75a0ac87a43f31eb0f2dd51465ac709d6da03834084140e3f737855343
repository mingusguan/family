import { Plugin } from 'vite';

interface Options {
    /**
     * default layout
     * @default "default"
     */
    layout: string;
    /**
     * layout dir
     * @default "src/layouts"
     */
    layoutDir: string;
    /**
     * @default process.cwd()
     */
    cwd: string;
}
interface UserOptions extends Partial<Options> {
}

declare function VitePluginUniLayouts(userOptions?: UserOptions): Plugin;

export { VitePluginUniLayouts, VitePluginUniLayouts as default };
