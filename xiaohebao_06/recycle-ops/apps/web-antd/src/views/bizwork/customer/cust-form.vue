<script lang="ts" setup>
import type { CustomerItemDetail } from '#/api/biz/customer';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import {
  Button,
  Col,
  Descriptions,
  message,
  Modal,
  Row,
  Tag,
} from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  customerApi,
  gotNoOptions,
  houseFlagOptions,
  yesNoOptions,
} from '#/api/biz/customer';

const outData = ref();
const _progressOptions = ref<{ label: string; value: number }[]>([]);
const _callTipsOptions = ref<{ label: string; value: number }[]>([]);
const canEdit = ref<boolean>(false);
const customerDetailInfoRef = ref<CustomerItemDetail>({});
const zhimaThreshold = ref<number | null>(null);
const zhimaScoreValue = ref<number | null>(null);
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
  if (
    monthDiff < 0 ||
    (monthDiff === 0 && today.getDate() < birthDate.getDate())
  ) {
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

function displayText(value?: null | number | string) {
  return value === 0 || value ? String(value) : '';
}

function displayZhima() {
  return (
    customerDetailInfoRef.value?.hyyZhimaDesc ||
    displayText(customerDetailInfoRef.value?.zhimaScore)
  );
}

function displayLoanAmount() {
  return (
    customerDetailInfoRef.value?.hyyLoanAmountDesc ||
    displayText(customerDetailInfoRef.value?.reqLoanAmount)
  );
}

function displayBirthday() {
  return (
    customerDetailInfoRef.value?.birthday ||
    derivedIdCardInfo.value?.birthday ||
    ''
  );
}

function displayHukou() {
  return formatRegion([
    customerDetailInfoRef.value?.hukouProvince,
    customerDetailInfoRef.value?.hukouCity,
    customerDetailInfoRef.value?.hukouDistrict,
  ]);
}

function displayHouseVal() {
  return customerDetailInfoRef.value?.houseVal
    ? `${customerDetailInfoRef.value.houseVal} 万元`
    : '';
}

function displayProvidentAmount() {
  return customerDetailInfoRef.value?.providentAmountYuan
    ? `${customerDetailInfoRef.value.providentAmountYuan} 元`
    : '';
}

function simpleItem(label: string, value?: null | number | string) {
  return {
    label,
    value: displayText(value),
  };
}

const customerInfoItems = computed(() => [
  simpleItem('姓名', customerDetailInfoRef.value?.name),
  simpleItem('手机号', customerDetailInfoRef.value?.mobile),
  simpleItem('申请时间', customerDetailInfoRef.value?.applyDate),
  simpleItem('身份证号', customerDetailInfoRef.value?.idCardNo),
  simpleItem('性别', getDerivedSexDesc()),
  simpleItem('年龄', getDerivedAge()),
  simpleItem('芝麻分', displayZhima()),
  simpleItem('婚姻', customerDetailInfoRef.value?.marriageStatusDesc),
  simpleItem('出生日期', displayBirthday()),
  simpleItem('手机号归属地', customerDetailInfoRef.value?.mobileArea),
  simpleItem('户籍所在地', displayHukou()),
]);

const basicInfoItems = computed(() => [
  simpleItem('逾期情况', customerDetailInfoRef.value?.hyyOverdueDesc),
  simpleItem('申请贷款金额', displayLoanAmount()),
  simpleItem('跟进状态', customerDetailInfoRef.value?.progressDesc),
  simpleItem(
    '社保',
    customerDetailInfoRef.value?.hyySocialInsuranceDesc ||
      customerDetailInfoRef.value?.socialInsuranceFlagDesc,
  ),
  simpleItem('芝麻分', displayZhima()),
  simpleItem(
    '公积金',
    customerDetailInfoRef.value?.hyyProvidentDesc ||
      customerDetailInfoRef.value?.providentFlagDesc,
  ),
  simpleItem(
    '房',
    customerDetailInfoRef.value?.hyyHouseDesc ||
      customerDetailInfoRef.value?.houseFlagDesc,
  ),
  simpleItem(
    '车',
    customerDetailInfoRef.value?.hyyCarDesc ||
      customerDetailInfoRef.value?.carFlagDesc,
  ),
  simpleItem('职业', customerDetailInfoRef.value?.hyyOccupationDesc),
  simpleItem(
    '投保',
    customerDetailInfoRef.value?.hyyInsuranceDesc ||
      customerDetailInfoRef.value?.insuranceFlagDesc,
  ),
  simpleItem('IP地址', customerDetailInfoRef.value?.hyyIp),
  simpleItem('沟通结果', customerDetailInfoRef.value?.callTipsDesc),
  simpleItem('房产价值', displayHouseVal()),
  simpleItem('公积金金额', displayProvidentAmount()),
]);

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
        [tag.fieldName]: selected ? (tag.valueOn ?? 1) : 0,
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
    const next = current.replace(regex, ' ').replace(/\s+/g, ' ').trim();
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

function formatRegion(parts: Array<string | undefined>) {
  return parts.filter((item) => item && item.trim()).join('/');
}

async function saveCustomerBizInfo() {
  detailModalApi.setState({ loading: true });
  const basicInfoFormData = await basicInfoFormApi.getValues();
  const backgroundInfoFormData = await backgroundInfoFormApi.getValues();
  const _formData = {
    ...basicInfoFormData,
    ...backgroundInfoFormData,
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
  _progressOptions.value = (outData.value.progressOptions || []).map(
    (item: { label: string; value: number }) => {
      if (item.label === '成交') {
        return { ...item, disabled: true };
      }
      return item;
    },
  );
  _callTipsOptions.value = outData.value.callTipsOptions;
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
  class: 'xs:w-full detail-modal-compact',
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
  // 取用户详情
  customerApi.getCustomerDetail(currentCid.value).then((res) => {
    customerDetailInfoRef.value = res;
    zhimaScoreValue.value =
      typeof res?.zhimaScore === 'number' ? res.zhimaScore : null;
    if (!customerDetailInfoRef.value.idCardNo && outData.value?.idCardNo) {
      customerDetailInfoRef.value.idCardNo = String(
        outData.value.idCardNo,
      ).trim();
    }
    canEdit.value = res.ownerUserId !== null && res.ownerUserId > 0;
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
  if (customerDetailInfoRef.value) {
    basicInfoFormApi.setValues(customerDetailInfoRef.value);
    backgroundInfoFormApi.setValues(customerDetailInfoRef.value);
    zhimaScoreValue.value =
      typeof customerDetailInfoRef.value.zhimaScore === 'number'
        ? customerDetailInfoRef.value.zhimaScore
        : null;
    initRemarkTagSelections();
  }
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
            <Descriptions
              :column="3"
              class="dense-desc compact-desc"
              size="small"
            >
              <Descriptions.Item
                v-for="item in customerInfoItems"
                :key="item.label"
                :label="item.label"
              >
                <span
                  :class="
                    item.label === '芝麻分' && isZhimaHigh
                      ? 'zhima-score-highlight'
                      : ''
                  "
                >
                  {{ item.value }}
                </span>
              </Descriptions.Item>
            </Descriptions>
          </div>

          <div class="section-card">
            <h3 class="section-title">基本信息</h3>
            <Descriptions
              :column="3"
              class="dense-desc compact-desc hyy-desc"
              size="small"
            >
              <Descriptions.Item
                v-for="item in basicInfoItems"
                :key="item.label"
                :label="item.label"
              >
                {{ item.value }}
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

        <Col :flex="'0 0 300px'" class="detail-col detail-right">
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
        <Button type="default" @click="initForms(canEdit)"> 重置 </Button>
        <Button type="primary" @click="saveCustomerBizInfo()"> 保存 </Button>
      </div>
    </template>
  </detailModal>
</template>
<style scoped>
:global(.detail-modal-compact) {
  width: min(980px, calc(100vw - 32px)) !important;
}

.detail-wrap {
  width: 100%;
}

.detail-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 12px;
  align-items: flex-start;
  margin-left: 0 !important;
  margin-right: 0 !important;
}

.detail-col {
  display: block;
  flex: none !important;
  min-width: 0;
  max-width: none;
  padding-left: 0 !important;
  padding-right: 0 !important;
}

.detail-left {
  min-width: 0;
}

.detail-right {
  min-width: 0;
  max-width: 300px;
}

.footer-actions {
  display: flex;
  gap: 8px;
  margin-right: 8px;
}

.section-card {
  border: 1px solid hsl(var(--border));
  border-radius: 8px;
  background: hsl(var(--card));
  padding: 12px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.06);
  margin-bottom: 12px;
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
  margin-bottom: 6px;
}

.compact-desc :deep(.ant-descriptions-row > th),
.compact-desc :deep(.ant-descriptions-row > td) {
  padding-bottom: 6px;
}

.compact-desc :deep(.ant-descriptions-item-label),
.compact-desc :deep(.ant-descriptions-item-content) {
  font-size: 12px;
  line-height: 1.5;
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
  max-height: 560px;
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
:global([data-theme='dark'])
  .form-block.is-zhima-high
  :deep(.zhima-score-input) {
  background: rgba(248, 113, 113, 0.25);
}

:global(.dark)
  .form-block.is-zhima-high
  :deep(.zhima-score-input .ant-input-number-input),
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
    grid-template-columns: 1fr;
  }

  .detail-left,
  .detail-right {
    min-width: 0;
    max-width: none;
    width: 100%;
  }

  .detail-right {
    max-width: none;
  }
}
</style>
