<script lang="ts" setup>
import type { DeptInfo, EditDepartmentInfoParams } from '#/api/biz/dept';

import { onMounted, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message, Space } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { deptApi } from '#/api/biz/dept';

const outData = ref();
const [Modal, modalApi] = useVbenModal({
  destroyOnClose: true,
  draggable: false,
  closeOnPressEscape: false,
  centered: false,
  closeOnClickModal: false,
  class: 'w-1/2',
  onClosed() {
    // 调用外部回调
    if (outData.value.callbackFunc) {
      outData.value.callbackFunc();
    }
  },
  onCancel() {
    modalApi.close();
  },
  onConfirm() {
    formApi.submitForm();
  },
  onOpenChange(isOpen: boolean) {
    if (isOpen) {
      outData.value = modalApi.getData<Record<string, any>>();
      // modalApi.setTitle = outData.title;
      modalApi.setState({
        title: outData.value.title,
        confirmText: outData.value.confirmText,
      });
      if (outData.value.deptInfo) {
        const _formData = { ...outData.value.deptInfo };
        if (_formData.parentDeptId === 0) {
          _formData.parentDeptId = null;
        }
        formApi.setValues(_formData);
        formApi.updateSchema([
          { fieldName: 'deptName', disabled: outData.value.del },
          { fieldName: 'deptCode', disabled: outData.value.del },
          { fieldName: 'parentDeptId', disabled: outData.value.del },
          { fieldName: 'introduction', disabled: outData.value.del },
        ]);
      }
    }
  },
});
const [AddDeptForm, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
  },
  showDefaultActions: false,
  // 提交函数
  handleSubmit: async (values: Record<string, any>) => {
    const deptInfo = values as EditDepartmentInfoParams;
    if (outData.value.deptInfo) {
      if (outData.value.del) {
        deptApi.delDeptInfo(outData.value.deptInfo.id).then(() => {
          afterSubmitSuccess('删除部门信息成功');
        });
        return;
      }
      deptInfo.id = outData.value.deptInfo.id;
      deptApi.editDeptInfo(deptInfo).then(() => {
        afterSubmitSuccess('更新部门信息成功');
      });
      return;
    }

    deptApi.addDeptInfo(deptInfo).then(() => {
      afterSubmitSuccess('保存部门信息成功');
    });
  },
  // 垂直布局，label和input在不同行，值为vertical
  // 水平布局，label和input在同一行
  layout: 'horizontal',
  schema: [
    {
      component: 'Input',
      fieldName: 'deptName',
      label: '部门名称',
      rules: 'required',
      componentProps: {
        placeholder: '请输入部门名称',
      },
    },
    {
      component: 'Input',
      fieldName: 'deptCode',
      label: '部门编号',
      componentProps: {
        placeholder: '请输入部门编号, 可不填',
      },
    },
    {
      component: 'Select',
      fieldName: 'parentDeptId',
      label: '上级部门',
      componentProps: {
        placeholder: '无上级部门',
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
      fieldName: 'introduction',
      label: '部门简介',
      componentProps: {
        placeholder: '请输入部门简介，可不填',
      },
    },
  ],
  wrapperClass: 'grid-cols-2',
});

function initDeptInfo() {
  deptApi
    .listDeptInfo({ status: 1, needExtendQry: false })
    .then((data: any) => {
      const optionsPairs = data
        // .filter((item: DeptInfo) => {
        //   if (outData.value?.deptInfo) {
        //     // 把当前待编辑的节点过滤掉，不让选择本部门作为上级部门
        //     return item.id !== outData.value.deptInfo.id;
        //   }
        //   return true;
        // })
        .map((item: DeptInfo) => {
          return { label: item.deptName, value: item.id };
        });
      formApi.updateSchema([
        {
          fieldName: 'parentDeptId',
          componentProps: { options: optionsPairs },
        },
      ]);
    });
}

function afterSubmitSuccess(msg: string) {
  message.success({
    content: msg,
    duration: 5,
  });

  // 关闭 MODAL
  modalApi.close();
}

onMounted(() => {
  initDeptInfo();
});
</script>
<template>
  <Modal>
    <Space />

    <div class="w-full">
      <AddDeptForm />
    </div>
  </Modal>
</template>
