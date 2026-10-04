package com.group10.scheduler.persistence.sql;

import com.group10.scheduler.booking.ConcreteStrategies;
import com.group10.scheduler.booking.Payment;
import com.group10.scheduler.booking.PaymentMethod;
import com.group10.scheduler.booking.PaymentStatus;
import com.group10.scheduler.persistence.PaymentRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SqlitePaymentRepository implements PaymentRepository {

    private final SqliteDatabase db;

    public SqlitePaymentRepository(SqliteDatabase db) {
        this.db = db;
    }

    @Override
    public List<Payment> loadPayments() {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT paymentId, bookingId, amount, method, status, paymentDate FROM payments ORDER BY rowid";
        try (Connection conn = db.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                PaymentMethod method = PaymentMethod.valueOf(rs.getString("method"));
                payments.add(new Payment(
                        rs.getString("paymentId"),
                        rs.getString("bookingId"),
                        rs.getDouble("amount"),
                        method,
                        PaymentStatus.valueOf(rs.getString("status")),
                        rs.getString("paymentDate"),
                        ConcreteStrategies.fromMethod(method)));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load payments", e);
        }
        return payments;
    }

    @Override
    public void savePayments(List<Payment> payments) {
        db.inTransaction("payments", conn -> {
            try (Statement del = conn.createStatement()) {
                del.executeUpdate("DELETE FROM payments");
            }
            String sql = "INSERT INTO payments (paymentId, bookingId, amount, method, status, paymentDate)"
                    + " VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Payment p : payments) {
                    ps.setString(1, p.getPaymentId());
                    ps.setString(2, p.getBookingId());
                    ps.setDouble(3, p.getAmount());
                    ps.setString(4, p.getMethod().name());
                    ps.setString(5, p.getStatus().name());
                    ps.setString(6, p.getPaymentDate());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        });
    }
}
