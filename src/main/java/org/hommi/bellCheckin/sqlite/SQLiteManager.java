package org.hommi.bellCheckin.sqlite;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
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

    /**
     * Khởi tạo và cập nhật database
     */
    public void initializeDatabase() {
        try {
            connect();
            createTables();
            BellCheckin.getInstance().getLogger().info("Đã khởi tạo database thành công!");
        } catch (SQLException e) {
            BellCheckin.getInstance().getLogger().severe("Lỗi khi khởi tạo database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Khởi tạo và cập nhật database theo phiên bản plugin
     * 
     * @param targetVersion Phiên bản database mục tiêu
     */
    public void initializeDatabaseForPluginVersion(String targetVersion) {
        try {
            connect();
            createTables();
            BellCheckin.getInstance().getLogger().info("Đã khởi tạo database cho phiên bản " + targetVersion);
        } catch (SQLException e) {
            BellCheckin.getInstance().getLogger()
                    .severe("Lỗi khi khởi tạo database cho phiên bản " + targetVersion + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tạo các bảng cần thiết trong database
     */
    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Tạo bảng checkin
            stmt.execute("CREATE TABLE IF NOT EXISTS checkin (" +
                    "userUuid TEXT PRIMARY KEY NOT NULL, " +
                    "lastCheckin INTEGER NOT NULL DEFAULT 0, " +
                    "streak INTEGER NOT NULL DEFAULT 0, " +
                    "lastStreakReward INTEGER NOT NULL DEFAULT 0)");

            // Tạo bảng bell_locations
            stmt.execute("CREATE TABLE IF NOT EXISTS bell_locations (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "worldName TEXT NOT NULL, " +
                    "x INTEGER NOT NULL, " +
                    "y INTEGER NOT NULL, " +
                    "z INTEGER NOT NULL)");
        }
    }

    /**
     * Tạo bảng checkin nếu chưa tồn tại
     * 
     * @deprecated Sử dụng initializeDatabase() thay thế
     */
    @Deprecated
    public void createCheckinTable() throws SQLException {
        initializeDatabase();
    }

    /**
     * Tạo bảng bell_locations nếu chưa tồn tại
     * 
     * @deprecated Sử dụng initializeDatabase() thay thế
     */
    @Deprecated
    public void createBellLocationTable() throws SQLException {
        initializeDatabase();
    }

    @Override
    public void close() throws Exception {
        disconnect();
    }
}
