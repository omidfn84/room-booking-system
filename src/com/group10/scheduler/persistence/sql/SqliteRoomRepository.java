package com.group10.scheduler.persistence.sql;

import com.group10.scheduler.persistence.RoomRepository;
import com.group10.scheduler.room.Room;
import com.group10.scheduler.room.RoomStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter: implements the domain-facing RoomRepository (Target) on top of
 * JDBC/SQLite (Adaptee). RoomManager only ever talks to RoomRepository — it
 * never imports java.sql.
 */
public class SqliteRoomRepository implements RoomRepository {

    private final SqliteDatabase db;

    public SqliteRoomRepository(SqliteDatabase db) {
        this.db = db;
    }

    @Override
    public List<Room> loadRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT roomId, capacity, building, roomNumber, status FROM rooms ORDER BY rowid";
        try (Connection conn = db.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                rooms.add(new Room(
                        rs.getString("roomId"),
                        rs.getInt("capacity"),
                        rs.getString("building"),
                        rs.getString("roomNumber"),
                        RoomStatus.valueOf(rs.getString("status"))));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load rooms", e);
        }
        return rooms;
    }

    @Override
    public void saveRooms(List<Room> rooms) {
        db.inTransaction("rooms", conn -> {
            try (Statement del = conn.createStatement()) {
                del.executeUpdate("DELETE FROM rooms");
            }
            String sql = "INSERT INTO rooms (roomId, capacity, building, roomNumber, status) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Room r : rooms) {
                    ps.setString(1, r.getRoomId());
                    ps.setInt(2, r.getCapacity());
                    ps.setString(3, r.getBuilding());
                    ps.setString(4, r.getRoomNumber());
                    ps.setString(5, r.getStatus().name());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        });
    }
}
