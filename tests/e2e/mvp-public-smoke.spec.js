const { test, expect } = require('@playwright/test');

test('MVP public smoke: login visible and protected pages redirect', async ({ page }) => {
  const consoleErrors = [];
  const pageErrors = [];
  const failedRequests = [];

  page.on('console', (msg) => {
    if (msg.type() === 'error') {
      consoleErrors.push(msg.text());
    }
  });
  page.on('pageerror', (error) => pageErrors.push(error.message));
  page.on('requestfailed', (request) => {
    failedRequests.push(`${request.method()} ${request.url()} ${request.failure()?.errorText || ''}`);
  });

  await page.goto('/login');
  await expect(page.locator('body')).toBeVisible();
  await expect(page.locator('body')).not.toHaveText('');

  const passwordInput = page.locator('input[type="password"]').first();
  await expect(passwordInput).toBeVisible();

  await page.goto('/register');
  await expect(page.locator('body')).toBeVisible();
  await expect(page.locator('input[type="password"]').first()).toBeVisible();

  await page.goto('/games');
  await expect(page).toHaveURL(/.*login.*/);

  await page.goto('/stats');
  await expect(page).toHaveURL(/.*login.*/);

  expect(consoleErrors).toEqual([]);
  expect(pageErrors).toEqual([]);
  expect(failedRequests).toEqual([]);
});
