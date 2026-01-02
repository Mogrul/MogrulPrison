package com.mogrul.prison.model;

import com.mogrul.prison.manager.SchematicManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.joml.Vector2d;
import org.joml.Vector3d;

import java.util.Set;
import java.util.UUID;

public class PlacedSchematic {
    private final UUID uuid;
    private final Location spawnLocation;
    private final Vector2d chunkCoordinates;
    private final Vector2d chunkBlockPosition;
    private final Vector3d spawnPositionOffset;
    private final Set<Long> schematicBlockSet;

    public PlacedSchematic(
            UUID uuid,
            Vector2d chunkCoordinates,
            Vector3d spawnPositionOffset,
            World world,
            Set<Long> schematicBlockSet
    ) {
        this.uuid = uuid;
        this.chunkCoordinates = chunkCoordinates;
        this.spawnPositionOffset = spawnPositionOffset;
        this.chunkBlockPosition = new Vector2d(16 * chunkCoordinates.x, 16 * chunkCoordinates.y);
        this.spawnLocation = new Location(
                world,
                this.chunkBlockPosition.x + spawnPositionOffset.x,
                spawnPositionOffset.y,
                this.chunkBlockPosition.y + spawnPositionOffset.z,
                180, 0
        );
        this.schematicBlockSet = schematicBlockSet;
    }

    public boolean blockInSchematic(Block block) {
        // Shift block position to schematic block position
        int blockSchematicX = block.getX() - (int) chunkBlockPosition.x;
        int blockSchematicZ = block.getZ() - (int) chunkBlockPosition.y;
        int blockSchematicY = block.getY() - (int) spawnPositionOffset.y;

        if (
                blockSchematicX < 0
                        || blockSchematicX >= 16
                        || blockSchematicZ < 0
                        || blockSchematicZ >= 16
                        || blockSchematicY < 0
        ) {
            return false;
        }

        return schematicBlockSet.contains(
                SchematicManager.packCoordinates(blockSchematicX, blockSchematicY, blockSchematicZ)
        );
    }

    // GETTERS
    public UUID getUuid() {
        return uuid;
    }

    public Vector2d getChunkCoordinates() {
        return chunkCoordinates;
    }

    public Vector2d getChunkBlockPosition() {
        return chunkBlockPosition;
    }

    public Vector3d getSpawnPositionOffset() {
        return spawnPositionOffset;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }
}
