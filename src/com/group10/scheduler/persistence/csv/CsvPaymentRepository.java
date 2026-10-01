package com.group10.scheduler.persistence.csv;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.group10.scheduler.booking.ConcreteStrategies;
import com.group10.scheduler.booking.Payment;
import com.group10.scheduler.booking.PaymentMethod;
import com.group10.scheduler.booking.PaymentStatus;
import com.group10.scheduler.persistence.PaymentRepository;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvPaymentRepository implements PaymentRepository {

    private final String filePath;
    private static final String[] HEADERS = {
        "paymentId", "bookingId", "amount", "method", "status", "paymentDate"
    };

    public CsvPaymentRepository(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public List<Payment> loadPayments() {
        List<Payment> payments = new ArrayList<>();
        CsvReader reader = null;
        try {
            reader = new CsvReader(filePath);
            reader.readHeaders();
            while (reader.readRecord()) {
                String paymentId = reader.get("paymentId");
                String bookingId = reader.get("bookingId");
                double amount = Double.parseDouble(reader.get("amount"));
                PaymentMethod method = PaymentMethod.valueOf(reader.get("method"));
                PaymentStatus status = PaymentStatus.valueOf(reader.get("status"));
                String paymentDate = reader.get("paymentDate");

                payments.add(new Payment(paymentId, bookingId, amount, method, status,
                        paymentDate, ConcreteStrategies.fromMethod(method)));
            }
        } catch (IOException e) {
            System.out.println("No existing payments.csv found, starting empty: " + e.getMessage());
        } finally {
            if (reader != null) reader.close();
        }
        return payments;
    }

    @Override
    public void savePayments(List<Payment> payments) {
        CsvWriter writer = null;
        try {
            writer = new CsvWriter(new FileWriter(filePath, false), ',');
            for (String h : HEADERS) writer.write(h);
            writer.endRecord();

            for (Payment p : payments) {
                writer.write(p.getPaymentId());
                writer.write(p.getBookingId());
                writer.write(String.valueOf(p.getAmount()));
                writer.write(p.getMethod().name());
                writer.write(p.getStatus().name());
                writer.write(p.getPaymentDate());
                writer.endRecord();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save payments.csv", e);
        } finally {
            if (writer != null) writer.close();
        }
    }
}
