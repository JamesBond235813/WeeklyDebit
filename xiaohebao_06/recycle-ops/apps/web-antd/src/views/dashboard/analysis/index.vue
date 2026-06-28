<script lang="ts" setup>
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue';
import { Button, Card, DatePicker, Empty, Select, Space, Table, Tag, message } from 'ant-design-vue';
import type { ECharts } from 'echarts';

import {
  getChannelTrend,
  getDashboardChannels,
  getSalesAssignmentStats,
  getStarPublicPoolEntryTrend,
  getTodayHourlyAdmissionStats,
  type ChannelPushTrendVO,
  type SalesAssignmentStatsItemVO,
} from '#/api/biz/stats';

const loading = ref(false);
const filterRange = ref<[string, string]>([offsetDate(6), offsetDate(0)]);
const selectedChannel = ref<string | undefined>();
const channelOptions = ref<string[]>([]);
const assignmentRows = ref<SalesAssignmentStatsItemVO[]>([]);
const inboundEmpty = ref(false);
const admissionEmpty = ref(false);
const starEmpty = ref(false);
const hourlyEmpty = ref(false);

const inboundChartRef = ref<HTMLDivElement | null>(null);
const admissionChartRef = ref<HTMLDivElement | null>(null);
const starChartRef = ref<HTMLDivElement | null>(null);
const hourlyChartRef = ref<HTMLDivElement | null>(null);
let echartsModule: typeof import('echarts') | null = null;
let inboundChart: ECharts | null = null;
let admissionChart: ECharts | null = null;
let starChart: ECharts | null = null;
let hourlyChart: ECharts | null = null;

const assignmentColumns = [
  { title: '业务员', dataIndex: 'userName', width: 120 },
  { title: '部门', dataIndex: 'deptName', width: 150 },
  { title: '自动分配', dataIndex: 'autoCount', align: 'right', width: 90 },
  { title: '公海领取', dataIndex: 'manualCount', align: 'right', width: 90 },
  { title: '带☆领取', dataIndex: 'starManualCount', align: 'right', width: 90 },
  { title: '合计', dataIndex: 'totalCount', align: 'right', width: 80 },
];

function offsetDate(offset: number) {
  const date = new Date();
  date.setDate(date.getDate() - offset);
  const year = date.getFullYear();
  const month = `${date.getMonth() + 1}`.padStart(2, '0');
  const day = `${date.getDate()}`.padStart(2, '0');
  return `${year}-${month}-${day}`;
}

async function ensureEcharts() {
  if (!echartsModule) {
    echartsModule = await import('echarts');
  }
  return echartsModule;
}

function getRange() {
  const [startDate, endDate] = filterRange.value || [];
  return {
    startDate: startDate || offsetDate(6),
    endDate: endDate || offsetDate(0),
  };
}

function buildLineSeries(data: ChannelPushTrendVO) {
  return data.channels.map((channel) => ({
    name: channel,
    type: 'line',
    smooth: true,
    symbolSize: 5,
    data: data.series[channel] || [],
  }));
}

function hasLineData(data: ChannelPushTrendVO) {
  return data.channels.some((channel) => (data.series[channel] || []).some((value) => Number(value) > 0));
}

function hasNumberData(values?: number[]) {
  return Array.isArray(values) && values.some((value) => Number(value) > 0);
}

async function renderChannelChart(target: 'inbound' | 'admission') {
  const echarts = await ensureEcharts();
  const { startDate, endDate } = getRange();
  const data = await getChannelTrend(startDate, endDate, selectedChannel.value, target === 'admission');
  const isEmpty = !hasLineData(data);
  if (target === 'inbound') {
    inboundEmpty.value = isEmpty;
  } else {
    admissionEmpty.value = isEmpty;
  }
  await nextTick();
  const chartRef = target === 'inbound' ? inboundChartRef : admissionChartRef;
  if (!chartRef.value) {
    return;
  }
  const chart = target === 'inbound'
    ? (inboundChart ??= echarts.init(chartRef.value))
    : (admissionChart ??= echarts.init(chartRef.value));
  if (isEmpty) {
    chart.clear();
    return;
  }
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { type: 'scroll', top: 0, itemWidth: 14, itemHeight: 8 },
    grid: { left: 18, right: 16, top: 34, bottom: 22, containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: data.dates },
    yAxis: { type: 'value', minInterval: 1 },
    series: buildLineSeries(data),
  }, true);
}

async function renderStarChart() {
  const echarts = await ensureEcharts();
  const { startDate, endDate } = getRange();
  const data = await getStarPublicPoolEntryTrend(startDate, endDate);
  starEmpty.value = !hasNumberData(data.starEntryCounts);
  await nextTick();
  if (!starChartRef.value) {
    return;
  }
  starChart ??= echarts.init(starChartRef.value);
  if (starEmpty.value) {
    starChart.clear();
    return;
  }
  starChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 18, right: 16, top: 18, bottom: 22, containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: data.dates },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{ name: '每日推入公海☆', type: 'line', smooth: true, symbolSize: 5, data: data.starEntryCounts }],
  }, true);
}

async function renderHourlyChart() {
  const echarts = await ensureEcharts();
  const data = await getTodayHourlyAdmissionStats(selectedChannel.value);
  hourlyEmpty.value = !hasNumberData(data.passedCounts) && !hasNumberData(data.passRates);
  await nextTick();
  if (!hourlyChartRef.value) {
    return;
  }
  hourlyChart ??= echarts.init(hourlyChartRef.value);
  if (hourlyEmpty.value) {
    hourlyChart.clear();
    return;
  }
  hourlyChart.setOption({
    tooltip: {
      trigger: 'axis',
      valueFormatter: (value: number | string) => `${value}`,
    },
    legend: { top: 0, itemWidth: 14, itemHeight: 8 },
    grid: { left: 18, right: 34, top: 34, bottom: 22, containLabel: true },
    xAxis: { type: 'category', boundaryGap: true, data: data.hours },
    yAxis: [
      { type: 'value', minInterval: 1, name: '数量' },
      { type: 'value', min: 0, max: 100, name: '通过率', axisLabel: { formatter: '{value}%' } },
    ],
    series: [
      { name: '符合准入量', type: 'bar', yAxisIndex: 0, data: data.passedCounts, barMaxWidth: 24 },
      { name: '通过率', type: 'line', smooth: true, yAxisIndex: 1, data: data.passRates },
    ],
  }, true);
}

async function loadAssignmentStats() {
  const { startDate, endDate } = getRange();
  assignmentRows.value = await getSalesAssignmentStats(startDate, endDate);
}

async function refreshAll() {
  const { startDate, endDate } = getRange();
  if (!startDate || !endDate) {
    message.warning('请选择日期区间');
    return;
  }
  loading.value = true;
  try {
    await Promise.all([
      renderChannelChart('inbound'),
      renderChannelChart('admission'),
      renderStarChart(),
      renderHourlyChart(),
      loadAssignmentStats(),
    ]);
  } finally {
    loading.value = false;
  }
}

async function loadChannelOptions() {
  channelOptions.value = await getDashboardChannels();
}

function resizeCharts() {
  inboundChart?.resize();
  admissionChart?.resize();
  starChart?.resize();
  hourlyChart?.resize();
}

onMounted(async () => {
  await loadChannelOptions();
  await refreshAll();
  window.addEventListener('resize', resizeCharts);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts);
  inboundChart?.dispose();
  admissionChart?.dispose();
  starChart?.dispose();
  hourlyChart?.dispose();
  inboundChart = null;
  admissionChart = null;
  starChart = null;
  hourlyChart = null;
});
</script>

<template>
  <div class="dashboard-page">
    <Card class="filter-card" :bordered="false">
      <Space wrap>
        <DatePicker.RangePicker
          v-model:value="filterRange"
          value-format="YYYY-MM-DD"
          :allow-clear="false"
          size="small"
        />
        <Select
          v-model:value="selectedChannel"
          allow-clear
          class="channel-select"
          placeholder="全部渠道"
          size="small"
        >
          <Select.Option v-for="item in channelOptions" :key="item" :value="item">{{ item }}</Select.Option>
        </Select>
        <Button size="small" type="primary" @click="refreshAll">查询</Button>
      </Space>
    </Card>

    <div class="chart-grid">
      <Card title="渠道每日入库量" :bordered="false" size="small" class="chart-card">
        <div class="chart-box">
          <div ref="inboundChartRef" class="chart" :class="{ hidden: inboundEmpty }"></div>
          <Empty v-if="inboundEmpty" class="chart-empty" description="暂无入库数据" />
        </div>
      </Card>
      <Card title="渠道每日符合准入量" :bordered="false" size="small" class="chart-card">
        <div class="chart-box">
          <div ref="admissionChartRef" class="chart" :class="{ hidden: admissionEmpty }"></div>
          <Empty v-if="admissionEmpty" class="chart-empty" description="暂无准入统计数据" />
        </div>
      </Card>
      <Card title="☆公海客户趋势" :bordered="false" size="small" class="chart-card">
        <div class="chart-box">
          <div ref="starChartRef" class="chart" :class="{ hidden: starEmpty }"></div>
          <Empty v-if="starEmpty" class="chart-empty" description="暂无☆公海数据" />
        </div>
      </Card>
      <Card title="今日每小时准入量 / 通过率" :bordered="false" size="small" class="chart-card">
        <div class="chart-box">
          <div ref="hourlyChartRef" class="chart" :class="{ hidden: hourlyEmpty }"></div>
          <Empty v-if="hourlyEmpty" class="chart-empty" description="今日暂无准入统计" />
        </div>
      </Card>
    </div>

    <Card title="业务员客户分配统计" :bordered="false" :loading="loading" size="small" class="assignment-card">
      <Table
        :columns="assignmentColumns"
        :data-source="assignmentRows"
        :pagination="{ pageSize: 10, showSizeChanger: false, size: 'small' }"
        row-key="userId"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'starManualCount'">
            <Tag v-if="record.starManualCount > 0" color="gold">☆ {{ record.starManualCount }}</Tag>
            <span v-else>0</span>
          </template>
        </template>
        <template #emptyText>
          <Empty description="暂无分配数据" />
        </template>
      </Table>
    </Card>
  </div>
</template>

<style scoped>
.dashboard-page {
  display: grid;
  gap: 8px;
  grid-template-rows: auto minmax(300px, 52%) minmax(260px, 1fr);
  height: calc(100vh - 92px);
  min-height: 660px;
  overflow: hidden;
  padding: 8px 12px;
}

.filter-card :deep(.ant-card-body) {
  padding: 8px 12px;
}

.channel-select {
  width: 180px;
}

.chart-grid {
  display: grid;
  gap: 8px;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  grid-template-rows: minmax(0, 1fr) minmax(0, 1fr);
  min-height: 0;
}

.chart-card {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

.chart-grid :deep(.ant-card-head) {
  min-height: 32px;
  padding: 0 12px;
}

.chart-grid :deep(.ant-card-head-title) {
  padding: 6px 0;
}

.chart-grid :deep(.ant-card-body) {
  flex: 1;
  padding: 6px 8px;
  min-height: 0;
}

.chart {
  height: 100%;
  min-height: 120px;
}

.chart-box {
  height: 100%;
  min-height: 0;
  position: relative;
}

.chart.hidden {
  visibility: hidden;
}

.chart-empty {
  left: 50%;
  position: absolute;
  top: 50%;
  transform: translate(-50%, -50%);
}

.assignment-card {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

.assignment-card :deep(.ant-card-head) {
  min-height: 32px;
  padding: 0 12px;
}

.assignment-card :deep(.ant-card-head-title) {
  padding: 6px 0;
}

.assignment-card :deep(.ant-card-body) {
  flex: 1;
  min-height: 0;
  padding: 4px 8px 8px;
}
</style>
