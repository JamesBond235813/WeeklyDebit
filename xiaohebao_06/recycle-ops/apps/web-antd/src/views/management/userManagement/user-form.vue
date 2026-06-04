<script lang="ts" setup>
import type { DeptInfo } from '#/api/biz/dept';
import type { EditUserInfoParams, RoleInfo } from '#/api/biz/user';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { deptApi } from '#/api/biz/dept';
import { userApi } from '#/api/biz/user';

const outData = ref();
const [Modal, modalApi] = useVbenModal({
  destroyOnClose: true,
  draggable: false,
  closeOnPressEscape: false,
  centered: false,
  closeOnClickModal: false,
  class: 'w-2/3',
  onCancel() {
    modalApi.close();
  },
  onConfirm() {
    modalApi.setState({ loading: true });
    formApi.submitForm();
  },
  onClosed() {
    // 调用外部回调
    if (outData.value.callbackFunc) {
      outData.value.callbackFunc();
    }
  },
  onOpenChange(isOpen: boolean) {
    if (isOpen) {
      _initDeptInfo();
      outData.value = modalApi.getData<Record<string, any>>();
      // modalApi.setTitle = outData.title;
      modalApi.setState({
        title: outData.value.title,
        confirmText: outData.value.confirmText,
      });
      if (outData.value.data) {
        const _formData = { ...outData.value.data };
        formApi.setValues(_formData);
        const state = formApi.getState();
        const toUpdSchema = state?.schema
          ?.filter((e) => !['id', 'password', 'userName'].includes(e.fieldName))
          .map((e) => {
            return { fieldName: e.fieldName, disabled: outData.value.del };
          });
        toUpdSchema?.push(
          { fieldName: 'userName', disabled: true },
          {
            fieldName: 'password',
            disabled: !outData.value.del,
            value: '***',
          },
        );
        formApi.updateSchema(toUpdSchema);
      }
    }
  },
});
const [AddUserForm, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
  },
  showDefaultActions: false,
  // 提交函数
  handleSubmit: async (values: Record<string, any>) => {
    const userInfo = values as EditUserInfoParams;
    try {
      if (outData.value.data) {
        if (outData.value.del) {
          await userApi.delUserInfo(outData.value.data.id);
          afterSubmitSuccess('删除用户信息成功');
          return;
        }
        userInfo.id = outData.value.data.id;
        await userApi.updUserInfo(userInfo);
        afterSubmitSuccess('更新用户信息成功');
        return;
      }

      await userApi.addUserInfo(userInfo);
      afterSubmitSuccess('保存用户信息成功');
    } catch {
      // 全局请求拦截器已展示后端返回的具体失败原因；这里负责阻止未捕获异常。
    } finally {
      modalApi.setState({ loading: false });
    }
  },
  // 垂直布局，label和input在不同行，值为vertical
  // 水平布局，label和input在同一行
  layout: 'horizontal',
  schema: [
    {
      component: 'Input',
      fieldName: 'id',
      label: '用户ID',
      disabled: true,
      componentProps: {
        placeholder: '新增用户后自动生成',
      },
    },

    {
      component: 'Input',
      fieldName: 'employeeNo',
      label: '工号',
      componentProps: {
        placeholder: '不超过32个字符',
      },
    },
    {
      component: 'Input',
      fieldName: 'userName',
      label: '用户名',
      componentProps: {
        placeholder: '用户名,即用户登录账号，不可重复。不超过64个字符',
      },
      rules: 'required',
    },
    {
      component: 'Input',
      fieldName: 'realName',
      label: '用户姓名',
      componentProps: {
        placeholder: '请输入真实姓名,不超过32个字符',
      },
      rules: 'required',
    },

    {
      component: 'InputPassword',
      fieldName: 'password',
      label: '用户密码',
      componentProps: {
        placeholder: '请指定用户初始密码,8~16位，要求包含数字和字母',
      },
      rules: 'required',
    },

    {
      component: 'Input',
      fieldName: 'phone',
      label: '手机号',
      componentProps: {
        placeholder: '请输入用户手机号,要求为11位数字,且以1打头',
      },
      rules: 'required',
    },
    {
      component: 'Select',
      fieldName: 'departmentId',
      label: '所属部门',
      componentProps: {
        placeholder: '无归属部门',
        allowClear: true,
        showSearch: true,
        filterOption: (inputValue: string, option: { label: string }) => {
          return option.label.toLowerCase().includes(inputValue.toLowerCase());
        },
        options: [],
      },
    },
    {
      component: 'Input',
      fieldName: 'jobName',
      label: '职位',
      componentProps: {
        placeholder: '职位信息不超过32个字符',
      },
    },
    {
      component: 'Input',
      fieldName: 'email',
      label: '用户邮箱',
      componentProps: {
        placeholder: '请输入用户邮箱',
      },
    },
    {
      component: 'Select',
      fieldName: 'sex',
      label: '性别',
      componentProps: {
        // placeholder: '保密',
        allowClear: false,
        showSearch: false,
        options: [
          { label: '保密', value: 0 },
          { label: '男', value: 1 },
          { label: '女', value: 2 },
        ],
        defaultValue: 0,
      },
    },

    {
      label: '权限角色',
      component: 'CheckboxGroup',
      componentProps: {
        name: 'roles',
        gird: {
          column: 1,
        },
        options: [],
      },
      fieldName: 'roles',
    },
    {
      component: 'Input',
      fieldName: 'birthday',
      label: '生日',
      componentProps: {
        placeholder: '使用yyyy-MM-dd格式',
      },
    },
  ],
  wrapperClass: 'grid-cols-2',
});
function afterSubmitSuccess(msg: string) {
  message.success({
    content: msg,
    duration: 5,
  });
  // 关闭 MODAL
  modalApi.close();
}
function _initDeptInfo() {
  deptApi
    .listDeptInfo({ status: 1, needExtendQry: false })
    .then((data: any) => {
      const optionsPairs = data.map((item: DeptInfo) => {
        return { label: item.deptName, value: item.id };
      });
      formApi.updateSchema([
        {
          fieldName: 'departmentId',
          componentProps: { options: optionsPairs },
        },
      ]);
    });
  userApi.listAllRoles().then((data: any) => {
    const checkBoxOptions = data.map((item: RoleInfo) => {
      return {
        label: `${item.roleDispName}: ${item.desc}`,
        value: item.roleName,
      };
    });
    formApi.updateSchema([
      {
        fieldName: 'roles',
        componentProps: { options: checkBoxOptions },
      },
    ]);
  });
}
// onMounted(() => {
//   _initDeptInfo();
// });
</script>
<template>
  <Modal>
    <div class="w-full">
      <AddUserForm />
    </div>
  </Modal>
</template>
