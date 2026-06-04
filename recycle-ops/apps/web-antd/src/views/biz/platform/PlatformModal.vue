<script lang="ts" setup>
import { ref } from 'vue';
import { useVbenModal } from '@vben/common-ui';
import { useVbenForm } from '#/adapter/form';
import { message } from 'ant-design-vue';
import { savePlatform } from '#/api/biz/platform';

const emit = defineEmits(['success']);

const isUpdate = ref(false);
const recordId = ref<number | undefined>(undefined);

const [Form, formApi] = useVbenForm({
  wrapperClass: 'grid-cols-4 gap-x-4 gap-y-2',
  showDefaultActions: false,
  layout: 'horizontal',
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },  
  },
  schema: [
    {
      component: 'Input',
      fieldName: 'name',
      label: '平台名称',
      rules: 'required',
      componentProps: {
        placeholder: '请输入平台名称',
        style: 'max-width: 260px',
      },
      formItemClass: 'col-span-3 pr-2',
    },
    {
      component: 'Switch',
      fieldName: 'status',
      label: '启用状态',
      componentProps: {
        checkedChildren: '启',
        unCheckedChildren: '禁',
        size: 'small',
        style: 'min-width: 52px; width: auto',
      },
      defaultValue: true,
      formItemClass: 'col-span-1 justify-self-end platform-status-item',
      labelClass: 'whitespace-nowrap',
    },
    {
      component: 'Select',
      fieldName: 'type',
      label: '类型',
      rules: 'required',
      componentProps: {
        placeholder: '请选择类型',
        options: [
          { label: '分期', value: 'INSTALLMENT' },
          { label: '租赁', value: 'RENTAL' },
        ],
      },
      formItemClass: 'col-span-4',
    },
    {
      component: 'Input',
      fieldName: 'link',
      label: '链接地址',
      componentProps: {
        placeholder: '请输入链接地址',
      },
      formItemClass: 'col-span-4',
    },
    {
      component: 'Textarea',
      fieldName: 'remark',
      label: '备注',
      componentProps: {
        placeholder: '请输入备注',
        rows: 3,
      },
      formItemClass: 'col-span-4',
    },
  ],
});

const [Modal, modalApi] = useVbenModal({
  title: '平台信息',
  onConfirm: handleSubmit,
  async onOpenChange(isOpen: boolean) {
    if (isOpen) {
      const data = modalApi.getData<Record<string, any>>();
      isUpdate.value = !!data?.record;
      recordId.value = data?.record?.id;
      
      if (isUpdate.value) {
        // Transform status 1/0 to boolean for Switch if needed, or keep as number if Switch handles it.
        // Antdv Switch usually expects boolean. Backend uses 1/0.
        const formData = { ...data.record };
        formData.status = formData.status === 1;
        formApi.setValues(formData);
      } else {
        await formApi.resetForm();
        formApi.setValues({ status: true });
      }
    }
  },
});

async function handleSubmit() {
  try {
    const { valid } = await formApi.validate();
    if (!valid) return;
    
    modalApi.setState({ loading: true });
    
    const values = await formApi.getValues();
    const payload = {
      ...values,
      id: isUpdate.value ? recordId.value : undefined,
      status: values.status ? 1 : 0, 
    };

    await savePlatform(payload);
    message.success('保存成功');
    emit('success');
    modalApi.close();
  } catch (error) {
    console.error(error);
  } finally {
    modalApi.setState({ loading: false });
  }
}
</script>

<template>
  <Modal>
    <div class="p-5">
       <Form />
    </div>
  </Modal>
</template>

<style scoped>
.platform-status-item :deep(.ant-form-item-label > label) {
  pointer-events: none;
}
</style>
