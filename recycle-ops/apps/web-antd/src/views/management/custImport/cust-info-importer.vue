<script lang="ts" setup>
import type { UploadChangeParam } from 'ant-design-vue';

import type { DeptInfo } from '#/api/biz/dept';
import type { UserInfoTeamMember } from '#/api/biz/user';

import { onMounted, ref } from 'vue';

import { InboxOutlined } from '@vben/icons';
import { useAccessStore } from '@vben/stores';

import { Button, message, UploadDragger } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { deptApi } from '#/api/biz/dept';
import { userApi } from '#/api/biz/user';

interface ParentCallbacks {
  afterUploadSucc: () => void;
}
const props = defineProps<ParentCallbacks>();
const deptUserMap = new Map<number, { label: string; value: number }[]>();
const deptNameOptionsRef = ref<{ label: string; value: number }[]>([]);
const apiPrefix = import.meta.env.VITE_GLOB_API_URL;
const apiUrl = apiPrefix
  ? `${apiPrefix}/sys/cust/import-user-info`
  : '/api/sys/cust/import-user-info';
// console.log(`apiPrefix=${apiPrefix}, apiUrl=${apiUrl}`);
const asscessStore = useAccessStore();
const token = `Bearer ${asscessStore.accessToken}`;
const header = {
  ClientId: 'su-ops',
  Authorization: `${token}`,
};
const invalidateDataInfoRef = ref<string[]>([]);
const currentFileNameRef = ref<string>('');
const showInvalidateDivRef = ref<boolean>(false);
const handleChange = (info: UploadChangeParam) => {
  if (invalidateDataInfoRef.value) {
    clearInvalidateMsgRegion();
  }
  const status = info.file.status;
  if (status === 'error') {
    console.error(`${info.file.name} 上传失败: ${info.file.response ?? ''}`);
    message.error(`${info.file.name} 文件上传失败`);
    return;
  }
  if (status === 'done') {
    const response = info.file.response;
    if (response?.code !== 0) {
      if (response.data) {
        invalidateDataInfoRef.value = response.data;
        currentFileNameRef.value = info.file.name;
        showInvalidateDivRef.value = true;
      }
      message.error({
        content: `${info.file.name} ${response?.msg}`,
        duration: 5,
      });
      return;
    }
    message.success(`${info.file.name} file uploaded successfully.`);
    props.afterUploadSucc();
  }
};

const getUploadData = async () => {
  const values: any = await dispatchCustFormApi.getValues();
  if (values?.targetDeptId === undefined) {
    values.targetDeptId = null;
  }
  if (values) {
    values.targetDeptId = values.targetDeptId ?? '';
    values.targetUserId = values.targetUserId ?? '';
  }
  return values;
};

const [DispatchCustForm, dispatchCustFormApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full m-2',
      colon: true,
    },
  },
  wrapperClass: 'grid-cols-2 text-left',
  showDefaultActions: false,
  layout: 'horizontal',
  schema: [
    {
      component: 'Select',
      fieldName: 'targetDeptId',
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
      fieldName: 'targetUserId',
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
            options: deptUserMap.get(values.targetDeptId) ?? [],
          };
        },
        triggerFields: ['targetDeptId'],
      },
      colon: true,
    },
  ],
});
/**
 * 查询当前在线的用户信息
 */
async function initUserOptions() {
  userApi.listTeamUser().then((res: UserInfoTeamMember[]) => {
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

  deptApi
    .listDeptInfo({
      status: 1,
      needExtendQry: false,
    })
    .then((deptRes) => {
      deptNameOptionsRef.value = deptRes.map((e: DeptInfo) => {
        return { label: e.deptName, value: e.id };
      });
    });
}

function clearInvalidateMsgRegion() {
  invalidateDataInfoRef.value = [];
  currentFileNameRef.value = '';
  showInvalidateDivRef.value = false;
}

function copyInvalidateMsg() {
  if (!invalidateDataInfoRef.value) {
    return;
  }
  const content: string = invalidateDataInfoRef.value.join('\n');
  navigator.clipboard.writeText(content).then(() => {
    message.info('已将错误信息复制到剪切板');
  });
}

onMounted(() => {
  initUserOptions();
});
</script>
<template>
  <div>
    <UploadDragger
      name="file"
      :action="apiUrl"
      :multiple="false"
      :headers="header"
      :data="getUploadData"
      :show-upload-list="false"
      @change="handleChange"
    >
      <p class="padding-1">
        <InboxOutlined class="size-20 w-full object-center" />
      </p>
      <p class="ant-upload-text">
        点击或将文件拖到本区域即可上传文件并导入记录
      </p>
    </UploadDragger>
    <DispatchCustForm />
    <div class="p-1 text-sm text-blue-500">
      <p>导入事项说明：</p>
      <p>
        1.&nbsp;上传文件前，请先确认数据分配的对象，默认情况下，导入的数据将分配到本部门公海。上传文件后点击列表右角刷新图标，可刷新查看导入数据进度
      </p>
      <p>
        2.&nbsp;上传文件后，若文件内容校验不通过，则不会出现操作记录，但会提示校验不通过的原因，请按提示修正后重新上传文件
      </p>
      <p>
        3.&nbsp;上传文件导入数据时，遇已存在的数据，仅更新【申请时间】、【来源文件】、【渠道】（若指定渠道的话），导入完成后，已存在的数据将汇成文件可供下载。
      </p>
      <p>
        4.&nbsp;同名文件不可重复导入。多个用户或者多个文件同时导入时，可能会因为数据内容重复而导致导入失败，届时请稍等片刻后重新上传文件
      </p>
    </div>
    <div
      class="rounded border-2 border-dashed border-rose-200 bg-white p-1"
      v-if="showInvalidateDivRef"
    >
      <Button
        size="small"
        type="link"
        @click="clearInvalidateMsgRegion()"
        class="float-right"
      >
        X
      </Button>
      <Button
        size="small"
        type="link"
        @click="copyInvalidateMsg()"
        class="float-right text-cyan-800"
      >
        复制内容
      </Button>
      <p class="text-rose-600">
        {{ `文件【${currentFileNameRef}】 数据校验未通过: ` }}
      </p>
      <div class="h-64 w-full overflow-y-auto">
        <p v-for="(info, index) in invalidateDataInfoRef" :key="index">
          {{ info }}
        </p>
      </div>
    </div>
  </div>
</template>
