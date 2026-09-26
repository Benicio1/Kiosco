# docs/ — Documentación del Proyecto

> Esta carpeta contiene la memoria y las decisiones del proyecto. Las reglas generales están en [AGENTS.md](../AGENTS.md).

## Dónde documentar cada cosa

| Necesidad | Archivo o carpeta | Qué debe contener |
|---|---|---|
| Visión y requerimientos | [PLAN.md](./PLAN.md) | Objetivo, alcance, decisiones pendientes y checklist de trabajo. |
| Estado para el siguiente turno | [ESTADO_ACTUAL.md](./ESTADO_ACTUAL.md) | Qué funciona, qué se está haciendo, bloqueos y próximo paso. |
| Error técnico no trivial | [bugs/BUGS.md](./bugs/BUGS.md) | Síntoma, causa y solución para evitar regresiones. |
| Decisión estructural | [decisiones/](./decisiones/) | ADR con contexto, decisión, alternativas y consecuencias. |
| Guía paso a paso | [guias/](./guias/) | Instalación, uso, arquitectura, despliegue u operación. |
| Histórico cerrado | [archivo/](./archivo/) | Documentos obsoletos que no deben confundirse con el estado actual. |

## Regla para crear carpetas nuevas

Toda carpeta nueva debe nacer con un archivo README.md. Ese README explica el propósito de la carpeta, qué archivos deben vivir allí, qué queda fuera y dónde continúa la documentación relacionada. No se crean carpetas genéricas como misc/, temp/ u otros/ sin una decisión registrada.

## Checklist antes de terminar una tarea

- [ ] Actualicé docs/ESTADO_ACTUAL.md.
- [ ] Si hubo un error no trivial, registré docs/bugs/BUGS.md.
- [ ] Si cambié arquitectura, agregué o actualicé un ADR.
- [ ] Si creé una carpeta, agregué su README.md.
- [ ] Registré el hito y la versión cuando corresponde.
- [ ] Verifiqué que los enlaces relativos funcionen.

Para el estándar completo y ejemplos de redacción, consultar GUIA_DOCUMENTACION.md.
