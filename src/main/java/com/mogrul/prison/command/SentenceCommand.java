package com.mogrul.prison.command;

import com.mogrul.prison.MogrulPrison;
import com.mogrul.prison.manager.LocaleManager;
import com.mogrul.prison.manager.PrisonerManager;
import com.mogrul.prison.model.Prisoner;
import com.mogrul.prison.util.SubCommandUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class SentenceCommand implements CommandExecutor, TabCompleter {
    public SentenceCommand(MogrulPrison plugin) {
        PluginCommand cmd = plugin.getCommand("sentence");

        if (cmd != null) {
            cmd.setExecutor(this);
            cmd.setTabCompleter(this);
        } else {
            throw new NullPointerException("No sentence command found!");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("prison.sentence")) {
            sender.sendMessage(LocaleManager.get("command.error.no-permission"));
            return true;
        }

        switch (args.length) {
            case 0 -> getOwn(sender);
            case 1 -> getPrisoner(sender, args[0]);

            default -> sender.sendMessage(LocaleManager.get("command.error.invalid-subcommand"));
        }

        return true;
    }

    // SUBCOMMANDS
    private void getOwn(CommandSender sender) {
        if (!sender.hasPermission("mogrulprison.sentence.getOwn")) {
            sender.sendMessage(LocaleManager.get("command.error.no-permission"));
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(LocaleManager.get("command.error.player-only"));
            return;
        }

        Prisoner prisoner = PrisonerManager.getPrisoner(player.getUniqueId());
        if (prisoner == null) {
            sender.sendMessage(LocaleManager.get("command.error.prisoner-not-found"));
            return;
        }

        sender.sendMessage(LocaleManager.get("command.sentence.get.own", prisoner.getSentence()));
    }

    private void getPrisoner(CommandSender sender, String targetString) {
        if (!sender.hasPermission("mogrulprison.sentence.getPrisoner")) {
            sender.sendMessage(LocaleManager.get("command.error.no-permission"));
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(targetString);
        if (targetPlayer == null) {
            sender.sendMessage(LocaleManager.get("command.error.player-not-found", targetString));
            return;
        }

        Prisoner targetPrisoner = PrisonerManager.getPrisoner(targetPlayer.getUniqueId());
        if (targetPrisoner == null) {
            sender.sendMessage(LocaleManager.get("command.error.prisoner-not-found"));
            return;
        }

        sender.sendMessage(LocaleManager.get(
                "command.sentence.get.prisoner",
                targetPrisoner.getUsername(),
                targetPrisoner.getSentence()
        ));
    }

    // TAB COMPLETIONS
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return PrisonerManager.getOnlinePlayerNames();
        } else {
            return Collections.emptyList();
        }
    }
}
