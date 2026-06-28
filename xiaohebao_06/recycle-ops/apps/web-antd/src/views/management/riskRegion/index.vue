<script lang="ts" setup>
import type {
  RegionItemDto,
  RiskRegionItemDto,
  RiskRegionType,
} from '#/api/biz/config';

import { computed, onMounted, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';

import {
  Button,
  message,
  Popconfirm,
  Select,
  Space,
  Table,
  Tag,
} from 'ant-design-vue';

import { bizConfigApi } from '#/api/biz/config';

interface RegionPanelState {
  cityId?: number;
  cityName?: string;
  loading: boolean;
  provinceId?: number;
  provinceName?: string;
  saving: boolean;
}

const loading = ref(false);
const provinces = ref<RegionItemDto[]>([]);
const cityOptions = reactive<Record<RiskRegionType, RegionItemDto[]>>({
  BLACK: [],
  RISK: [],
});
const rows = reactive<Record<RiskRegionType, RiskRegionItemDto[]>>({
  BLACK: [],
  RISK: [],
});
const formState = reactive<Record<RiskRegionType, RegionPanelState>>({
  BLACK: { loading: false, saving: false },
  RISK: { loading: false, saving: false },
});

const columns = [
  { title: '省份', dataIndex: 'province', width: 120 },
  { title: '城市', dataIndex: 'city', width: 120 },
  { title: '操作', key: 'action', width: 80, align: 'center' as const },
];

const blackCount = computed(() => rows.BLACK.length);
const riskCount = computed(() => rows.RISK.length);

async function loadAll() {
  loading.value = true;
  try {
    const [blackList, riskList] = await Promise.all([
      bizConfigApi.listRiskRegions('BLACK'),
      bizConfigApi.listRiskRegions('RISK'),
    ]);
    rows.BLACK = Array.isArray(blackList) ? blackList : [];
    rows.RISK = Array.isArray(riskList) ? riskList : [];
  } finally {
    loading.value = false;
  }
}

async function loadProvinces() {
  provinces.value = await bizConfigApi.listRegionChildren(0);
}

async function handleProvinceChange(type: RiskRegionType, provinceId?: number) {
  const state = formState[type];
  const province = provinces.value.find((item) => item.id === provinceId);
  state.provinceId = province?.id;
  state.provinceName = province?.name;
  state.cityId = undefined;
  state.cityName = undefined;
  cityOptions[type] = [];
  if (!province?.id) {
    return;
  }
  state.loading = true;
  try {
    cityOptions[type] = await bizConfigApi.listRegionChildren(province.id);
  } finally {
    state.loading = false;
  }
}

function handleCityChange(type: RiskRegionType, cityId?: number) {
  const city = cityOptions[type].find((item) => item.id === cityId);
  formState[type].cityId = city?.id;
  formState[type].cityName = city?.name;
}

async function addRegion(type: RiskRegionType) {
  const state = formState[type];
  if (!state.provinceName || !state.cityName) {
    message.warning('请选择省份和城市');
    return;
  }
  state.saving = true;
  try {
    await bizConfigApi.addRiskRegion({
      regionType: type,
      province: state.provinceName,
      city: state.cityName,
    });
    message.success('已添加');
    state.cityId = undefined;
    state.cityName = undefined;
    await loadAll();
  } finally {
    state.saving = false;
  }
}

async function deleteRegion(record: RiskRegionItemDto) {
  await bizConfigApi.deleteRiskRegion(record.id);
  message.success('已删除');
  await loadAll();
}

onMounted(async () => {
  await Promise.all([loadProvinces(), loadAll()]);
});
</script>

<template>
  <Page title="风险地区提醒">
    <div class="risk-region-page">
      <div class="region-grid">
        <section class="region-panel">
          <div class="panel-header">
            <div>
              <div class="panel-title">黑名单地区</div>
              <div class="panel-subtitle">命中后客户姓名旁展示黑底“黑”标签</div>
            </div>
            <Tag color="default">{{ blackCount }} 条</Tag>
          </div>
          <Space class="panel-form" :size="8" wrap>
            <Select
              class="area-select"
              placeholder="选择省份"
              :value="formState.BLACK.provinceId"
              :options="provinces.map((item) => ({ label: item.name, value: item.id }))"
              @change="(value) => handleProvinceChange('BLACK', Number(value))"
            />
            <Select
              class="area-select"
              placeholder="选择城市"
              :loading="formState.BLACK.loading"
              :value="formState.BLACK.cityId"
              :options="cityOptions.BLACK.map((item) => ({ label: item.name, value: item.id }))"
              @change="(value) => handleCityChange('BLACK', Number(value))"
            />
            <Button
              type="primary"
              :loading="formState.BLACK.saving"
              @click="addRegion('BLACK')"
            >
              添加
            </Button>
          </Space>
          <Table
            class="region-table"
            :columns="columns"
            :data-source="rows.BLACK"
            :loading="loading"
            row-key="id"
            size="small"
            :pagination="false"
            bordered
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'action'">
                <Popconfirm title="确认删除该地区？" @confirm="deleteRegion(record)">
                  <Button danger size="small" type="link">删除</Button>
                </Popconfirm>
              </template>
            </template>
          </Table>
        </section>

        <section class="region-panel">
          <div class="panel-header">
            <div>
              <div class="panel-title">风险地区</div>
              <div class="panel-subtitle">命中后客户姓名旁展示红底“风”标签</div>
            </div>
            <Tag color="red">{{ riskCount }} 条</Tag>
          </div>
          <Space class="panel-form" :size="8" wrap>
            <Select
              class="area-select"
              placeholder="选择省份"
              :value="formState.RISK.provinceId"
              :options="provinces.map((item) => ({ label: item.name, value: item.id }))"
              @change="(value) => handleProvinceChange('RISK', Number(value))"
            />
            <Select
              class="area-select"
              placeholder="选择城市"
              :loading="formState.RISK.loading"
              :value="formState.RISK.cityId"
              :options="cityOptions.RISK.map((item) => ({ label: item.name, value: item.id }))"
              @change="(value) => handleCityChange('RISK', Number(value))"
            />
            <Button
              type="primary"
              danger
              :loading="formState.RISK.saving"
              @click="addRegion('RISK')"
            >
              添加
            </Button>
          </Space>
          <Table
            class="region-table"
            :columns="columns"
            :data-source="rows.RISK"
            :loading="loading"
            row-key="id"
            size="small"
            :pagination="false"
            bordered
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'action'">
                <Popconfirm title="确认删除该地区？" @confirm="deleteRegion(record)">
                  <Button danger size="small" type="link">删除</Button>
                </Popconfirm>
              </template>
            </template>
          </Table>
        </section>
      </div>
    </div>
  </Page>
</template>

<style scoped>
.risk-region-page {
  padding: 16px;
}

.region-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.region-panel {
  min-width: 0;
  padding: 16px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.panel-subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: #6b7280;
}

.panel-form {
  display: flex;
  width: 100%;
  margin-bottom: 28px;
  padding-bottom: 4px;
}

.region-table {
  margin-top: 12px;
}

.area-select {
  width: 180px;
}

@media (max-width: 900px) {
  .region-grid {
    grid-template-columns: 1fr;
  }
}
</style>
