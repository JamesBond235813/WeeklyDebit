<script lang="ts" setup>
import { ref } from 'vue';
import { Button, Table, Tag } from 'ant-design-vue';
import { useVbenModal } from '@vben/common-ui';
import { useVbenForm } from '#/adapter/form';
import { getOrderPage } from '#/api/biz/order';
import { getPlatformOptions } from '#/api/biz/platform';
import { getOrderStatusOptions } from '#/api/biz/order';
import { OrderStatusColorMap } from '#/api/biz/model/orderModel';
import { userApi } from '#/api/biz/user';
import { deptApi } from '#/api/biz/dept';
import OrderModal from './OrderModal.vue';

const DATA_TIME_FMT = 'YYYY-MM-DD HH:mm:ss';
const tableLoading = ref(false);
const dataSource = ref([]);
const pagination = ref({
  current: 1,
  pageSize: 20,
  total: 0,
});
const platformOptions = ref<any[]>([]);
const statusOptions = ref<any[]>([]);
const userOptions = ref<any[]>([]);
const deptOptions = ref<any[]>([]);

initOptions();

async function initOptions() {
    getPlatformOptions().then(res => {
        platformOptions.value = res.map(p => ({ label: p.name, value: p.name })); // Search by name keywords potentially? Request has kw.
    });
    getOrderStatusOptions().then(res => {
        statusOptions.value = res;
    });
    userApi.listTeamUser(0).then(res => {
        userOptions.value = res.map(u => ({
            label: `${u.name}${u.deptName ? `（${u.deptName}）` : ''}`,
            value: u.id,
        }));
    });
    deptApi.getDeptItems().then(res => {
        deptOptions.value = res.map(d => ({ label: d.deptName, value: d.id }));
    });
}

const [SearchForm, searchFormApi] = useVbenForm({
  wrapperClass: 'grid-cols-3',
  compact: true,
  schema: [
    {
      component: 'Input',
      fieldName: 'customerKeyword',
      label: '客户',
      componentProps: { placeholder: '姓名/手机号' },
    },
    {
      component: 'Input', 
      fieldName: 'platformKeyword',
      label: '平台',
      componentProps: { placeholder: '平台名称' },
    },
    {
      component: 'Select',
      fieldName: 'statusList',
      label: '状态',
      componentProps: { 
        placeholder: '订单状态',
        options: statusOptions,
        allowClear: true,
        mode: 'multiple',
      },
    },
    {
      component: 'Select',
      fieldName: 'ownerUserId',
      label: '业务员姓名',
      componentProps: {
        placeholder: '请选择业务员',
        options: userOptions,
        allowClear: true,
        showSearch: true,
        optionFilterProp: 'label',
      },
    },
    {
      component: 'Select',
      fieldName: 'ownerDeptId',
      label: '部门',
      componentProps: {
        placeholder: '请选择部门',
        options: deptOptions,
        allowClear: true,
        showSearch: true,
        optionFilterProp: 'label',
      },
    },
    {
      component: 'RangePicker',
      fieldName: 'orderTimePicker',
      label: '时间区间',
      componentProps: {
        showTime: true,
        showNow: true,
        showToday: true,
        placeholder: ['开始时间', '结束时间'],
      },
    },
  ],
  handleSubmit: onSearch,
  submitButtonOptions: { content: '查询' },
  resetButtonOptions: { content: '重置' },
});

const [ModalComponent, modalApi] = useVbenModal({
  connectedComponent: OrderModal,
});

const columns = [
  { title: 'ID', dataIndex: 'id', width: 60 },
  { title: '客户', dataIndex: 'customerId', customRender: ({record}) => `${record.customerName || record.customerId} ${record.customerMobile || ''}` }, // Assuming backend doesn't populate yet, verify later
  { title: '平台', dataIndex: 'platformName', customRender: ({record}) => record.platformName || record.platformId },
  { title: '型号', dataIndex: 'deviceModel' },
  { title: '状态', dataIndex: 'status', width: 100 },
  { title: '首付', dataIndex: 'downPaymentAmount', customRender: ({text}) => text ? `¥${text}` : '-' },
  { title: '垫付?', dataIndex: 'isDownPaymentAdvanced', width: 60, customRender: ({text}) => text ? '是' : '否' },
  { title: '回收/支出', dataIndex: 'recyclePrice', customRender: ({text}) => text ? `¥${text}` : '-' },
  { title: '利润', dataIndex: 'grossProfit', customRender: ({text}) => text ? `¥${text}` : '-' },
  { title: '渠道返佣', dataIndex: 'channelCommission', customRender: ({text}) => text ? `¥${text}` : '-' },
  { title: '操作', key: 'action', width: 100, align: 'center' },
];

async function loadData(page = 1) {
  tableLoading.value = true;
  try {
    const values = await searchFormApi.getValues();
    const { orderTimePicker, ...restValues } = values || {};
    const res = await getOrderPage({
      page,
      pageSize: pagination.value.pageSize,
      ...restValues,
      orderTimeStart: orderTimePicker?.[0]?.format(DATA_TIME_FMT),
      orderTimeEnd: orderTimePicker?.[1]?.format(DATA_TIME_FMT),
    });
    // Backend doesn't join tables yet, so names might be missing.
    // For now we just display IDs if names are null. 
    // Ideally update backend to return VOs with names.
    dataSource.value = res.records;
    pagination.value.current = res.current;
    pagination.value.total = res.total;
  } finally {
    tableLoading.value = false;
  }
}

async function onSearch() {
  loadData(1);
}

function handleTableChange(pag:any) {
  pagination.value.current = pag.current;
  pagination.value.pageSize = pag.pageSize;
  loadData(pag.current);
}

function handleAdd() {
  modalApi.setData({ record: null }).open();
}

function handleEdit(record: any) {
  modalApi.setData({ record }).open();
}

function handleSuccess() {
  loadData(pagination.value.current);
}

// Initial load
loadData();

</script>

<template>
  <div class="p-4">
    <div
      class="search-panel mb-4 rounded-lg border border-gray-200 bg-white p-4 shadow-sm transition-colors duration-200 dark:border-[#1f2a44] dark:bg-[#0f172a] dark:text-[#c7d2fe]"
    >
      <SearchForm />
    </div>
    
    <div
      class="order-table rounded-lg border border-gray-200 bg-white p-4 shadow-sm transition-colors duration-200 dark:border-[#1f2a44] dark:bg-[#0b1220] dark:text-[#c7d2fe]"
    >
        <div class="mb-2">
            <Button type="primary" @click="handleAdd">新建订单</Button>
        </div>
        <Table
          :columns="columns"
          :data-source="dataSource"
          :pagination="pagination"
          :loading="tableLoading"
          @change="handleTableChange"
          row-key="id"
          bordered
          size="small"
        >
            <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'action'">
                    <Button type="link" size="small" @click="handleEdit(record)">详情/处理</Button>
                </template>
                 <template v-if="column.dataIndex === 'status'">
                    <Tag :color="OrderStatusColorMap[record.status] || 'default'">
                        {{ statusOptions.find(o => o.value === record.status)?.label || record.status }}
                    </Tag>
                </template>
            </template>
        </Table>
    </div>
    <ModalComponent @success="handleSuccess" />
  </div>
</template>

<style scoped>
:global(.dark) .search-panel :deep(label),
:global([data-theme='dark']) .search-panel :deep(label),
:global(.dark) .search-panel :deep(.ant-form-item-label > label),
:global([data-theme='dark']) .search-panel :deep(.ant-form-item-label > label) {
  color: #c7d2fe;
}

.search-panel :deep(.ant-form-item-control-input),
.search-panel :deep(.ant-form-item-control-input-content),
.search-panel :deep(.ant-input),
.search-panel :deep(.ant-select),
.search-panel :deep(.ant-picker) {
  width: 100%;
}

:global(.dark) .order-table :deep(.ant-table),
:global([data-theme='dark']) .order-table :deep(.ant-table) {
  background: transparent;
  color: #e2e8f0;
}

:global(.dark) .order-table :deep(.ant-table-container),
:global([data-theme='dark']) .order-table :deep(.ant-table-container) {
  border-color: #1f2a44;
}

:global(.dark) .order-table :deep(.ant-table-thead > tr > th),
:global([data-theme='dark']) .order-table :deep(.ant-table-thead > tr > th) {
  background: #0f172a;
  color: #cbd5e1;
  border-color: #1f2a44;
}

:global(.dark) .order-table :deep(.ant-table-tbody > tr > td),
:global([data-theme='dark']) .order-table :deep(.ant-table-tbody > tr > td) {
  background: transparent;
  border-color: #1f2a44;
  color: #e2e8f0;
}

:global(.dark) .order-table :deep(.ant-table-tbody > tr.ant-table-row:hover > td),
:global([data-theme='dark'])
  .order-table
  :deep(.ant-table-tbody > tr.ant-table-row:hover > td) {
  background: #111827;
}

:global(.dark) .order-table :deep(.ant-pagination-item),
:global([data-theme='dark']) .order-table :deep(.ant-pagination-item) {
  background: transparent;
  border-color: #1f2a44;
  color: #cbd5e1;
}

:global(.dark) .order-table :deep(.ant-pagination-item-active),
:global([data-theme='dark']) .order-table :deep(.ant-pagination-item-active) {
  background: #1f2a44;
  border-color: #3b4a6b;
}

:global(.dark) .order-table :deep(.ant-pagination-prev .ant-pagination-item-link),
:global(.dark) .order-table :deep(.ant-pagination-next .ant-pagination-item-link),
:global([data-theme='dark'])
  .order-table
  :deep(.ant-pagination-prev .ant-pagination-item-link),
:global([data-theme='dark'])
  .order-table
  :deep(.ant-pagination-next .ant-pagination-item-link) {
  background: transparent;
  border-color: #1f2a44;
  color: #cbd5e1;
}
</style>
