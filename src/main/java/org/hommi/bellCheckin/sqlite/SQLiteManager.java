package org.hommi.bellCheckin.sqlite;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.hommi.bellCheckin.BellCheckin;

public class SQLiteManager implements AutoCloseable {
    private final String dbPath;
    private Connection connection;
    private DatabaseVersionManager versionManager;

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

            // Khởi tạo quản lý phiên bản nếu chưa có
            if (versionManager == null) {
                versionManager = new DatabaseVersionManager(BellCheckin.getInstance(), this);
            }

            // Cập nhật database lên phiên bản mới nhất
            versionManager.updateToLatest();
        } catch (SQLException e) {
            BellCheckin.getInstance().getLogger().severe("Lỗi khi khởi tạo database: " + e.getMessage());
            e.printStackTrace();
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
