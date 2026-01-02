package com.mogrul.prison.manager;

import com.mogrul.prison.MogrulPrison;
import com.mogrul.prison.model.Cell;
import com.mogrul.prison.model.Prisoner;
import com.mogrul.prison.util.SqlUtil;
import com.mogrul.prison.util.TimeUtil;
import org.bukkit.entity.Player;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

public class PrisonerManager {
    private static Connection connection;
    private static Logger logger;

    public static Map<UUID, Prisoner> onlinePrisoners = new HashMap<>();

    public static void init(Logger logger, Connection connection) {
        PrisonerManager.logger = logger;
        PrisonerManager.connection = connection;
    }

    public static Prisoner createPrisoner(Player player) {
        Random random = new Random();
        List<String> sentences = ConfigManager.getStringList("prison-sentences");
        String sentence = sentences.get(random.nextInt(sentences.size()));
        Long now = System.currentTimeMillis();
        Cell cell = CellManager.getFreeCell();

        Prisoner prisoner = new Prisoner(
                player.getUniqueId(),
                player.getName(),
                sentence,
                ConfigManager.getInt("starting-contraband"),
                TimeUtil.getTimeFromString(now),
                TimeUtil.getTimeFromString(now),
                cell
        );

        insertPrisoner(prisoner);
        CellManager.generateCell(prisoner.getCell());

        return prisoner;
    }

    public static List<String> getOnlinePlayerNames() {
        return onlinePrisoners.values()
                .stream()
                .map(Prisoner::getUsername)
                .toList();
    }

    // SQL FUNCTIONS
    public static Prisoner getPrisoner(UUID uuid) {
        String sql = SqlUtil.get("prisoner/get_prisoner.sql");

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;

                UUID prisonerUUID = UUID.fromString(rs.getString("prisoner_uuid"));
                UUID cellUUID = UUID.fromString(rs.getString("cell_uuid"));

                String username = rs.getString("username");
                String sentence = rs.getString("sentence");

                int contraband =  rs.getInt("contraband");
                int chunkX = rs.getInt("chunk_x");
                int chunkZ = rs.getInt("chunk_z");

                LocalDateTime firstJoin = TimeUtil.getTimeFromString(rs.getLong("first_join"));
                LocalDateTime lastJoin = TimeUtil.getTimeFromString(rs.getLong("last_join"));

                Cell cell = new Cell(cellUUID, chunkX, chunkZ);

                return new Prisoner(
                        prisonerUUID, username, sentence,
                        contraband, firstJoin,
                        lastJoin, cell
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getPrisonerCount() {
        String sql = "SELECT COUNT(*) FROM `prisoner`";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't get player count from database: " + e.getMessage());
        }
    }

    public static void insertPrisoner(Prisoner prisoner) {
        // Insert cell before adding a prisoner.
        CellManager.insertCell(prisoner.getCell());

        String sql = SqlUtil.get("prisoner/insert_prisoner.sql");

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, prisoner.getUuid().toString());
            stmt.setString(2, prisoner.getUsername());
            stmt.setInt(3, prisoner.getContraband());
            stmt.setLong(4, TimeUtil.getLongFromTime(prisoner.getFirstJoin()));
            stmt.setLong(5, TimeUtil.getLongFromTime(prisoner.getLastJoin()));
            stmt.setString(6, prisoner.getSentence());
            stmt.setString(7, prisoner.getCell().getUuid().toString());

            stmt.executeUpdate();

            logger.info("Added prisoner " + prisoner.getUsername() + " to database.");

        } catch (SQLException e) {
            throw new RuntimeException("Couldn't insert prisoner " + prisoner.getUuid() + " to database: " + e.getMessage());
        }
    }

    public static void updatePrisoner(Prisoner prisoner) {
        String sql = SqlUtil.get("prisoner/update_prisoner.sql");

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, prisoner.getUsername());
            stmt.setLong(2, TimeUtil.getLongFromTime(prisoner.getLastJoin()));
            stmt.setInt(3, prisoner.getContraband());

            stmt.setString(4, prisoner.getUuid().toString());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Couldn't update player " + prisoner.getUsername() + " from database: " + e.getMessage());
        }
    }
}
