import type { Router } from 'vue-router';

import { DEFAULT_HOME_PATH, LOGIN_PATH } from '@vben/constants';
import { preferences } from '@vben/preferences';
import { useAccessStore, useUserStore } from '@vben/stores';
import { startProgress, stopProgress } from '@vben/utils';

import { accessRoutes, coreRouteNames } from '#/router/routes';
import { useAuthStore } from '#/store';

import { generateAccess } from './access';

const OBSERVER_HOME_PATH = '/biz-work/public-pool';
const OBSERVER_ROLE = 'ROLE_OBSERVER';
const ANALYSIS_PATH = '/dashboard/analysis';
const ANALYSIS_ROLES = ['ROLE_SUPPER', 'ROLE_DEPT_DATA_ADMIN'];

function resolveHomePath(userInfo: null | undefined | { homePath?: string; roles?: string[] }) {
  if (userInfo?.roles?.includes(OBSERVER_ROLE)) {
    return OBSERVER_HOME_PATH;
  }
  return userInfo?.homePath || DEFAULT_HOME_PATH;
}

function sanitizeRedirectPath(path: string, userInfo: null | undefined | { homePath?: string; roles?: string[] }) {
  if (userInfo?.roles?.includes(OBSERVER_ROLE) && path.startsWith('/dashboard')) {
    return OBSERVER_HOME_PATH;
  }
  if (
    path === ANALYSIS_PATH &&
    !userInfo?.roles?.some((role) => ANALYSIS_ROLES.includes(role))
  ) {
    return resolveHomePath(userInfo);
  }
  return path;
}

/**
 * 通用守卫配置
 * @param router
 */
function setupCommonGuard(router: Router) {
  // 记录已经加载的页面
  const loadedPaths = new Set<string>();

  router.beforeEach(async (to) => {
    to.meta.loaded = loadedPaths.has(to.path);

    // 页面加载进度条
    if (!to.meta.loaded && preferences.transition.progress) {
      startProgress();
    }
    return true;
  });

  router.afterEach((to) => {
    // 记录页面是否加载,如果已经加载，后续的页面切换动画等效果不在重复执行

    loadedPaths.add(to.path);

    // 关闭页面加载进度条
    if (preferences.transition.progress) {
      stopProgress();
    }
  });
}

/**
 * 权限访问守卫配置
 * @param router
 */
function setupAccessGuard(router: Router) {
  router.beforeEach(async (to, from) => {
    const accessStore = useAccessStore();
    const userStore = useUserStore();
    const authStore = useAuthStore();

    // 基本路由，这些路由不需要进入权限拦截
    if (coreRouteNames.includes(to.name as string)) {
      if (to.path === LOGIN_PATH && accessStore.accessToken) {
        const redirectPath = decodeURIComponent(
          (to.query?.redirect as string) ||
            resolveHomePath(userStore.userInfo),
        );
        return sanitizeRedirectPath(redirectPath, userStore.userInfo);
      }
      return true;
    }

    // accessToken 检查
    if (!accessStore.accessToken) {
      // 明确声明忽略权限访问权限，则可以访问
      if (to.meta.ignoreAccess) {
        return true;
      }

      // 没有访问权限，跳转登录页面
      if (to.fullPath !== LOGIN_PATH) {
        return {
          path: LOGIN_PATH,
          // 如不需要，直接删除 query
          query:
            to.fullPath === DEFAULT_HOME_PATH
              ? {}
              : { redirect: encodeURIComponent(to.fullPath) },
          // 携带当前跳转的页面，登录后重新跳转该页面
          replace: true,
        };
      }
      return to;
    }

    // 是否已经生成过动态路由
    if (accessStore.isAccessChecked) {
      if (to.path === DEFAULT_HOME_PATH && userStore.userInfo?.roles?.includes(OBSERVER_ROLE)) {
        return {
          path: OBSERVER_HOME_PATH,
          replace: true,
        };
      }
      return true;
    }

    // 生成路由表
    // 当前登录用户拥有的角色标识列表
    const userInfo =
      userStore.userInfo || (await authStore.fetchUserInfo().catch(() => null));
    if (!userInfo) {
      accessStore.setAccessToken(null);
      accessStore.setRefreshToken(null);
      accessStore.setAccessCodes([]);
      accessStore.setAccessMenus([]);
      accessStore.setAccessRoutes([]);
      accessStore.setIsAccessChecked(false);
      accessStore.setLoginExpired(false);
      userStore.setUserInfo(null);

      return {
        path: LOGIN_PATH,
        query:
          to.fullPath === DEFAULT_HOME_PATH
            ? {}
            : { redirect: encodeURIComponent(to.fullPath) },
        replace: true,
      };
    }
    const userRoles = userInfo.roles ?? [];

    // 生成菜单和路由
    const { accessibleMenus, accessibleRoutes } = await generateAccess({
      roles: userRoles,
      router,
      // 则会在菜单中显示，但是访问会被重定向到403
      routes: accessRoutes,
    });

    // 保存菜单信息和路由信息
    accessStore.setAccessMenus(accessibleMenus);
    accessStore.setAccessRoutes(accessibleRoutes);
    accessStore.setIsAccessChecked(true);
    const rawRedirectPath = (from.query.redirect ??
      (to.path === DEFAULT_HOME_PATH
        ? resolveHomePath(userInfo)
        : to.fullPath)) as string;
    const redirectPath = sanitizeRedirectPath(decodeURIComponent(rawRedirectPath), userInfo);

    return {
      ...router.resolve(redirectPath),
      replace: true,
    };
  });
}

/**
 * 项目守卫配置
 * @param router
 */
function createRouterGuard(router: Router) {
  /** 通用 */
  setupCommonGuard(router);
  /** 权限访问 */
  setupAccessGuard(router);
}

export { createRouterGuard };
