package com.mogrul.prison;

import com.mogrul.prison.manager.*;
import com.mogrul.prison.register.CommandRegister;
import com.mogrul.prison.register.EventRegister;
import org.bukkit.plugin.java.JavaPlugin;

public final class MogrulPrison extends JavaPlugin {
    @Override
    public void onLoad() {
        saveDefaultConfig();
        ConfigManager.init(getConfig());
        LocaleManager.init(this);
        DatabaseManager.init(this);
        SchematicManager.init(this, DatabaseManager.connection);
        PrisonerManager.init(getLogger(), DatabaseManager.connection);

        CellManager.init(getLogger(), DatabaseManager.connection);
        MineMangager.init(getLogger(), DatabaseManager.connection);
    }

    @Override
    public void onEnable() {
        // Initiate registers
        new EventRegister(this);
        new CommandRegister(this);

        // Initiate managers
        WorldManager.init(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
