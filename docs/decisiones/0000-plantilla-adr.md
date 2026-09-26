# ADR-0000: Título de la Decisión Técnica

> **¿QUÉ ES UN ADR (Architecture Decision Record)?:**  
> Es un documento breve que captura una **decisión técnica importante y difícil de revertir**, junto con su contexto, consecuencias y las alternativas que fueron descartadas.  
> **REGLA:** No todo cambio merece un ADR; solo decisiones estructurales (elección de base de datos, frameworks, protocolos de seguridad, etc.).

* **Fecha:** YYYY-MM-DD
* **Estado:** Propuesto | Aceptado | Reemplazado por ADR-XXXX
* **Autor:** MÖLDEAstudio

---

## 1. Contexto y Problema
¿Qué situación técnica o de negocio estamos abordando? ¿Qué restricciones existen?

## 2. Decisión Tomada
¿Cuál es la solución elegida y qué enfoque adoptamos?

## 3. Consecuencias
* **Positivas:** ¿Qué beneficios obtenemos?
* **Negativas / Compromisos:** ¿Qué costos o limitaciones aceptamos?

## 4. Alternativas Descartadas
¿Qué otras opciones evaluamos y por qué no las elegimos?

---

## 💡 Ejemplo de Referencia Real (Para redactar futuros ADRs):
```markdown
# ADR-0003: Persistencia Local de Configuraciones con SQLite Embedido en lugar de Archivos JSON Planos

* **Fecha:** 2026-08-24
* **Estado:** Aceptado
* **Autor:** Marco & MÖLDEAstudio

### 1. Contexto y Problema
Al guardar más de 500 cotizaciones históricas y 50 perfiles de materiales, la reescritura constante de un archivo JSON plano provocaba bloqueos de concurrencia y riesgo de corrupción en caso de cierre inesperado.

### 2. Decisión Tomada
Se adopta SQLite nativo mediante el módulo 'better-sqlite3' con transacciones WAL (Write-Ahead Logging).

### 3. Consecuencias
* Positivas: Consultas indexadas en < 2ms, transacciones ACID seguras contra cortes de energía y cero dependencias de servidores externos.
* Negativas: Requiere compilar binarios nativos si se distribuye a múltiples arquitecturas.

### 4. Alternativas Descartadas
* PostgreSQL / MySQL: Descartados por requerir instalación de servicios externos en la máquina del usuario.
* Archivos JSON por registro: Descartados por lentitud al generar estadísticas agregadas.
```
