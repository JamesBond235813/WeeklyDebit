<script lang="ts" setup>
import type { ComputedRef } from 'vue';

import type { VbenFormProps } from '#/adapter/form';
import type { VxeGridProps } from '#/adapter/vxe-table';
import type { PagedInfo } from '#/api/biz/biz-common';
import type { BizDictItem, CustomerFieldConfigItem, CustomerItem } from '#/api/biz/customer';
import type { DeptItem } from '#/api/biz/dept';
import type { UserInfoTeamMember } from '#/api/biz/user';

import { computed, onMounted, ref } from 'vue';

import { useAccess } from '@vben/access';
import { useVbenModal } from '@vben/common-ui';
import { useUserStore } from '@vben/stores';

import { Button, message, Modal } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
// import { VbenSelect } from 'packages/@core/ui-kit/shadcn-ui/src/components/select/index.ts';
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
});

const userStore = useUserStore();
const access = useAccess();
const userId = userStore.userInfo?.userId;

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
const custGroupDesc = ref<string>('');
const visibleFieldKeys = ref<Set<string> | null>(null);
// deptUserMap用于与部门筛选框联动，目前暂先不用
// const deptUserMap = new Map<number, { label: string; value: number }[]>();
const bizDictRepoPromise = customerApi.getBizDictItems();
let cidList: number[] = [];
// userId - deptId map
let orderByFollowTimeDesc: any;
let orderByApplyDateDesc: any;

const [VbenModal, modalApi] = useVbenModal({
  // 连接抽离的组件
  connectedComponent: ExtraModal,
});

const [AssignModal, assignModalApi] = useVbenModal({
  // 连接抽离的组件
  connectedComponent: DispatchCustModal,
});
onMounted(() => {
  // 制定列表排序规则：按最近处理优先
  orderByFollowTimeDesc = true;
  orderByApplyDateDesc = true;
  initOptions();
});

function openAssignModal(cids: number[], custDescList: string[]) {
  if (!cids || cids.length === 0) {
    message.error('请选择要分配的客户');
    return;
  }
  const modalData = {
    cids,
    custDescList,
    callbackFunc: () => {
      gridApi.query();
    },
  };

  assignModalApi.setData(modalData).open();
}
function assignCust(cid: number, name: string, mobile: string) {
  openAssignModal([cid], [`${name}-${mobile}`]);
}
function initOptions() {
  bizDictRepoPromise.then((res) => {
    customerGroupOptions.value = res.CUSTOMER_STAR_GROUP.map(
      (e: BizDictItem) => {
        return { label: e.label, value: e.intValue };
      },
    );
    custGroupDesc.value = '';
    res.CUSTOMER_STAR_GROUP.forEach((e: BizDictItem) => {
      custGroupDesc.value += `${e.label}: ${e.description}\n`;
    });
    // gridApi.setGridOptions();
    gridOptions.columns[5].titleSuffix.content = custGroupDesc.value;
    gridApi.setState({
      gridOptions: {
        columns: gridOptions.columns,
      },
    });
    progressOptions.value = res.PROGRESS.map((e: BizDictItem) => {
      return { label: e.label, value: e.intValue };
    });
    callTipsOptions.value = res.CALL_RESULT_TIPS.map((e: BizDictItem) => {
      return { label: e.label, value: e.intValue };
    });
    if (res.DATA_CHANNEL) {
      channelOptions.value = res.DATA_CHANNEL.map((e: BizDictItem) => {
        return { label: e.label, value: e.intValue };
      });
      channelOptions.value.unshift({ label: '无渠道', value: 0 });
    }
  });
  loadFieldConfig().then(() => {
    applyFieldVisibility();
  });
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
}

async function loadFieldConfig() {
  try {
    const list = await customerApi.getCustomerFieldConfig();
    const keys = (list || [])
      .map((item: CustomerFieldConfigItem) => item.fieldKey)
      .filter(Boolean);
    visibleFieldKeys.value = new Set(keys);
  } catch (error) {
    visibleFieldKeys.value = null;
  }
}

function isFieldVisible(fieldKey?: string) {
  if (!fieldKey) {
    return true;
  }
  if (visibleFieldKeys.value === null) {
    return true;
  }
  return visibleFieldKeys.value.has(fieldKey);
}

function applyFieldVisibility() {
  gridOptions.columns.forEach((column) => {
    const fieldKey = String(column.field || '');
    if (!fieldKey || fieldKey === 'idPicker' || fieldKey === 'action') {
      return;
    }
    if (column.visible === false) {
      return;
    }
    column.visible = isFieldVisible(fieldKey);
  });
  gridApi.setState({
    gridOptions: {
      columns: gridOptions.columns,
    },
  });
}
const searchFormOptions: VbenFormProps = {
  wrapperClass: 'grid-cols-5',
  schema: [
    {
      component: 'RangePicker',
      fieldName: 'followTimePicker',
      componentProps: {
        showTime: true,
        showNow: true,
        showToday: true,
        placeholder: ['开始时间', '结束时间'],
      },
      label: '跟进时间',
    },
    {
      component: 'RangePicker',
      fieldName: 'applyDatePicker',
      label: '申请时间',
      componentProps: {
        showTime: true,
        showNow: true,
        showToday: true,
        placeholder: ['开始时间', '结束时间'],
      },
    },
    {
      component: 'Input',
      fieldName: 'mobile',
      label: '手机号',
      componentProps: {
        placeholder: '精确查询',
      },
    },
    {
      component: 'Input',
      fieldName: 'namePrefix',
      label: '客户姓名',
      componentProps: {
        placeholder: '可按姓名前缀查询',
      },
    },
    {
      component: 'Select',
      fieldName: 'customerGroup',
      label: '客户星级',
      componentProps: {
        placeholder: '不限星级',
        allowClear: true,
        options: customerGroupOptions,
      },
    },
    {
      component: 'Select',
      fieldName: 'progress',
      label: '跟进状态',
      componentProps: {
        placeholder: '不限',
        allowClear: true,
        options: progressOptions,
      },
    },
    {
      component: 'Input',
      fieldName: 'ignoreDays',
      label: '未跟进 >=',
      componentProps: {
        placeholder: '不限',
      },
      suffix: '天',
    },
    {
      component: 'Select',
      fieldName: 'ownerDeptIds',
      label: '所属部门',
      hideLabel: hideAdminSearchCondition,
      componentProps: {
        placeholder: '不限部门',
        allowClear: true,
        showSearch: true,
        filterOption: (inputValue: string, option: { label: string }) => {
          return option.label.toLowerCase().includes(inputValue.toLowerCase());
        },
        options: deptNameOptions,
        mode: 'multiple',
        hidden: hideAdminSearchCondition,
      },
    },
    {
      component: 'Select',
      fieldName: 'userIdList',
      label: '数据归属人',
      hideLabel: hideAdminSearchCondition,
      componentProps: {
        placeholder: '不限',
        allowClear: true,
        showSearch: true,
        filterOption: (inputValue: string, option: { label: string }) => {
          return option.label.toLowerCase().includes(inputValue.toLowerCase());
        },
        options: userNameOptions,
        mode: 'multiple',
        hidden: hideAdminSearchCondition,
      },
    },
    {
      component: 'Select',
      fieldName: 'ownerFavorite',
      label: '收藏类型',
      hideLabel: hideAdminSearchCondition,
      help: '用于查看客户信息在归属人的哪个菜单中',
      componentProps: {
        placeholder: '不限',
        allowClear: true,
        options: FavoriteTypeEnum.getFavOptions(),
        hidden: hideAdminSearchCondition,
      },
    },
    {
      component: 'Select',
      fieldName: 'channel',
      label: '渠道',
      componentProps: {
        placeholder: '不限',
        allowClear: true,
        showSearch: true,
        options: channelOptions,
        filterOption: (inputValue: string, option: { label: string }) => {
          return option.label.toLowerCase().includes(inputValue.toLowerCase());
        },
      },
    },
  ],
  showCollapseButton: false,
  submitButtonOptions: {
    content: '查询',
  },
  submitOnChange: false,
  submitOnEnter: true,
};

const gridOptions: VxeGridProps<CustomerItem> = {
  cellStyle: {
    padding: '3px 1px',
  },
  rowClassName: (params) => {
    // 若数据没有最后跟进人， 则换高亮背景提示
    if (params?.row && params?.row.followerUserId === 0) {
      return 'bg-zinc-600/50 text-white';
    }
  },
  // virtualYConfig: {
  //   gt: 10,
  //   threshold: 18,
  //   preSize: 10,
  // },
  // height: 800,
  // 客户姓名，手机号，性别，分组，跟进状态， 客户备注， 最后跟进时间， 跟进次数
  columns: [
    {
      title: '',
      field: 'idPicker',
      width: 50,
      fixed: 'left',
      type: 'checkbox',
    },
    { title: '姓名', field: 'name', width: 100, fixed: 'left' },
    { title: '手机号', field: 'mobile', width: 130 },
    { title: '性别', field: 'sexDesc', width: 60 },
    { title: '需要金额', field: 'reqLoanAmount', width: 100 },
    {
      title: '客户星级',
      field: 'customerGroupDesc',
      width: 120,
      titleSuffix: {
        content: custGroupDesc.value,
      },
    },
    {
      title: '跟进状态',
      field: 'progressDesc',
      width: 100,
      className: (params: { row: { progress: number } }) => {
        if (HIGHT_LIGHT_PROGRESS_VALUES.has(params?.row?.progress)) {
          return 'text-red-600';
        }
      },
    },
    { title: '客户备注', field: 'customerRemark', width: 200 },
    {
      title: '最后跟进时间',
      field: 'followTime',
      width: 180,
    },
    {
      title: '跟进次数',
      field: 'followCnt',
      width: 80,
    },

    { title: '年龄', field: 'age', width: 60 },
    {
      title: '数据归属人',
      field: 'ownerUserName',
      width: 100,
      visible: hasMaintainRole,
    },
    {
      title: '数据归属部门',
      field: 'ownerDeptName',
      width: 100,
      visible: hasMaintainRole,
    },
    { title: '最后跟进人', field: 'followerUserName', width: 100 },
    {
      title: '收藏类型',
      field: 'ownerFavoriteDesc',
      width: 130,
      visible: hasMaintainRole,
    },
    {
      title: '推广渠道',
      field: 'channelDesc',
      minWidth: 180,
    },
    {
      title: '资质',
      field: 'qualification',
      width: 200,
      showOverflow: false,
      padding: true,
    },
    {
      title: '房产',
      field: 'houseFlagValDesc',
      width: 100,
      showOverflow: false,
      padding: true,
    },
    {
      title: '公积金',
      field: 'providentFlagValDesc',
      width: 80,
    },
    { title: '婚姻状况', field: 'marriageStatusDesc', width: 100 },
    { title: '沟通结果', field: 'callTipsDesc', width: 100 },
    { title: '跟进情况备注', field: 'followRemark', width: 200 },
    { title: '申请时间', field: 'applyDate', width: 180 },

    {
      field: 'action',
      title: '操作',
      width: 100,
      slots: { default: 'action' },
      fixed: 'right',
    },
  ],
  showOverflow: false,
  pagerConfig: {
    enabled: true,
    pageSize: 30,
    pageSizes: [20, 30, 50, 100],
    layouts: [
      'Total',
      'Sizes',
      'Home',
      'PrevJump',
      'PrevPage',
      'Number',
      'NextPage',
      'NextJump',
      'Jump',
      'End',
    ],
  },
  border: true,
  proxyConfig: {
    ajax: {
      query: async (component: { page: any }, formValues: any) => {
        return await pagedListCustomerInfo({
          page: component.page.currentPage,
          pageSize: component.page.pageSize,
          ...formValues,
        });
      },
    },
  },
  rowConfig: {
    isHover: true,
    keyField: 'row.id',
  },
};
async function pagedListCustomerInfo(formValues?: any) {
  const data: PagedInfo<CustomerItem> = await customerApi.pagedListCustomerInfo(
    {
      ...formValues,
      followTimeStart: formValues.followTimePicker?.[0].format(DATA_TIME_FMT),
      followTimeEnd: formValues.followTimePicker?.[1].format(DATA_TIME_FMT),
      applyDateStart: formValues.applyDatePicker?.[0].format(DATA_TIME_FMT),
      applyDateEnd: formValues.applyDatePicker?.[1].format(DATA_TIME_FMT),
      // 优先使用传入的favoriteFlag作为筛选条件，若有 prop.favoriteFlag, 说明当前的页面处于【xx客户】的收藏页,不可对收藏类型进行筛选
      ownerFavorite:
        props.favoriteFlag === null
          ? formValues.ownerFavorite
          : props.favoriteFlag,
      ownerUserId:
        props.favoriteFlag !== null && props.favoriteFlag >= 0
          ? userId
          : undefined,
      selfOnly: props.favoriteFlag !== null,
      orderByFollowTimeDesc,
      orderByApplyDateDesc,
    },
  );

  cidList = [];
  data.list?.forEach((item: CustomerItem) => {
    cidList.push(item.id);
    item.ownerFavoriteDesc = item.ownerUserId
      ? FavoriteTypeEnum.getFavTypeName(item.ownerFavorite)
      : '';
    item.houseFlagValDesc = item.houseVal ? `${item.houseVal}万元` : '';
    item.providentFlagValDesc = item.providentAmountYuan
      ? `${item.providentAmountYuan}`
      : '';
  });

  return { items: Object.freeze(data.list), total: data.total };
}

function openModal(record: CustomerItem) {
  const modalData = {
    callbackFunc: gridApi.query,
    cid: record.id,
    idCardNo: record.idCardNo,
    uid: userId,
    customerGroupOptions,
    progressOptions,
    deptNameOptions,
    callTipsOptions,
    hasMaintainRole,
    cidList,
    callbackFnc: async () => {
      gridApi.query();
    },
  };
  modalApi.setData(modalData).open();
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
  await customerApi.batchOptFavorite(cids, favFlag).then(() => {
    message.success({
      content: `客户信息已移动至【${FavoriteTypeEnum.getFavTypeName(favFlag)}】`,
      duration: 3,
    });
    gridApi.query();
  });
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
  await customerApi.batchBackToOcean(cids).then(() => {
    gridApi.query();
    message.success({ content: '客户信息退回公海成功', duration: 3 });
  });
}

function returnCustomerConfirm(cids: number[], name: string, mobile: string) {
  if (!(cids && cids.length > 0)) {
    message.error('请选择要退回公海的客户');
    return;
  }
  const custDesc = name
    ? `客户 ${name}(${mobile}) 退回公海`
    : '所选客户批量退回公海';
  Modal.confirm({
    title: '退回客户信息',
    content: `确定要将${custDesc}吗?\n退回公海后，您将无法查看该客户信息，是否继续?`,
    onOk: () => {
      returnCustomer(cids);
    },
  });
}

function renderEmptySlot() {
  // do nothing
}
const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: searchFormOptions,
  gridOptions,
});
/**
 * 批量分配客户
 */
function batchAssign() {
  const custIds: number[] = [];
  const custDescList: string[] = [];
  gridApi.grid.getCheckboxRecords().forEach((item: any) => {
    custIds.push(item.id);
    custDescList.push(`${item.name} - ${item.mobile}`);
  });
  openAssignModal(custIds, custDescList);
}

function batchBackToOcean() {
  const custIds: number[] = pickCheckedCids() ?? [];
  if (custIds.length === 1) {
    returnCustomerConfirm(
      custIds,
      gridApi.grid.getCheckboxRecords()[0].name,
      gridApi.grid.getCheckboxRecords()[0].mobile,
    );
    return;
  }
  returnCustomerConfirm(custIds, '', '');
}

function batchSwitchFavorite(favFlag: number) {
  const custIds: number[] = pickCheckedCids() ?? [];
  switchFavorite(custIds, favFlag);
}

function pickCheckedCids() {
  const custIds: number[] = [];
  gridApi.grid.getCheckboxRecords().forEach((item: any) => {
    custIds.push(item.id);
  });
  if (custIds.length === 0) {
    message.error({ content: '请选择客户信息', duration: 3 });
    return;
  }
  return custIds;
}
</script>
<template>
  <div class="vp-raw w-full">
    <VbenModal />
    <Grid>
      <template #toolbar>
        <span class="mr-1">批量操作:</span>
        <Button
          size="small"
          type="link"
          class="mr-1"
          @click="batchAssign"
          v-if="favoriteFlag === null"
        >
          分配
        </Button>
        <Button
          size="small"
          type="link"
          class="-m-1 p-2"
          @click="batchSwitchFavorite(FavoriteTypeEnum.MY_CUST)"
          v-if="
            favoriteFlag !== null && favoriteFlag !== FavoriteTypeEnum.MY_CUST
          "
        >
          移入我的客户
        </Button>
        <Button
          size="small"
          type="link"
          class="-m-1 p-2"
          @click="batchSwitchFavorite(FavoriteTypeEnum.KEY_CUST)"
          v-if="
            favoriteFlag !== null && favoriteFlag !== FavoriteTypeEnum.KEY_CUST
          "
        >
          移入重点客户
        </Button>
        <Button
          size="small"
          type="link"
          class="-m-1 p-2"
          @click="batchSwitchFavorite(FavoriteTypeEnum.NORMAL)"
          v-if="
            favoriteFlag !== null && favoriteFlag !== FavoriteTypeEnum.NORMAL
          "
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
      </template>
      <template #action="{ row }">
        <div>
          <Button
            size="small"
            type="link"
            class="-m-1 p-2"
            @click="openModal(row)"
          >
            查看
          </Button>
          <Button
            size="small"
            type="link"
            class="-m-1 p-2"
            @click="assignCust(row.id, row.name, row.mobile)"
            v-if="hasMaintainRole && favoriteFlag === null"
          >
            分配
          </Button>
          <Button
            danger
            size="small"
            type="link"
            class="-m-1 p-2"
            title="将数据放入本部门公海"
            @click="
              returnCustomerConfirm([row.id], row.wrappedName, row.mobile)
            "
            v-if="userId === `${row.ownerUserId}` && favoriteFlag !== null"
          >
            退回
          </Button>
        </div>
      </template>

      <template #empty>
        {{ renderEmptySlot() }}
      </template>
    </Grid>
  </div>
  <AssignModal />
</template>
