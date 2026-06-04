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

const userDeptMap = new Map<number, number>();
const teamUserOptions = ref<{ label: string; value: number }[]>([]);
const custDescListRef = ref<string[]>([]);
let callbackFunc = () => {};
let toAssignCustIds: number[] = [];
// 分配客户的 modal
const [DispatchCustModal, dispatchCustModalApi] = useVbenModal({
  destroyOnClose: true,
  draggable: false,
  closeOnPressEscape: false,
  centered: false,
  closeOnClickModal: false,
  class: 'w-3/5',
  title: '分配客户',
  cancelText: '取消',
  confirmText: '确定',
  onCancel() {
    dispatchCustModalApi.close();
  },
  onBeforeClose: async () => {
    if (callbackFunc) {
      await callbackFunc();
    }
  },
  onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      return;
    }
    dispatchCustModalApi.setState({ loading: true });
    const outData = dispatchCustModalApi.getData<Record<string, any>>();
    toAssignCustIds = outData.cids;
    callbackFunc = outData.callbackFunc;
    custDescListRef.value = outData.custDescList;
    initUserOptions().then(() => {
      dispatchCustModalApi.setState({ loading: false });
    });
  },
  onConfirm() {
    // 分配客户
    dispatchCustModalApi.setState({ loading: true });
    dispatchCustFormApi
      .getValues()
      .then((values) => {
        const cid = values.assignUserId;
        const params: DispatchCustomerInfoParams = {
          cids: toAssignCustIds,
          ownerId: values.assignUserId,
          ownerDeptId: userDeptMap.get(cid),
        };
        customerApi
          .dispatchCustomerInfo(params)
          .then(() => {
            message.success({ content: '分配客户成功', duration: 3 });
            dispatchCustModalApi.close();
          })
          .finally(() => {
            dispatchCustModalApi.setState({ loading: false });
          });
      })
      .catch(() => {
        dispatchCustModalApi.setState({ loading: false });
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
      fieldName: 'assignUserId',
      label: '将以上客户分配给用户',
      componentProps: {
        placeholder: '请选择',
        allowClear: true,
        showSearch: true,
        filterOption: (inputValue: string, option: { label: string }) => {
          return option.label.toLowerCase().includes(inputValue.toLowerCase());
        },
        options: teamUserOptions,
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
    userDeptMap.clear();
    teamUserOptions.value = res.map((e: UserInfoTeamMember) => {
      userDeptMap.set(e.id, e.deptId);
      return { label: `${e.name} - (${e.deptName})`, value: e.id };
    });
  });
}
</script>

<template>
  <DispatchCustModal>
    <Descriptions :column="3">
      <template v-for="item in custDescListRef" :key="item">
        <Descriptions.Item>
          {{ item }}
        </Descriptions.Item>
      </template>
    </Descriptions>
    <DispatchCustForm />
  </DispatchCustModal>
</template>
