# Guía de Uso y Operación — Proyecto

> Manual de referencia para desarrollar, probar y colaborar en el proyecto.

---

## 1. Flujo Diario de Trabajo

1. **Al iniciar la jornada:**
   * Abrir y leer [`docs/ESTADO_ACTUAL.md`](../ESTADO_ACTUAL.md) para saber exactamente qué quedó pendiente.
   * Revisar si hay advertencias o bugs activos en [`docs/bugs/BUGS.md`](../bugs/BUGS.md).

2. **Durante el desarrollo:**
   * Ejecutar `SERVER.bat` para previsualizar los cambios en tiempo real en `http://127.0.0.1:3000`.
   * Mantener los módulos desacoplados en `src/`.

3. **Al finalizar la tarea o jornada:**
   * Ejecutar la auditoría de calidad: `npm run revisar`.
   * Actualizar el parte de turno en `docs/ESTADO_ACTUAL.md`.
   * Ejecutar `COMMITEAR.bat` para registrar los cambios en Git local con un mensaje claro.
