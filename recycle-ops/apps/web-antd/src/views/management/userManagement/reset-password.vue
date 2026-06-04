<script lang="ts" setup>
import type { ResetPasswordParams } from '#/api/biz/user';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { Descriptions, message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { userApi } from '#/api/biz/user';

const outData = ref();
const [ResetPswdModal, resetPswdModalApi] = useVbenModal({
  destroyOnClose: true,
  draggable: false,
  closeOnPressEscape: false,
  centered: false,
  closeOnClickModal: false,
  class: 'w-1/3',
  title: '重置密码',
  onCancel() {
    resetPswdModalApi.close();
  },
  onConfirm() {
    resetPswdModalApi.setState({ loading: true });
    resetPswdFormApi.submitForm();
  },
  onClosed() {
    // 调用外部回调
    if (outData.value.callbackFunc) {
      outData.value.callbackFunc();
    }
  },
  onOpenChange(isOpen: boolean) {
    if (isOpen) {
      outData.value = resetPswdModalApi.getData<Record<string, any>>();
    }
  },
});
const [ResetPswdForm, resetPswdFormApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
  },
  showDefaultActions: false,
  // 提交函数
  handleSubmit: async (values: Record<string, any>) => {
    const resetPswdRequest = {
      password: values.password,
      id: outData.value.data.id,
    } as ResetPasswordParams;
    const validateResult = await resetPswdFormApi.validate();
    if (!validateResult.valid) {
      resetPswdModalApi.setState({ loading: false });
      return;
    }
    if (resetPswdRequest.password !== values.confirmPassword) {
      message.error('两次输入的密码不一致,请重新输入');
      resetPswdModalApi.setState({ loading: false });
      return;
    }
    userApi
      .resetUserPwd(resetPswdRequest)
      .then(() => {
        message.success('更新用户信息成功');
        resetPswdModalApi.close();
      })
      .finally(() => {
        resetPswdModalApi.setState({ loading: false });
      });
  },
  // 垂直布局，label和input在不同行，值为vertical
  // 水平布局，label和input在同一行
  layout: 'horizontal',
  schema: [
    {
      component: 'InputPassword',
      fieldName: 'password',
      label: '新密码',
      componentProps: {
        placeholder: '请指定用户初始密码,8~16位，要求包含数字和字母',
      },
      rules: 'required',
    },

    {
      component: 'InputPassword',
      fieldName: 'confirmPassword',
      label: '确认新密码',
      componentProps: {
        placeholder: '请指定用户初始密码,8~16位，要求包含数字和字母',
      },
      rules: 'required',
    },
  ],
  wrapperClass: 'grid-cols-1',
});
</script>
<template>
  <ResetPswdModal>
    <div class="w-full"></div>
    <Descriptions :column="1">
      <Descriptions.Item label="用户名(登录账号)">
        {{ outData.data?.userName }}
      </Descriptions.Item>
      <Descriptions.Item label="姓名">
        {{ outData.data?.realName }}
      </Descriptions.Item>
    </Descriptions>
    <ResetPswdForm />
  </ResetPswdModal>
</template>
