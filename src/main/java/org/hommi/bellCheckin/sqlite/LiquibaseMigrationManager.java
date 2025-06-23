package org.hommi.bellCheckin.sqlite;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.hommi.bellCheckin.BellCheckin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Quản lý migration database bằng Liquibase
 */
public class LiquibaseMigrationManager {
    private final BellCheckin plugin;
    private final SQLiteManager sqliteManager;
    private static final String CHANGELOG_MASTER = "changelog/db.changelog-master.yaml";

    public LiquibaseMigrationManager(BellCheckin plugin, SQLiteManager sqliteManager) {
        this.plugin = plugin;
        this.sqliteManager = sqliteManager;
    }

    /**
     * Thực hiện migration database lên phiên bản mới nhất
     */
    public void updateToLatest() {
        try {
            Connection connection = sqliteManager.getConnection();
            performUpdate(connection, null);
            plugin.getLogger().info("Đã cập nhật database thành công!");
        } catch (SQLException | LiquibaseException e) {
            plugin.getLogger().severe("Lỗi khi cập nhật database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cập nhật database đến một phiên bản cụ thể
     * 
     * @param targetTag Tag phiên bản đích
     */
    public void updateToVersion(String targetTag) {
        try {
            Connection connection = sqliteManager.getConnection();
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
            Liquibase liquibase = new Liquibase(CHANGELOG_MASTER, resourceAccessor, database);

            // Cập nhật đến tag phiên bản cụ thể
            liquibase.update(targetTag, new Contexts(), new LabelExpression());
            plugin.getLogger().info("Đã cập nhật database đến phiên bản " + targetTag);
        } catch (SQLException | LiquibaseException e) {
            plugin.getLogger().severe("Lỗi khi cập nhật database đến phiên bản " + targetTag + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Thực hiện rollback database đến một phiên bản cụ thể
     * 
     * @param targetTag Tag phiên bản đích
     */
    public void rollbackToVersion(String targetTag) {
        try {
            Connection connection = sqliteManager.getConnection();
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
            Liquibase liquibase = new Liquibase(CHANGELOG_MASTER, resourceAccessor, database);

            // Rollback đến tag phiên bản cụ thể
            liquibase.rollback(targetTag, new Contexts(), new LabelExpression());
            plugin.getLogger().info("Đã rollback database đến phiên bản " + targetTag);
        } catch (SQLException | LiquibaseException e) {
            plugin.getLogger().severe("Lỗi khi rollback database đến phiên bản " + targetTag + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lấy thông tin phiên bản hiện tại của database
     * 
     * @return Phiên bản hiện tại
     */
    public String getCurrentVersion() {
        try {
            Connection connection = sqliteManager.getConnection();

            // Kiểm tra xem bảng DATABASECHANGELOG đã tồn tại chưa
            if (!tableExists(connection, "DATABASECHANGELOG")) {
                return "0.0";
            }

            // Truy vấn tag mới nhất từ bảng DATABASECHANGELOG
            String sql = "SELECT tag FROM DATABASECHANGELOG WHERE tag IS NOT NULL ORDER BY DATEEXECUTED DESC LIMIT 1";
            try (PreparedStatement stmt = connection.prepareStatement(sql);
                    ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("tag");
                }
            }

            return "0.0";
        } catch (SQLException e) {
            plugin.getLogger().severe("Lỗi khi lấy phiên bản database: " + e.getMessage());
            e.printStackTrace();
            return "unknown";
        }
    }

    /**
     * Kiểm tra xem một bảng có tồn tại trong database hay không
     * 
     * @param connection Kết nối database
     * @param tableName  Tên bảng cần kiểm tra
     * @return true nếu bảng tồn tại, false nếu không
     * @throws SQLException Nếu có lỗi khi truy vấn
     */
    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Thực hiện update database
     * 
     * @param connection Kết nối database
     * @param targetTag  Tag phiên bản đích (null để cập nhật lên phiên bản mới
     *                   nhất)
     * @throws LiquibaseException Nếu có lỗi khi thực hiện migration
     * @throws SQLException       Nếu có lỗi khi kết nối database
     */
    private void performUpdate(Connection connection, String targetTag) throws LiquibaseException, SQLException {
        Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(connection));
        ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
        Liquibase liquibase = new Liquibase(CHANGELOG_MASTER, resourceAccessor, database);

        if (targetTag == null) {
            liquibase.update(new Contexts(), new LabelExpression());
        } else {
            liquibase.update(targetTag, new Contexts(), new LabelExpression());
        }
    }
}