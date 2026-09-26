# ARQUITECTURA TÉCNICA Y ESPECIFICACIÓN DE SISTEMA: AURAPAINT STUDIO
## Software de Ilustración Digital de Alto Rendimiento (Windows, macOS, Linux)
**Nivel de Referencia:** ibis Paint X / Clip Studio Paint / Procreate Core

---

## 1. EVALUACIÓN Y SELECCIÓN DEL STACK TECNOLÓGICO

Para construir una herramienta de dibujo digital profesional capaz de manejar lienzos a **300-600 DPI (formatos A4, B5, 4K, 8K)** con más de 50 capas a **60-120 FPS sostenidos**, la elección del stack debe equilibrar:
1. **Latencia de Entrada (Input-to-Photon):** Debe ser estrictamente menor a **16.6 ms** (meta: < 8 ms para displays de 120 Hz). Cualquier recolector de basura (GC pause) o capa intermedia de serialización destruye la experiencia del ilustrador.
2. **Eficiencia en VRAM y Ancho de Banda de Memoria:** Un lienzo A4 a 600 DPI en color de 32 bits (RGBA8) pesa ~139 MB por capa. Con 30 capas = 4.17 GB de memoria cruda. Se requiere acceso de bajo nivel a asignadores de memoria virtual y buffers de GPU (Sparse Virtual Texturing / Tiling).
3. **Multiplataforma Nativa sin Compromiso de Entrada:** Soporte de Windows Ink / Wintab, macOS Cocoa `NSEvent` (Apple Pencil en Sidecar / tabletas Wacom/Huion), y Linux Wayland `wl_tablet`.

### Matriz Comparativa de Arquitectura

| Criterio | C++20 + Vulkan / Metal / DX12 | Rust + WGPU + winit | C# / .NET 8 + SkiaSharp / Direct2D | C++ / Qt6 + QPainter / QOpenGLWidget |
| :--- | :--- | :--- | :--- | :--- |
| **Control de Memoria & Cero-GC** | Absoluto (Manual / Custom Allocators) | Absoluto (RAII / Sin GC / Memory Safety) | No (Pausas de GC en trazos intensos) | Parcial (QObject overhead, heap fragmentado) |
| **Latencia Input-to-Photon** | **Óptima (< 5 ms)** | **Óptima (< 5 ms)** | Media (~12-25 ms) | Media-Baja (~10-18 ms) |
| **Abstracción Multi-GPU** | Muy costosa (escribir 3 backends) | **Nativa y Unificada (WGPU: Vulkan/Metal/DX12/WebGPU)** | Limitada (Direct2D es solo Windows; Skia abstrae pero no expone compute shaders modernos) | Regular (QRhi existe pero es rígido) |
| **Seguridad de Memoria en Concurrencia** | Riesgo de Data Races y Buffer Overflows | **Garantía en Tiempo de Compilación (Sin Data Races)** | Segura pero con penalización de runtime | Riesgo de Data Races |
| **Desarrollo de GUI Profesional** | Complejo (requiere Dear ImGui / Slint) | Alto rendimiento (egui / Slint / Iced) | Rápido (Avalonia / WPF) | Muy Rápido (Qt Widgets / QML) |

### Veredicto del Arquitecto

* **Arquitectura de Producción Recomendada:** **Rust + WGPU (con runtime de C++ para interoperabilidad con librerías legadas como PSD SDK / libspiro).**
  * **Motor Gráfico y Núcleo de Entrada:** Rust con `wgpu-native` o C++20 moderno. `wgpu` compila directamente a **DirectX 12 en Windows**, **Metal en macOS**, y **Vulkan en Linux**, permitiendo Compute Shaders modernos (WGSL) con cero sobrecosto de abstracción.
  * **Sub-capa de Entrada:** Rust `winit` + bindings nativos directos a `WM_POINTER` en Windows, `NSEvent` en macOS y `wayland-client` / `libinput` en Linux.
  * **Capa de Interfaz de Usuario (UI):** Slint o Dear ImGui desacoplado del hilo de renderizado del lienzo (renderizado asíncrono con triple buffer para que la UI nunca bloquee el lápiz).

---

## 2. PIPELINE DE RENDERIZADO ACELERADO POR GPU

### 2.1 Virtual Canvas & Sparse Tiled Backing Store
En lugar de asignar una textura monolítica de $4960 \times 7016$ píxeles por cada capa (lo que saturaría rápidamente los 6-8 GB de VRAM de GPUs de gama media), el motor implementa un **Sparse Virtual Texture Tile System**:
- El lienzo se subdivide en una cuadrícula lógica de mosaicos (*tiles*) de **$256 \times 256$ píxeles** (o $512 \times 512$).
- Cada capa solo aloja en memoria los mosaicos que contienen píxeles no vacíos (Sparse Matrix / HashMap de `TileCoord(x,y) -> TileData`).
- **Gestión de Memoria en 2 Niveles:**
  1. **VRAM Tile Atlas (LRU Cache):** Un Texture Array de GPU (ej. $4096 \times 4096$ compuesto por 256 ranuras de mosaicos de $256 \times 256$). Solo los mosaicos visibles en el frustum del viewport se cargan en la VRAM activa.
  2. **RAM Backing Store:** Mosaicos inactivos comprimidos mediante **LZ4** en la memoria principal del sistema.

### 2.2 Transformación Matricial No Destructiva
La cámara del lienzo utiliza una matriz ortográfica de proyección afín 2D calculada por:
$$\mathbf{M}_{\text{viewport}} = \mathbf{T}(\text{Pan}_x, \text{Pan}_y) \times \mathbf{R}(\theta) \times \mathbf{S}(\text{Zoom}, \text{Zoom})$$

* **Zoom:** Desde $0.01\times$ (1%) hasta $64.0\times$ (6400%). El pivote de escala se calcula preservando la coordenada en espacio del lienzo bajo el cursor:
  $$\mathbf{P}_{\text{canvas}} = \mathbf{M}_{\text{viejo}}^{-1} \cdot \mathbf{P}_{\text{screen}} \implies \text{Pan}_{\text{nuevo}} = \mathbf{P}_{\text{screen}} - \mathbf{R}(\theta)\mathbf{S}(\text{Zoom}_{\text{nuevo}})\mathbf{P}_{\text{canvas}}$$
* **Rotación Continua:** 0° a 360° sin ningún tipo de remuestreo destructivo sobre los píxeles del lienzo; la rotación ocurre únicamente en el Vertex Shader del quad de presentación de la pantalla.
* **Filtrado Dinámico:**
  - Cuando $\text{Zoom} < 1.0$: Interpolación Bilineal / Trilineal Mipmapped para evitar aliasing y moiré.
  - Cuando $\text{Zoom} \ge 2.0$: Filtrado Nearest Neighbor (con retícula de píxeles opcional) para permitir dibujo de precisión pixel-art.

---

## 3. SUBSISTEMA DE ENTRADA Y HARDWARE DE LÁPIZ (LOW-LATENCY INPUT)

### 3.1 Integración Nativa por Sistema Operativo
1. **Windows:**
   - Se utiliza la API moderna **`WM_POINTER` (`WM_POINTERDOWN`, `WM_POINTERUPDATE`, `WM_POINTERUP`)** con `GetPointerPenInfo` y `GetPointerPenInfoHistory`.
   - **Por qué NO Win32 WM_MOUSE / GetCursorPos:** El ratón clásico procesa a 125 Hz y quantiza la presión. `WM_POINTER` lee directamente a la frecuencia nativa del digitalizador (200 Hz a 500 Hz en tabletas Wacom Intuos / Cintiq / XP-Pen) y recupera los eventos coalescentes (sub-frame points) que el sistema operativo agrupa durante picos de carga.
   - Presión: Soporta de **2048 a 8192 niveles** lineales (`penInfo.pressure`), inclinación X/Y (`tiltX`, `tiltY` de $-90^\circ$ a $+90^\circ$), rotación del cilindro y botones de lápiz (barrel button y borrador invertido).
   - Fallback a `wintab32.dll` configurable para tabletas antiguas de la era Windows 7.
2. **macOS:**
   - Eventos nativos `NSEvent` de tipo `NSEventTypeTabletPoint` y `NSEventTypeTabletProximity`.
   - Extracción de `pressure`, `tilt` (vector 2D), `rotation` y soporte para Sidecar con Apple Pencil (lectura de doble toque y azimuth).
3. **Linux:**
   - Protocolo Wayland `wl_tablet_manager_v2` con interfaces `wl_tablet_tool_v2` / `zwp_tablet_tool_v2_pressure`.
   - Fallback X11: XInput 2.2 (`XI_Motion` con valuadores de presión e inclinación).

### 3.2 Cola de Estabilización y Suavizado Matemático
Para eliminar el temblor natural de la mano del ilustrador sin introducir latencia perceptible, el pipeline implementa dos etapas:

1. **Interpolación Spline Centripetal Catmull-Rom a Bézier Cúbico:**
   - Dada una secuencia de puntos de entrada $P_{i-1}, P_i, P_{i+1}, P_{i+2}$, se construye una curva continua $C^1$ paramétrica. A diferencia del Bézier tradicional que requiere puntos de control externos, Catmull-Rom pasa obligatoriamente por los puntos reales capturados.
   - Parámetro de nudo centripetal $\alpha = 0.5$: previene la aparición de auto-intersecciones y bucles en trazos rápidos y cerrados.
2. **Estabilizador Dinámico de Cuerda Elástica (Lazy Brush / Spring Stabilizer):**
   $$\mathbf{P}_{\text{brush}}(t) = \mathbf{P}_{\text{brush}}(t-1) + \left( \mathbf{P}_{\text{pen}}(t) - \mathbf{P}_{\text{brush}}(t-1) \right) \cdot \lambda(v, \text{strength})$$
   Donde el factor de amortiguamiento $\lambda$ se adapta en función de la velocidad del trazo $v$ para permitir esquinas afiladas si el usuario desacelera voluntariamente.

---

## 4. MOTOR DE PINCELES PARAMÉTRICO Y COMPUTE SHADERS

El trazado no se dibuja trazando líneas de vectores simples, sino estampando **micro-sellos (dabs)** a lo largo de la curva spline equidistante:

1. **Espaciado Adaptativo (Dab Spacing):**
   El paso entre sellos consecutivos se calcula como:
   $$\Delta s = 2 \cdot R(p) \cdot \text{Spacing}$$
   Donde $\text{Spacing}$ varía típicamente entre $0.05$ (5% para sombreado aerógrafo ultra suave) y $0.20$ (20% para plumillas estilo manga G-Pen).
2. **Compute Shader de Estampado de Pincel (WGSL / HLSL):**
   - Cada grupo de 64 hilos en la GPU evalúa el dab sobre el mosaico de destino:
   $$\text{Alpha}(r) = \text{Flow} \cdot \left(1.0 - \text{smoothstep}(\text{Hardness} \cdot R, R, r)\right)$$
   - Soporte para texturas de cerdas (Brush Tip Texture) y textura de grano de papel (Dual Texture).
3. **Mezcla de Color en Tiempo Real (Wet-on-Wet Paint Blending):**
   - El pincel lee el color existente en el lienzo antes de depositar el nuevo color:
   $$\mathbf{C}_{\text{mezcla}} = (1 - \text{ColorMix}) \cdot \mathbf{C}_{\text{pincel}} + \text{ColorMix} \cdot \mathbf{C}_{\text{lienzo}}$$
   Permitiendo difuminado al estilo óleo o acuarela similar al motor de pinceles de Clip Studio Paint.

---

## 5. GESTIÓN DE CAPAS, COMPOSICIÓN Y MODOS DE FUSIÓN

### 5.1 Árbol de Composición (DAG)
El gestor de capas mantiene un árbol jerárquico que admite:
- **Carpetas con modo 'Pass-Through' o 'Aislado':** El modo aislado renderiza la carpeta en un Framebuffer intermedio antes de mezclarla con el fondo.
- **Máscaras de Recorte (Clipping Masks):** La capa recortada utiliza el canal alfa de la capa inmediatamente inferior como máscara:
  $$A_{\text{efectivo}}(x, y) = A_{\text{capa}}(x, y) \cdot A_{\text{base}}(x, y)$$
- **Bloqueo Alfa (Alpha Lock):** Bloquea la escritura en el canal alfa en el fragment shader ($A_{\text{salida}} = A_{\text{previo}}$).

### 5.2 Ecuaciones de Modos de Fusión en GPU (Fragment Shader)
Donde $B$ es el color base (lienzo), $S$ es el color fuente (capa superior):
* **Multiplicar (Multiply):** $C_R = B \times S$
* **Pantalla (Screen):** $C_R = 1 - (1 - B) \times (1 - S)$
* **Superponer (Overlay):**
  $$C_R = \begin{cases} 2 \cdot B \cdot S & \text{si } B < 0.5 \\ 1 - 2 \cdot (1 - B) \cdot (1 - S) & \text{si } B \ge 0.5 \end{cases}$$
* **Luz Suave (Soft Light):** Fórmula de Pegtop para gradientes continuos sin bandas.
* **Esquivar / Quemar Color (Color Dodge / Color Burn).**

---

## 6. HISTORIAL NO DESTRUCTIVO (UNDO/REDO POR MOSAICOS) Y TIMELAPSE

1. **Undo/Redo Basado en Mosaicos Sucios (Tile-Diff Journal):**
   - En lugar de clonar toda la capa (lo que consumiría cientos de megabytes por trazo), el motor solo registra los identificadores de los mosaicos `(tx, ty)` modificados por el trazo.
   - Los datos previos se respaldan en un hilo en segundo plano (Worker Thread) comprimidos con **LZ4**, reduciendo el impacto en RAM a menos de **500 KB por trazo**.
2. **Grabación de Timelapse:**
   - **Enfoque Híbrido:** Registro del flujo de eventos de trazo vectoriales deterministas (puntos con presión, sellos, selecciones de color) serializados en un archivo `.aurasession`.
   - Permite tanto la **reproducción paso a paso a cualquier resolución** (re-renderizado vectorial estilo ibis Paint X) como la exportación acelerada por hardware a video **MP4 (H.264 / HEVC)** usando NVENC / MediaFoundation en Windows o VideoToolbox en macOS.
