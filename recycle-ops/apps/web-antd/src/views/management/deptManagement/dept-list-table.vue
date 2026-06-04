<script lang="ts" setup>
import type { VbenFormProps } from '#/adapter/form';
import type { VxeGridProps } from '#/adapter/vxe-table';
import type { DeptInfo } from '#/api/biz/dept';

import { useVbenModal } from '@vben/common-ui';

import { Button } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { BizCommonApi } from '#/api/biz/biz-common';
import { deptApi } from '#/api/biz/dept';

import ExtraModal from './dept-form.vue';

const [Modal, modalApi] = useVbenModal({
  // 连接抽离的组件
  connectedComponent: ExtraModal,
});
const formOptions: VbenFormProps = {
  collapsed: false,
  schema: [
    {
      component: 'Input',
      fieldName: 'deptNamePrefix',
      label: '部门名称',
      componentProps: {
        placeholder: '根据部门名称前缀查询',
      },
    },
    {
      component: 'Input',
      fieldName: 'parentDeptNamePrefix',
      label: '上级部门名称',
      componentProps: {
        placeholder: '根据上级部门名称前缀查询，仅支持一层',
      },
    },
    // {
    //   component: 'Select',
    //   componentProps: {
    //     allowClear: true,
    //     options: [
    //       { label: '正常', value: 1 },
    //       { label: '禁用', value: 0 },
    //     ],
    //     placeholder: '请选择状态',
    //   },
    //   fieldName: 'status',
    //   label: '状态',
    // },
  ],
  showCollapseButton: false,
  submitButtonOptions: {
    content: '查询',
  },
  submitOnChange: false,
  submitOnEnter: true,
};

const gridOptions: VxeGridProps<DeptInfo> = {
  cellStyle: {
    padding: '3px 1px',
  },
  showOverflow: false,
  columns: [
    { field: 'seq', title: '#', type: 'seq', width: 50 },
    {
      field: 'deptName',
      title: '部门名称',
      minWidth: 150,
      showOverflow: false,
    },
    { field: 'deptCode', title: '部门编号', minWidth: 150 },
    {
      field: 'parentDeptName',
      title: '上级部门',
      minWidth: 150,
      showOverflow: false,
    },
    {
      field: 'introduction',
      title: '部门简介',
      showFooterOverflow: 'tooltip',
      minWidth: '180',
      maxWidth: '300',
    },
    // { field: 'statusDesc', title: '状态' },
    {
      field: 'createByUserName',
      title: '创建人',
      minWidth: '150',
      maxWidth: '180',
    },
    { field: 'gmtCreateStr', title: '创建记录时间', width: '180' },
    {
      field: 'lastModifiedByUserName',
      title: '最后修改人',
      minWidth: '150',
      maxWidth: '180',
    },
    { field: 'gmtModifiedStr', title: '最后修改时间', width: '180' },
    {
      field: 'action',
      title: '操作',
      width: 130,
      slots: { default: 'action' },
      fixed: 'right',
    },
  ],

  pagerConfig: {
    enabled: false,
  },
  // keepSource: true,
  border: true,
  // sortConfig: { multiple: true },
  proxyConfig: {
    ajax: {
      query: async (formValues: any) => {
        return await getDeptList({ ...formValues, needExtendQry: true });
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

async function getDeptList(formValues?: any) {
  return BizCommonApi.makeupPromise4VxeTable(
    deptApi.listDeptInfo({ ...formValues }),
  );
}
const [Grid, gridApi] = useVbenVxeGrid({ formOptions, gridOptions });

function openModal(row: any, del = false) {
  const modalData = {
    title: '新增部门信息',
    confirmText: '保存',
    callbackFunc: gridApi.query,
    deptInfo: null,
    del,
  };

  if (row) {
    modalData.title = '编辑部门信息';
    modalData.confirmText = '更新';
    modalData.deptInfo = row;
    if (del) {
      modalData.title = '删除部门信息';
      modalData.confirmText = '删除';
    }
  }
  modalApi.setData(modalData).open();
}
</script>

<template>
  <div class="vp-raw w-full">
    <Modal />
    <Grid class="dept-grid">
      <template #toolbar-tools>
        <Button type="primary" class="mr-2" @click="openModal(null)">
          新增部门信息
        </Button>
      </template>
      <template #action="{ row }">
        <div>
          <Button size="small" type="link" class="mr-2" @click="openModal(row)">
            编辑
          </Button>
          <Button
            danger
            size="small"
            type="link"
            class="mr-2"
            @click="openModal(row, true)"
          >
            删除
          </Button>
        </div>
      </template>
      <template #empty>
        <!-- -->
      </template>
    </Grid>
  </div>
</template>

<style scoped>
:global(.dark) .dept-grid :deep(.vxe-grid--form-wrapper),
:global([data-theme='dark']) .dept-grid :deep(.vxe-grid--form-wrapper) {
  background: #0f172a;
  border: 1px solid #1f2a44;
  border-radius: 8px;
  color: #c7d2fe;
}

:global(.dark) .dept-grid :deep(.vxe-grid--form-wrapper label),
:global([data-theme='dark']) .dept-grid :deep(.vxe-grid--form-wrapper label),
:global(.dark)
  .dept-grid
  :deep(.vxe-grid--form-wrapper .ant-form-item-label > label),
:global([data-theme='dark'])
  .dept-grid
  :deep(.vxe-grid--form-wrapper .ant-form-item-label > label) {
  color: #c7d2fe;
}
</style>
