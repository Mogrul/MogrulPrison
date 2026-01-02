package com.mogrul.prison.manager;

import com.mogrul.prison.model.Cell;
import com.mogrul.prison.model.PlacedSchematic;
import com.mogrul.prison.record.Schematic;
import com.mogrul.prison.record.SchematicBlock;
import com.mogrul.prison.record.SchematicInit;
import com.mogrul.prison.util.SchematicUtil;
import com.mogrul.prison.util.SqlUtil;
import org.bukkit.World;
import org.joml.Vector2d;
import org.joml.Vector3d;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

public class CellManager {
    private static Connection connection;
    private static Logger logger;

    public static final int cellSpawnY = 65;
    public static String schematicId;
    public static Schematic schematic;
    public static List<SchematicBlock> schematicBlocks;
    public static Set<Long> schematicBlocksSet;

    public static void init(Logger logger, Connection connection) {
        CellManager.logger = logger;
        CellManager.connection = connection;

        String schematicName = "default_cell";

        SchematicInit schematicInit = SchematicUtil.getSchematicInit(schematicName);
        schematicId = schematicInit.schematicId();
        schematic = schematicInit.schematic();
        schematicBlocks = schematicInit.schematicBlocks();
        schematicBlocksSet = schematicInit.schematicBlocksSet();
    }

    public static Cell getFreeCell() {
        int chunksPerRow = 1000;
        int playerCount = PrisonerManager.getPrisonerCount();
        int chunkX = playerCount % chunksPerRow;
        int chunkZ = playerCount / chunksPerRow;

        return new Cell(
                UUID.randomUUID(),
                new Vector2d(chunkX, chunkZ),
                new Vector3d(chunkX + 8.5, cellSpawnY,  chunkZ + 8.5),
                WorldManager.cellsWorld,
                schematicBlocksSet
        );
    }

    public static void insertCell(Cell cell) {
        String sql = SqlUtil.get("cell/insert_cell.sql");

        Vector2d cellChunkCoordinates = cell.getChunkCoordinates();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cell.getUuid().toString());
            stmt.setInt(2, (int) cellChunkCoordinates.x);
            stmt.setInt(3, (int) cellChunkCoordinates.y);

            stmt.executeUpdate();

            logger.info("Added cell " + cell.getUuid().toString() + " to database");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void generateCell(Cell cell) {
        World world = WorldManager.cellsWorld;
        Vector2d cellChunkCoordinates = cell.getChunkCoordinates();
        Vector3d cellSpawnPositionOffset = cell.getSpawnPositionOffset();

        SchematicManager.pasteAt(
                world,
                (int) cellChunkCoordinates.x, (int) cellSpawnPositionOffset.y - 2, (int) cellChunkCoordinates.y,
                schematic, true
        );
        logger.info(
                "Generated cell (" + cell.getUuid().toString() + ") " +
                        "at chunk (" + cellChunkCoordinates.x + ", " + cellChunkCoordinates.y + ")"
        );
    }
}
