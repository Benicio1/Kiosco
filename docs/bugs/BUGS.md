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

### BUG-005: Form submit por defecto causaba recarga de página y bloqueo de scripts en file:///
* **Síntoma:** Al pulsar "Añadir ahora" se reiniciaba la página sin añadir el producto ni sumar en los contadores; los botones `+`/`-` junto al campo numérico eran redundantes con las flechas nativas del input.
* **Causa:** El elemento `<form>` ejecutaba el submit HTTP nativo del navegador al no tener `onsubmit="return false"`, y al abrirse localmente con doble clic (`file:///`), el navegador bloqueaba `<script type="module">` por CORS impidiendo la inicialización de listeners.
* **Solución:** Se agregó `onsubmit="event.preventDefault(); window.agregarProducto(); return false;"` en los formularios, todos los botones se tiparon como `type="button"`, se retiraron los botones `+`/`-` redundantes de la caja de adición dejando el campo numérico directo, y `app.js` se adaptó a un script universal autocontenido sin módulos de runtime para compatibilidad 100% tanto en servidor HTTP como en doble clic local.


