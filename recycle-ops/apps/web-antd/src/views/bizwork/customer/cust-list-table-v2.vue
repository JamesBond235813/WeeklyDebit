<script lang="ts" setup>
import type {
  TableColumnType,
  TablePaginationConfig,
  TableProps,
} from 'ant-design-vue';

import type { ComputedRef } from 'vue';

import type { PagedInfo } from '#/api/biz/biz-common';
import type {
  BizDictItem,
  CustomerFieldConfigItem,
  CustomerItem,
} from '#/api/biz/customer';
import type { DeptItem } from '#/api/biz/dept';
import type { UserInfoTeamMember } from '#/api/biz/user';

import {
  computed,
  nextTick,
  onBeforeUnmount,
  onMounted,
  reactive,
  ref,
  watch,
} from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { useAccess } from '@vben/access';
import { useVbenModal } from '@vben/common-ui';
import { AdQuestion } from '@vben/icons';
import { useUserStore } from '@vben/stores';

import { Button, message, Modal, Table, Tooltip } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  customerApi,
  FavoriteTypeEnum,
  HIGHT_LIGHT_PROGRESS_VALUES,
} from '#/api/biz/customer';
import { deptApi } from '#/api/biz/dept';
import { userApi } from '#/api/biz/user';

import ExtraModal from './cust-form.vue';
import DispatchCustModal from './dispatch-cust-modal.vue';

const props = defineProps({
  title: {
    type: String,
    default: '客户信息',
  },
  favoriteFlag: {
    // 收藏标识,用于区分 我的客户、重点客户以及再分配客户
    type: Number,
    default: null,
  },
  publicPoolOnly: {
    type: Boolean,
    default: false,
  },
});
const tableLoading = ref<boolean>(false);
const userStore = useUserStore();
const access = useAccess();
const userId = userStore.userInfo?.userId;
const route = useRoute();
const router = useRouter();
const lastOpenCid = ref<null | number>(null);

// 用于控制下属部门操作的标识
const hasMaintainRole = access.hasAccessByRoles([
  'ROLE_SUPPER',
  'ROLE_DEPT_DATA_ADMIN',
]);
// 是否隐藏管理员使用的搜索条件
const hideAdminSearchCondition: ComputedRef<boolean> = computed(() => {
  return !hasMaintainRole || props.favoriteFlag !== null;
});
const DATA_TIME_FMT = 'YYYY-MM-DD HH:mm:ss';
const customerGroupOptions = ref<{ label: string; value: number }[]>([]);
const callTipsOptions = ref<{ label: string; value: number }[]>([]);
const progressOptions = ref<{ label: string; value: number }[]>([]);
const deptNameOptions = ref<{ label: string; value: number }[]>([]);
const userNameOptions = ref<{ label: string; value: number }[]>([]);
const channelOptions = ref<{ label: string; value: number }[]>([]);
const zhimaThreshold = ref<null | number>(null);
const zhimaScoreMinRef = ref<null | number>(null);
// deptUserMap用于与部门筛选框联动，目前暂先不用
// const deptUserMap = new Map<number, { label: string; value: number }[]>();
const bizDictRepoPromise = customerApi.getBizDictItems();
let cidList: number[] = [];
// userId - deptId map
let orderByFollowTimeDesc: any;
let orderByApplyDateDesc: any;
const customerItemsRef = ref<PagedInfo<CustomerItem>>({
  list: [],
  pageSize: 30,
});
const pagination = ref<TablePaginationConfig>({});
const custDescMap: Map<number, string> = new Map<number, string>();
const custGroupDescList: string[] = [];
const visibleFieldKeys = ref<null | Set<string>>(null);
const [VbenModal, modalApi] = useVbenModal({
  // 连接抽离的组件
  connectedComponent: ExtraModal,
});

const [AssignModal, assignModalApi] = useVbenModal({
  // 连接抽离的组件
  connectedComponent: DispatchCustModal,
});
const initCustomerRowClass: string = 'bg-lime-400/80';
// const initCustomerRowClass: string = 'text-red-800';
const viewportHeight = ref<number>(420);
onMounted(() => {
  // 制定列表排序规则：按最近处理优先
  orderByFollowTimeDesc = true;
  orderByApplyDateDesc = true;
  const totalHeight = document.documentElement.clientHeight;
  const searchFormHeight = hideAdminSearchCondition.value ? 120 : 158;
  viewportHeight.value = Math.max(totalHeight - searchFormHeight - 230, 320);
  // console.log(`viewportHeight: ${viewportHeight}, totalHeight: ${totalHeight}`)
  void initOptions();
});

async function initOptions() {
  try {
    const res = await bizDictRepoPromise.catch(
      () => ({}) as Record<string, BizDictItem[]>,
    );
    const customerStarGroup = res.CUSTOMER_STAR_GROUP || [];
    const progressList = res.PROGRESS || [];
    const callResultTips = res.CALL_RESULT_TIPS || [];
    const dataChannel = res.DATA_CHANNEL || [];

    custGroupDescList.splice(0, custGroupDescList.length);
    customerGroupOptions.value = customerStarGroup.map((e: BizDictItem) => {
      custGroupDescList.push(`${e.label}: ${e.description || ''}`);
      return { label: e.label, value: e.intValue };
    });
    progressOptions.value = progressList.map((e: BizDictItem) => {
      return { label: e.label, value: e.intValue };
    });
    callTipsOptions.value = callResultTips.map((e: BizDictItem) => {
      return { label: e.label, value: e.intValue };
    });
    if (dataChannel.length > 0) {
      channelOptions.value = dataChannel.map((e: BizDictItem) => {
        return { label: e.label, value: e.intValue };
      });
      channelOptions.value.unshift({ label: '无渠道', value: 0 });
    }
    const zhimaList = res.ZHIMA_SCORE_THRESHOLD || [];
    const thresholdValue = zhimaList[0]?.intValue;
    zhimaThreshold.value =
      typeof thresholdValue === 'number' ? thresholdValue : null;
    await loadFieldConfig();
    initSchemaList();
    setupColumns();

    if (hasMaintainRole) {
      deptApi.getDeptItems().then((res: DeptItem[]) => {
        deptNameOptions.value = res.map((e: DeptItem) => {
          return { label: e.deptName, value: e.id };
        });
      });
      // 此处不要求用户一定要在线
      userApi.listTeamUser(0).then((res: UserInfoTeamMember[]) => {
        // deptUserMap.clear();
        const tmpList = [{ label: '公海客户', value: 0 }];
        res.forEach((e: UserInfoTeamMember) => {
          tmpList.push({ label: e.name, value: e.id });
        });
        userNameOptions.value = tmpList;
      });
    }
    await pagedListCustomerInfo({});
  } catch {
    customerItemsRef.value = { list: [], pageSize: 30 };
    pagination.value = {};
    tableLoading.value = false;
    message.error('客户列表初始化失败，请稍后刷新重试');
  }
}

async function loadFieldConfig() {
  try {
    const list = await customerApi.getCustomerFieldConfig();
    const keys = (list || [])
      .map((item: CustomerFieldConfigItem) => item.fieldKey)
      .filter(Boolean);
    visibleFieldKeys.value = new Set(keys);
  } catch {
    visibleFieldKeys.value = null;
  }
}

function isFieldVisible(fieldKey: string) {
  if (!fieldKey) {
    return true;
  }
  if (fieldKey === 'zhimaScore') {
    return true;
  }
  if (visibleFieldKeys.value === null) {
    return true;
  }
  return visibleFieldKeys.value.has(fieldKey);
}

function isZhimaScoreHigh(score?: null | number) {
  if (score === null || score === undefined) {
    return false;
  }
  if (zhimaThreshold.value === null || zhimaThreshold.value === undefined) {
    return false;
  }
  return score >= zhimaThreshold.value;
}

function getZhimaScoreClass(score?: null | number) {
  return isZhimaScoreHigh(score) ? 'zhima-score-highlight' : '';
}

const searchFieldKeyMap: Record<string, string> = {
  followTimePicker: 'followTime',
  applyDatePicker: 'applyDate',
  mobile: 'mobile',
  namePrefix: 'name',
  customerGroup: 'customerGroupDesc',
  progress: 'progressDesc',
  callTips: 'callTipsDesc',
  ownerDeptIds: 'ownerDeptName',
  userIdList: 'ownerUserName',
  zhimaScoreMin: 'zhimaScore',
};

function isSearchFieldVisible(fieldName: string) {
  const fieldKey = searchFieldKeyMap[fieldName];
  if (!fieldKey) {
    return true;
  }
  if (fieldKey === 'callTipsDesc') {
    return true;
  }
  return isFieldVisible(fieldKey);
}

const schemaList: any[] = [];

function initSchemaList() {
  schemaList.length = 0;
  if (isSearchFieldVisible('followTimePicker')) {
    schemaList.push({
      component: 'RangePicker',
      fieldName: 'followTimePicker',
      componentProps: {
        showTime: true,
        showNow: true,
        showToday: true,
        placeholder: ['开始时间', '结束时间'],
      },
      label: '跟进时间',
    });
  }
  if (isSearchFieldVisible('applyDatePicker')) {
    schemaList.push({
      component: 'RangePicker',
      fieldName: 'applyDatePicker',
      label: '申请时间',
      componentProps: {
        showTime: true,
        showNow: true,
        showToday: true,
        placeholder: ['开始时间', '结束时间'],
      },
    });
  }
  if (isSearchFieldVisible('mobile')) {
    schemaList.push({
      component: 'Input',
      fieldName: 'mobile',
      label: '手机号',
      componentProps: {
        placeholder: '精确查询',
      },
    });
  }
  if (isSearchFieldVisible('namePrefix')) {
    schemaList.push({
      component: 'Input',
      fieldName: 'namePrefix',
      label: '客户姓名',
      componentProps: {
        placeholder: '可按姓名前缀查询',
      },
    });
  }
  if (isSearchFieldVisible('customerGroup')) {
    schemaList.push({
      component: 'Select',
      fieldName: 'customerGroup',
      label: '客户星级',
      componentProps: {
        placeholder: '不限星级',
        allowClear: true,
        options: customerGroupOptions,
      },
    });
  }
  if (isSearchFieldVisible('progress')) {
    schemaList.push({
      component: 'Select',
      fieldName: 'progress',
      label: '跟进状态',
      componentProps: {
        placeholder: '不限',
        allowClear: true,
        mode: 'multiple',
        options: progressOptions,
      },
    });
  }
  if (isSearchFieldVisible('callTips')) {
    schemaList.push({
      component: 'Select',
      fieldName: 'callTips',
      label: '沟通结果',
      componentProps: {
        placeholder: '不限',
        allowClear: true,
        options: callTipsOptions,
      },
    });
  }
  schemaList.push({
    component: 'Input',
    fieldName: 'ignoreDays',
    label: '未跟进 >=',
    componentProps: {
      placeholder: '不限',
    },
    suffix: '天',
  });
  if (isSearchFieldVisible('zhimaScoreMin')) {
    schemaList.push({
      component: 'InputNumber',
      fieldName: 'zhimaScoreMin',
      label: '芝麻分 >=',
      componentProps: {
        placeholder: '不限',
        min: 0,
        max: 950,
        onChange: (value: null | number) => {
          zhimaScoreMinRef.value = typeof value === 'number' ? value : null;
        },
      },
    });
  }
  if (!hideAdminSearchCondition.value && isSearchFieldVisible('ownerDeptIds')) {
    schemaList.push({
      component: 'Select',
      fieldName: 'ownerDeptIds',
      label: '所属部门',
      componentProps: {
        placeholder: '不限部门',
        allowClear: true,
        showSearch: true,
        filterOption: (inputValue: string, option: { label: string }) => {
          return option.label.toLowerCase().includes(inputValue.toLowerCase());
        },
        options: deptNameOptions,
        mode: 'multiple',
      },
    });
  }
  if (!hideAdminSearchCondition.value && isSearchFieldVisible('userIdList')) {
    schemaList.push({
      component: 'Select',
      fieldName: 'userIdList',
      label: '数据归属人',
      componentProps: {
        placeholder: '不限',
        allowClear: true,
        showSearch: true,
        filterOption: (inputValue: string, option: { label: string }) => {
          return option.label.toLowerCase().includes(inputValue.toLowerCase());
        },
        options: userNameOptions,
        mode: 'multiple',
      },
    });
  }
  if (!hideAdminSearchCondition.value) {
    schemaList.push({
      component: 'Select',
      fieldName: 'ownerFavorite',
      label: '收藏类型',
      help: '用于查看客户信息在归属人的哪个菜单中',
      componentProps: {
        placeholder: '不限',
        allowClear: true,
        options: FavoriteTypeEnum.getFavOptions(),
      },
    });
  }
  schemaList.push({
    component: 'Select',
    fieldName: 'channel',
    label: '渠道',
    componentProps: {
      placeholder: '不限',
      allowClear: true,
      showSearch: true,
      mode: 'multiple',
      options: channelOptions,
      filterOption: (inputValue: string, option: { label: string }) => {
        return option.label.toLowerCase().includes(inputValue.toLowerCase());
      },
    },
  });
  searchFormApi.setState(() => {
    return {
      schema: [...schemaList],
    };
  });
}

const [SearchForm, searchFormApi] = useVbenForm({
  wrapperClass: 'grid-cols-5',
  // actionWrapperClass: 'mb-3',
  compact: true,
  commonConfig: {
    // 所有表单项
    componentProps: {
      class: 'w-full border-1',
    },
  },
  schema: schemaList,
  showCollapseButton: false,
  submitButtonOptions: {
    content: '查询',
    // size: 'small',
  },
  resetButtonOptions: {
    content: '清除',
    // size: 'small',
  },
  submitOnChange: false,
  submitOnEnter: true,
  handleSubmit: onSearchSubmit,
});

watch(
  () => route.query.openCid,
  async (value) => {
    const raw = Array.isArray(value) ? value[0] : value;
    const cid = raw ? Number(raw) : 0;
    if (!cid || cid === lastOpenCid.value) {
      return;
    }
    lastOpenCid.value = cid;
    await openDetailById(cid);
    const nextQuery = { ...route.query };
    delete nextQuery.openCid;
    router.replace({ query: nextQuery });
  },
  { immediate: true },
);

async function onSearchSubmit(formValues: any) {
  if (typeof formValues?.zhimaScoreMin === 'number') {
    zhimaScoreMinRef.value = formValues.zhimaScoreMin;
  } else if (formValues?.zhimaScoreMin) {
    zhimaScoreMinRef.value = Number(formValues.zhimaScoreMin);
  } else {
    zhimaScoreMinRef.value = null;
  }
  await pagedListCustomerInfo(formValues);
}

async function pagedListCustomerInfo(formValues?: any) {
  tableLoading.value = true;
  try {
    const progressValue = formValues?.progress;
    const channelValue = formValues?.channel;
    const callTipsValue = formValues?.callTips;
    const zhimaValue = formValues?.zhimaScoreMin ?? zhimaScoreMinRef.value;
    const progressList =
      Array.isArray(progressValue) && progressValue.length > 0
        ? progressValue
        : undefined;
    const channelList =
      Array.isArray(channelValue) && channelValue.length > 0
        ? channelValue
        : undefined;
    const progress = Array.isArray(progressValue) ? undefined : progressValue;
    const channel = Array.isArray(channelValue) ? undefined : channelValue;
    const callTips =
      callTipsValue === '' ||
      callTipsValue === null ||
      callTipsValue === undefined
        ? undefined
        : Number(callTipsValue);
    const zhimaScoreMin =
      zhimaValue === '' || zhimaValue === null || zhimaValue === undefined
        ? undefined
        : Number(zhimaValue);
    const cleanedValues = { ...formValues };
    if (Array.isArray(progressValue)) {
      delete cleanedValues.progress;
    }
    if (Array.isArray(channelValue)) {
      delete cleanedValues.channel;
    }
    const data: PagedInfo<CustomerItem> =
      await customerApi.pagedListCustomerInfo({
        ...cleanedValues,
        followTimeStart:
          formValues?.followTimePicker?.[0]?.format?.(DATA_TIME_FMT),
        followTimeEnd:
          formValues?.followTimePicker?.[1]?.format?.(DATA_TIME_FMT),
        applyDateStart:
          formValues?.applyDatePicker?.[0]?.format?.(DATA_TIME_FMT),
        applyDateEnd: formValues?.applyDatePicker?.[1]?.format?.(DATA_TIME_FMT),
        progress,
        progressList,
        channel,
        channelList,
        callTips: Number.isFinite(callTips) ? callTips : undefined,
        zhimaScoreMin: Number.isFinite(zhimaScoreMin)
          ? zhimaScoreMin
          : undefined,
        ownerFavorite:
          props.favoriteFlag === null
            ? formValues?.ownerFavorite
            : props.favoriteFlag,
        ownerUserId:
          props.favoriteFlag !== null && props.favoriteFlag >= 0
            ? userId
            : undefined,
        selfOnly: props.favoriteFlag !== null,
        publicPoolOnly: props.publicPoolOnly,
        orderByFollowTimeDesc,
        orderByApplyDateDesc,
        page: formValues?.page ?? 1,
        pageSize: formValues?.pageSize ?? 30,
      });

    cidList = [];
    custDescMap.clear();
    data.list?.forEach((item: CustomerItem) => {
      cidList.push(item.id);
      item.ownerFavoriteDesc = item.ownerUserId
        ? FavoriteTypeEnum.getFavTypeName(item.ownerFavorite)
        : '';
      item.houseFlagValDesc = item.houseVal ? `${item.houseVal}万元` : '';
      item.key = item.id;
      item.providentFlagValDesc = item.providentAmountYuan
        ? `${item.providentAmountYuan}`
        : '';
      custDescMap.set(item.id, `${item.name} - ${item.mobile}`);
    });
    customerItemsRef.value = data;
    pagination.value = {
      total: data?.total,
      current: data?.pageNum,
      pageSize: data.pageSize,
      showTotal: (total) => `共 ${total} 条数据`,
      pageSizeOptions: ['20', '30', '50', '100'],
      defaultPageSize: 30,
      position: ['bottomLeft'],
      showSizeChanger: true,
    };
    state.selectedRowKeys = [];
    return data;
  } catch {
    customerItemsRef.value = { list: [], pageSize: 30 };
    pagination.value = {};
    message.error('客户列表加载失败，请稍后重试');
  } finally {
    tableLoading.value = false;
  }
}

const columns = ref<TableColumnType<CustomerItem>[]>([]);
const draggingColumnKey = ref<null | string>(null);
const dragOverColumnKey = ref<null | string>(null);
const columnOrderStorageKey = 'cust-list-table-column-order';

function setupColumns() {
  columns.value = [];
  if (isFieldVisible('name')) {
    columns.value.push({
      title: '姓名',
      dataIndex: 'name',
      key: 'name',
      width: 100,
      fixed: 'left',
    });
  }
  if (isFieldVisible('mobile')) {
    columns.value.push({
      title: '手机号',
      dataIndex: 'mobile',
      key: 'mobile',
      width: 140,
    });
  }
  if (isFieldVisible('idCardNo')) {
    columns.value.push({
      title: '身份证号码',
      dataIndex: 'idCardNo',
      key: 'idCardNo',
      width: 200,
      ellipsis: true,
      className: 'cell-nowrap',
    });
  }
  if (isFieldVisible('sexDesc')) {
    columns.value.push({
      title: '性别',
      dataIndex: 'sexDesc',
      key: 'sexDesc',
      width: 60,
    });
  }
  if (isFieldVisible('customerGroupDesc')) {
    columns.value.push({
      title: '客户星级',
      dataIndex: 'customerGroupDesc',
      width: 120,
      key: 'customerGroupDesc',
    });
  }
  if (isFieldVisible('progressDesc')) {
    columns.value.push({
      title: '跟进状态',
      dataIndex: 'progressDesc',
      width: 100,
      key: 'progressDesc',
    });
  }
  if (isFieldVisible('customerRemark')) {
    columns.value.push({
      title: '客户备注',
      dataIndex: 'customerRemark',
      key: 'customerRemark',
      width: 300,
    });
  }
  if (isFieldVisible('followTime')) {
    columns.value.push({
      title: '最后跟进时间',
      dataIndex: 'followTime',
      key: 'followTime',
      width: 180,
    });
  }
  if (isFieldVisible('followCnt')) {
    columns.value.push({
      title: '跟进次数',
      dataIndex: 'followCnt',
      key: 'followCnt',
      width: 80,
    });
  }
  if (isFieldVisible('age')) {
    columns.value.push({
      title: '年龄',
      dataIndex: 'age',
      key: 'age',
      width: 60,
    });
  }
  if (isFieldVisible('reqLoanAmount')) {
    columns.value.push({
      title: '需要金额',
      dataIndex: 'reqLoanAmount',
      key: 'reqLoanAmount',
      width: 100,
    });
  }
  if (isFieldVisible('zhimaScore')) {
    columns.value.push({
      title: '芝麻分',
      dataIndex: 'zhimaScore',
      key: 'zhimaScore',
      width: 80,
    });
  }
  if (hasMaintainRole && isFieldVisible('ownerUserName')) {
    columns.value.push({
      title: '数据归属人',
      dataIndex: 'ownerUserName',
      key: 'ownerUserName',
      width: 100,
    });
  }
  if (isFieldVisible('applyDate')) {
    columns.value.push({
      title: '申请时间',
      dataIndex: 'applyDate',
      key: 'applyDate',
      width: 180,
    });
  }
  if (isFieldVisible('followerUserName')) {
    columns.value.push({
      title: '最后跟进人',
      dataIndex: 'followerUserName',
      key: 'followerUserName',
      width: 100,
    });
  }
  if (isFieldVisible('callTipsDesc')) {
    columns.value.push({
      title: '沟通结果',
      dataIndex: 'callTipsDesc',
      key: 'callTipsDesc',
      width: 100,
    });
  }
  if (isFieldVisible('followRemark')) {
    columns.value.push({
      title: '跟进情况备注',
      dataIndex: 'followRemark',
      key: 'followRemark',
      width: 200,
    });
  }
  if (isFieldVisible('ownerDeptName')) {
    columns.value.push({
      title: '数据归属部门',
      dataIndex: 'ownerDeptName',
      key: 'ownerDeptName',
      width: 120,
    });
  }
  columns.value.push({
    key: 'action',
    title: '操作',
    width: 100,
    fixed: 'right',
    align: 'center',
  });
  columns.value = applyColumnOrder(columns.value);
}

const resizing = ref<null | {
  key: string;
  startWidth: number;
  startX: number;
}>(null);

function getColumnKey(column: TableColumnType<CustomerItem>) {
  return `${column.key ?? column.dataIndex ?? ''}`;
}

function canReorderColumn(column: TableColumnType<CustomerItem>) {
  if (!column) {
    return false;
  }
  if (column.fixed === 'left' || column.fixed === 'right') {
    return false;
  }
  const key = getColumnKey(column);
  return !!key;
}

function loadColumnOrder(): string[] {
  try {
    const raw = localStorage.getItem(columnOrderStorageKey);
    if (!raw) {
      return [];
    }
    const parsed = JSON.parse(raw);
    if (!Array.isArray(parsed)) {
      return [];
    }
    return parsed.filter((item) => typeof item === 'string' && item.trim());
  } catch {
    return [];
  }
}

function saveColumnOrder(order: string[]) {
  try {
    localStorage.setItem(columnOrderStorageKey, JSON.stringify(order));
  } catch {
    // ignore storage errors
  }
}

function applyColumnOrder(cols: TableColumnType<CustomerItem>[]) {
  const order = loadColumnOrder();
  if (order.length === 0) {
    return cols;
  }
  const fixedLeft = cols.filter((col) => col.fixed === 'left');
  const fixedRight = cols.filter((col) => col.fixed === 'right');
  const reorderable = cols.filter((col) => canReorderColumn(col));
  const reorderMap = new Map(
    reorderable.map((col) => [getColumnKey(col), col]),
  );
  const ordered: TableColumnType<CustomerItem>[] = [];
  order.forEach((key) => {
    const col = reorderMap.get(key);
    if (col) {
      ordered.push(col);
      reorderMap.delete(key);
    }
  });
  ordered.push(...reorderMap.values());
  return [...fixedLeft, ...ordered, ...fixedRight];
}

function reorderColumns(sourceKey: string, targetKey: string) {
  if (sourceKey === targetKey) {
    return;
  }
  const current = columns.value;
  const sourceIndex = current.findIndex(
    (col) => getColumnKey(col) === sourceKey,
  );
  const targetIndex = current.findIndex(
    (col) => getColumnKey(col) === targetKey,
  );
  if (sourceIndex === -1 || targetIndex === -1) {
    return;
  }
  const sourceCol = current[sourceIndex];
  const targetCol = current[targetIndex];
  if (!canReorderColumn(sourceCol) || !canReorderColumn(targetCol)) {
    return;
  }
  const next = [...current];
  next.splice(sourceIndex, 1);
  const insertIndex = sourceIndex < targetIndex ? targetIndex - 1 : targetIndex;
  next.splice(insertIndex, 0, sourceCol);
  columns.value = next;
  const orderKeys = next
    .filter((col) => canReorderColumn(col))
    .map((col) => getColumnKey(col))
    .filter(Boolean);
  saveColumnOrder(orderKeys as string[]);
}

function handleColumnDragStart(
  column: TableColumnType<CustomerItem>,
  event: DragEvent,
) {
  if (!canReorderColumn(column)) {
    return;
  }
  const key = getColumnKey(column);
  if (!key) {
    return;
  }
  draggingColumnKey.value = key;
  event.dataTransfer?.setData('text/plain', key);
  event.dataTransfer?.setDragImage(new Image(), 0, 0);
  event.dataTransfer?.setData('application/x-column-key', key);
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move';
  }
}

function handleColumnDragOver(
  column: TableColumnType<CustomerItem>,
  event: DragEvent,
) {
  if (!draggingColumnKey.value) {
    return;
  }
  if (!canReorderColumn(column)) {
    return;
  }
  const key = getColumnKey(column);
  if (!key || key === draggingColumnKey.value) {
    return;
  }
  event.preventDefault();
  dragOverColumnKey.value = key;
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'move';
  }
}

function handleColumnDragLeave(column: TableColumnType<CustomerItem>) {
  if (!draggingColumnKey.value) {
    return;
  }
  const key = getColumnKey(column);
  if (dragOverColumnKey.value === key) {
    dragOverColumnKey.value = null;
  }
}

function handleColumnDrop(
  column: TableColumnType<CustomerItem>,
  event: DragEvent,
) {
  const sourceKey = draggingColumnKey.value;
  const targetKey = getColumnKey(column);
  if (!sourceKey || !targetKey) {
    return;
  }
  if (!canReorderColumn(column)) {
    return;
  }
  event.preventDefault();
  reorderColumns(sourceKey, targetKey);
  draggingColumnKey.value = null;
  dragOverColumnKey.value = null;
}

function handleColumnDragEnd() {
  draggingColumnKey.value = null;
  dragOverColumnKey.value = null;
}

function updateColumnWidth(columnKey: string, width: number) {
  const target = columns.value.find((col) => getColumnKey(col) === columnKey);
  if (target) {
    target.width = width;
  }
}

function handleResizeStart(
  column: TableColumnType<CustomerItem>,
  event: MouseEvent,
) {
  if (!column?.width) {
    return;
  }
  event.stopPropagation();
  event.preventDefault();
  const key = getColumnKey(column);
  if (!key) {
    return;
  }
  resizing.value = {
    key,
    startX: event.clientX,
    startWidth: Number(column.width) || 120,
  };
  document.addEventListener('mousemove', handleResizeMove);
  document.addEventListener('mouseup', handleResizeEnd);
}

function handleResizeMove(event: MouseEvent) {
  if (!resizing.value) {
    return;
  }
  const delta = event.clientX - resizing.value.startX;
  const nextWidth = Math.max(60, resizing.value.startWidth + delta);
  updateColumnWidth(resizing.value.key, nextWidth);
}

function handleResizeEnd() {
  if (!resizing.value) {
    return;
  }
  resizing.value = null;
  document.removeEventListener('mousemove', handleResizeMove);
  document.removeEventListener('mouseup', handleResizeEnd);
}

onBeforeUnmount(() => {
  document.removeEventListener('mousemove', handleResizeMove);
  document.removeEventListener('mouseup', handleResizeEnd);
});

const state = reactive<{
  loading: boolean;
  selectedRowKeys: number[];
}>({
  selectedRowKeys: [], // Check here to configure the default column
  loading: false,
});
const onSelectChange = (selectedRowKeys: number[]) => {
  state.selectedRowKeys = selectedRowKeys;
};
const handleTableChange: TableProps['onChange'] = async (pag) => {
  pagination.value = {
    ...pagination.value,
    current: pag.current,
    pageSize: pag.pageSize,
  };
  getSearchConditions().then((formValues) => {
    pagedListCustomerInfo({
      ...formValues,
      page: pag.current,
      pageSize: pag.pageSize,
    });
  });
};

async function getSearchConditions() {
  let formValues = await searchFormApi.getValues();
  if (formValues === undefined) {
    formValues = {};
  }
  formValues.pageSize = pagination.value.pageSize;
  formValues.page = pagination.value.current;
  return formValues;
}

async function openModal(record: CustomerItem) {
  const formValues = await getSearchConditions();
  const modalData = {
    cid: record.id,
    idCardNo: record.idCardNo,
    uid: userId,
    customerGroupOptions,
    progressOptions,
    deptNameOptions,
    callTipsOptions,
    hasMaintainRole,
    cidList,
    zhimaThreshold: zhimaThreshold.value,
    callbackFnc: async () => {
      await pagedListCustomerInfo(formValues);
    },
  };
  modalApi.setData(modalData).open();
}

async function openDetailById(cid: number) {
  const formValues = await getSearchConditions();
  await pagedListCustomerInfo(formValues);
  await nextTick();
  openModal({ id: cid } as CustomerItem);
}

async function openAssignModal(cids: number[], custDescList: string[]) {
  if (!cids || cids.length === 0) {
    message.error('请选择要分配的客户');
    return;
  }
  const formValues = await getSearchConditions();
  const modalData = {
    cids,
    custDescList,
    callbackFunc: async () => {
      await pagedListCustomerInfo(formValues);
    },
  };

  assignModalApi.setData(modalData).open();
}

function assignCust(cid: number, name: string, mobile: string) {
  openAssignModal([cid], [`${name}-${mobile}`]);
}

/**
 * 将客户信息退回至公海
 * @param cids
 */
async function returnCustomer(cids: number[]) {
  if (!(cids && cids.length > 0)) {
    message.error('请选择要退回公海的客户');
    return;
  }
  tableLoading.value = true;
  const formValues = await getSearchConditions();
  await customerApi.batchBackToOcean(cids).then(() => {
    pagedListCustomerInfo(formValues).then(() => {
      message.success({ content: '客户信息退回公海成功', duration: 3 });
    });
  });
  tableLoading.value = false;
}

function returnCustomerConfirm(cids: number[], custNameMobile: string) {
  if (!(cids && cids.length > 0)) {
    message.error('请选择要退回公海的客户');
    return;
  }
  const custDesc = custNameMobile
    ? `客户 ${custNameMobile} 退回公海`
    : '所选客户批量退回公海';
  Modal.confirm({
    title: '退回客户信息',
    content: `确定要将${custDesc}吗?\n退回公海后，您将无法查看该客户信息，是否继续?`,
    onOk: () => {
      returnCustomer(cids);
    },
  });
}

/**
 * 批量分配客户
 */
function batchAssign() {
  const custIds: number[] = [];
  const custDescList: string[] = [];
  state.selectedRowKeys.forEach((custId: any) => {
    const desc: string | undefined = custDescMap.get(custId);
    if (!desc) {
      return;
    }
    custIds.push(custId);
    custDescList.push(desc);
  });
  openAssignModal(custIds, custDescList);
}

async function claimCustomer(cid: number) {
  await customerApi.claimCustomer(cid).then(() => {
    message.success({ content: '领取客户成功', duration: 3 });
  });
  const formValues = await getSearchConditions();
  await pagedListCustomerInfo(formValues);
}

/**
 * 根据favFlag将客户信息移动到不同的菜单列表
 * @param cids
 * @param favFlag
 */
async function switchFavorite(cids: number[], favFlag: number) {
  if (!(cids && cids.length > 0)) {
    message.error('请选择要移动的客户');
    return;
  }
  tableLoading.value = true;
  await customerApi.batchOptFavorite(cids, favFlag).then(() => {
    message.success({
      content: `客户信息已移动至【${FavoriteTypeEnum.getFavTypeName(favFlag)}】`,
      duration: 3,
    });
  });
  const formValues = await getSearchConditions();
  formValues.page = 1;
  await pagedListCustomerInfo(formValues);
  tableLoading.value = false;
}

async function batchSwitchFavorite(favFlag: number) {
  await switchFavorite(state.selectedRowKeys, favFlag);
}

function batchBackToOcean() {
  const custIds: number[] = state.selectedRowKeys ?? [];
  if (custIds && custIds.length === 1) {
    returnCustomerConfirm(custIds, custDescMap.get(custIds[0] ?? 0) ?? '');
    return;
  }
  returnCustomerConfirm(custIds, '');
}
</script>
<template>
  <VbenModal />
  <div
    class="cust-search-panel w-full rounded-lg border border-gray-200 bg-white p-2 pb-1 shadow-sm transition-colors duration-200 dark:border-[#1f2a44] dark:bg-[#0f172a] dark:text-[#c7d2fe]"
  >
    <SearchForm />
  </div>
  <div class="m-1 mt-5 w-full">
    <span class="mr-1">批量操作:</span>
    <Button
      size="small"
      type="link"
      class="mr-1"
      @click="batchAssign"
      v-if="favoriteFlag === null && !publicPoolOnly"
    >
      分配
    </Button>
    <Button
      size="small"
      type="link"
      class="-m-1 p-2"
      @click="batchSwitchFavorite(FavoriteTypeEnum.MY_CUST)"
      v-if="favoriteFlag !== null && favoriteFlag !== FavoriteTypeEnum.MY_CUST"
    >
      移入我的客户
    </Button>
    <Button
      size="small"
      type="link"
      class="-m-1 p-2"
      @click="batchSwitchFavorite(FavoriteTypeEnum.KEY_CUST)"
      v-if="favoriteFlag !== null && favoriteFlag !== FavoriteTypeEnum.KEY_CUST"
    >
      移入重点客户
    </Button>
    <Button
      size="small"
      type="link"
      class="-m-1 p-2"
      @click="batchSwitchFavorite(FavoriteTypeEnum.NORMAL)"
      v-if="favoriteFlag !== null && favoriteFlag !== FavoriteTypeEnum.NORMAL"
    >
      移入再分配客户
    </Button>
    <Button
      danger
      size="small"
      type="link"
      class="-m-1 p-2"
      title="将数据放入本部门公海"
      @click="batchBackToOcean"
      v-if="favoriteFlag !== null"
    >
      退回
    </Button>
  </div>
  <!--      // 若数据没有最后跟进人， 则换高亮背景提示
      if (params?.row && params?.row.followerUserId === 0) {
        return 'bg-zinc-600/50 text-white';
      }-->
  <div class="mt-0 w-full">
    <!--
        if (params?.row && params?.row.followerUserId === 0) {
          return 'bg-zinc-600/50 text-white';
        }
    -->
    <Table
      class="cust-list-table"
      :row-class-name="
        (_rec) => (_rec.followerUserId === 0 ? initCustomerRowClass : '')
      "
      :loading="tableLoading"
      :columns="columns"
      :data-source="customerItemsRef.list"
      :pagination="pagination"
      size="small"
      :scroll="{ x: 220, y: viewportHeight }"
      :row-selection="{
        selectedRowKeys: state.selectedRowKeys,
        onChange: onSelectChange,
      }"
      @change="handleTableChange"
      bordered
    >
      <!--      customRow="{(_rec)=>{return { class: _rec.followerUserId === 0 ? 'row-su-high-light' : null)}}}"-->
      <template #headerCell="{ column }">
        <div
          class="table-header-cell"
          :class="{
            'is-draggable': canReorderColumn(column),
            'is-dragging': draggingColumnKey === getColumnKey(column),
            'is-dragover': dragOverColumnKey === getColumnKey(column),
          }"
          @dragover="handleColumnDragOver(column, $event)"
          @dragleave="handleColumnDragLeave(column)"
          @drop="handleColumnDrop(column, $event)"
        >
          <div
            class="table-header-title"
            :draggable="canReorderColumn(column)"
            @dragstart="handleColumnDragStart(column, $event)"
            @dragend="handleColumnDragEnd"
          >
            <template v-if="column.dataIndex === 'customerGroupDesc'">
              <div class="flex flex-shrink-0 items-center">
                客户星级
                <Tooltip>
                  <template #title>
                    <ul>
                      <template v-for="item in custGroupDescList" :key="item">
                        <li>{{ item }}</li>
                      </template>
                    </ul>
                  </template>
                  <AdQuestion class="m-1 size-4" />
                </Tooltip>
              </div>
            </template>
            <template v-else>
              {{ column.title ?? '' }}
            </template>
          </div>
          <span
            v-if="column.width"
            class="table-col-resizer"
            @mousedown="handleResizeStart(column, $event)"
          ></span>
        </div>
      </template>
      <template #bodyCell="{ column, record }">
        <template
          v-if="
            column.key === 'progressDesc' &&
            HIGHT_LIGHT_PROGRESS_VALUES.has(record.progress)
          "
        >
          <span class="text-red-600">{{ record.progressDesc }}</span>
        </template>
        <template v-if="column.key === 'zhimaScore'">
          <span :class="getZhimaScoreClass(record.zhimaScore)">
            {{ record.zhimaScore ?? '' }}
          </span>
        </template>
        <template v-if="column.key === 'action'">
          <Button
            size="small"
            type="link"
            class="-m-2 p-1"
            @click="openModal(record)"
          >
            查看
          </Button>
          <Button
            size="small"
            type="link"
            class="-m-1 p-2"
            v-if="hasMaintainRole && favoriteFlag === null"
            @click="assignCust(record.id, record.name, record.mobile)"
          >
            分配
          </Button>
          <Button
            size="small"
            type="link"
            class="-m-1 p-2"
            v-if="publicPoolOnly && record.ownerUserId === 0"
            @click="claimCustomer(record.id)"
          >
            领取
          </Button>
          <Button
            danger
            size="small"
            type="link"
            class="-m-1 p-2"
            title="将数据放入本部门公海"
            @click="
              returnCustomerConfirm(
                [record.id],
                `${record.name} - ${record.mobile}`,
              )
            "
            v-if="
              Number(userId) === Number(record.ownerUserId) &&
              favoriteFlag !== null
            "
          >
            退回
          </Button>
        </template>
      </template>
    </Table>
  </div>

  <AssignModal />
</template>
<style scoped>
/*
.cust-search-panel {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow:
    0 1px 0 rgba(15, 23, 42, 0.04),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  transition:
    background-color 0.2s ease,
    border-color 0.2s ease,
    box-shadow 0.2s ease;
}

:global(.dark) .cust-search-panel,
:global([data-theme='dark']) .cust-search-panel {
  background: linear-gradient(180deg, #0f172a 0%, #0b1220 100%);
  border-color: #1f2a44;
  box-shadow:
    0 0 0 1px rgba(10, 18, 32, 0.7),
    inset 0 1px 0 rgba(255, 255, 255, 0.03);
}
*/

:global(.dark) .cust-search-panel :deep(.ant-form-item-label > label),
:global([data-theme='dark'])
  .cust-search-panel
  :deep(.ant-form-item-label > label) {
  color: #c7d2fe;
}

.table-header-cell {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  padding-right: 10px;
}

.table-header-title {
  flex: 1 1 auto;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.table-header-cell.is-draggable .table-header-title {
  cursor: move;
}

.table-header-cell.is-dragover {
  outline: 1px dashed rgba(148, 163, 184, 0.7);
  outline-offset: -2px;
}

.table-header-cell.is-dragging {
  opacity: 0.6;
}

.table-col-resizer {
  position: absolute;
  top: 0;
  right: -4px;
  width: 8px;
  height: 100%;
  cursor: col-resize;
  z-index: 2;
}

.table-col-resizer::after {
  content: '';
  position: absolute;
  top: 20%;
  right: 3px;
  width: 2px;
  height: 60%;
  background: rgba(0, 0, 0, 0.12);
  border-radius: 2px;
}

:global(.dark) .table-col-resizer::after,
:global([data-theme='dark']) .table-col-resizer::after {
  background: rgba(148, 163, 184, 0.6);
}

.cust-list-table :deep(.ant-table-body) {
  scrollbar-gutter: stable;
}

.cust-list-table :deep(.ant-table-header) {
  padding-right: 12px;
}

.cust-list-table
  :deep(.ant-table-tbody > tr:hover > td.ant-table-cell-fix-left),
.cust-list-table
  :deep(.ant-table-tbody > tr:hover > td.ant-table-cell-fix-right),
.cust-list-table
  :deep(.ant-table-tbody > tr.ant-table-row-hover > td.ant-table-cell-fix-left),
.cust-list-table
  :deep(
    .ant-table-tbody > tr.ant-table-row-hover > td.ant-table-cell-fix-right
  ) {
  background: linear-gradient(
    90deg,
    rgba(59, 130, 246, 0.06),
    rgba(251, 191, 36, 0.06)
  );
}

:global(.dark)
  .cust-list-table
  :deep(.ant-table-tbody > tr:hover > td.ant-table-cell-fix-left),
:global(.dark)
  .cust-list-table
  :deep(.ant-table-tbody > tr:hover > td.ant-table-cell-fix-right),
:global(.dark)
  .cust-list-table
  :deep(.ant-table-tbody > tr.ant-table-row-hover > td.ant-table-cell-fix-left),
:global(.dark)
  .cust-list-table
  :deep(
    .ant-table-tbody > tr.ant-table-row-hover > td.ant-table-cell-fix-right
  ),
:global([data-theme='dark'])
  .cust-list-table
  :deep(.ant-table-tbody > tr:hover > td.ant-table-cell-fix-left),
:global([data-theme='dark'])
  .cust-list-table
  :deep(.ant-table-tbody > tr:hover > td.ant-table-cell-fix-right),
:global([data-theme='dark'])
  .cust-list-table
  :deep(.ant-table-tbody > tr.ant-table-row-hover > td.ant-table-cell-fix-left),
:global([data-theme='dark'])
  .cust-list-table
  :deep(
    .ant-table-tbody > tr.ant-table-row-hover > td.ant-table-cell-fix-right
  ) {
  background: linear-gradient(
    90deg,
    rgba(56, 189, 248, 0.08),
    rgba(251, 191, 36, 0.08)
  );
}

.cust-list-table
  :deep(.ant-table-tbody > tr.bg-lime-400\/80 > td.ant-table-cell-fix-left),
.cust-list-table
  :deep(.ant-table-tbody > tr.bg-lime-400\/80 > td.ant-table-cell-fix-right),
.cust-list-table
  :deep(
    .ant-table-tbody > tr.bg-lime-400\/80:hover > td.ant-table-cell-fix-left
  ),
.cust-list-table
  :deep(
    .ant-table-tbody > tr.bg-lime-400\/80:hover > td.ant-table-cell-fix-right
  ),
.cust-list-table
  :deep(
    .ant-table-tbody
      > tr.bg-lime-400\/80.ant-table-row-hover
      > td.ant-table-cell-fix-left
  ),
.cust-list-table
  :deep(
    .ant-table-tbody
      > tr.bg-lime-400\/80.ant-table-row-hover
      > td.ant-table-cell-fix-right
  ) {
  background: rgb(163 230 53 / 0.8);
}

.cust-list-table :deep(.cell-nowrap) {
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

:global(.dark) .zhima-score-highlight,
:global([data-theme='dark']) .zhima-score-highlight {
  color: #fecaca;
  background: rgba(248, 113, 113, 0.25);
}
</style>
