package com.isaacmod.stats;

import com.isaacmod.synergy.ItemModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlayerStats {
    public static final float BASE_DAMAGE = 3.5f;
    public static final int BASE_FIRE_RATE = 12; // Cooldown ticks between shots
    public static final float BASE_SHOT_SPEED = 1.2f;
    public static final int BASE_RANGE = 40; // Lifetime in ticks

    private final float damage;
    private final int fireRate;
    private final float shotSpeed;
    private final int range;
    private final int tearCount;
    private final float spreadAngle;
    private final boolean explosive;
    private final boolean lobbed;
    private final boolean homing;
    private final boolean piercing;
    private final boolean holyMantle;
    private final boolean giant;
    private final boolean godheadAura;
    private final boolean rubberCement;
    private final boolean theWafer;
    private final boolean pyromaniac;
    private final boolean gnawedLeaf;
    private final boolean twentyTwenty;
    private final boolean brimstoneLaser;

    public PlayerStats(float damage, int fireRate, float shotSpeed, int range,
                       int tearCount, float spreadAngle, boolean explosive, boolean lobbed) {
        this(damage, fireRate, shotSpeed, range, tearCount, spreadAngle, explosive, lobbed,
                false, false, false, false, false, false, false, false, false, false, false);
    }

    public PlayerStats(float damage, int fireRate, float shotSpeed, int range,
                       int tearCount, float spreadAngle, boolean explosive, boolean lobbed,
                       boolean homing, boolean piercing, boolean holyMantle, boolean giant) {
        this(damage, fireRate, shotSpeed, range, tearCount, spreadAngle, explosive, lobbed,
                homing, piercing, holyMantle, giant, false, false, false, false, false, false, false);
    }

    public PlayerStats(float damage, int fireRate, float shotSpeed, int range,
                       int tearCount, float spreadAngle, boolean explosive, boolean lobbed,
                       boolean homing, boolean piercing, boolean holyMantle, boolean giant,
                       boolean godheadAura, boolean rubberCement, boolean theWafer,
                       boolean pyromaniac, boolean gnawedLeaf, boolean twentyTwenty) {
        this(damage, fireRate, shotSpeed, range, tearCount, spreadAngle, explosive, lobbed,
                homing, piercing, holyMantle, giant, godheadAura, rubberCement, theWafer,
                pyromaniac, gnawedLeaf, twentyTwenty, false);
    }

    public PlayerStats(float damage, int fireRate, float shotSpeed, int range,
                       int tearCount, float spreadAngle, boolean explosive, boolean lobbed,
                       boolean homing, boolean piercing, boolean holyMantle, boolean giant,
                       boolean godheadAura, boolean rubberCement, boolean theWafer,
                       boolean pyromaniac, boolean gnawedLeaf, boolean twentyTwenty,
                       boolean brimstoneLaser) {
        this.damage = Math.max(0.5f, damage);
        this.fireRate = Math.max(2, fireRate);
        this.shotSpeed = Math.max(0.2f, shotSpeed);
        this.range = Math.max(10, range);
        this.tearCount = Math.max(1, tearCount);
        this.spreadAngle = spreadAngle;
        this.explosive = explosive;
        this.lobbed = lobbed;
        this.homing = homing;
        this.piercing = piercing;
        this.holyMantle = holyMantle;
        this.giant = giant;
        this.godheadAura = godheadAura;
        this.rubberCement = rubberCement;
        this.theWafer = theWafer;
        this.pyromaniac = pyromaniac;
        this.gnawedLeaf = gnawedLeaf;
        this.twentyTwenty = twentyTwenty;
        this.brimstoneLaser = brimstoneLaser;
    }

    public float getDamage() {
        return damage;
    }

    public int getFireRate() {
        return fireRate;
    }

    public float getShotSpeed() {
        return shotSpeed;
    }

    public int getRange() {
        return range;
    }

    public int getTearCount() {
        return tearCount;
    }

    public float getSpreadAngle() {
        return spreadAngle;
    }

    public boolean isExplosive() {
        return explosive;
    }

    public boolean isLobbed() {
        return lobbed;
    }

    public boolean isHoming() {
        return homing;
    }

    public boolean isPiercing() {
        return piercing;
    }

    public boolean hasHolyMantle() {
        return holyMantle;
    }

    public boolean isGiant() {
        return giant;
    }

    public boolean hasGodheadAura() {
        return godheadAura;
    }

    public boolean hasRubberCement() {
        return rubberCement;
    }

    public boolean hasTheWafer() {
        return theWafer;
    }

    public boolean hasPyromaniac() {
        return pyromaniac;
    }

    public boolean hasGnawedLeaf() {
        return gnawedLeaf;
    }

    public boolean hasTwentyTwenty() {
        return twentyTwenty;
    }

    public boolean hasBrimstoneLaser() {
        return brimstoneLaser;
    }

    /**
     * Computes player stats dynamically by evaluating active passive items in the player's inventory.
     */
    public static PlayerStats compute(PlayerEntity player) {
        Builder builder = new Builder();
        List<ItemModifier> modifiers = new ArrayList<>();

        for (ItemStack stack : player.getInventory().main) {
            if (!stack.isEmpty() && stack.getItem() instanceof ItemModifier modifier) {
                int count = stack.getCount();
                for (int i = 0; i < count; i++) {
                    modifiers.add(modifier);
                }
            }
        }

        // Sort modifiers by priority: flat bonuses first, then multipliers
        modifiers.sort(Comparator.comparingInt(ItemModifier::getPriority));

        for (ItemModifier mod : modifiers) {
            mod.modifyStats(builder);
        }

        return builder.build();
    }

    public static class Builder {
        private float flatDamage = BASE_DAMAGE;
        private float damageMultiplier = 1.0f;

        private float flatFireRate = BASE_FIRE_RATE;
        private float fireRateMultiplier = 1.0f;

        private float flatShotSpeed = BASE_SHOT_SPEED;
        private float shotSpeedMultiplier = 1.0f;

        private float flatRange = BASE_RANGE;
        private float rangeMultiplier = 1.0f;

        private int tearCount = 1;
        private float spreadAngle = 0.0f;
        private boolean explosive = false;
        private boolean lobbed = false;
        private boolean homing = false;
        private boolean piercing = false;
        private boolean holyMantle = false;
        private boolean giant = false;
        private boolean godheadAura = false;
        private boolean rubberCement = false;
        private boolean theWafer = false;
        private boolean pyromaniac = false;
        private boolean gnawedLeaf = false;
        private boolean twentyTwenty = false;
        private boolean brimstoneLaser = false;

        public Builder addDamage(float amount) {
            this.flatDamage += amount;
            return this;
        }

        public Builder multiplyDamage(float multiplier) {
            this.damageMultiplier *= multiplier;
            return this;
        }

        public Builder addFireRate(float ticks) {
            this.flatFireRate += ticks;
            return this;
        }

        public Builder multiplyFireRate(float multiplier) {
            this.fireRateMultiplier *= multiplier;
            return this;
        }

        public Builder addShotSpeed(float speed) {
            this.flatShotSpeed += speed;
            return this;
        }

        public Builder multiplyShotSpeed(float multiplier) {
            this.shotSpeedMultiplier *= multiplier;
            return this;
        }

        public Builder addRange(float range) {
            this.flatRange += range;
            return this;
        }

        public Builder multiplyRange(float multiplier) {
            this.rangeMultiplier *= multiplier;
            return this;
        }

        public Builder setTearCount(int count) {
            this.tearCount = Math.max(this.tearCount, count);
            return this;
        }

        public Builder setSpreadAngle(float angle) {
            this.spreadAngle = Math.max(this.spreadAngle, angle);
            return this;
        }

        public Builder setExplosive(boolean explosive) {
            this.explosive = this.explosive || explosive;
            return this;
        }

        public Builder setLobbed(boolean lobbed) {
            this.lobbed = this.lobbed || lobbed;
            return this;
        }

        public Builder setHoming(boolean homing) {
            this.homing = this.homing || homing;
            return this;
        }

        public Builder setPiercing(boolean piercing) {
            this.piercing = this.piercing || piercing;
            return this;
        }

        public Builder setHolyMantle(boolean holyMantle) {
            this.holyMantle = this.holyMantle || holyMantle;
            return this;
        }

        public Builder setGiant(boolean giant) {
            this.giant = this.giant || giant;
            return this;
        }

        public Builder setGodheadAura(boolean godheadAura) {
            this.godheadAura = this.godheadAura || godheadAura;
            return this;
        }

        public Builder setRubberCement(boolean rubberCement) {
            this.rubberCement = this.rubberCement || rubberCement;
            return this;
        }

        public Builder setTheWafer(boolean theWafer) {
            this.theWafer = this.theWafer || theWafer;
            return this;
        }

        public Builder setPyromaniac(boolean pyromaniac) {
            this.pyromaniac = this.pyromaniac || pyromaniac;
            return this;
        }

        public Builder setGnawedLeaf(boolean gnawedLeaf) {
            this.gnawedLeaf = this.gnawedLeaf || gnawedLeaf;
            return this;
        }

        public Builder setTwentyTwenty(boolean twentyTwenty) {
            this.twentyTwenty = this.twentyTwenty || twentyTwenty;
            return this;
        }

        public Builder setBrimstoneLaser(boolean brimstoneLaser) {
            this.brimstoneLaser = this.brimstoneLaser || brimstoneLaser;
            return this;
        }

        public PlayerStats build() {
            float finalDamage = flatDamage * damageMultiplier;
            int finalFireRate = Math.round(flatFireRate * fireRateMultiplier);
            float finalShotSpeed = flatShotSpeed * shotSpeedMultiplier;
            int finalRange = Math.round(flatRange * rangeMultiplier);

            return new PlayerStats(finalDamage, finalFireRate, finalShotSpeed, finalRange,
                    tearCount, spreadAngle, explosive, lobbed, homing, piercing, holyMantle, giant,
                    godheadAura, rubberCement, theWafer, pyromaniac, gnawedLeaf, twentyTwenty,
                    brimstoneLaser);
        }
    }
}
