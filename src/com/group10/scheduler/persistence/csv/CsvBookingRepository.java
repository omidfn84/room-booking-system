package com.group10.scheduler.persistence.csv;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.group10.scheduler.booking.Booking;
import com.group10.scheduler.booking.BookingStatus;
import com.group10.scheduler.booking.ConcreteStates;
import com.group10.scheduler.persistence.BookingRepository;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvBookingRepository implements BookingRepository {

    private final String filePath;
    private static final String[] HEADERS = {
        "bookingId", "userEmail", "roomId", "startTime", "endTime",
        "depositAmount", "checkInTime", "status"
    };

    public CsvBookingRepository(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public List<Booking> loadBookings() {
        List<Booking> bookings = new ArrayList<>();
        CsvReader reader = null;
        try {
            reader = new CsvReader(filePath);
            reader.readHeaders();
            while (reader.readRecord()) {
                try {
                    String bookingId = reader.get("bookingId");
                    String userEmail = reader.get("userEmail");
                    String roomId = reader.get("roomId");
                    String startTime = reader.get("startTime");
                    String endTime = reader.get("endTime");
                    double depositAmount = Double.parseDouble(reader.get("depositAmount"));
                    String checkInTime = reader.get("checkInTime");
                    BookingStatus status = BookingStatus.valueOf(reader.get("status"));

                    Booking booking = new Booking(bookingId, userEmail, roomId, startTime, endTime, depositAmount, status);
                    // empty cell means "never checked in" -> must stay null, or Req4
                    // would wrongly apply the deposit after a restart
                    booking.setCheckInTime(checkInTime == null || checkInTime.isBlank() ? null : checkInTime);
                    bookings.add(booking);
                } catch (RuntimeException badRow) {
                    // skip a corrupted row instead of losing every booking
                    System.out.println("Skipping bad row in bookings.csv: " + badRow.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("No existing bookings.csv found, starting empty: " + e.getMessage());
        } finally {
            if (reader != null) reader.close();
        }
        return bookings;
    }

    @Override
    public void saveBookings(List<Booking> bookings) {
        CsvWriter writer = null;
        try {
            writer = new CsvWriter(new FileWriter(filePath, false), ',');
            for (String h : HEADERS) writer.write(h);
            writer.endRecord();

            for (Booking b : bookings) {
                writer.write(b.getBookingId());
                writer.write(b.getUserEmail());
                writer.write(b.getRoomId());
                writer.write(b.getStartTime());
                writer.write(b.getEndTime());
                writer.write(String.valueOf(b.getDepositAmount()));
                writer.write(b.getCheckInTime() == null ? "" : b.getCheckInTime());
                writer.write(b.getStatus().name());
                writer.endRecord();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save bookings.csv", e);
        } finally {
            if (writer != null) writer.close();
        }
    }
}