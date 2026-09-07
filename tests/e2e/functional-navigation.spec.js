const fs = require('fs');
const { test, expect } = require('@playwright/test');

test.setTimeout(60000);

function collectDiagnostics(page) {
  const diagnostics = {
    consoleErrors: [],
    pageErrors: [],
    requestFailures: [],
    notFoundResponses: [],
    serverErrors: []
  };

  page.on('console', (msg) => {
    if (msg.type() === 'error') {
      diagnostics.consoleErrors.push(msg.text());
    }
  });

  page.on('pageerror', (error) => {
    diagnostics.pageErrors.push(error.message);
  });

  page.on('requestfailed', (request) => {
    diagnostics.requestFailures.push(`${request.method()} ${request.url()} ${request.failure()?.errorText || ''}`);
  });

  page.on('response', (response) => {
    const url = response.url();
    if (response.status() === 404 && !url.endsWith('/favicon.ico')) {
      diagnostics.notFoundResponses.push(`404 ${url}`);
    }
    if (response.status() >= 500) {
      diagnostics.serverErrors.push(`${response.status()} ${url}`);
    }
  });

  return diagnostics;
}

async function expectHealthyPage(page, diagnostics, label) {
  await expect(page.locator('body'), `${label}: body visible`).toBeVisible();
  const bodyText = (await page.locator('body').innerText()).trim();
  expect(bodyText.length, `${label}: body not blank`).toBeGreaterThan(0);
  expect(diagnostics.pageErrors, `${label}: no page errors`).toEqual([]);
  expect(diagnostics.notFoundResponses, `${label}: no HTTP 404`).toEqual([]);
  expect(diagnostics.serverErrors, `${label}: no HTTP 5xx`).toEqual([]);
  expect(diagnostics.requestFailures, `${label}: no failed requests`).toEqual([]);
}

async function clickFirstVisible(page, report, label, locatorFactory) {
  const locator = locatorFactory();
  const count = await locator.count();

  for (let index = 0; index < count; index += 1) {
    const candidate = locator.nth(index);
    if (await candidate.isVisible()) {
      const beforeUrl = page.url();
      await candidate.click();
      await page.waitForLoadState('domcontentloaded').catch(() => {});
      report.push(`- ${label}: bouton/lien present + clic OK (${beforeUrl} -> ${page.url()}).`);
      return true;
    }
  }

  report.push(`- ${label}: front absent ou bouton non visible.`);
  return false;
}

async function fillByName(page, fieldName, value) {
  const field = page.locator(`[name="${fieldName}"]`).first();
  if (await field.count()) {
    await field.fill(value);
    return true;
  }
  return false;
}

async function submitByButton(page, pattern) {
  const button = page.getByRole('button', { name: pattern }).first();
  if (await button.count()) {
    await button.click();
    await page.waitForLoadState('domcontentloaded').catch(() => {});
    return true;
  }
  return false;
}

test('navigation fonctionnelle complete: auth, pages protegees et clics visibles', async ({ page }, testInfo) => {
  const diagnostics = collectDiagnostics(page);
  const stamp = Date.now();
  const user = {
    email: `trio.e2e.${stamp}@test.local`,
    pseudo: `e2e_${stamp}`,
    password: 'Password123!'
  };
  const report = [
    '# Rapport Playwright FUNCTIONAL-01',
    '',
    `- Base URL: ${testInfo.project.use.baseURL || 'http://localhost:8080'}`,
    `- Navigateur: ${testInfo.project.name}`,
    `- Utilisateur: ${user.email}`,
    ''
  ];

  await page.goto('/register');
  await expectHealthyPage(page, diagnostics, 'register');
  await fillByName(page, 'email', user.email);
  await fillByName(page, 'pseudo', user.pseudo);
  await fillByName(page, 'password', user.password);
  await submitByButton(page, /Cr.er mon compte|Register|S'inscrire/i);
  await expectHealthyPage(page, diagnostics, 'post-register');
  report.push(`- register: formulaire present + backend OK (${page.url()}).`);

  await page.goto('/logout');
  await page.waitForLoadState('domcontentloaded').catch(() => {});

  await page.goto('/login');
  await expectHealthyPage(page, diagnostics, 'login');
  await fillByName(page, 'email', user.email);
  await fillByName(page, 'password', user.password);
  await submitByButton(page, /Se connecter|Login/i);
await expect(page).toHaveURL(/.*\/accueil.*/);
await expectHealthyPage(page, diagnostics, 'post-login-accueil');
report.push(`- login: formulaire present + backend OK (${page.url()}).`);

await page.getByRole('link', { name: /Jouer/i }).click();
await expect(page).toHaveURL(/.*\/games.*/);
await expectHealthyPage(page, diagnostics, 'post-login-games');

  await clickFirstVisible(page, report, 'navigation /stats', () => page.getByRole('link', { name: /Statistiques|Stats/i }));
  await expect(page).toHaveURL(/.*\/stats.*/);
  await expectHealthyPage(page, diagnostics, 'stats');

  await clickFirstVisible(page, report, 'navigation /games', () => page.getByRole('link', { name: /Lobby|Parties disponibles|Jeux|Games/i }));
  if (!/\/games(\?|$)/.test(new URL(page.url()).pathname)) {
    await page.goto('/games');
  }
  await expectHealthyPage(page, diagnostics, 'games');

  await clickFirstVisible(page, report, 'navigation /games/finished', () => page.getByRole('link', { name: /Parties termin.es|Archives|Finished/i }));
  await expect(page).toHaveURL(/.*\/games\/finished.*/);
  await expectHealthyPage(page, diagnostics, 'finished');

  await page.goto('/games');
  await expectHealthyPage(page, diagnostics, 'games-before-create');
  const createClicked = await clickFirstVisible(page, report, 'navigation /games/create', () => page.getByRole('link', { name: /Cr.er une partie|Create/i }));
  if (!createClicked) {
    await page.goto('/games/create');
    report.push('- navigation /games/create: endpoint present teste par URL directe.');
  }
  await expect(page).toHaveURL(/.*\/games\/create.*/);
  await expectHealthyPage(page, diagnostics, 'create');

  const variant = page.locator('select[name="variant"]').first();
  if (await variant.count()) {
    const optionValues = await variant.locator('option:not([disabled])').evaluateAll((options) =>
      options.map((option) => option.value).filter(Boolean)
    );
    if (optionValues.length > 0) {
      await variant.selectOption(optionValues[0]);
    } else {
      report.push('- create.variant: aucune option selectionnable cote front.');
    }
  } else {
    report.push('- create.variant: champ absent cote front.');
  }
  if (!(await fillByName(page, 'maxPlayers', '3'))) {
    report.push('- create.maxPlayers: champ absent cote front.');
  }
  await submitByButton(page, /Cr.er la partie|Create/i);
  await expectHealthyPage(page, diagnostics, 'post-create');
  report.push(`- create: bouton present + backend OK ou blocage propre sans 5xx (${page.url()}).`);

  const createdUrl = new URL(page.url());
  const createdId = createdUrl.searchParams.get('created');
  if (createdId) {
    await page.goto(`/games/${createdId}`);
    await expectHealthyPage(page, diagnostics, 'created-game');
    report.push(`- /games/${createdId}: front present + endpoint backend OK.`);

    await clickFirstVisible(page, report, 'game.join', () => page.getByRole('button', { name: /Rejoindre/i }));
    await expectHealthyPage(page, diagnostics, 'after-game-join-click');

    await clickFirstVisible(page, report, 'game.start', () => page.getByRole('button', { name: /D.marrer/i }));
    await expectHealthyPage(page, diagnostics, 'after-game-start-click');

    const moveButtons = await page.getByRole('button', { name: /Jouer|Valider|Passer|Annoncer/i }).count();
    if (moveButtons === 0) {
      report.push('- game.move: front absent ou precondition metier non atteinte; non teste en Playwright.');
    }
  } else {
    report.push('- create: identifiant de partie non visible dans URL; page game non testee par URL directe.');
  }

  await page.goto('/games');
  await expectHealthyPage(page, diagnostics, 'games-final');
  await clickFirstVisible(page, report, 'lobby.join', () => page.getByRole('button', { name: /Rejoindre/i }));
  await expectHealthyPage(page, diagnostics, 'after-lobby-join-click');

  await clickFirstVisible(page, report, 'logout', () => page.getByRole('link', { name: /D.connexion|Logout/i }));
  await expectHealthyPage(page, diagnostics, 'after-logout');

  expect(diagnostics.consoleErrors, 'no critical console errors').toEqual([]);

  report.push('');
  report.push('## Synthese technique');
  report.push('- Bouton present + backend OK: register, login, navigation protegee, creation si l endpoint repond sans 5xx.');
  report.push('- Bouton present + backend KO propre: documente via clics join/start si preconditions backend refusent sans 5xx.');
  report.push('- Front present + endpoint absent: aucun 404/5xx detecte par le test.');
  report.push('- Endpoint present + front absent: clics non visibles notes comme absents, sans inventer de bouton.');
  report.push('');
  report.push('## Diagnostics');
  report.push(`- Console errors: ${diagnostics.consoleErrors.length}`);
  report.push(`- Page errors: ${diagnostics.pageErrors.length}`);
  report.push(`- HTTP 404: ${diagnostics.notFoundResponses.length}`);
  report.push(`- Failed requests: ${diagnostics.requestFailures.length}`);
  report.push(`- HTTP 5xx: ${diagnostics.serverErrors.length}`);

  const reportPath = testInfo.outputPath('functional-navigation-report.md');
  fs.writeFileSync(reportPath, `${report.join('\n')}\n`, 'utf8');
  console.log(report.join('\n'));
});
