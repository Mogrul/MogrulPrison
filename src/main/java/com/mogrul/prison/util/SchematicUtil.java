package com.mogrul.prison.util;

import com.mogrul.prison.MogrulPrison;
import com.mogrul.prison.manager.SchematicManager;
import com.mogrul.prison.record.Schematic;
import com.mogrul.prison.record.SchematicBlock;
import com.mogrul.prison.record.SchematicInit;
import org.apache.maven.artifact.repository.metadata.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class SchematicUtil {
    public static SchematicInit getSchematicInit(String schematicName) {
        Logger logger = JavaPlugin.getPlugin(MogrulPrison.class).getLogger();

        try {
            String schematicPathString = "schematic/" + schematicName + ".schem";

            Schematic schematic = SchematicManager.loadFromResource(schematicPathString);
            String schematicId = SchematicManager.computeSchematicId(schematic);

            List<SchematicBlock> schematicBlocks;

            if (SchematicManager.schematicExists(schematicId)) {
                schematicBlocks = SchematicManager.getSchematicBlocksFromDB(schematicId);
            } else {
                schematicBlocks = SchematicManager.getSchematicBlocks(schematicPathString);

                SchematicManager.insertSchematic(schematicName, schematic, schematicBlocks);

                logger.info(
                        "Added schematic " +
                                schematicName +
                                " with " +
                                (long) schematicBlocks.size() +
                                " blocks to database."
                );
            }

            Set<Long> schematicBlocksSet = SchematicManager.getSchematicBlocksSet(schematicBlocks);

            logger.info("Loaded schematic: " + schematicPathString);

            return new SchematicInit(
                    schematic,
                    schematicId,
                    schematicBlocks,
                    schematicBlocksSet
            );
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
