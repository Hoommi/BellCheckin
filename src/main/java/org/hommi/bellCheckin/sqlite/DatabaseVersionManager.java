package org.hommi.bellCheckin.sqlite;

import org.hommi.bellCheckin.BellCheckin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Quản lý phiên bản database và xử lý việc nâng cấp/hạ cấp
 */
public class DatabaseVersionManager {
    private final BellCheckin plugin;
    private final SQLiteManager sqliteManager;
    private final Map<Integer, DatabaseMigration> migrations = new HashMap<>();
    private int currentVersion = 0;
    private int latestVersion = 2; // Phiên bản database mới nhất

    public DatabaseVersionManager(BellCheckin plugin, SQLiteManager sqliteManager) {
        this.plugin = plugin;
        this.sqliteManager = sqliteManager;
        registerMigrations();
    }

    /**
     * Đăng ký các migration giữa các phiên bản
     */
    private void registerMigrations() {
        // Migration từ v1 lên v2
        migrations.put(1, new DatabaseMigration(1, 2,
                // Up migration
                connection -> {
                    // Thêm các cột mới vào bảng checkin
                    plugin.getLogger().info("Nâng cấp database từ v1 lên v2...");
                    try (var stmt = connection.createStatement()) {
                        // Tạo bảng tạm để lưu dữ liệu
                        stmt.execute(
                                "CREATE TABLE checkin_temp (userUuid TEXT PRIMARY KEY, lastCheckin INTEGER, streak INTEGER DEFAULT 0, lastStreakReward INTEGER DEFAULT 0)");

                        // Sao chép dữ liệu từ bảng cũ sang bảng tạm
                        stmt.execute(
                                "INSERT INTO checkin_temp (userUuid, lastCheckin) SELECT userUuid, lastCheckin FROM checkin");

                        // Xóa bảng cũ
                        stmt.execute("DROP TABLE checkin");

                        // Đổi tên bảng tạm thành bảng chính
                        stmt.execute("ALTER TABLE checkin_temp RENAME TO checkin");

                        // Tạo bảng bell_locations nếu chưa tồn tại
                        stmt.execute(
                                "CREATE TABLE IF NOT EXISTS bell_locations (id INTEGER PRIMARY KEY AUTOINCREMENT, worldName TEXT NOT NULL, x INTEGER NOT NULL, y INTEGER NOT NULL, z INTEGER NOT NULL)");
                    }
                    plugin.getLogger().info("Nâng cấp database thành công!");
                },
                // Down migration
                connection -> {
                    // Quay lại phiên bản v1
                    plugin.getLogger().info("Hạ cấp database từ v2 xuống v1...");
                    try (var stmt = connection.createStatement()) {
                        // Tạo bảng tạm để lưu dữ liệu
                        stmt.execute("CREATE TABLE checkin_temp (userUuid TEXT PRIMARY KEY, lastCheckin INTEGER)");

                        // Sao chép dữ liệu từ bảng hiện tại sang bảng tạm
                        stmt.execute(
                                "INSERT INTO checkin_temp (userUuid, lastCheckin) SELECT userUuid, lastCheckin FROM checkin");

                        // Xóa bảng hiện tại
                        stmt.execute("DROP TABLE checkin");

                        // Đổi tên bảng tạm thành bảng chính
                        stmt.execute("ALTER TABLE checkin_temp RENAME TO checkin");

                        // Giữ lại bảng bell_locations nếu có
                    }
                    plugin.getLogger().info("Hạ cấp database thành công!");
                }));

        // Migration từ v0 (không có bảng version) lên v1
        migrations.put(0, new DatabaseMigration(0, 1,
                // Up migration
                connection -> {
                    plugin.getLogger().info("Khởi tạo database v1...");
                    try (var stmt = connection.createStatement()) {
                        // Tạo bảng checkin cơ bản
                        stmt.execute(
                                "CREATE TABLE IF NOT EXISTS checkin (userUuid TEXT PRIMARY KEY, lastCheckin INTEGER)");
                    }
                    plugin.getLogger().info("Khởi tạo database thành công!");
                },
                // Down migration - không cần thiết vì đây là phiên bản đầu tiên
                connection -> {
                }));
    }

    /**
     * Kiểm tra và tạo bảng version nếu cần
     */
    private void ensureVersionTableExists() throws SQLException {
        try (var stmt = sqliteManager.getConnection().createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS db_version (version INTEGER PRIMARY KEY)");

            // Kiểm tra xem bảng có dữ liệu không
            try (ResultSet rs = stmt.executeQuery("SELECT version FROM db_version")) {
                if (!rs.next()) {
                    // Xác định phiên bản hiện tại dựa trên cấu trúc bảng
                    currentVersion = detectDatabaseVersion();
                    stmt.execute("INSERT INTO db_version (version) VALUES (" + currentVersion + ")");
                } else {
                    currentVersion = rs.getInt("version");
                }
            }
        }
    }

    /**
     * Phát hiện phiên bản database dựa trên cấu trúc bảng
     */
    private int detectDatabaseVersion() throws SQLException {
        try (var stmt = sqliteManager.getConnection().createStatement()) {
            // Kiểm tra xem bảng checkin có tồn tại không
            try (ResultSet rs = stmt
                    .executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='checkin'")) {
                if (!rs.next()) {
                    return 0; // Chưa có bảng checkin, phiên bản 0
                }
            }

            // Kiểm tra cấu trúc bảng checkin
            try (ResultSet rs = stmt.executeQuery("PRAGMA table_info(checkin)")) {
                List<String> columns = new ArrayList<>();
                while (rs.next()) {
                    columns.add(rs.getString("name"));
                }

                if (columns.contains("streak") && columns.contains("lastStreakReward")) {
                    return 2; // Phiên bản 2 với các cột streak
                } else {
                    return 1; // Phiên bản 1 chỉ có cột cơ bản
                }
            }
        }
    }

    /**
     * Cập nhật database lên phiên bản mới nhất
     */
    public void updateToLatest() {
        try {
            sqliteManager.connect();
            ensureVersionTableExists();

            plugin.getLogger().info("Phiên bản database hiện tại: " + currentVersion);
            plugin.getLogger().info("Phiên bản database mới nhất: " + latestVersion);

            if (currentVersion < latestVersion) {
                // Nâng cấp lên từng phiên bản một
                for (int version = currentVersion; version < latestVersion; version++) {
                    DatabaseMigration migration = migrations.get(version);
                    if (migration != null) {
                        migration.up(sqliteManager.getConnection());
                        updateVersion(version + 1);
                    }
                }
            } else if (currentVersion > latestVersion) {
                // Hạ cấp xuống từng phiên bản một
                for (int version = currentVersion; version > latestVersion; version--) {
                    DatabaseMigration migration = migrations.get(version - 1);
                    if (migration != null) {
                        migration.down(sqliteManager.getConnection());
                        updateVersion(version - 1);
                    }
                }
            } else {
                plugin.getLogger().info("Database đã ở phiên bản mới nhất!");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Lỗi khi cập nhật database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cập nhật phiên bản trong database
     */
    private void updateVersion(int version) throws SQLException {
        try (var stmt = sqliteManager.getConnection().createStatement()) {
            stmt.execute("UPDATE db_version SET version = " + version);
            currentVersion = version;
            plugin.getLogger().info("Đã cập nhật database lên phiên bản " + version);
        }
    }

    /**
     * Lấy phiên bản hiện tại của database
     */
    public int getCurrentVersion() {
        return currentVersion;
    }

    /**
     * Lấy phiên bản mới nhất của database
     */
    public int getLatestVersion() {
        return latestVersion;
    }
}