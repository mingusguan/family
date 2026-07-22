import { getAccessToken } from "@/utils/auth";
import { ApiCode } from "@/enums/api-code-enum";

// H5 使用 VITE_APP_BASE_API 作为代理路径，其他平台使用 VITE_APP_API_URL 作为请求路径
let baseApi =
  import.meta.env.MODE !== "production"
    ? import.meta.env.VITE_APP_DEV_API_URL
    : import.meta.env.VITE_APP_API_URL;
// #ifdef H5
baseApi = import.meta.env.VITE_APP_BASE_API;
// #endif

const FileAPI = {
  /**
   * 文件上传地址
   */
  uploadUrl: baseApi + "/api/v1/files",

  /**
   * 上传文件
   *
   * @param filePath
   */
  upload(filePath: string, rawFile?: File): Promise<FileInfo> {
    // #ifdef H5
    if (rawFile) {
      return this.uploadBrowserFile(rawFile);
    }
    // #endif

    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: this.uploadUrl,
        filePath: filePath,
        name: "file",
        header: {
          Authorization: getAccessToken() ? `Bearer ${getAccessToken()}` : "",
        },
        formData: {},
        success: (response) => {
          const resData = JSON.parse(response.data) as ResponseData<FileInfo>;
          // 业务状态码 00000 表示成功
          if (resData.code === ApiCode.SUCCESS) {
            resolve(resData.data);
          } else {
            // 其他业务处理失败
            uni.showToast({
              title: resData.msg || "文件上传失败",
              icon: "none",
            });
            reject({
              message: resData.msg || "业务处理失败",
              code: resData.code,
            });
          }
        },
        fail: (error) => {
          console.log("fail error", error);
          uni.showToast({
            title: "文件上传请求失败",
            icon: "none",
            duration: 2000,
          });
          reject({
            message: "文件上传请求失败",
            error,
          });
        },
      });
    });
  },
  // #ifdef H5
  /** 使用浏览器原生 File 对象上传，兼容 H5 自定义照片/视频选择器。 */
  async uploadBrowserFile(rawFile: File): Promise<FileInfo> {
    const formData = new FormData();
    formData.append("file", rawFile, rawFile.name);
    try {
      const response = await fetch(this.uploadUrl, {
        method: "POST",
        headers: {
          Authorization: getAccessToken() ? `Bearer ${getAccessToken()}` : "",
        },
        body: formData,
      });
      const resData = (await response.json()) as ResponseData<FileInfo>;
      if (resData.code === ApiCode.SUCCESS) {
        return resData.data;
      }
      throw { message: resData.msg || "文件上传失败", code: resData.code };
    } catch (error: any) {
      uni.showToast({
        title: error?.message || "文件上传请求失败",
        icon: "none",
      });
      throw error;
    }
  },
  // #endif
};

export default FileAPI;

/**
 * 文件API类型声明
 */
export interface FileInfo {
  /** 文件名 */
  name: string;
  /** 文件稳定存储地址 */
  url: string;
  /** 文件临时可访问地址 */
  previewUrl?: string;
}
