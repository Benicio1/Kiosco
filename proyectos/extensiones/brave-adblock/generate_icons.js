const fs = require('fs');
const path = require('path');
const zlib = require('zlib');

// Minimal PNG encoder without external dependencies
function createPNG(width, height, getPixel) {
  // Buffer for raw image data: (width * 4 + 1) * height
  // 1 filter byte (0 = None) per scanline + RGBA pixels
  const rowStride = 1 + width * 4;
  const rawData = Buffer.alloc(rowStride * height);

  for (let y = 0; y < height; y++) {
    const rowOffset = y * rowStride;
    rawData[rowOffset] = 0; // Filter type None
    for (let x = 0; x < width; x++) {
      const [r, g, b, a] = getPixel(x, y, width, height);
      const pixelOffset = rowOffset + 1 + x * 4;
      rawData[pixelOffset] = r;
      rawData[pixelOffset + 1] = g;
      rawData[pixelOffset + 2] = b;
      rawData[pixelOffset + 3] = a;
    }
  }

  const compressed = zlib.deflateSync(rawData);

  // PNG Signature
  const signature = Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]);

  // IHDR chunk
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(width, 0);
  ihdr.writeUInt32BE(height, 4);
  ihdr[8] = 8; // Bit depth: 8
  ihdr[9] = 6; // Color type: RGBA (6)
  ihdr[10] = 0; // Compression method
  ihdr[11] = 0; // Filter method
  ihdr[12] = 0; // Interlace method
  const ihdrChunk = makeChunk('IHDR', ihdr);

  // IDAT chunk
  const idatChunk = makeChunk('IDAT', compressed);

  // IEND chunk
  const iendChunk = makeChunk('IEND', Buffer.alloc(0));

  return Buffer.concat([signature, ihdrChunk, idatChunk, iendChunk]);
}

function makeChunk(type, data) {
  const length = data.length;
  const buffer = Buffer.alloc(8 + length + 4);
  buffer.writeUInt32BE(length, 0);
  buffer.write(type, 4, 4, 'ascii');
  data.copy(buffer, 8);

  const crc = crc32(buffer.subarray(4, 8 + length));
  buffer.writeUInt32BE(crc, 8 + length);
  return buffer;
}

// CRC32 implementation
const crcTable = new Uint32Array(256);
for (let n = 0; n < 256; n++) {
  let c = n;
  for (let k = 0; k < 8; k++) {
    c = (c & 1) ? (0xEDB88320 ^ (c >>> 1)) : (c >>> 1);
  }
  crcTable[n] = c >>> 0;
}

function crc32(buf) {
  let crc = 0xFFFFFFFF;
  for (let i = 0; i < buf.length; i++) {
    crc = crcTable[(crc ^ buf[i]) & 0xFF] ^ (crc >>> 8);
  }
  return (crc ^ 0xFFFFFFFF) >>> 0;
}

// Draw a modern Shield icon with Lion/Brave orange or Indigo/Violet gradient
function shieldPixel(x, y, w, h) {
  // Normalize coordinates (-1 to 1)
  const nx = (x / (w - 1)) * 2 - 1;
  const ny = (y / (h - 1)) * 2 - 1; // -1 top, 1 bottom

  // Outer shield shape equation
  // Top horizontal part, then curves down to a point at bottom
  let inShield = false;
  if (ny >= -0.75 && ny <= 0.85) {
    let maxWidth = 0.8;
    if (ny > 0) {
      maxWidth = 0.8 * Math.sqrt(Math.max(0, 1 - (ny / 0.9) ** 2));
    }
    if (Math.abs(nx) <= maxWidth) {
      inShield = true;
    }
  }

  if (!inShield) {
    return [0, 0, 0, 0]; // Transparent
  }

  // Inner border or fill
  // Gradient from Brave Orange (#FF5500) to Crimson/Purple (#990033)
  const t = (ny + 0.75) / 1.6;
  let r = Math.round(255 * (1 - t * 0.4));
  let g = Math.round(85 * (1 - t * 0.8));
  let b = Math.round(20 + 80 * t);

  // Draw an inner "minus" or "slash" (adblock icon) or shield core
  const distCenter = Math.sqrt(nx * nx + (ny + 0.05) * (ny + 0.05));
  if (distCenter < 0.35 && Math.abs(ny + 0.05) < 0.12) {
    // White block bar
    return [255, 255, 255, 255];
  }

  return [r, g, b, 255];
}

const sizes = [16, 48, 128];
const iconsDir = path.join(__dirname, 'icons');

for (const size of sizes) {
  const png = createPNG(size, size, shieldPixel);
  fs.writeFileSync(path.join(iconsDir, `icon${size}.png`), png);
  console.log(`Generated icon${size}.png (${size}x${size})`);
}
