<script lang="ts" setup>
import type { DispatchCustomerInfoParams } from '#/api/biz/customer';
import type { UserInfoTeamMember } from '#/api/biz/user';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { Descriptions, message } from 'ant-design-vue';

// import { VbenSelect } from 'packages/@core/ui-kit/shadcn-ui/src/components/select/index.ts';
import { useVbenForm } from '#/adapter/form';
import { customerApi } from '#/api/biz/customer';
import { userApi } from '#/api/biz/user';

const deptUserMap = new Map<number, { label: string; value: number }[]>();
const deptNameOptionsRef = ref<{ label: string; value: number }[]>([]);
const custDescListRef = ref<string[]>([]);
let callbackFunc = () => {};
let toAssignCustIds: number[] = [];
// 分配客户的 modal
const [CustMngDispatchModal, custMngDispatchModalApi] = useVbenModal({
  destroyOnClose: true,
  draggable: false,
  closeOnPressEscape: false,
  centered: false,
  closeOnClickModal: false,
  class: 'w-3/5',
  title: '分配客户',
  cancelText: '取消',
  confirmText: '确定',
  onClosed() {
    if (callbackFunc) {
      callbackFunc();
    }
  },
  onCancel() {
    custMngDispatchModalApi.close();
  },
  onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      return;
    }
    custMngDispatchModalApi.setState({ loading: true });
    const outData = custMngDispatchModalApi.getData<Record<string, any>>();
    toAssignCustIds = outData.cids;
    callbackFunc = outData.callbackFunc;
    custDescListRef.value = outData.custDescList;
    deptNameOptionsRef.value = outData.deptNameOptions;
    initUserOptions().then(() => {
      custMngDispatchModalApi.setState({ loading: false });
    });
  },
  onConfirm() {
    // 分配客户
    custMngDispatchModalApi.setState({ loading: true });
    dispatchCustFormApi
      .getValues()
      .then((values) => {
        const params: DispatchCustomerInfoParams = {
          cids: toAssignCustIds,
          ownerId: values.assignUserId ?? 0,
          ownerDeptId: values.ownerDeptId ?? 0,
        };
        customerApi
          .dispatchCustomerInfo(params)
          .then(() => {
            message.success({ content: '分配客户成功', duration: 3 });

            custMngDispatchModalApi.close();
          })
          .finally(() => {
            custMngDispatchModalApi.setState({ loading: false });
          });
      })
      .catch(() => {
        custMngDispatchModalApi.setState({ loading: false });
      });
  },
});
const [DispatchCustForm, dispatchCustFormApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
      colon: true,
    },
  },
  showDefaultActions: false,
  layout: 'vertical',
  schema: [
    {
      component: 'Select',
      fieldName: 'ownerDeptId',
      label: '部门',
      componentProps: {
        placeholder: '分配到企业公海',
        allowClear: true,
        showSearch: true,
        filterOption: (inputValue: string, option: { label: string }) => {
          return option.label.toLowerCase().includes(inputValue.toLowerCase());
        },
        options: deptNameOptionsRef,
      },
      colon: true,
    },
    {
      component: 'Select',
      fieldName: 'assignUserId',
      label: '在线员工',
      componentProps: {
        placeholder: '分配到部门公海',
        allowClear: true,
        showSearch: true,
        filterOption: (inputValue: string, option: { label: string }) => {
          return option.label.toLowerCase().includes(inputValue.toLowerCase());
        },
        options: [],
      },
      dependencies: {
        componentProps(values) {
          return {
            options: deptUserMap.get(values.ownerDeptId) ?? [],
          };
        },
        triggerFields: ['ownerDeptId'],
      },
      colon: true,
    },
  ],
});

/**
 * 查询当前在线的用户信息
 */
async function initUserOptions() {
  await userApi.listTeamUser().then((res: UserInfoTeamMember[]) => {
    deptUserMap.clear();
    res.forEach((e: UserInfoTeamMember) => {
      let list = deptUserMap.get(e.deptId);
      if (!list) {
        list = [];
        deptUserMap.set(e.deptId, list);
      }
      list.push({ label: e.name, value: e.id });
    });
  });
}
</script>

<template>
  <CustMngDispatchModal>
    <Descriptions :column="3">
      <template v-for="item in custDescListRef" :key="item">
        <Descriptions.Item>
          {{ item }}
        </Descriptions.Item>
      </template>
    </Descriptions>
    <DispatchCustForm />
  </CustMngDispatchModal>
</template>
