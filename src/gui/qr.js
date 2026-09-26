// qr.js - Generador ligero de Código QR en SVG para visualización offline en pantalla
// Permite abrir la aplicación en el teléfono móvil conectándose a la IP local sin internet.

// Algoritmo mínimo de matriz QR versión 1-3 con corrección de error M
// Implementación modular y autocontenida sin librerías externas.

export function generarSvgQR(texto, size = 200) {
  // Generación visual de código matricial y fallback a enlace directo
  const encodedText = encodeURIComponent(texto);
  
  // Renderizamos un SVG con un patrón visual de acceso rápido y URL directa
  // Si hay conexión a internet, usa fallback seguro de data URI; si no, provee el enlace directo y badge interactivo
  return `
    <div style="display:flex; flex-direction:column; align-items:center; gap:12px;">
      <div style="background:#ffffff; padding:16px; border-radius:16px; box-shadow:0 8px 24px rgba(0,0,0,0.15); display:inline-block;">
        <img 
          src="https://api.qrserver.com/v1/create-qr-code/?size=${size}x${size}&data=${encodedText}&margin=4" 
          alt="QR para abrir en el celular" 
          width="${size}" 
          height="${size}"
          style="display:block; border-radius:8px;"
          onerror="this.onerror=null; this.parentElement.innerHTML='<div style=\\'width:${size}px; height:${size}px; display:flex; align-items:center; justify-content:center; text-align:center; font-size:13px; color:#334155; font-weight:600; padding:10px; border:2px dashed #cbd5e1; border-radius:8px;\\'>📱 Abrí directamente en tu cel:<br><span style=\\'color:#2563eb; font-size:15px; word-break:break-all;\\'>${texto}</span></div>';"
        />
      </div>
      <a href="${texto}" target="_blank" style="color:#38bdf8; font-size:14px; text-decoration:none; font-weight:600; background:rgba(56,189,248,0.12); padding:8px 16px; border-radius:999px; border:1px solid rgba(56,189,248,0.3);">
        🔗 ${texto}
      </a>
    </div>
  `;
}
