<script lang="ts" setup>
import type { TablePaginationConfig } from 'ant-design-vue';

import { computed, onMounted, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { usePreferences } from '@vben/preferences';

import {
  Button,
  Card,
  Empty,
  Form,
  Input,
  message,
  Modal,
  Table,
} from 'ant-design-vue';
import dayjs from 'dayjs';

import { getRiskHistory, getRiskReport, searchCustomers } from '#/api/biz/risk';

const { isDark } = usePreferences();

const queryForm = reactive({
  name: '',
  idCard: '',
  phone: '',
});

const historyForm = reactive({
  keyword: '',
});

const tableLoading = ref(false);
const reportLoading = ref(false);
const reportVisible = ref(false);
const historyRows = ref<any[]>([]);
const currentReport = ref<any>(null);

const pagination = ref<TablePaginationConfig>({
  current: 1,
  pageSize: 20,
  showSizeChanger: true,
  total: 0,
});

const historyColumns = [
  { title: '客户姓名', dataIndex: 'name', key: 'name', width: 130 },
  { title: '手机号', dataIndex: 'phone', key: 'phone', width: 150 },
  { title: '身份证号', dataIndex: 'idCard', key: 'idCard', width: 210 },
  { title: '查询时间', dataIndex: 'queryTime', key: 'queryTime', width: 180 },
  { title: '报告查询', key: 'action', width: 130, fixed: 'right' as const },
];

const behaviorTimeRows = [
  {
    label: '近1个月',
    keys: [
      'B22170016',
      'B22170002',
      'B22170007',
      'B22170040',
      'B22170045',
      'B22170035',
    ],
  },
  {
    label: '近3个月',
    keys: [
      'B22170017',
      'B22170003',
      'B22170008',
      'B22170041',
      'B22170046',
      'B22170036',
    ],
  },
  {
    label: '近6个月',
    keys: [
      'B22170018',
      'B22170004',
      'B22170009',
      'B22170042',
      'B22170047',
      'B22170037',
    ],
  },
  {
    label: '近12个月',
    keys: [
      'B22170019',
      'B22170005',
      'B22170010',
      'B22170043',
      'B22170048',
      'B22170038',
    ],
  },
  {
    label: '近24个月',
    keys: [
      'B22170020',
      'B22170006',
      'B22170011',
      'B22170044',
      'B22170049',
      'B22170039',
    ],
  },
];

const normalizeResponse = (res: any) => res?.data ?? res;

const parsePayload = (value: any) => {
  if (!value) return null;
  if (typeof value === 'object') return value;
  try {
    return JSON.parse(value);
  } catch {
    return null;
  }
};

const formatDateTime = (value: any) => {
  if (!value) return '--';
  const parsed = dayjs(value);
  return parsed.isValid()
    ? parsed.format('YYYY-MM-DD HH:mm:ss')
    : String(value);
};

const displayValue = (source: any, key: string, fallback = '--') => {
  const value = source?.[key];
  return value === 0 || value === '0' || value ? value : fallback;
};

const normalizedReport = computed(() => currentReport.value || {});
const payload = computed(() => {
  const parsed = parsePayload(
    normalizedReport.value.reportJson ?? normalizedReport.value.report_json,
  );
  if (parsed?.panorama || parsed?.probe_c) return parsed;

  const legacyData = parsed?.data ?? parsed ?? {};
  return parsed
    ? {
        report_type: 'RONGSHUHUA_RISK',
        title: '榕树花风控报告',
        query_time:
          normalizedReport.value.queryTime ?? normalizedReport.value.query_time,
        user_profile: {
          name: normalizedReport.value.name,
          phone: normalizedReport.value.phone,
          id_card:
            normalizedReport.value.idCard ?? normalizedReport.value.id_card,
        },
        system_risk: {},
        latest_order: {},
        recent_access: [],
        panorama: { source: 'PANORAMA', payload: legacyData },
        probe_c: {},
      }
    : null;
});
const profile = computed(() => payload.value?.user_profile || {});
const panoramaPayload = computed(() => payload.value?.panorama?.payload || {});
const panoramaData = computed(() => panoramaPayload.value?.data || {});
const applyDetail = computed(
  () =>
    panoramaData.value?.apply_report_detail ||
    panoramaPayload.value?.apply_report_detail ||
    {},
);
const behaviorDetail = computed(
  () =>
    panoramaData.value?.behavior_report_detail ||
    panoramaPayload.value?.behavior_report_detail ||
    {},
);
const probeC = computed(() => payload.value?.probe_c || {});
const probeData = computed(() => probeC.value?.payload?.data || {});

const normalizeReportRow = (item: any) => ({
  ...item,
  idCard: item?.idCard ?? item?.id_card ?? item?.id_card_no,
  queryTime: item?.queryTime ?? item?.query_time,
  reportJson: item?.reportJson ?? item?.report_json,
  rowType: item?.rowType ?? 'history',
});

const normalizeCustomerRow = (item: any) => ({
  ...item,
  idCard: item?.idCard ?? item?.id_card ?? item?.id_card_no,
  phone: item?.phone ?? item?.mobile,
  rowType: 'customer',
});

const getIdentityKey = (item: any) => {
  const idCard = item?.idCard ?? item?.id_card ?? item?.id_card_no ?? '';
  const phone = item?.phone ?? item?.mobile ?? '';
  const name = item?.name ?? '';
  return [String(idCard).trim(), String(phone).trim(), String(name).trim()]
    .filter(Boolean)
    .join('|');
};

const mergeHistoryAndCustomers = (historyItems: any[], customerItems: any[]) => {
  const rows: any[] = [];
  const seen = new Set<string>();

  for (const item of historyItems.map((row) => normalizeReportRow(row))) {
    const key = getIdentityKey(item) || `history-${item.id}`;
    if (!seen.has(key)) {
      rows.push(item);
      seen.add(key);
    }
  }

  for (const item of customerItems.map((row) => normalizeCustomerRow(row))) {
    const key = getIdentityKey(item) || `customer-${item.id}`;
    if (!seen.has(key)) {
      rows.push(item);
      seen.add(key);
    }
  }

  return rows;
};

const getRowKey = (record: any) =>
  `${record.rowType || 'history'}-${record.id || getIdentityKey(record)}`;

const loadHistory = async (page = pagination.value.current || 1) => {
  tableLoading.value = true;
  try {
    const keyword = historyForm.keyword?.trim();
    if (keyword) {
      const [historyRes, customerRes] = await Promise.all([
        getRiskHistory({
          keyword,
          page: 1,
          pageSize: 100,
        }),
        searchCustomers(keyword),
      ]);
      const historyData = normalizeResponse(historyRes);
      const customerData = normalizeResponse(customerRes);
      historyRows.value = mergeHistoryAndCustomers(
        historyData?.records || [],
        Array.isArray(customerData) ? customerData : [],
      );
      pagination.value = {
        ...pagination.value,
        current: 1,
        total: historyRows.value.length,
      };
      return;
    }

    const res = normalizeResponse(
      await getRiskHistory({
        page,
        pageSize: pagination.value.pageSize || 20,
      }),
    );
    historyRows.value = (res?.records || []).map((item: any) =>
      normalizeReportRow(item),
    );
    pagination.value = {
      ...pagination.value,
      current: res?.current || page,
      pageSize: res?.size || pagination.value.pageSize || 20,
      total: res?.total || 0,
    };
  } finally {
    tableLoading.value = false;
  }
};

const handleTableChange = (pag: TablePaginationConfig) => {
  pagination.value = {
    ...pagination.value,
    current: pag.current,
    pageSize: pag.pageSize,
  };
  if (historyForm.keyword?.trim()) {
    return;
  }
  loadHistory(pag.current || 1);
};

const openReport = (report: any) => {
  currentReport.value = normalizeReportRow(report);
  reportVisible.value = true;
};

const queryReportFromRow = async (record: any) => {
  reportLoading.value = true;
  try {
    const res = normalizeResponse(
      await getRiskReport({
        name: record.name?.trim(),
        idCard: record.idCard?.trim(),
        phone: record.phone?.trim(),
      }),
    );
    openReport(res);
    await loadHistory(1);
  } catch (error: any) {
    message.error(error?.message || '获取风控报告失败');
  } finally {
    reportLoading.value = false;
  }
};

const submitSingleQuery = async () => {
  if (!queryForm.name && !queryForm.idCard && !queryForm.phone) {
    message.warning('请至少填写姓名、身份证号、手机号中的一项');
    return;
  }
  reportLoading.value = true;
  try {
    const res = normalizeResponse(
      await getRiskReport({
        name: queryForm.name?.trim(),
        idCard: queryForm.idCard?.trim(),
        phone: queryForm.phone?.trim(),
      }),
    );
    openReport(res);
    historyForm.keyword = '';
    await loadHistory(1);
  } catch (error: any) {
    message.error(error?.message || '获取风控报告失败');
  } finally {
    reportLoading.value = false;
  }
};

const resetSingleQuery = () => {
  queryForm.name = '';
  queryForm.idCard = '';
  queryForm.phone = '';
};

const searchHistory = () => {
  pagination.value.current = 1;
  loadHistory(1);
};

onMounted(() => {
  loadHistory(1);
});
</script>

<template>
  <Page title="风控报告">
    <div class="risk-page">
      <Card title="风控报告单查" :bordered="false">
        <p class="risk-tip">
          三要素均可选填；系统会优先匹配已有客户，无法唯一匹配时需补齐三要素。
        </p>
        <Form layout="inline" class="single-form">
          <Form.Item label="姓名">
            <Input
              v-model:value="queryForm.name"
              allow-clear
              placeholder="请输入姓名"
            />
          </Form.Item>
          <Form.Item label="身份证号">
            <Input
              v-model:value="queryForm.idCard"
              allow-clear
              placeholder="请输入身份证号"
            />
          </Form.Item>
          <Form.Item label="手机号">
            <Input
              v-model:value="queryForm.phone"
              allow-clear
              placeholder="请输入手机号"
              @press-enter="submitSingleQuery"
            />
          </Form.Item>
          <Form.Item>
            <Button
              type="primary"
              :loading="reportLoading"
              @click="submitSingleQuery"
            >
              查看报告
            </Button>
          </Form.Item>
          <Form.Item>
            <Button @click="resetSingleQuery">清空</Button>
          </Form.Item>
        </Form>
      </Card>

      <Card title="查询历史清单" :bordered="false">
        <template #extra>
          <div class="history-search">
            <Input
              v-model:value="historyForm.keyword"
              allow-clear
              placeholder="姓名 / 手机号 / 身份证号"
              style="width: 260px"
              @press-enter="searchHistory"
            />
            <Button
              type="primary"
              :loading="tableLoading"
              @click="searchHistory"
            >
              查询
            </Button>
          </div>
        </template>
        <Table
          class="risk-history-table"
          :columns="historyColumns"
          :data-source="historyRows"
          :loading="tableLoading"
          :pagination="pagination"
          :row-key="getRowKey"
          size="middle"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'queryTime'">
              {{ formatDateTime(record.queryTime) }}
            </template>
            <template v-if="column.key === 'action'">
              <Button
                type="link"
                :loading="reportLoading"
                @click="queryReportFromRow(record)"
              >
                查询
              </Button>
            </template>
          </template>
        </Table>
      </Card>

      <Modal
        v-model:open="reportVisible"
        title="榕树花风控报告"
        width="1120px"
        :footer="null"
        style="top: 2vh"
      >
        <div
          v-if="payload"
          class="composite-risk-shell"
          :class="[{ 'risk-report-dark': isDark }]"
        >
          <section class="risk-hero">
            <div class="risk-hero-top">
              <div class="risk-hero-main">
                <span class="risk-eyebrow">RONGSHUHUA RISK</span>
                <h3>榕树花风控报告</h3>
                <p>
                  合并全景雷达与探针C数据，展示客户申请行为、履约付款行为及外部履约探查结果。
                </p>
              </div>
              <div class="risk-hero-side">
                <div class="hero-metric">
                  <span>申请准入分</span>
                  <strong>{{ displayValue(applyDetail, 'A22160001') }}</strong>
                </div>
                <div class="hero-metric">
                  <span>信用行为分</span>
                  <strong>{{ displayValue(behaviorDetail, 'B22170001') }}</strong>
                </div>
              </div>
            </div>
            <div class="hero-profile-grid">
              <div class="hero-profile-item">
                <label>客户姓名</label>
                <span>{{ profile.name || normalizedReport.name || '--' }}</span>
              </div>
              <div class="hero-profile-item">
                <label>身份证号</label>
                <span>{{
                  profile.id_card || normalizedReport.idCard || '--'
                }}</span>
              </div>
              <div class="hero-profile-item">
                <label>手机号</label>
                <span>{{ profile.phone || normalizedReport.phone || '--' }}</span>
              </div>
              <div class="hero-profile-item">
                <label>报告时间</label>
                <span>{{
                  formatDateTime(normalizedReport.queryTime || payload.query_time)
                }}</span>
              </div>
            </div>
          </section>

          <section class="report-section">
            <div class="section-head">
              <div>
                <h4>申请行为详情</h4>
                <p>
                  申请准入置信度：{{
                    displayValue(applyDetail, 'A22160002')
                  }}
                </p>
              </div>
            </div>
            <table class="report-table">
              <tbody>
                <tr>
                  <th>申请准入分</th>
                  <td>{{ displayValue(applyDetail, 'A22160001') }}</td>
                  <th>近1个月机构总查询笔数</th>
                  <td>{{ displayValue(applyDetail, 'A22160008') }}</td>
                  <th>申请命中机构数</th>
                  <td>{{ displayValue(applyDetail, 'A22160003') }}</td>
                </tr>
                <tr>
                  <th>机构总查询次数</th>
                  <td>{{ displayValue(applyDetail, 'A22160006') }}</td>
                  <th>近3个月机构总查询笔数</th>
                  <td>{{ displayValue(applyDetail, 'A22160009') }}</td>
                  <th>申请命中消金类机构数</th>
                  <td>{{ displayValue(applyDetail, 'A22160004') }}</td>
                </tr>
                <tr>
                  <th>最近一次查询时间</th>
                  <td>{{ displayValue(applyDetail, 'A22160007') }}</td>
                  <th>近6个月机构总查询笔数</th>
                  <td>{{ displayValue(applyDetail, 'A22160010') }}</td>
                  <th>申请命中网络信用服务类机构数</th>
                  <td>{{ displayValue(applyDetail, 'A22160005') }}</td>
                </tr>
              </tbody>
            </table>
          </section>

          <section class="report-section">
            <div class="section-head">
              <div>
                <h4>履约付款详情</h4>
                <p>
                  信用行为置信度：{{
                    displayValue(behaviorDetail, 'B22170051')
                  }}
                </p>
              </div>
            </div>

            <table class="report-table">
              <tbody>
                <tr>
                  <th>信用行为分</th>
                  <td>{{ displayValue(behaviorDetail, 'B22170001') }}</td>
                  <th>最近一次服务发放时间</th>
                  <td>{{ displayValue(behaviorDetail, 'B22170054') }}</td>
                  <th>已结清订单数</th>
                  <td>{{ displayValue(behaviorDetail, 'B22170052') }}</td>
                </tr>
                <tr>
                  <th>信用服务时长</th>
                  <td>{{ displayValue(behaviorDetail, 'B22170053') }}</td>
                  <th>最近一次履约距今天数</th>
                  <td>{{ displayValue(behaviorDetail, 'B22170050') }}</td>
                  <th>正常付款订单占总订单数比例</th>
                  <td class="emphasis danger">
                    {{ displayValue(behaviorDetail, 'B22170034') }}
                  </td>
                </tr>
              </tbody>
            </table>

            <table class="report-table">
              <thead>
                <tr>
                  <th>行为时间</th>
                  <th>机构数</th>
                  <th>订单笔数</th>
                  <th>订单总金额</th>
                  <th>履约订单总金额</th>
                  <th>履约订单数</th>
                  <th>失败扣款数</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in behaviorTimeRows" :key="row.label">
                  <th>{{ row.label }}</th>
                  <td>{{ displayValue(behaviorDetail, row.keys[0]) }}</td>
                  <td>{{ displayValue(behaviorDetail, row.keys[1]) }}</td>
                  <td>{{ displayValue(behaviorDetail, row.keys[2]) }}</td>
                  <td class="emphasis success">
                    {{ displayValue(behaviorDetail, row.keys[3]) }}
                  </td>
                  <td class="emphasis success">
                    {{ displayValue(behaviorDetail, row.keys[4]) }}
                  </td>
                  <td class="emphasis danger">
                    {{ displayValue(behaviorDetail, row.keys[5]) }}
                  </td>
                </tr>
              </tbody>
            </table>

            <div class="grid-block">
              <table class="report-table">
                <thead>
                  <tr>
                    <th>近12个月订单金额</th>
                    <th>1K及以下</th>
                    <th>1K-3K</th>
                    <th>3K-10K</th>
                    <th>1W以上</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <th>订单笔数</th>
                    <td>{{ displayValue(behaviorDetail, 'B22170012', '0') }}</td>
                    <td>{{ displayValue(behaviorDetail, 'B22170013', '0') }}</td>
                    <td>{{ displayValue(behaviorDetail, 'B22170014', '0') }}</td>
                    <td>{{ displayValue(behaviorDetail, 'B22170015', '0') }}</td>
                  </tr>
                </tbody>
              </table>

              <table class="report-table">
                <thead>
                  <tr>
                    <th>近12个月消费信用类机构数</th>
                    <th>近24个月消费信用类机构数</th>
                    <th>近12个月线上信用类机构数</th>
                    <th>近24个月线上信用类机构数</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td>{{ displayValue(behaviorDetail, 'B22170021', '0') }}</td>
                    <td>{{ displayValue(behaviorDetail, 'B22170022', '0') }}</td>
                    <td>{{ displayValue(behaviorDetail, 'B22170023', '0') }}</td>
                    <td>{{ displayValue(behaviorDetail, 'B22170024', '0') }}</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <table class="report-table">
              <tbody>
                <tr>
                  <th>近6个月M0+逾期订单笔数</th>
                  <td>{{ displayValue(behaviorDetail, 'B22170025') }}</td>
                  <th>近6个月M1+逾期订单笔数</th>
                  <td>{{ displayValue(behaviorDetail, 'B22170028') }}</td>
                  <th>近6个月累计逾期金额</th>
                  <td>{{ displayValue(behaviorDetail, 'B22170031') }}</td>
                </tr>
                <tr>
                  <th>近12个月M0+逾期订单笔数</th>
                  <td class="emphasis danger">
                    {{ displayValue(behaviorDetail, 'B22170026') }}
                  </td>
                  <th>近12个月M1+逾期订单笔数</th>
                  <td class="emphasis danger">
                    {{ displayValue(behaviorDetail, 'B22170029') }}
                  </td>
                  <th>近12个月累计逾期金额</th>
                  <td class="emphasis danger">
                    {{ displayValue(behaviorDetail, 'B22170032') }}
                  </td>
                </tr>
                <tr>
                  <th>近24个月M0+逾期订单笔数</th>
                  <td>{{ displayValue(behaviorDetail, 'B22170027') }}</td>
                  <th>近24个月M1+逾期订单笔数</th>
                  <td>{{ displayValue(behaviorDetail, 'B22170030') }}</td>
                  <th>近24个月累计逾期金额</th>
                  <td>{{ displayValue(behaviorDetail, 'B22170033') }}</td>
                </tr>
              </tbody>
            </table>
          </section>

          <section class="report-section">
            <div class="section-head">
              <div>
                <h4>探针C信息</h4>
                <p>展示探针C返回的履约与逾期概况。</p>
              </div>
            </div>
            <table class="report-table">
              <tbody>
                <tr>
                  <th>探查结果</th>
                  <td>{{ probeC.result_label || '--' }}</td>
                  <th>最大逾期金额</th>
                  <td>{{ probeData.max_overdue_amt || '--' }}</td>
                  <th>最长逾期天数</th>
                  <td>{{ probeData.max_overdue_days || '--' }}</td>
                </tr>
                <tr>
                  <th>最近逾期时间</th>
                  <td>{{ probeData.latest_overdue_time || '--' }}</td>
                  <th>当前逾期机构数</th>
                  <td>{{ probeData.currently_overdue || '--' }}</td>
                  <th>当前履约机构数</th>
                  <td>{{ probeData.currently_performance || '--' }}</td>
                </tr>
                <tr>
                  <th>异常还款机构数</th>
                  <td>{{ probeData.acc_exc || '--' }}</td>
                  <th>睡眠机构数</th>
                  <td>{{ probeData.acc_sleep || '--' }}</td>
                  <th>报告来源</th>
                  <td>{{ probeC.source || '--' }}</td>
                </tr>
                <tr>
                  <th>最大履约金额</th>
                  <td>{{ probeData.max_performance_amt || '--' }}</td>
                  <th>最近履约时间</th>
                  <td>{{ probeData.latest_performance_time || '--' }}</td>
                  <th>履约笔数</th>
                  <td>{{ probeData.count_performance || '--' }}</td>
                </tr>
              </tbody>
            </table>
          </section>
        </div>
        <Empty v-else description="暂无榕树花风控报告数据" />
      </Modal>
    </div>
  </Page>
</template>

<style scoped>
.risk-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
}

.risk-tip {
  margin: 0 0 16px;
  color: #6b7280;
  font-size: 13px;
}

.single-form {
  row-gap: 12px;
}

.single-form :deep(.ant-input) {
  width: 220px;
}

.history-search {
  display: flex;
  gap: 8px;
}

.composite-risk-shell {
  max-height: calc(100vh - 140px);
  overflow-y: auto;
  padding-right: 4px;
}

.risk-hero {
  display: grid;
  gap: 18px;
  padding: 20px 22px;
  border-radius: 8px;
  color: #ffffff;
  background: linear-gradient(135deg, #163b76 0%, #2871b8 54%, #45a28d 100%);
}

.risk-hero-top {
  display: flex;
  justify-content: space-between;
  gap: 18px;
}

.risk-hero-main {
  flex: 1;
  min-width: 0;
}

.risk-eyebrow {
  display: inline-flex;
  padding: 5px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  font-size: 12px;
  text-transform: uppercase;
}

.risk-hero-main h3 {
  margin: 14px 0 8px;
  font-size: 26px;
  line-height: 1.15;
}

.risk-hero-main p {
  margin: 0;
  color: rgba(255, 255, 255, 0.86);
  line-height: 1.6;
}

.risk-hero-side {
  display: grid;
  grid-template-columns: repeat(2, minmax(120px, 1fr));
  gap: 12px;
  min-width: 280px;
}

.hero-metric {
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.14);
}

.hero-metric span {
  display: block;
  color: rgba(255, 255, 255, 0.74);
  font-size: 12px;
}

.hero-metric strong {
  display: block;
  margin-top: 10px;
  font-size: 28px;
  line-height: 1;
}

.hero-profile-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.hero-profile-item {
  min-width: 0;
  padding: 12px 14px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.14);
}

.hero-profile-item label {
  display: block;
  margin-bottom: 7px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 12px;
}

.hero-profile-item span {
  display: block;
  color: #ffffff;
  font-weight: 600;
  word-break: break-all;
}

.report-section {
  margin-top: 18px;
  padding: 18px;
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid #e8eef8;
}

.section-head {
  margin-bottom: 12px;
}

.section-head h4 {
  margin: 0;
  color: #1f3c63;
  font-size: 17px;
}

.section-head p {
  margin: 6px 0 0;
  color: #6d7f96;
  font-size: 13px;
}

.report-table {
  width: 100%;
  margin-top: 14px;
  border-collapse: collapse;
  table-layout: fixed;
}

.report-table th,
.report-table td {
  padding: 10px;
  border: 1px solid #e7edf6;
  font-size: 12px;
  text-align: center;
  word-break: break-word;
}

.report-table th {
  background: #f8fbff;
  color: #59708f;
  font-weight: 500;
}

.report-table td {
  color: #2d4059;
}

.grid-block {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
  margin-top: 14px;
}

.emphasis.success {
  color: #14905f;
  font-weight: 700;
}

.emphasis.danger {
  color: #d84b47;
  font-weight: 700;
}

.risk-report-dark .report-section,
.risk-report-dark .report-table {
  background: #0b1220;
  border-color: #1f2a44;
}

.risk-report-dark .section-head p,
.risk-report-dark .report-table th {
  color: #94a3b8;
}

.risk-report-dark .section-head h4,
.risk-report-dark .report-table td {
  color: #e2e8f0;
}

.risk-report-dark .report-table th {
  background: #0f172a;
}

@media (max-width: 1200px) {
  .risk-hero-top {
    flex-direction: column;
  }

  .risk-hero-side {
    min-width: 0;
  }

  .hero-profile-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .hero-profile-grid,
  .risk-hero-side {
    grid-template-columns: 1fr;
  }
}
</style>
