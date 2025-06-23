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
     * Khởi tạo và cập nhật database theo phiên bản plugin
     * 
     * @param targetVersion Phiên bản database mục tiêu
     */
    public void initializeDatabaseForPluginVersion(String targetVersion) {
        try {
            connect();

            // Khởi tạo Liquibase migration manager nếu chưa có
            if (migrationManager == null) {
                migrationManager = new LiquibaseMigrationManager(BellCheckin.getInstance(), this);
            }

            // Lấy phiên bản hiện tại của database
            String currentVersion = migrationManager.getCurrentVersion();

            // So sánh phiên bản hiện tại với phiên bản mục tiêu
            if (compareVersions(currentVersion, targetVersion) < 0) {
                // Nếu phiên bản hiện tại nhỏ hơn phiên bản mục tiêu, thực hiện cập nhật
                BellCheckin.getInstance().getLogger()
                        .info("Đang nâng cấp database từ phiên bản " + currentVersion + " lên " + targetVersion);
                migrationManager.updateToVersion(targetVersion);
            } else if (compareVersions(currentVersion, targetVersion) > 0) {
                // Nếu phiên bản hiện tại lớn hơn phiên bản mục tiêu, thực hiện rollback
                BellCheckin.getInstance().getLogger()
                        .info("Đang hạ cấp database từ phiên bản " + currentVersion + " xuống " + targetVersion);
                migrationManager.rollbackToVersion(targetVersion);
            } else {
                // Nếu phiên bản hiện tại bằng phiên bản mục tiêu, không làm gì cả
                BellCheckin.getInstance().getLogger()
                        .info("Database đã ở phiên bản " + targetVersion + ", không cần cập nhật");
            }
        } catch (SQLException e) {
            BellCheckin.getInstance().getLogger()
                    .severe("Lỗi khi khởi tạo database cho phiên bản " + targetVersion + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * So sánh hai chuỗi phiên bản
     * 
     * @param version1 Phiên bản thứ nhất
     * @param version2 Phiên bản thứ hai
     * @return -1 nếu version1 < version2, 0 nếu version1 = version2, 1 nếu version1
     *         > version2
     */
    private int compareVersions(String version1, String version2) {
        if (version1 == null || version1.equals("unknown"))
            return -1;
        if (version2 == null)
            return 1;

        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");

        int length = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < length; i++) {
            int v1 = (i < parts1.length) ? Integer.parseInt(parts1[i]) : 0;
            int v2 = (i < parts2.length) ? Integer.parseInt(parts2[i]) : 0;

            if (v1 < v2)
                return -1;
            if (v1 > v2)
                return 1;
        }

        return 0;
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
