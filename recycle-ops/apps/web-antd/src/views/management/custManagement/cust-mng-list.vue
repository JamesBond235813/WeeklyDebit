<script lang="ts" setup>
import type { VbenFormProps } from '#/adapter/form';
import type { VxeGridProps } from '#/adapter/vxe-table';
import type { PagedInfo } from '#/api/biz/biz-common';
import type { BizDictItem, CustomerItem } from '#/api/biz/customer';
import type { DeptInfo } from '#/api/biz/dept';

import { onMounted, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { Button, message } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { BizCommonApi } from '#/api/biz/biz-common';
import { customerApi } from '#/api/biz/customer';
import { deptApi } from '#/api/biz/dept';

import CustMngDispatchModal from './cust-mng-dispatch-modal.vue';
import ExtraEditCustInfoModal from './edit-cust-info-modal.vue';

const DATA_TIME_FMT = 'YYYY-MM-DD HH:mm:ss';
const deptNameOptions = ref<{ label: string; value: number }[]>([]);
const dataChannelOptions = ref<{ label: string; value: number }[]>([]);
const bizDictRepoPromise = customerApi.getBizDictItems();
// channelId - channelDesc map
const channelMap = new Map<number, string>();
onMounted(() => {
  initOptions();
});
const [EditCustInfoModal, editCustInfoModalApi] = useVbenModal({
  // 连接抽离的组件
  connectedComponent: ExtraEditCustInfoModal,
});
const [AssignModal, assignModalApi] = useVbenModal({
  // 连接抽离的组件
  connectedComponent: CustMngDispatchModal,
});
// 分配客户的 modal
function initOptions() {
  bizDictRepoPromise.then((res) => {
    if (!res.DATA_CHANNEL) {
      return;
    }
    dataChannelOptions.value = res.DATA_CHANNEL.map((e: BizDictItem) => {
      return { label: e.label, value: e.intValue };
    });
    dataChannelOptions.value.unshift({ label: '无渠道', value: 0 });
    res.DATA_CHANNEL.forEach((e: BizDictItem) => {
      channelMap.set(e.intValue, e.label);
    });
  });
  const deptPromise = deptApi.listDeptInfo({
    status: 1,
    needExtendQry: false,
  });
  deptPromise.then((res) => {
    deptNameOptions.value = res.map((e: DeptInfo) => {
      return { label: e.deptName, value: e.id };
    });
  });
}
const formOptions: VbenFormProps = {
  wrapperClass: 'grid-cols-3',
  collapsed: false,
  schema: [
    {
      component: 'Input',
      fieldName: 'mobile',
      label: '手机号',
      componentProps: {
        placeholder: '精确查询',
      },
    },
    {
      component: 'Input',
      fieldName: 'namePrefix',
      label: '客户姓名',
      componentProps: {
        placeholder: '可按姓名前缀查询',
      },
    },

    {
      component: 'Select',
      fieldName: 'ownerDeptIds',
      label: '所属部门',
      componentProps: {
        placeholder: '不限部门',
        allowClear: true,
        showSearch: true,
        filterOption: (inputValue: string, option: { label: string }) => {
          return option.label.toLowerCase().includes(inputValue.toLowerCase());
        },
        options: deptNameOptions,
        mode: 'multiple',
      },
    },
    {
      component: 'RangePicker',
      fieldName: 'applyDatePicker',
      label: '申请时间',
      componentProps: {
        showTime: true,
        showNow: true,
        showToday: true,
        placeholder: ['开始时间', '结束时间'],
      },
    },
    {
      component: 'Select',
      fieldName: 'channel',
      label: '渠道',
      componentProps: {
        placeholder: '不限',
        allowClear: true,
        options: dataChannelOptions,
        showSearch: true,
        filterOption: (inputValue: string, option: { label: string }) => {
          return option.label.toLowerCase().includes(inputValue.toLowerCase());
        },
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

const gridOptions: VxeGridProps<CustomerItem> = {
  // cellClassName: 'vxe-table--pre',
  columns: [
    {
      title: '',
      field: 'idPicker',
      width: 50,
      fixed: 'left',
      type: 'checkbox',
    },
    { title: '姓名', field: 'name', width: 150, fixed: 'left' },
    { title: '手机号', field: 'mobile', width: 150 },
    { title: '性别', field: 'sexDesc', width: 60 },
    { title: '年龄', field: 'age', width: 60 },

    {
      title: '数据归属人',
      field: 'ownerUserName',
      width: 100,
    },
    {
      title: '数据归属部门',
      field: 'ownerDeptName',
      minWidth: 200,
    },
    { title: '申请时间', field: 'applyDate', width: 200 },
    { title: '来源文件', field: 'sourceFileName', minWidth: 180 },
    { title: '推广渠道', field: 'channelDesc', minWidth: 180 },
    { title: '新增记录时间', field: 'gmtCreate', width: 200 },

    {
      field: 'action',
      title: '操作',
      width: 100,
      slots: { default: 'action' },
      fixed: 'right',
    },
  ],
  showOverflow: true,
  pagerConfig: {
    enabled: true,
    pageSize: 30,
  },
  border: true,
  proxyConfig: {
    ajax: {
      query: async (component: { page: any }, formValues: any) => {
        return await pagedListCustomerInfo({
          page: component.page.currentPage,
          pageSize: component.page.pageSize,
          ...formValues,
        });
      },
    },
  },
  rowConfig: {
    isHover: true,
  },
};

async function pagedListCustomerInfo(formValues?: any) {
  return BizCommonApi.makeupPromise4VxeTable(
    customerApi.pagedListCustomerInfo({
      ...formValues,
      applyDateStart: formValues.applyDatePicker?.[0].format(DATA_TIME_FMT),
      applyDateEnd: formValues.applyDatePicker?.[1].format(DATA_TIME_FMT),
    }),
    (data: PagedInfo<CustomerItem>) => {
      data.list?.forEach((item: CustomerItem) => {
        item.channelDesc = channelMap.get(item.channel);
      });
      return { items: data.list, total: data.total };
    },
  );
}

function renderEmptySlot() {
  // do nothing
}
const [Grid, gridApi] = useVbenVxeGrid({ formOptions, gridOptions });
function openEditCustInfoModal(cid?: number) {
  const modalData = {
    callbackFunc: gridApi.query,
    deptNameOptions,
    dataChannelOptions,
    callbackFnc: async () => {
      gridApi.query();
    },
    cid,
  };
  editCustInfoModalApi.setData(modalData).open();
}
function batchAssign() {
  const custIds: number[] = [];
  const custDescList: string[] = [];
  gridApi.grid.getCheckboxRecords().forEach((item: any) => {
    custIds.push(item.id);
    custDescList.push(`${item.name} - ${item.mobile}`);
  });
  openAssignModal(custIds, custDescList);
}
function openAssignModal(cids: number[], custDescList: string[]) {
  if (!cids || cids.length === 0) {
    message.error('请选择要分配的客户');
    return;
  }
  const modalData = {
    cids,
    custDescList,
    deptNameOptions: deptNameOptions.value,
    callbackFunc: () => {
      gridApi.query();
    },
  };

  assignModalApi.setData(modalData).open();
}
</script>
<template>
  <EditCustInfoModal />
  <div class="vp-raw w-full">
    <Grid>
      <template #toolbar>
        <div class="mb-2 w-full">
          <span class="mr-1">批量操作:</span>
          <Button size="small" type="link" class="mr-1" @click="batchAssign">
            分配
          </Button>

          <Button
            size="small"
            type="primary"
            class="float-right"
            @click="openEditCustInfoModal()"
          >
            新增客户信息
          </Button>
        </div>
      </template>
      <template #action="{ row }">
        <div>
          <Button
            size="small"
            type="link"
            class="mr-1"
            @click="openEditCustInfoModal(row.id)"
          >
            编辑
          </Button>
        </div>
      </template>
      <template #empty>
        {{ renderEmptySlot() }}
      </template>
    </Grid>
  </div>
  <AssignModal />
</template>
