package com.mogrul.prison.manager;

import com.mogrul.prison.record.Schematic;
import com.mogrul.prison.record.SchematicBlock;
import com.mogrul.prison.record.SchematicInit;
import com.mogrul.prison.util.SchematicUtil;
import org.joml.Vector3d;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class MineMangager {
    private static Connection connection;
    private static Logger logger;

    public static String schematicId;
    public static Schematic schematic;
    public static List<SchematicBlock> schematicBlocks;
    public static Set<Long> schematicBlocksSet;

    private static final Vector3d mineAreaStart = new Vector3d(4, 63, 4);
    private static final Vector3d mineAreaEnd = new Vector3d(11, -61, 11);

    public static void init(Logger logger, Connection connection) {
        MineMangager.logger = logger;
        MineMangager.connection = connection;

        String schematicName = "default_mine";

        SchematicInit schematicInit = SchematicUtil.getSchematicInit(schematicName);
        schematicId = schematicInit.schematicId();
        schematic = schematicInit.schematic();
        schematicBlocks = schematicInit.schematicBlocks();
        schematicBlocksSet = schematicInit.schematicBlocksSet();
    }
}
