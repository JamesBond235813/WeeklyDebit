import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    meta: {
      icon: 'lucide:keyboard',
      order: -2,
      title: '工作台',
      authority: ['ROLE_SUPPER', 'ROLE_DEPT_DATA_ADMIN', 'ROLE_SALES'],
    },
    name: 'BizWork',
    path: '/biz-work',
    children: [
      {
        name: 'Customer',
        path: '/biz-work/customer',
        component: () => import('#/views/bizwork/customer/index.vue'),
        meta: {
          icon: 'carbon:customer-service',
          keepAlive: false,
          title: '客户信息',
          authority: ['ROLE_SUPPER', 'ROLE_DEPT_DATA_ADMIN'],
        },
      },
      {
        name: 'PublicPoolCustomer',
        path: '/biz-work/public-pool',
        component: () => import('#/views/bizwork/public-pool/index.vue'),
        meta: {
          icon: 'carbon:customer-service',
          keepAlive: false,
          title: '公海客户',
        },
      },
      {
        name: 'MyCustomer',
        path: '/biz-work/my-customer',
        component: () => import('#/views/bizwork/my-customer/index.vue'),
        meta: {
          icon: 'carbon:customer-service',
          keepAlive: false,
          title: '我的客户',
          // query: { favoriteOnly: true, title: '我的客户' },
        },
      },
      {
        name: 'AssignCustomer',
        path: '/biz-work/assign-customer',
        component: () => import('#/views/bizwork/assign-customer/index.vue'),
        meta: {
          icon: 'carbon:customer-service',
          title: '再分配客户',
          hideInMenu: true,
          // query: { favoriteOnly: true, title: '我的客户' },
        },
      },
      {
        name: 'KeyCustomer',
        path: '/biz-work/key-customer',
        component: () => import('#/views/bizwork/key-customer/index.vue'),
        meta: {
          icon: 'carbon:customer-service',
          keepAlive: false,
          title: '重点客户',
          hideInMenu: true,
        },
      },
      {
        name: 'LoanRecord',
        path: '/biz-work/loan-record',
        component: () => import('#/views/biz/loan/index.vue'),
        meta: {
          icon: 'lucide:receipt',
          title: '放款记录',
        },
      },
      {
        name: 'BizOrder',
        path: '/biz-work/order',
        component: () => import('#/views/biz/order/index.vue'),
        meta: {
          icon: 'lucide:shopping-cart',
          title: '业务订单',
          hideInMenu: true,
        },
      },
    ],
  },
];

export default routes;
