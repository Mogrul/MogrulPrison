package com.mogrul.prison.record;

import java.util.List;
import java.util.Set;

public record SchematicInit(
        Schematic schematic,
        String schematicId,
        List<SchematicBlock> schematicBlocks,
        Set<Long> schematicBlocksSet
) {
}
