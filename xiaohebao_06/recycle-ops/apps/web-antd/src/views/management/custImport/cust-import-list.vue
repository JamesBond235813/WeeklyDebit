<script lang="ts" setup>
import type { VxeGridProps } from '#/adapter/vxe-table';
import type { PagedInfo } from '#/api/biz/biz-common';
import type {
  ImportRecInfo,
  PagedListImportRecordParams,
} from '#/api/biz/customer';

import { Button } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { customerApi } from '#/api/biz/customer';

import CustInfoImporter from './cust-info-importer.vue';

const gridOptions: VxeGridProps<ImportRecInfo> = {
  cellStyle: {
    padding: '1px',
  },
  columns: [
    {
      title: '#',
      width: 50,
      fixed: 'left',
      type: 'seq',
    },
    { title: '文件名', field: 'oriFileName', width: 200, fixed: 'left' },
    { title: '处理状态', field: 'procStatusDesc', width: 100 },
    { title: '记录数', field: 'totalCnt', width: 90 },
    { title: '已处理', field: 'processedCnt', width: 90 },
    { title: '入库记录数', field: 'insertedCnt', width: 110 },
    { title: '数据分配目标部门', field: 'targetDeptName', width: 150 },
    { title: '数据分配目标人员', field: 'targetUserName', width: 150 },
    { title: '导入完成时间', field: 'finishTime', width: 160 },
    { title: '上传文件时间', field: 'gmtCreate', width: 160 },
    { title: '导入失败原因', field: 'channelDesc', width: 200 },
    {
      field: 'download',
      title: '重复客户',
      width: 100,
      align: 'center',
      slots: { default: 'download' },
      fixed: 'right',
    },
  ],
  showOverflow: false,
  scrollX: {
    enabled: true,
  },
  scrollY: {
    enabled: false,
  },
  pagerConfig: {
    enabled: true,
    pageSize: 20,
    pageSizes: [20, 50],
  },
  border: true,
  proxyConfig: {
    ajax: {
      query: async (component: { page: any }) => {
        return await pagedListImportRecord({
          page: component.page.currentPage,
          pageSize: component.page.pageSize,
          selfOnly: false,
        });
      },
    },
  },
  rowConfig: {
    isHover: true,
  },
  toolbarConfig: {
    refresh: true,
    slots: {
      buttons: 'downloadTmpl',
    },
  },
};

async function pagedListImportRecord(params: PagedListImportRecordParams) {
  // return await customerApi.pagedListImportRecord(params);
  const data: PagedInfo<ImportRecInfo> =
    await customerApi.pagedListImportRecord(params);
  return { items: data.list, total: data.total };
}

const [ImportRecordGrid, importRecordGridApi] = useVbenVxeGrid({ gridOptions });

function downloadById(recId: number) {
  customerApi.downloadExistedCustRecord(recId);
}
</script>
<template>
  <div class="w-full">
    <CustInfoImporter :after-upload-succ="importRecordGridApi.reload" />
    <ImportRecordGrid class="import-record-table">
      <template #downloadTmpl>
        <Button
          size="small"
          type="link"
          @click="customerApi.downloadCustTemplate()"
        >
          下载导入模板
        </Button>
      </template>
      <template #download="{ row }">
        <Button
          size="small"
          type="link"
          class="mr-1"
          @click="downloadById(row.id)"
          v-if="row.downloadDupRecFlag === 1"
        >
          下载
        </Button>
      </template>
    </ImportRecordGrid>
  </div>
</template>

<style scoped>
.import-record-table :deep(.vxe-table) {
  /* 确保表格正确渲染 */
}

.import-record-table :deep(.vxe-table--header) {
  /* 强制表头与表体对齐 */
  overflow: hidden !important;
}

.import-record-table :deep(.vxe-body--column.col--fixed-right) {
  /* 确保固定列正确渲染 */
  border-left: 1px solid #e8e8e8;
}
</style>
