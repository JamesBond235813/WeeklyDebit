import { expect, test } from '@playwright/test';

test.describe('customer list call tips filter', () => {
  const paths = [
    '/biz-work/customer',
    '/biz-work/my-customer',
    '/biz-work/key-customer',
    '/biz-work/assign-customer',
  ];

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

  test('shows call tips filter and sends callTips in request', async ({ page }) => {
    await login(page);

    const searchPanel = page.locator('.cust-search-panel');

    for (const path of paths) {
      await gotoPath(page, path);
      await page.waitForLoadState('networkidle');

      const formItem = searchPanel
        .locator('form .grid > div')
        .filter({ hasText: '沟通结果' })
        .first();
      await expect(formItem).toBeVisible();

      const selectTrigger = formItem.locator('.ant-select-selector');
      await selectTrigger.click();

      const dropdownOption = page.locator(
        '.ant-select-dropdown:visible .ant-select-item-option',
      );
      await expect(dropdownOption.first()).toBeVisible();

      const optionText = (await dropdownOption.first().textContent())?.trim() ?? '';
      expect(optionText.length).toBeGreaterThan(0);
      await dropdownOption.first().click();
      await expect(formItem).toContainText(optionText);

      const queryButton = page.locator(
        '.cust-search-panel button.ant-btn-primary',
      );
      await expect(queryButton).toBeVisible();

      const [request] = await Promise.all([
        page.waitForRequest((request) => {
          return (
            request.method() === 'POST' &&
            request.url().includes('/cust/page-list-customers')
          );
        }),
        queryButton.click(),
      ]);
      const payload = request.postDataJSON();
      expect(typeof payload.callTips).toBe('number');
    }
  });
});
