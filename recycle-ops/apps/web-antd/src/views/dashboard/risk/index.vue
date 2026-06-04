<script lang="ts" setup>
import {
  Button,
  Card,
  Input,
  Modal,
  Table,
  Tabs,
  TabPane,
  Empty,
  message,
  Form
} from 'ant-design-vue';
import { ref, reactive } from 'vue';
import { Page } from '@vben/common-ui';
import { usePreferences } from '@vben/preferences';

import { getRiskReport, searchCustomers } from '#/api/biz/risk';

const columns = [
  {
    title: '姓名',
    dataIndex: 'name',
    key: 'name',
  },
  {
    title: '身份证号',
    dataIndex: 'idCard',
    key: 'idCard',
  },
  {
    title: '手机号',
    dataIndex: 'phone',
    key: 'phone',
  },
  {
    title: '报告时间',
    dataIndex: 'queryTime',
    key: 'queryTime',
  },
  {
    title: '操作',
    key: 'action',
  },
];

const behaviorTimeRows = {
  '近1个月': ['B22170016', 'B22170002', 'B22170007', 'B22170040', 'B22170045', 'B22170035'],
  '近3个月': ['B22170017', 'B22170003', 'B22170008', 'B22170041', 'B22170046', 'B22170036'],
  '近6个月': ['B22170018', 'B22170004', 'B22170009', 'B22170042', 'B22170047', 'B22170037'],
  '近12个月': ['B22170019', 'B22170005', 'B22170010', 'B22170043', 'B22170048', 'B22170038'],
  '近24个月': ['B22170020', 'B22170006', 'B22170011', 'B22170044', 'B22170049', 'B22170039']
};

const searchForm = reactive({
  keyword: '',
});

const loading = ref(false);
const dataSource = ref([]);
const reportVisible = ref(false);
const reportLoading = ref(false);
const currentReport = ref<any>(null);
const { isDark } = usePreferences();

const handleSearch = async () => {
  loading.value = true;
  try {
    const res = await searchCustomers(searchForm.keyword);
    const list = Array.isArray(res) ? res : (res as any)?.data ?? [];
    dataSource.value = list.map((item: any) => ({
      ...item,
      name: item?.name ?? item?.customerName ?? '',
      idCard: item?.idCard ?? item?.idCardNo ?? '',
      phone: item?.phone ?? item?.customerMobile ?? '',
    }));
  } finally {
    loading.value = false;
  }
};

const viewReport = async (record: any) => {
  reportLoading.value = true;
  currentReport.value = null; // Reset
  try {
    const res = await getRiskReport({
      name: record.name,
      idCard: record.idCard,
      phone: record.phone,
    });

    console.log('Report Raw:', res);

    const reportPayload = (res as any)?.data ?? res;
    let reportDetail =
      reportPayload?.reportJson ??
      reportPayload?.report_json ??
      reportPayload?.data?.reportJson ??
      reportPayload?.data?.report_json ??
      reportPayload?.data ??
      reportPayload;

    if (typeof reportDetail === 'string') {
      try {
        reportDetail = JSON.parse(reportDetail);
      } catch (error) {
        console.warn('Failed to parse report detail JSON string', error);
      }
    }

    const reportData = reportDetail?.data ?? {};
    const applyReportDetail =
      reportDetail?.apply_report_detail ?? reportData?.apply_report_detail;
    const behaviorReportDetail =
      reportDetail?.behavior_report_detail ?? reportData?.behavior_report_detail;
    const currentReportDetail =
      reportDetail?.current_report_detail ?? reportData?.current_report_detail;
    const normalizedDetail = {
      ...reportDetail,
      ...reportData,
      apply_report_detail: applyReportDetail ?? {},
      behavior_report_detail: behaviorReportDetail ?? {},
      current_report_detail: currentReportDetail ?? {},
    };

    console.log('Report Data:', normalizedDetail);
    
    // Flatten the structure for easier access if unified access is preferred
    // But keeping original structure { apply_report_detail: {...} } is fine
    // Just ensure we attach the basic info for display
    currentReport.value = {
      ...normalizedDetail,
      name: record.name,
      idCard: record.idCard,
      phone: record.phone,
      queryTime: reportPayload?.queryTime ?? reportDetail?.queryTime
    };
    
    reportVisible.value = true;
  } catch (error) {
    console.error(error);
    message.error('获取风控报告失败');
  } finally {
    reportLoading.value = false;
  }
}

</script>

<template>
  <Page title="风控报告查询">
    <template #extra>
      <Form layout="inline" class="risk-search">
        <Form.Item label="关键词">
          <Input
            v-model:value="searchForm.keyword"
            placeholder="输入姓名/手机号/身份证"
            style="width: 260px"
            @pressEnter="handleSearch"
          />
        </Form.Item>
        <Form.Item>
          <Button type="primary" :loading="loading" @click="handleSearch">查询</Button>
        </Form.Item>
      </Form>
    </template>
    <div class="p-4">
      <Card title="查询结果" :bordered="false">
        <Table :dataSource="dataSource" :columns="columns" :loading="loading" rowKey="id">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'action'">
              <Button type="link" @click="viewReport(record)">获取风控报告</Button>
            </template>
          </template>
        </Table>
      </Card>

      <Modal
        v-model:open="reportVisible"
        title="全景雷达风控报告"
        width="1100px"
        :footer="null"
        style="top: 20px"
      >
        <div
          v-if="currentReport && currentReport.apply_report_detail"
          :class="['risk-report', { 'risk-report-dark': isDark }]"
        >
          <!-- 头部基础信息 -->
          <div class="mb-6 p-4 bg-gray-50 rounded">
            <h3 class="text-lg font-bold text-blue-800 mb-2">查询基础信息</h3>
            <div class="text-gray-600 flex flex-nowrap items-center gap-6">
               <span>名字: {{ currentReport.name }}</span>
               <span>身份证号: {{ currentReport.idCard }}</span>
               <span>查询手机号: {{ currentReport.phone }}</span>
            </div>
          </div>

          <Tabs default-active-key="1">
            <TabPane key="1" tab="全景雷达">
              
              <!-- 1. 申请行为详情 -->
              <div class="section mb-8">
                 <div class="flex items-center mb-4">
                    <h4 class="text-blue-500 font-medium text-base mr-2">申请行为详情</h4>
                    <span class="text-gray-500 text-sm">(申请准入置信度: <span class="text-green-600 font-bold">{{ currentReport.apply_report_detail['A22160002'] || '-' }}</span>)</span>
                 </div>
                 
                 <table class="report-table w-full border-collapse border border-gray-200 text-sm text-center">
                   <tbody>
                      <tr>
                        <td class="bg-gray-50 p-3 border border-gray-200">申请准入分</td>
                        <td class="p-3 border border-gray-200">{{ currentReport.apply_report_detail['A22160001'] || '-' }}</td>
                        <td class="bg-gray-50 p-3 border border-gray-200">近1个月机构总查询笔数</td>
                        <td class="p-3 border border-gray-200">{{ currentReport.apply_report_detail['A22160008'] || '-' }}</td>
                        <td class="bg-gray-50 p-3 border border-gray-200">申请命中机构数</td>
                        <td class="p-3 border border-gray-200">{{ currentReport.apply_report_detail['A22160003'] || '-' }}</td>
                      </tr>
                      <tr>
                        <td class="bg-gray-50 p-3 border border-gray-200">机构总查询次数</td>
                        <td class="p-3 border border-gray-200">{{ currentReport.apply_report_detail['A22160006'] || '-' }}</td>
                        <td class="bg-gray-50 p-3 border border-gray-200">近3个月机构总查询笔数</td>
                        <td class="p-3 border border-gray-200">{{ currentReport.apply_report_detail['A22160009'] || '-' }}</td>
                         <td class="bg-gray-50 p-3 border border-gray-200">申请命中消金类机构数</td>
                        <td class="p-3 border border-gray-200">{{ currentReport.apply_report_detail['A22160004'] || '-' }}</td>
                      </tr>
                       <tr>
                        <td class="bg-gray-50 p-3 border border-gray-200">最近一次查询时间</td>
                        <td class="p-3 border border-gray-200">{{ currentReport.apply_report_detail['A22160007'] || '-' }}</td>
                        <td class="bg-gray-50 p-3 border border-gray-200">近6个月机构总查询笔数</td>
                        <td class="p-3 border border-gray-200">{{ currentReport.apply_report_detail['A22160010'] || '-' }}</td>
                         <td class="bg-gray-50 p-3 border border-gray-200">申请命中网络贷款类机构数</td>
                        <td class="p-3 border border-gray-200">{{ currentReport.apply_report_detail['A22160005'] || '-' }}</td>
                      </tr>
                   </tbody>
                 </table>
              </div>

              <!-- 2. 放款还款详情 -->
              <div class="section mb-8">
                 <div class="flex items-center mb-4">
                    <h4 class="text-blue-500 font-medium text-base mr-2">放款还款详情</h4>
                    <span class="text-gray-500 text-sm">(贷款行为置信度: <span class="text-green-600 font-bold">{{ currentReport.behavior_report_detail['B22170051'] || '-' }}</span>)</span>
                 </div>
                 
                 <table class="report-table w-full border-collapse border border-gray-200 text-sm text-center mb-6">
                   <tbody>
                      <tr>
                        <td class="bg-gray-50 p-3 border border-gray-200">贷款行为分</td>
                        <td class="p-3 border border-gray-200">{{ currentReport.behavior_report_detail['B22170001'] || '-' }}</td>
                        <td class="bg-gray-50 p-3 border border-gray-200">最近一次放款时间</td>
                        <td class="p-3 border border-gray-200">{{ currentReport.behavior_report_detail['B22170054'] || '-' }}</td>
                        <td class="bg-gray-50 p-3 border border-gray-200">贷款已结清订单数</td>
                        <td class="p-3 border border-gray-200">{{ currentReport.behavior_report_detail['B22170052'] || '-' }}</td>
                      </tr>
                      <tr>
                        <td class="bg-gray-50 p-3 border border-gray-200">信用贷款时长</td>
                        <td class="p-3 border border-gray-200">{{ currentReport.behavior_report_detail['B22170053'] || '-' }}</td>
                        <td class="bg-gray-50 p-3 border border-gray-200">最近一次履约据今天数</td>
                        <td class="p-3 border border-gray-200">{{ currentReport.behavior_report_detail['B22170050'] || '-' }}</td>
                         <td class="bg-gray-50 p-3 border border-gray-200">正常还款订单占贷款总订单数比例</td>
                        <td class="p-3 border border-gray-200 text-red-500 font-bold">{{ currentReport.behavior_report_detail['B22170034'] || '-' }}</td>
                      </tr>
                   </tbody>
                 </table>

                 <!-- 行为时间表 (Behavior Time Table) -->
                 <table class="report-table w-full border-collapse border border-gray-200 text-sm text-center mb-6">
                    <thead class="bg-gray-50 text-gray-600 font-normal">
                      <tr>
                        <th class="p-3 border border-gray-200 font-normal">行为时间</th>
                        <th class="p-3 border border-gray-200 font-normal">机构数</th>
                        <th class="p-3 border border-gray-200 font-normal">贷款笔数</th>
                        <th class="p-3 border border-gray-200 font-normal">贷款总金额</th>
                        <th class="p-3 border border-gray-200 font-normal">履约贷款总金额</th>
                         <th class="p-3 border border-gray-200 font-normal">履约贷款数</th>
                        <th class="p-3 border border-gray-200 font-normal">失败扣款数</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="(row, label) in behaviorTimeRows" :key="label">
                         <td class="p-3 border border-gray-200 text-gray-500">{{ label }}</td>
                         <td class="p-3 border border-gray-200 text-gray-500">{{ currentReport.behavior_report_detail[row[0]] || '-' }}</td>
                         <td class="p-3 border border-gray-200 text-gray-500">{{ currentReport.behavior_report_detail[row[1]] || '-' }}</td>
                         <td class="p-3 border border-gray-200 text-gray-500">{{ currentReport.behavior_report_detail[row[2]] || '-' }}</td>
                         <td class="p-3 border border-gray-200 text-green-600">{{ currentReport.behavior_report_detail[row[3]] || '-' }}</td>
                         <td class="p-3 border border-gray-200 text-green-600">{{ currentReport.behavior_report_detail[row[4]] || '-' }}</td>
                         <td class="p-3 border border-gray-200 text-gray-500">{{ currentReport.behavior_report_detail[row[5]] || '-' }}</td>
                      </tr>
                    </tbody>
                 </table>
                 
                 <!-- 贷款金额分布 (Loan Amount Dist) -->
                 <table class="report-table w-full border-collapse border border-gray-200 text-sm text-center mb-6">
                    <thead>
                       <tr>
                         <th class="bg-gray-50 p-3 border border-gray-200 font-normal w-1/4">近12个月贷款金额</th>
                         <th class="bg-gray-50 p-3 border border-gray-200 font-normal w-1/4">1K及以下</th>
                         <th class="bg-gray-50 p-3 border border-gray-200 font-normal w-1/4">1K-3K</th>
                         <th class="bg-gray-50 p-3 border border-gray-200 font-normal w-1/4">3K-10K</th>
                         <th class="bg-gray-50 p-3 border border-gray-200 font-normal w-1/4">1W以上</th>
                       </tr>
                    </thead>
                    <tbody>
                       <tr>
                          <td class="bg-gray-50 p-3 border border-gray-200">贷款笔数</td>
                          <td class="p-3 border border-gray-200 text-gray-500">{{ currentReport.behavior_report_detail['B22170012'] || '0' }}</td>
                          <td class="p-3 border border-gray-200 text-gray-500">{{ currentReport.behavior_report_detail['B22170013'] || '0' }}</td>
                          <td class="p-3 border border-gray-200 text-gray-500">{{ currentReport.behavior_report_detail['B22170014'] || '0' }}</td>
                          <td class="p-3 border border-gray-200 text-gray-500">{{ currentReport.behavior_report_detail['B22170015'] || '0' }}</td>
                       </tr>
                    </tbody>
                 </table>

                 <!-- 贷款机构类型分布 (Org Types) -->
                 <table class="report-table w-full border-collapse border border-gray-200 text-sm text-center mb-6">
                    <thead>
                       <tr>
                         <th class="bg-gray-50 p-3 border border-gray-200 font-normal w-1/4">近12个月消金类贷款机构数</th>
                         <th class="bg-gray-50 p-3 border border-gray-200 font-normal w-1/4">近24个月消金类贷款机构数</th>
                         <th class="bg-gray-50 p-3 border border-gray-200 font-normal w-1/4">近12个月网贷类贷款机构数</th>
                         <th class="bg-gray-50 p-3 border border-gray-200 font-normal w-1/4">近24个月网贷类贷款机构数</th>
                       </tr>
                    </thead>
                    <tbody>
                       <tr>
                          <td class="p-3 border border-gray-200 text-gray-500">{{ currentReport.behavior_report_detail['B22170021'] || '0' }}</td>
                          <td class="p-3 border border-gray-200 text-gray-500">{{ currentReport.behavior_report_detail['B22170022'] || '0' }}</td>
                          <td class="p-3 border border-gray-200 text-gray-500">{{ currentReport.behavior_report_detail['B22170023'] || '0' }}</td>
                          <td class="p-3 border border-gray-200 text-gray-500">{{ currentReport.behavior_report_detail['B22170024'] || '0' }}</td>
                       </tr>
                    </tbody>
                 </table>
                 
                 <!-- 逾期详情 (Overdue Details) -->
                 <table class="report-table w-full border-collapse border border-gray-200 text-sm text-center mb-6">
                    <tbody>
                       <tr>
                          <td class="bg-gray-50 p-3 border border-gray-200 w-1/4">近6个月M0+逾期贷款笔数</td>
                          <td class="p-3 border border-gray-200 w-1/4">{{ currentReport.behavior_report_detail['B22170025'] || '-' }}</td>
                          <td class="bg-gray-50 p-3 border border-gray-200 w-1/4">近6个月M1+逾期贷款笔数</td>
                          <td class="p-3 border border-gray-200 w-1/4">{{ currentReport.behavior_report_detail['B22170028'] || '-' }}</td>
                          <td class="bg-gray-50 p-3 border border-gray-200 w-1/4">近6个月累计逾期金额</td>
                          <td class="p-3 border border-gray-200 w-1/4">{{ currentReport.behavior_report_detail['B22170031'] || '-' }}</td>
                       </tr>
                        <tr>
                          <td class="bg-gray-50 p-3 border border-gray-200">近12个月M0+逾期贷款笔数</td>
                          <td class="p-3 border border-gray-200 text-red-500 font-bold">{{ currentReport.behavior_report_detail['B22170026'] || '-' }}</td>
                          <td class="bg-gray-50 p-3 border border-gray-200">近12个月M1+逾期贷款笔数</td>
                          <td class="p-3 border border-gray-200 text-red-500 font-bold">{{ currentReport.behavior_report_detail['B22170029'] || '-' }}</td>
                          <td class="bg-gray-50 p-3 border border-gray-200">近12个月累计逾期金额</td>
                          <td class="p-3 border border-gray-200 text-red-500 font-bold">{{ currentReport.behavior_report_detail['B22170032'] || '-' }}</td>
                       </tr>
                        <tr>
                          <td class="bg-gray-50 p-3 border border-gray-200">近24个月M0+逾期贷款笔数</td>
                          <td class="p-3 border border-gray-200">{{ currentReport.behavior_report_detail['B22170027'] || '-' }}</td>
                          <td class="bg-gray-50 p-3 border border-gray-200">近24个月M1+逾期贷款笔数</td>
                          <td class="p-3 border border-gray-200">{{ currentReport.behavior_report_detail['B22170030'] || '-' }}</td>
                          <td class="bg-gray-50 p-3 border border-gray-200">近24个月累计逾期金额</td>
                          <td class="p-3 border border-gray-200">{{ currentReport.behavior_report_detail['B22170033'] || '-' }}</td>
                       </tr>
                    </tbody>
                 </table>
              </div>
              
              <!-- 3. 信用详情 -->
               <div class="section mb-8">
                 <h4 class="text-blue-500 font-medium text-base mb-4">信用详情</h4>
                 <table class="report-table w-full border-collapse border border-gray-200 text-sm text-center">
                   <tbody>
                      <tr>
                        <td class="bg-gray-50 p-3 border border-gray-200 w-1/4">网贷建议授信额度</td>
                        <td class="p-3 border border-gray-200 w-1/4">{{ currentReport.current_report_detail['C22180001'] || '0' }}</td>
                        <td class="bg-gray-50 p-3 border border-gray-200 w-1/4">网贷额度置信度</td>
                        <td class="p-3 border border-gray-200 w-1/4 text-red-500 font-bold">{{ currentReport.current_report_detail['C22180002'] || '0' }}</td>
                        <td class="bg-gray-50 p-3 border border-gray-200 w-1/4">网络贷款类机构数</td>
                        <td class="p-3 border border-gray-200 w-1/4">{{ currentReport.current_report_detail['C22180003'] || '0' }}</td>
                      </tr>
                      <tr>
                        <td class="bg-gray-50 p-3 border border-gray-200">网络贷款类产品数</td>
                        <td class="p-3 border border-gray-200">{{ currentReport.current_report_detail['C22180004'] || '0' }}</td>
                        <td class="bg-gray-50 p-3 border border-gray-200">网络贷款机构最大授信额度</td>
                        <td class="p-3 border border-gray-200">{{ currentReport.current_report_detail['C22180005'] || '0' }}</td>
                        <td class="bg-gray-50 p-3 border border-gray-200">网络贷款机构平均授信额度</td>
                        <td class="p-3 border border-gray-200">{{ currentReport.current_report_detail['C22180006'] || '0' }}</td>
                      </tr>
                      <tr>
                        <td class="bg-gray-50 p-3 border border-gray-200">消金贷款类机构数</td>
                        <td class="p-3 border border-gray-200">{{ currentReport.current_report_detail['C22180007'] || '0' }}</td>
                        <td class="bg-gray-50 p-3 border border-gray-200">消金贷款类产品数</td>
                        <td class="p-3 border border-gray-200">{{ currentReport.current_report_detail['C22180008'] || '0' }}</td>
                        <td class="bg-gray-50 p-3 border border-gray-200">消金贷款类机构最大授信额度</td>
                        <td class="p-3 border border-gray-200">{{ currentReport.current_report_detail['C22180009'] || '0' }}</td>
                      </tr>
                      <tr>
                        <td class="bg-gray-50 p-3 border border-gray-200">消金贷款类机构平均授信额度</td>
                        <td class="p-3 border border-gray-200">{{ currentReport.current_report_detail['C22180010'] || '0' }}</td>
                        <td class="bg-gray-50 p-3 border border-gray-200">消金建议授信额度</td>
                        <td class="p-3 border border-gray-200">{{ currentReport.current_report_detail['C22180011'] || '0' }}</td>
                        <td class="bg-gray-50 p-3 border border-gray-200">消金额度置信度</td>
                        <td class="p-3 border border-gray-200 text-red-500 font-bold">{{ currentReport.current_report_detail['C22180012'] || '0' }}</td>
                      </tr>
                   </tbody>
                 </table>
               </div>
               
            </TabPane>
          </Tabs>

        </div>
        <div v-else class="text-center p-8">
           <Empty description="暂无详细数据或格式不匹配" />
           <div class="text-xs text-gray-400 mt-4 max-h-40 overflow-auto bg-gray-100 p-2 rounded text-left">
              {{ currentReport }}
           </div>
        </div>
      </Modal>
    </div>
  </Page>
</template>

<style scoped>
.risk-report .report-table {
  table-layout: fixed;
  border-color: #e5e7eb;
  background: #ffffff;
}

.risk-report .report-table th,
.risk-report .report-table td {
  padding: 8px 10px;
  line-height: 1.4;
  word-break: break-word;
}

.risk-report .report-table td {
  font-size: 12px;
  color: #4b5563;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.2px;
}

.risk-report .report-table th {
  font-size: 12px;
  color: #6b7280;
  font-weight: 500;
}

.risk-search {
  display: flex;
  align-items: center;
  gap: 8px;
}

.risk-report-dark {
  color: #e2e8f0;
}

.risk-report-dark .bg-gray-50 {
  background-color: #0f172a !important;
}

.risk-report-dark .text-gray-600,
.risk-report-dark .text-gray-500 {
  color: #94a3b8;
}

.risk-report-dark .report-table {
  background: #0b1220;
  border-color: #1f2a44;
}

.risk-report-dark .report-table th,
.risk-report-dark .report-table td {
  border-color: #1f2a44;
  color: #e2e8f0;
}

.risk-report-dark .report-table thead th {
  background: #0f172a;
  color: #cbd5e1;
}

.risk-report-dark .section {
  border-color: #1f2a44;
}
</style>
