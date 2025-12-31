package com.mogrul.prison.manager;

import com.mogrul.prison.MogrulPrison;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class LocaleManager {
    private static FileConfiguration localeConfig;
    private static FileConfiguration fallbackConfig;
    private static final LegacyComponentSerializer AMP = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .build();
    private static String defaultLocale = "en_GB";
    private static MogrulPrison plugin;

    public static void init(MogrulPrison plugin) {
        LocaleManager.plugin = plugin;

        String configLocale = ConfigManager.getString("locale");
        fallbackConfig = loadLocaleFile(defaultLocale);

        if (exists(configLocale)) {
            localeConfig = loadLocaleFile(configLocale);
        } else {
            localeConfig = loadLocaleFile(defaultLocale);
        }
    }

    // LOADERS
    private static FileConfiguration loadLocaleFile(String locale) {
        File folder = new File(plugin.getDataFolder(), "locale");
        if (!folder.exists()) folder.mkdirs();

        File file = new File(folder, locale + ".yml");
        plugin.saveResource("locale/" + locale + ".yml", true);

        return YamlConfiguration.loadConfiguration(file);
    }

    private static boolean exists(String locale) {
        InputStream stream = plugin.getResource("locale/" + locale + ".yml");
        if (stream == null) return false;

        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            YamlConfiguration.loadConfiguration(reader);
        } catch (Exception e) {
            return false;
        }

        return true;
    }


    // GETTERS
    public static Component get(String path, Object... args) {
        String raw = localeConfig.getString(path);
        if (raw == null) raw = fallbackConfig.getString(path);
        if (raw == null) {
            return Component.text("Missing locale key: " + path).color(NamedTextColor.RED);
        }

        String formatted = (args != null && args.length > 0) ? String.format(raw, args) : raw;
        return AMP.deserialize(formatted);
    }
}
