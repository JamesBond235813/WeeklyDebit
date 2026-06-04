<script lang="ts" setup>
import { ref } from 'vue';
import { useVbenModal } from '@vben/common-ui';
import { useVbenForm } from '#/adapter/form';
import { Button, Spin, Timeline, message } from 'ant-design-vue';
import { createOrder, getOrderStatusOptions, getOrderTracking, updateOrderStatus } from '#/api/biz/order';
import { customerApi } from '#/api/biz/customer';
import { getPlatformOptions } from '#/api/biz/platform';
import type { ExpressTraceItem } from '#/api/biz/model/orderModel';
import { regionApi } from '#/api/biz/region';

const emit = defineEmits(['success']);

const isUpdate = ref(false);
const recordId = ref<number | undefined>(undefined);
const customerOptions = ref<any[]>([]);
const platformOptions = ref<any[]>([]);
const statusOptions = ref<any[]>([]);
const currentStatus = ref('');
type RegionOption = {
  id: number;
  value: string;
  label: string;
  isLeaf?: boolean;
  loading?: boolean;
  children?: RegionOption[];
};
const regionOptions = ref<RegionOption[]>([]);
const addressInfoRef = ref({
  platformRecvProvince: '',
  platformRecvCity: '',
  platformRecvDistrict: '',
  platformRecvStreet: '',
  selfRecvProvince: '',
  selfRecvCity: '',
  selfRecvDistrict: '',
  selfRecvStreet: '',
});
const trackingState = ref({
  platform: {
    loading: false,
    message: '',
    companyName: '',
    traces: [] as ExpressTraceItem[],
  },
  customer: {
    loading: false,
    message: '',
    companyName: '',
    traces: [] as ExpressTraceItem[],
  },
});

function calcRecyclePrice(agreed?: number, downPayment?: number) {
  const agreedVal = Number(agreed ?? 0);
  const downVal = Number(downPayment ?? 0);
  if (Number.isNaN(agreedVal) || Number.isNaN(downVal)) {
    return 0;
  }
  const raw = agreedVal - downVal;
  const rounded = Math.round(raw * 100) / 100;
  return rounded < 0 ? 0 : rounded;
}

async function syncRecyclePriceFromBase() {
  if (!isUpdate.value) {
    return;
  }
  const baseValues = await basicFormApi.getValues();
  const value = calcRecyclePrice(baseValues.agreedRecyclePrice, baseValues.downPaymentAmount);
  updateFormApi.setValues({ recyclePrice: value });
}

function canEditStatusFields(status: string) {
  return {
    canEditStatus: isUpdate.value,
    canEditResale: isUpdate.value && status === 'RESOLD',
    canEditPlatformTracking:
      isUpdate.value &&
      ['SHIPPED', 'FORWARDED', 'RECEIVED', 'PAID', 'RESOLD'].includes(status),
    canEditCustomerTracking:
      isUpdate.value &&
      ['FORWARDED', 'RECEIVED', 'PAID', 'RESOLD'].includes(status),
  };
}

function syncUpdateFieldAccess() {
  const status = currentStatus.value;
  const access = canEditStatusFields(status);
  updateFormApi.updateSchema([
    { fieldName: 'targetStatus', disabled: !access.canEditStatus },
    { fieldName: 'recyclePrice', disabled: true },
    { fieldName: 'resalePrice', disabled: !access.canEditResale },
  ]);
  trackingFormApi.updateSchema([
    {
      fieldName: 'trackingNoPlatform',
      disabled: !access.canEditPlatformTracking,
    },
    {
      fieldName: 'trackingNoCustomer',
      disabled: !access.canEditCustomerTracking,
    },
  ]);
}

async function refreshTracking(source: 'platform' | 'customer', trackingNo?: string) {
  const target = trackingState.value[source];
  const normalized = (trackingNo || '').trim();
  if (!normalized) {
    target.loading = false;
    target.message = '暂无物流单号';
    target.companyName = '';
    target.traces = [];
    return;
  }
  target.loading = true;
  target.message = '';
  target.companyName = '';
  target.traces = [];
  try {
    const res = await getOrderTracking(normalized);
    target.message = res?.message || '';
    target.companyName = res?.companyName || '';
    target.traces = res?.traces || [];
  } catch (error) {
    target.message = '查询失败，请稍后再试';
    target.traces = [];
  } finally {
    target.loading = false;
  }
}

async function refreshTrackingFromForm(source: 'platform' | 'customer') {
  if (!isUpdate.value) {
    return;
  }
  const values = await trackingFormApi.getValues();
  const trackingNo =
    source === 'platform' ? values.trackingNoPlatform : values.trackingNoCustomer;
  await refreshTracking(source, trackingNo);
}

async function refreshTrackingAll(record?: Record<string, any>) {
  await refreshTracking('platform', record?.trackingNoPlatform);
  await refreshTracking('customer', record?.trackingNoCustomer);
}

function buildAreaPath(parts: Array<string | undefined>) {
  return parts.filter((item) => item && item.trim()).map((item) => item as string);
}

function toRegionOption(item: { id: number; name: string; leaf: boolean }) {
  return {
    id: item.id,
    value: item.name,
    label: item.name,
    isLeaf: item.leaf,
  } as RegionOption;
}

async function fetchRegionChildren(parentId?: number) {
  try {
    const list = await regionApi.listChildren(parentId);
    return (list || []).map(toRegionOption);
  } catch (error) {
    return [];
  }
}

async function initRegionOptions() {
  if (regionOptions.value.length > 0) {
    return;
  }
  regionOptions.value = await fetchRegionChildren(0);
}

async function loadRegionChildren(selectedOptions: RegionOption[]) {
  const targetOption = selectedOptions[selectedOptions.length - 1];
  if (!targetOption || targetOption.isLeaf || targetOption.loading) {
    return;
  }
  targetOption.loading = true;
  targetOption.children = await fetchRegionChildren(targetOption.id);
  targetOption.loading = false;
}

async function ensureRegionPath(path: string[]) {
  if (path.length === 0) {
    return;
  }
  await initRegionOptions();
  let currentOptions = regionOptions.value;
  for (const name of path) {
    const match = currentOptions.find((item) => item.value === name);
    if (!match || match.isLeaf) {
      return;
    }
    if (!match.children) {
      match.children = await fetchRegionChildren(match.id);
    }
    currentOptions = match.children || [];
  }
}

function syncPlatformRecvArea(values: string[] = [], selectedOptions: RegionOption[] = []) {
  const limited = values.slice(0, 4);
  if (values.length > 4) {
    addressFormApi.setValues({ platformRecvArea: limited });
  }
  const labels =
    selectedOptions.length > 0
      ? selectedOptions.map((item) => item.label || item.value)
      : limited;
  addressInfoRef.value = {
    ...addressInfoRef.value,
    platformRecvProvince: labels[0] || '',
    platformRecvCity: labels[1] || '',
    platformRecvDistrict: labels[2] || '',
    platformRecvStreet: labels[3] || '',
  };
}

function syncSelfRecvArea(values: string[] = [], selectedOptions: RegionOption[] = []) {
  const limited = values.slice(0, 4);
  if (values.length > 4) {
    addressFormApi.setValues({ selfRecvArea: limited });
  }
  const labels =
    selectedOptions.length > 0
      ? selectedOptions.map((item) => item.label || item.value)
      : limited;
  addressInfoRef.value = {
    ...addressInfoRef.value,
    selfRecvProvince: labels[0] || '',
    selfRecvCity: labels[1] || '',
    selfRecvDistrict: labels[2] || '',
    selfRecvStreet: labels[3] || '',
  };
}

const [BasicForm, basicFormApi] = useVbenForm({
  wrapperClass: 'grid-cols-4 gap-x-4 gap-y-2',
  showDefaultActions: false,
  layout: 'vertical',
  commonConfig: {
    componentProps: {
      class: 'w-full',
      size: 'small',
    },
  },
  schema: [
    {
      component: 'Select',
      fieldName: 'customerId',
      label: '客户',
      rules: 'required',
      componentProps: {
        placeholder: '搜索客户姓名/手机号',
        showSearch: true,
        filterOption: false,
        options: customerOptions,
        onSearch: onSearchCustomer,
        labelInValue: false, // We need ID, but maybe backend needs ID.
      },
      formItemClass: 'col-span-1',
      // Hide in update mode if we don't want to change customer
    },
    {
      component: 'Select',
      fieldName: 'platformId',
      label: '平台',
      rules: 'required',
      componentProps: {
        placeholder: '选择平台',
        options: platformOptions,
      },
      formItemClass: 'col-span-1',
    },
    {
      component: 'Input',
      fieldName: 'deviceModel',
      label: '设备型号',
      rules: 'required',
      componentProps: {
        placeholder: '如 iPhone 15 Pro',
      },
      formItemClass: 'col-span-1',
    },
    {
      component: 'InputNumber',
      fieldName: 'deviceQuantity',
      label: '数量',
      rules: 'required',
      componentProps: {
        placeholder: '1',
        precision: 0,
        min: 1,
      },
      formItemClass: 'col-span-1',
    },
    {
      component: 'InputNumber',
      fieldName: 'agreedRecyclePrice',
      label: '约定回收价',
      rules: 'required',
      componentProps: {
        placeholder: '0.00',
        prefix: '¥',
        precision: 2,
        min: 0,
        onChange: () => {
          syncRecyclePriceFromBase();
        },
      },
      formItemClass: 'col-span-1',
    },
    {
      component: 'Switch',
      fieldName: 'isDownPaymentAdvanced',
      label: '是否垫付首付',
      componentProps: {
        checkedChildren: '是',
        unCheckedChildren: '否',
      },
      formItemClass: 'col-span-1',
    },
    {
      component: 'InputNumber',
      fieldName: 'downPaymentAmount',
      label: '首付总额',
      componentProps: {
        placeholder: '0.00',
        prefix: '¥',
        precision: 2,
        min: 0,
        onChange: () => {
          syncRecyclePriceFromBase();
        },
      },
      formItemClass: 'col-span-1',
    },
    {
      component: 'InputNumber',
      fieldName: 'channelCommission',
      label: '渠道返佣',
      componentProps: {
        placeholder: '0.00',
        prefix: '¥',
        precision: 2,
        min: 0,
      },
      formItemClass: 'col-span-1',
    },
    {
        component: 'InputTextArea',
        fieldName: 'remark',
        label: '备注',
        componentProps: {
            placeholder: '订单备注...',
            rows: 2,
        },
        formItemClass: 'col-span-4',
    },
  ],
});

const [AddressForm, addressFormApi] = useVbenForm({
  wrapperClass: 'grid-cols-2 gap-x-4 gap-y-2',
  showDefaultActions: false,
  layout: 'vertical',
  commonConfig: {
    componentProps: {
      class: 'w-full',
      size: 'small',
    },
  },
  schema: [
    {
      component: 'Cascader',
      fieldName: 'platformRecvArea',
      label: '平台收件地址',
      rules: 'required',
      componentProps: {
        options: regionOptions,
        allowClear: true,
        changeOnSelect: true,
        showSearch: true,
        loadData: loadRegionChildren,
        onChange: (values: string[], selectedOptions: RegionOption[]) => {
          syncPlatformRecvArea(values, selectedOptions);
        },
      },
      formItemClass: 'col-span-1',
    },
    {
      component: 'Input',
      fieldName: 'platformRecvDetail',
      label: '平台收件详细地址',
      rules: 'required',
      componentProps: {
        placeholder: '门牌号/楼层/房间号',
      },
      formItemClass: 'col-span-1',
    },
    {
      component: 'Cascader',
      fieldName: 'selfRecvArea',
      label: '我方收件地址',
      rules: 'required',
      componentProps: {
        options: regionOptions,
        allowClear: true,
        changeOnSelect: true,
        showSearch: true,
        loadData: loadRegionChildren,
        onChange: (values: string[], selectedOptions: RegionOption[]) => {
          syncSelfRecvArea(values, selectedOptions);
        },
      },
      formItemClass: 'col-span-1',
    },
    {
      component: 'Input',
      fieldName: 'selfRecvDetail',
      label: '我方收件详细地址',
      rules: 'required',
      componentProps: {
        placeholder: '门牌号/楼层/房间号',
      },
      formItemClass: 'col-span-1',
    },
  ],
});

const [UpdateForm, updateFormApi] = useVbenForm({
  wrapperClass: 'grid-cols-3 gap-x-4 gap-y-2',
  showDefaultActions: false,
  layout: 'vertical',
  commonConfig: {
    componentProps: {
      class: 'w-full',
      size: 'small',
    },
  },
  schema: [
    {
      component: 'Select',
      fieldName: 'targetStatus',
      label: '更新状态',
      rules: 'required',
      componentProps: {
        options: statusOptions,
        onChange: (val: string) => {
          currentStatus.value = val;
          syncUpdateFieldAccess();
        },
      },
    },
    {
      component: 'InputNumber',
      fieldName: 'recyclePrice',
      label: '回收价/尾款支出',
      componentProps: {
        placeholder: '自动计算',
        precision: 2,
        prefix: '¥',
        disabled: true,
      },
    },
    {
      component: 'InputNumber',
      fieldName: 'resalePrice',
      label: '出售价/变现收入',
      componentProps: { placeholder: '0.00', precision: 2, prefix: '¥' },
    },
  ],
});

const [TrackingForm, trackingFormApi] = useVbenForm({
  wrapperClass: 'grid-cols-2 gap-x-8 gap-y-2',
  showDefaultActions: false,
  layout: 'vertical',
  commonConfig: {
    componentProps: {
      class: 'w-full',
      size: 'small',
    },
  },
  schema: [
    {
      component: 'Input',
      fieldName: 'trackingNoPlatform',
      label: '平台发货物流单号',
      componentProps: { placeholder: '平台发给客户的物流单号' },
    },
    {
      component: 'Input',
      fieldName: 'trackingNoCustomer',
      label: '客户转寄物流单号',
      componentProps: { placeholder: '客户寄回给我们的物流单号' },
    },
  ],
});

const [Modal, modalApi] = useVbenModal({
  title: '订单详情',
  class: 'w-[800px]',
  onConfirm: handleSubmit,
  async onOpenChange(isOpen: boolean) {
    if (isOpen) {
      await loadOptions();
      await initRegionOptions();
      const data = modalApi.getData<Record<string, any>>();
      isUpdate.value = !!data?.record;
      recordId.value = data?.record?.id;

      if (isUpdate.value) {
        const r = data.record;
        basicFormApi.setValues({
            customerId: r.customerId,
            platformId: r.platformId,
            deviceModel: r.deviceModel,
            deviceQuantity: r.deviceQuantity,
            downPaymentAmount: r.downPaymentAmount,
            isDownPaymentAdvanced: r.isDownPaymentAdvanced,
            agreedRecyclePrice: r.agreedRecyclePrice,
            channelCommission: r.channelCommission,
            remark: r.remark,
        });
        addressFormApi.setValues({
          platformRecvDetail: r.platformRecvDetail,
          selfRecvDetail: r.selfRecvDetail,
        });
        addressInfoRef.value = {
          platformRecvProvince: r.platformRecvProvince || '',
          platformRecvCity: r.platformRecvCity || '',
          platformRecvDistrict: r.platformRecvDistrict || '',
          platformRecvStreet: r.platformRecvStreet || '',
          selfRecvProvince: r.selfRecvProvince || '',
          selfRecvCity: r.selfRecvCity || '',
          selfRecvDistrict: r.selfRecvDistrict || '',
          selfRecvStreet: r.selfRecvStreet || '',
        };
        const platformPath = buildAreaPath([
          r.platformRecvProvince,
          r.platformRecvCity,
          r.platformRecvDistrict,
          r.platformRecvStreet,
        ]);
        const selfPath = buildAreaPath([
          r.selfRecvProvince,
          r.selfRecvCity,
          r.selfRecvDistrict,
          r.selfRecvStreet,
        ]);
        if (platformPath.length > 0) {
          await ensureRegionPath(platformPath);
          addressFormApi.setValues({ platformRecvArea: platformPath });
        }
        if (selfPath.length > 0) {
          await ensureRegionPath(selfPath);
          addressFormApi.setValues({ selfRecvArea: selfPath });
        }
        updateFormApi.setValues({
          targetStatus: r.status,
          recyclePrice: r.recyclePrice,
          resalePrice: r.resalePrice,
        });
        await syncRecyclePriceFromBase();
        trackingFormApi.setValues({
          trackingNoPlatform: r.trackingNoPlatform,
          trackingNoCustomer: r.trackingNoCustomer,
        });
        currentStatus.value = r.status;
        syncUpdateFieldAccess();
        
        // Pre-fill customer info into options so it displays correctly
        if (r.customerId && r.customerName) {
             customerOptions.value = [{ label: `${r.customerName} ${r.customerMobile||''}`, value: r.customerId }];
        }
        await refreshTrackingAll(r);
      } else {
        await basicFormApi.resetForm();
        await addressFormApi.resetForm();
        await updateFormApi.resetForm();
        await trackingFormApi.resetForm();
        currentStatus.value = '';
        addressInfoRef.value = {
          platformRecvProvince: '',
          platformRecvCity: '',
          platformRecvDistrict: '',
          platformRecvStreet: '',
          selfRecvProvince: '',
          selfRecvCity: '',
          selfRecvDistrict: '',
          selfRecvStreet: '',
        };
        trackingState.value = {
          platform: { loading: false, message: '', companyName: '', traces: [] },
          customer: { loading: false, message: '', companyName: '', traces: [] },
        };
        basicFormApi.setValues({ isDownPaymentAdvanced: false });
        const defaultStatus = statusOptions.value?.[0]?.value || '';
        currentStatus.value = defaultStatus;
        updateFormApi.setValues({
          targetStatus: defaultStatus,
          recyclePrice: 0,
          resalePrice: 0,
        });
        syncUpdateFieldAccess();
      }
    }
  },
});

async function loadOptions() {
    // Load Status Options
    const statusRes = await getOrderStatusOptions();
    statusOptions.value = statusRes;
    
    // Load Platform Options
    const platformRes = await getPlatformOptions();
    platformOptions.value = platformRes.map(p => ({ label: p.name, value: p.id }));
}

async function onSearchCustomer(keyword: string) {
    if (!keyword) return;
    // Simple logic: if keyword is number -> mobile, else namePrefix
    const isNum = /^\d+$/.test(keyword);
    const params:any = { page: 1, pageSize: 20 };
    if (isNum) params.mobile = keyword;
    else params.namePrefix = keyword;
    
    const searchRes = await customerApi.pagedListCustomerInfo(params);
    
    if (searchRes && searchRes.list) {
        customerOptions.value = searchRes.list.map(c => ({
            label: `${c.name} ${c.mobile}`,
            value: c.id
        }));
    }
}

async function handleSubmit() {
  try {
    const { valid: baseValid } = await basicFormApi.validate();
    if (!baseValid) {
      return;
    }
    const { valid: addressValid } = await addressFormApi.validate();
    if (!addressValid) {
      return;
    }
    if (isUpdate.value) {
      const { valid: updateValid } = await updateFormApi.validate();
      if (!updateValid) {
        return;
      }
    }

    modalApi.setState({ loading: true });
    const baseValues = await basicFormApi.getValues();
    const addressValues = await addressFormApi.getValues();
    const { platformRecvArea, selfRecvArea, ...addressPlain } = addressValues || {};
    const updateValues = isUpdate.value ? await updateFormApi.getValues() : {};
    const trackingValues = isUpdate.value ? await trackingFormApi.getValues() : {};
    const formValues = {
      ...baseValues,
      ...addressPlain,
      ...addressInfoRef.value,
      ...updateValues,
      ...trackingValues,
    };

    if (isUpdate.value) {
        // Update Status
        await updateOrderStatus({
            id: recordId.value!,
            targetStatus: formValues.targetStatus,
            deviceModel: formValues.deviceModel,
            deviceQuantity: formValues.deviceQuantity,
            downPaymentAmount: formValues.downPaymentAmount,
            isDownPaymentAdvanced: formValues.isDownPaymentAdvanced,
            agreedRecyclePrice: formValues.agreedRecyclePrice,
            channelCommission: formValues.channelCommission,
            trackingNoPlatform: formValues.trackingNoPlatform,
            trackingNoCustomer: formValues.trackingNoCustomer,
            platformRecvProvince: formValues.platformRecvProvince,
            platformRecvCity: formValues.platformRecvCity,
            platformRecvDistrict: formValues.platformRecvDistrict,
            platformRecvStreet: formValues.platformRecvStreet,
            recyclePrice: formValues.recyclePrice,
            resalePrice: formValues.resalePrice,
            remark: formValues.remark,
            selfRecvProvince: formValues.selfRecvProvince,
            selfRecvCity: formValues.selfRecvCity,
            selfRecvDistrict: formValues.selfRecvDistrict,
            selfRecvStreet: formValues.selfRecvStreet,
            platformRecvDetail: formValues.platformRecvDetail,
            selfRecvDetail: formValues.selfRecvDetail,
        });
    } else {
        // Create
        await createOrder({
            customerId: formValues.customerId,
            platformId: formValues.platformId,
            deviceModel: formValues.deviceModel,
            deviceQuantity: formValues.deviceQuantity,
            downPaymentAmount: formValues.downPaymentAmount,
            isDownPaymentAdvanced: formValues.isDownPaymentAdvanced,
            agreedRecyclePrice: formValues.agreedRecyclePrice,
            channelCommission: formValues.channelCommission,
            remark: formValues.remark,
            platformRecvProvince: formValues.platformRecvProvince,
            platformRecvCity: formValues.platformRecvCity,
            platformRecvDistrict: formValues.platformRecvDistrict,
            platformRecvStreet: formValues.platformRecvStreet,
            platformRecvDetail: formValues.platformRecvDetail,
            selfRecvProvince: formValues.selfRecvProvince,
            selfRecvCity: formValues.selfRecvCity,
            selfRecvDistrict: formValues.selfRecvDistrict,
            selfRecvStreet: formValues.selfRecvStreet,
            selfRecvDetail: formValues.selfRecvDetail,
        });
    }

    message.success('保存成功');
    emit('success');
    modalApi.close();
  } catch (error) {
    console.error(error);
  } finally {
    modalApi.setState({ loading: false });
  }
}
</script>

<template>
  <Modal>
    <div class="p-5 space-y-5">
      <div>
        <div class="mb-2 text-sm font-semibold text-gray-700 dark:text-[#cbd5e1]">
          基础信息
        </div>
        <BasicForm />
      </div>

      <div class="border-t border-gray-100 pt-4 dark:border-[#1f2a44]">
        <div class="mb-2 text-sm font-semibold text-gray-700 dark:text-[#cbd5e1]">
          快递信息
        </div>
        <AddressForm />
      </div>

      <div class="border-t border-gray-100 pt-4 dark:border-[#1f2a44]">
        <div class="mb-2 text-sm font-semibold text-gray-700 dark:text-[#cbd5e1]">
          订单处理
        </div>
        <UpdateForm />
      </div>

      <div class="border-t border-gray-100 pt-4 dark:border-[#1f2a44]">
        <div class="mb-2 text-sm font-semibold text-gray-700 dark:text-[#cbd5e1]">
          包裹流转信息
        </div>
        <div class="mb-3">
          <TrackingForm />
        </div>
        <div class="grid grid-cols-1 gap-4 md:grid-cols-2">
          <div class="pr-0 md:border-r md:border-gray-200 md:pr-4 dark:md:border-[#1f2a44]">
            <div class="mb-2 flex items-center justify-between">
              <div class="text-sm font-medium">平台发货物流</div>
              <Button
                size="small"
                type="default"
                :disabled="!canEditStatusFields(currentStatus).canEditPlatformTracking"
                @click="refreshTrackingFromForm('platform')"
                >刷新</Button
              >
            </div>
            <div v-if="trackingState.platform.loading" class="py-2 text-sm text-gray-500">
              <Spin size="small" /> 查询中...
            </div>
            <div
              v-else-if="!trackingState.platform.traces.length"
              class="py-2 text-sm text-gray-500"
            >
              {{ trackingState.platform.message || '暂无轨迹' }}
            </div>
            <Timeline v-else>
              <Timeline.Item
                v-for="item in trackingState.platform.traces"
                :key="`${item.time}-${item.context}`"
              >
                <div class="text-xs text-gray-500">{{ item.time }}</div>
                <div class="text-sm">{{ item.context }}</div>
              </Timeline.Item>
            </Timeline>
          </div>
          <div class="pl-0 md:pl-4">
            <div class="mb-2 flex items-center justify-between">
              <div class="text-sm font-medium">客户转寄物流</div>
              <Button
                size="small"
                type="default"
                :disabled="!canEditStatusFields(currentStatus).canEditCustomerTracking"
                @click="refreshTrackingFromForm('customer')"
                >刷新</Button
              >
            </div>
            <div v-if="trackingState.customer.loading" class="py-2 text-sm text-gray-500">
              <Spin size="small" /> 查询中...
            </div>
            <div
              v-else-if="!trackingState.customer.traces.length"
              class="py-2 text-sm text-gray-500"
            >
              {{ trackingState.customer.message || '暂无轨迹' }}
            </div>
            <Timeline v-else>
              <Timeline.Item
                v-for="item in trackingState.customer.traces"
                :key="`${item.time}-${item.context}`"
              >
                <div class="text-xs text-gray-500">{{ item.time }}</div>
                <div class="text-sm">{{ item.context }}</div>
              </Timeline.Item>
            </Timeline>
          </div>
        </div>
      </div>
    </div>
  </Modal>
</template>
