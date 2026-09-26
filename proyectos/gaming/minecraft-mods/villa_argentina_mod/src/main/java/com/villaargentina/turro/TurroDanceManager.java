package com.villaargentina.turro;

import com.villaargentina.VillaArgentinaMod;
import com.villaargentina.init.ModItems;
import com.villaargentina.init.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.VanillaGameEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = VillaArgentinaMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TurroDanceManager {

    // Servidor: Almacena coordenadas de tocadiscos activos con cumbia y su tiempo de expiración en ticks
    private static final Map<BlockPos, Long> SERVER_CUMBIA_JUKEBOXES = new ConcurrentHashMap<>();

    // Cliente: Almacena coordenadas y tiempo de expiración en ms
    private static final Map<BlockPos, Long> CLIENT_CUMBIA_JUKEBOXES = new ConcurrentHashMap<>();

    private static final Set<ResourceLocation> CUMBIA_SOUND_IDS = Set.of(
            new ResourceLocation(VillaArgentinaMod.MODID, "cumbia_villera"),
            new ResourceLocation(VillaArgentinaMod.MODID, "tamo_chelo"),
            new ResourceLocation(VillaArgentinaMod.MODID, "lgante_rkt"),
            new ResourceLocation(VillaArgentinaMod.MODID, "perrito_malvado")
    );

    public static boolean isCumbiaDisc(net.minecraft.world.item.ItemStack stack) {
        return stack.is(ModItems.DISCO_CUMBIA.get()) ||
               stack.is(ModItems.DISCO_TAMO_CHELO.get()) ||
               stack.is(ModItems.DISCO_LGANTE_RKT.get()) ||
               stack.is(ModItems.DISCO_PERRITO_MALVADO.get());
    }

    public static long getDiscDurationTicks(net.minecraft.world.item.ItemStack stack) {
        if (stack.is(ModItems.DISCO_TAMO_CHELO.get())) return 2220L;
        if (stack.is(ModItems.DISCO_LGANTE_RKT.get())) return 3720L;
        if (stack.is(ModItems.DISCO_PERRITO_MALVADO.get())) return 5620L;
        return 3630L;
    }

    public static long getDiscDurationMs(ResourceLocation soundId) {
        String path = soundId.getPath();
        if (path.contains("tamo_chelo")) return 112000L;
        if (path.contains("lgante_rkt")) return 187000L;
        if (path.contains("perrito_malvado")) return 282000L;
        return 182000L;
    }

    /**
     * 1. Detectar eventos de juego vanilla: cuando un tocadiscos comienza o detiene una reproducción
     */
    @SubscribeEvent
    public static void onVanillaGameEvent(VanillaGameEvent event) {
        if (event.getVanillaEvent() == GameEvent.JUKEBOX_PLAY) {
            Vec3 posVec = event.getEventPosition();
            BlockPos pos = BlockPos.containing(posVec.x, posVec.y, posVec.z);
            Level level = event.getLevel();
            if (level.isLoaded(pos)) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof JukeboxBlockEntity jb && isCumbiaDisc(jb.getFirstItem())) {
                    SERVER_CUMBIA_JUKEBOXES.put(pos.immutable(), level.getGameTime() + getDiscDurationTicks(jb.getFirstItem()));
                }
            }
        } else if (event.getVanillaEvent() == GameEvent.JUKEBOX_STOP_PLAY) {
            Vec3 posVec = event.getEventPosition();
            BlockPos pos = BlockPos.containing(posVec.x, posVec.y, posVec.z);
            SERVER_CUMBIA_JUKEBOXES.remove(pos);
        }
    }

    /**
     * 2. Detectar interacción de jugador con tocadiscos
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        if (!level.isLoaded(pos)) return;

        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.JUKEBOX)) {
            if (isCumbiaDisc(event.getItemStack())) {
                long durationTicks = getDiscDurationTicks(event.getItemStack());
                if (level.isClientSide()) {
                    CLIENT_CUMBIA_JUKEBOXES.put(pos.immutable(), System.currentTimeMillis() + (durationTicks * 50L));
                } else {
                    SERVER_CUMBIA_JUKEBOXES.put(pos.immutable(), level.getGameTime() + durationTicks);
                }
            } else if (state.hasProperty(JukeboxBlock.HAS_RECORD) && state.getValue(JukeboxBlock.HAS_RECORD) && event.getItemStack().isEmpty()) {
                if (level.isClientSide()) {
                    CLIENT_CUMBIA_JUKEBOXES.remove(pos);
                } else {
                    SERVER_CUMBIA_JUKEBOXES.remove(pos);
                }
            }
        }
    }

    /**
     * 3. Limpiar tocadiscos si el bloque es destruido
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getState().is(Blocks.JUKEBOX)) {
            SERVER_CUMBIA_JUKEBOXES.remove(event.getPos());
        }
    }

    /**
     * 4. Monitoreo y limpieza periódica en el nivel (cada 20 ticks / 1 segundo)
     */
    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide()) {
            return;
        }

        Level level = event.level;
        long gameTime = level.getGameTime();

        if (gameTime % 20 == 0) {
            // Limpiar tocadiscos finalizados o descargados
            SERVER_CUMBIA_JUKEBOXES.entrySet().removeIf(entry -> {
                BlockPos pos = entry.getKey();
                if (!level.isLoaded(pos)) return true;
                BlockState state = level.getBlockState(pos);
                if (!state.is(Blocks.JUKEBOX) || !state.hasProperty(JukeboxBlock.HAS_RECORD) || !state.getValue(JukeboxBlock.HAS_RECORD)) {
                    return true;
                }
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof JukeboxBlockEntity jb) {
                    return !jb.isRecordPlaying() || !isCumbiaDisc(jb.getFirstItem());
                }
                return entry.getValue() < gameTime;
            });

            // Respaldo robusto: Escaneo en radio de los jugadores por si el tocadiscos se activó antes
            if (SERVER_CUMBIA_JUKEBOXES.isEmpty() && level instanceof ServerLevel serverLevel) {
                for (ServerPlayer player : serverLevel.players()) {
                    BlockPos pPos = player.blockPosition();
                    for (int dx = -14; dx <= 14; dx += 2) {
                        for (int dy = -4; dy <= 4; dy += 2) {
                            for (int dz = -14; dz <= 14; dz += 2) {
                                BlockPos checkPos = pPos.offset(dx, dy, dz);
                                if (level.isLoaded(checkPos) && level.getBlockState(checkPos).is(Blocks.JUKEBOX)) {
                                    BlockState bs = level.getBlockState(checkPos);
                                    if (bs.hasProperty(JukeboxBlock.HAS_RECORD) && bs.getValue(JukeboxBlock.HAS_RECORD)) {
                                        BlockEntity be = level.getBlockEntity(checkPos);
                                        if (be instanceof JukeboxBlockEntity jb && jb.isRecordPlaying() && isCumbiaDisc(jb.getFirstItem())) {
                                            SERVER_CUMBIA_JUKEBOXES.put(checkPos.immutable(), gameTime + getDiscDurationTicks(jb.getFirstItem()));
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 5. Tick del aldeano: Comprueba tocadiscos cercanos y realiza el auténtico Baile Turro
     */
    @SubscribeEvent
    public static void onVillagerTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Villager villager)) {
            return;
        }

        Level level = villager.level();
        BlockPos nearestJukebox = findActiveJukebox(level, villager.blockPosition(), 18);

        if (nearestJukebox != null) {
            hacerBaileTurro(villager, nearestJukebox);
        } else if (villager.getPose() == Pose.CROUCHING) {
            villager.setPose(Pose.STANDING);
        }
    }

    private static BlockPos findActiveJukebox(Level level, BlockPos villagerPos, int radius) {
        double rSq = radius * radius;

        if (level.isClientSide()) {
            long now = System.currentTimeMillis();
            CLIENT_CUMBIA_JUKEBOXES.entrySet().removeIf(entry -> entry.getValue() < now);
            for (BlockPos pos : CLIENT_CUMBIA_JUKEBOXES.keySet()) {
                if (villagerPos.distSqr(pos) <= rSq) {
                    return pos;
                }
            }
        } else {
            for (BlockPos pos : SERVER_CUMBIA_JUKEBOXES.keySet()) {
                if (level.isLoaded(pos) && villagerPos.distSqr(pos) <= rSq) {
                    return pos;
                }
            }
        }

        return null;
    }

    /**
     * Coreografía inspirada en Jorge y Nacho ("Bailan las rochas y las chetas"):
     * 1. Deslizamiento lateral con paso turro y saltito (Shuffle lateral).
     * 2. Los "Pasos Prohibidos": Agachada profunda (Pose.CROUCHING), rebote en cuclillas y cabeceo marcado.
     * 3. Quiebre de perfil (90°) y tirada de corte con brazos.
     * 4. Giro 360° ("Tirar la vueltita") y salto alto festivo con remate.
     */
    private static void hacerBaileTurro(Villager villager, BlockPos jukeboxPos) {
        Level level = villager.level();
        double distSq = villager.distanceToSqr(jukeboxPos.getX() + 0.5, jukeboxPos.getY() + 0.5, jukeboxPos.getZ() + 0.5);

        // Control de pista de baile: congregarse cerca del tocadiscos
        if (!level.isClientSide()) {
            if (distSq > 30.0) {
                if (villager.tickCount % 20 == 0) {
                    villager.getNavigation().moveTo(jukeboxPos.getX() + 0.5, jukeboxPos.getY(), jukeboxPos.getZ() + 0.5, 0.65D);
                }
            } else {
                villager.getNavigation().stop();
            }
        }

        // Calcular orientación base hacia el tocadiscos
        double dx = (jukeboxPos.getX() + 0.5) - villager.getX();
        double dz = (jukeboxPos.getZ() + 0.5) - villager.getZ();
        float baseYaw = (float) (Math.atan2(-dx, dz) * (180.0 / Math.PI));

        // Vectores laterales perpendiculares para los pasos de costado
        double rad = Math.toRadians(baseYaw);
        double rightX = Math.cos(rad);
        double rightZ = Math.sin(rad);

        // Ciclo de frase musical de 192 ticks (~9.6 segundos), dividido en 4 rutinas de 48 ticks (~2.4s)
        long gameTime = level.getGameTime();
        int phraseTick = (int) ((gameTime + (villager.getId() * 11L)) % 192);
        int routine = phraseTick / 48;
        int stepTick = phraseTick % 48;

        // Sentido de espejo entre aldeanos contiguos (como Jorge y Nacho bailando en dúo)
        int mirror = (villager.getId() % 2 == 0) ? 1 : -1;

        switch (routine) {
            case 0:
                // --- RUTINA 1: Shuffle Lateral / Paso de Costado de Jorge y Nacho ---
                villager.setPose(Pose.STANDING);
                villager.getLookControl().setLookAt(jukeboxPos.getX() + 0.5, jukeboxPos.getY() + 1.2, jukeboxPos.getZ() + 0.5, 30.0F, 30.0F);

                int subBeat = stepTick / 12; // 0, 1 = un lado; 2, 3 = otro lado
                int tickInBeat = stepTick % 12;
                int dir = (subBeat < 2 ? 1 : -1) * mirror;

                if (tickInBeat == 0) {
                    // Deslizamiento lateral con saltito
                    if (villager.onGround()) {
                        double speed = 0.17;
                        villager.setDeltaMovement(rightX * dir * speed, 0.24, rightZ * dir * speed);
                        villager.hasImpulse = true;
                    }
                    villager.swing(dir > 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, true);
                } else if (tickInBeat == 6) {
                    // Contragolpe / rebote en el lugar
                    if (villager.onGround()) {
                        villager.setDeltaMovement(0, 0.15, 0);
                        villager.hasImpulse = true;
                    }
                    villager.swing(dir > 0 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, true);
                }

                // Inclinación de cabeza y cuerpo hacia el lado del paso
                villager.setYHeadRot(baseYaw + (dir * 28.0F));
                villager.setYBodyRot(baseYaw + (dir * 18.0F));
                villager.setXRot((tickInBeat < 6) ? 14.0F : -8.0F);
                break;

            case 1:
                // --- RUTINA 2: Los "Pasos Prohibidos" (Agachada profunda y rebote en cuclillas) ---
                villager.setPose(Pose.CROUCHING);
                villager.getLookControl().setLookAt(jukeboxPos.getX() + 0.5, jukeboxPos.getY() + 0.8, jukeboxPos.getZ() + 0.5, 35.0F, 35.0F);

                // Rebote constante en cuclillas cada 6 ticks
                if (stepTick % 6 == 0) {
                    if (villager.onGround()) {
                        villager.setDeltaMovement(0, 0.18, 0);
                        villager.hasImpulse = true;
                    }
                    boolean alt = (stepTick / 6) % 2 == 0;
                    villager.swing(alt ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, true);

                    // Cabeceo y quebre intenso con el bajo cumbiero
                    float swing = (alt ? 35.0F : -35.0F) * mirror;
                    villager.setYHeadRot(baseYaw + swing);
                    villager.setYBodyRot(baseYaw + swing * 0.7F);
                    villager.setXRot(alt ? 24.0F : -12.0F);
                }
                break;

            case 2:
                // --- RUTINA 3: Quiebre de Perfil (90°) y Tirar Corte ---
                villager.setPose(Pose.STANDING);
                boolean ladoDerecho = (stepTick < 24);
                float profileYaw = baseYaw + (ladoDerecho ? 90.0F : -90.0F) * mirror;
                villager.setYRot(profileYaw);
                villager.setYHeadRot(profileYaw);
                villager.setYBodyRot(profileYaw);

                if (stepTick % 6 == 0) {
                    if (villager.onGround()) {
                        villager.setDeltaMovement(0, 0.17, 0);
                        villager.hasImpulse = true;
                    }
                    villager.swing(InteractionHand.MAIN_HAND, true);
                    villager.setXRot((stepTick % 12 == 0) ? 18.0F : -10.0F);
                }
                break;

            case 3:
                // --- RUTINA 4: Tirar la Vueltita (360°) y Remate Festivo ---
                villager.setPose(Pose.STANDING);

                if (stepTick < 24) {
                    // Giro sobre el propio eje a 15° por tick (completa 360° exactos)
                    float currentSpin = baseYaw + (stepTick * 15.0F * mirror);
                    villager.setYRot(currentSpin);
                    villager.setYHeadRot(currentSpin);
                    villager.setYBodyRot(currentSpin);
                    if (stepTick % 4 == 0) {
                        villager.swing(InteractionHand.MAIN_HAND, true);
                    }
                } else if (stepTick == 24) {
                    // Gran salto de remate festivo con ambos brazos
                    if (villager.onGround()) {
                        villager.setDeltaMovement(0, 0.32, 0);
                        villager.hasImpulse = true;
                    }
                    villager.swing(InteractionHand.MAIN_HAND, true);
                    villager.swing(InteractionHand.OFF_HAND, true);
                    villager.setYRot(baseYaw);
                    villager.setYHeadRot(baseYaw);
                } else {
                    villager.getLookControl().setLookAt(jukeboxPos.getX() + 0.5, jukeboxPos.getY() + 1.2, jukeboxPos.getZ() + 0.5, 30.0F, 30.0F);
                    if (stepTick % 6 == 0) {
                        villager.swing(InteractionHand.OFF_HAND, true);
                    }
                }
                break;
        }

        // Efectos visuales y festejos sonoros en el servidor
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            // Notas musicales de colores saliendo de la cabeza del aldeano
            if (villager.tickCount % 4 == 0) {
                double noteColor = (villager.tickCount % 24) / 24.0D;
                serverLevel.sendParticles(
                        ParticleTypes.NOTE,
                        villager.getX() + (villager.getRandom().nextDouble() - 0.5) * 0.6,
                        villager.getY() + 2.2,
                        villager.getZ() + (villager.getRandom().nextDouble() - 0.5) * 0.6,
                        1, noteColor, 0.0, 0.0, 1.0
                );
            }

            // Destellos de felicidad en los drops y giros
            if (villager.tickCount % 20 == 0) {
                serverLevel.sendParticles(
                        ParticleTypes.HAPPY_VILLAGER,
                        villager.getX(),
                        villager.getY() + 1.2,
                        villager.getZ(),
                        2, 0.3, 0.2, 0.3, 0.05
                );
            }

            // Festejo vocal alegre del aldeano ("Hurr!")
            if (villager.tickCount % 45 == 0 && villager.getRandom().nextFloat() < 0.55F) {
                serverLevel.playSound(null, villager.getX(), villager.getY(), villager.getZ(),
                        SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 0.85F, 1.2F + villager.getRandom().nextFloat() * 0.35F);
            }
        }
    }

    /**
     * Subscriptor exclusivo para cliente: detecta reproducción del disco de cumbia en el motor de audio
     */
    @Mod.EventBusSubscriber(modid = VillaArgentinaMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ClientSoundHandler {
        @SubscribeEvent
        public static void onPlaySound(PlaySoundEvent event) {
            if (event.getSound() != null && CUMBIA_SOUND_IDS.contains(event.getSound().getLocation())) {
                BlockPos pos = BlockPos.containing(event.getSound().getX(), event.getSound().getY(), event.getSound().getZ());
                CLIENT_CUMBIA_JUKEBOXES.put(pos.immutable(), System.currentTimeMillis() + getDiscDurationMs(event.getSound().getLocation()));
            }
        }
    }
}
