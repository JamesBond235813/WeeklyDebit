const defaultAppName = 'CRM管理平台';
const defaultLogoSource = '/logo.svg';
const defaultCompanyName = '';

export const appConfig = {
  name: import.meta.env.VITE_APP_NAME?.trim() || defaultAppName,
  logoSource: import.meta.env.VITE_APP_LOGO_SOURCE?.trim() || defaultLogoSource,
  companyName: import.meta.env.VITE_APP_COMPANY_NAME?.trim() || defaultCompanyName,
};
