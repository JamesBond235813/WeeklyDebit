<script lang="ts" setup>
import type { TreeProps } from 'ant-design-vue';
import type { Dayjs } from 'dayjs';
import type { DispatchPlanMember } from '#/api/biz/dispatch';
import type { DeptInfo } from '#/api/biz/dept';
import type { UserInfoTeamMember } from '#/api/biz/user';

import { computed, onMounted, ref } from 'vue';
import dayjs from 'dayjs';

import { useAccess } from '@vben/access';

import {
  Button,
  Card,
  DatePicker,
  InputNumber,
  message,
  Radio,
  Table,
  Tree,
} from 'ant-design-vue';

import { dispatchApi } from '#/api/biz/dispatch';
import { deptApi } from '#/api/biz/dept';
import { userApi } from '#/api/biz/user';

const access = useAccess();
const canEdit = access.hasAccessByRoles(['ROLE_SUPPER']);

const MODE_MANUAL = 'MANUAL';
const MODE_AUTO = 'AUTO';
const DATE_TIME_FMT = 'YYYY-MM-DD HH:mm:ss';
const DEFAULT_LIMIT = 50;

type TreeNode = {
  key: string;
  title: string;
  children?: TreeNode[];
  isUser?: boolean;
  userId?: number;
  deptId?: number;
  disableCheckbox?: boolean;
};

const mode = ref(MODE_MANUAL);
const timeRange = ref<[Dayjs, Dayjs] | null>(null);
const treeData = ref<TreeNode[]>([]);
const checkedKeys = ref<string[]>([]);
const memberMap = ref<Map<number, DispatchPlanMember>>(new Map());
const loading = ref(false);

const userLookup = ref<Map<number, UserInfoTeamMember>>(new Map());
const deptNameMap = ref<Map<number, string>>(new Map());

const memberList = computed(() => {
  return Array.from(memberMap.value.values()).sort((a, b) => {
    if (a.deptId !== b.deptId) {
      return (a.deptId ?? 0) - (b.deptId ?? 0);
    }
    return (a.userId ?? 0) - (b.userId ?? 0);
  });
});

const columns = [
  { title: '部门', dataIndex: 'deptName', key: 'deptName', width: 160 },
  { title: '人员', dataIndex: 'userName', key: 'userName', width: 140 },
  { title: '每日上限', dataIndex: 'dailyLimit', key: 'dailyLimit', width: 140 },
];

function buildTree(depts: DeptInfo[], users: UserInfoTeamMember[]) {
  const nodeMap = new Map<number, TreeNode>();
  const roots: TreeNode[] = [];
  const nameMap = new Map<number, string>();
  depts.forEach((dept) => {
    nameMap.set(dept.id, dept.deptName);
    nodeMap.set(dept.id, {
      key: `dept-${dept.id}`,
      title: dept.deptName,
      deptId: dept.id,
      disableCheckbox: true,
      children: [],
    });
  });
  depts.forEach((dept) => {
    const node = nodeMap.get(dept.id);
    if (!node) return;
    if (dept.parentDeptId && nodeMap.has(dept.parentDeptId)) {
      nodeMap.get(dept.parentDeptId)?.children?.push(node);
    } else {
      roots.push(node);
    }
  });

  users.forEach((user) => {
    const deptNode = nodeMap.get(user.deptId);
    const title = user.onlineStatus === 1 ? `${user.name}（在线）` : user.name;
    const userNode: TreeNode = {
      key: `user-${user.id}`,
      title,
      isUser: true,
      userId: user.id,
      deptId: user.deptId,
    };
    if (deptNode) {
      deptNode.children = deptNode.children || [];
      deptNode.children.push(userNode);
    } else {
      roots.push({
        key: `dept-${user.deptId}`,
        title: nameMap.get(user.deptId) || `部门${user.deptId}`,
        deptId: user.deptId,
        disableCheckbox: true,
        children: [userNode],
      });
    }
  });
  treeData.value = roots;
  deptNameMap.value = nameMap;
}

function syncCheckedKeys() {
  checkedKeys.value = Array.from(memberMap.value.keys()).map(
    (id) => `user-${id}`,
  );
}

function setMembersFromChecked(keys: string[]) {
  const nextMap = new Map(memberMap.value);
  const selectedIds = new Set<number>();
  keys.forEach((key) => {
    if (!key.startsWith('user-')) return;
    const userId = Number(key.replace('user-', ''));
    if (!Number.isNaN(userId)) {
      selectedIds.add(userId);
    }
  });
  selectedIds.forEach((userId) => {
    if (!nextMap.has(userId)) {
      const user = userLookup.value.get(userId);
      const deptId = user?.deptId ?? 0;
      nextMap.set(userId, {
        userId,
        deptId,
        userName: user?.name ?? '',
        deptName: deptNameMap.value.get(deptId) ?? '',
        dailyLimit: DEFAULT_LIMIT,
      });
    }
  });
  Array.from(nextMap.keys()).forEach((userId) => {
    if (!selectedIds.has(userId)) {
      nextMap.delete(userId);
    }
  });
  memberMap.value = nextMap;
}

const handleCheck: TreeProps['onCheck'] = (checked) => {
  const keys = Array.isArray(checked) ? checked : checked.checked;
  checkedKeys.value = (keys as string[]).filter((key) => key.startsWith('user-'));
  setMembersFromChecked(checkedKeys.value);
};

async function loadMeta() {
  const [deptRes, userRes] = await Promise.all([
    deptApi.listDeptInfo({ status: 1, needExtendQry: false }),
    userApi.listTeamUser(0),
  ]);
  const userMap = new Map<number, UserInfoTeamMember>();
  (userRes || []).forEach((item) => {
    userMap.set(item.id, item);
  });
  userLookup.value = userMap;
  buildTree(deptRes || [], userRes || []);
}

async function loadPlan() {
  loading.value = true;
  try {
    const plan = await dispatchApi.getPlan();
    mode.value = plan?.mode || MODE_MANUAL;
    if (plan?.effectStart && plan?.effectEnd) {
      timeRange.value = [
        dayjs(plan.effectStart, DATE_TIME_FMT),
        dayjs(plan.effectEnd, DATE_TIME_FMT),
      ];
    } else {
      timeRange.value = null;
    }
    const nextMap = new Map<number, DispatchPlanMember>();
    (plan?.members || []).forEach((member) => {
      if (!member?.userId) return;
      nextMap.set(member.userId, {
        ...member,
        dailyLimit: member.dailyLimit ?? DEFAULT_LIMIT,
      });
    });
    memberMap.value = nextMap;
    syncCheckedKeys();
  } finally {
    loading.value = false;
  }
}

async function onSave() {
  if (!timeRange.value || timeRange.value.length !== 2) {
    message.error('请设置有效时间区间');
    return;
  }
  if (mode.value === MODE_AUTO && memberMap.value.size === 0) {
    message.error('自动分配需要选择人员');
    return;
  }
  const members = memberList.value.map((item, index) => ({
    userId: item.userId,
    deptId: item.deptId,
    dailyLimit: item.dailyLimit ?? 0,
    sortNo: index,
  }));
  await dispatchApi.savePlan({
    mode: mode.value,
    effectStart: timeRange.value[0].format(DATE_TIME_FMT),
    effectEnd: timeRange.value[1].format(DATE_TIME_FMT),
    members,
  });
  message.success('已保存分配方案');
  await loadPlan();
}

async function onReset() {
  await loadPlan();
}

onMounted(async () => {
  if (!canEdit) {
    return;
  }
  await loadMeta();
  await loadPlan();
});
</script>

<template>
  <div class="p-4">
    <Card v-if="canEdit" :bordered="false" :loading="loading">
      <div class="mb-4 flex flex-wrap items-center gap-4">
        <div class="flex items-center gap-2">
          <span class="text-sm text-gray-600">推送方式</span>
          <Radio.Group v-model:value="mode">
            <Radio value="MANUAL">人工分配</Radio>
            <Radio value="AUTO">自动分配</Radio>
          </Radio.Group>
        </div>
        <div class="flex items-center gap-2">
          <span class="text-sm text-gray-600">有效时间</span>
          <DatePicker.RangePicker
            v-model:value="timeRange"
            show-time
            format="YYYY-MM-DD HH:mm:ss"
            class="min-w-[320px]"
          />
        </div>
        <div class="ml-auto flex items-center gap-2">
          <Button @click="onReset">重置</Button>
          <Button type="primary" @click="onSave">保存</Button>
        </div>
      </div>

      <div class="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <Card title="人员名单" size="small">
          <Tree
            checkable
            block-node
            :check-strictly="true"
            :checked-keys="checkedKeys"
            :tree-data="treeData"
            :disabled="mode === MODE_MANUAL"
            @check="handleCheck"
          />
        </Card>
        <Card title="人员上限配置" size="small">
          <Table
            :columns="columns"
            :data-source="memberList"
            row-key="userId"
            size="small"
            :pagination="false"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'dailyLimit'">
                <InputNumber
                  v-model:value="record.dailyLimit"
                  :min="0"
                  :disabled="mode === MODE_MANUAL"
                />
              </template>
            </template>
          </Table>
        </Card>
      </div>
    </Card>
    <Card v-else :bordered="false">无权限查看</Card>
  </div>
</template>
