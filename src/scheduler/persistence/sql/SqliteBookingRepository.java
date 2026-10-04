package scheduler.persistence.sql;

import scheduler.booking.Booking;
import scheduler.booking.BookingStatus;
import scheduler.persistence.BookingRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SqliteBookingRepository implements BookingRepository {

    private final SqliteDatabase db;

    public SqliteBookingRepository(SqliteDatabase db) {
        this.db = db;
    }

    @Override
    public List<Booking> loadBookings() {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT bookingId, userEmail, roomId, startTime, endTime, depositAmount, checkInTime, status"
                + " FROM bookings ORDER BY rowid";
        try (Connection conn = db.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                try {
                    Booking booking = new Booking(
                            rs.getString("bookingId"),
                            rs.getString("userEmail"),
                            rs.getString("roomId"),
                            rs.getString("startTime"),
                            rs.getString("endTime"),
                            rs.getDouble("depositAmount"),
                            BookingStatus.valueOf(rs.getString("status")));
                    // NULL means "never checked in" -> must stay null, or Req4
                    // would wrongly apply the deposit after a restart
                    booking.setCheckInTime(rs.getString("checkInTime"));
                    bookings.add(booking);
                } catch (RuntimeException badRow) {
                    // skip a corrupted row instead of losing every booking
                    System.out.println("Skipping bad row in bookings: " + badRow.getMessage());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load bookings", e);
        }
        return bookings;
    }

    @Override
    public void saveBookings(List<Booking> bookings) {
        db.inTransaction("bookings", conn -> {
            try (Statement del = conn.createStatement()) {
                del.executeUpdate("DELETE FROM bookings");
            }
            String sql = "INSERT INTO bookings (bookingId, userEmail, roomId, startTime, endTime, depositAmount, checkInTime, status)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Booking b : bookings) {
                    ps.setString(1, b.getBookingId());
                    ps.setString(2, b.getUserEmail());
                    ps.setString(3, b.getRoomId());
                    ps.setString(4, b.getStartTime());
                    ps.setString(5, b.getEndTime());
                    ps.setDouble(6, b.getDepositAmount());
                    ps.setString(7, b.getCheckInTime());
                    ps.setString(8, b.getStatus().name());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        });
    }
}
