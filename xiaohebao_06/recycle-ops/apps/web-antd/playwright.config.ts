import { defineConfig } from '@playwright/test';

const baseURL =
  process.env.PLAYWRIGHT_BASE_URL ?? 'http://192.168.43.62:5666';

export default defineConfig({
  testDir: './e2e',
  timeout: 60_000,
  expect: {
    timeout: 10_000,
  },
  use: {
    baseURL,
    headless: true,
    viewport: { width: 1400, height: 900 },
  },
});
