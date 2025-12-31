package com.mogrul.prison.util;

import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Set;

public class SubCommandUtil {
    public static List<String> getUsableSubcommands(CommandSender sender, String basePermission, Set<String> subcommands) {
        return subcommands.stream()
                .filter(sub -> sender.hasPermission(basePermission + sub))
                .sorted()
                .toList();
    }
}
