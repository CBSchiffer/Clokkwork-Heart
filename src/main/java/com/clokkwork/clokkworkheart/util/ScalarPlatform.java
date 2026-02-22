package com.clokkwork.clokkworkheart.util;

import com.hypixel.hytale.server.core.entity.InteractionManager;

import java.lang.reflect.Field;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.ToDoubleFunction;

public final class ScalarPlatform {
    private ScalarPlatform(){}

    @SuppressWarnings("unchecked")
    private static CopyOnWriteArrayList<ToDoubleFunction<Object[]>> getList(String path){
        try {
            Field f = InteractionManager.class.getDeclaredField(path);
            f.setAccessible(true);
            return (CopyOnWriteArrayList<ToDoubleFunction<Object[]>>) f.get(null);
        } catch (Throwable t) {
            return null;
        }
    }

    public static boolean available() {
        return getList("clokkworkHeart$HYX_COOLDOWN_SCALARS") != null
                && getList("clokkworkHeart$HYX_TIME_SCALARS") != null;
    }

    public static void registerCooldownScalar(ToDoubleFunction<Object[]> fn) {
        CopyOnWriteArrayList<ToDoubleFunction<Object[]>> list = getList("clokkworkHeart$HYX_COOLDOWN_SCALARS");
        if(list != null) list.add(fn);
    }

    public static void registerTimeScalar(ToDoubleFunction<Object[]> fn) {
        CopyOnWriteArrayList<ToDoubleFunction<Object[]>> list = getList("clokkworkHeart$HYX_TIME_SCALARS");
        if(list != null) list.add(fn);
    }

}
