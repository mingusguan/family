import { getAccessToken, clearTokens } from "./auth";
import { ApiCode } from "@/enums/api-code-enum";

// 401 跳转防抖锁，避免并发请求多次跳转登录页
let isRedirecting401 = false;

/**
 * 请求错误类
 */
export class RequestError extends Error {
  /** HTTP 状态码 */
  statusCode: number;
  /** 业务错误码 */
  code: string;

  constructor(message: string, statusCode: number, code?: string) {
    super(message);
    this.name = "RequestError";
    this.statusCode = statusCode;
    this.code = code ?? String(statusCode);
  }
}

// 请求配置
interface RequestOptions<T = any> {
  url: string;
  method: "GET" | "POST" | "PUT" | "DELETE";
  data?: T;
  header?: Record<string, string>;
  timeout?: number;
  responseType?: "text" | "arraybuffer";
  /** 是否携带访问令牌并在令牌失效时跳转登录页，公开接口设为 false */
  auth?: boolean;
}

/**
 * 请求函数
 * - 有 token 则自动添加 Authorization 头
 * - 无 token 则直接发送请求，由后端判断是否需要认证
 * - 401 时清除 token 并跳转登录页
 */
function request<T = any>(options: RequestOptions): Promise<T> {
  return new Promise<T>((resolve, reject) => {
    // 构建请求头
    const header = Object.assign({}, options.header || {});

    // 登录、注册等公开接口不能携带本地残留的过期令牌
    const requiresAuth = options.auth !== false;
    const token = getAccessToken();
    if (requiresAuth && token) {
      header["Authorization"] = `Bearer ${token}`;
    }

    // 根据平台决定URL前缀
    let requestUrl = options.url;
    // #ifdef H5
    // H5 开发环境通过 Vite 代理访问后端
    requestUrl = `${import.meta.env.VITE_APP_BASE_API}${options.url}`;
    // #endif

    // #ifndef H5
    // App 和小程序没有 Vite 代理，必须使用可从设备访问的完整后端地址
    const appApiUrl =
      import.meta.env.MODE !== "production"
        ? import.meta.env.VITE_APP_DEV_API_URL
        : import.meta.env.VITE_APP_API_URL;
    requestUrl = `${appApiUrl}${options.url}`;
    // #endif

    // 统一处理请求
    uni.request({
      url: requestUrl,
      method: options.method,
      data: options.data,
      header,
      timeout: options.timeout || 30000,
      responseType: options.responseType,
      success: (res: any) => {
        const serverCode = res?.data?.code;
        const serverMsg = res?.data?.msg || res?.data?.message;

        // 令牌无效/过期（业务码 A023x 或 HTTP 401）：清除 token 并跳转登录页（防抖）
        const isTokenError =
          (res.statusCode >= 200 && res.statusCode < 300 && serverCode?.startsWith("A023")) ||
          res.statusCode === 401;
        if (isTokenError) {
          if (requiresAuth) clearTokens();
          if (requiresAuth && !isRedirecting401) {
            isRedirecting401 = true;
            uni.navigateTo({
              url: "/pages/login/index",
              complete: () => {
                isRedirecting401 = false;
              },
            });
          }
          reject(new RequestError(serverMsg || "未授权，请重新登录", 401, serverCode || "A0230"));
          return;
        }

        // HTTP 成功：校验业务码
        if (res.statusCode >= 200 && res.statusCode < 300) {
          if (!serverCode || serverCode === ApiCode.SUCCESS) {
            resolve(res.data.data);
          } else {
            reject(new RequestError(serverMsg || "请求失败", res.statusCode, serverCode));
          }
          return;
        }

        // 其他 HTTP 错误
        reject(
          new RequestError(serverMsg || `请求失败: ${res.statusCode}`, res.statusCode, serverCode)
        );
      },
      fail: (err) => {
        reject(new RequestError(err.errMsg || "网络请求失败", 0));
      },
    });
  });
}

export default request;
