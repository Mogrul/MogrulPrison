package com.mogrul.prison.event;

import com.mogrul.prison.manager.LocaleManager;
import com.mogrul.prison.manager.PrisonerManager;
import com.mogrul.prison.manager.WorldManager;
import com.mogrul.prison.model.Cell;
import com.mogrul.prison.model.Prisoner;
import com.mogrul.prison.util.SentenceUtil;
import com.mogrul.prison.util.TimeUtil;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerEvent implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Prisoner prisoner = PrisonerManager.getPrisoner(player.getUniqueId());

        if (prisoner == null) {
            prisoner = PrisonerManager.createPrisoner(player);
            SentenceUtil.sendFirstJoinMessage(player, prisoner);
        } else {
            prisoner.setLastJoin(TimeUtil.getTimeFromString(System.currentTimeMillis()));
            prisoner.setUsername(player.getName());
            PrisonerManager.updatePrisoner(prisoner);
        }

        PrisonerManager.onlinePrisoners.put(prisoner.getUuid(), prisoner);

        // Teleport player to their cell
        player.teleport(prisoner.getCell().getSpawnLocation());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PrisonerManager.onlinePrisoners.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("mogrulprison.cell.break")) return;

        Block block = event.getBlock();
        Prisoner prisoner = PrisonerManager.getPrisoner(player.getUniqueId());
        if (prisoner == null) {
            event.setCancelled(true);
            return;
        }
        Cell cell = prisoner.getCell();

        if (cell.blockInCell(block)) {
            event.setCancelled(true);
            player.sendMessage(LocaleManager.get("event.block-break.cell"));
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Prisoner prisoner = PrisonerManager.getPrisoner(player.getUniqueId());

        if (prisoner == null) {
            event.setRespawnLocation(WorldManager.prisonWorld.getSpawnLocation()); // Fallback to prison world
        } else {
            event.setRespawnLocation(prisoner.getCell().getSpawnLocation());
        }
    }
}
