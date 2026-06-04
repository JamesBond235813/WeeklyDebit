<script lang="ts" setup>
import { ref, computed } from 'vue';
import { Card, Tabs, TabPane, Table, Button, Tag, Switch, Space, Select, Input, InputNumber, message } from 'ant-design-vue';
import { useAccess } from '@vben/access';
import { useVbenModal } from '@vben/common-ui';
import { useVbenForm } from '#/adapter/form';
import { bizConfigApi, type BizDictItemDto, type BizDictType } from '#/api/biz/config';
import { userApi, type RoleInfo, type UserInfoRowItem } from '#/api/biz/user';

const access = useAccess();
const canDict = access.hasAccessByRoles(['ROLE_SUPPER','ROLE_DEPT_INFO_ADMIN']);
const canUser = access.hasAccessByRoles(['ROLE_SUPPER','ROLE_USER_INFO_ADMIN']);
const canField = access.hasAccessByRoles(['ROLE_SUPPER']);

// ---------- 字典配置 ----------
const currentType = ref<BizDictType>('PROGRESS');
const dictLoading = ref(false);
const dictData = ref<BizDictItemDto[]>([]);

async function loadDict() {
  dictLoading.value = true;
  try {
    dictData.value = await bizConfigApi.pageList(currentType.value, false);
  } finally {
    dictLoading.value = false;
  }
}
loadDict();

// ---------- 客户字段配置 ----------
const fieldLoading = ref(false);
const fieldData = ref<BizDictItemDto[]>([]);

async function loadFieldConfig() {
  fieldLoading.value = true;
  try {
    fieldData.value = await bizConfigApi.pageList('CUSTOMER_LIST_FIELD', false);
  } finally {
    fieldLoading.value = false;
  }
}
if (canField) {
  loadFieldConfig();
}

async function toggleFieldStatus(row: BizDictItemDto) {
  await bizConfigApi.update({
    id: row.id,
    bizType: row.bizType,
    status: row.status ? 0 : 1,
  });
  loadFieldConfig();
}

// ---------- 芝麻分阈值 ----------
const zhimaLoading = ref(false);
const zhimaThreshold = ref<number | null>(null);
const zhimaConfigId = ref<number | null>(null);
const zhimaLabel = '芝麻分阈值';
const enableRecvLoading = ref(false);
const enableRecv = ref(true);

async function loadZhimaThreshold() {
  zhimaLoading.value = true;
  try {
    const list = await bizConfigApi.pageList('ZHIMA_SCORE_THRESHOLD', false);
    const item = Array.isArray(list) && list.length ? list[0] : null;
    zhimaConfigId.value = item?.id ?? null;
    zhimaThreshold.value =
      typeof item?.intValue === 'number' ? item.intValue : null;
  } finally {
    zhimaLoading.value = false;
  }
}

async function saveZhimaThreshold() {
  if (zhimaThreshold.value === null || Number.isNaN(zhimaThreshold.value)) {
    message.error('请填写有效的芝麻分阈值');
    return;
  }
  zhimaLoading.value = true;
  try {
    if (zhimaConfigId.value) {
      await bizConfigApi.update({
        id: zhimaConfigId.value,
        bizType: 'ZHIMA_SCORE_THRESHOLD',
        intValue: zhimaThreshold.value,
        label: zhimaLabel,
        status: 1,
      });
    } else {
      await bizConfigApi.add({
        bizType: 'ZHIMA_SCORE_THRESHOLD',
        intValue: zhimaThreshold.value,
        label: zhimaLabel,
        status: 1,
      });
    }
    message.success('保存成功');
    loadZhimaThreshold();
  } finally {
    zhimaLoading.value = false;
  }
}

async function loadEnableRecvConfig() {
  enableRecvLoading.value = true;
  try {
    const data = await bizConfigApi.getEnableRecv();
    enableRecv.value = !!data?.enable;
  } finally {
    enableRecvLoading.value = false;
  }
}

async function onEnableRecvChange(checked: boolean | string | number) {
  const enabled = checked === true || checked === 'true' || checked === 1;
  enableRecvLoading.value = true;
  try {
    await bizConfigApi.updateEnableRecv({ enable: enabled });
    enableRecv.value = enabled;
    message.success(`接收上游流量已${enabled ? '开启' : '关闭'}`);
  } catch (error) {
    message.error('更新接收上游流量开关失败');
    await loadEnableRecvConfig();
  } finally {
    enableRecvLoading.value = false;
  }
}

if (canDict) {
  loadZhimaThreshold();
  loadEnableRecvConfig();
}

function openAddDict() {
  dictModalApi.setData({ mode: 'add', bizType: currentType.value }).open();
}
function openEditDict(row: BizDictItemDto) {
  dictModalApi.setData({ mode: 'edit', record: row }).open();
}

// 状态跟踪
const currentModalState = ref<{ mode: 'add' | 'edit'; id?: number }>({ mode: 'add' });

const [DictModal, dictModalApi] = useVbenModal({
  title: '配置详情',
  onConfirm: async () => {
    const { valid } = await dictFormApi.validate();
    if (!valid) return;
    const values = await dictFormApi.getValues();
    const { mode, id } = currentModalState.value;
    const payload: any = { ...values };
    // 后端期望 1/0，表单 Switch 返回布尔，需要转换
    if (typeof (payload as any).status === 'boolean') {
      payload.status = (payload as any).status ? 1 : 0;
    }
    if (mode === 'add') {
      await bizConfigApi.add({ ...payload, bizType: currentType.value });
    } else {
      await bizConfigApi.update({ ...payload, id });
    }
    message.success('保存成功');
    dictModalApi.close();
    loadDict();
  },
  onOpenChange(isOpen) {
    if (isOpen) {
      const data = dictModalApi.getData<Record<string, any>>();
      currentModalState.value = { mode: data.mode, id: data.record?.id };
      
      if (data.mode === 'add') {
        dictFormApi.resetForm();
        dictFormApi.setValues({ status: true, bizType: currentType.value });
      } else {
        dictFormApi.resetForm();
        dictFormApi.setValues({ ...data.record });
      }
    }
  },
});

const [DictForm, dictFormApi] = useVbenForm({
  layout: 'vertical',
  showDefaultActions: false,
  schema: [
    { component: 'Input', fieldName: 'bizType', label: '所属分类', componentProps: { disabled: true } },
    { component: 'InputNumber', fieldName: 'intValue', label: '系统编号', componentProps: { min: 0 } },
    { component: 'Input', fieldName: 'label', label: '显示文字' },
    { component: 'Input', fieldName: 'description', label: '备注' },
    { component: 'Switch', fieldName: 'status', label: '是否启用', componentProps: {}, defaultValue: true },
  ],
});

async function toggleStatus(row: BizDictItemDto) {
  await bizConfigApi.update({ id: row.id, bizType: row.bizType, status: row.status ? 0 : 1 });
  loadDict();
}

// ---------- 用户权限 ----------
const roleList = ref<RoleInfo[]>([]);
const userLoading = ref(false);
const users = ref<UserInfoRowItem[]>([]);
const query = ref({ name: '', userName: '' });

async function loadRoles() { roleList.value = await userApi.listAllRoles(); }
async function loadUsers() {
  userLoading.value = true;
  try { users.value = (await userApi.pagedListUserInfo({ page:1, pageSize:50, userRealNamePrefix: query.value.name, userName: query.value.userName, needExtendQry: true }))?.list || []; }
  finally { userLoading.value = false; }
}
loadRoles();
loadUsers();

function openEditRoles(row: UserInfoRowItem) {
  roleModalApi.setData({ user: row }).open();
}

const [RoleModal, roleModalApi] = useVbenModal({
  title: '编辑用户角色',
  onOpenChange(isOpen) {
    if (isOpen) {
      const { user } = roleModalApi.getData<Record<string, any>>();
      roleFormApi.resetForm();
      roleFormApi.setValues({ id: user.id, roles: (user.roles || []).map((r:any)=>r as string) });
    }
  },
  onConfirm: async () => {
    const { valid } = await roleFormApi.validate();
    if (!valid) return;
    const v = await roleFormApi.getValues();
    await userApi.updUserInfo({ id: v.id, roles: v.roles, userName: '', realName: '' } as any);
    message.success('已保存角色');
    roleModalApi.close();
    loadUsers();
  },
});

const [RoleForm, roleFormApi] = useVbenForm({
  layout: 'vertical', showDefaultActions: false,
  schema: [
    { component: 'Input', fieldName: 'id', label: '用户ID', componentProps: { disabled: true } },
    { component: 'Select', fieldName: 'roles', label: '角色', componentProps: {
      mode: 'multiple', options: computed(()=> (roleList.value || []).filter(Boolean).map(r=>({ label: `${r?.roleDispName}(${r?.roleName})`, value: r?.roleName })))
    } },
  ]
});
</script>

<template>
  <div class="p-4">
    <Tabs class="biz-config-tabs">
      <TabPane key="dict" tab="参数配置">
        <Card v-if="canDict" :bordered="false">
          <Card class="mb-3" size="small">
            <Space align="center" size="middle">
              <span class="text-sm text-gray-700">接收上游流量</span>
              <Switch
                :checked="enableRecv"
                :loading="enableRecvLoading"
                checked-children="开启"
                un-checked-children="关闭"
                @change="onEnableRecvChange"
              />
              <span class="text-xs text-gray-500">关闭后，CRM 将拒绝上游进量</span>
            </Space>
          </Card>
          <Space class="mb-3">
            <Select v-model:value="currentType" style="width:220px" @change="loadDict" :options="[
              {label:'跟进进度',value:'PROGRESS'},{label:'通话结论',value:'CALL_RESULT_TIPS'},{label:'客户等级',value:'CUSTOMER_STAR_GROUP'},{label:'数据来源',value:'DATA_CHANNEL'}
            ]"/>
            <Button type="primary" @click="openAddDict">新增</Button>
            <Button @click="loadDict">刷新</Button>
          </Space>
          <div ref="dictWrapRef">
          <Table :data-source="dictData" :loading="dictLoading" row-key="id" size="small" bordered>
            <Table.Column title="ID" dataIndex="id" width="80"/>
            <Table.Column title="所属分类" dataIndex="bizType" width="160"/>
            <Table.Column title="系统编号" dataIndex="intValue" width="100"/>
            <Table.Column title="显示文字" dataIndex="label"/>
            <Table.Column title="备注" dataIndex="description"/>
            <Table.Column title="是否启用" key="status" width="100"/>
            <Table.Column title="操作" key="action" width="120"/>
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <Switch :checked="record.status === 1" @change="() => toggleStatus(record)" />
              </template>
              <template v-else-if="column.key === 'action'">
                <Button type="link" @click="openEditDict(record)">编辑</Button>
              </template>
            </template>
          </Table>
          </div>
          <DictModal>
            <div class="p-4"><DictForm/></div>
          </DictModal>
        </Card>
        <Card v-else :bordered="false">无权限查看</Card>
      </TabPane>
      <TabPane key="custField" tab="客户字段">
        <Card v-if="canField" :bordered="false">
          <Space class="mb-3">
            <Button @click="loadFieldConfig">刷新</Button>
          </Space>
          <Table :data-source="fieldData" :loading="fieldLoading" row-key="id" size="small" bordered>
            <Table.Column title="字段名称" dataIndex="label" />
            <Table.Column title="字段Key" dataIndex="description" />
            <Table.Column title="显示" key="status" width="100" />
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <Switch :checked="record.status === 1" @change="() => toggleFieldStatus(record)" />
              </template>
            </template>
          </Table>
        </Card>
        <Card v-else :bordered="false">无权限查看</Card>
      </TabPane>
      <TabPane key="zhima" tab="芝麻分">
        <Card v-if="canDict" :bordered="false">
          <Space class="mb-3" align="center">
            <span>芝麻分阈值</span>
            <InputNumber
              v-model:value="zhimaThreshold"
              :min="0"
              :max="950"
              style="width: 140px"
              placeholder="请输入阈值"
            />
            <Button type="primary" :loading="zhimaLoading" @click="saveZhimaThreshold">保存</Button>
            <Button :loading="zhimaLoading" @click="loadZhimaThreshold">刷新</Button>
          </Space>
          <div class="text-xs text-gray-500">保存后立即生效</div>
        </Card>
        <Card v-else :bordered="false">无权限查看</Card>
      </TabPane>
      <TabPane key="perm" tab="用户权限">
        <Card v-if="canUser" :bordered="false">
          <Space class="mb-3">
            <Input v-model:value="query.name" placeholder="姓名" style="width:160px"/>
            <Input v-model:value="query.userName" placeholder="用户名" style="width:160px"/>
            <Button type="primary" @click="loadUsers">查询</Button>
          </Space>
          <Table :data-source="users" :loading="userLoading" row-key="id" size="small" bordered>
            <Table.Column title="ID" dataIndex="id" width="70"/>
            <Table.Column title="姓名" dataIndex="realName"/>
            <Table.Column title="用户名" dataIndex="userName"/>
            <Table.Column title="部门" dataIndex="departmentName"/>
            <Table.Column title="角色" key="roles" />
            <Table.Column title="操作" key="action" width="140" />
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'roles'">
                <template v-for="(r, idx) in (record.roles || [])" :key="idx">
                  <Tag class="mr-1">{{ r }}</Tag>
                </template>
              </template>
              <template v-else-if="column.key === 'action'">
                <Button type="link" @click="openEditRoles(record)">编辑角色</Button>
              </template>
            </template>
          </Table>
          <RoleModal>
            <div class="p-4"><RoleForm/></div>
          </RoleModal>
        </Card>
        <Card v-else :bordered="false">无权限查看</Card>
      </TabPane>
    </Tabs>
  </div>
</template>

<style scoped>
.biz-config-tabs :deep(.ant-tabs-nav) {
  margin-left: 8px;
  margin-bottom: 16px;
  padding-left: 4px;
}
</style>
