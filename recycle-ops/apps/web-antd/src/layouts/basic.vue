<script lang="ts" setup>
import type { NotificationItem } from '@vben/layouts';
import type { CustomerNoticeItem } from '#/api/biz/customer';

import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';

import { AuthenticationLoginExpiredModal, useVbenModal } from '@vben/common-ui';
import { useAppConfig, useWatermark } from '@vben/hooks';
import { BookOpenText } from '@vben/icons';
import {
  BasicLayout,
  LockScreen,
  Notification,
  UserDropdown,
} from '@vben/layouts';
import { preferences } from '@vben/preferences';
import { useAccessStore, useUserStore } from '@vben/stores';

import { Button, message, Modal } from 'ant-design-vue';

import { BizCommonApi } from '#/api/biz/biz-common';
import { customerApi } from '#/api/biz/customer';
import { userApi } from '#/api/biz/user';
import { useAuthStore } from '#/store';
import LoginForm from '#/views/_core/authentication/login.vue';

import PswdModal from './changePswd.vue';

const [PwdModal, pwdModalApi] = useVbenModal({
  // 连接抽离的组件
  connectedComponent: PswdModal,
});
const notifications = ref<NotificationItem[]>([]);
const noticeItems = ref<CustomerNoticeItem[]>([]);
const noticeWs = ref<WebSocket | null>(null);
const noticeWsTimer = ref<number | null>(null);
const showAllUnreadModal = ref(false);
const allUnreadLoading = ref(false);

const userStore = useUserStore();
const authStore = useAuthStore();
const accessStore = useAccessStore();
const router = useRouter();
const { apiURL } = useAppConfig(import.meta.env, import.meta.env.PROD);
const { destroyWatermark, updateWatermark } = useWatermark();
const unreadCount = computed(() => noticeItems.value.length);
const showDot = computed(() => unreadCount.value > 0);
const showGlow = computed(() => unreadCount.value > 0);
const noticeReadEventName = 'cust-notice-read';

const menus = computed(() => [
  {
    handler: () => {
      pwdModalApi.open();
    },
    icon: BookOpenText,
    text: '修改密码',
  },
]);

const avatar = computed(() => {
  return userStore.userInfo?.avatar ?? preferences.app.defaultAvatar;
});

async function handleLogout() {
  await authStore.logout(false);
}

async function handleNoticeClear() {
  await markAllRead();
}

async function handleMakeAll() {
  await markAllRead();
}

async function loadCustomerNotices() {
  try {
    const list = await customerApi.getCustomerNoticeUnread(20);
    noticeItems.value = Array.isArray(list) ? list : [];
    syncNotifications();
  } catch (error) {
    // ignore notice errors
  }
}

function syncNotifications() {
  notifications.value = noticeItems.value.map((item) => ({
    avatar: 'https://avatar.vercel.sh/vercel.svg?text=NEW',
    title: item.noticeType === 'UPDATE' ? '客户信息更新' : '新增客户入库',
    message: `${item.custName || ''} ${maskMobile(item.custMobile)} ${maskIdCard(
      item.custIdCard,
    )}`.trim(),
    date: '刚刚',
    isRead: false,
  }));
}

function maskMobile(value?: string) {
  if (!value) return '';
  const digits = value.replace(/\s+/g, '');
  if (digits.length < 7) return digits;
  return `${digits.slice(0, 3)}****${digits.slice(-4)}`;
}

function maskIdCard(value?: string) {
  if (!value) return '';
  const trimmed = value.replace(/\s+/g, '');
  if (trimmed.length <= 8) return trimmed;
  return `${trimmed.slice(0, 4)}********${trimmed.slice(-4)}`;
}

function buildWsUrl() {
  if (!canStartNoticeWs()) {
    return null;
  }
  const token = accessStore.accessToken;
  if (!token) {
    return null;
  }
  const base = apiURL.replace(/^http/, 'ws').replace(/\/$/, '');
  return `${base}/ws/notice?token=${token}`;
}

function parseJwtPayload(token: string) {
  try {
    const parts = token.split('.');
    if (parts.length < 2 || !parts[1]) {
      return null;
    }
    const payload = parts[1].replace(/-/g, '+').replace(/_/g, '/');
    const normalized = payload.padEnd(Math.ceil(payload.length / 4) * 4, '=');
    const decoded = atob(normalized);
    return JSON.parse(decoded) as { exp?: number };
  } catch {
    return null;
  }
}

function isAccessTokenUsable(token: string) {
  const payload = parseJwtPayload(token);
  if (!payload || typeof payload.exp !== 'number') {
    return false;
  }
  const nowInSeconds = Math.floor(Date.now() / 1000);
  return payload.exp > nowInSeconds;
}

function canStartNoticeWs() {
  const token = accessStore.accessToken;
  return Boolean(token && userStore.userInfo?.userId && isAccessTokenUsable(token));
}

function startNoticeWs() {
  const wsUrl = buildWsUrl();
  if (!wsUrl) {
    return;
  }
  if (noticeWs.value) {
    noticeWs.value.close();
    noticeWs.value = null;
  }
  const ws = new WebSocket(wsUrl);
  noticeWs.value = ws;
  ws.onopen = () => {
    if (noticeWsTimer.value) {
      window.clearTimeout(noticeWsTimer.value);
      noticeWsTimer.value = null;
    }
    loadCustomerNotices();
  };
  ws.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data || '{}');
      if (data?.type === 'NOTICE' && data.notice) {
        addNotice(data.notice as CustomerNoticeItem);
      }
    } catch (e) {
      // ignore parsing errors
    }
  };
  ws.onclose = () => {
    if (noticeWsTimer.value) {
      window.clearTimeout(noticeWsTimer.value);
    }
    if (!canStartNoticeWs()) {
      noticeWsTimer.value = null;
      return;
    }
    noticeWsTimer.value = window.setTimeout(() => {
      startNoticeWs();
    }, 3000);
  };
}

function addNotice(item: CustomerNoticeItem) {
  if (!item || !item.id) {
    return;
  }
  if (noticeItems.value.some((n) => n.id === item.id)) {
    return;
  }
  noticeItems.value.unshift(item);
  syncNotifications();
}

async function markAllRead() {
  const ids = noticeItems.value.map((item) => item.id).filter(Boolean);
  if (ids.length === 0) {
    return;
  }
  try {
    await customerApi.markCustomerNoticeRead(ids);
  } catch (e) {
    // ignore
  }
  noticeItems.value = [];
  syncNotifications();
}

async function handleNoticeAction(item: CustomerNoticeItem) {
  if (!item) {
    return;
  }
  showAllUnreadModal.value = false;
  await markNoticeItemsRead([item]);
  const path = resolveNoticePath(item);
  router.push({
    path,
    query: {
      openCid: String(item.custId),
    },
  });
}

async function handleNoticeReadEvent(event: Event) {
  const detail = (event as CustomEvent)?.detail || {};
  const noticeId = detail?.noticeId ? Number(detail.noticeId) : 0;
  const custId = detail?.custId ? Number(detail.custId) : 0;
  if (!noticeId && !custId) {
    return;
  }
  const matched = noticeId
    ? noticeItems.value.filter((item) => item.id === noticeId)
    : noticeItems.value.filter((item) => item.custId === custId);
  if (matched.length === 0) {
    return;
  }
  await markNoticeItemsRead(matched);
}

async function handleViewAllUnread() {
  showAllUnreadModal.value = true;
  allUnreadLoading.value = true;
  try {
    const list = await customerApi.getCustomerNoticeUnread(100);
    noticeItems.value = Array.isArray(list) ? list : [];
    syncNotifications();
  } catch (e) {
    // ignore
  } finally {
    allUnreadLoading.value = false;
  }
}

function resolveNoticePath(item: CustomerNoticeItem) {
  if (item.ownerUserId === 0) {
    return '/biz-work/public-pool';
  }
  return '/biz-work/my-customer';
}

async function markNoticeItemsRead(items: CustomerNoticeItem[]) {
  if (!items.length) {
    return;
  }
  const ids = items.map((item) => item.id).filter(Boolean);
  if (ids.length === 0) {
    return;
  }
  const idSet = new Set(ids);
  noticeItems.value = noticeItems.value.filter((item) => !idSet.has(item.id));
  syncNotifications();
  try {
    await customerApi.markCustomerNoticeRead(ids);
  } catch (e) {
    // ignore
  }
}

watch(
  () => preferences.app.watermark,
  async (enable) => {
    if (enable) {
      await updateWatermark({
        content: `${userStore.userInfo?.username}`,
      });
    } else {
      destroyWatermark();
    }
  },
  {
    immediate: true,
  },
);

watch(
  () => userStore.userInfo?.userId,
  (userId) => {
    if (userId) {
      loadCustomerNotices();
      startNoticeWs();
    }
  },
  { immediate: true },
);


watch(
  () => accessStore.accessToken,
  (token) => {
    if (token && canStartNoticeWs()) {
      startNoticeWs();
    } else {
      if (noticeWsTimer.value) {
        window.clearTimeout(noticeWsTimer.value);
        noticeWsTimer.value = null;
      }
      if (!noticeWs.value) {
        return;
      }
      noticeWs.value.close();
      noticeWs.value = null;
    }
  },
);

onMounted(() => {
  window.addEventListener(noticeReadEventName, handleNoticeReadEvent);
});

onBeforeUnmount(() => {
  window.removeEventListener(noticeReadEventName, handleNoticeReadEvent);
});

function updateOnlineStatusChange(checked: boolean) {
  if (checked === (userStore.userInfo.onlineStatus === 1)) {
    return;
  }
  const onlineStatus = checked ? 1 : 0;
  // 更新用户在线状态
  userApi
    .markOnlineStatus(onlineStatus)
    .then(() => {
      authStore
        .fetchUserInfo()
        .then(() => {
          message.success({
            content: checked ? '上线成功' : '下线成功',
          });
        })
        .catch((error: any) => {
          BizCommonApi.showErrorMsg(error, '请稍后刷新，再试尝试操作。');
        });
    })
    .catch((error: any) => {
      BizCommonApi.showErrorMsg(error, '请稍后刷新，再试尝试操作。');
    });
  // console.log(`${checked}=============>${userStore.userInfo.onlineStatus}`);
}
</script>

<template>
  <BasicLayout
    @clear-preferences-and-logout="handleLogout"
    @handle-online-status-change="updateOnlineStatusChange"
  >
    <template #header-right-49>
      <div class="notice-glow-wrapper">
        <div :class="showGlow ? 'notice-glow' : 'notice-idle'">
          {{ showGlow ? '新' : '等待' }}
        </div>
        <span v-if="showGlow" class="notice-count">{{ unreadCount }}</span>
        <div v-if="showGlow" class="notice-popover">
          <div class="notice-popover-title">
            <span>您有新数据，请及时处理</span>
            <Button type="link" size="small" @click="handleViewAllUnread">
              查看全部未读
            </Button>
          </div>
          <div class="notice-popover-list">
            <div v-for="item in noticeItems" :key="item.id" class="notice-item">
              <div class="notice-line">姓名：{{ item.custName || '-' }}</div>
              <div class="notice-line">
                手机号：{{ maskMobile(item.custMobile) || '-' }}
              </div>
              <div class="notice-line">
                身份证号：{{ maskIdCard(item.custIdCard) || '-' }}
              </div>
              <div class="notice-actions">
                <Button size="small" type="primary" @click="handleNoticeAction(item)">
                  立即处理
                </Button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
    <template #user-dropdown>
      <UserDropdown
        :avatar
        :menus
        :text="userStore.userInfo?.realName"
        description=""
        tag-text=""
        @logout="handleLogout"
      />
    </template>
    <template #notification>
      <Notification
        :dot="showDot"
        :notifications="notifications"
        @clear="handleNoticeClear"
        @make-all="handleMakeAll"
      />
    </template>
    <template #extra>
      <AuthenticationLoginExpiredModal
        v-model:open="accessStore.loginExpired"
        :avatar
      >
        <LoginForm />
      </AuthenticationLoginExpiredModal>
    </template>
    <template #lock-screen>
      <LockScreen :avatar @to-login="handleLogout" />
    </template>
  </BasicLayout>
  <Modal
    v-model:open="showAllUnreadModal"
    title="全部未读"
    :footer="null"
    :width="420"
  >
    <div class="notice-modal-body">
      <div v-if="allUnreadLoading" class="notice-loading">加载中...</div>
      <template v-else>
        <div v-if="noticeItems.length === 0" class="notice-empty">
          暂无未读
        </div>
        <div v-else class="notice-modal-list">
          <div v-for="item in noticeItems" :key="item.id" class="notice-item">
            <div class="notice-line">姓名：{{ item.custName || '-' }}</div>
            <div class="notice-line">
              手机号：{{ maskMobile(item.custMobile) || '-' }}
            </div>
            <div class="notice-line">
              身份证号：{{ maskIdCard(item.custIdCard) || '-' }}
            </div>
            <div class="notice-actions">
              <Button size="small" type="primary" @click="handleNoticeAction(item)">
                立即处理
              </Button>
            </div>
          </div>
        </div>
      </template>
    </div>
  </Modal>
  <PwdModal />
</template>
<style scoped>
.notice-glow-wrapper {
  position: relative;
  margin-right: 8px;
  display: flex;
  align-items: center;
}

.notice-glow {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #ff8a3d;
  color: #fff;
  font-weight: 700;
  font-size: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  animation: noticeGlow 1.6s ease-in-out infinite;
  box-shadow: 0 0 8px rgba(255, 138, 61, 0.7);
}

.notice-idle {
  width: 36px;
  height: 28px;
  border-radius: 999px;
  background: #e5e7eb;
  color: #6b7280;
  font-weight: 600;
  font-size: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.notice-count {
  margin-left: 6px;
  font-size: 12px;
  color: #ff8a3d;
  font-weight: 600;
}

.notice-popover {
  position: absolute;
  top: 38px;
  left: -80px;
  z-index: 20;
  width: 260px;
  background: #fff;
  border: 1px solid #f2f2f2;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.12);
  padding: 10px 12px;
}

.notice-popover-title {
  font-size: 12px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.notice-popover-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 220px;
  overflow: auto;
}

.notice-item {
  background: #fff7ed;
  border: 1px solid #fed7aa;
  border-radius: 6px;
  padding: 8px;
}

.notice-line {
  font-size: 12px;
  color: #4b5563;
  line-height: 1.4;
}

.notice-actions {
  margin-top: 6px;
  display: flex;
  justify-content: flex-end;
}

.notice-modal-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 460px;
  overflow: auto;
}

.notice-modal-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.notice-empty,
.notice-loading {
  font-size: 12px;
  color: #6b7280;
  text-align: center;
  padding: 20px 0;
}

@keyframes noticeGlow {
  0% {
    box-shadow: 0 0 6px rgba(255, 138, 61, 0.35);
  }
  50% {
    box-shadow: 0 0 14px rgba(255, 138, 61, 0.9);
  }
  100% {
    box-shadow: 0 0 6px rgba(255, 138, 61, 0.35);
  }
}
</style>
