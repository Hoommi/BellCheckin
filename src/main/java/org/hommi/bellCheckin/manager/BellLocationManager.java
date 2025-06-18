package org.hommi.bellCheckin.manager;

import org.hommi.bellCheckin.model.BellLocation;
import org.hommi.bellCheckin.sqlite.SQLiteManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BellLocationManager {
    private final List<BellLocation> bellLocations = new ArrayList<>();

    public void loadFromDb() {
        bellLocations.clear();
        try (SQLiteManager sqLiteManager = new SQLiteManager()) {
            sqLiteManager.connect();
            String sql = "CREATE TABLE IF NOT EXISTS bell_locations (id INTEGER PRIMARY KEY AUTOINCREMENT, world TEXT, x REAL, y REAL, z REAL)";
            sqLiteManager.getConnection().createStatement().execute(sql);
            sql = "SELECT world, x, y, z FROM bell_locations";
            ResultSet rs = sqLiteManager.getConnection().createStatement().executeQuery(sql);
            while (rs.next()) {
                bellLocations.add(new BellLocation(
                        rs.getString("world"),
                        rs.getDouble("x"),
                        rs.getDouble("y"),
                        rs.getDouble("z")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveBellLocation(BellLocation bellLocation) {
        try (SQLiteManager sqLiteManager = new SQLiteManager()) {
            sqLiteManager.connect();
            String sql = "INSERT INTO bell_locations (world, x, y, z) VALUES (?, ?, ?, ?)";
            getStatement(bellLocation, sqLiteManager, sql);
            bellLocations.add(bellLocation);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Remove a bell location from both the in-memory list and the database, only if it exists in memory
    public boolean removeBellLocation(BellLocation bellLocation) {
        boolean exists = bellLocations.removeIf(loc ->
            loc.worldName().equals(bellLocation.worldName()) &&
            loc.x() == bellLocation.x() &&
            loc.y() == bellLocation.y() &&
            loc.z() == bellLocation.z()
        );
        if (!exists) return false;
        try (SQLiteManager sqLiteManager = new SQLiteManager()) {
            sqLiteManager.connect();
            String sql = "DELETE FROM bell_locations WHERE world = ? AND x = ? AND y = ? AND z = ?";
            getStatement(bellLocation, sqLiteManager, sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private void getStatement(BellLocation bellLocation,
            SQLiteManager sqLiteManager,
            String sql) throws SQLException {
        var pstmt = sqLiteManager.getConnection().prepareStatement(sql);
        pstmt.setString(1, bellLocation.worldName());
        pstmt.setDouble(2, bellLocation.x());
        pstmt.setDouble(3, bellLocation.y());
        pstmt.setDouble(4, bellLocation.z());
        pstmt.executeUpdate();
    }

    public List<BellLocation> getBellLocations() {
        return bellLocations;
    }
}
