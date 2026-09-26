// build_dist.mjs - Generador de distribución para la nube y uso portable en el celular
// Crea la carpeta dist/ lista para GitHub Pages, Vercel, Netlify y genera un archivo único portable

import fs from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const PROJECT_ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const SRC_GUI = path.join(PROJECT_ROOT, 'src', 'gui');
const DIST_DIR = path.join(PROJECT_ROOT, 'dist');

async function build() {
  console.log('🚀 Iniciando compilación de distribución en dist/ ...');

  await fs.mkdir(DIST_DIR, { recursive: true });

  // 1. Leer archivos fuente
  const htmlSrc = await fs.readFile(path.join(SRC_GUI, 'index.html'), 'utf-8');
  const stylesCss = await fs.readFile(path.join(SRC_GUI, 'styles.css'), 'utf-8');
  const componentsCss = await fs.readFile(path.join(SRC_GUI, 'components.css'), 'utf-8');
  const modalsCss = await fs.readFile(path.join(SRC_GUI, 'modals.css'), 'utf-8');
  const appJs = await fs.readFile(path.join(SRC_GUI, 'app.js'), 'utf-8');
  const manifestJson = await fs.readFile(path.join(SRC_GUI, 'manifest.json'), 'utf-8');

  // 2. Escribir distribución modular para hosting web (GitHub Pages, Vercel, Netlify, Cloudflare)
  await fs.writeFile(path.join(DIST_DIR, 'index.html'), htmlSrc, 'utf-8');
  await fs.writeFile(path.join(DIST_DIR, 'styles.css'), stylesCss, 'utf-8');
  await fs.writeFile(path.join(DIST_DIR, 'components.css'), componentsCss, 'utf-8');
  await fs.writeFile(path.join(DIST_DIR, 'modals.css'), modalsCss, 'utf-8');
  await fs.writeFile(path.join(DIST_DIR, 'app.js'), appJs, 'utf-8');
  await fs.writeFile(path.join(DIST_DIR, 'manifest.json'), manifestJson, 'utf-8');

  // 3. Generar versión monolítica portable para enviar por WhatsApp o abrir directo en el teléfono
  const combinedCss = `\n<style>\n${stylesCss}\n${componentsCss}\n${modalsCss}\n</style>\n`;
  const combinedJs = `\n<script>\n${appJs}\n</script>\n`;

  let portableHtml = htmlSrc
    .replace(/<link rel="stylesheet"[^>]*>/g, '')
    .replace('<script src="./app.js"></script>', '')
    .replace('</head>', `${combinedCss}\n</head>`)
    .replace('</body>', `${combinedJs}\n</body>`);

  await fs.writeFile(path.join(DIST_DIR, 'kiosco_app_celular.html'), portableHtml, 'utf-8');

  console.log('✅ Compilación completada con éxito en dist/:');
  console.log('  📁 dist/index.html + assets (Listo para subir a GitHub Pages / Vercel / Netlify)');
  console.log('  📄 dist/kiosco_app_celular.html (Archivo único portable para abrir en el celular sin internet ni PC)');
}

build().catch((err) => {
  console.error('❌ Error en el proceso de build:', err);
  process.exit(1);
});
