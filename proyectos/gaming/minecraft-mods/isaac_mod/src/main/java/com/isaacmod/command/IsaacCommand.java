package com.isaacmod.command;

import com.isaacmod.dungeon.DungeonRoom;
import com.isaacmod.dungeon.DungeonRoomManager;
import com.isaacmod.dungeon.IsaacRunManager;
import com.isaacmod.item.IsaacItems;
import com.isaacmod.stats.PlayerStats;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class IsaacCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register(IsaacCommand::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher,
                                         CommandRegistryAccess registryAccess,
                                         CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("isaac")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("start")
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            ServerPlayerEntity player = source.getPlayerOrThrow();
                            IsaacRunManager.startNewRun(player);
                            return 1;
                        })
                )
                .then(CommandManager.literal("reset")
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            ServerPlayerEntity player = source.getPlayerOrThrow();
                            IsaacRunManager.startNewRun(player);
                            return 1;
                        })
                )
                .then(CommandManager.literal("next_floor")
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            ServerPlayerEntity player = source.getPlayerOrThrow();
                            if (!IsaacRunManager.isRunActive()) {
                                source.sendMessage(Text.literal("§cNo active Isaac run. Use /isaac start first!"));
                                return 0;
                            }
                            IsaacRunManager.nextFloor(player);
                            return 1;
                        })
                )
                .then(CommandManager.literal("end")
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            IsaacRunManager.endRun();
                            source.sendMessage(Text.literal("§e[Isaac] Active run ended."));
                            return 1;
                        })
                )
                .then(CommandManager.literal("room")
                        .then(CommandManager.literal("spawn")
                                .executes(context -> {
                                    ServerCommandSource source = context.getSource();
                                    ServerPlayerEntity player = source.getPlayerOrThrow();
                                    ServerWorld world = source.getWorld();

                                    DungeonRoom room = DungeonRoomManager.generateTestRoom(world, player.getBlockPos().down());
                                    source.sendMessage(Text.literal("§a[Isaac] 16x16 Dungeon Room generated at Grid (" 
                                            + room.getGridX() + ", " + room.getGridZ() + ")! Step inside to trigger combat."));
                                    return 1;
                                })
                        )
                )
                .then(CommandManager.literal("stats")
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            ServerPlayerEntity player = source.getPlayerOrThrow();
                            PlayerStats stats = PlayerStats.compute(player);

                            source.sendMessage(Text.literal("=== Isaac Player Stats ===").formatted(Formatting.GOLD, Formatting.BOLD));
                            source.sendMessage(Text.literal(" Damage: §e" + String.format("%.2f", stats.getDamage())));
                            source.sendMessage(Text.literal(" Fire Rate (Cooldown): §e" + stats.getFireRate() + " ticks"));
                            source.sendMessage(Text.literal(" Shot Speed: §e" + String.format("%.2f", stats.getShotSpeed())));
                            source.sendMessage(Text.literal(" Range (Lifetime): §e" + stats.getRange() + " ticks"));
                            source.sendMessage(Text.literal(" Tear Count: §e" + stats.getTearCount() + (stats.getTearCount() > 1 ? " (Spread: " + stats.getSpreadAngle() + "°)" : "")));
                            source.sendMessage(Text.literal(" Explosive Shots: §e" + (stats.isExplosive() ? "YES" : "NO")));
                            source.sendMessage(Text.literal(" Lobbed Tears: §e" + (stats.isLobbed() ? "YES" : "NO")));
                            source.sendMessage(Text.literal(" Homing Tears: §e" + (stats.isHoming() ? "YES" : "NO")));
                            source.sendMessage(Text.literal(" Piercing Tears: §e" + (stats.isPiercing() ? "YES" : "NO")));
                            source.sendMessage(Text.literal(" Mega Tears (Polyphemus): §e" + (stats.isGiant() ? "YES" : "NO")));
                            source.sendMessage(Text.literal(" Godhead Aura: §e" + (stats.hasGodheadAura() ? "YES" : "NO")));
                            source.sendMessage(Text.literal(" Rubber Cement (Bouncing): §e" + (stats.hasRubberCement() ? "YES" : "NO")));
                            source.sendMessage(Text.literal(" Holy Mantle Shield: §e" + (stats.hasHolyMantle() ? "YES" : "NO")));
                            source.sendMessage(Text.literal(" The Wafer (Damage Cap): §e" + (stats.hasTheWafer() ? "YES" : "NO")));
                            source.sendMessage(Text.literal(" Pyromaniac (Blast Heal): §e" + (stats.hasPyromaniac() ? "YES" : "NO")));
                            source.sendMessage(Text.literal(" Gnawed Leaf (Stone Guard): §e" + (stats.hasGnawedLeaf() ? "YES" : "NO")));
                            return 1;
                        })
                )
                .then(CommandManager.literal("give")
                        .then(CommandManager.argument("item", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    builder.suggest("tear");
                                    builder.suggest("sacred_heart");
                                    builder.suggest("godhead");
                                    builder.suggest("mushroom");
                                    builder.suggest("2020");
                                    builder.suggest("soy_milk");
                                    builder.suggest("rubber_cement");
                                    builder.suggest("wafer");
                                    builder.suggest("pyromaniac");
                                    builder.suggest("gnawed_leaf");
                                    builder.suggest("cat_tails");
                                    builder.suggest("halo");
                                    builder.suggest("abaddon");
                                    builder.suggest("d6");
                                    builder.suggest("yum_heart");
                                    builder.suggest("belial");
                                    builder.suggest("necronomicon");
                                    builder.suggest("tammy");
                                    builder.suggest("black_heart");
                                    builder.suggest("bomb");
                                    builder.suggest("key");
                                    builder.suggest("coin");
                                    builder.suggest("brimstone");
                                    builder.suggest("spoon_bender");
                                    builder.suggest("onion");
                                    builder.suggest("spider");
                                    builder.suggest("polyphemus");
                                    builder.suggest("holy_mantle");
                                    builder.suggest("red_heart");
                                    builder.suggest("soul_heart");
                                    builder.suggest("cricket");
                                    builder.suggest("inner_eye");
                                    builder.suggest("ipecac");
                                    builder.suggest("poop");
                                    builder.suggest("butter_bean");
                                    builder.suggest("lemon");
                                    builder.suggest("breakfast");
                                    builder.suggest("spoon");
                                    builder.suggest("planet");
                                    builder.suggest("pageant");
                                    builder.suggest("all");
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    ServerCommandSource source = context.getSource();
                                    ServerPlayerEntity player = source.getPlayerOrThrow();
                                    String itemChoice = StringArgumentType.getString(context, "item");

                                    switch (itemChoice.toLowerCase()) {
                                        case "tear" -> player.giveItemStack(new ItemStack(IsaacItems.ISAAC_TEAR));
                                        case "sacred_heart" -> player.giveItemStack(new ItemStack(IsaacItems.SACRED_HEART));
                                        case "godhead" -> player.giveItemStack(new ItemStack(IsaacItems.GODHEAD));
                                        case "mushroom" -> player.giveItemStack(new ItemStack(IsaacItems.MAGIC_MUSHROOM));
                                        case "2020" -> player.giveItemStack(new ItemStack(IsaacItems.TWENTY_TWENTY));
                                        case "soy_milk" -> player.giveItemStack(new ItemStack(IsaacItems.SOY_MILK));
                                        case "rubber_cement" -> player.giveItemStack(new ItemStack(IsaacItems.RUBBER_CEMENT));
                                        case "wafer" -> player.giveItemStack(new ItemStack(IsaacItems.THE_WAFER));
                                        case "pyromaniac" -> player.giveItemStack(new ItemStack(IsaacItems.PYROMANIAC));
                                        case "gnawed_leaf" -> player.giveItemStack(new ItemStack(IsaacItems.GNAWED_LEAF));
                                        case "cat_tails" -> player.giveItemStack(new ItemStack(IsaacItems.CAT_O_NINE_TAILS));
                                        case "halo" -> player.giveItemStack(new ItemStack(IsaacItems.THE_HALO));
                                        case "abaddon" -> player.giveItemStack(new ItemStack(IsaacItems.ABADDON));
                                        case "d6" -> player.giveItemStack(new ItemStack(IsaacItems.THE_D6));
                                        case "yum_heart" -> player.giveItemStack(new ItemStack(IsaacItems.YUM_HEART));
                                        case "belial" -> player.giveItemStack(new ItemStack(IsaacItems.BOOK_OF_BELIAL));
                                        case "necronomicon" -> player.giveItemStack(new ItemStack(IsaacItems.THE_NECRONOMICON));
                                        case "tammy" -> player.giveItemStack(new ItemStack(IsaacItems.TAMMYS_HEAD));
                                        case "black_heart" -> player.giveItemStack(new ItemStack(IsaacItems.BLACK_HEART, 4));
                                        case "bomb" -> player.giveItemStack(new ItemStack(IsaacItems.ISAAC_BOMB, 10));
                                        case "key" -> player.giveItemStack(new ItemStack(IsaacItems.ISAAC_KEY, 5));
                                        case "coin" -> player.giveItemStack(new ItemStack(IsaacItems.ISAAC_COIN, 15));
                                        case "cricket" -> player.giveItemStack(new ItemStack(IsaacItems.CRICKETS_HEAD));
                                        case "inner_eye" -> player.giveItemStack(new ItemStack(IsaacItems.THE_INNER_EYE));
                                        case "ipecac" -> player.giveItemStack(new ItemStack(IsaacItems.IPECAC));
                                        case "brimstone" -> player.giveItemStack(new ItemStack(IsaacItems.BRIMSTONE));
                                        case "spoon_bender" -> player.giveItemStack(new ItemStack(IsaacItems.SPOON_BENDER));
                                        case "onion" -> player.giveItemStack(new ItemStack(IsaacItems.THE_SAD_ONION));
                                        case "spider" -> player.giveItemStack(new ItemStack(IsaacItems.MUTANT_SPIDER));
                                        case "polyphemus" -> player.giveItemStack(new ItemStack(IsaacItems.POLYPHEMUS));
                                        case "holy_mantle" -> player.giveItemStack(new ItemStack(IsaacItems.HOLY_MANTLE));
                                        case "red_heart" -> player.giveItemStack(new ItemStack(IsaacItems.RED_HEART, 4));
                                        case "soul_heart" -> player.giveItemStack(new ItemStack(IsaacItems.SOUL_HEART, 4));
                                        case "poop" -> player.giveItemStack(new ItemStack(IsaacItems.THE_POOP));
                                        case "butter_bean" -> player.giveItemStack(new ItemStack(IsaacItems.BUTTER_BEAN));
                                        case "lemon" -> player.giveItemStack(new ItemStack(IsaacItems.LEMON_MISHAP));
                                        case "breakfast" -> player.giveItemStack(new ItemStack(IsaacItems.BREAKFAST));
                                        case "spoon" -> player.giveItemStack(new ItemStack(IsaacItems.WOODEN_SPOON));
                                        case "planet" -> player.giveItemStack(new ItemStack(IsaacItems.TINY_PLANET));
                                        case "pageant" -> player.giveItemStack(new ItemStack(IsaacItems.PAGEANT_BOY));
                                        case "all" -> {
                                            for (net.minecraft.item.Item itm : DungeonRoom.getArtifactPool()) {
                                                player.giveItemStack(new ItemStack(itm));
                                            }
                                            player.giveItemStack(new ItemStack(IsaacItems.THE_POOP));
                                            player.giveItemStack(new ItemStack(IsaacItems.BUTTER_BEAN));
                                            player.giveItemStack(new ItemStack(IsaacItems.LEMON_MISHAP));
                                            player.giveItemStack(new ItemStack(IsaacItems.BREAKFAST));
                                            player.giveItemStack(new ItemStack(IsaacItems.WOODEN_SPOON));
                                            player.giveItemStack(new ItemStack(IsaacItems.TINY_PLANET));
                                            player.giveItemStack(new ItemStack(IsaacItems.PAGEANT_BOY));
                                            player.giveItemStack(new ItemStack(IsaacItems.ISAAC_TEAR));
                                            player.giveItemStack(new ItemStack(IsaacItems.RED_HEART, 4));
                                            player.giveItemStack(new ItemStack(IsaacItems.SOUL_HEART, 4));
                                            player.giveItemStack(new ItemStack(IsaacItems.BLACK_HEART, 4));
                                            player.giveItemStack(new ItemStack(IsaacItems.ISAAC_BOMB, 10));
                                            player.giveItemStack(new ItemStack(IsaacItems.ISAAC_KEY, 5));
                                            player.giveItemStack(new ItemStack(IsaacItems.ISAAC_COIN, 25));
                                        }
                                        default -> {
                                            source.sendMessage(Text.literal("§cUnknown item. Choose: sacred_heart, godhead, brimstone, poop, butter_bean, lemon, breakfast, planet, etc."));
                                            return 0;
                                        }
                                    }

                                    source.sendMessage(Text.literal("§a[Isaac] Granted: " + itemChoice));
                                    return 1;
                                })
                        )
                )
        );
    }
}
