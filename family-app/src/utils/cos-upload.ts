import type { CosPostUploadTicket } from "@/api/album";

const COS_UPLOAD_TIMEOUT_MS = 3 * 60 * 1000;

export function uploadToCos(
  ticket: CosPostUploadTicket,
  filePath: string,
  rawFile?: File,
  onProgress?: (percent: number) => void
): Promise<void> {
  // #ifdef H5
  if (rawFile) {
    return uploadBrowserFile(ticket, rawFile, onProgress);
  }
  // #endif

  return new Promise((resolve, reject) => {
    const formData: Record<string, string> = {
      key: ticket.objectKey,
      policy: ticket.policy,
      success_action_status: "200",
      "q-sign-algorithm": ticket.qSignAlgorithm,
      "q-ak": ticket.qAk,
      "q-key-time": ticket.qKeyTime,
      "q-signature": ticket.qSignature,
    };
    if (ticket.securityToken) {
      formData["x-cos-security-token"] = ticket.securityToken;
    }

    const uploadTask = uni.uploadFile({
      url: ticket.uploadUrl,
      filePath,
      name: "file",
      timeout: COS_UPLOAD_TIMEOUT_MS,
      formData,
      success: (response) => {
        if (response.statusCode >= 200 && response.statusCode < 300) {
          onProgress?.(100);
          resolve();
          return;
        }
        reject(new Error("COS上传失败（" + response.statusCode + "）"));
      },
      fail: (error) => reject(new Error(error.errMsg || "COS上传请求失败")),
    });
    uploadTask.onProgressUpdate((event) => onProgress?.(event.progress));
  });
}

// #ifdef H5
function uploadBrowserFile(
  ticket: CosPostUploadTicket,
  rawFile: File,
  onProgress?: (percent: number) => void
): Promise<void> {
  return new Promise((resolve, reject) => {
    const formData = new FormData();
    formData.append("key", ticket.objectKey);
    formData.append("policy", ticket.policy);
    formData.append("success_action_status", "200");
    formData.append("q-sign-algorithm", ticket.qSignAlgorithm);
    formData.append("q-ak", ticket.qAk);
    formData.append("q-key-time", ticket.qKeyTime);
    formData.append("q-signature", ticket.qSignature);
    if (ticket.securityToken) {
      formData.append("x-cos-security-token", ticket.securityToken);
    }
    // COS 要求文件字段位于表单最后。
    formData.append("file", rawFile, rawFile.name);

    const xhr = new XMLHttpRequest();
    xhr.open("POST", ticket.uploadUrl);
    xhr.timeout = COS_UPLOAD_TIMEOUT_MS;
    xhr.upload.onprogress = (event) => {
      if (event.lengthComputable && event.total > 0) {
        onProgress?.(Math.round((event.loaded * 100) / event.total));
      }
    };
    xhr.onload = () => {
      if (xhr.status >= 200 && xhr.status < 300) {
        onProgress?.(100);
        resolve();
      } else {
        reject(new Error("COS上传失败（" + xhr.status + "）"));
      }
    };
    xhr.onerror = () => reject(new Error("COS上传请求失败"));
    xhr.ontimeout = () => reject(new Error("COS上传超过3分钟"));
    xhr.send(formData);
  });
}
// #endif
