<script lang="ts" setup>
import { ref, onMounted, onBeforeUnmount, nextTick, computed } from 'vue';
import { Card, Row, Col, Table, Tag, Empty } from 'ant-design-vue';
import * as echarts from 'echarts';
import { useAccess } from '@vben/access';
import { getDashboardSummary, type StatsDashboardVO } from '#/api/biz/stats';

// 权限控制
const access = useAccess();
const isManagement = access.hasAccessByRoles(['ROLE_SUPPER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_DEPT_ADMIN', 'ROLE_DEPT_DATA_ADMIN']);

const loading = ref(false);
const data = ref<StatsDashboardVO | null>(null);
const periodKpis = computed(() => {
  if (!data.value) {
    return [];
  }
  return [
    { label: '今日', data: data.value.todayKpi },
    { label: '近7日', data: data.value.recent7Kpi },
    { label: '近30日', data: data.value.recent30Kpi },
  ];
});

function formatMoney(value?: number) {
  const num = Number(value || 0);
  return num.toFixed(2);
}

function formatCount(value?: number) {
  const num = Number(value || 0);
  return Number.isFinite(num) ? num : 0;
}

// Chart Refs
const trendChartRef = ref<HTMLDivElement | null>(null);
const funnelChartRef = ref<HTMLDivElement | null>(null);
const regionChartRef = ref<HTMLDivElement | null>(null);
const ageChartRef = ref<HTMLDivElement | null>(null);
const sexChartRef = ref<HTMLDivElement | null>(null);

const leaderboardColumns = [
  { title: '排名', dataIndex: 'rank', width: 60, align: 'center' },
  { title: '姓名', dataIndex: 'name' },
  { title: '部门', dataIndex: 'department' },
  { title: '客户数', dataIndex: 'newCustomers', sorter: (a:any, b:any) => a.newCustomers - b.newCustomers }, // 暂用总客户数
  // { title: '跟进次数', dataIndex: 'followUps', sorter: (a:any, b:any) => a.followUps - b.followUps },
];

const trendDayOptions = [3, 5, 7, 10, 15, 30];
const selectedTrendDays = ref(30);

onMounted(async () => {
  loading.value = true;
  try {
    data.value = await getDashboardSummary();
    // Ensure DOM updated before initializing charts
    await nextTick();
    initCharts();
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
  window.addEventListener('resize', resizeCharts);
});

let trendChart: echarts.ECharts | null = null;
let funnelChart: echarts.ECharts | null = null;
let regionChart: echarts.ECharts | null = null;
let ageChart: echarts.ECharts | null = null;
let sexChart: echarts.ECharts | null = null;

function getTrendSlice(days: number) {
  if (!data.value) {
    return { dates: [], newCustomers: [], orderCounts: [] };
  }
  const trend = data.value.trend;
  const total = trend?.dates?.length ?? 0;
  const size = Math.min(days, total);
  const start = Math.max(total - size, 0);
  return {
    dates: trend?.dates?.slice(start) ?? [],
    newCustomers: trend?.newCustomers?.slice(start) ?? [],
    orderCounts: trend?.orderCounts?.slice(start) ?? [],
  };
}

function buildTrendOption(days: number) {
  const slice = getTrendSlice(days);
  return {
    title: { text: `近${days}天新增趋势`, left: 'left' },
    tooltip: { trigger: 'axis' },
    legend: { data: ['新增客户', '订单数量'], top: 6, right: 12 },
    grid: { left: 24, right: 24, top: 56, bottom: 50, containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: slice.dates,
      axisLabel: { rotate: 30, margin: 12 },
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      { name: '新增客户', type: 'line', data: slice.newCustomers, smooth: true, itemStyle: { color: '#1890ff' } },
      { name: '订单数量', type: 'line', data: slice.orderCounts, smooth: true, itemStyle: { color: '#52c41a' } },
    ],
  };
}

function updateTrendChart(days: number) {
  if (!trendChart) {
    return;
  }
  trendChart.setOption(buildTrendOption(days));
}

function handleTrendDaysChange(days: number) {
  selectedTrendDays.value = days;
  updateTrendChart(days);
}

function initCharts() {
  if (!data.value) return;

  // Trend
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value);
    trendChart.setOption(buildTrendOption(selectedTrendDays.value));
  }

  // Funnel
  if (funnelChartRef.value) {
    funnelChart = echarts.init(funnelChartRef.value);
    funnelChart.setOption({
      title: { text: '客户状态漏斗' },
      tooltip: { trigger: 'item', formatter: '{a} <br/>{b} : {c}' },
      series: [
        {
          name: '漏斗图',
          type: 'funnel',
          left: '10%',
          top: 60,
          bottom: 60,
          width: '80%',
          min: 0,
          maxSize: '100%',
          sort: 'descending',
          gap: 2,
          label: { show: true, position: 'inside' },
          itemStyle: { borderColor: '#fff', borderWidth: 1 },
          data: data.value.funnel
        }
      ]
    });
  }
  
  // Sex (Pie)
  if (sexChartRef.value && data.value.distribution.sex.length) {
      sexChart = echarts.init(sexChartRef.value);
      sexChart.setOption({
          title: { text: '性别占比', left: 'center' },
          tooltip: { trigger: 'item' },
          series: [{ type: 'pie', radius: '50%', data: data.value.distribution.sex }]
      });
  }
  
  // Age (Bar)
  if (ageChartRef.value && data.value.distribution.age.length) {
      ageChart = echarts.init(ageChartRef.value);
      ageChart.setOption({
          title: { text: '年龄分布', left: 'center' },
          tooltip: { trigger: 'axis' },
          xAxis: { type: 'category', data: data.value.distribution.age.map(i => i.name) },
          yAxis: { type: 'value' },
          series: [{ type: 'bar', data: data.value.distribution.age.map(i => i.value), itemStyle: { color: '#52c41a' } }]
      });
  }
  
  // Region (Bar Horizontal)
  if (regionChartRef.value && data.value.distribution.region.length) {
      regionChart = echarts.init(regionChartRef.value);
      regionChart.setOption({
          title: { text: '地域分布 (Top 10)', left: 'center' },
          tooltip: { trigger: 'axis' },
          grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
          xAxis: { type: 'value' },
          yAxis: { type: 'category', data: data.value.distribution.region.map(i => i.name).reverse() },
          series: [{ type: 'bar', data: data.value.distribution.region.map(i => i.value).reverse(), itemStyle: { color: '#faad14' } }]
      });
  }
}

function resizeCharts() {
  trendChart?.resize();
  funnelChart?.resize();
  regionChart?.resize();
  ageChart?.resize();
  sexChart?.resize();
}

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts);
  trendChart?.dispose();
  funnelChart?.dispose();
  regionChart?.dispose();
  ageChart?.dispose();
  sexChart?.dispose();
  trendChart = funnelChart = regionChart = ageChart = sexChart = null;
});

</script>

<template>
  <div class="p-4" v-if="data">
    <!-- Period KPI -->
    <Card :bordered="false" class="mb-4">
      <div class="space-y-4">
        <div
          v-for="item in periodKpis"
          :key="item.label"
          class="grid grid-cols-6 gap-4 items-stretch"
        >
          <div
            class="flex items-center justify-center rounded bg-blue-50 text-blue-700 text-sm font-semibold dark:bg-[#0f172a] dark:text-[#93c5fd]"
          >
            {{ item.label }}
          </div>
          <div class="col-span-5 grid grid-cols-5 gap-4">
            <div class="rounded border border-gray-100 bg-white p-3 text-center shadow-sm dark:border-[#1f2a44] dark:bg-[#0b1220]">
              <div class="text-xs text-gray-500 dark:text-[#94a3b8]">毛收入</div>
              <div class="text-lg font-semibold text-red-600">¥{{ formatMoney(item.data?.grossProfit) }}</div>
            </div>
            <div class="rounded border border-gray-100 bg-white p-3 text-center shadow-sm dark:border-[#1f2a44] dark:bg-[#0b1220]">
              <div class="text-xs text-gray-500 dark:text-[#94a3b8]">渠道返佣总额</div>
              <div class="text-lg font-semibold">¥{{ formatMoney(item.data?.channelCommission) }}</div>
            </div>
            <div class="rounded border border-gray-100 bg-white p-3 text-center shadow-sm dark:border-[#1f2a44] dark:bg-[#0b1220]">
              <div class="text-xs text-gray-500 dark:text-[#94a3b8]">成交数</div>
              <div class="text-lg font-semibold">{{ formatCount(item.data?.orderCount) }}</div>
            </div>
            <div class="rounded border border-gray-100 bg-white p-3 text-center shadow-sm dark:border-[#1f2a44] dark:bg-[#0b1220]">
              <div class="text-xs text-gray-500 dark:text-[#94a3b8]">进行中订单</div>
              <div class="text-lg font-semibold text-amber-600">{{ formatCount(item.data?.pendingOrderCount) }}</div>
            </div>
            <div class="rounded border border-gray-100 bg-white p-3 text-center shadow-sm dark:border-[#1f2a44] dark:bg-[#0b1220]">
              <div class="text-xs text-gray-500 dark:text-[#94a3b8]">新增客户</div>
              <div class="text-lg font-semibold">{{ formatCount(item.data?.newCustomers) }}</div>
            </div>
          </div>
        </div>
      </div>
    </Card>

    <!-- Charts -->
    <Row :gutter="16" class="mb-4">
      <Col :span="16">
        <Card title="数据趋势分析" :bordered="false">
          <template #extra>
            <div class="trend-range">
              <button
                v-for="days in trendDayOptions"
                :key="days"
                type="button"
                class="trend-range-item"
                :class="{ active: selectedTrendDays === days }"
                @click="handleTrendDaysChange(days)"
              >
                {{ days }}天
              </button>
            </div>
          </template>
          <div ref="trendChartRef" style="height: 350px;"></div>
        </Card>
      </Col>
      <Col :span="8">
        <Card title="客户状态漏斗" :bordered="false">
          <div ref="funnelChartRef" style="height: 350px;"></div>
        </Card>
      </Col>
    </Row>
    
    <!-- Distribution Charts -->
     <Row :gutter="16" class="mb-4">
      <Col :span="8">
        <Card title="地域分布" :bordered="false">
          <div ref="regionChartRef" style="height: 300px;">
             <Empty v-if="!data.distribution.region.length" description="暂无地域数据" />
          </div>
        </Card>
      </Col>
      <Col :span="8">
        <Card title="年龄分布" :bordered="false">
          <div ref="ageChartRef" style="height: 300px;">
             <Empty v-if="!data.distribution.age.length" description="暂无年龄数据" />
          </div>
        </Card>
      </Col>
      <Col :span="8">
        <Card title="性别分布" :bordered="false">
          <div ref="sexChartRef" style="height: 300px;">
             <Empty v-if="!data.distribution.sex.length" description="暂无性别数据" />
          </div>
        </Card>
      </Col>
    </Row>

    <!-- Leaderboard (Management Only) -->
    <Row :gutter="16" v-if="isManagement">
      <Col :span="24">
        <Card title="团队销售风云榜" :bordered="false">
          <Table :columns="leaderboardColumns" :data-source="data.leaderboard" :pagination="false" size="small">
            <template #bodyCell="{ column, record, index }">
               <template v-if="column.dataIndex === 'rank'">
                  <Tag :color="index < 3 ? 'red' : 'default'">{{ record.rank }}</Tag>
               </template>
            </template>
          </Table>
        </Card>
      </Col>
    </Row>
  </div>
  <div v-else class="p-4" v-loading="loading">
     <Empty description="数据加载中..." />
  </div>
</template>

<style scoped>
.trend-range {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.trend-range-item {
  border: 1px solid #e5e7eb;
  border-radius: 999px;
  padding: 2px 10px;
  font-size: 12px;
  color: #475569;
  background: #fff;
  transition: all 0.2s ease;
}

.trend-range-item:hover {
  border-color: #94a3b8;
  color: #1f2937;
}

.trend-range-item.active {
  border-color: #3b82f6;
  color: #1d4ed8;
  background: rgba(59, 130, 246, 0.12);
  font-weight: 600;
}

:global(.dark) .trend-range-item,
:global([data-theme='dark']) .trend-range-item {
  border-color: #334155;
  background: #0f172a;
  color: #cbd5f5;
}

:global(.dark) .trend-range-item.active,
:global([data-theme='dark']) .trend-range-item.active {
  border-color: #60a5fa;
  color: #bfdbfe;
  background: rgba(59, 130, 246, 0.2);
}
</style>
