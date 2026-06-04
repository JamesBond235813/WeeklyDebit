<script lang="ts" setup>
import type { LoanRecord } from '#/api/biz/loan';
import type { TableColumnType } from 'ant-design-vue';

import { ref } from 'vue';

import { Page } from '@vben/common-ui';

import { Button, Modal, Table, message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { loanApi } from '#/api/biz/loan';

const tableLoading = ref(false);
const dataSource = ref<LoanRecord[]>([]);
const statusOptions = ref<{ label: string; value: string }[]>([]);
const editOpen = ref(false);
const editingId = ref<number | undefined>();
const pagination = ref({
  current: 1,
  pageSize: 20,
  total: 0,
});

loanApi.statusOptions().then((res) => {
  statusOptions.value = res || [];
});

const [SearchForm, searchFormApi] = useVbenForm({
  wrapperClass: 'grid-cols-4',
  compact: true,
  schema: [
    {
      component: 'InputNumber',
      fieldName: 'customerId',
      label: '客户ID',
      componentProps: { min: 1, precision: 0, placeholder: '客户ID' },
    },
    {
      component: 'Select',
      fieldName: 'loanStatus',
      label: '状态',
      componentProps: {
        allowClear: true,
        options: statusOptions,
        placeholder: '放款状态',
      },
    },
  ],
  handleSubmit: onSearch,
  resetButtonOptions: { content: '重置' },
  submitButtonOptions: { content: '查询' },
});

const [EditForm, editFormApi] = useVbenForm({
  wrapperClass: 'grid-cols-2 gap-x-4',
  showDefaultActions: false,
  schema: [
    {
      component: 'InputNumber',
      fieldName: 'customerId',
      label: '客户ID',
      rules: 'required',
      componentProps: { min: 1, precision: 0 },
    },
    {
      component: 'InputNumber',
      fieldName: 'loanAmount',
      label: '放款金额',
      rules: 'required',
      componentProps: { min: 0, precision: 2, prefix: '¥' },
    },
    {
      component: 'InputNumber',
      fieldName: 'loanTerm',
      label: '期限',
      rules: 'required',
      componentProps: { min: 1, precision: 0 },
    },
    {
      component: 'InputNumber',
      fieldName: 'serviceFeeRate',
      label: '服务费费率',
      rules: 'required',
      componentProps: { min: 0, precision: 4 },
    },
    {
      component: 'InputNumber',
      fieldName: 'receivableAmount',
      label: '应回款金额',
      rules: 'required',
      componentProps: { min: 0, precision: 2, prefix: '¥' },
    },
    {
      component: 'InputNumber',
      fieldName: 'repaymentAmount',
      label: '已回款金额',
      componentProps: { min: 0, precision: 2, prefix: '¥' },
    },
    {
      component: 'Select',
      fieldName: 'loanStatus',
      label: '状态',
      componentProps: { options: statusOptions },
    },
    {
      component: 'InputTextArea',
      fieldName: 'remark',
      label: '备注',
      formItemClass: 'col-span-2',
      componentProps: { rows: 3 },
    },
  ],
});

const columns: TableColumnType<LoanRecord>[] = [
  { title: 'ID', dataIndex: 'id', width: 70 },
  { title: '客户ID', dataIndex: 'customerId', width: 90 },
  { title: '放款金额', dataIndex: 'loanAmount', width: 120 },
  { title: '期限', dataIndex: 'loanTerm', width: 80 },
  { title: '服务费费率', dataIndex: 'serviceFeeRate', width: 110 },
  { title: '应回款', dataIndex: 'receivableAmount', width: 120 },
  { title: '已回款', dataIndex: 'repaymentAmount', width: 120 },
  { title: '状态', dataIndex: 'loanStatus', width: 120 },
  { title: '创建时间', dataIndex: 'gmtCreate', width: 170 },
  { title: '操作', dataIndex: 'action', width: 90, fixed: 'right' },
];

function getStatusLabel(value?: string) {
  return statusOptions.value.find((item) => item.value === value)?.label || value || '-';
}

async function loadData(params: any = {}) {
  tableLoading.value = true;
  try {
    const res = await loanApi.page({
      ...params,
      page: params.page ?? pagination.value.current,
      pageSize: params.pageSize ?? pagination.value.pageSize,
    });
    dataSource.value = res?.records || [];
    pagination.value = {
      current: res?.current || 1,
      pageSize: res?.size || 20,
      total: res?.total || 0,
    };
  } finally {
    tableLoading.value = false;
  }
}

async function onSearch(values: any) {
  await loadData({ ...values, page: 1 });
}

function openCreate() {
  editingId.value = undefined;
  editOpen.value = true;
  editFormApi.resetForm();
  editFormApi.setValues({ loanStatus: 'DISBURSED' });
}

function openEdit(record: LoanRecord) {
  editingId.value = record.id;
  editOpen.value = true;
  editFormApi.setValues({ ...record });
}

async function handleSave() {
  const { valid } = await editFormApi.validate();
  if (!valid) {
    return;
  }
  const values = await editFormApi.getValues();
  await loanApi.save({ ...values, id: editingId.value });
  message.success('保存成功');
  editOpen.value = false;
  const searchValues = await searchFormApi.getValues();
  await loadData(searchValues);
}

function handleTableChange(pager: any) {
  searchFormApi.getValues().then((values) => {
    loadData({ ...values, page: pager.current, pageSize: pager.pageSize });
  });
}

loadData();
</script>

<template>
  <Page>
    <div class="mb-3 flex items-center justify-between">
      <SearchForm />
      <Button type="primary" @click="openCreate">新增放款记录</Button>
    </div>
    <Table
      :columns="columns"
      :data-source="dataSource"
      :loading="tableLoading"
      :pagination="pagination"
      row-key="id"
      size="small"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'loanStatus'">
          {{ getStatusLabel(record.loanStatus) }}
        </template>
        <template v-if="column.dataIndex === 'gmtCreate'">
          {{ record.gmtCreate || '-' }}
        </template>
        <template v-if="column.dataIndex === 'action'">
          <Button type="link" size="small" @click="openEdit(record)">编辑</Button>
        </template>
      </template>
    </Table>
    <Modal v-model:open="editOpen" title="放款记录" width="720px" @ok="handleSave">
      <EditForm />
    </Modal>
  </Page>
</template>
