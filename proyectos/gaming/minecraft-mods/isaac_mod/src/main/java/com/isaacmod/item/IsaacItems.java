package com.isaacmod.item;

import com.isaacmod.IsaacMod;
import com.isaacmod.block.IsaacBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class IsaacItems {
    // Weapons & Starter
    public static final Item ISAAC_TEAR = registerItem("isaac_tear", new IsaacTearItem(new Item.Settings()));

    // Original Passives
    public static final Item CRICKETS_HEAD = registerItem("crickets_head", new CricketsHeadItem(new Item.Settings()));
    public static final Item THE_INNER_EYE = registerItem("the_inner_eye", new TheInnerEyeItem(new Item.Settings()));
    public static final Item IPECAC = registerItem("ipecac", new IpecacItem(new Item.Settings()));
    public static final Item BRIMSTONE = registerItem("brimstone", new BrimstoneItem(new Item.Settings()));
    public static final Item SPOON_BENDER = registerItem("spoon_bender", new SpoonBenderItem(new Item.Settings()));
    public static final Item THE_SAD_ONION = registerItem("the_sad_onion", new TheSadOnionItem(new Item.Settings()));
    public static final Item MUTANT_SPIDER = registerItem("mutant_spider", new MutantSpiderItem(new Item.Settings()));
    public static final Item POLYPHEMUS = registerItem("polyphemus", new PolyphemusItem(new Item.Settings()));
    public static final Item HOLY_MANTLE = registerItem("holy_mantle", new HolyMantleItem(new Item.Settings()));

    // New Legend Passives
    public static final Item SACRED_HEART = registerItem("sacred_heart", new SacredHeartItem(new Item.Settings()));
    public static final Item GODHEAD = registerItem("godhead", new GodheadItem(new Item.Settings()));
    public static final Item MAGIC_MUSHROOM = registerItem("magic_mushroom", new MagicMushroomItem(new Item.Settings()));
    public static final Item TWENTY_TWENTY = registerItem("twenty_twenty", new TwentyTwentyItem(new Item.Settings()));
    public static final Item SOY_MILK = registerItem("soy_milk", new SoyMilkItem(new Item.Settings()));
    public static final Item RUBBER_CEMENT = registerItem("rubber_cement", new RubberCementItem(new Item.Settings()));
    public static final Item THE_WAFER = registerItem("the_wafer", new TheWaferItem(new Item.Settings()));
    public static final Item PYROMANIAC = registerItem("pyromaniac", new PyromaniacItem(new Item.Settings()));
    public static final Item GNAWED_LEAF = registerItem("gnawed_leaf", new GnawedLeafItem(new Item.Settings()));
    public static final Item CAT_O_NINE_TAILS = registerItem("cat_o_nine_tails", new CatONineTailsItem(new Item.Settings()));
    public static final Item THE_HALO = registerItem("the_halo", new TheHaloItem(new Item.Settings()));
    public static final Item ABADDON = registerItem("abaddon", new AbaddonItem(new Item.Settings()));

    // Active Items (Room-Charge Spacebar Items)
    public static final Item THE_D6 = registerItem("the_d6", new TheD6Item(new Item.Settings()));
    public static final Item YUM_HEART = registerItem("yum_heart", new YumHeartItem(new Item.Settings()));
    public static final Item BOOK_OF_BELIAL = registerItem("book_of_belial", new BookOfBelialItem(new Item.Settings()));
    public static final Item THE_NECRONOMICON = registerItem("the_necronomicon", new TheNecronomiconItem(new Item.Settings()));
    public static final Item TAMMYS_HEAD = registerItem("tammys_head", new TammysHeadItem(new Item.Settings()));

    // Pickups & Consumables
    public static final Item RED_HEART = registerItem("red_heart", new RedHeartItem(new Item.Settings()));
    public static final Item SOUL_HEART = registerItem("soul_heart", new SoulHeartItem(new Item.Settings()));
    public static final Item BLACK_HEART = registerItem("black_heart", new BlackHeartItem(new Item.Settings()));
    public static final Item ISAAC_BOMB = registerItem("isaac_bomb", new IsaacBombItem(new Item.Settings()));
    public static final Item ISAAC_KEY = registerItem("isaac_key", new IsaacKeyItem(new Item.Settings()));
    public static final Item ISAAC_COIN = registerItem("isaac_coin", new IsaacCoinItem(new Item.Settings()));

    // Trash & Low-Tier / Troll Items
    public static final Item THE_POOP = registerItem("the_poop", new ThePoopItem(new Item.Settings()));
    public static final Item BUTTER_BEAN = registerItem("butter_bean", new ButterBeanItem(new Item.Settings()));
    public static final Item LEMON_MISHAP = registerItem("lemon_mishap", new LemonMishapItem(new Item.Settings()));
    public static final Item BREAKFAST = registerItem("breakfast", new BreakfastItem(new Item.Settings()));
    public static final Item WOODEN_SPOON = registerItem("wooden_spoon", new WoodenSpoonItem(new Item.Settings()));
    public static final Item TINY_PLANET = registerItem("tiny_planet", new TinyPlanetItem(new Item.Settings()));
    public static final Item PAGEANT_BOY = registerItem("pageant_boy", new PageantBoyItem(new Item.Settings()));

    public static final RegistryKey<ItemGroup> ISAAC_GROUP_KEY = RegistryKey.of(
            Registries.ITEM_GROUP.getKey(),
            new Identifier(IsaacMod.MOD_ID, "isaac_group")
    );

    public static final ItemGroup ISAAC_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ISAAC_TEAR))
            .displayName(Text.translatable("itemGroup.isaac.isaac_group"))
            .entries((context, entries) -> {
                // Weapons & Actives
                entries.add(ISAAC_TEAR);
                entries.add(THE_D6);
                entries.add(YUM_HEART);
                entries.add(BOOK_OF_BELIAL);
                entries.add(THE_NECRONOMICON);
                entries.add(TAMMYS_HEAD);

                // Passives
                entries.add(SACRED_HEART);
                entries.add(GODHEAD);
                entries.add(MAGIC_MUSHROOM);
                entries.add(TWENTY_TWENTY);
                entries.add(SOY_MILK);
                entries.add(RUBBER_CEMENT);
                entries.add(THE_WAFER);
                entries.add(PYROMANIAC);
                entries.add(GNAWED_LEAF);
                entries.add(CAT_O_NINE_TAILS);
                entries.add(THE_HALO);
                entries.add(ABADDON);
                entries.add(BRIMSTONE);
                entries.add(SPOON_BENDER);
                entries.add(THE_SAD_ONION);
                entries.add(MUTANT_SPIDER);
                entries.add(POLYPHEMUS);
                entries.add(HOLY_MANTLE);
                entries.add(CRICKETS_HEAD);
                entries.add(THE_INNER_EYE);
                entries.add(IPECAC);

                // Consumables & Pickups
                entries.add(RED_HEART);
                entries.add(SOUL_HEART);
                entries.add(BLACK_HEART);
                entries.add(ISAAC_BOMB);
                entries.add(ISAAC_KEY);
                entries.add(ISAAC_COIN);

                // Low-tier & Trash / Troll Items
                entries.add(THE_POOP);
                entries.add(BUTTER_BEAN);
                entries.add(LEMON_MISHAP);
                entries.add(BREAKFAST);
                entries.add(WOODEN_SPOON);
                entries.add(TINY_PLANET);
                entries.add(PAGEANT_BOY);

                // Blocks
                entries.add(IsaacBlocks.DOOR_BARRIER);
            })
            .build();

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(IsaacMod.MOD_ID, name), item);
    }

    public static void registerModItems() {
        IsaacMod.LOGGER.info("Registering Isaac Mod Items");
        Registry.register(Registries.ITEM_GROUP, ISAAC_GROUP_KEY, ISAAC_GROUP);
    }
}
