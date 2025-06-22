package org.hommi.bellCheckin.sqlite;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Định nghĩa migration giữa các phiên bản database
 */
public class DatabaseMigration {
    private final int fromVersion;
    private final int toVersion;
    private final MigrationOperation upOperation;
    private final MigrationOperation downOperation;

    /**
     * Tạo một migration giữa hai phiên bản
     * 
     * @param fromVersion   Phiên bản nguồn
     * @param toVersion     Phiên bản đích
     * @param upOperation   Thao tác nâng cấp
     * @param downOperation Thao tác hạ cấp
     */
    public DatabaseMigration(int fromVersion, int toVersion, MigrationOperation upOperation,
            MigrationOperation downOperation) {
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
        this.upOperation = upOperation;
        this.downOperation = downOperation;
    }

    /**
     * Thực hiện nâng cấp
     * 
     * @param connection Kết nối database
     * @throws SQLException Nếu có lỗi SQL
     */
    public void up(Connection connection) throws SQLException {
        upOperation.execute(connection);
    }

    /**
     * Thực hiện hạ cấp
     * 
     * @param connection Kết nối database
     * @throws SQLException Nếu có lỗi SQL
     */
    public void down(Connection connection) throws SQLException {
        downOperation.execute(connection);
    }

    /**
     * Lấy phiên bản nguồn
     * 
     * @return Phiên bản nguồn
     */
    public int getFromVersion() {
        return fromVersion;
    }

    /**
     * Lấy phiên bản đích
     * 
     * @return Phiên bản đích
     */
    public int getToVersion() {
        return toVersion;
    }

    /**
     * Interface định nghĩa thao tác migration
     */
    @FunctionalInterface
    public interface MigrationOperation {
        /**
         * Thực hiện thao tác migration
         * 
         * @param connection Kết nối database
         * @throws SQLException Nếu có lỗi SQL
         */
        void execute(Connection connection) throws SQLException;
    }
}