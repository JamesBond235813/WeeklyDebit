import type { RequestResponse } from '@vben/request';

import { message } from 'ant-design-vue';
/**
 * @description: 通用函数集合， 不含数据字段属性
 */
export const BizCommonApi = {
  /**
   * 在当前页面展示错误消息
   * @description: show error message
   * @param {object} error
   */
  showErrorMsg: (error: any, addMsg: string | undefined = '') => {
    if (error.code && error.msg) {
      message.error({
        content: `${error.code} - ${error.msg} ${addMsg?.trim()}`,
        duration: 10,
      });
      return;
    }
    message.error(error);
  },

  /**
   * 将 Promise 转换为适合 VxeTable 的格式, 包含 items 和 total。 一般用于从服务端拉取数据列表类 API 调用
   *
   * @description: Transforms a promise into a format suitable for VxeTable.
   *
   * @param {Promise<any>} promise - The input promise to be transformed.
   * @returns {Promise<{ items: any; total: number }>} - A promise that resolves with an object containing
   *  the items from the resolved promise and their total count, or rejects with an error containing
   *  error code and message if available.
   */
  makeupPromise4VxeTable: (
    promise: Promise<any>,
    callback: (arg0: any) => any,
  ): Promise<{ items: any; total: number }> => {
    return new Promise<{ items: any; total: number }>((resolve, reject) => {
      promise.then((res) => {
        if (callback) {
          const data = callback(res);
          resolve({ items: data.items, total: data?.total });
          return;
        }
        resolve({ items: res, total: res?.length });
      });
      promise.catch((error: any) => {
        if (error?.code && error.msg) {
          reject(new Error(`${error.code} - ${error.msg}`));
          return;
        }
        reject(error);
      });
    });
  },

  /**
   * 处理 Blob 响应并保存文件
   * @param res Blob 响应
   * @param defaultFilename 默认文件名（如果后端未返回）
   */
  handleBlobResponse: (res: RequestResponse, defaultFilename: string) => {
    // 1. 创建 Blob URL
    const blob = new Blob([res.data]);
    const url = window.URL.createObjectURL(blob);

    // 2. 创建隐藏的 <a> 标签触发下载
    const link = document.createElement('a');
    link.href = url;

    // 3. 尝试从响应头中获取文件名
    const contentDisposition = res.headers?.['content-disposition'];
    let filename = defaultFilename;

    if (contentDisposition) {
      const matches = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/.exec(
        contentDisposition,
      );
      if (matches?.[1]) {
        filename = matches[1].replaceAll(/['"]/g, '');
        if (filename.includes('%')) {
          // 尝试 urldecode 操作
          try {
            filename = decodeURIComponent(filename);
          } catch (error: any) {
            console.error(`解析文件名失败: ${filename} => ${error?.message}`);
          }
        }
      }
    }

    link.download = filename;
    document.body.append(link);
    link.click();

    // 4. 清理资源
    link.remove();
    window.URL.revokeObjectURL(url);
  },
};

/** 分页查询返回结果基类 */
export interface PagedInfo<T> {
  /** 总数据量 */
  total?: number;
  /** 当前页号 */
  pageNum?: number;

  /** 页大小 */
  pageSize?: number;

  /** 当前数据量 */
  size?: number;

  /** 总页数 */
  pages?: number;

  /** 用户数据列表 */
  list?: T[];
}
