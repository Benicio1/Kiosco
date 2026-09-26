package com.isaacmod.synergy;

import com.isaacmod.stats.PlayerStats;

public interface ItemModifier {
    /**
     * Modifies the player's cumulative stats.
     */
    void modifyStats(PlayerStats.Builder builder);

    /**
     * Item priority order for stacking (e.g. flat additions vs multipliers).
     */
    default int getPriority() {
        return 100;
    }
}
