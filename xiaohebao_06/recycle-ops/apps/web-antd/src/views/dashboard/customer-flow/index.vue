<script lang="ts" setup>
import { computed, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';

import { Button, Card, Empty, Form, Input, message, Tag, Timeline } from 'ant-design-vue';

import {
  queryCustomerFlow,
  type CustomerFlowQueryResult,
} from '#/api/biz/risk';

defineOptions({ name: 'CustomerFlowQuery' });

const formState = reactive({
  name: '',
  mobile: '',
});

const loading = ref(false);
const result = ref<CustomerFlowQueryResult | null>(null);

const hasResult = computed(() => result.value?.found);

const normalizeResponse = (res: any) => res?.data ?? res;

const queryFlow = async () => {
  const name = formState.name.trim();
  const mobile = formState.mobile.trim();
  if (!name && !mobile) {
    message.warning('请填写客户姓名或手机号');
    return;
  }
  loading.value = true;
  try {
    result.value = normalizeResponse(
      await queryCustomerFlow({
        name,
        mobile,
      }),
    );
  } catch (error: any) {
    message.error(error?.message || '查询客户流转失败');
  } finally {
    loading.value = false;
  }
};

const resetForm = () => {
  formState.name = '';
  formState.mobile = '';
  result.value = null;
};

const nodeColor = (type?: string) => {
  if (type === 'INBOUND') return 'blue';
  if (type === 'PUBLIC_POOL' || type === 'BACK_PUBLIC_POOL') return 'orange';
  if (type === 'CLAIM' || type === 'ASSIGN') return 'green';
  return 'gray';
};
</script>

<template>
  <Page title="客户流转查询">
    <div class="customer-flow-page">
      <Card title="查询条件" :bordered="false">
        <Form layout="inline" class="flow-form">
          <Form.Item label="客户姓名">
            <Input
              v-model:value="formState.name"
              allow-clear
              placeholder="请输入客户姓名"
              @press-enter="queryFlow"
            />
          </Form.Item>
          <Form.Item label="手机号">
            <Input
              v-model:value="formState.mobile"
              allow-clear
              placeholder="请输入手机号"
              @press-enter="queryFlow"
            />
          </Form.Item>
          <Form.Item>
            <Button type="primary" :loading="loading" @click="queryFlow">
              查询
            </Button>
          </Form.Item>
          <Form.Item>
            <Button @click="resetForm">清空</Button>
          </Form.Item>
        </Form>
      </Card>

      <Card title="流转结果" :bordered="false">
        <template v-if="hasResult">
          <div class="flow-summary">
            <div class="summary-main">{{ result?.summary }}</div>
            <div class="summary-tags">
              <Tag color="blue">客户ID：{{ result?.customerId || '--' }}</Tag>
              <Tag color="cyan">当前状态：{{ result?.currentStatus || '--' }}</Tag>
              <Tag>当前部门：{{ result?.currentDept || '--' }}</Tag>
              <Tag>当前归属：{{ result?.currentOwner || '--' }}</Tag>
            </div>
          </div>

          <div class="flow-diagram">
            <div
              v-for="(node, index) in result?.nodes || []"
              :key="`${node.time}-${node.title}-${index}`"
              class="flow-step"
            >
              <div class="step-index">{{ index + 1 }}</div>
              <div class="step-content">
                <div class="step-title">{{ node.title }}</div>
                <div class="step-time">{{ node.time || '--' }}</div>
              </div>
            </div>
          </div>

          <Timeline class="flow-timeline">
            <Timeline.Item
              v-for="(node, index) in result?.nodes || []"
              :key="`${node.type}-${index}`"
              :color="nodeColor(node.type)"
            >
              <div class="timeline-node">
                <div class="timeline-head">
                  <strong>{{ node.title }}</strong>
                  <span>{{ node.time || '--' }}</span>
                </div>
                <p>{{ node.description }}</p>
                <small>操作人：{{ node.operator || '系统' }}</small>
              </div>
            </Timeline.Item>
          </Timeline>
        </template>
        <Empty v-else :description="result?.summary || '请输入客户信息后查询'" />
      </Card>
    </div>
  </Page>
</template>

<style scoped>
.customer-flow-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.flow-form {
  row-gap: 12px;
}

.flow-summary {
  padding: 16px;
  margin-bottom: 18px;
  background: hsl(var(--accent));
  border-radius: 8px;
}

.summary-main {
  line-height: 1.8;
  color: hsl(var(--foreground));
}

.summary-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.flow-diagram {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 12px;
  margin-bottom: 24px;
}

.flow-step {
  display: flex;
  align-items: center;
  min-height: 72px;
  padding: 12px;
  border: 1px solid hsl(var(--border));
  border-radius: 8px;
  background: hsl(var(--card));
}

.step-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  margin-right: 10px;
  font-weight: 600;
  color: #1677ff;
  border: 1px solid #91caff;
  border-radius: 50%;
  background: #e6f4ff;
  flex: 0 0 auto;
}

.step-title {
  font-weight: 600;
  color: hsl(var(--foreground));
}

.step-time {
  margin-top: 4px;
  font-size: 12px;
  color: hsl(var(--muted-foreground));
}

.flow-timeline {
  padding: 4px 8px 0;
}

.timeline-node {
  padding-bottom: 8px;
}

.timeline-head {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.timeline-head span,
.timeline-node small {
  color: hsl(var(--muted-foreground));
}

.timeline-node p {
  margin: 6px 0 2px;
  line-height: 1.7;
}
</style>
