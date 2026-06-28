<script lang="ts" setup>
import type { VbenFormProps } from '#/adapter/form';
import type { VxeGridProps } from '#/adapter/vxe-table';
import type { PagedInfo } from '#/api/biz/biz-common';
import type { DeptInfo } from '#/api/biz/dept';
import type { UserInfoRowItem } from '#/api/biz/user';

import { onBeforeUnmount, onMounted } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { Button } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { BizCommonApi } from '#/api/biz/biz-common';
import { deptApi } from '#/api/biz/dept';
import { userApi } from '#/api/biz/user';

import ResetPasswordModal from './reset-password.vue';
import ExtraModal from './user-form.vue';

const [Modal, modalApi] = useVbenModal({
  // 连接抽离的组件
  connectedComponent: ExtraModal,
});
const [ResetPswdModal, resetPswdModalApi] = useVbenModal({
  // 连接抽离的组件
  connectedComponent: ResetPasswordModal,
});
let onlineRefreshTimer: number | undefined;

const formOptions: VbenFormProps = {
  collapsed: false,
  schema: [
    {
      component: 'Input',
      fieldName: 'userRealNamePrefix',
      label: '用户真实姓名',
      componentProps: {
        placeholder: '根据用户真实姓名前缀查询',
      },
    },
    {
      component: 'Input',
      fieldName: 'userName',
      label: '用户名',
      componentProps: {
        placeholder: '用户登录账号',
      },
    },
    {
      component: 'Input',
      fieldName: 'phone',
      label: '用户手机号',
      componentProps: {
        placeholder: '用户手机号',
      },
    },
    {
      component: 'Select',
      fieldName: 'departmentId',
      label: '所属部门',
      componentProps: {
        placeholder: '不限部门',
        allowClear: true,
        showSearch: true,
        filterOption: (inputValue: string, option: { label: string }) => {
          return option.label.toLowerCase().includes(inputValue.toLowerCase());
        },
        options: [],
      },
    },
  ],
  showCollapseButton: false,
  submitButtonOptions: {
    content: '查询',
  },
  submitOnChange: false,
  submitOnEnter: true,
};

const gridOptions: VxeGridProps<UserInfoRowItem> = {
  cellStyle: {
    padding: '3px 1px',
  },
  columns: [
    { field: 'seq', title: '#', type: 'seq', width: 50 },
    { field: 'userName', title: '用户名', width: 100, slots: { default: 'userName' } },
    { field: 'realName', title: '姓名', width: 120 },
    { field: 'phone', title: '手机号', width: 130 },
    { field: 'departmentName', title: '部门', width: 150 },
    { field: 'employeeNo', title: '工号', width: 100 },
    { field: 'jobName', title: '职位', width: 120 },
    {
      field: 'roleDispNames',
      title: '权限角色',
      width: 200,
      showOverflow: false,
    },
    { field: 'email', title: '邮箱', width: 180 },
    { field: 'sexDispName', title: '性别', width: 80 },
    { field: 'birthday', title: '生日', width: 120 },
    { field: 'gmtCreate', title: '创建时间', width: 180 },
    { field: 'gmtModified', title: '更新时间', width: 180 },
    { field: 'createByName', title: '创建人', width: 180 },
    {
      field: 'action',
      title: '操作',
      width: 200,
      slots: { default: 'action' },
      fixed: 'right',
    },
  ],
  showOverflow: false,
  pagerConfig: {
    enabled: true,
    pageSize: 30,
  },
  // keepSource: true,
  border: true,
  // sortConfig: { multiple: true },
  proxyConfig: {
    ajax: {
      query: async (component: { page: any }, formValues: any) => {
        return await pagedListUserInfo({
          ...formValues,
          needExtendQry: true,
          page: component.page?.currentPage,
          pageSize: component.page?.pageSize,
        });
      },
    },
  },
  toolbarConfig: {
    // 是否显示搜索表单控制按钮
    // @ts-ignore 正式环境时有完整的类型声明
    // search: true,
    custom: false,
  },
  rowConfig: {
    isHover: true,
    keyField: 'row.id',
  },
};
function initDeptInfo() {
  deptApi
    .listDeptInfo({ status: 1, needExtendQry: false })
    .then((data: any) => {
      const optionsPairs = data.map((item: DeptInfo) => {
        return { label: item.deptName, value: item.id };
      });
      gridApi.formApi.updateSchema([
        {
          fieldName: 'departmentId',
          componentProps: { options: optionsPairs },
        },
      ]);
    });
}

async function pagedListUserInfo(formValues?: any) {
  return BizCommonApi.makeupPromise4VxeTable(
    userApi.pagedListUserInfo({ ...formValues }),
    (data: PagedInfo<UserInfoRowItem>) => {
      data.list.forEach((item: UserInfoRowItem) => {
        switch (item.sex) {
          case 0: {
            item.sexDispName = '保密';
            break;
          }
          case 1: {
            item.sexDispName = '男';
            break;
          }
          case 2: {
            item.sexDispName = '女';
            break;
          }
          default: {
            item.sexDispName = '保密';
            break;
          }
        }
      });
      return { items: data.list, total: data.total };
    },
  );
}

const [Grid, gridApi] = useVbenVxeGrid({ formOptions, gridOptions });
function openModal(row: any, del = false) {
  const modalData = {
    title: '新增用户信息',
    confirmText: '保存',
    callbackFunc: gridApi.query,
    data: null,
    del,
  };

  if (row) {
    modalData.title = '编辑用户信息';
    modalData.confirmText = '更新';
    modalData.data = row;
    if (del) {
      modalData.title = '确认要删除用户信息吗？';
      modalData.confirmText = '确认删除';
    }
  }
  modalApi.setData(modalData).open();
}

function openResetPswdModal(row: any) {
  const modalData = {
    callbackFunc: gridApi.query,
    data: row,
  };

  resetPswdModalApi.setData(modalData).open();
}
const renderEmptySlot = () => {
  // ...
};
onMounted(() => {
  initDeptInfo();
  onlineRefreshTimer = window.setInterval(() => {
    gridApi.query();
  }, 10_000);
});

onBeforeUnmount(() => {
  if (onlineRefreshTimer) {
    window.clearInterval(onlineRefreshTimer);
    onlineRefreshTimer = undefined;
  }
});
</script>
<template>
  <div class="vp-raw w-full">
    <Modal />
    <Grid>
      <template #toolbar-tools>
        <Button type="primary" class="mr-2" @click="openModal(null)">
          新增用户
        </Button>
      </template>
      <template #action="{ row }">
        <div>
          <Button size="small" type="link" class="m-0" @click="openModal(row)">
            编辑
          </Button>
          <Button
            size="small"
            type="link"
            class="m-0"
            @click="openResetPswdModal(row)"
          >
            重置密码
          </Button>
          <Button
            danger
            size="small"
            type="link"
            class="m-0"
            @click="openModal(row, true)"
          >
            删除
          </Button>
        </div>
      </template>
      <template #userName="{ row }">
        <span class="user-name-cell">
          <span>{{ row.userName }}</span>
          <span
            :class="row.onlineStatus === 1 ? 'online-dot' : 'offline-dot'"
            :title="row.onlineStatus === 1 ? '在线' : '离线'"
          ></span>
        </span>
      </template>
      <template #empty>
        {{ renderEmptySlot() }}
      </template>
    </Grid>
  </div>
  <ResetPswdModal />
</template>

<style scoped>
.user-name-cell {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.online-dot,
.offline-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 999px;
  flex: 0 0 auto;
}

.online-dot {
  background: #22c55e;
  box-shadow: 0 0 0 2px rgb(34 197 94 / 12%);
}

.offline-dot {
  background: #9ca3af;
}
</style>
