package com.mogrul.prison.register;

import com.mogrul.prison.MogrulPrison;
import com.mogrul.prison.event.PlayerEvent;
import org.bukkit.plugin.PluginManager;

public class EventRegister {
    private final MogrulPrison plugin;
    private final PluginManager pluginManager;

    public EventRegister(MogrulPrison plugin) {
        this.plugin = plugin;
        this.pluginManager = plugin.getServer().getPluginManager();

        registerEvents();
    }

    private void registerEvents() {
        pluginManager.registerEvents(new PlayerEvent(), plugin);
    }
}
