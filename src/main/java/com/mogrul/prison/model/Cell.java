package com.mogrul.prison.model;

import com.mogrul.prison.MogrulPrison;
import com.mogrul.prison.manager.CellManager;
import com.mogrul.prison.manager.SchematicManager;
import com.mogrul.prison.manager.WorldManager;
import com.mogrul.prison.record.SchematicBlock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class Cell {
    private final UUID uuid;
    private final int chunkX;
    private final int chunkZ;
    private final int chunkBlockX;
    private final int chunkBlockZ;
    private final int chunkBlockY = 64;
    private final Location spawnLocation;

    public Cell(UUID uuid, int chunkX, int chunkZ) {
        this.uuid = uuid;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.chunkBlockX = 16 * chunkX;
        this.chunkBlockZ = 16 * chunkZ;
        this.spawnLocation = new Location(
                WorldManager.cellsWorld,
                this.chunkBlockX + 8,
                66,
                this.chunkBlockZ + 8,
                180,0
        );
    }

    public boolean blockInCell(Block block) {
        // Shift block position to schematic block position
        int blockSchematicX = block.getX() - chunkBlockX;
        int blockSchematicZ = block.getZ() - chunkBlockZ;
        int blockSchematicY = block.getY() - chunkBlockY;

        if (
                blockSchematicX < 0
                || blockSchematicX >= 16
                || blockSchematicZ < 0
                || blockSchematicZ >= 16
                || blockSchematicY < 0
        ) {
            return false;
        }

        return CellManager.cellSchematicBlocksSet.contains(
                SchematicManager.packCoordinates(blockSchematicX, blockSchematicY, blockSchematicZ)
        );
    }

    // GETTERS
    public UUID getUuid() {
        return uuid;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public int getChunkBlockX() {
        return chunkBlockX;
    }

    public int getChunkBlockZ() {
        return chunkBlockZ;
    }

    public int getChunkBlockY() {
        return chunkBlockY;
    }
}
