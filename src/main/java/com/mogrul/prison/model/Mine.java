package com.mogrul.prison.model;

import org.bukkit.World;
import org.joml.Vector2d;
import org.joml.Vector3d;

import java.util.Set;
import java.util.UUID;

public class Mine extends PlacedSchematic {
    private boolean isClaimed;

    public Mine(
            UUID uuid,
            Vector2d chunkCoordinates,
            Vector3d spawnPositionOffset,
            World world,
            Set<Long> schematicBlockSet,
            Boolean isClaimed
    ) {
        super(uuid, chunkCoordinates, spawnPositionOffset, world, schematicBlockSet);
        this.isClaimed = isClaimed;
    }

    // GETTERS
    public boolean isClaimed() {
        return isClaimed;
    }
}
