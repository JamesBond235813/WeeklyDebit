import type { RouteRecordRaw } from 'vue-router';

const dashboard: RouteRecordRaw[] = [
  {
    path: '/dashboard',
    name: 'Dashboard',
    redirect: '/dashboard/risk',
    meta: {
      icon: 'lucide:shield-check',
      title: '风控查询',
      order: -3,
      authority: [
        'ROLE_SUPPER',
        'ROLE_DEPT_INFO_ADMIN',
        'ROLE_DEPT_DATA_ADMIN',
        'ROLE_USER_INFO_ADMIN',
        'ROLE_SALES',
      ],
    },
    children: [
      {
        path: '/dashboard/analysis',
        name: 'Analysis',
        component: () => import('#/views/dashboard/analysis/index.vue'),
        meta: {
          title: '数据看板',
          icon: 'lucide:chart-line',
          authority: ['ROLE_SUPPER', 'ROLE_DEPT_DATA_ADMIN'],
        },
      },
      {
        path: '/dashboard/risk',
        name: 'RiskReport',
        component: () => import('#/views/dashboard/risk/index.vue'),
        meta: {
          title: '风控报告',
          icon: 'lucide:shield-check',
          authority: [
            'ROLE_SUPPER',
            'ROLE_DEPT_INFO_ADMIN',
            'ROLE_DEPT_DATA_ADMIN',
            'ROLE_USER_INFO_ADMIN',
            'ROLE_SALES',
          ],
        },
      },
      {
        path: '/dashboard/customer-flow',
        name: 'CustomerFlowQuery',
        component: () => import('#/views/dashboard/customer-flow/index.vue'),
        meta: {
          title: '客户流转查询',
          icon: 'lucide:git-branch',
          authority: [
            'ROLE_SUPPER',
            'ROLE_DEPT_INFO_ADMIN',
            'ROLE_DEPT_DATA_ADMIN',
            'ROLE_USER_INFO_ADMIN',
            'ROLE_SALES',
          ],
        },
      },
    ],
  },
];

export default dashboard;
