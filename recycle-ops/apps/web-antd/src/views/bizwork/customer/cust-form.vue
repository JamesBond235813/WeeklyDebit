<script lang="ts" setup>
import type { CustomerItemDetail } from '#/api/biz/customer';

import { computed, onBeforeUnmount, onMounted, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import {
  Button,
  Col,
  Descriptions,
  Divider,
  message,
  Modal,
  Row,
  Tag,
} from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  customerApi,
  FavoriteTypeEnum,
  gotNoOptions,
  houseFlagOptions,
  marriageStatusOptions,
  yesNoOptions,
} from '#/api/biz/customer';
import { regionApi } from '#/api/biz/region';

const outData = ref();
const _customerGroupOptions = ref<{ label: string; value: number }[]>([]);
const _progressOptions = ref<{ label: string; value: number }[]>([]);
const _deptNameOptions = ref<{ label: string; value: number }[]>([]);
const _callTipsOptions = ref<{ label: string; value: number }[]>([]);
const canEdit = ref<boolean>(false);
const hideLeaderRemark = ref(true);
const customerStarEditable = ref(true);
const customerDetailInfoRef = ref<CustomerItemDetail>({});
const zhimaThreshold = ref<number | null>(null);
const zhimaScoreValue = ref<number | null>(null);
type RegionOption = {
  id: number;
  value: string;
  label: string;
  isLeaf?: boolean;
  loading?: boolean;
  children?: RegionOption[];
};
const regionOptions = ref<RegionOption[]>([]);
const locationInfoRef = ref({
  hukouProvince: '',
  hukouCity: '',
  hukouDistrict: '',
  currentProvince: '',
  currentCity: '',
  currentDistrict: '',
  currentStreet: '',
});
let cidList: number[] = [];
const currentCid = ref(0);
let userId = '';
const currentIndex = computed(() => {
  if (cidList.length === 0) {
    return -1;
  }
  return cidList.indexOf(currentCid.value);
});
const nextCid = computed(() => {
  if (currentIndex.value < 0 || currentIndex.value === cidList.length - 1) {
    return -1;
  }
  return cidList[currentIndex.value + 1];
});
const prevCid = computed(() => {
  if (currentIndex.value <= 0) {
    return -1;
  }
  return cidList[currentIndex.value - 1];
});

const isZhimaHigh = computed(() => {
  const score =
    zhimaScoreValue.value ??
    (typeof customerDetailInfoRef.value?.zhimaScore === 'number'
      ? customerDetailInfoRef.value.zhimaScore
      : null);
  if (score === null || score === undefined) {
    return false;
  }
  if (zhimaThreshold.value === null || zhimaThreshold.value === undefined) {
    return false;
  }
  return score >= zhimaThreshold.value;
});

const ID_CARD_REGEX = /^[0-9]{17}([0-9]|[xX])$/;
function parseIdCardInfo(idCardNo?: string) {
  const normalized = (idCardNo || '').trim().toUpperCase();
  if (!ID_CARD_REGEX.test(normalized)) {
    return null;
  }
  const birth = normalized.slice(6, 14);
  const birthday = `${birth.slice(0, 4)}-${birth.slice(4, 6)}-${birth.slice(6, 8)}`;
  const birthDate = new Date(birthday);
  if (Number.isNaN(birthDate.getTime())) {
    return null;
  }
  const sexCode = Number.parseInt(normalized.charAt(16), 10);
  const sex = sexCode % 2 === 0 ? 2 : 1;
  const today = new Date();
  let age = today.getFullYear() - birthDate.getFullYear();
  const monthDiff = today.getMonth() - birthDate.getMonth();
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
    age -= 1;
  }
  if (age <= 0 || age > 150) {
    return null;
  }
  return { birthday, sex, age };
}

function resolveIdCardNo() {
  return customerDetailInfoRef.value?.idCardNo || outData.value?.idCardNo || '';
}

async function loadZhimaThreshold() {
  const fromOutData = outData.value?.zhimaThreshold;
  if (typeof fromOutData === 'number') {
    zhimaThreshold.value = fromOutData;
    return;
  }
  try {
    const res = await customerApi.getBizDictItems();
    const zhimaList = res.ZHIMA_SCORE_THRESHOLD || [];
    const thresholdValue = zhimaList[0]?.intValue;
    zhimaThreshold.value =
      typeof thresholdValue === 'number' ? thresholdValue : null;
  } catch (error) {
    zhimaThreshold.value = null;
  }
}

const derivedIdCardInfo = computed(() => {
  return parseIdCardInfo(resolveIdCardNo());
});

function getDerivedSexDesc() {
  const info = derivedIdCardInfo.value;
  if (info) {
    if (info.sex === 1) {
      return '男';
    }
    if (info.sex === 2) {
      return '女';
    }
    return '保密';
  }
  return customerDetailInfoRef.value?.sexDesc || '';
}

function getDerivedAge() {
  const info = derivedIdCardInfo.value;
  if (info) {
    return info.age;
  }
  return customerDetailInfoRef.value?.age ?? '';
}

const HOUSE_VAL_PLACEHOLDER = '请输入房产价值，单位万元';
const PROVIDENT_AMOUNT_PLACEHOLDER = '请输入公积金金额，单位元';

const quickRemarkTags = [
  { label: '本地人' },
  { label: '外地人' },
  { label: '有房', fieldName: 'houseFlag', valueOn: 2 },
  { label: '有车', fieldName: 'carFlag', valueOn: 1 },
  { label: '有社保', fieldName: 'socialInsuranceFlag', valueOn: 1 },
  { label: '有公积金', fieldName: 'providentFlag', valueOn: 1 },
  { label: '有保单', fieldName: 'insuranceFlag', valueOn: 1 },
  { label: '有信用卡', fieldName: 'creditCardFlag', valueOn: 1 },
  { label: '有营业执照', fieldName: 'enterpriseFlag', valueOn: 1 },
  { label: '有当前逾期' },
  { label: '有历史逾期' },
  { label: '有司法判决' },
  { label: '有执行' },
];

const remarkTagSelections = ref<string[]>([]);

function isRemarkTagSelected(label: string) {
  return remarkTagSelections.value.includes(label);
}

function setAmountInputState(hasHouse: boolean, hasProvident: boolean) {
  basicInfoFormApi.updateSchema([
    {
      fieldName: 'houseVal',
      componentProps: {
        placeholder: HOUSE_VAL_PLACEHOLDER,
        disabled: !hasHouse,
      },
    },
    {
      fieldName: 'providentAmountYuan',
      componentProps: {
        placeholder: PROVIDENT_AMOUNT_PLACEHOLDER,
        disabled: !hasProvident,
      },
    },
  ]);
}

function applyQualificationByTags(tags: string[], changedLabel?: string) {
  const tagSet = new Set(tags);
  if (changedLabel) {
    const tag = quickRemarkTags.find((item) => item.label === changedLabel);
    if (tag && 'fieldName' in tag) {
      const selected = tagSet.has(tag.label);
      backgroundInfoFormApi.setValues({
        [tag.fieldName]: selected ? tag.valueOn ?? 1 : 0,
      });
    }
  }
  setAmountInputState(tagSet.has('有房'), tagSet.has('有公积金'));
}

function appendRemarkTag(label: string) {
  basicInfoFormApi.getValues().then((values) => {
    const current = String(values?.customerRemark || '').trim();
    if (current.includes(label)) {
      return;
    }
    const next = current ? `${current} ${label}` : label;
    basicInfoFormApi.setValues({ customerRemark: next });
  });
}

function removeRemarkTag(label: string) {
  basicInfoFormApi.getValues().then((values) => {
    const current = String(values?.customerRemark || '');
    if (!current.trim()) {
      return;
    }
    const escaped = label.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    const regex = new RegExp(`(^|\\s+)${escaped}(?=\\s+|$)`, 'g');
    const next = current
      .replace(regex, ' ')
      .replace(/\s+/g, ' ')
      .trim();
    basicInfoFormApi.setValues({ customerRemark: next });
  });
}

function toggleRemarkTag(label: string) {
  const next = new Set(remarkTagSelections.value);
  if (next.has(label)) {
    next.delete(label);
    removeRemarkTag(label);
  } else {
    next.add(label);
    appendRemarkTag(label);
  }
  remarkTagSelections.value = Array.from(next);
  applyQualificationByTags(remarkTagSelections.value, label);
}

function initRemarkTagSelections() {
  const next = new Set<string>();
  const detail = customerDetailInfoRef.value || {};
  const remark = String(detail.customerRemark || '');
  if (detail.houseFlag === 1 || detail.houseFlag === 2) {
    next.add('有房');
  }
  if (detail.carFlag === 1) {
    next.add('有车');
  }
  if (detail.socialInsuranceFlag === 1) {
    next.add('有社保');
  }
  if (detail.providentFlag === 1) {
    next.add('有公积金');
  }
  if (detail.insuranceFlag === 1) {
    next.add('有保单');
  }
  if (detail.creditCardFlag === 1) {
    next.add('有信用卡');
  }
  if (detail.enterpriseFlag === 1) {
    next.add('有营业执照');
  }
  quickRemarkTags.forEach((tag) => {
    if (!('fieldName' in tag) && remark.includes(tag.label)) {
      next.add(tag.label);
    }
  });
  remarkTagSelections.value = Array.from(next);
  setAmountInputState(next.has('有房'), next.has('有公积金'));
}

const descColumns = ref(10);

function calcDescColumns(width: number) {
  if (width < 640) {
    return 1;
  }
  if (width < 1024) {
    return 2;
  }
  if (width < 1280) {
    return 6;
  }
  return 10;
}

function updateDescColumns() {
  if (typeof window === 'undefined') {
    return;
  }
  descColumns.value = calcDescColumns(window.innerWidth);
}

function getDescSpan(span: number) {
  return descColumns.value >= 10 ? span : 1;
}

onMounted(() => {
  updateDescColumns();
  window.addEventListener('resize', updateDescColumns);
  void loadCustomerFieldConfig();
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateDescColumns);
});

function formatRegion(parts: Array<string | undefined>) {
  return parts.filter((item) => item && item.trim()).join('/');
}

function formatRegionWithDetail(
  parts: Array<string | undefined>,
  detail?: string,
) {
  const base = formatRegion(parts);
  if (!detail || !detail.trim()) {
    return base;
  }
  return base ? `${base} ${detail.trim()}` : detail.trim();
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

async function fillHukouFromIdCard(idCardNo?: string) {
  const normalized = (idCardNo || '').trim();
  if (!normalized) {
    return;
  }
  try {
    const info = await regionApi.getIdCardArea(normalized);
    if (!info) {
      return;
    }
    locationInfoRef.value = {
      ...locationInfoRef.value,
      hukouProvince: info.province || '',
      hukouCity: info.city || '',
      hukouDistrict: info.district || '',
    };
    const path = buildAreaPath([info.province, info.city, info.district]);
    locationFormApi.setValues({ hukouArea: path });
    await ensureRegionPath(path);
  } catch (error) {
    return;
  }
}

async function saveCustomerBizInfo() {
  detailModalApi.setState({ loading: true });
  const attrInfoFormData = await attrInfoFormApi.getValues();
  const basicInfoFormData = await basicInfoFormApi.getValues();
  const backgroundInfoFormData = await backgroundInfoFormApi.getValues();
  const locationFormData = await locationFormApi.getValues();
  const { hukouArea, currentArea, ...locationPlainData } = locationFormData || {};
  const leaderRemarkFormData = await leaderRemarkFormApi.getValues();
  const _formData = {
    ...attrInfoFormData,
    ...basicInfoFormData,
    ...backgroundInfoFormData,
    ...locationPlainData,
    ...locationInfoRef.value,
    ...leaderRemarkFormData,
    version: customerDetailInfoRef.value.version,
    id: customerDetailInfoRef.value.id,
  };
  customerApi
    .updateBizCustomerInfo(_formData)
    .then(() => {
      customerApi
        .getCustomerDetail(customerDetailInfoRef.value.id)
        .then((res) => {
          customerDetailInfoRef.value = res;
          initForms(canEdit.value);
        });
      message.success({ content: `保存客户信息成功`, duration: 3 });
      window.dispatchEvent(
        new CustomEvent('cust-notice-read', {
          detail: { custId: customerDetailInfoRef.value.id },
        }),
      );
    })
    .finally(() => {
      detailModalApi.setState({ loading: false });
    });
}

/**
 * 初始化下拉选项
 */
function initSelectOptions() {
  _customerGroupOptions.value = outData.value.customerGroupOptions;
  _progressOptions.value = (outData.value.progressOptions || []).map(
    (item: { label: string; value: number }) => {
      if (item.label === '成交') {
        return { ...item, disabled: true };
      }
      return item;
    },
  );
  _callTipsOptions.value = outData.value.callTipsOptions;
  _deptNameOptions.value = outData.value.deptNameOptions;
}
async function switchFavorite(cid: number, favFlag: number) {
  await customerApi.batchOptFavorite([cid], favFlag).then(() => {
    message.success({
      content: `客户信息已移动至【${FavoriteTypeEnum.getFavTypeName(favFlag)}】`,
      duration: 3,
    });
    detailModalApi.onCancel();
  });
}

/**
 * 将客户信息退回至公海
 * @param cids
 */
async function returnCustomer2Ocean(cids: number[]) {
  if (!(cids && cids.length > 0)) {
    message.error('请选择要退回公海的客户');
    return;
  }
  await customerApi.batchBackToOcean(cids).then(() => {
    message.success({ content: '客户信息退回公海成功', duration: 3 });
    detailModalApi.onCancel();
  });
}

function returnCustomer2OceanConfirm(
  cids: number[],
  name: string,
  mobile: string,
) {
  if (!(cids && cids.length > 0)) {
    message.error('请选择要退回公海的客户');
    return;
  }
  const custDesc = `客户 ${name}(${mobile}) 退回公海`;
  Modal.confirm({
    title: '退回客户信息',
    content: `确定要将${custDesc}吗?\n退回公海后，您将无法查看该客户信息，是否继续?`,
    onOk: () => {
      returnCustomer2Ocean(cids);
    },
  });
}
const [detailModal, detailModalApi] = useVbenModal({
  destroyOnClose: true,
  draggable: false,
  closeOnPressEscape: false,
  centered: false,
  closeOnClickModal: false,
  class: 'xs:w-full sm:w-4/5  w-2/3',
  title: '客户信息详情',
  showConfirmButton: false,
  cancelText: '关闭',
  onClosed() {
    if (outData.value?.callbackFnc) {
      outData.value?.callbackFnc().then(() => {
        detailModalApi.close();
      });
    }
  },
  onCancel() {
    detailModalApi.close();
  },

  onOpenChange(isOpen: boolean) {
    if (isOpen) {
      outData.value = detailModalApi.getData<Record<string, any>>();
      void loadZhimaThreshold();
      reloadCustInfo(outData.value.cid);
      refreshCidList(outData.value.cidList);
    }
  },
});
/**
 * 刷新界面信息
 * @param cid
 */
function reloadCustInfo(cid: number) {
  detailModalApi.setState({ loading: true });
  currentCid.value = cid;
  userId = outData.value.uid;
  const hasMaintainRole = outData.value.hasMaintainRole;
  // 取用户详情
    customerApi.getCustomerDetail(currentCid.value).then((res) => {
      customerDetailInfoRef.value = res;
      zhimaScoreValue.value =
        typeof res?.zhimaScore === 'number' ? res.zhimaScore : null;
      if (!customerDetailInfoRef.value.idCardNo && outData.value?.idCardNo) {
        customerDetailInfoRef.value.idCardNo = String(outData.value.idCardNo).trim();
      }
      canEdit.value = res.ownerUserId !== null && res.ownerUserId > 0;
    // 非本人的客户，且本人拥有超管或部门主管权限才可显示【上级评价】
    hideLeaderRemark.value = !(
      userId !== `${res.ownerUserId}` && hasMaintainRole
    );
    initForms(canEdit.value);
    detailModalApi.setState({ loading: false });
  });
}

function refreshCidList(recvCidList: number[]) {
  if (!recvCidList || recvCidList.length === 0) {
    cidList = [];
  }
  cidList = recvCidList;
}

function initForms(edit: boolean) {
  if (!edit) {
    return;
  }
  initSelectOptions();
  updateCustomerStarEditState();
  if (customerDetailInfoRef.value) {
    basicInfoFormApi.setValues(customerDetailInfoRef.value);
    backgroundInfoFormApi.setValues(customerDetailInfoRef.value);
    const idCardNo =
      customerDetailInfoRef.value.idCardNo || outData.value?.idCardNo;
    const hasIdCard = !!idCardNo;
    const idCardInfo = parseIdCardInfo(idCardNo);
    const hasValidIdCard = !!idCardInfo;
    attrInfoFormApi.updateSchema([
      { fieldName: 'idCardNo', disabled: hasIdCard },
      { fieldName: 'sex', disabled: hasValidIdCard },
      { fieldName: 'age', disabled: hasValidIdCard },
    ]);
    const attrValues = {
      ...customerDetailInfoRef.value,
      idCardNo,
    };
    if (idCardInfo) {
      attrValues.sex = idCardInfo.sex;
      attrValues.age = idCardInfo.age;
      attrValues.birthday =
        customerDetailInfoRef.value.birthday ?? idCardInfo.birthday;
    }
    attrInfoFormApi.setValues(attrValues);
    zhimaScoreValue.value =
      typeof attrValues.zhimaScore === 'number' ? attrValues.zhimaScore : null;
    initRemarkTagSelections();
    locationInfoRef.value = {
      hukouProvince: customerDetailInfoRef.value.hukouProvince || '',
      hukouCity: customerDetailInfoRef.value.hukouCity || '',
      hukouDistrict: customerDetailInfoRef.value.hukouDistrict || '',
      currentProvince: customerDetailInfoRef.value.currentProvince || '',
      currentCity: customerDetailInfoRef.value.currentCity || '',
      currentDistrict: customerDetailInfoRef.value.currentDistrict || '',
      currentStreet: customerDetailInfoRef.value.currentStreet || '',
    };
    locationFormApi.setValues({
      hukouArea: buildAreaPath([
        customerDetailInfoRef.value.hukouProvince,
        customerDetailInfoRef.value.hukouCity,
        customerDetailInfoRef.value.hukouDistrict,
      ]),
      hukouAddressDetail: customerDetailInfoRef.value.hukouAddressDetail,
      currentArea: buildAreaPath([
        customerDetailInfoRef.value.currentProvince,
        customerDetailInfoRef.value.currentCity,
        customerDetailInfoRef.value.currentDistrict,
        customerDetailInfoRef.value.currentStreet,
      ]),
      currentAddressDetail: customerDetailInfoRef.value.currentAddressDetail,
    });
    void initLocationForms();
    leaderRemarkFormApi.setValues({ leaderRemark: '' });
  }
}

async function loadCustomerFieldConfig() {
  try {
    const list = await customerApi.getCustomerFieldConfig();
    const keys = (list || [])
      .map((item) => item.fieldKey)
      .filter(Boolean);
    if (keys.length > 0) {
      customerStarEditable.value = keys.includes('customerGroupDesc');
    } else {
      customerStarEditable.value = true;
    }
  } catch (error) {
    customerStarEditable.value = true;
  }
  updateCustomerStarEditState();
}

function updateCustomerStarEditState() {
  basicInfoFormApi.updateSchema([
    {
      fieldName: 'customerGroup',
      componentProps: {
        disabled: !customerStarEditable.value,
      },
    },
  ]);
}

async function initLocationForms() {
  const hukouPath = buildAreaPath([
    customerDetailInfoRef.value.hukouProvince,
    customerDetailInfoRef.value.hukouCity,
    customerDetailInfoRef.value.hukouDistrict,
  ]);
  const currentPath = buildAreaPath([
    customerDetailInfoRef.value.currentProvince,
    customerDetailInfoRef.value.currentCity,
    customerDetailInfoRef.value.currentDistrict,
    customerDetailInfoRef.value.currentStreet,
  ]);
  await initRegionOptions();
  await ensureRegionPath(hukouPath);
  await ensureRegionPath(currentPath);
}

// 编辑客户基本信息
const [EditCustBasicInfoForm, basicInfoFormApi] = useVbenForm({
  compact: true,
  commonConfig: {
    componentProps: {
      class: 'w-full',
      colon: true,
    },
  },
  wrapperClass: 'grid-cols-3 text-left',
  showDefaultActions: false,
  layout: 'horizontal',
  schema: [
    {
      component: 'Input',
      fieldName: 'reqLoanAmount',
      label: '申请贷款金额',
      formItemClass: 'left',
      componentProps: {
        placeholder: '请输入金额',
      },
      colon: true,
      suffix: '元',
    },
    {
      component: 'Select',
      fieldName: 'callTips',
      label: '沟通结果',
      componentProps: {
        placeholder: '请选择',
        options: _callTipsOptions,
        style: 'max-width: 220px',
      },
      colon: true,
    },
    {
      component: 'Select',
      fieldName: 'progress',
      label: '跟进状态',
      componentProps: {
        placeholder: '请选择',
        options: _progressOptions,
        style: 'max-width: 220px',
      },
      colon: true,
    },
    {
      component: 'Select',
      fieldName: 'customerGroup',
      label: '星级',
      formItemClass: 'text-left',
      componentProps: { placeholder: '请选择', options: _customerGroupOptions },
      colon: true,
    },
    {
      component: 'Input',
      fieldName: 'houseVal',
      label: '房产价值',
      componentProps: {
        placeholder: '请输入房产价值，单位万元',
        disabled: true,
      },
      colon: true,
      suffix: '万元',
    },
    {
      component: 'Input',
      fieldName: 'providentAmountYuan',
      label: '公积金金额',
      componentProps: {
        placeholder: '请输入公积金金额，单位元',
        disabled: true,
      },
      colon: true,
      suffix: '元',
    },
    {
      component: 'Textarea',
      fieldName: 'customerRemark',
      label: '客户备注',
      componentProps: { placeholder: '请输入客户备注' },
      colon: true,
      formItemClass: 'col-span-3',
      wrapperClass: 'grid-cols-1',
      labelClass: 'text-left',
    },
  ],
});
// 编辑客户背景信息
const [EditCustBackgroundInfoForm, backgroundInfoFormApi] = useVbenForm({
  compact: true,
  commonConfig: {
    componentProps: {
      class: 'w-full',
      colon: true,
    },
  },
  wrapperClass: 'grid-cols-3',
  showDefaultActions: false,
  layout: 'horizontal',
  schema: [
    {
      component: 'Select',
      fieldName: 'houseFlag',
      label: '房产',
      componentProps: { placeholder: '请选择', options: houseFlagOptions },
      colon: true,
    },

    {
      component: 'Select',
      fieldName: 'socialInsuranceFlag',
      label: '社保',
      componentProps: { placeholder: '请选择', options: gotNoOptions },
      colon: true,
    },

    {
      component: 'Select',
      fieldName: 'providentFlag',
      label: '公积金',
      componentProps: { placeholder: '请选择', options: gotNoOptions },
      colon: true,
    },
    {
      component: 'Select',
      fieldName: 'carFlag',
      label: '车产',
      componentProps: { placeholder: '请选择', options: gotNoOptions },
      colon: true,
    },
    {
      component: 'Select',
      fieldName: 'insuranceFlag',
      label: '保单',
      componentProps: { placeholder: '请选择', options: gotNoOptions },
      colon: true,
    },
    {
      component: 'Select',
      fieldName: 'creditCardFlag',
      label: '信用卡',
      componentProps: { placeholder: '请选择', options: gotNoOptions },
      colon: true,
    },
    {
      component: 'Select',
      fieldName: 'enterpriseFlag',
      label: '企业主',
      componentProps: { placeholder: '请选择', options: yesNoOptions },
      colon: true,
    },
  ],
});
// 编辑客户属性信息
const [EditCustAttrInfoForm, attrInfoFormApi] = useVbenForm({
  compact: true,
  commonConfig: {
    componentProps: {
      class: 'w-full',
      colon: true,
    },
  },
  wrapperClass: 'grid-cols-1 sm:grid-cols-2 lg:grid-cols-10 text-left',
  showDefaultActions: false,
  layout: 'horizontal',
  schema: [
    {
      component: 'Input',
      fieldName: 'idCardNo',
      label: '身份证号',
      componentProps: {
        placeholder: '请输入身份证号',
        style: 'max-width: 260px',
        onBlur: (event: FocusEvent) => {
          const value = (event.target as HTMLInputElement)?.value;
          const info = parseIdCardInfo(value);
          if (info) {
            attrInfoFormApi.getValues().then((values) => {
              attrInfoFormApi.setValues({
                ...values,
                sex: info.sex,
                age: info.age,
                birthday: info.birthday,
              });
            });
            attrInfoFormApi.updateSchema([
              { fieldName: 'sex', disabled: true },
              { fieldName: 'age', disabled: true },
            ]);
          } else {
            attrInfoFormApi.updateSchema([
              { fieldName: 'sex', disabled: false },
              { fieldName: 'age', disabled: false },
            ]);
          }
          void fillHukouFromIdCard(value);
        },
      },
      colon: true,
      formItemClass: 'col-span-full sm:col-span-2 lg:col-span-3 pl-2',
    },
    {
      component: 'Select',
      fieldName: 'sex',
      label: '性别',
      componentProps: {
        placeholder: '请指定性别',
        options: [
          { label: '保密', value: 0 },
          { label: '男', value: 1 },
          { label: '女', value: 2 },
        ],
      },
      colon: true,
      formItemClass: 'col-span-full sm:col-span-1 lg:col-span-2 pl-2',
    },
    {
      component: 'Input',
      fieldName: 'age',
      label: '年龄',
      componentProps: {
        placeholder: '请输入年龄',
        style: 'max-width: 120px',
      },
      colon: true,
      formItemClass: 'col-span-full sm:col-span-1 lg:col-span-2 pl-2',
    },
    {
      component: 'InputNumber',
      fieldName: 'zhimaScore',
      label: '芝麻分',
      componentProps: {
        placeholder: '请输入芝麻分',
        min: 0,
        max: 950,
        class: 'zhima-score-input',
        onChange: (value: number | null) => {
          zhimaScoreValue.value = typeof value === 'number' ? value : null;
        },
      },
      colon: true,
      formItemClass: 'col-span-full sm:col-span-2 lg:col-span-3',
    },
    {
      component: 'Select',
      fieldName: 'marriageStatus',
      label: '婚姻',
      componentProps: { placeholder: '请选择', options: marriageStatusOptions },
      colon: true,
      formItemClass: 'col-span-full sm:col-span-1 lg:col-span-2',
    },
    {
      component: 'Input',
      fieldName: 'birthday',
      label: '出生日期',
      componentProps: { placeholder: 'yyyy-MM-dd 格式' },
      colon: true,
      formItemClass: 'col-span-full sm:col-span-1 lg:col-span-3',
    },
    {
      component: 'Input',
      fieldName: 'mobileArea',
      label: '手机号归属地',
      componentProps: {
        placeholder: '自动识别手机号归属地',
        disabled: true,
      },
      colon: true,
      formItemClass: 'col-span-full sm:col-span-2 lg:col-span-5',
    },
  ],
});

function syncHukouArea(values: string[] = [], selectedOptions: RegionOption[] = []) {
  const limited = values.slice(0, 3);
  if (values.length > 3) {
    locationFormApi.setValues({ hukouArea: limited });
  }
  const labels =
    selectedOptions.length > 0
      ? selectedOptions.map((item) => item.label || item.value)
      : limited;
  locationInfoRef.value = {
    ...locationInfoRef.value,
    hukouProvince: labels[0] || '',
    hukouCity: labels[1] || '',
    hukouDistrict: labels[2] || '',
  };
}

function syncCurrentArea(values: string[] = [], selectedOptions: RegionOption[] = []) {
  const limited = values.slice(0, 4);
  if (values.length > 4) {
    locationFormApi.setValues({ currentArea: limited });
  }
  const labels =
    selectedOptions.length > 0
      ? selectedOptions.map((item) => item.label || item.value)
      : limited;
  locationInfoRef.value = {
    ...locationInfoRef.value,
    currentProvince: labels[0] || '',
    currentCity: labels[1] || '',
    currentDistrict: labels[2] || '',
    currentStreet: labels[3] || '',
  };
}

const [EditCustLocationInfoForm, locationFormApi] = useVbenForm({
  compact: true,
  commonConfig: {
    componentProps: {
      class: 'w-full',
      colon: true,
    },
  },
  wrapperClass: 'grid-cols-4 text-left',
  showDefaultActions: false,
  layout: 'horizontal',
  schema: [
    {
      component: 'Cascader',
      fieldName: 'hukouArea',
      label: '户籍所在地',
      componentProps: {
        options: regionOptions,
        allowClear: true,
        changeOnSelect: true,
        showSearch: true,
        loadData: loadRegionChildren,
        onChange: (values: string[], selectedOptions: RegionOption[]) => {
          syncHukouArea(values, selectedOptions);
        },
      },
      colon: true,
      formItemClass: 'col-span-2',
    },
    {
      component: 'Input',
      fieldName: 'hukouAddressDetail',
      label: '户籍门牌号',
      componentProps: { placeholder: '请输入楼门号' },
      colon: true,
      formItemClass: 'col-span-2',
    },
    {
      component: 'Cascader',
      fieldName: 'currentArea',
      label: '当前所在地',
      componentProps: {
        options: regionOptions,
        allowClear: true,
        changeOnSelect: true,
        showSearch: true,
        loadData: loadRegionChildren,
        onChange: (values: string[], selectedOptions: RegionOption[]) => {
          syncCurrentArea(values, selectedOptions);
        },
      },
      colon: true,
      formItemClass: 'col-span-2',
    },
    {
      component: 'Input',
      fieldName: 'currentAddressDetail',
      label: '当前门牌号',
      componentProps: { placeholder: '请输入楼门号' },
      colon: true,
      formItemClass: 'col-span-2',
    },
  ],
});
// 主管评价
const [EditLeaderRemarkInfoForm, leaderRemarkFormApi] = useVbenForm({
  compact: true,
  commonConfig: {
    componentProps: {
      // class: 'w-full',
      colon: true,
    },
  },
  showDefaultActions: false,
  layout: 'vertical',
  schema: [
    {
      component: 'Textarea',
      fieldName: 'leaderRemark',
      label: '主管评价',
      componentProps: {
        placeholder: '请输入主管评价。 评价内容不可修改只能追加，请谨慎填写。',
        hidden: hideLeaderRemark,
      },
      colon: true,
      hideLabel: true,
    },
  ],
  wrapperClass: 'grid-cols-1',
});
</script>
<template>
  <detailModal>
    <div class="detail-wrap">
      <Row :gutter="16" class="detail-row">
        <Col :flex="'1 1 0'" class="detail-col detail-left">
          <div class="section-card">
            <div class="section-head">
              <h3 class="section-title">客户信息</h3>
              <div class="header-actions">
                <Button
                  size="small"
                  type="link"
                  danger
                  @click="
                    returnCustomer2OceanConfirm(
                      [customerDetailInfoRef.id],
                      customerDetailInfoRef.name,
                      customerDetailInfoRef.mobile,
                    )
                  "
                  v-if="userId === `${customerDetailInfoRef.ownerUserId}`"
                >
                  移入公海
                </Button>
                <Button
                  size="small"
                  type="link"
                  @click="
                    switchFavorite(
                      customerDetailInfoRef.id,
                      FavoriteTypeEnum.MY_CUST,
                    )
                  "
                  v-if="
                    customerDetailInfoRef.ownerFavorite !==
                      FavoriteTypeEnum.MY_CUST &&
                    userId === `${customerDetailInfoRef.ownerUserId}`
                  "
                >
                  移入我的客户
                </Button>
                <Button
                  size="small"
                  type="link"
                  @click="
                    switchFavorite(
                      customerDetailInfoRef.id,
                      FavoriteTypeEnum.KEY_CUST,
                    )
                  "
                  v-if="
                    customerDetailInfoRef.ownerFavorite !==
                      FavoriteTypeEnum.KEY_CUST &&
                    userId === `${customerDetailInfoRef.ownerUserId}`
                  "
                >
                  移入重点客户
                </Button>
                <Button
                  size="small"
                  type="link"
                  @click="
                    switchFavorite(
                      customerDetailInfoRef.id,
                      FavoriteTypeEnum.NORMAL,
                    )
                  "
                  v-if="
                    customerDetailInfoRef.ownerFavorite !==
                      FavoriteTypeEnum.NORMAL &&
                    userId === `${customerDetailInfoRef.ownerUserId}`
                  "
                >
                  移入再分配客户
                </Button>
                <Button
                  type="link"
                  @click="reloadCustInfo(prevCid)"
                  size="small"
                  :disabled="prevCid < 0"
                >
                  &lt;&lt;上一条
                </Button>
                <Button
                  type="link"
                  @click="reloadCustInfo(nextCid)"
                  size="small"
                  :disabled="nextCid < 0"
                >
                  下一条&gt;&gt;
                </Button>
              </div>
            </div>
            <Descriptions :column="4" class="dense-desc">
              <Descriptions.Item label="姓名">
                {{ customerDetailInfoRef?.name }}
              </Descriptions.Item>
              <Descriptions.Item label="手机号">
                {{ customerDetailInfoRef?.mobile }}
              </Descriptions.Item>
              <Descriptions.Item label="申请时间" :span="2">
                {{ customerDetailInfoRef?.applyDate }}
              </Descriptions.Item>
            </Descriptions>
            <Descriptions :column="descColumns" v-if="!canEdit" class="dense-desc">
              <Descriptions.Item label="身份证号" :span="getDescSpan(3)">
                {{ customerDetailInfoRef?.idCardNo }}
              </Descriptions.Item>
              <Descriptions.Item label="性别" :span="getDescSpan(2)">
                {{ getDerivedSexDesc() }}
              </Descriptions.Item>
              <Descriptions.Item label="年龄" :span="getDescSpan(2)">
                {{ getDerivedAge() }}
              </Descriptions.Item>
              <Descriptions.Item label="芝麻分" :span="getDescSpan(3)">
                <span :class="isZhimaHigh ? 'zhima-score-highlight' : ''">
                  {{ customerDetailInfoRef?.zhimaScore ?? '' }}
                </span>
              </Descriptions.Item>
              <Descriptions.Item label="婚姻" :span="getDescSpan(2)">
                {{ customerDetailInfoRef?.marriageStatusDesc }}
              </Descriptions.Item>
              <Descriptions.Item label="出生日期" :span="getDescSpan(3)">
                {{ customerDetailInfoRef?.birthday }}
              </Descriptions.Item>
              <Descriptions.Item label="手机号归属地" :span="getDescSpan(5)">
                {{ customerDetailInfoRef?.mobileArea }}
              </Descriptions.Item>
              <Descriptions.Item label="户籍所在地" :span="getDescSpan(5)">
                {{
                  formatRegionWithDetail(
                    [
                      customerDetailInfoRef?.hukouProvince,
                      customerDetailInfoRef?.hukouCity,
                      customerDetailInfoRef?.hukouDistrict,
                    ],
                    customerDetailInfoRef?.hukouAddressDetail,
                  )
                }}
              </Descriptions.Item>
              <Descriptions.Item label="当前所在地" :span="getDescSpan(5)">
                {{
                  formatRegionWithDetail(
                    [
                      customerDetailInfoRef?.currentProvince,
                      customerDetailInfoRef?.currentCity,
                      customerDetailInfoRef?.currentDistrict,
                      customerDetailInfoRef?.currentStreet,
                    ],
                    customerDetailInfoRef?.currentAddressDetail,
                  )
                }}
              </Descriptions.Item>
            </Descriptions>
            <div class="form-block" v-if="canEdit" :class="{ 'is-zhima-high': isZhimaHigh }">
              <EditCustAttrInfoForm />
              <Divider />
              <EditCustLocationInfoForm />
            </div>
          </div>

          <div class="section-card">
            <h3 class="section-title">基本信息</h3>
            <Descriptions :column="3" class="dense-desc" v-if="!canEdit">
              <Descriptions.Item label="申请贷款金额">
                {{
                  customerDetailInfoRef?.reqLoanAmount
                    ? `${customerDetailInfoRef?.reqLoanAmount}元`
                    : ''
                }}
              </Descriptions.Item>
              <Descriptions.Item label="沟通结果">
                {{ customerDetailInfoRef?.callTipsDesc }}
              </Descriptions.Item>
              <Descriptions.Item label="跟进状态">
                {{ customerDetailInfoRef?.progressDesc }}
              </Descriptions.Item>
              <Descriptions.Item label="客户星级">
                {{ customerDetailInfoRef?.customerGroupDesc }}
              </Descriptions.Item>
              <Descriptions.Item label="房产价值">
                {{
                  customerDetailInfoRef?.houseVal
                    ? `${customerDetailInfoRef?.houseVal} 万元`
                    : ''
                }}
              </Descriptions.Item>
              <Descriptions.Item label="公积金金额">
                {{
                  customerDetailInfoRef?.providentAmountYuan
                    ? `${customerDetailInfoRef?.providentAmountYuan} 元`
                    : ''
                }}
              </Descriptions.Item>
            </Descriptions>
            <div class="remark-block" v-if="!canEdit">
              <div class="remark-label">客户备注</div>
              <div class="remark-content">
                {{ customerDetailInfoRef?.customerRemark }}
              </div>
            </div>
            <div class="form-block" v-if="canEdit">
              <EditCustBasicInfoForm />
              <div class="remark-tags">
                <Tag
                  v-for="tag in quickRemarkTags"
                  :key="tag.label"
                  class="cursor-pointer"
                  :color="isRemarkTagSelected(tag.label) ? 'blue' : undefined"
                  @click="toggleRemarkTag(tag.label)"
                >
                  {{ tag.label }}
                </Tag>
              </div>
              <div class="hidden">
                <EditCustBackgroundInfoForm />
              </div>
            </div>
          </div>

        </Col>

        <Col :flex="'0 1 clamp(300px, 30vw, 440px)'" class="detail-col detail-right">
          <div class="section-card">
            <h3 class="section-title">主管评价</h3>
            <div class="form-block" v-if="canEdit">
              <EditLeaderRemarkInfoForm />
            </div>
            <div class="list-scroll">
              <template
                v-for="leaderRemark in customerDetailInfoRef?.leaderRemarkList"
                :key="leaderRemark.id"
              >
                <Row class="list-item">
                  <Col :span="4" class="list-author">
                    {{ leaderRemark.optUserName }}
                  </Col>
                  <Col :span="14" class="list-content">
                    <template
                      v-for="desc in leaderRemark.descList"
                      :key="desc.id"
                    >
                      {{ desc?.replace('[上级评价]:', '') }}
                    </template>
                  </Col>
                  <Col :span="6" class="list-time">
                    {{ leaderRemark.updTime }}
                  </Col>
                </Row>
              </template>
            </div>
          </div>

          <div class="section-card">
            <h3 class="section-title">跟进历史</h3>
            <div class="list-scroll list-scroll--tall">
              <template
                v-for="trace in customerDetailInfoRef?.progressList"
                :key="trace.id"
              >
                <Row class="list-item">
                  <Col :span="4" class="list-author">
                    {{ trace.optUserName }}
                  </Col>
                  <Col :span="14" class="list-content">
                    <template v-for="desc in trace.descList" :key="desc.id">
                      {{ desc?.replace(/更新.*:/g, '') }}<br />
                    </template>
                  </Col>
                  <Col :span="6" class="list-time">
                    {{ trace.updTime }}
                  </Col>
                </Row>
              </template>
            </div>
          </div>
        </Col>
      </Row>
    </div>
    <template #prepend-footer>
      <div v-if="canEdit" class="footer-actions">
        <Button type="default" @click="initForms(canEdit)">
          重置
        </Button>
        <Button type="primary" @click="saveCustomerBizInfo()">
          保存
        </Button>
      </div>
    </template>
  </detailModal>
</template>
<style scoped>
.detail-wrap {
  width: 100%;
}

.detail-row {
  align-items: flex-start;
}

.detail-col {
  min-width: 0;
}

.detail-left {
  min-width: 560px;
}

.detail-right {
  min-width: 320px;
  max-width: 460px;
}

.footer-actions {
  display: flex;
  gap: 8px;
  margin-right: 8px;
}

.section-card {
  border: 1px solid hsl(var(--border));
  border-radius: 12px;
  background: hsl(var(--card));
  padding: 16px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.06);
  margin-bottom: 16px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: hsl(var(--foreground));
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 12px;
}

.section-head .section-title {
  margin: 0;
}

.section-title::before {
  content: '';
  width: 4px;
  height: 14px;
  border-radius: 2px;
  background: hsl(var(--primary));
}

.header-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
  justify-content: flex-end;
}

.dense-desc {
  margin-bottom: 8px;
}

.form-block {
  margin-top: 8px;
}

.remark-block {
  border: 1px solid hsl(var(--border));
  border-radius: 10px;
  padding: 12px;
  background: hsl(var(--accent));
  margin-top: 10px;
}

.remark-label {
  font-size: 12px;
  color: hsl(var(--muted-foreground));
  margin-bottom: 6px;
}

.remark-content {
  color: hsl(var(--foreground));
  white-space: pre-wrap;
}

.remark-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
  padding-left: 100px;
  box-sizing: border-box;
}

.list-scroll {
  max-height: 220px;
  overflow-y: auto;
}

.list-scroll--tall {
  max-height: 320px;
}

.list-item {
  margin: 8px 0;
  border-radius: 10px;
  border: 1px solid hsl(var(--border));
  background: hsl(var(--accent));
  padding: 10px 12px;
  line-height: 1.5;
}

.list-author {
  font-size: 12px;
  color: hsl(var(--primary));
  white-space: nowrap;
}

.list-content {
  font-size: 12px;
  color: hsl(var(--foreground));
}

.list-time {
  font-size: 12px;
  color: hsl(var(--muted-foreground));
  text-align: right;
  white-space: nowrap;
}

.zhima-score-highlight {
  display: inline-flex;
  align-items: center;
  padding: 0 6px;
  border-radius: 4px;
  color: #b91c1c;
  background: rgba(248, 113, 113, 0.2);
  font-weight: 600;
}

.form-block.is-zhima-high :deep(.zhima-score-input) {
  border-radius: 6px;
  background: rgba(248, 113, 113, 0.2);
}

.form-block.is-zhima-high :deep(.zhima-score-input .ant-input-number-input) {
  color: #b91c1c;
}

:global(.dark) .zhima-score-highlight,
:global([data-theme='dark']) .zhima-score-highlight {
  color: #fecaca;
  background: rgba(248, 113, 113, 0.25);
}

:global(.dark) .form-block.is-zhima-high :deep(.zhima-score-input),
:global([data-theme='dark']) .form-block.is-zhima-high :deep(.zhima-score-input) {
  background: rgba(248, 113, 113, 0.25);
}

:global(.dark) .form-block.is-zhima-high :deep(.zhima-score-input .ant-input-number-input),
:global([data-theme='dark'])
  .form-block.is-zhima-high
  :deep(.zhima-score-input .ant-input-number-input) {
  color: #fecaca;
}

:deep(.ant-descriptions-item-label) {
  color: hsl(var(--muted-foreground));
}

:deep(.ant-descriptions-item-content) {
  color: hsl(var(--foreground));
}

.list-scroll::-webkit-scrollbar {
  width: 6px;
}

.list-scroll::-webkit-scrollbar-thumb {
  background: #a8a2a2;
  border-radius: 3px;
}

@media (max-width: 1024px) {
  .detail-row {
    flex-direction: column;
  }

  .detail-left,
  .detail-right {
    min-width: 0;
    max-width: none;
    width: 100%;
  }

  .detail-right {
    flex: 1 1 0 !important;
  }
}
</style>
