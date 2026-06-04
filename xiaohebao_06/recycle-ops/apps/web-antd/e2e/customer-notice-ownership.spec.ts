import { expect, test } from '@playwright/test';

type SuResult<T> = {
  code?: number;
  data?: T;
  msg?: string;
};

type NoticeItem = {
  id: number;
  custId: number;
};

type CustomerDetail = {
  ownerUserId?: number;
};

type UserInfo = {
  userId?: number;
};

test.describe.serial('customer notice ownership flow', () => {
  async function gotoPath(page: any, path: string) {
    const useHash = await page.evaluate(() => location.hash.startsWith('#/'));
    const target = useHash ? `/#${path}` : path;
    await page.goto(target);
  }

  async function login(page: any, username: string, password: string) {
    await gotoPath(page, '/auth/login');
    const usernameInput = page.getByPlaceholder('请输入用户名');
    const passwordInput = page.getByPlaceholder('密码');
    await expect(usernameInput).toBeVisible();
    await expect(passwordInput).toBeVisible();
    await usernameInput.fill(username);
    await passwordInput.fill(password);
    await page.getByRole('button', { name: /登录|login/i }).click();
    await page.waitForURL((url: URL) => !url.pathname.startsWith('/auth/login'));
    await page.waitForLoadState('networkidle');
  }

  async function unwrapResult<T>(response: any): Promise<T> {
    const payload = (await response.json()) as SuResult<T>;
    if (payload && typeof payload === 'object' && 'data' in payload) {
      return payload.data as T;
    }
    return payload as T;
  }

  test('filters unread list and blocks handle action for reassigned customers', async ({
    page,
    browser,
  }) => {
    await login(page, 'qr8', 'abc123456');
    await gotoPath(page, '/biz-work/customer');
    await page.waitForLoadState('networkidle');

    const noticeCount = page.locator('.notice-count');
    if (!(await noticeCount.isVisible())) {
      test.skip(true, 'no unread notice count visible');
    }

    const beforeCountText = (await noticeCount.textContent())?.trim() ?? '0';
    const beforeCount = Number(beforeCountText);

    const [noticeResponse] = await Promise.all([
      page.waitForResponse((response) => {
        return (
          response.request().method() === 'GET' &&
          response.url().includes('/cust/notice/unread')
        );
      }),
      page.getByRole('button', { name: '查看全部未读' }).click(),
    ]);
    const modal = page.getByRole('dialog', { name: '全部未读' });
    await expect(modal).toBeVisible();

    const noticeList = (await unwrapResult<NoticeItem[]>(noticeResponse)) ?? [];
    if (!Array.isArray(noticeList) || noticeList.length === 0) {
      await expect(modal).toContainText('暂无未读');
      return;
    }

    const userInfoResponse = await page.request.get('/user/info');
    const userInfo = (await unwrapResult<UserInfo>(userInfoResponse)) ?? {};
    const currentUserId = Number(userInfo.userId ?? 0);
    expect(currentUserId).toBeGreaterThan(0);

    let targetIndex = -1;
    for (let i = 0; i < noticeList.length; i += 1) {
      const notice = noticeList[i];
      const detailResponse = await page.request.get(
        `/cust/detail?cid=${notice.custId}`,
      );
      const detail = (await unwrapResult<CustomerDetail>(detailResponse)) ?? {};
      const ownerUserId = Number(detail.ownerUserId ?? 0);
      expect(ownerUserId === 0 || ownerUserId === currentUserId).toBeTruthy();
      if (ownerUserId === 0 && targetIndex === -1) {
        targetIndex = i;
      }
    }

    if (targetIndex < 0) {
      test.skip(true, 'no public pool notice available to claim');
    }

    const targetNotice = noticeList[targetIndex];
    const noticeItems = modal.locator('.notice-item');
    const noticeItem = noticeItems.nth(targetIndex);
    await expect(noticeItem).toBeVisible();

    const baseURL = new URL(page.url()).origin;
    const otherContext = await browser.newContext({ baseURL });
    const otherPage = await otherContext.newPage();
    await login(otherPage, 'xiaowang', 'abc123456');

    const claimResponse = await otherPage.request.get(
      `/cust/claim?cid=${targetNotice.custId}`,
    );
    const claimPayload = (await claimResponse.json()) as SuResult<unknown>;
    expect(claimPayload?.code).toBe(0);
    await otherContext.close();

    const urlBeforeClick = page.url();
    await noticeItem.getByRole('button', { name: '立即处理' }).click();
    await expect(page.getByText('该客户已分配给其他用户')).toBeVisible();
    await expect(page).not.toHaveURL(
      new RegExp(`openCid=${targetNotice.custId}`),
    );
    await expect(page).toHaveURL(urlBeforeClick);

    const [refreshResponse] = await Promise.all([
      page.waitForResponse((response) => {
        return (
          response.request().method() === 'GET' &&
          response.url().includes('/cust/notice/unread')
        );
      }),
      page.getByRole('button', { name: '查看全部未读' }).click(),
    ]);
    await expect(modal).toBeVisible();
    const refreshedList = (await unwrapResult<NoticeItem[]>(refreshResponse)) ?? [];
    expect(refreshedList.some((item) => item.id === targetNotice.id)).toBeFalsy();

    if (refreshedList.length === 0) {
      await expect(noticeCount).toHaveCount(0);
    } else {
      const afterCountText = (await noticeCount.textContent())?.trim() ?? '0';
      const afterCount = Number(afterCountText);
      expect(afterCount).toBe(refreshedList.length);
      expect(afterCount).toBeLessThanOrEqual(beforeCount);
    }
  });
});
