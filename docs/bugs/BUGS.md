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
* **Solución:** Se rediseñó `.item-card` en un layout vertical de dos filas espaciosas: la fila superior alberga el botón de estado `[FALTA/LISTO]`, el nombre completo sin truncar (`word-break: break-word`, `font-size: 16px`, sin `nowrap`) y las acciones; la fila inferior aloja los metadatos y el stepper de cantidad alineado a la derecha, garantizando 0% de solapamiento. Además, se mejoró la legibilidad de productos en stock (`.tachado`) con contraste nítido y se reposicionó el botón `+ AÑADIR AHORA` a ancho completo.



