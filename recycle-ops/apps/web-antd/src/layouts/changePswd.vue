<script lang="ts" setup>
import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { changePassword } from '#/api/core/auth';

const [ChgPswdModal, chgPswdModalApi] = useVbenModal({
  destroyOnClose: true,
  draggable: false,
  closeOnPressEscape: false,
  centered: false,
  closeOnClickModal: false,
  class: 'w-1/3',
  onCancel() {
    chgPswdModalApi.close();
  },
  onConfirm() {
    chgPswdModalApi.setState({ loading: true });
    // 校验表单
    chgPswdFormApi
      .validate()
      .then((validateResult) => {
        if (!validateResult || !validateResult.valid) {
          chgPswdModalApi.setState({ loading: false });
          return;
        }
        chgPswdFormApi.getValues().then((values) => {
          const params = { ...values };
          if (params.newPassword !== params.confirmPassword) {
            throw new Error('两次输入的密码不一致，请确认后再操作');
          }
          // 提交改密
          changePassword(params)
            .then(() => {
              message.success({ content: '密码修改成功', duration: 3 });
              chgPswdModalApi.setState({ loading: false });
              chgPswdModalApi.close();
            })
            .catch(() => {
              chgPswdModalApi.setState({ loading: false });
            });
        });
      })
      .catch(() => {
        chgPswdModalApi.setState({ loading: false });
        chgPswdModalApi.close();
      });
  },
});
const [ChgPswdForm, chgPswdFormApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-3/4',
    },
  },
  showDefaultActions: false,
  layout: 'vertical',
  schema: [
    {
      component: 'InputPassword',
      fieldName: 'oldPassword',
      label: '原密码',
      componentProps: {
        placeholder: '请输入原密码',
      },
      rules: 'required',
    },
    {
      component: 'InputPassword',
      fieldName: 'newPassword',
      label: '新密码',
      componentProps: {
        placeholder: '请输入新密码,8~16位，要求包含数字和字母',
      },
      rules: 'required',
    },
    {
      component: 'InputPassword',
      fieldName: 'confirmPassword',
      label: '确认新密码',
      componentProps: {
        placeholder: '请再次输入新密码,8~16位，要求包含数字和字母',
      },
      rules: 'required',
    },
  ],
});
</script>
<template>
  <ChgPswdModal>
    <ChgPswdForm />
  </ChgPswdModal>
</template>
