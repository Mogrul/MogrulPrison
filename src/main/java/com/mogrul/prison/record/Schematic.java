package com.mogrul.prison.record;

public record Schematic(
        int width,
        int height,
        int length,
        String[] paletteById,
        byte[] blockData
) {
}
