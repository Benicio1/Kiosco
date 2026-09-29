# Bugs Conocidos y Soluciones (In-Repo Memory Bank)

> **CÓMO SE USA ESTE ARCHIVO:**  
> Este documento registra problemas técnicos detectados y cómo fueron resueltos en la arquitectura para **evitar regresiones futuras**.  
> **REGLA:** Todo error no trivial que tomó tiempo diagnosticar se registra aquí antes de cerrar la tarea.  
> **FORMATO OBLIGATORIO:**  
> ```markdown
> ### BUG-001: Título claro del problema
> * **Síntoma:** ¿Qué falló exactamente o qué error arrojó?
> * **Causa:** ¿Por qué ocurrió la falla en el código?
> * **Solución:** ¿Qué cambio específico se realizó para resolverlo y blindarlo?
> ```

---

## 💡 Ejemplo de Referencia Real (Para consultar antes de escribir código):

### BUG-001: Volumen negativo o cero al procesar mallas STL con caras invertidas
* **Síntoma:** El cálculo de cotización devolvía $0 o valores negativos en modelos 3D complejos.
* **Causa:** El algoritmo de cálculo por tetraedros asumía normales orientadas hacia afuera; mallas con triángulos invertidos generaban volúmenes negativos parciales.
* **Solución:** Se aplicó valor absoluto a cada componente del producto vectorial cruzado y se añadió una validación previa que advierte si la malla no es *manifold* (cerrada).

---

### BUG-002: Búsqueda móvil sensible a tildes omitía productos como "Guaymallén"
* **Síntoma:** Al tipear "guaymallen" sin acento en el buscador del teléfono, no aparecía el alfajor "Guaymallén".
* **Causa:** La búsqueda comparaba cadenas directamente con `toLowerCase()` sin remover diacríticos Unicode.
* **Solución:** Se implementó normalización NFD (`.normalize('NFD').replace(/[\u0300-\u036f]/g, '')`) tanto en la query como en el nombre del producto en `src/core/KioscoList.mjs`.

### BUG-003: Exceso de límite de 400 líneas en archivo styles.css
* **Síntoma:** La suite de pruebas de arquitectura (`tests/kiosco.test.mjs`) falló reportando que `styles.css` alcanzaba 618 líneas.
* **Causa:** Se incluyeron tanto los estilos base/layout como los componentes de tarjetas y modales en un único archivo.
* **Solución:** Se extrajeron los componentes de tarjetas, controles de cantidad y modales a `src/gui/components.css` (<220 líneas) manteniendo `src/gui/styles.css` en <200 líneas, desacoplados y respetando el límite estricto de 400 líneas.

### BUG-004: Inercia táctil en tarjetas métricas y ausencia de caja directa de adición rápida en pantalla principal
* **Síntoma:** El usuario no podía interactuar con los recuadros de "Control de reposición y stock" superiores y la adición de productos requería abrir un modal secundario poco evidente.
* **Causa:** Las métricas superiores eran contenedores `<div>` no interactivos en vez de botones con estado, y la adición dependía de un icono diminuto `+` que abría un diálogo flotante en lugar de estar accesible de inmediato en la pantalla.
* **Solución:** Se convirtieron las 3 tarjetas de stock en botones táctiles interactivos (`<button class="metric-btn">`) que filtran la lista con un toque, y se agregó una caja directa visible y permanente en la pantalla principal ("➕ Añadir lo que falta") con input, selectores de cantidad y feedback visual destacado (glow animado en el producto agregado + toast vibrante).

### BUG-006: Stepper de cantidad solapaba y truncaba los nombres de productos en pantallas móviles estrechas
* **Síntoma:** En smartphones con pantallas de menos de 400px de ancho, los nombres de los productos se truncaban a 1 o 2 letras ("F...", "A...", "P...") y quedaban tapados por el control de cantidad `[- 1 caja +]`, haciendo imposible leer qué producto estaba en stock o faltaba.
* **Causa:** `.item-card` utilizaba un layout flex horizontal en una sola fila con 4 elementos interactivos (botón de estado, nombre, stepper de cantidad y botones de edición/borrado), dejando menos de 30px disponibles para el texto del título y forzando `text-overflow: ellipsis`.
### BUG-007: Eventos de clic en pantalla de lectura no ocultaban las barras en móviles táctiles
* **Síntoma:** Al tocar la pantalla de lectura en celulares, las barras de herramientas no se ocultaban, dejando poco espacio para leer.
* **Causa:** Escuchar el evento estándar `click` en contenedores de texto o canvas era interferido o cancelado por la inercia táctil de scroll y selección de texto de navegadores móviles (WebKit / Chromium).
* **Solución:** Se implementó detección táctil con `pointerdown` y `pointerup` midiendo delta de desplazamiento (<15px) y tiempo (<400ms) para discriminar un tap intencional de un arrastre de scroll, sumado a un botón explícito `[🔲 Pantalla]` en la cabecera y un botón flotante `[👁️ Mostrar Controles]`.

### BUG-008: Ausencia de botón de giro de pantalla por software en el visor de lectura
* **Síntoma:** El usuario no podía girar la lectura a modo horizontal sin tener activado el auto-giro del sistema en su teléfono o sin voltear el dispositivo físicamente.
* **Causa:** La aplicación dependía exclusivamente de los sensores de orientación del sistema operativo y media queries CSS `@media (orientation: landscape)`.
* **Solución:** Se añadió el botón `[🔄 Girar]` en la barra superior que conmuta la clase `.forced-landscape` en `#view-reader` aplicando transformación CSS de 90 grados (`width: 100vh; height: 100vw; transform: rotate(90deg) translateY(-100%);`) e invoca `screen.orientation.lock('landscape')` cuando está disponible.

### BUG-009: Menús ocultos dejaban una franja o borde superior del color de fondo y el botón flotante tapaba las palabras
* **Síntoma:** Al ocultar los menús para leer a pantalla completa, quedaba una franja/borde superior del color del fondo que empujaba el texto hacia abajo; además el botón flotante `[👁️ Mostrar Controles]` se quedaba fijo en medio de la pantalla tapando las frases, y deslizar para hacer scroll activaba los menús por accidente.
* **Causa:** En un contenedor flex, `transform: translateY(-100%)` oculta visualmente el encabezado pero retiene la altura física en el flujo de la pantalla. El botón flotante no tenía temporizador de desvanecimiento, y los eventos táctiles no discriminaban el desplazamiento continuo (`pointermove`/`scroll`) del toque estático (`tap`).
* **Solución:** Se reestructuraron las barras superior e inferior como overlays flotantes con `position: absolute; left: 0; right: 0;` y fondo translúcido con desenfoque de cristal (`backdrop-filter: blur(12px); background: rgba(24,24,27,0.88)`), permitiendo que el visor de lectura ocupe el 100% de la pantalla (`inset: 0`) y colapse su padding en modo inmersión (cero bordes ni franjas vacías). Se creó `ImmersionController` que discrimina el deslizamiento/scroll para lectura ininterrumpida, auto-oculta las barras tras 3.8s de inactividad y desvanece suavemente el botón flotante a transparente tras 2.2s.

### BUG-010: Modal de filtros cortaba los presets superiores y el encabezado en modo horizontal
* **Síntoma:** Al abrir el panel de filtros con la pantalla en horizontal, la mitad superior del modal (filtros Normal, Cálido, Sepia y botón de cerrar) quedaba empujada hacia arriba fuera de la pantalla y no permitía elegirlos.
* **Causa:** `.filters-sheet` utilizaba un layout bottom-sheet fijo sin límite de altura (`max-height`) ni desplazamiento vertical (`overflow-y: auto`). Al medir ~500px de alto en una pantalla apaisada de ~360px, la parte superior quedaba fuera del viewport. Además, los 6 presets en 3 columnas consumían 2 filas de altura innecesaria.
* **Solución:** Se reubicó `#modal-filters` dentro de `#view-reader` y se definieron reglas de diseño apaisado (`@media (orientation: landscape)` y `.forced-landscape`) con `max-height: 94vh; overflow-y: auto;`, centrado de modal (`align-items: center`), y un grid de presets de 6 columnas (`grid-template-columns: repeat(6, 1fr)`) que muestra los 6 filtros en una sola fila compacta junto con sliders y botones reducidos.

