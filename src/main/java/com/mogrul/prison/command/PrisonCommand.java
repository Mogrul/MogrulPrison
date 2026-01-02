package com.mogrul.prison.command;

import com.mogrul.prison.MogrulPrison;
import com.mogrul.prison.manager.LocaleManager;
import com.mogrul.prison.manager.PrisonerManager;
import com.mogrul.prison.manager.WorldManager;
import com.mogrul.prison.model.Prisoner;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public class PrisonCommand implements CommandExecutor {
    private final String commandName = "prison";
    private final Logger logger;

    public PrisonCommand(MogrulPrison plugin) {
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

        if (!player.hasPermission("mogrulprison.cell.teleport")) {
            sender.sendMessage(LocaleManager.get("command.error.no-permission"));
            return true;
        }

        Prisoner prisoner = PrisonerManager.onlinePrisoners.get(player.getUniqueId());
        if (prisoner == null) {
            sender.sendMessage(LocaleManager.get("command.error.prisoner-not-found"));
            return true;
        }

        Location prisonLocation = WorldManager.prisonWorld.getSpawnLocation();

        player.teleport(new Location(
                prisonLocation.getWorld(),
                prisonLocation.x() + 0.5,
                prisonLocation.y(),
                prisonLocation.z() + 0.5
        ));
        player.sendMessage(LocaleManager.get("command.prison.teleported"));

        return true;
    }
}
