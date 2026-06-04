<script lang="ts" setup>
import type {
  TableColumnType,
  TablePaginationConfig,
  TableProps,
} from 'ant-design-vue';

import type { PagedInfo } from '#/api/biz/biz-common';
import type { BizDictItem, CustomerItem } from '#/api/biz/customer';
import type { DeptInfo } from '#/api/biz/dept';

import { onMounted, reactive, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { Button, message, Table } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
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
const tableLoading: ref<boolean> = ref(false);
const pagination = ref<TablePaginationConfig>({});
const custDescMap: Map<number, string> = new Map<number, string>();
const customerItemsRef = ref<PagedInfo<CustomerItem>>({
  list: [],
  pageSize: 30,
});
onMounted(() => {
  initOptions();
  pagedListCustomerInfo({});
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

const [MngSearchForm, mngSearchFormApi] = useVbenForm({
  wrapperClass: 'grid-cols-3',
  compact: true,
  collapsed: false,
  showCollapseButton: false,
  commonConfig: {
    // 所有表单项
    componentProps: {
      class: 'w-full border-1',
    },
  },
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
  submitButtonOptions: {
    content: '查询',
    // size: 'small',
  },
  resetButtonOptions: {
    content: '清除',
    // size: 'small',
  },
  submitOnChange: false,
  submitOnEnter: true,
  handleSubmit: pagedListCustomerInfo,
});

const columns: TableColumnType<CustomerItem>[] = [
  {
    title: '姓名',
    dataIndex: 'name',
    width: 150,
    fixed: 'left',
    align: 'center',
  },
  { title: '手机号', dataIndex: 'mobile', width: 150, align: 'center' },
  { title: '性别', dataIndex: 'sexDesc', width: 60, align: 'center' },
  { title: '年龄', dataIndex: 'age', width: 60, align: 'center' },

  {
    title: '数据归属人',
    dataIndex: 'ownerUserName',
    width: 100,
    align: 'center',
  },
  {
    title: '数据归属部门',
    dataIndex: 'ownerDeptName',
    width: 200,
    align: 'center',
  },
  { title: '申请时间', dataIndex: 'applyDate', width: 200, align: 'center' },
  {
    title: '来源文件',
    dataIndex: 'sourceFileName',
    width: 180,
    align: 'center',
  },
  { title: '推广渠道', dataIndex: 'channelDesc', width: 180, align: 'center' },
  {
    title: '新增记录时间',
    dataIndex: 'gmtCreate',
    width: 200,
    align: 'center',
  },

  {
    key: 'action',
    title: '操作',
    width: 100,
    fixed: 'right',
    align: 'center',
    customCell: () => ({ class: 'items-center' }),
  },
];

async function pagedListCustomerInfo(formValues?: any) {
  tableLoading.value = true;

  const data: PagedInfo<CustomerItem> = await customerApi.pagedListCustomerInfo(
    {
      ...formValues,
      applyDateStart: formValues.applyDatePicker?.[0].format(DATA_TIME_FMT),
      applyDateEnd: formValues.applyDatePicker?.[1].format(DATA_TIME_FMT),
      page: formValues?.page ?? 1,
      pageSize: formValues?.pageSize ?? 30,
    },
  );
  custDescMap.clear();
  data.list?.forEach((item: CustomerItem) => {
    item.key = item.id;
    custDescMap.set(item.id, `${item.name} - ${item.mobile}`);
  });
  customerItemsRef.value = data;
  pagination.value = {
    total: data?.total,
    current: data?.pageNum,
    pageSize: data.pageSize,
    showTotal: (total) => `共 ${total} 条数据`,
    pageSizeOptions: ['20', '30', '50', '100'],
    position: ['bottomLeft'],
  };
  state.selectedRowKeys = [];
  tableLoading.value = false;
}

async function getSearchConditions() {
  let formValues = await mngSearchFormApi.getValues();
  if (formValues === undefined) {
    formValues = {};
  }
  formValues.pageSize = pagination.value.pageSize;
  formValues.page = pagination.value.current;
  return formValues;
}

async function openEditCustInfoModal(cid?: number) {
  const formValues = await getSearchConditions();
  const modalData = {
    deptNameOptions,
    dataChannelOptions,
    callbackFunc: async () => {
      await pagedListCustomerInfo(formValues);
    },
    cid,
  };
  editCustInfoModalApi.setData(modalData).open();
}

function batchAssign() {
  const custIds: number[] = [];
  const custDescList: string[] = [];
  state.selectedRowKeys.forEach((cid: any) => {
    custIds.push(cid);
    custDescList.push(custDescMap.get(cid) ?? '');
  });
  openAssignModal(custIds, custDescList);
}

function openAssignModal(cids: number[], custDescList: string[]) {
  if (!cids || cids.length === 0) {
    message.error('请选择要分配的客户');
    return;
  }
  const formValues = getSearchConditions();
  const modalData = {
    cids,
    custDescList,
    deptNameOptions: deptNameOptions.value,
    callbackFunc: async () => {
      await pagedListCustomerInfo(formValues);
    },
  };

  assignModalApi.setData(modalData).open();
}

const state = reactive<{
  loading: boolean;
  selectedRowKeys: number[];
}>({
  selectedRowKeys: [], // Check here to configure the default column
  loading: false,
});
const onSelectChange = (selectedRowKeys: number[]) => {
  state.selectedRowKeys = selectedRowKeys;
};
const viewportHeight = document.documentElement.clientHeight - 360;

const handleTableChange: TableProps['onChange'] = async (pag) => {
  pagination.value = {
    ...pagination.value,
    current: pag.current,
    pageSize: pag.pageSize,
  };
  mngSearchFormApi.getValues().then((formValues) => {
    pagedListCustomerInfo({
      ...formValues,
      page: pag.current,
      pageSize: pag.pageSize,
    });
  });
};
</script>

<template>
  <EditCustInfoModal />
  <div
    class="cust-mng-search vp-raw w-full rounded-lg border border-gray-200 bg-white p-2 shadow-sm transition-colors duration-200 dark:border-[#1f2a44] dark:bg-[#0f172a] dark:text-[#c7d2fe]"
  >
    <MngSearchForm />
  </div>
  <div
    class="cust-mng-table mt-5 w-full rounded-lg border border-gray-200 bg-white shadow-sm transition-colors duration-200 dark:border-[#1f2a44] dark:bg-[#0b1220] dark:text-[#c7d2fe]"
  >
    <span class="m-2">批量操作:</span>
    <Button size="small" type="link" class="m-2" @click="batchAssign">
      分配
    </Button>
    <Button
      size="small"
      type="primary"
      class="float-right m-2"
      @click="openEditCustInfoModal()"
    >
      新增客户信息
    </Button>
    <!--  </div>-->
    <!--  <div class="vp-raw w-full">-->
    <Table
      :columns="columns"
      :loading="tableLoading"
      :data-source="customerItemsRef.list"
      :pagination="pagination"
      size="small"
      :scroll="{ x: 220, y: viewportHeight }"
      :row-selection="{
        selectedRowKeys: state.selectedRowKeys,
        onChange: onSelectChange,
      }"
      @change="handleTableChange"
      class="w-full p-2 pb-0"
      bordered
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <Button
            size="small"
            type="link"
            class="-m-2 p-1"
            @click="openEditCustInfoModal(record.id)"
          >
            编辑
          </Button>
        </template>
      </template>
    </Table>
  </div>
  <AssignModal />
</template>

<style scoped>
:global(.dark) .cust-mng-search :deep(label),
:global([data-theme='dark']) .cust-mng-search :deep(label),
:global(.dark) .cust-mng-search :deep(.ant-form-item-label > label),
:global([data-theme='dark']) .cust-mng-search :deep(.ant-form-item-label > label) {
  color: #c7d2fe;
}

:global(.dark) .cust-mng-table :deep(.ant-table),
:global([data-theme='dark']) .cust-mng-table :deep(.ant-table) {
  background: transparent;
  color: #e2e8f0;
}

:global(.dark) .cust-mng-table :deep(.ant-table-container),
:global([data-theme='dark']) .cust-mng-table :deep(.ant-table-container) {
  border-color: #1f2a44;
}

:global(.dark) .cust-mng-table :deep(.ant-table-thead > tr > th),
:global([data-theme='dark']) .cust-mng-table :deep(.ant-table-thead > tr > th) {
  background: #0f172a;
  color: #cbd5e1;
  border-color: #1f2a44;
}

:global(.dark) .cust-mng-table :deep(.ant-table-tbody > tr > td),
:global([data-theme='dark']) .cust-mng-table :deep(.ant-table-tbody > tr > td) {
  background: transparent;
  border-color: #1f2a44;
  color: #e2e8f0;
}

:global(.dark) .cust-mng-table :deep(.ant-table-tbody > tr.ant-table-row:hover > td),
:global([data-theme='dark'])
  .cust-mng-table
  :deep(.ant-table-tbody > tr.ant-table-row:hover > td) {
  background: #111827;
}

:global(.dark) .cust-mng-table :deep(.ant-pagination-item),
:global([data-theme='dark']) .cust-mng-table :deep(.ant-pagination-item) {
  background: transparent;
  border-color: #1f2a44;
  color: #cbd5e1;
}

:global(.dark) .cust-mng-table :deep(.ant-pagination-item-active),
:global([data-theme='dark']) .cust-mng-table :deep(.ant-pagination-item-active) {
  background: #1f2a44;
  border-color: #3b4a6b;
}

:global(.dark) .cust-mng-table :deep(.ant-pagination-prev .ant-pagination-item-link),
:global(.dark) .cust-mng-table :deep(.ant-pagination-next .ant-pagination-item-link),
:global([data-theme='dark'])
  .cust-mng-table
  :deep(.ant-pagination-prev .ant-pagination-item-link),
:global([data-theme='dark'])
  .cust-mng-table
  :deep(.ant-pagination-next .ant-pagination-item-link) {
  background: transparent;
  border-color: #1f2a44;
  color: #cbd5e1;
}
</style>
