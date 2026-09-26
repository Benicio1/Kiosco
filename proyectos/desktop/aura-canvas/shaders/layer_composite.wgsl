// =============================================================================
// AURAPAINT GPU PIPELINE: WGSL LAYER COMPOSITE & BLEND MODES FRAGMENT SHADER
// Modos de fusión de capas acelerados por GPU idénticos a Clip Studio / Photoshop
// =============================================================================

struct LayerUniforms {
    opacity: f32,          // Opacidad de capa [0.0, 1.0]
    blend_mode: u32,       // 0: Normal, 1: Multiply, 2: Screen, 3: Overlay, 4: Soft Light, 5: Color Dodge
    is_clipping_mask: u32, // 1 si esta capa está recortada por la capa inferior
    has_mask: u32,         // 1 si existe máscara de capa en escala de grises
};

@group(0) @binding(0) var<uniform> layer: LayerUniforms;
@group(0) @binding(1) var base_texture: texture_2d<f32>;        // Lienzo acumulado inferior
@group(0) @binding(2) var source_texture: texture_2d<f32>;      // Capa actual a fusionar
@group(0) @binding(3) var clipping_base_tex: texture_2d<f32>;   // Textura de la capa base de recorte
@group(0) @binding(4) var tex_sampler: sampler;

struct VertexOutput {
    @builtin(position) position: vec4<f32>,
    @location(0) uv: vec2<f32>,
};

// --- ECUACIONES MATEMÁTICAS DE MODOS DE FUSIÓN (Photoshop / Clip Studio Specs) ---

fn blend_multiply(base: vec3<f32>, src: vec3<f32>) -> vec3<f32> {
    return base * src;
}

fn blend_screen(base: vec3<f32>, src: vec3<f32>) -> vec3<f32> {
    return 1.0 - (1.0 - base) * (1.0 - src);
}

fn blend_overlay_channel(b: f32, s: f32) -> f32 {
    if (b < 0.5) {
        return 2.0 * b * s;
    } else {
        return 1.0 - 2.0 * (1.0 - b) * (1.0 - s);
    }
}

fn blend_overlay(base: vec3<f32>, src: vec3<f32>) -> vec3<f32> {
    return vec3<f32>(
        blend_overlay_channel(base.r, src.r),
        blend_overlay_channel(base.g, src.g),
        blend_overlay_channel(base.b, src.b)
    );
}

fn blend_soft_light_channel(b: f32, s: f32) -> f32 {
    // Ecuación suave Pegtop
    return (1.0 - 2.0 * s) * b * b + 2.0 * s * b;
}

fn blend_soft_light(base: vec3<f32>, src: vec3<f32>) -> vec3<f32> {
    return vec3<f32>(
        blend_soft_light_channel(base.r, src.r),
        blend_soft_light_channel(base.g, src.g),
        blend_soft_light_channel(base.b, src.b)
    );
}

fn blend_color_dodge(base: vec3<f32>, src: vec3<f32>) -> vec3<f32> {
    let denom = max(vec3<f32>(0.001), 1.0 - src);
    return min(vec3<f32>(1.0), base / denom);
}

@fragment
fn fs_composite_layer(in: VertexOutput) -> @location(0) vec4<f32> {
    let base_col = textureSample(base_texture, tex_sampler, in.uv);
    var src_col = textureSample(source_texture, tex_sampler, in.uv);

    // Si la capa está marcada como Máscara de Recorte (Clipping Mask):
    if (layer.is_clipping_mask == 1u) {
        let clip_base_col = textureSample(clipping_base_tex, tex_sampler, in.uv);
        // Modula el alfa de la capa actual por el canal alfa de la base de recorte
        src_col.a = src_col.a * clip_base_col.a;
    }

    // Aplicar opacidad general de la capa
    let effective_src_alpha = src_col.a * layer.opacity;

    if (effective_src_alpha <= 0.0001) {
        return base_col;
    }

    // Evaluar color mezclado según el modo de fusión seleccionado
    var blended_rgb: vec3<f32>;
    switch (layer.blend_mode) {
        case 1u: { // Multiply
            blended_rgb = blend_multiply(base_col.rgb, src_col.rgb);
        }
        case 2u: { // Screen
            blended_rgb = blend_screen(base_col.rgb, src_col.rgb);
        }
        case 3u: { // Overlay
            blended_rgb = blend_overlay(base_col.rgb, src_col.rgb);
        }
        case 4u: { // Soft Light
            blended_rgb = blend_soft_light(base_col.rgb, src_col.rgb);
        }
        case 5u: { // Color Dodge
            blended_rgb = blend_color_dodge(base_col.rgb, src_col.rgb);
        }
        default: { // Normal
            blended_rgb = src_col.rgb;
        }
    }

    // Composición final de píxel sobre el fondo existente
    let out_a = effective_src_alpha + base_col.a * (1.0 - effective_src_alpha);
    var final_rgb: vec3<f32>;
    if (out_a > 0.0001) {
        final_rgb = (blended_rgb * effective_src_alpha + base_col.rgb * base_col.a * (1.0 - effective_src_alpha)) / out_a;
    } else {
        final_rgb = vec3<f32>(0.0);
    }

    return vec4<f32>(final_rgb, out_a);
}
