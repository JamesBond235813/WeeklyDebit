import type { RouteRecordRaw } from 'vue-router';

import { BasicLayout } from '#/layouts';

const dashboard: RouteRecordRaw[] = [
  {
    path: '/dashboard',
    component: BasicLayout,
    name: 'Dashboard',
    redirect: '/dashboard/risk',
    meta: {
      icon: 'lucide:shield-check',
      title: '风控查询',
      order: -3,
    },
    children: [
      {
        path: '/dashboard/analysis',
        name: 'Analysis',
        redirect: '/dashboard/risk',
        meta: {
          hideInMenu: true,
          hideInTab: true,
          title: '风控报告',
        },
      },
      {
        path: '/dashboard/risk',
        name: 'RiskReport',
        component: () => import('#/views/dashboard/risk/index.vue'),
        meta: {
          title: '风控报告',
          icon: 'lucide:shield-check',
        },
      },
    ],
  },
];

export default dashboard;
