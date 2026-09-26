# Pipeline Real de Ofuscación (`src/` → `dist/`) — Proyecto

> **AGENTS.md dice que `dist/` es "salida ofuscada" — este documento es la receta real para generarla, no solo el nombre de la carpeta.** No ejecutar este pipeline sin necesidad real (ver sección "¿Hace falta esto?" más abajo).

---

## 1. ¿Hace falta esto?

Ofuscar JavaScript **no es seguridad real, es "esconder"**. Alguien con tiempo puede des-ofuscar código JS. Sirve para desalentar copia casual o cumplir un requisito contractual de "no entregar código legible" — **no sirve** como reemplazo de proteger de verdad una fórmula o lógica valiosa. Si algo necesita protección real, la arquitectura correcta es moverlo a un backend (Cloud Function / API) que el cliente nunca ve — no ofuscación del lado del cliente.

Si igual hace falta (por contrato, o para desalentar copia casual), seguir los pasos de abajo.

## 2. Stack

* **`esbuild`** — bundlea todo el árbol de `src/` en un único archivo (aplanar imports ya dificulta rastrear la estructura).
* **`javascript-obfuscator`** — ofusca ese bundle (renombra variables, codifica strings, aplana el flujo de control).

## 3. Pipeline

```bash
npm install --save-dev esbuild javascript-obfuscator

# 1. Bundlear a un solo archivo
npx esbuild src/index.mjs --bundle --platform=node --format=esm --outfile=dist/_bundle.mjs

# 2. Ofuscar ese bundle
npx javascript-obfuscator dist/_bundle.mjs --output dist/index.mjs --config obfuscator.config.json
```

`obfuscator.config.json` de referencia (preset agresivo, razonable para Node):
```json
{
  "compact": true,
  "controlFlowFlattening": true,
  "controlFlowFlatteningThreshold": 0.75,
  "deadCodeInjection": true,
  "deadCodeInjectionThreshold": 0.4,
  "stringArray": true,
  "stringArrayEncoding": ["base64"],
  "stringArrayThreshold": 0.75,
  "selfDefending": true,
  "identifierNamesGenerator": "hexadecimal",
  "renameGlobals": false
}
```

## 4. Automatizar

Agregar a `package.json`:
```json
"scripts": {
  "build:dist": "esbuild src/index.mjs --bundle --platform=node --format=esm --outfile=dist/_bundle.mjs && javascript-obfuscator dist/_bundle.mjs --output dist/index.mjs --config obfuscator.config.json"
}
```

## 5. Validar antes de entregar

* Correr `dist/index.mjs` y confirmar que funciona igual que `src/index.mjs`.
* Nunca editar dentro de `dist/` a mano (regla de `AGENTS.md`) — todo cambio va en `src/` y se regenera con `npm run build:dist`.
