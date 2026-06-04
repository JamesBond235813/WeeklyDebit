import { expect, test } from '@playwright/test';

test.describe('customer detail modal layout', () => {
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
    await gotoPath(page, '/biz-work/customer');
    await page.waitForLoadState('networkidle');
    if (await usernameInput.isVisible()) {
      throw new Error('Login failed or redirected back to login page.');
    }
  }

  async function openCustomerDetail(page: any) {
    const candidatePaths = [
      '/biz-work/customer',
      '/biz-work/my-customer',
      '/biz-work/key-customer',
      '/biz-work/assign-customer',
    ];

    let viewButton = page.locator('.ant-table').getByText('查看', { exact: true }).first();

    for (const path of candidatePaths) {
      await gotoPath(page, path);
      await page.waitForLoadState('networkidle');
      viewButton = page
        .locator('.ant-table')
        .getByText('查看', { exact: true })
        .first();
      try {
        await expect(viewButton).toBeVisible({ timeout: 4000 });
        break;
      } catch (error) {
        viewButton = page
          .locator('.ant-table')
          .getByText('查看', { exact: true })
          .first();
      }
    }

    await expect(viewButton).toBeVisible();
    await viewButton.click();
    const dialog = page.getByRole('dialog');
    await expect(dialog).toContainText('客户信息详情');
    return dialog;
  }

  test('footer actions and column width on desktop', async ({ page }) => {
    await login(page);
    const dialog = await openCustomerDetail(page);

    const leftCol = dialog.locator('.detail-left');
    const rightCol = dialog.locator('.detail-right');
    await expect(leftCol).toBeVisible();
    await expect(rightCol).toBeVisible();

    const leftBox = await leftCol.boundingBox();
    const rightBox = await rightCol.boundingBox();
    expect(leftBox?.width ?? 0).toBeGreaterThan(rightBox?.width ?? 0);

    const resetButton = dialog.getByRole('button', { name: /重\s*置/ });
    const saveButton = dialog.getByRole('button', { name: /保\s*存/ });
    const closeButton = dialog.getByRole('button', { name: /关\s*闭/ });

    await expect(closeButton).toBeVisible();
    await expect(resetButton).toBeVisible();
    await expect(saveButton).toBeVisible();

    const dialogBox = await dialog.boundingBox();
    const resetBox = await resetButton.boundingBox();
    const saveBox = await saveButton.boundingBox();
    const closeBox = await closeButton.boundingBox();

    const footerThreshold = (dialogBox?.y ?? 0) + (dialogBox?.height ?? 0) - 120;
    expect(resetBox?.y ?? 0).toBeGreaterThan(footerThreshold);
    expect(saveBox?.y ?? 0).toBeGreaterThan(footerThreshold);
    expect(closeBox?.y ?? 0).toBeGreaterThan(footerThreshold);

    expect(resetBox?.x ?? 0).toBeLessThan(saveBox?.x ?? 0);
    expect(saveBox?.x ?? 0).toBeLessThan(closeBox?.x ?? 0);
  });

  test('stacks columns on narrow screens', async ({ page }) => {
    await page.setViewportSize({ width: 1000, height: 900 });
    await login(page);
    const dialog = await openCustomerDetail(page);

    await expect(dialog.locator('.detail-left')).toBeVisible();
    await expect(dialog.locator('.detail-right')).toBeVisible();

    const layout = await page.evaluate(() => {
      const row = document.querySelector('.detail-row') as HTMLElement | null;
      const left = document.querySelector('.detail-left') as HTMLElement | null;
      const right = document.querySelector('.detail-right') as HTMLElement | null;
      if (!row || !left || !right) {
        return null;
      }
      const rowStyle = window.getComputedStyle(row);
      const leftRect = left.getBoundingClientRect();
      const rightRect = right.getBoundingClientRect();
      return {
        flexDirection: rowStyle.flexDirection,
        leftRect: {
          top: leftRect.top,
          bottom: leftRect.bottom,
          width: leftRect.width,
        },
        rightRect: {
          top: rightRect.top,
          width: rightRect.width,
        },
      };
    });

    expect(layout?.flexDirection).toBe('column');
    expect(layout?.rightRect.top ?? 0).toBeGreaterThan((layout?.leftRect.bottom ?? 0) - 5);
    const widthDiff = Math.abs((layout?.leftRect.width ?? 0) - (layout?.rightRect.width ?? 0));
    expect(widthDiff).toBeLessThan(16);
  });
});
