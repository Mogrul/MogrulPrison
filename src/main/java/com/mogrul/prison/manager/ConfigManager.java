package com.mogrul.prison.manager;

import com.mogrul.prison.MogrulPrison;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigManager {
    private static FileConfiguration config;

    public static void init(FileConfiguration config) {
        ConfigManager.config = config;
    }

    public static List<String> getStringList(String key) {
        return config.getStringList(key);
    }

    public static int getInt(String key) {
        return config.getInt(key);
    }

    public static String getString(String key) {
        return config.getString(key);
    }
}
