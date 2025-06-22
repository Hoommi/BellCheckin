package org.hommi.bellCheckin.sqlite;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.hommi.bellCheckin.BellCheckin;

public class SQLiteManager implements AutoCloseable {
    private final String dbPath;
    private Connection connection;

    public SQLiteManager() {
        File dataFolder = BellCheckin.getInstance().getDataFolder();
        this.dbPath = new File(dataFolder, "bellcheckin.db").getAbsolutePath();
    }

    public void connect() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            // Bật các cài đặt tối ưu cho SQLite
            try (var stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
                stmt.execute("PRAGMA journal_mode = WAL");
                stmt.execute("PRAGMA synchronous = NORMAL");
            }
        }
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
        }
        return connection;
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                BellCheckin.getInstance().getLogger().severe("Lỗi khi đóng kết nối database: " + e.getMessage());
            }
        }
    }

    public void createCheckinTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS checkin (" +
                "userUuid TEXT PRIMARY KEY," +
                "lastCheckin INTEGER," +
                "streak INTEGER DEFAULT 0," +
                "lastStreakReward INTEGER DEFAULT 0" +
                ")";
        try (var stmt = getConnection().createStatement()) {
            stmt.execute(sql);
        }
    }

    public void createBellLocationTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS bell_locations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "worldName TEXT NOT NULL," +
                "x INTEGER NOT NULL," +
                "y INTEGER NOT NULL," +
                "z INTEGER NOT NULL" +
                ")";
        try (var stmt = getConnection().createStatement()) {
            stmt.execute(sql);
        }
    }

    @Override
    public void close() throws Exception {
        disconnect();
    }
}
