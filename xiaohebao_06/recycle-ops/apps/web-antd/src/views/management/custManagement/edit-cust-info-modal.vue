<script lang="ts" setup>
import type { UpdCustomerInfoParams } from '#/api/biz/customer';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';
import dayjs from 'dayjs';

import { useVbenForm } from '#/adapter/form';
import { customerApi } from '#/api/biz/customer';

const deptNameOptionsRef = ref<{ label: string; value: number }[]>([]);
const dataChannelOptionsRef = ref<{ label: string; value: number }[]>([]);
const canSelectDeptRef = ref(false);
let customerId: number;
const DATA_TIME_FMT = 'YYYY-MM-DD HH:mm:ss';
let callbackFunc: () => void;
const [EditCustModal, editCustModalApi] = useVbenModal({
  destroyOnClose: true,
  draggable: false,
  closeOnPressEscape: false,
  centered: false,
  closeOnClickModal: false,
  class: 'w-3/5',
  confirmText: '保存',
  onClosed: () => {
    // 刷新表格
    if (callbackFunc) {
      callbackFunc();
    }
  },
  onCancel() {
    editCustModalApi.close();
  },
  onConfirm() {
    // formApi.submitForm();
    editCustModalApi.setState({ loading: true });
    editCustInfoFormApi
      .validate()
      .then((validateResult) => {
        if (!validateResult || !validateResult.valid) {
          editCustModalApi.setState({ loading: false });
          return;
        }
        editCustInfoFormApi.getValues().then((values) => {
          const params = {
            ...values,
            id: customerId,
            applyDateStr: values.applyDate?.format(DATA_TIME_FMT),
          } as UpdCustomerInfoParams;
          const promise = customerId
            ? customerApi.updCustomerInfo(params)
            : customerApi.addCustomerInfo(params);
          promise
            .then(() => {
              message.success({ content: '保存客户信息成功', duration: 3 });
              editCustModalApi.setState({ loading: false });
              editCustModalApi.close();
            })
            .catch(() => {
              editCustModalApi.setState({ loading: false });
            });
        });
      })
      .catch(() => {
        editCustModalApi.setState({ loading: false });
      });
  },
  onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      return;
    }
    const outData = editCustModalApi.getData<Record<string, any>>();
    customerId = outData.cid;
    callbackFunc = outData.callbackFunc;
    canSelectDeptRef.value = Boolean(outData.canSelectDept);
    editCustModalApi.setState({
      title: customerId ? '编辑客户信息' : '新增客户信息',
    });
    deptNameOptionsRef.value = outData.deptNameOptions.value;
    dataChannelOptionsRef.value = outData.dataChannelOptions.value;
    editCustInfoFormApi.updateSchema([
      {
        fieldName: 'ownerDeptId',
        componentProps: {
          allowClear: true,
          disabled: !canSelectDeptRef.value,
          filterOption: (inputValue: string, option: { label: string }) => {
            return option.label
              .toLowerCase()
              .includes(inputValue.toLowerCase());
          },
          options: deptNameOptionsRef,
          placeholder: canSelectDeptRef.value
            ? '将数据分配至指定部门的公海'
            : '系统将按当前用户所属部门写入',
          showSearch: true,
        },
      },
    ]);
    // 若为编辑，则去服务端拉详情数据
    if (!customerId) {
      editCustInfoFormApi.setValues({
        ownerDeptId: undefined,
      });
      return;
    }
    editCustModalApi.setState({ loading: true });
    customerApi.getCustomerDetail(customerId).then((res) => {
      editCustInfoFormApi.setValues({
        ...res,
        applyDate: res.applyDate
          ? dayjs(res?.applyDate as unknown as string)
          : null,
        channel: res.channel && res.channel !== 0 ? Number(res.channel) : null,
      });
      editCustModalApi.setState({ loading: false });
    });
  },
});
const [EditCustInfoForm, editCustInfoFormApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
  },
  showDefaultActions: false,
  layout: 'horizontal',
  schema: [
    {
      component: 'Input',
      fieldName: 'name',
      label: '客户姓名',
      rules: 'required',
      componentProps: { placeholder: '请输入客户姓名' },
    },
    {
      component: 'Input',
      fieldName: 'mobile',
      label: '手机号',
      rules: 'required',
      componentProps: { placeholder: '请输入客户手机号' },
    },
    {
      component: 'DatePicker',
      fieldName: 'applyDate',
      label: '申请时间',
      componentProps: {
        placeholder: '请输入申请时间（进本平台件的时间）',
        showTime: true,
        showNow: true,
        showToday: true,
        format: DATA_TIME_FMT,
      },
    },
    {
      component: 'Input',
      fieldName: 'idCardNo',
      label: '身份证号',
      componentProps: { placeholder: '请输入客户身份证号' },
    },

    {
      component: 'Input',
      fieldName: 'sourceFileName',
      label: '数据来源',
      componentProps: { placeholder: '请输入数据来源文件名称' },
    },

    {
      component: 'Select',
      fieldName: 'channel',
      label: '推广渠道',
      componentProps: {
        placeholder: '请输入推广渠道',
        allowClear: true,
        options: dataChannelOptionsRef,
      },
    },

    {
      component: 'Select',
      fieldName: 'sex',
      label: '性别',
      componentProps: {
        placeholder: '请选择',
        options: [
          { label: '保密', value: 0 },
          { label: '男', value: 1 },
          { label: '女', value: 2 },
        ],
        defaultValue: 0,
      },
    },

    {
      component: 'Input',
      fieldName: 'age',
      label: '年龄',
      componentProps: { placeholder: '请输入年龄' },
    },
    {
      component: 'Input',
      fieldName: 'birthday',
      label: '出生日期',
      componentProps: { placeholder: '请输入出生日期。 yyyy-MM-dd 格式' },
    },
    {
      component: 'Select',
      fieldName: 'ownerDeptId',
      label: '归属部门',
      componentProps: {
        placeholder: '将数据分配至指定部门的公海',
        allowClear: true,
        showSearch: true,
        filterOption: (inputValue: string, option: { label: string }) => {
          return option.label.toLowerCase().includes(inputValue.toLowerCase());
        },
        options: deptNameOptionsRef,
      },
    },
  ],
  wrapperClass: 'grid-cols-3',
});
</script>
<template>
  <EditCustModal>
    <EditCustInfoForm />
  </EditCustModal>
</template>
