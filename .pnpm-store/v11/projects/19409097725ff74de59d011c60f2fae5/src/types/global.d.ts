// 全局类型声明文件
// 自动引入 virtual 模块类型

/// <reference types="@uni-helper/vite-plugin-uni-pages/client" />
/// <reference types="@dcloudio/types" />
/// <reference types="@uni-helper/uni-types" />

// 如果需要其他 virtual 模块类型，可以在这里添加
// 例如：/// <reference types="xxx/client" />

declare global {
  /**
   * 分页查询参数
   */
  interface PageQuery {
    pageNum: number;
    pageSize: number;
  }

  /**
   * 分页响应对象
   */
  interface PageResult<T> {
    /** 数据列表 */
    list: T[];
    /** 总数 */
    total: number;
  }

  /**
   * 组件数据源
   */
  interface OptionType {
    /** 值 */
    value: string | number;
    /** 文本 */
    label: string;
    /** 子列表  */
    children?: OptionType[];
  }

  /**
   * 响应数据
   */
  interface ResponseData<T = any> {
    code: string;
    data: T;
    msg: string;
  }
}

export {};
