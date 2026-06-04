<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { Button, Card, Empty, Space, Table } from 'ant-design-vue';

import { getDispatchSummary, type DispatchStatsItemVO } from '#/api/biz/stats';

const loading = ref(false);
const dataSource = ref<DispatchStatsItemVO[]>([]);

const columns = [
  { title: '姓名', dataIndex: 'userName', key: 'userName' },
  { title: '部门', dataIndex: 'deptName', key: 'deptName' },
  { title: '今日', dataIndex: 'todayCount', key: 'todayCount', width: 120 },
  { title: '昨日', dataIndex: 'yesterdayCount', key: 'yesterdayCount', width: 120 },
  { title: '近7日', dataIndex: 'recent7Count', key: 'recent7Count', width: 120 },
];

async function loadData() {
  loading.value = true;
  try {
    dataSource.value = (await getDispatchSummary()) || [];
  } finally {
    loading.value = false;
  }
}

onMounted(loadData);
</script>

<template>
  <div class="p-4">
    <Card :bordered="false">
      <div class="mb-4 flex items-center justify-between">
        <div class="text-base font-medium">客户分配统计</div>
        <Space>
          <Button @click="loadData" :loading="loading">刷新</Button>
        </Space>
      </div>

      <Table
        :columns="columns"
        :data-source="dataSource"
        row-key="userId"
        size="small"
        :pagination="false"
        :loading="loading"
      />
      <Empty v-if="!loading && dataSource.length === 0" class="py-6" />
    </Card>
  </div>
</template>
