package org.hommi.bellCheckin.sqlite;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.hommi.bellCheckin.BellCheckin;

public class SQLiteManager implements AutoCloseable {
    private final String dbPath;
    private Connection connection;
    private LiquibaseMigrationManager migrationManager;

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
     * Khởi tạo và cập nhật database sử dụng Liquibase
     */
    public void initializeDatabase() {
        try {
            connect();

            // Khởi tạo Liquibase migration manager nếu chưa có
            if (migrationManager == null) {
                migrationManager = new LiquibaseMigrationManager(BellCheckin.getInstance(), this);
            }

            // Cập nhật database lên phiên bản mới nhất
            migrationManager.updateToLatest();
        } catch (SQLException e) {
            BellCheckin.getInstance().getLogger().severe("Lỗi khi khởi tạo database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cập nhật database đến một phiên bản cụ thể
     * 
     * @param targetVersion Phiên bản đích
     */
    public void updateDatabaseToVersion(String targetVersion) {
        try {
            connect();

            if (migrationManager == null) {
                migrationManager = new LiquibaseMigrationManager(BellCheckin.getInstance(), this);
            }

            migrationManager.updateToVersion(targetVersion);
        } catch (SQLException e) {
            BellCheckin.getInstance().getLogger()
                    .severe("Lỗi khi cập nhật database đến phiên bản " + targetVersion + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Rollback database đến một phiên bản cụ thể
     * 
     * @param targetVersion Phiên bản đích
     */
    public void rollbackDatabaseToVersion(String targetVersion) {
        try {
            connect();

            if (migrationManager == null) {
                migrationManager = new LiquibaseMigrationManager(BellCheckin.getInstance(), this);
            }

            migrationManager.rollbackToVersion(targetVersion);
        } catch (SQLException e) {
            BellCheckin.getInstance().getLogger()
                    .severe("Lỗi khi rollback database đến phiên bản " + targetVersion + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lấy phiên bản hiện tại của database
     * 
     * @return Phiên bản hiện tại
     */
    public String getDatabaseVersion() {
        try {
            connect();

            if (migrationManager == null) {
                migrationManager = new LiquibaseMigrationManager(BellCheckin.getInstance(), this);
            }

            return migrationManager.getCurrentVersion();
        } catch (SQLException e) {
            BellCheckin.getInstance().getLogger().severe("Lỗi khi lấy phiên bản database: " + e.getMessage());
            e.printStackTrace();
            return "unknown";
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
