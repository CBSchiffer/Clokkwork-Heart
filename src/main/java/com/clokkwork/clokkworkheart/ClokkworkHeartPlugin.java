package com.clokkwork.clokkworkheart;

import com.clokkwork.clokkworkheart.hooks.AttackSpeedHooks;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

public class ClokkworkHeartPlugin extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public ClokkworkHeartPlugin(JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from %s version %s", this.getName(), this.getManifest().getVersion().toString());
    }

    @Override
    protected void setup() {
        this.getCommandRegistry().registerCommand(new ExampleCommand(this.getName(), this.getManifest().getVersion().toString()));
        AttackSpeedHooks.registerHooks();
    }

    public static HytaleLogger getCHLogger() {
        return HytaleLogger.forEnclosingClass();
    }
}
