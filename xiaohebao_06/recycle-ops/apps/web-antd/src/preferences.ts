import { defineOverridesPreferences } from '@vben/preferences';

import { appConfig } from './config/app';

/**
 * @description 项目配置文件
 * 只需要覆盖项目中的一部分配置，不需要的配置不用覆盖，会自动使用默认配置
 * !!! 更改配置后请清空缓存，否则可能不生效
 */
export const overridesPreferences = defineOverridesPreferences({
  // overrides
  app: {
    name: appConfig.name,
    loginExpiredMode: 'modal',
    watermark: false,
    enableRefreshToken: false,
  },
  logo: {
    enable: true,
    source: appConfig.logoSource,
  },
  widget: {
    notification: false,
    languageToggle: false,
    globalSearch: false,
  },
  navigation: {
    accordion: false,
  },
  tabbar: {
    // 业务后台页面以频繁切换和录入为主，关闭页面缓存可避免标签切换时复用半卸载组件。
    keepAlive: false,
    persist: false,
  },
  transition: {
    enable: false,
    loading: false,
    progress: false,
  },
  sidebar: {
    collapsed: false,
    expandOnHover: false,
    extraCollapse: false,
  },
  theme: {
    mode: 'light',
  },
  copyright: {
    companyName: appConfig.companyName,
    companySiteLink: '',
    date: ` ${new Date().getFullYear()}`,
    enable: false,
    icp: '',
    icpLink: '',
    settingShow: true,
  },
});
