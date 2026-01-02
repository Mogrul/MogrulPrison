package com.mogrul.prison.manager;

import com.flowpowered.nbt.ByteArrayTag;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.stream.NBTInputStream;
import com.mogrul.prison.MogrulPrison;
import com.mogrul.prison.record.Schematic;
import com.mogrul.prison.record.SchematicBlock;
import com.mogrul.prison.util.SqlUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

public class SchematicManager {
    public static MogrulPrison plugin;
    public static Logger logger;
    private static Connection connection;

    public static void init(MogrulPrison plugin, Connection connection) {
        SchematicManager.connection = connection;
        SchematicManager.plugin = plugin;
        SchematicManager.logger = plugin.getLogger();
    }

    // SQL
    public static String insertSchematic(
            String schematicName,
            Schematic schematic,
            List<SchematicBlock> blocks
    ) throws SQLException {
        String schematicId = computeSchematicId(schematic);
        long now = System.currentTimeMillis();

        String insertSchematicSql = SqlUtil.get("schematic/insert_schematic.sql");
        String insertSchematicBlockSql = SqlUtil.get("schematic/insert_schematic_block.sql");

        connection.setAutoCommit(false);
        try (
                PreparedStatement sStmt = connection.prepareStatement(insertSchematicSql);
                PreparedStatement bStmt = connection.prepareStatement(insertSchematicBlockSql)
        ) {
            sStmt.setString(1, schematicId);
            sStmt.setString(2, schematicName);
            sStmt.setInt(3, schematic.width());
            sStmt.setInt(4, schematic.height());
            sStmt.setInt(5, schematic.width());
            sStmt.setLong(6, now);

            sStmt.executeUpdate();

            // Blocks import
            for (SchematicBlock block : blocks) {
                bStmt.setString(1, schematicId);
                bStmt.setInt(2, block.x());
                bStmt.setInt(3, block.y());
                bStmt.setInt(4, block.z());
                bStmt.setString(5, block.blockState());

                bStmt.addBatch();
            }
            bStmt.executeBatch();

            connection.commit();
            return schematicId;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public static boolean schematicExists(String schematicId) {
        String sql = "SELECT 1 FROM `schematic` WHERE `id` = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, schematicId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<SchematicBlock> getSchematicBlocksFromDB(String schematicId) {
        String sql = SqlUtil.get("schematic/get_schematic_block.sql");

        List<SchematicBlock> out = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, schematicId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int x = rs.getInt("x");
                    int y = rs.getInt("y");
                    int z = rs.getInt("z");
                    String state = rs.getString("block_state");

                    out.add(new SchematicBlock(x, y, z, state));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return out;
    }

    public static Set<Long> getSchematicBlocksSet(List<SchematicBlock> schematicBlocks) {
        Set<Long> set = new HashSet<>(schematicBlocks.size() * 2);
        for (SchematicBlock block : schematicBlocks) {
            set.add(packCoordinates(block.x(), block.y(), block.z()));
        }

        return set;
    }

    public static long packCoordinates(int x, int y, int z) {
        return ((long) (x & 0xFFFFF) << 40)
                | ((long) (y & 0xFFFFF) << 20)
                |  (long) (z & 0xFFFFF);
    }

    // SCHEMATIC LOADING
    public static Schematic loadFromResource(String resourcePath) throws IOException {
        try (InputStream in = plugin.getResource(resourcePath)) {
            if (in == null) throw new FileNotFoundException("Missing resource " + resourcePath);
            return loadFromStream(in, resourcePath);
        }
    }

    public static Schematic loadFromFile(File file) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            return loadFromStream(in, file.getName());
        }
    }

    public static List<SchematicBlock> getSchematicBlocks(String resourcePath) throws IOException {
        try (InputStream in = plugin.getResource(resourcePath)) {
            if (in == null) throw new FileNotFoundException("Missing resource " + resourcePath);
            return loadSchematicBlocksFromStream(in, resourcePath, false);
        }
    }

    public static String computeSchematicId(Schematic schematic) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            md.update(intBytes(schematic.width()));
            md.update(intBytes(schematic.height()));
            md.update(intBytes(schematic.length()));

            String[] palette = schematic.paletteById();
            md.update(intBytes(palette.length));
            for (String s : palette) {
                if (s == null) s = "";
                byte[] str = s.getBytes(StandardCharsets.UTF_8);
                md.update(intBytes(str.length));
                md.update(str);
            }

            byte[] data = schematic.blockData();
            md.update(intBytes(data.length));
            md.update(data);

            return toHex(md.digest());

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * Paste schematic so its bounding box is centered within a chunk.
     *
     * @param world The world to paste the schematic in
     * @param chunkX The chunks X coordinate
     * @param chunkZ The chunks Z coordinate
     * @param y The base Y coordinate
     * @param schem The schematic to place
     * @param ignoreAir Whether to ignore air.
     */
    public static void pasteCenteredInChunk(
            World world,
            int chunkX,
            int chunkZ,
            int y,
            Schematic schem,
            boolean ignoreAir
    ) {
        int chunkMinX = chunkX << 4;
        int chunkMinZ = chunkZ << 4;

        int centerX = chunkMinX + 8;
        int centerZ = chunkMinZ + 8;

        int startX = centerX - (schem.width() / 2);
        int startZ = centerZ - (schem.length() / 2);

        pasteAt(world, startX, y, startZ, schem, ignoreAir);
    }

    /**
     *
     *  Pastes a schematic into a world using a given X, Y, Z
     *
     * @param world The world the schematic is placed in.
     * @param startX The starting X coordinate for the schematic.
     * @param startY The starting Y coordinate for the schematic.
     * @param startZ The starting Z coordinate for the schematic.
     * @param schem The schematic being placed.
     * @param ignoreAir Whether to ignore air.
     */
    public static void pasteAt(
            World world,
            int startX,
            int startY,
            int startZ,
            Schematic schem,
            boolean ignoreAir
    ) {
        int w = schem.width();
        int h = schem.height();
        int l = schem.length();

        byte[] data = schem.blockData();
        String[] paletteById = schem.paletteById();

        // Recursing through blocks to place.
        for (int yy = 0; yy < h; yy++) {
            for (int zz = 0; zz < l; zz++) {
                for (int xx = 0; xx < w; xx++) {
                    int index = yy * (l * w) + zz * w + xx;

                    int paletteId = data[index] & 0xFF;
                    if (paletteId >= paletteById.length) continue;

                    String state = paletteById[paletteId];
                    if (state == null) continue;

                    if (ignoreAir && (state.equals("minecraft:air") || state.equals("air"))) continue;

                    int bx = startX + xx;
                    int by = startY + yy;
                    int bz = startZ + zz;

                    Block block = world.getBlockAt(bx, by, bz);
                    // Paste using block data
                    BlockData bd = toBlockData(state);
                    if (bd != null) {
                        block.setBlockData(bd, false);
                    } else {
                        // Fallback to material
                        String base = state.split("\\[")[0];
                        base = base.replace("minecraft:", "");
                        Material mat = Material.matchMaterial(base);

                        if (mat != null) {
                            block.setType(mat, false);
                        }
                    }
                }
            }
        }
    }


    // INTERNAL LOADING / PARSING
    private static BlockData toBlockData(String schemState) {
        String s = schemState.replace("minecraft:", "");
        try {
            return Bukkit.createBlockData(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static List<SchematicBlock> loadSchematicBlocksFromStream(
            InputStream raw, String debugName, boolean includeAir
    ) throws IOException {
        try (NBTInputStream nbt = new NBTInputStream(raw)) {
            Tag<?> rootTag = nbt.readTag();

            if (!(rootTag instanceof CompoundTag root)) {
                throw new IOException("Invalid schematic root (not CompoundTag): " + debugName);
            }

            CompoundTag schematic = getCompound(root, "Schematic");

            int width = getNumber(schematic, "Width").intValue();
            int height = getNumber(schematic, "Height").intValue();
            int length = getNumber(schematic, "Length").intValue();

            CompoundTag blocks = getCompound(schematic, "Blocks");
            CompoundTag palette = getCompound(blocks, "Palette");
            byte[] data = getByteArray(blocks, "Data");

            String[] paletteById = buildPaletteById(palette);

            ArrayList<SchematicBlock> out = new ArrayList<>(width * height * length);
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    for (int x = 0; x < width; x++) {
                        int index = y * (length * width) + z * width + x;

                        int paletteId = data[index] & 0xFF;
                        if (paletteId >= paletteById.length) continue;

                        String state = paletteById[paletteId];
                        if (state == null) continue;

                        if (!includeAir && (state.equals("minecraft:air") || state.equals("air"))) {
                            continue;
                        }

                        // dx/dy/dz relative to schematic origin (0..width-1 etc)
                        out.add(new SchematicBlock(x, y, z, state));
                    }
                }
            }

            return out;
        }
    }

    private static Schematic loadFromStream(InputStream raw, String debugName) throws IOException {
        try (NBTInputStream nbt = new NBTInputStream(raw)) {
            Tag<?> rootTag = nbt.readTag();

            if (!(rootTag instanceof CompoundTag root)) {
                throw new IOException("Invalid schematic root (not CompoundTag): " + debugName);
            }

            CompoundTag schematic = getCompound(root, "Schematic");

            // Dimensions
            int width = getNumber(schematic, "Width").intValue();
            int height = getNumber(schematic, "Height").intValue();
            int length =  getNumber(schematic, "Length").intValue();

            CompoundTag blocks = getCompound(schematic, "Blocks");
            CompoundTag palette = getCompound(blocks, "Palette");
            byte[] blockData = getByteArray(blocks, "Data");

            String[] paletteById = buildPaletteById(palette);

            return new Schematic(width, height, length, paletteById, blockData);
        }
    }

    private static CompoundTag getCompound(CompoundTag parent, String key) throws IOException {
        Tag<?> t = parent.getValue().get(key);
        if (t instanceof CompoundTag ct) return ct;
        throw new IOException("Missing/invalid CompoundTag '" + key + "'");
    }

    private static Number getNumber(CompoundTag parent, String key) throws IOException {
        Tag<?> t = parent.getValue().get(key);
        if (t == null) throw new IOException("Missing number tag: '" + key + "'");
        Object v = t.getValue();
        if (v instanceof Number number) return number;
        throw new IOException("Tag '" + key + "' is not numeric (" + v.getClass().getSimpleName() + ")");
    }

    private static byte[] getByteArray(CompoundTag parent, String key) throws IOException {
        Tag<?> t = parent.getValue().get(key);
        if (t instanceof ByteArrayTag bt) return bt.getValue();
        throw new IOException("Missing/invalid ByteArrayTag '" + key + "'");
    }

    private static String[] buildPaletteById(CompoundTag palette) throws IOException {
        Map<String, Tag<?>> map = palette.getValue();

        int maxId = -1;
        for (Map.Entry<String, Tag<?>> e : map.entrySet()) {
            int id = asInt(e.getValue());
            if (id > maxId) maxId = id;
        }

        if (maxId < 0) throw new IOException("Palette is empty");

        String[] byId = new String[maxId + 1];
        for (Map.Entry<String, Tag<?>> e : map.entrySet()) {
            String blockState = e.getKey();
            int id = asInt(e.getValue());
            if (id >= 0 && id < byId.length) byId[id] = blockState;
        }

        return byId;
    }

    private static int asInt(Tag<?> tag) throws IOException {
        Object v = tag.getValue();
        if (v instanceof Number n) return n.intValue();
        throw new IOException("Unsupported palette id tag type: " + tag.getClass().getSimpleName());
    }

    private static byte[] intBytes(int v) {
        return ByteBuffer.allocate(4).putInt(v).array();
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
