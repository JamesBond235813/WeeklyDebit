import { expect, test } from '@playwright/test';

test.describe('customer notice read flow', () => {
  async function gotoPath(page: any, path: string) {
    const useHash = await page.evaluate(() => location.hash.startsWith('#/'));
    const target = useHash ? `/#${path}` : path;
    await page.goto(target);
  }

  async function login(page: any) {
    await gotoPath(page, '/auth/login');
    const usernameInput = page.getByPlaceholder('请输入用户名');
    const passwordInput = page.getByPlaceholder('密码');
    await expect(usernameInput).toBeVisible();
    await expect(passwordInput).toBeVisible();
    await usernameInput.fill('qr8');
    await passwordInput.fill('abc123456');
    await page.getByRole('button', { name: /登录|login/i }).click();
    await page.waitForURL((url: URL) => !url.pathname.startsWith('/auth/login'));
    await page.waitForLoadState('networkidle');
  }

  test('marks notice read when clicking handle action', async ({ page }) => {
    await login(page);
    await gotoPath(page, '/biz-work/customer');
    await page.waitForLoadState('networkidle');

    const noticeCount = page.locator('.notice-count');
    const hasCount = await noticeCount.isVisible();

    if (!hasCount) {
      await expect(page.getByText('您有新数据，请及时处理')).toHaveCount(0);
      return;
    }

    const beforeCountText = (await noticeCount.textContent())?.trim() ?? '0';
    const beforeCount = Number(beforeCountText);

    await page.getByRole('button', { name: '查看全部未读' }).click();
    const modal = page.getByRole('dialog', { name: '全部未读' });
    await expect(modal).toBeVisible();

    const noticeItem = modal.locator('.notice-item').first();
    await expect(noticeItem).toBeVisible();
    const noticeSnapshot = (await noticeItem.textContent())?.trim() ?? '';

    await noticeItem.getByRole('button', { name: '立即处理' }).click();
    await expect(modal).toBeHidden();

    if (beforeCount > 1) {
      await expect(noticeCount).toBeVisible();
      const afterCountText = (await noticeCount.textContent())?.trim() ?? '0';
      expect(Number(afterCountText)).toBe(beforeCount - 1);
    } else {
      await expect(noticeCount).toHaveCount(0);
    }

    await page.getByRole('button', { name: '查看全部未读' }).click();
    await expect(modal).toBeVisible();
    await expect(modal).not.toContainText(noticeSnapshot);
    await page.reload();
    await page.waitForLoadState('networkidle');

    if (beforeCount > 1) {
      await expect(noticeCount).toBeVisible();
    } else {
      await expect(noticeCount).toHaveCount(0);
    }
  });
});
