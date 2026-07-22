import request from "@/utils/request";

const AUTH_BASE_URL = "/api/v1/auth";
const WXMA_AUTH_BASE_URL = "/api/v1/wxma/auth";
const APP_AUTH_BASE_URL = "/api/v1/auth/app";

export interface LoginData {
  username: string;
  password: string;
  captchaId?: string;
  captchaCode?: string;
}

export interface RegisterData {
  username: string;
  nickname: string;
  password: string;
}

export interface LoginResult {
  accessToken: string;
  refreshToken?: string;
  tokenType: string;
  expiresIn: number;
  isNewUser?: boolean;
}

export interface Captcha {
  captchaId: string;
  captchaBase64: string;
}

export interface SmsLoginData {
  mobile: string;
  code: string;
}

export interface WxMaLoginResp extends LoginResult {
  isNewUser: boolean;
}

const AuthAPI = {
  /**
   * 获取图形验证码
   */
  getCaptcha(): Promise<Captcha> {
    return request<Captcha>({
      url: `${AUTH_BASE_URL}/captcha`,
      method: "GET",
      auth: false,
    });
  },

  /**
   * 注册 APP 用户，注册成功后直接返回登录令牌
   */
  register(data: RegisterData): Promise<LoginResult> {
    return request<LoginResult>({
      url: `${APP_AUTH_BASE_URL}/register`,
      method: "POST",
      auth: false,
      data,
    });
  },

  /**
   * 账号密码登录
   */
  login(data: LoginData): Promise<LoginResult> {
    return request<LoginResult>({
      url: `${APP_AUTH_BASE_URL}/login`,
      method: "POST",
      auth: false,
      data: data,
    });
  },

  /**
   * 发送短信验证码
   *
   * 演示环境说明：短信服务未配置，验证码固定为 123456
   */
  sendSmsLoginCode(mobile: string): Promise<void> {
    const mobileSafe = encodeURIComponent(mobile);
    return request({
      url: `${AUTH_BASE_URL}/sms/code?mobile=${mobileSafe}`,
      method: "POST",
      auth: false,
    });
  },

  /**
   * 短信验证码登录
   */
  loginBySms(data: SmsLoginData): Promise<LoginResult> {
    const mobileSafe = encodeURIComponent(data.mobile);
    const codeSafe = encodeURIComponent(data.code);
    return request<LoginResult>({
      url: `${AUTH_BASE_URL}/login/sms?mobile=${mobileSafe}&code=${codeSafe}`,
      method: "POST",
      auth: false,
    });
  },

  /**
   * 微信小程序登录
   *
   * 首次登录自动创建用户，后续按 openid 直接登录。
   * 整个流程不获取也不要求绑定手机号。
   */
  wxMaSilentLogin(code: string): Promise<WxMaLoginResp> {
    return request<WxMaLoginResp>({
      url: `${WXMA_AUTH_BASE_URL}/silent-login?code=${encodeURIComponent(code)}`,
      method: "POST",
      auth: false,
    });
  },

  /**
   * 检查会话有效性
   */
  checkSession(): Promise<{ valid: boolean }> {
    return request<{ valid: boolean }>({
      url: `${AUTH_BASE_URL}/check-session`,
      method: "GET",
    });
  },

  /**
   * 登出
   */
  logout() {
    return request({
      url: `${AUTH_BASE_URL}/logout`,
      method: "DELETE",
    });
  },

  /**
   * 刷新令牌
   */
  refreshToken(refreshToken: string): Promise<{ accessToken: string; expiresIn: number }> {
    return request<{ accessToken: string; expiresIn: number }>({
      url: `${AUTH_BASE_URL}/refresh-token`,
      method: "POST",
      auth: false,
      data: { refreshToken },
    });
  },
};

export default AuthAPI;
