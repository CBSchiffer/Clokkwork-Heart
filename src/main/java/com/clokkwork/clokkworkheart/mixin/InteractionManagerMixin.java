package com.clokkwork.clokkworkheart.mixin;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.ToDoubleFunction;

@Mixin(InteractionManager.class)
public class InteractionManagerMixin {
    @Shadow
    protected CommandBuffer<EntityStore> commandBuffer;

    @Unique
    private static final ThreadLocal<Object[]> HYX_CTX = new ThreadLocal<>();

    @Unique
    private static final ThreadLocal<Object[]> HYX_TIME_CTX = new ThreadLocal<>();

    @Unique
    public static final CopyOnWriteArrayList<ToDoubleFunction<Object[]>> HYX_COOLDOWN_SCALARS
            = new CopyOnWriteArrayList<>();

    @Unique
    public static final CopyOnWriteArrayList<ToDoubleFunction<Object[]>> HYX_TIME_SCALARS
            = new CopyOnWriteArrayList<>();

    @Unique
    public static double clokkworkHeart$clamp(double value, double min, double max) {
        if(!Double.isFinite(value)) return 1.0;
        if(value < min) return min;
        if(value > max) return max;
        return value;
    }

    @Unique
    private static boolean clokkworkHeart$isHeldWeapon(InteractionType type) {
        return type == InteractionType.Primary || type == InteractionType.Secondary;
    }

    @Unique
    private float clokkworkHeart$applyCooldownScalar(Ref<EntityStore> ref, InteractionType type, RootInteraction root,
                                                      String cooldownId, float cooldownSecs, boolean remote) {
        if(ref == null || type == null || root == null || cooldownId == null) return cooldownSecs;
        if(!clokkworkHeart$isHeldWeapon(type)) return cooldownSecs;
        if(cooldownSecs <= 0.0f) return cooldownSecs;
        Object[] ctx = new Object[] {
                this.commandBuffer, ref, type, root,  cooldownId, cooldownSecs, remote
        };
        double mult = 1.0;
        for(ToDoubleFunction<Object[]> func : HYX_COOLDOWN_SCALARS) {
            try {
                double m = func.applyAsDouble(ctx);
                if(Double.isFinite(m) && m > 0.0) mult *= m;
            } catch (Throwable ignored) {}
        }
        mult = clokkworkHeart$clamp(mult, 0.01, 20.0);
        return (float)(cooldownSecs /  mult);
    }

    @Inject(method = "isOnCooldown", at = @At("HEAD"))
    private void clokkworkHeart$cooldownCtxHead(
            Ref<EntityStore> ref,
            InteractionType type,
            RootInteraction root,
            boolean remote,
            CallbackInfoReturnable<Boolean> cir) {
        HYX_CTX.set(new Object[] { ref, type, root, remote });
    }

    @Inject(method = "isOnCooldown", at = @At("RETURN"))
    private void clokkworkHeart$cooldownCtxReturn(
            Ref<EntityStore> ref,
            InteractionType type,
            RootInteraction root,
            boolean remote,
            CallbackInfoReturnable<Boolean> cir) {
        HYX_CTX.remove();
    }

    @Redirect(
            method = "isOnCooldown",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/hypixel/hytale/server/core/modules/interaction/interaction/CooldownHandler;" +
                            "isOnCooldown(Lcom/hypixel/hytale/server/core/modules/interaction/interaction/config/RootInteraction;" +
                            "Ljava/lang/String;F[FZ)Z"
            )
    )
    private boolean clokkworkHeart$redirect_isOnCooldown(
            CooldownHandler handler,
            RootInteraction root,
            String id,
            float maxTime,
            float[] chargeTimes,
            boolean interruptRecharge
    ) {
        Object[] ctx = HYX_CTX.get();
        float scaled = clokkworkHeart$applyCooldownScalar((Ref<EntityStore>) ctx[0], (InteractionType) ctx[1], (RootInteraction) ctx[2], id, maxTime, (boolean) ctx[3]);
        return handler.isOnCooldown(root, id, scaled, chargeTimes, interruptRecharge);
    }

    @Redirect(
            method = "isOnCooldown",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/hypixel/hytale/server/core/modules/interaction/interaction/CooldownHandler;" +
                            "resetCooldown(Ljava/lang/String;F[FZ)V",
                    ordinal = 0
            )
    )
    private void clokkworkHeart$redirect_resetCooldown0(
            CooldownHandler handler,
            String id,
            float maxTime,
            float[] chargeTimes,
            boolean interruptRecharge) {
        Object[] ctx = HYX_CTX.get();
        float scaled = clokkworkHeart$applyCooldownScalar((Ref<EntityStore>) ctx[0], (InteractionType) ctx[1], (RootInteraction) ctx[2], id, maxTime, (boolean) ctx[3]);
        handler.resetCooldown(id, scaled, chargeTimes, interruptRecharge);
    }

    @Redirect(
            method = "isOnCooldown",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/hypixel/hytale/server/core/modules/interaction/interaction/CooldownHandler;" +
                            "resetCooldown(Ljava/lang/String;F[FZ)V",
                    ordinal = 1
            )
    )
    private void clokkworkHeart$redirect_resetCooldown1(
            CooldownHandler handler,
            String id,
            float maxTime,
            float[] chargeTimes,
            boolean interruptRecharge) {
        Object[] ctx = HYX_CTX.get();
        float scaled = clokkworkHeart$applyCooldownScalar((Ref<EntityStore>) ctx[0], (InteractionType) ctx[1], (RootInteraction) ctx[2], id, maxTime, (boolean) ctx[3]);
        handler.resetCooldown(id, scaled, chargeTimes, interruptRecharge);
    }

    @Inject(method = "serverTick", at = @At("HEAD"))
    private void clokkworkHeart$timeCtxHead(Ref<EntityStore> ref, InteractionChain chain, long tickTime,
                                 CallbackInfoReturnable<?> cir) {
        HYX_TIME_CTX.set(new Object[] { ref, chain, tickTime });
    }

    @Inject(method = "serverTick", at = @At("RETURN"))
    private void clokkworkHeart$timeCtxReturn(Ref<EntityStore> ref, InteractionChain chain, long tickTime,
                                   CallbackInfoReturnable<?> cir) {
        HYX_TIME_CTX.remove();
    }

    @Redirect(
            method = "serverTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/hypixel/hytale/server/core/modules/time/TimeResource;getTimeDilationModifier()F"
            )
    )
    private float clokkworkHeart$redirect_timeDilationModifier(TimeResource timeResource) {
        float base = timeResource.getTimeDilationModifier();

        Object[] ctx = HYX_TIME_CTX.get();
        if (ctx == null) return base;

        InteractionType type = ((InteractionChain) ctx[1]).getType();
        if(!clokkworkHeart$isHeldWeapon(type)) return base;

        RootInteraction root = ((InteractionChain) ctx[1]).getRootInteraction();

        Object[] args = new Object[] {
                this.commandBuffer,
                ctx[0],
                type,
                root,
                base,
                (long) ctx[2]
        };
        double mult = 1.0;
        for(ToDoubleFunction<Object[]> f : HYX_TIME_SCALARS) {
            try {
                double m = f.applyAsDouble(args);
                if(Double.isFinite(m) && m > 0.0) mult *= m;
            } catch (Throwable ignored) {}
        }
        mult = clokkworkHeart$clamp(mult, 0.01, 20.0);
        return (float)(base * mult);
    }
}
