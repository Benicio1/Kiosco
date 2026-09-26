#pragma once
// =============================================================================
// AURAPAINT CORE: ARQUITECTURA DE CÓDIGO BASE MODULAR DE PRODUCCIÓN (C++20)
// Módulos: Input Handler, Spline Stabilizer, Tiled Layer Engine, Undo Journal
// =============================================================================

#include <vector>
#include <unordered_map>
#include <memory>
#include <cmath>
#include <chrono>
#include <cstdint>
#include <string>
#include <functional>

namespace AuraCore {

    // =========================================================================
    // 1. ESTRUCTURAS DE ENTRADA DE ALTA PRECISIÓN (SUB-FRAME STYLUS PACKETS)
    // =========================================================================
    enum class DeviceType { Mouse, StylusWindowsInk, StylusWintab, ApplePencil, WaylandTablet };

    struct StylusPacket {
        float x;                 // Coordenada X absoluta en píxeles del dispositivo
        float y;                 // Coordenada Y absoluta en píxeles del dispositivo
        float pressure;          // Normalizado [0.0f, 1.0f] a partir de 2048/8192 niveles
        float tiltX;             // Inclinación azimutal [-90.0f, +90.0f]
        float tiltY;             // Inclinación cenital [-90.0f, +90.0f]
        float rotation;          // Rotación del cilindro (barrel roll) [0.0f, 360.0f]
        uint32_t buttons;        // Máscara de botones (Barril 1, Barril 2, Borrador)
        uint64_t timestampNs;    // Marca de tiempo de hardware de precisión de nanosegundos
    };

    // =========================================================================
    // 2. FILTRO DE ESTABILIZACIÓN Y SUAVIZADO CATMULL-ROM CENTRÍPETO
    // =========================================================================
    class StrokeStabilizer {
    public:
        float stabilizerStrength = 0.5f; // Factor elástico (0.0 = crudo, 1.0 = lazy brush fuerte)
        float brushBaseRadius = 12.0f;
        float dabSpacing = 0.10f;        // 10% del diámetro

        void BeginStroke(const StylusPacket& start) {
            m_history.clear();
            m_history.push_back(start);
            m_lazyPos = { start.x, start.y };
            m_carryDistance = 0.0f;
        }

        std::vector<StylusPacket> FeedSample(const StylusPacket& sample) {
            // 1. Amortiguamiento elástico de cuerda (Spring Dampener)
            float factor = 1.0f - (stabilizerStrength * 0.85f);
            m_lazyPos.x += (sample.x - m_lazyPos.x) * factor;
            m_lazyPos.y += (sample.y - m_lazyPos.y) * factor;

            StylusPacket smoothed = sample;
            smoothed.x = m_lazyPos.x;
            smoothed.y = m_lazyPos.y;

            m_history.push_back(smoothed);
            std::vector<StylusPacket> generatedDabs;

            if (m_history.size() < 4) return generatedDabs;

            // Catmull-Rom centripetal spline sobre los últimos 4 nudos: P0, P1, P2, P3
            size_t idx = m_history.size() - 2;
            const auto& p0 = m_history[idx - 1];
            const auto& p1 = m_history[idx];
            const auto& p2 = m_history[idx + 1];
            const auto& p3 = (idx + 2 < m_history.size()) ? m_history[idx + 2] : p2;

            float segLength = std::hypot(p2.x - p1.x, p2.y - p1.y);
            float effectiveRadius = std::max(1.0f, brushBaseRadius * std::pow(p1.pressure, 1.2f));
            float stepDistance = std::max(1.0f, effectiveRadius * 2.0f * dabSpacing);

            float d = m_carryDistance;
            while (d < segLength) {
                float t = d / std::max(0.001f, segLength);
                generatedDabs.push_back(EvaluateSpline(p0, p1, p2, p3, t));
                d += stepDistance;
            }
            m_carryDistance = d - segLength;

            return generatedDabs;
        }

    private:
        std::vector<StylusPacket> m_history;
        struct { float x, y; } m_lazyPos;
        float m_carryDistance = 0.0f;

        StylusPacket EvaluateSpline(const StylusPacket& p0, const StylusPacket& p1, 
                                   const StylusPacket& p2, const StylusPacket& p3, float t) {
            float t2 = t * t;
            float t3 = t2 * t;

            StylusPacket out;
            out.x = 0.5f * ((2.f*p1.x) + (-p0.x + p2.x)*t + (2.f*p0.x - 5.f*p1.x + 4.f*p2.x - p3.x)*t2 + (-p0.x + 3.f*p1.x - 3.f*p2.x + p3.x)*t3);
            out.y = 0.5f * ((2.f*p1.y) + (-p0.y + p2.y)*t + (2.f*p0.y - 5.f*p1.y + 4.f*p2.y - p3.y)*t2 + (-p0.y + 3.f*p1.y - 3.f*p2.y + p3.y)*t3);
            out.pressure = p1.pressure + (p2.pressure - p1.pressure) * t;
            out.tiltX = p1.tiltX + (p2.tiltX - p1.tiltX) * t;
            out.tiltY = p1.tiltY + (p2.tiltY - p1.tiltY) * t;
            out.rotation = p1.rotation;
            out.buttons = p1.buttons;
            out.timestampNs = p1.timestampNs;
            return out;
        }
    };

    // =========================================================================
    // 3. GESTIÓN DE MEMORIA POR MOSAICOS DISPERSOS (SPARSE VIRTUAL TILES)
    // =========================================================================
    constexpr int TILE_SIZE = 256;
    constexpr size_t TILE_BYTE_SIZE = TILE_SIZE * TILE_SIZE * 4; // 256 KB por mosaico RGBA8

    struct TileKey {
        int32_t x;
        int32_t y;

        bool operator==(const TileKey& other) const { return x == other.x && y == other.y; }
    };

    struct TileKeyHash {
        std::size_t operator()(const TileKey& k) const {
            return (static_cast<uint64_t>(k.x) << 32) ^ static_cast<uint32_t>(k.y);
        }
    };

    class MemoryTile {
    public:
        TileKey key;
        std::vector<uint8_t> pixels; // RGBA 32-bit o FP16
        bool isGpuSynchronized = false;
        bool isDirty = false;

        MemoryTile(TileKey k) : key(k), pixels(TILE_BYTE_SIZE, 0) {}

        std::unique_ptr<MemoryTile> Clone() const {
            auto copy = std::make_unique<MemoryTile>(key);
            copy->pixels = this->pixels;
            return copy;
        }
    };

    // =========================================================================
    // 4. HISTORIAL DE DESHACER/REHACER BASADO EN MOSAICOS SUCIOS (TILE-DIFF)
    // =========================================================================
    struct TileDiffCommand {
        std::string layerId;
        std::unordered_map<TileKey, std::unique_ptr<MemoryTile>, TileKeyHash> beforeSnapshot;
        std::unordered_map<TileKey, std::unique_ptr<MemoryTile>, TileKeyHash> afterSnapshot;

        void RecordBefore(TileKey k, const MemoryTile& current) {
            if (beforeSnapshot.find(k) == beforeSnapshot.end()) {
                beforeSnapshot[k] = current.Clone();
            }
        }

        void RecordAfter(TileKey k, const MemoryTile& current) {
            afterSnapshot[k] = current.Clone();
        }
    };

    class UndoJournal {
    public:
        void PushStrokeAction(TileDiffCommand command) {
            m_undoStack.push_back(std::move(command));
            m_redoStack.clear();
        }

        bool CanUndo() const { return !m_undoStack.empty(); }
        bool CanRedo() const { return !m_redoStack.empty(); }

        void Undo(std::function<void(const std::string&, const TileKey&, const MemoryTile&)> restoreCallback) {
            if (m_undoStack.empty()) return;
            auto cmd = std::move(m_undoStack.back());
            m_undoStack.pop_back();

            for (const auto& [key, tile] : cmd.beforeSnapshot) {
                restoreCallback(cmd.layerId, key, *tile);
            }
            m_redoStack.push_back(std::move(cmd));
        }

        void Redo(std::function<void(const std::string&, const TileKey&, const MemoryTile&)> restoreCallback) {
            if (m_redoStack.empty()) return;
            auto cmd = std::move(m_redoStack.back());
            m_redoStack.pop_back();

            for (const auto& [key, tile] : cmd.afterSnapshot) {
                restoreCallback(cmd.layerId, key, *tile);
            }
            m_undoStack.push_back(std::move(cmd));
        }

    private:
        std::vector<TileDiffCommand> m_undoStack;
        std::vector<TileDiffCommand> m_redoStack;
    };
}
