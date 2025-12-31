package com.mogrul.prison.manager;

import com.mogrul.prison.model.Cell;
import com.mogrul.prison.record.Schematic;
import com.mogrul.prison.record.SchematicBlock;
import com.mogrul.prison.util.SqlUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.IOException;
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

    public static String cellSchematicId;
    public static Schematic cellSchematic;
    public static List<SchematicBlock> cellSchematicBlocks;
    public static Set<Long> cellSchematicBlocksSet;

    public static void init(Logger logger, Connection connection) {
        CellManager.logger = logger;
        CellManager.connection = connection;

        String schematicName = "default_cell";

        try {
            String schematicPathString = "schematic/" + schematicName + ".schem";

            cellSchematic = SchematicManager.loadFromResource(schematicPathString);
            cellSchematicId = SchematicManager.computeSchematicId(cellSchematic);

            if (SchematicManager.schematicExists(cellSchematicId)) {
                cellSchematicBlocks = SchematicManager.getSchematicBlocksFromDB(cellSchematicId);
            } else {
                cellSchematicBlocks = SchematicManager.getSchematicBlocks(schematicPathString);

                SchematicManager.insertSchematic(schematicName, cellSchematic, cellSchematicBlocks);

                logger.info(
                        "Added schematic " +
                                schematicName +
                                " with " +
                                (long) cellSchematicBlocks.size() +
                                " blocks to database."
                );
            }

            cellSchematicBlocksSet = SchematicManager.getSchematicBlocksSet(cellSchematicBlocks);

            logger.info("Loaded schematic: " + schematicPathString);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load cell schematic: " + e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Cell getFreeCell() {
        int chunksPerRow = 1000;
        int playerCount = PrisonerManager.getPrisonerCount();
        int chunkX = playerCount % chunksPerRow;
        int chunkZ = playerCount / chunksPerRow;

        return new Cell(UUID.randomUUID(), chunkX, chunkZ);
    }

    public static void insertCell(Cell cell) {
        String sql = SqlUtil.get("cell/insert_cell.sql");

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cell.getUuid().toString());
            stmt.setInt(2, cell.getChunkX());
            stmt.setInt(3, cell.getChunkZ());

            stmt.executeUpdate();

            logger.info("Added cell " + cell.getUuid().toString() + " to database");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void generateCell(Cell cell) {
        World world = WorldManager.cellsWorld;
        int chunkBlockX = cell.getChunkBlockX();
        int chunkBlockZ = cell.getChunkBlockZ();

        SchematicManager.pasteAt(
                world,
                chunkBlockX, 64, chunkBlockZ,
                cellSchematic, true
        );
        logger.info("Generated cell (" + cell.getUuid().toString() + ") at chunk (" + cell.getChunkX() + ", " + cell.getChunkZ() + ")");
    }
}
