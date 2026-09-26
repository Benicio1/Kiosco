// build_dist.mjs - Generador de distribución para la nube y uso portable en el celular
import fs from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const PROJECT_ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const SRC_GUI = path.join(PROJECT_ROOT, 'src', 'gui');
const DIST_DIR = path.join(PROJECT_ROOT, 'dist');
const ICONS_DIR = path.join(PROJECT_ROOT, 'icons');

async function copyDir(src, dest) {
  await fs.mkdir(dest, { recursive: true });
  const entries = await fs.readdir(src, { withFileTypes: true });
  for (const entry of entries) {
    const srcPath = path.join(src, entry.name);
    const destPath = path.join(dest, entry.name);
    if (entry.isDirectory()) {
      await copyDir(srcPath, destPath);
    } else {
      await fs.copyFile(srcPath, destPath);
    }
  }
}

async function build() {
  console.log('🚀 Iniciando compilación de distribución en dist/ ...');

  await fs.mkdir(DIST_DIR, { recursive: true });

  // 1. Leer archivos fuente
  const htmlSrc = await fs.readFile(path.join(SRC_GUI, 'index.html'), 'utf-8');
  const stylesCss = await fs.readFile(path.join(SRC_GUI, 'styles.css'), 'utf-8');
  const componentsCss = await fs.readFile(path.join(SRC_GUI, 'components.css'), 'utf-8');
  const modalsCss = await fs.readFile(path.join(SRC_GUI, 'modals.css'), 'utf-8');
  const pwaCss = await fs.readFile(path.join(SRC_GUI, 'pwa.css'), 'utf-8');
  const appJs = await fs.readFile(path.join(SRC_GUI, 'app.js'), 'utf-8');
  const pwaJs = await fs.readFile(path.join(SRC_GUI, 'pwa.js'), 'utf-8');
  const swJs = await fs.readFile(path.join(PROJECT_ROOT, 'sw.js'), 'utf-8');
  const manifestJson = await fs.readFile(path.join(PROJECT_ROOT, 'manifest.json'), 'utf-8');

  // 2. Escribir distribución modular para hosting web
  await fs.writeFile(path.join(DIST_DIR, 'index.html'), htmlSrc, 'utf-8');
  await fs.writeFile(path.join(DIST_DIR, 'styles.css'), stylesCss, 'utf-8');
  await fs.writeFile(path.join(DIST_DIR, 'components.css'), componentsCss, 'utf-8');
  await fs.writeFile(path.join(DIST_DIR, 'modals.css'), modalsCss, 'utf-8');
  await fs.writeFile(path.join(DIST_DIR, 'pwa.css'), pwaCss, 'utf-8');
  await fs.writeFile(path.join(DIST_DIR, 'app.js'), appJs, 'utf-8');
  await fs.writeFile(path.join(DIST_DIR, 'pwa.js'), pwaJs, 'utf-8');
  await fs.writeFile(path.join(DIST_DIR, 'sw.js'), swJs, 'utf-8');
  await fs.writeFile(path.join(DIST_DIR, 'manifest.json'), manifestJson, 'utf-8');

  // Copiar iconos a dist/icons
  await copyDir(ICONS_DIR, path.join(DIST_DIR, 'icons'));

  // 3. Generar versión monolítica portable
  const combinedCss = `\n<style>\n${stylesCss}\n${componentsCss}\n${modalsCss}\n${pwaCss}\n</style>\n`;
  const combinedJs = `\n<script>\n${appJs}\n${pwaJs}\n</script>\n`;

  let portableHtml = htmlSrc
    .replace(/<link rel="stylesheet"[^>]*>/g, '')
    .replace('<script src="./app.js"></script>', '')
    .replace('<script src="./pwa.js"></script>', '')
    .replace('</head>', `${combinedCss}\n</head>`)
    .replace('</body>', `${combinedJs}\n</body>`);

  await fs.writeFile(path.join(DIST_DIR, 'kiosco_app_celular.html'), portableHtml, 'utf-8');

  console.log('✅ Compilación completada con éxito en dist/ con PWA completa');
}

build().catch((err) => {
  console.error('❌ Error en el proceso de build:', err);
  process.exit(1);
});
