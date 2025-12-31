package com.mogrul.prison.manager;

import com.mogrul.prison.MogrulPrison;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.*;
import org.bukkit.generator.ChunkGenerator;
import org.joml.Vector3d;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class WorldManager {
    private static final List<String> REQUIRED_WORLD_NAMES = List.of(
            "world_cells", "world_prison", "world_mines"
    );
    private static Map<String, World> CURRENT_WORLDS;
    private static Logger logger;

    public static World cellsWorld;
    public static World prisonWorld;
    public static World minesWorld;

    public static void init(MogrulPrison plugin) {
        CURRENT_WORLDS = getCurrentWorlds();
        logger = plugin.getLogger();

        initWorlds();
    }

    private static Map<String, World> getCurrentWorlds() {
        return Bukkit.getWorlds().stream()
                .collect(Collectors.toMap(
                                World::getName,
                                Function.identity(),
                                (a, b) -> a
                        ));
    }

    private static void initWorlds() {
        for (String worldName : REQUIRED_WORLD_NAMES) {
            World world = generateVoidWorld(worldName);
            setWorldViewDistance(world);

            world.setSpawnFlags(false, false);

            world.setGameRule(GameRules.KEEP_INVENTORY, true);
            world.setGameRule(GameRules.SHOW_ADVANCEMENT_MESSAGES, false);
            world.setGameRule(GameRules.SPAWN_MOBS, false);
            world.setGameRule(GameRules.MOB_GRIEFING, false);
            world.setGameRule(GameRules.FIRE_DAMAGE, false);


            if (Objects.equals(worldName, "world_prison") &&
                !CURRENT_WORLDS.containsKey(worldName)
            ) {
                generateWorldPlatform(world, 5, 5);
            }


            switch (worldName) {
                case "world_cells" -> cellsWorld = world;
                case "world_prison" -> prisonWorld = world;
                case "world_mines" -> minesWorld = world;
                default -> { }
            }
        }
    }

    private static World generateVoidWorld(String worldName) {
        if (CURRENT_WORLDS.containsKey(worldName)) return CURRENT_WORLDS.get(worldName);

        WorldCreator worldCreator = new WorldCreator(worldName);
        worldCreator.generator(new ChunkGenerator() {});
        return worldCreator.createWorld();
    }

    private static void setWorldViewDistance(World world) {
        world.setViewDistance(ConfigManager.getInt(world.getName() + "-view-distance"));
    }

    private static void generateWorldPlatform(World world, int length, int width) {
        int startX = world.getSpawnLocation().getBlockX() - width / 2;
        int startZ = world.getSpawnLocation().getBlockZ() - length / 2;
        int startY = world.getSpawnLocation().getBlockY() - 1;

        // Place blocks
        for (int z = 0; z < length; z++) {
            for (int x = 0; x < width; x++) {
                Location location = new Location(world, startX + x, startY, startZ + z);
                var BlockData = Bukkit.createBlockData(Material.STONE_BRICKS);
                world.getBlockAt(location).setBlockData(BlockData, false);
            }
        }

        logger.info("Generated platform for world: " + world.getName());
    }
}
