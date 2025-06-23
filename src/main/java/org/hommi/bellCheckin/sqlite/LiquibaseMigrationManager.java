package org.hommi.bellCheckin.sqlite;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.hommi.bellCheckin.BellCheckin;

import java.sql.Connection;
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
     * @param targetVersion Phiên bản đích (tag)
     */
    public void updateToVersion(String targetVersion) {
        try {
            Connection connection = sqliteManager.getConnection();
            performUpdate(connection, targetVersion);
            plugin.getLogger().info("Đã cập nhật database đến phiên bản " + targetVersion);
        } catch (SQLException | LiquibaseException e) {
            plugin.getLogger()
                    .severe("Lỗi khi cập nhật database đến phiên bản " + targetVersion + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Thực hiện rollback database đến một phiên bản cụ thể
     * 
     * @param targetVersion Phiên bản đích (tag)
     */
    public void rollbackToVersion(String targetVersion) {
        try {
            Connection connection = sqliteManager.getConnection();
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new Liquibase(CHANGELOG_MASTER, new ClassLoaderResourceAccessor(), database);
            liquibase.rollback(targetVersion, new Contexts(), new LabelExpression());
            plugin.getLogger().info("Đã rollback database đến phiên bản " + targetVersion);
        } catch (SQLException | LiquibaseException e) {
            plugin.getLogger()
                    .severe("Lỗi khi rollback database đến phiên bản " + targetVersion + ": " + e.getMessage());
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
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new Liquibase(CHANGELOG_MASTER, new ClassLoaderResourceAccessor(), database);
            return liquibase.getChangeLogFile();
        } catch (SQLException | LiquibaseException e) {
            plugin.getLogger().severe("Lỗi khi lấy phiên bản database: " + e.getMessage());
            e.printStackTrace();
            return "unknown";
        }
    }

    /**
     * Thực hiện update database
     * 
     * @param connection    Kết nối database
     * @param targetVersion Phiên bản đích (null để cập nhật lên phiên bản mới nhất)
     * @throws LiquibaseException Nếu có lỗi khi thực hiện migration
     * @throws SQLException       Nếu có lỗi khi kết nối database
     */
    private void performUpdate(Connection connection, String targetVersion) throws LiquibaseException, SQLException {
        Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Liquibase liquibase = new Liquibase(CHANGELOG_MASTER, new ClassLoaderResourceAccessor(), database);

        if (targetVersion == null) {
            liquibase.update(new Contexts(), new LabelExpression());
        } else {
            liquibase.update(targetVersion, new Contexts(), new LabelExpression());
        }
    }
}