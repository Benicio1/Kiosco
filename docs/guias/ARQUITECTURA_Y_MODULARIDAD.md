# Arquitectura de Software, Modularidad & Desacoplamiento — Proyecto

> **PROPÓSITO DE ESTE DOCUMENTO:**  
> Define los fundamentos formales de modularidad, conascencia y desacoplamiento para minimizar el radio de impacto (*blast radius*) y garantizar que los cambios en un módulo no degraden componentes adyacentes o distales.

---

## 1. Fundamentos de Conascencia (Connascence)

Dos componentes son **conascentes** si un cambio en uno exige modificar el otro para preservar la corrección del sistema.

### Tipos de Conascencia y Directivas:

| Tipo | Categoría | Descripción Mecánica | Regla de Mitigación / Diseño |
|---|---|---|---|
| **Conascencia de Nombre (CoN)** | Estática | Acuerdo sobre el identificador de un método o propiedad. | Permitida y deseable (refactorizable automáticamente). |
| **Conascencia de Tipo (CoT)** | Estática | Acuerdo sobre el tipo de dato, interfaz o firma de función. | Forzar contratos de interfaz y validación de tipos. |
| **Conascencia de Convención (CoM)** | Estática | Significado intrínseco a valores mágicos o números sueltos. | Reemplazar por Enums u Objetos de Valor (*Value Objects*). |
| **Conascencia de Posición (CoP)** | Estática | Dependencia del orden ordinal de parámetros en funciones. | **Prohibida para 4+ parámetros.** Usar Objetos de Parámetros (`f({ a, b, c })`). |
| **Conascencia de Algoritmo (CoA)** | Estática | Varios módulos deben implementar el mismo algoritmo idéntico. | Encapsular el algoritmo en un servicio o librería única compartida. |
| **Conascencia de Ejecución (CoE)** | Dinámica | Obligación estricta de invocar métodos en un orden secuencial. | Reemplazar por constructores formales, Builders o máquinas de estado. |
| **Conascencia de Tiempo (CoTm)** | Dinámica | Dependencia de ventanas temporales o latencias específicas. | Emplear colas asíncronas idempotentes o bloqueos optimistas. |
| **Conascencia de Valor (CoV)** | Dinámica | Múltiples valores distribuidos deben mutar simultáneamente. | Encapsular la transacción dentro de un único Agregado de Dominio. |
| **Conascencia de Identidad (CoI)**| Dinámica | Múltiples entidades referencian la misma instancia mutable. | Inmutabilidad de estructuras y paso de estados por valor. |

### 📐 Dimensiones Operativas:
* **Fuerza (Strength):** Transformar siempre formas dinámicas en formas estáticas débiles (Nombre y Tipo).
* **Grado (Degree):** Minimizar el número de módulos que comparten una dependencia.
* **Localidad (Locality):** A mayor distancia física/arquitectónica entre dos módulos, menor debe ser la fuerza de su acoplamiento.

---

## 2. Métricas de Robert C. Martin y la Secuencia Principal

Para evitar la fragilidad del software, el diseño debe equilibrar **Abstracción** e **Inestabilidad**:

1. **Inestabilidad ($I$):** $I = \frac{C_e}{C_a + C_e}$  
   * $I = 0$: Máxima estabilidad (muchos dependen de él, él no depende de nadie).
   * $I = 1$: Máxima inestabilidad (él depende de muchos, nadie depende de él).
2. **Abstractez ($A$):** $A = \frac{N_a}{N_c + N_a}$
3. **Distancia a la Secuencia Principal ($D$):** $D = |A + I - 1|$

### Cuadrantes Críticos a Evitar:
* ⚠️ **Zona de Dolor ($A \approx 0, I \approx 0$):** Módulos altamente concretos de los que dependen todos los demás. Cambiar una línea rompe el sistema completo.  
  * *Solución:* Extraer interfaces abstractas en el punto de consumo (Puertos y Adaptadores).
* ⚠️ **Zona de Inutilidad ($A \approx 1, I \approx 1$):** Abstracciones puras sin ningún consumidor real (sobreingeniería).

---

## 3. Arquitectura Hexagonal y Ley de Demeter

1. **Regla de Dependencia:** Las dependencias del código fuente solo apuntan hacia el interior:
   * `core/` (Dominio puro e interfaces) $\leftarrow$ `adapters/` (Infraestructura, APIs, DB) $\leftarrow$ `gui/ / cli/` (Presentación).
2. **Ley de Demeter (Mínimo Conocimiento):**
   * Un método solo debe llamar a sus colaboradores directos o parámetros recibidos.
   * ❌ *Prohibido:* `servicio.getEmpresa().getCliente().getFactura().calcular()`
   * ✔ *Correcto:* `servicio.calcularFacturaCliente(facturaId)`

---

## 4. Matriz de Diagnóstico y Refactorización

| Síntoma / Antipatrón | Principio Afectado | Causa en Conascencia | Refactorización de Desacoplamiento |
|---|---|---|---|
| **Efecto Dominó (*Shotgun Surgery*)** | SRP, OCP | Conascencia de Convención y Estructura. | Consolidar la lógica en un único Agregado de Dominio; encapsular con Value Objects. |
| **Fragilidad Transversal** | Ley de Demeter, ISP | Conascencia de Posición y Tipo. | Segregar interfaces extensas, emplear Parameter Objects e Inversión de Dependencias. |
| **Rigidez Estructural (*Zone of Pain*)** | DIP, SAP | Conascencia de Identidad y Algoritmo. | Extraer interfaces abstractas en el punto de consumo; reubicar implementación en adaptadores. |
| **Orquestación Oculta** | Encapsulamiento, LSP| Conascencia de Ejecución. | Reemplazar mutaciones intermedias por constructores formales o interfaces fluidas tipadas. |
| **Filtración de Infraestructura** | Arquitectura Limpia | Conascencia Externa. | Introducir interfaces de Repositorio en el dominio; aislar ORMs en adaptadores. |

---

## 5. Persistencia, BaaS y Seguridad en Profundidad

1. **Defensa en Profundidad (Autorización en el Motor de Persistencia):**
   * Las reglas de acceso y autorización deben forzarse en el nivel más profundo posible:
     * **PostgreSQL / Supabase:** Row Level Security (RLS) mandatorio en toda tabla con cláusulas `USING` y `WITH CHECK`, e índices B-Tree en FKs multitenant.
     * **Firestore / BaaS:** Reglas declarativas con `request.auth.uid` y validación formal de esquemas entrantes con `request.resource.data`.
     * **Bases de Datos Embebidas (PocketBase / SQLite WAL):** Reglas de acceso por colección y hooks de backend en JS/Go.
   * ⛔ **Antipatrón Crítico:** Nunca delegar la autorización exclusivamente en la UI o en microservicios intermediarios si los endpoints directos de persistencia quedan expuestos a clientes.
2. **Segregación de PII y Datos Confidenciales:**
   * Desacoplar atributos privados en tablas o subcolecciones restringidas para evitar divulgaciones accidentales en consultas generales.
3. **Protección Perimetral Zero Trust:**
   * Interceptar tráfico en el borde (Edge Network) con **Cloudflare Turnstile** para validación humana sin CAPTCHAs invasivos y mitigación WAF contra ataques de fuerza bruta.

---

## 6. Pipeline Agéntico en Planning Mode & Gobernanza de Contratos

Para intervenciones de arquitectura o refactorización complejas, los agentes coordinan su flujo de trabajo en 4 fases operativas:

1. **Fase 1 — Análisis Estático e Integridad:**
   * Descarte de claves y secretos expuestos en código e historial (TruffleHog).
   * Mapeo de flujo de datos sensibles y detección de inyecciones en AST (Semgrep + Bearer).
   * Escaneo de CVEs en dependencias y Dockerfiles (Trivy).
2. **Fase 2 — Gobernanza de Contratos & Persistencia:**
   * Forzado de DTOs tipados (OpenAPI / JSON Schema) para erradicar el *over-fetching* de contraseñas o roles internos hacia el cliente.
   * Auditoría de aislamiento de tenant por `auth.uid()` (PostgreSQL RLS / Firebase Rules).
3. **Fase 3 — Verificación Dinámica E2E & Navegador:**
   * Simulación de flujos de autenticación e interacción DOM mediante Playwright MCP.
   * Validación de cookies de sesión con flags `HttpOnly`, `Secure` y `SameSite`.
4. **Fase 4 — Observabilidad, GEO y Entrega:**
   * Enmascaramiento estricto de trazas de pila (stack traces) y volcados SQL en respuestas de error HTTP (Sentry).
   * Publicación y validación del estándar `/llms.txt` y metadatos JSON-LD para citabilidad en motores generativos.

---

## 7. Directivas Prácticas para Nuevos Módulos

1. **Límite Máximo de 400 Líneas por Archivo:** Ningún archivo de código (`.js`, `.mjs`, `.ts`, `.py`, etc.) debe superar las 400 líneas de código. Si excepcionalmente alcanza ~410-420 líneas, debe incluir en su cabecera un comentario `// JUSTIFICACION-MODULARIDAD:` justificando que resuelve una única función o algoritmo indivisible altamente cohesivo, y que fragmentarlo aumentaría la complejidad y el acoplamiento accidental más que dejarlo unificado.
2. Toda función con 4 o más parámetros se declara como objeto destructurado: `export function procesar({ archivo, opciones, salida }) {}`.
3. Los modelos de datos no exponen mutaciones de estado arbitrarias sin validación de invariantes.
4. El dominio nunca importa módulos de `node:fs`, `fetch` ni drivers de base de datos directamente; consume un adaptador inyectado o utilitarios puros.
5. Las respuestas HTTP nunca exponen la entidad de BD completa; devuelven siempre un DTO sanitizado.
6. **Testeo y Verificación Mandatoria:** Prohibido dar por terminada una tarea sin verificación activa (`npm test` o validación en ejecución real). Terminado = Probado + Documentado.
