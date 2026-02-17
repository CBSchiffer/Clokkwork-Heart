package com.clokkwork.clokkworkheart.hooks;

import com.clokkwork.clokkworkheart.ClokkworkHeartPlugin;
import com.clokkwork.clokkworkheart.util.ScalarPlatform;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.function.ToDoubleFunction;

public final class AttackSpeedHooks {
    private static final String ATTACK_SPEED_STAT_ID = "ClokkworkHeart:AttackSpeed";
    private static volatile int STAT_INDEX = Integer.MAX_VALUE;
    public static void registerHooks() {
        if(!ScalarPlatform.available()) {
            ClokkworkHeartPlugin.getCHLogger().atInfo().log("[AttackSpeed] Scalar hooks not available (mixin not applied?)");
            return;
        }

        ToDoubleFunction<Object[]> provider = AttackSpeedHooks::attackSpeedFromCtx;
        ScalarPlatform.registerCooldownScalar(provider);
        ScalarPlatform.registerTimeScalar(provider);

        ClokkworkHeartPlugin.getCHLogger().atInfo().log("[AttackSpeed] Registered AttackSpeed scalars");
    }

    @SuppressWarnings("unchecked")
    private static double attackSpeedFromCtx(Object[] ctx) {
        ClokkworkHeartPlugin.getCHLogger().atInfo().log("[AttackSpeed] Attempting calculation!");
        try {
            ClokkworkHeartPlugin.getCHLogger().atInfo().log("[AttackSpeed] Attempting calculation!");
            CommandBuffer<EntityStore> buffer = (CommandBuffer<EntityStore>) ctx[0];
            Ref<EntityStore> entityStoreRef = (Ref<EntityStore>) ctx[1];
            InteractionType interactionType = (InteractionType) ctx[2];

            int idx = STAT_INDEX;
            if(idx == Integer.MAX_VALUE) {
                idx = EntityStatType.getAssetMap().getIndex(ATTACK_SPEED_STAT_ID);

                // fallback
                if(idx == Integer.MAX_VALUE) {
                    idx = EntityStatType.getAssetMap().getIndex("AttackSpeed");
                }

                STAT_INDEX = idx;
            }

            EntityStatMap statMap = (EntityStatMap) buffer.getComponent(
                    entityStoreRef,
                    EntityStatsModule.get().getEntityStatMapComponentType()
            );
            if(statMap == null) return 1.0;

            EntityStatValue value = statMap.get(idx);
            if(value == null) return 1.0;

            float bonus = value.get();

            if(!Float.isFinite(bonus)) return 1.0;

            double mult = 1.0 + (double) bonus;
            if (!Double.isFinite(mult) || mult <= 0.0) return 1.0;
            if (mult < 0.01) mult = 0.01;
            if (mult > 20.0) mult = 20.0;

            return mult;
        } catch(Throwable t) {
            return 1.0;
        }
    }
}
