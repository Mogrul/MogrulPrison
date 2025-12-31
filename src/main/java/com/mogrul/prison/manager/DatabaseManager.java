package com.mogrul.prison.manager;

import com.mogrul.prison.MogrulPrison;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class DatabaseManager {
    public static Connection connection;
    private static MogrulPrison plugin;
    private static Logger logger;
    private static File databaseFile;

    public static void init(MogrulPrison plugin) {
        DatabaseManager.plugin = plugin;
        logger = plugin.getLogger();
        databaseFile = new File(plugin.getDataFolder() + File.separator + "database.db");
        connect();
        applySchema();
    }

    private static void connect() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getPath());
            connection.createStatement().execute("PRAGMA foreign_keys=ON");

            logger.info("Connected to database: " + databaseFile.getPath());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void applySchema() {
        try (InputStream inputStream = plugin.getResource("sql/schema.sql")) {
            if (inputStream == null) throw new IOException("Missing resource schema.sql");

            String sql = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            for (String stmt : sql.split(";")) {
                String s = stmt.trim();
                if (s.isEmpty()) continue;
                try (Statement st = connection.createStatement()) {
                    st.execute(s);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }

            logger.info("Applied schema to database: " + databaseFile.getPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
