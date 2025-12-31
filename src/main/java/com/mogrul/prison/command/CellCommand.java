package com.mogrul.prison.command;

import com.mogrul.prison.MogrulPrison;
import com.mogrul.prison.manager.LocaleManager;
import com.mogrul.prison.manager.PrisonerManager;
import com.mogrul.prison.manager.WorldManager;
import com.mogrul.prison.model.Prisoner;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public class CellCommand implements CommandExecutor {
    private final String commandName = "cell";
    private final Logger logger;

    public CellCommand(MogrulPrison plugin) {
        logger = plugin.getLogger();
        PluginCommand cmd = plugin.getCommand(commandName);

        if (cmd != null) {
            cmd.setExecutor(this);
        } else {
            logger.severe("Command not found: " + commandName);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LocaleManager.get("command.error.player-only"));
            return true;
        }

        Prisoner prisoner = PrisonerManager.onlinePrisoners.get(player.getUniqueId());
        if (prisoner == null) {
            sender.sendMessage(LocaleManager.get("command.error.prisoner-not-found"));
            return true;
        }

        player.teleport(prisoner.getCell().getSpawnLocation());
        player.sendMessage(LocaleManager.get("command.cell.teleported"));
        return true;
    }
}
