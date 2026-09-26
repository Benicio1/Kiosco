package com.isaacmod.dungeon;

import com.isaacmod.item.IsaacItems;
import net.minecraft.item.Item;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RunItemPool {
    private final List<Item> availablePool = new ArrayList<>();

    public RunItemPool() {
        reset();
    }

    public void reset() {
        availablePool.clear();
        Collections.addAll(availablePool,
                // Top-Tier Passives
                IsaacItems.SACRED_HEART,
                IsaacItems.GODHEAD,
                IsaacItems.MAGIC_MUSHROOM,
                IsaacItems.TWENTY_TWENTY,
                IsaacItems.POLYPHEMUS,
                IsaacItems.BRIMSTONE,
                IsaacItems.HOLY_MANTLE,
                IsaacItems.THE_WAFER,
                IsaacItems.PYROMANIAC,
                IsaacItems.CRICKETS_HEAD,

                // Good Passives & Synergies
                IsaacItems.MUTANT_SPIDER,
                IsaacItems.THE_INNER_EYE,
                IsaacItems.IPECAC,
                IsaacItems.SPOON_BENDER,
                IsaacItems.THE_SAD_ONION,
                IsaacItems.RUBBER_CEMENT,
                IsaacItems.CAT_O_NINE_TAILS,
                IsaacItems.THE_HALO,
                IsaacItems.ABADDON,
                IsaacItems.GNAWED_LEAF,

                // Actives
                IsaacItems.THE_D6,
                IsaacItems.YUM_HEART,
                IsaacItems.BOOK_OF_BELIAL,
                IsaacItems.THE_NECRONOMICON,
                IsaacItems.TAMMYS_HEAD,

                // Low-tier & Trash / Troll Items
                IsaacItems.THE_POOP,
                IsaacItems.BUTTER_BEAN,
                IsaacItems.LEMON_MISHAP,
                IsaacItems.BREAKFAST,
                IsaacItems.WOODEN_SPOON,
                IsaacItems.TINY_PLANET,
                IsaacItems.PAGEANT_BOY
        );
    }

    /**
     * Draws a random item from the pool and REMOVES it so it cannot repeat in this run!
     * If the pool is completely exhausted, returns Breakfast (the authentic Isaac fallback).
     */
    public Item drawItem(Random random) {
        if (availablePool.isEmpty()) {
            return IsaacItems.BREAKFAST;
        }
        int index = random.nextInt(availablePool.size());
        return availablePool.remove(index);
    }

    public Item drawItem(java.util.Random random) {
        if (availablePool.isEmpty()) {
            return IsaacItems.BREAKFAST;
        }
        int index = random.nextInt(availablePool.size());
        return availablePool.remove(index);
    }

    public int getRemainingCount() {
        return availablePool.size();
    }
}
