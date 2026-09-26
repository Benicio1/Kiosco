// =============================================================================
// AURAPAINT GPU PIPELINE: WGSL BRUSH DAB STAMPING COMPUTE SHADER
// Estampado masivo de sellos de pincel acelerado por GPU (DirectX 12 / Metal / Vulkan)
// =============================================================================

struct DabUniforms {
    canvas_pos: vec2<f32>,     // Coordenada (X, Y) central del sello en espacio de lienzo
    tile_offset: vec2<f32>,    // Coordenada de origen del mosaico (tile_x * 256, tile_y * 256)
    radius: f32,               // Radio dinámico modulado por la curva de presión
    flow: f32,                 // Flujo / Opacidad por sello
    hardness: f32,             // Dureza del borde (0.0 = aerógrafo difuso, 1.0 = borde cortante)
    color: vec4<f32>,          // Color RGBA del pincel
    alpha_lock: u32,           // 1 = Bloquear canal alfa, 0 = Escritura normal
    blend_mode: u32,           // Modo de mezcla del pincel
};

@group(0) @binding(0) var<uniform> dab: DabUniforms;
@group(0) @binding(1) var target_tile: texture_storage_2d<rgba8unorm, read_write>;

@compute @workgroup_size(16, 16)
fn cs_stamp_dab(@builtin(global_invocation_id) id: vec3<u32>) {
    let tile_dims = textureDimensions(target_tile);
    if (id.x >= tile_dims.x || id.y >= tile_dims.y) {
        return;
    }

    // Coordenada absoluta del píxel dentro del lienzo virtual
    let pixel_canvas_pos = dab.tile_offset + vec2<f32>(f32(id.x), f32(id.y));
    let dist = distance(pixel_canvas_pos, dab.canvas_pos);

    if (dist > dab.radius) {
        return; // Fuera del radio de influencia del sello
    }

    // Perfil de caída radial (Falloff) con suavizado anti-aliasing
    let normalized_dist = dist / dab.radius;
    let inner_edge = dab.hardness;
    let alpha_falloff = 1.0 - smoothstep(inner_edge, 1.0, normalized_dist);
    let dab_alpha = dab.color.a * dab.flow * alpha_falloff;

    if (dab_alpha <= 0.001) {
        return;
    }

    // Leer píxel previo en el mosaico
    let prev_color = textureLoad(target_tile, vec2<i32>(i32(id.x), i32(id.y)));

    var out_color: vec4<f32>;

    if (dab.alpha_lock == 1u) {
        // Bloqueo Alfa Activo: Solo modifica RGB donde ya existe contenido (A > 0)
        let blended_rgb = mix(prev_color.rgb, dab.color.rgb, dab_alpha);
        out_color = vec4<f32>(blended_rgb, prev_color.a);
    } else {
        // Composición estándar Porter-Duff Over (Premultiplied Alpha)
        let out_a = dab_alpha + prev_color.a * (1.0 - dab_alpha);
        var out_rgb: vec3<f32>;
        if (out_a > 0.0001) {
            out_rgb = (dab.color.rgb * dab_alpha + prev_color.rgb * prev_color.a * (1.0 - dab_alpha)) / out_a;
        } else {
            out_rgb = vec3<f32>(0.0);
        }
        out_color = vec4<f32>(out_rgb, out_a);
    }

    // Escribir píxel resultante en el Texture Storage de GPU
    textureStore(target_tile, vec2<i32>(i32(id.x), i32(id.y)), out_color);
}
