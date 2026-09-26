package com.isaacmod.item;

import com.isaacmod.synergy.ItemModifier;
import net.minecraft.item.Item;

public abstract class IsaacPassiveItem extends Item implements ItemModifier {
    public IsaacPassiveItem(Settings settings) {
        super(settings.maxCount(1));
    }
}
