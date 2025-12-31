package com.mogrul.prison.command;

import com.mogrul.prison.MogrulPrison;
import com.mogrul.prison.manager.ConfigManager;
import com.mogrul.prison.manager.LocaleManager;
import com.mogrul.prison.manager.PrisonerManager;
import com.mogrul.prison.model.Prisoner;
import com.mogrul.prison.util.SubCommandUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class ContrabandCommand implements CommandExecutor, TabCompleter {
    private static final Set<String> SUB_COMMANDS = Set.of(
            "get", "send", "add", "set", "remove", "reset"
    );

    public ContrabandCommand(MogrulPrison plugin) {
        PluginCommand command = plugin.getCommand("contraband");

        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        } else {
            throw new RuntimeException("No contraband command found!");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(LocaleManager.get("command.error.no-subcommand"));
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "get" -> getSubcommand(sender, args);
            case "send" -> sendSubcommand(sender, args);
            case "add" -> addSubcommand(sender, args);
            case "set" -> setSubcommand(sender, args);
            case "remove" -> removeSubcommand(sender, args);
            case "reset" -> resetSubcommand(sender, args);

            default -> sender.sendMessage(LocaleManager.get("command.error.invalid-subcommand"));
        }

        return true;
    }


    // SUBCOMMANDS
    private void getSubcommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mogrulprison.contraband.get")) {
            sender.sendMessage(LocaleManager.get("command.error.no-permission"));
            return;
        }

        if (args.length == 1) {
            sender.sendMessage(LocaleManager.get("command.error.invalid-subcommand"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(LocaleManager.get("command.error.player-not-found", args[1]));
            return;
        }
        Prisoner targetPrisoner = PrisonerManager.getPrisoner(target.getUniqueId());
        if (targetPrisoner == null) {
            sender.sendMessage(LocaleManager.get("command.error.prisoner-not-found"));
            return;
        }

        sender.sendMessage(LocaleManager.get(
                "command.contraband.get",
                targetPrisoner.getUsername(),
                targetPrisoner.getContraband()
        ));
    }

    private void sendSubcommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mogrulprison.contraband.send")) {
            sender.sendMessage(LocaleManager.get("command.error.no-permission"));
            return;
        }

        if (!(sender instanceof Player senderPlayer)) {
            sender.sendMessage(LocaleManager.get("command.error.player-only"));
            return;
        }

        if (args.length <= 2) {
            sender.sendMessage(LocaleManager.get("command.error.invalid-subcommand"));
            return;
        }

        // Validate amount is integer
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(LocaleManager.get("command.error.number-required", args[2]));
            return;
        }

        // Validate target is Player
        PlayerInfo targetInfo = validatePlayer(sender, args[1]);
        if (targetInfo == null) return;
        Prisoner targetPrisoner = targetInfo.prisoner;
        Player targetPlayer = targetInfo.player;

        Prisoner senderPrisoner = PrisonerManager.getPrisoner(senderPlayer.getUniqueId());
        if (senderPrisoner == null) {
            sender.sendMessage(LocaleManager.get("command.error.prisoner-not-found"));
            return;
        }

        if (senderPrisoner.getContraband() < amount) {
            sender.sendMessage(LocaleManager.get("command.contraband.send.not-enough", amount));
            return;
        }

        if (senderPrisoner.getUuid().equals(targetPlayer.getUniqueId())) {
            sender.sendMessage(LocaleManager.get("command.contraband.send.same-target"));
            return;
        }

        senderPrisoner.removeContraband(amount);
        targetPrisoner.addContraband(amount);

        PrisonerManager.updatePrisoner(senderPrisoner);
        PrisonerManager.updatePrisoner(targetPrisoner);

        sender.sendMessage(LocaleManager.get(
                "command.contraband.send.success-sender", amount, targetPrisoner.getUsername()
        ));
        targetPlayer.sendMessage(LocaleManager.get(
                "command.contraband.send.success-target", senderPrisoner.getUsername(), amount
        ));
    }

    private void addSubcommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mogrulprison.contraband.add")) {
            sender.sendMessage(LocaleManager.get("command.error.no-permission"));
            return;
        }

        if (args.length <= 2) {
            sender.sendMessage(LocaleManager.get("command.error.invalid-subcommand"));
            return;
        }

        // Validate amount is integer
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(LocaleManager.get("command.error.number-required", args[2]));
            return;
        }

        // Validate target is Player
        PlayerInfo targetInfo = validatePlayer(sender, args[1]);
        if (targetInfo == null) return;
        Prisoner targetPrisoner = targetInfo.prisoner;
        Player targetPlayer = targetInfo.player;

        targetPrisoner.addContraband(amount);
        PrisonerManager.updatePrisoner(targetPrisoner);

        targetPlayer.sendMessage(
                LocaleManager.get("command.contraband.add.success-target", amount)
        );
        sender.sendMessage(
                LocaleManager.get("command.contraband.add.success-sender", amount, targetPrisoner.getUsername())
        );
    }

    private void setSubcommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mogrulprison.contraband.set")) {
            sender.sendMessage(LocaleManager.get("command.error.no-permission"));
            return;
        }

        if (args.length <= 2) {
            sender.sendMessage(LocaleManager.get("command.error.invalid-subcommand"));
            return;
        }

        // Validate amount is integer
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(LocaleManager.get("command.error.number-required", args[2]));
            return;
        }

        // Validate target is Player
        PlayerInfo targetInfo = validatePlayer(sender, args[1]);
        if (targetInfo == null) return;
        Prisoner targetPrisoner = targetInfo.prisoner;
        Player targetPlayer = targetInfo.player;

        targetPrisoner.setContraband(amount);
        PrisonerManager.updatePrisoner(targetPrisoner);

        targetPlayer.sendMessage(
                LocaleManager.get("command.contraband.set.success-target", amount)
        );
        sender.sendMessage(
                LocaleManager.get("command.contraband.set.success-sender", targetPrisoner.getUsername(), amount)
        );
    }

    private void removeSubcommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mogrulprison.contraband.remove")) {
            sender.sendMessage(LocaleManager.get("command.error.no-permission"));
            return;
        }

        if (args.length <= 2) {
            sender.sendMessage(LocaleManager.get("command.error.invalid-subcommand"));
            return;
        }

        // Validate amount is integer
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(LocaleManager.get("command.error.number-required", args[2]));
            return;
        }

        // Validate target is Player
        PlayerInfo targetInfo = validatePlayer(sender, args[1]);
        if (targetInfo == null) return;
        Prisoner targetPrisoner = targetInfo.prisoner;
        Player targetPlayer = targetInfo.player;

        targetPrisoner.removeContraband(amount);
        PrisonerManager.updatePrisoner(targetPrisoner);

        targetPlayer.sendMessage(
                LocaleManager.get("command.contraband.remove.success-target", amount)
        );
        sender.sendMessage(
                LocaleManager.get("command.contraband.remove.success-sender", amount,  targetPrisoner.getUsername())
        );
    }

    private void resetSubcommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mogrulprison.contraband.reset")) {
            sender.sendMessage(LocaleManager.get("command.error.no-permission"));
            return;
        }

        if (args.length <= 1) {
            sender.sendMessage(LocaleManager.get("command.error.invalid-subcommand"));
            return;
        }

        // Validate target is Player
        PlayerInfo targetInfo = validatePlayer(sender, args[1]);
        if (targetInfo == null) return;
        Prisoner targetPrisoner = targetInfo.prisoner;
        Player targetPlayer = targetInfo.player;

        targetPrisoner.resetContraband();
        PrisonerManager.updatePrisoner(targetPrisoner);

        targetPlayer.sendMessage(
                LocaleManager.get(
                        "command.contraband.reset.success-target",
                        ConfigManager.getInt("starting-contraband")
                )
        );
        sender.sendMessage(
                LocaleManager.get(
                        "command.contraband.reset.success-sender",
                        targetPrisoner.getUsername(), ConfigManager.getInt("starting-contraband")
                )
        );
    }


    // HELPERS
    private record PlayerInfo(Player player, Prisoner prisoner) {}

    private PlayerInfo validatePlayer(CommandSender sender, String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            sender.sendMessage(LocaleManager.get("command.error.player-not-found", playerName));
            return null;
        }
        Prisoner prisoner = PrisonerManager.getPrisoner(player.getUniqueId());
        if (prisoner == null) {
            sender.sendMessage(LocaleManager.get("command.error.prisoner-not-found", playerName));
            return null;
        }

        return new PlayerInfo(player, prisoner);
    }

    // TAB COMPLETIONS
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return SubCommandUtil.getUsableSubcommands(sender, "mogrulprison.contraband", SUB_COMMANDS);
        } else {
            return switch (args[0].toLowerCase(Locale.ROOT)) {
                case "get", "reset" -> handleGetResetTabCompletion(args);
                case "send", "add", "set", "remove" -> handleSendAddSetRemoveTabCompletion(args);

                default -> Collections.emptyList();
            };
        }
    }

    private List<String> handleGetResetTabCompletion(String[] args) {
        if (args.length == 2) {
            return PrisonerManager.getOnlinePlayerNames();
        } else {
            return Collections.emptyList();
        }
    }

    private List<String> handleSendAddSetRemoveTabCompletion(String[] args) {
        return switch (args.length) {
            case 2 -> PrisonerManager.getOnlinePlayerNames();
            case 3 -> List.of("10", "50", "100", "200");

            default -> Collections.emptyList();
        };
    }

}
