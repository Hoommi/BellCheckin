package org.hommi.bellCheckin.manager;

import org.hommi.bellCheckin.BellCheckin;
import org.hommi.bellCheckin.model.BellLocation;
import org.hommi.bellCheckin.sqlite.SQLiteManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BellLocationManager {
    private final BellCheckin plugin;
    private final List<BellLocation> bellLocations = new ArrayList<>();

    public BellLocationManager() {
        plugin = BellCheckin.getInstance();
    }

    /**
     * Lấy danh sách các vị trí chuông
     * 
     * @return danh sách vị trí chuông
     */
    public List<BellLocation> getBellLocations() {
        return bellLocations;
    }

    /**
     * Thêm vị trí chuông mới
     * 
     * @param location vị trí chuông
     */
    public void addBellLocation(BellLocation location) {
        bellLocations.add(location);
        saveToDb(location);
    }

    /**
     * Xóa vị trí chuông
     * 
     * @param worldName tên thế giới
     * @param x         tọa độ x
     * @param y         tọa độ y
     * @param z         tọa độ z
     * @return true nếu xóa thành công
     */
    public boolean removeBellLocation(String worldName, int x, int y, int z) {
        boolean removed = bellLocations.removeIf(loc -> loc.worldName().equals(worldName) &&
                loc.x() == x &&
                loc.y() == y &&
                loc.z() == z);

        if (removed) {
            removeFromDb(worldName, x, y, z);
        }

        return removed;
    }

    /**
     * Kiểm tra xem một vị trí có phải là chuông điểm danh không
     * 
     * @param worldName tên thế giới
     * @param x         tọa độ x
     * @param y         tọa độ y
     * @param z         tọa độ z
     * @return true nếu là chuông điểm danh
     */
    public boolean isCheckinBell(String worldName, int x, int y, int z) {
        return bellLocations.stream().anyMatch(loc -> loc.worldName().equals(worldName) &&
                loc.x() == x &&
                loc.y() == y &&
                loc.z() == z);
    }

    /**
     * Tải danh sách vị trí chuông từ database
     */
    public void loadFromDb() {
        bellLocations.clear();

        try (SQLiteManager sqLiteManager = new SQLiteManager()) {
            sqLiteManager.connect();
            String sql = "SELECT worldName, x, y, z FROM bell_locations";
            try (var stmt = sqLiteManager.getConnection().createStatement();
                    var rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    String worldName = rs.getString("worldName");
                    int x = rs.getInt("x");
                    int y = rs.getInt("y");
                    int z = rs.getInt("z");
                    bellLocations.add(new BellLocation(worldName, x, y, z));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Lỗi khi tải vị trí chuông từ database: " + e.getMessage());
        }
    }

    /**
     * Lưu vị trí chuông vào database
     * 
     * @param location vị trí chuông
     */
    private void saveToDb(BellLocation location) {
        try (SQLiteManager sqLiteManager = new SQLiteManager()) {
            sqLiteManager.connect();
            String sql = "INSERT INTO bell_locations (worldName, x, y, z) VALUES (?, ?, ?, ?)";
            try (var pstmt = sqLiteManager.getConnection().prepareStatement(sql)) {
                pstmt.setString(1, location.worldName());
                pstmt.setInt(2, location.x());
                pstmt.setInt(3, location.y());
                pstmt.setInt(4, location.z());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Lỗi khi lưu vị trí chuông vào database: " + e.getMessage());
        }
    }

    /**
     * Xóa vị trí chuông khỏi database
     * 
     * @param worldName tên thế giới
     * @param x         tọa độ x
     * @param y         tọa độ y
     * @param z         tọa độ z
     */
    private void removeFromDb(String worldName, int x, int y, int z) {
        try (SQLiteManager sqLiteManager = new SQLiteManager()) {
            sqLiteManager.connect();
            String sql = "DELETE FROM bell_locations WHERE worldName = ? AND x = ? AND y = ? AND z = ?";
            try (var pstmt = sqLiteManager.getConnection().prepareStatement(sql)) {
                pstmt.setString(1, worldName);
                pstmt.setInt(2, x);
                pstmt.setInt(3, y);
                pstmt.setInt(4, z);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Lỗi khi xóa vị trí chuông khỏi database: " + e.getMessage());
        }
    }

    /**
     * Lưu tất cả vị trí chuông vào database
     */
    public void saveAllToDb() {
        try (SQLiteManager sqLiteManager = new SQLiteManager()) {
            sqLiteManager.connect();

            // Xóa tất cả vị trí cũ
            try (var stmt = sqLiteManager.getConnection().createStatement()) {
                stmt.executeUpdate("DELETE FROM bell_locations");
            }

            // Thêm lại tất cả vị trí mới
            String sql = "INSERT INTO bell_locations (worldName, x, y, z) VALUES (?, ?, ?, ?)";
            try (var pstmt = sqLiteManager.getConnection().prepareStatement(sql)) {
                for (BellLocation location : bellLocations) {
                    pstmt.setString(1, location.worldName());
                    pstmt.setInt(2, location.x());
                    pstmt.setInt(3, location.y());
                    pstmt.setInt(4, location.z());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Lỗi khi lưu tất cả vị trí chuông vào database: " + e.getMessage());
        }
    }
}
