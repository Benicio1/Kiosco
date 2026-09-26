# src/ — Código Fuente Productivo & Arquitectura Modular

> **LÍMITES DE ARQUITECTURA E INVARIANTES (REDUCCIÓN DE BLAST RADIUS):**  
> Esta carpeta contiene el código fuente ejecutable de la aplicación.
> Todo cambio en un módulo debe mantener acoplamiento débil para evitar que una modificación rompa componentes adyacentes o distales.

### Reglas Estrictas de Capa:
1. **Aislamiento de Dominio (`core/`):** La lógica de negocio, cálculos y entidades residen en el núcleo y **no importan frameworks de presentación (`gui/`) ni adaptadores de infraestructura**.
2. **Puertos y Adaptadores (`adapters/`):** Toda conexión externa (APIs, base de datos, sistema de archivos, hardware) se implementa mediante adaptadores que satisfacen interfaces abstractas del core.
3. **Parámetros con Nombre:** Prohibir listas largas de parámetros posicionales (`f(a, b, c, d)`); estructurar siempre mediante objetos de parámetros (`f({ a, b, c })`).
4. **Ley de Demeter (Mínimo Conocimiento):** No encadenar llamadas transitivas (`a.getB().getC().run()`). Interactuar únicamente con colaboradores inmediatos.
5. **Cero secretos:** Queda estrictamente prohibido guardar contraseñas, tokens o claves privadas en texto plano dentro de este directorio (usar variables de entorno `.env`).
6. **No contaminar con docs:** No crear carpetas temporales ni documentos extensos dentro de `src/` (la documentación vive exclusivamente en `docs/`).
7. **Ciclo de Distribución y Ofuscación (`src/` ➔ `dist/`):**
   * Todo desarrollo se hace aquí en `src/`.
   * Para entregar al cliente o desplegar en producción, se ejecuta el comando de build (`npm run build` o script de ofuscación), el cual compila, minifica y ofusca el código depositándolo en la carpeta externa `dist/`.

---

## 💡 Ejemplo de Referencia de Organización Modular de Código:
```
src/
├── core/         # Lógica pura del negocio, cálculos, Value Objects e interfaces (sin dependencias de UI)
│   ├── calculator.js
│   └── parser.js
├── gui/          # Vistas, componentes visuales, HTML/CSS o componentes React (independientes del core)
│   ├── index.html
│   └── app.js
├── adapters/     # Conexión con APIs externas, bases de datos o sistema de archivos
│   └── storage.js
└── index.js      # Punto de entrada principal / CLI launcher
```

> 🔒 **Salida de Distribución Protegida:**  
> Cuando se corre el build, se genera `dist/` (fuera de `src/`), conteniendo el binario ejecutable o los bundles ofuscados listos para distribución comercial sin exponer el código fuente.
