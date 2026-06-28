import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    meta: {
      icon: 'lucide:layout-dashboard',
      order: -1,
      title: '管理中心',
      authority: [
        'ROLE_SUPPER',
        'ROLE_DEPT_INFO_ADMIN',
        'ROLE_DEPT_DATA_ADMIN',
        'ROLE_USER_INFO_ADMIN',
        'ROLE_SALES',
      ],
    },
    name: 'Management',
    path: '/management',
    children: [
      {
        name: 'BizConfig',
        path: '/management/biz-config',
        component: () => import('#/views/management/bizConfig/index.vue'),
        meta: {
          icon: 'carbon:settings-adjust',
          title: '业务配置',
          authority: [
            'ROLE_SUPPER',
            'ROLE_DEPT_INFO_ADMIN',
            'ROLE_USER_INFO_ADMIN',
          ],
        },
      },
      {
        name: 'CustImport',
        path: '/custImport',
        component: () => import('#/views/management/custImport/index.vue'),
        meta: {
          icon: 'carbon:document-import',
          title: '导入客户信息',
          hideInMenu: true,
          authority: ['ROLE_SUPPER', 'ROLE_DEPT_INFO_ADMIN'],
        },
      },
      {
        name: 'CustDispatchPlan',
        path: '/management/dispatch',
        component: () => import('#/views/management/dispatch/index.vue'),
        meta: {
          icon: 'carbon:flow-data',
          title: '数据分配',
          authority: ['ROLE_SUPPER'],
        },
      },
      {
        name: 'CustManagement',
        path: '/custManagement',
        component: () => import('#/views/management/custManagement/index.vue'),
        meta: {
          icon: 'carbon:customer',
          title: '客户信息管理',
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
        name: 'RiskRegion',
        path: '/management/risk-region',
        component: () => import('#/views/management/riskRegion/index.vue'),
        meta: {
          icon: 'lucide:map-pinned',
          title: '风险地区提醒',
          authority: ['ROLE_SUPPER'],
        },
      },
      {
        name: 'DeptManagement',
        path: '/deptManagement',
        component: () => import('#/views/management/deptManagement/index.vue'),
        meta: {
          icon: 'carbon:workspace',
          title: '部门管理',
          authority: ['ROLE_SUPPER', 'ROLE_DEPT_INFO_ADMIN'],
        },
      },
      {
        name: 'UserManagement',
        path: '/userManagement',
        component: () => import('#/views/management/userManagement/index.vue'),
        meta: {
          icon: 'lucide:area-chart',
          title: '用户管理',
          authority: ['ROLE_SUPPER', 'ROLE_USER_INFO_ADMIN'],
        },
      },
      {
        name: 'BizPlatform',
        path: '/management/platform',
        component: () => import('#/views/biz/platform/index.vue'),
        meta: {
          icon: 'lucide:building-2',
          title: '分期/租赁平台',
          hideInMenu: true,
          authority: ['ROLE_SUPPER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_DEPT_ADMIN', 'ROLE_DEPT_DATA_ADMIN'],
        },
      },
    ],
  },
];

export default routes;
