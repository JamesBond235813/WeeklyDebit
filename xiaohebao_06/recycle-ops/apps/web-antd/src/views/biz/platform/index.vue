<script lang="ts" setup>
import { reactive, ref } from 'vue';
import { Button, Table, message, Modal, Tag } from 'ant-design-vue';
import { useVbenModal } from '@vben/common-ui';
import { useVbenForm } from '#/adapter/form';
import { getPlatformPage, deletePlatform } from '#/api/biz/platform';
import PlatformModal from './PlatformModal.vue';

const tableLoading = ref(false);
const dataSource = ref([]);
const pagination = ref({
  current: 1,
  pageSize: 20,
  total: 0,
});

const [SearchForm, searchFormApi] = useVbenForm({
  wrapperClass: 'grid-cols-4',
  compact: true,
  schema: [
    {
      component: 'Input',
      fieldName: 'name',
      label: '名称',
      componentProps: { placeholder: '平台名称' },
    },
    {
      component: 'Select',
      fieldName: 'type',
      label: '类型',
      componentProps: { 
        placeholder: '平台类型',
        options: [
            { label: '分期', value: 'INSTALLMENT' },
            { label: '租赁', value: 'RENTAL' },
        ],
        allowClear: true,
      },
    },
    {
      component: 'Select',
      fieldName: 'status',
      label: '状态',
      componentProps: { 
        placeholder: '启用状态',
         options: [
            { label: '启用', value: 1 },
            { label: '禁用', value: 0 },
        ],
        allowClear: true,
      },
    },
  ],
  handleSubmit: onSearch,
  submitButtonOptions: { content: '查询' },
  resetButtonOptions: { content: '重置' },
});

const [ModalComponent, modalApi] = useVbenModal({
  connectedComponent: PlatformModal,
});

const columns = [
  { title: 'ID', dataIndex: 'id', width: 60 },
  { title: '名称', dataIndex: 'name' },
  { title: '类型', dataIndex: 'type', width: 100, customRender: ({ text }) => text === 'INSTALLMENT' ? '分期' : '租赁' },
  { title: '链接', dataIndex: 'link', ellipsis: true },
  { title: '备注', dataIndex: 'remark', ellipsis: true },
  { title: '状态', dataIndex: 'status', width: 80, customRender: ({ text }) => text === 1 ? '启用' : '禁用' }, // We can use Tag later
  { title: '操作', key: 'action', width: 150, align: 'center' },
];

async function loadData(page = 1) {
  tableLoading.value = true;
  try {
    const values = await searchFormApi.getValues();
    const res = await getPlatformPage({
      page,
      pageSize: pagination.value.pageSize,
      ...values,
    });
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

function handleDelete(record: any) {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除平台 "${record.name}" 吗？`,
    onOk: async () => {
      await deletePlatform(record.id);
      message.success('删除成功');
      loadData(pagination.value.current);
    },
  });
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
      class="platform-table rounded-lg border border-gray-200 bg-white p-4 shadow-sm transition-colors duration-200 dark:border-[#1f2a44] dark:bg-[#0b1220] dark:text-[#c7d2fe]"
    >
        <div class="mb-2">
            <Button type="primary" @click="handleAdd">新增平台</Button>
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
                    <Button type="link" size="small" @click="handleEdit(record)">编辑</Button>
                    <Button type="link" size="small" danger @click="handleDelete(record)">删除</Button>
                </template>
                 <template v-if="column.dataIndex === 'status'">
                    <Tag :color="record.status === 1 ? 'success' : 'error'">
                        {{ record.status === 1 ? '启用' : '禁用' }}
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

:global(.dark) .platform-table :deep(.ant-table),
:global([data-theme='dark']) .platform-table :deep(.ant-table) {
  background: transparent;
  color: #e2e8f0;
}

:global(.dark) .platform-table :deep(.ant-table-container),
:global([data-theme='dark']) .platform-table :deep(.ant-table-container) {
  border-color: #1f2a44;
}

:global(.dark) .platform-table :deep(.ant-table-thead > tr > th),
:global([data-theme='dark']) .platform-table :deep(.ant-table-thead > tr > th) {
  background: #0f172a;
  color: #cbd5e1;
  border-color: #1f2a44;
}

:global(.dark) .platform-table :deep(.ant-table-tbody > tr > td),
:global([data-theme='dark']) .platform-table :deep(.ant-table-tbody > tr > td) {
  background: transparent;
  border-color: #1f2a44;
  color: #e2e8f0;
}

:global(.dark) .platform-table :deep(.ant-table-tbody > tr.ant-table-row:hover > td),
:global([data-theme='dark'])
  .platform-table
  :deep(.ant-table-tbody > tr.ant-table-row:hover > td) {
  background: #111827;
}

:global(.dark) .platform-table :deep(.ant-pagination-item),
:global([data-theme='dark']) .platform-table :deep(.ant-pagination-item) {
  background: transparent;
  border-color: #1f2a44;
  color: #cbd5e1;
}

:global(.dark) .platform-table :deep(.ant-pagination-item-active),
:global([data-theme='dark']) .platform-table :deep(.ant-pagination-item-active) {
  background: #1f2a44;
  border-color: #3b4a6b;
}

:global(.dark) .platform-table :deep(.ant-pagination-prev .ant-pagination-item-link),
:global(.dark) .platform-table :deep(.ant-pagination-next .ant-pagination-item-link),
:global([data-theme='dark'])
  .platform-table
  :deep(.ant-pagination-prev .ant-pagination-item-link),
:global([data-theme='dark'])
  .platform-table
  :deep(.ant-pagination-next .ant-pagination-item-link) {
  background: transparent;
  border-color: #1f2a44;
  color: #cbd5e1;
}
</style>
