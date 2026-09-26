# 📡 Driver Qualcomm Atheros AR9271 (USB Wi-Fi)

## Propósito
Paquete oficial de instalación desatendida del controlador para el adaptador Wi-Fi USB Qualcomm Atheros AR9271 (usado frecuentemente para auditorías de red y antenas externas).

## Incluye
- `netathurx.inf`: Archivo de información de instalación del dispositivo.
- `athurx.sys`: Binario del controlador para arquitectura de 64 bits.
- `athurextx.cat`: Catálogo de seguridad firmado por Microsoft WHQL.
- `instalar_driver.bat`: Script con auto-elevación UAC a Administrador que registra el driver en el almacén de Windows vía `pnputil`.

## No incluye
- Software de monitoreo o utilidades de terceros.

## Cómo Instalar
1. Conectar la antena o adaptador USB Atheros AR9271 a la computadora.
2. Hacer clic derecho sobre `instalar_driver.bat` y seleccionar **"Ejecutar como Administrador"** (o hacer doble clic; el script solicitará elevación automáticamente).
3. `pnputil` agregará el paquete `netathurx.inf` a la tienda de controladores del sistema y activará la interfaz de red inmediatamente.

## Relación con el Workspace MÖLDEA
- [AGENTS.md](../../../AGENTS.md)
- [proyectos/drivers/README.md](../README.md)
- [docs/GUIA_DOCUMENTACION.md](../../../docs/GUIA_DOCUMENTACION.md)
