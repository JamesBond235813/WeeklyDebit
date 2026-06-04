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
  Tag,
} from 'ant-design-vue';
import dayjs from 'dayjs';

import { getRiskHistory, getRiskReport } from '#/api/biz/risk';

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

const listText = (items: any) =>
  Array.isArray(items) && items.length > 0 ? items.join('、') : '--';
const displayValue = (source: any, key: string, fallback = '--') => {
  const value = source?.[key];
  return value === 0 || value === '0' || value ? value : fallback;
};
const money = (value: any) =>
  value === 0 || value ? `¥${Number(value).toLocaleString('zh-CN')}` : '--';

const normalizedReport = computed(() => currentReport.value || {});
const payload = computed(() => {
  const parsed = parsePayload(
    normalizedReport.value.reportJson ?? normalizedReport.value.report_json,
  );
  if (parsed?.panorama || parsed?.probe_c) return parsed;

  const legacyData = parsed?.data ?? parsed ?? {};
  return parsed
    ? {
        report_type: 'XIAOHEBAO_RISK',
        title: '小荷包风险报告',
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
const systemRisk = computed(() => payload.value?.system_risk || {});
const latestOrder = computed(() => payload.value?.latest_order || {});
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
const recentAccess = computed(() => payload.value?.recent_access || []);

const normalizeReportRow = (item: any) => ({
  ...item,
  idCard: item?.idCard ?? item?.id_card ?? item?.id_card_no,
  queryTime: item?.queryTime ?? item?.query_time,
  reportJson: item?.reportJson ?? item?.report_json,
});

const loadHistory = async (page = pagination.value.current || 1) => {
  tableLoading.value = true;
  try {
    const res = normalizeResponse(
      await getRiskHistory({
        keyword: historyForm.keyword,
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
  loadHistory(pag.current || 1);
};

const openReport = (report: any) => {
  currentReport.value = normalizeReportRow(report);
  reportVisible.value = true;
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
          row-key="id"
          size="middle"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'queryTime'">
              {{ formatDateTime(record.queryTime) }}
            </template>
            <template v-if="column.key === 'action'">
              <Button type="link" @click="openReport(record)">查看报告</Button>
            </template>
          </template>
        </Table>
      </Card>

      <Modal
        v-model:open="reportVisible"
        title="小荷包风险报告"
        width="1120px"
        :footer="null"
        style="top: 2vh"
      >
        <div
          v-if="payload"
          class="composite-risk-shell"
          :class="[{ 'risk-report-dark': isDark }]"
        >
          <section class="composite-hero">
            <div>
              <span class="composite-eyebrow">Xiaohebao Risk</span>
              <h3>小荷包风险报告</h3>
              <p>
                合并全景雷达与探针C数据，展示客户申请行为、履约付款行为及外部履约探查结果。
              </p>
            </div>
            <div class="hero-source">
              <span>系统数据</span>
              <span>全景雷达</span>
              <span>探针C</span>
            </div>
          </section>

          <section class="summary-grid">
            <div class="summary-item">
              <label>姓名</label>
              <span>{{ profile.name || normalizedReport.name || '--' }}</span>
            </div>
            <div class="summary-item">
              <label>手机号</label>
              <span>{{ profile.phone || normalizedReport.phone || '--' }}</span>
            </div>
            <div class="summary-item">
              <label>身份证号</label>
              <span>{{
                profile.id_card || normalizedReport.idCard || '--'
              }}</span>
            </div>
            <div class="summary-item">
              <label>报告时间</label>
              <span>{{
                formatDateTime(normalizedReport.queryTime || payload.query_time)
              }}</span>
            </div>
          </section>

          <section class="report-section">
            <div class="section-head">
              <h4>系统风险核查</h4>
              <p>来自本系统黑名单、位置风控、登录拦截与手机号绑定记录。</p>
            </div>
            <div class="risk-tags">
              <Tag :color="systemRisk.blacklist_hit ? 'error' : 'success'">
                黑名单：{{ systemRisk.blacklist_hit ? '命中' : '未命中' }}
              </Tag>
              <Tag
                :color="systemRisk.location_risk_hit ? 'warning' : 'success'"
              >
                风险地址：{{ systemRisk.location_risk_hit ? '命中' : '未命中' }}
              </Tag>
              <Tag
                :color="systemRisk.login_location_blocked ? 'error' : 'success'"
              >
                登录位置拦截：{{
                  systemRisk.login_location_blocked ? '触发' : '未触发'
                }}
              </Tag>
              <Tag color="processing">
                同手机号绑定：{{ systemRisk.same_phone_binding_count || 0 }}
                条
              </Tag>
            </div>
            <table class="report-table">
              <tbody>
                <tr>
                  <th>黑名单原因</th>
                  <td>{{ systemRisk.blacklist_reason || '--' }}</td>
                  <th>风险地址明细</th>
                  <td>{{ systemRisk.location_risk_detail || '--' }}</td>
                </tr>
                <tr>
                  <th>登录拦截原因</th>
                  <td>{{ systemRisk.login_location_reason || '--' }}</td>
                  <th>风险关键词</th>
                  <td>{{ listText(systemRisk.location_risk_keywords) }}</td>
                </tr>
              </tbody>
            </table>
          </section>

          <section class="report-section">
            <div class="section-head">
              <h4>当前订单摘要</h4>
              <p>展示客户最近一笔订单的当前状态。</p>
            </div>
            <table class="report-table">
              <tbody>
                <tr>
                  <th>订单状态</th>
                  <td>{{ latestOrder?.status || '--' }}</td>
                  <th>授信额度</th>
                  <td>{{ money(latestOrder?.credit_limit) }}</td>
                  <th>可用额度</th>
                  <td>{{ money(latestOrder?.available_credit_limit) }}</td>
                </tr>
                <tr>
                  <th>商品名称</th>
                  <td>{{ latestOrder?.product_name || '--' }}</td>
                  <th>应付金额</th>
                  <td>
                    {{
                      money(
                        latestOrder?.payment_amount ||
                          latestOrder?.product_total_price,
                      )
                    }}
                  </td>
                  <th>到期日</th>
                  <td>{{ formatDateTime(latestOrder?.due_date) }}</td>
                </tr>
              </tbody>
            </table>
          </section>

          <section class="report-section">
            <div class="section-head">
              <h4>全景雷达摘要</h4>
              <p>保留原始全景雷达报告，本报告提取关键字段展示。</p>
            </div>
            <table class="report-table">
              <tbody>
                <tr>
                  <th>申请准入分</th>
                  <td>{{ displayValue(applyDetail, 'A22160001') }}</td>
                  <th>信用行为分</th>
                  <td>{{ displayValue(behaviorDetail, 'B22170001') }}</td>
                  <th>最近一次查询时间</th>
                  <td>{{ displayValue(applyDetail, 'A22160007') }}</td>
                </tr>
                <tr>
                  <th>机构总查询次数</th>
                  <td>{{ displayValue(applyDetail, 'A22160006') }}</td>
                  <th>近12个月M0+逾期订单笔数</th>
                  <td>{{ displayValue(behaviorDetail, 'B22170026') }}</td>
                  <th>近12个月累计逾期金额</th>
                  <td>{{ displayValue(behaviorDetail, 'B22170032') }}</td>
                </tr>
              </tbody>
            </table>
          </section>

          <section class="report-section">
            <div class="section-head">
              <h4>探针C摘要</h4>
              <p>展示探针C返回的履约与逾期概况。</p>
            </div>
            <table class="report-table">
              <tbody>
                <tr>
                  <th>结果</th>
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

          <section class="report-section">
            <div class="section-head">
              <h4>最近访问记录</h4>
              <p>按时间由近到远展示最近访问、IP和经纬度解析结果。</p>
            </div>
            <Table
              :columns="[
                {
                  title: '时间',
                  dataIndex: 'created_at',
                  key: 'created_at',
                  width: 160,
                },
                { title: '操作', dataIndex: 'title', key: 'title', width: 160 },
                { title: 'IP', dataIndex: 'ip', key: 'ip', width: 130 },
                {
                  title: 'IP地址',
                  dataIndex: 'ip_address',
                  key: 'ip_address',
                  width: 180,
                },
                {
                  title: '经纬度',
                  dataIndex: 'lon_lat',
                  key: 'lon_lat',
                  width: 150,
                },
                {
                  title: '经纬度地址',
                  dataIndex: 'lon_lat_address',
                  key: 'lon_lat_address',
                  width: 200,
                },
              ]"
              :data-source="recentAccess"
              :pagination="false"
              row-key="id"
              size="small"
              bordered
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'created_at'">
                  {{ formatDateTime(record.created_at) }}
                </template>
              </template>
            </Table>
          </section>
        </div>
        <Empty v-else description="暂无小荷包风险报告数据" />
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

.composite-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  padding: 20px 22px;
  border-radius: 8px;
  color: #ffffff;
  background: linear-gradient(135deg, #163b76 0%, #2871b8 54%, #45a28d 100%);
}

.composite-eyebrow {
  display: inline-flex;
  padding: 5px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  font-size: 12px;
}

.composite-hero h3 {
  margin: 14px 0 8px;
  font-size: 26px;
  line-height: 1.15;
}

.composite-hero p {
  margin: 0;
  color: rgba(255, 255, 255, 0.86);
  line-height: 1.6;
}

.hero-source {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
  min-width: 220px;
}

.hero-source span {
  padding: 8px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.18);
  font-size: 12px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.summary-item {
  padding: 14px 16px;
  border-radius: 8px;
  background: #f7faff;
  border: 1px solid #e6edf8;
}

.summary-item label {
  display: block;
  margin-bottom: 7px;
  font-size: 12px;
  color: #73849b;
}

.summary-item span {
  display: block;
  color: #25384f;
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

.risk-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.report-table {
  width: 100%;
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

.risk-report-dark .summary-item,
.risk-report-dark .report-section,
.risk-report-dark .report-table {
  background: #0b1220;
  border-color: #1f2a44;
}

.risk-report-dark .summary-item label,
.risk-report-dark .section-head p,
.risk-report-dark .report-table th {
  color: #94a3b8;
}

.risk-report-dark .summary-item span,
.risk-report-dark .section-head h4,
.risk-report-dark .report-table td {
  color: #e2e8f0;
}

.risk-report-dark .report-table th {
  background: #0f172a;
}
</style>
