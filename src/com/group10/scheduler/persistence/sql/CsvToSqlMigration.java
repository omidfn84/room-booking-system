package com.group10.scheduler.persistence.sql;

import com.group10.scheduler.accounts.RegisteredUserFactory;
import com.group10.scheduler.persistence.csv.CsvBookingRepository;
import com.group10.scheduler.persistence.csv.CsvPaymentRepository;
import com.group10.scheduler.persistence.csv.CsvRoomRepository;
import com.group10.scheduler.persistence.csv.CsvUserRepository;

import java.io.File;
import java.util.List;

/**
 * One-time import of the old CSV files into SQLite, so switching storage
 * doesn't lose anyone's existing rooms, users, bookings or payments.
 *
 * Each table is only filled when it is still empty AND its CSV file exists,
 * so running this on every start-up is safe: after the first import it does
 * nothing.
 */
public final class CsvToSqlMigration {

    private CsvToSqlMigration() {}

    public static void migrateIfNeeded(String csvDir, SqliteDatabase db, RegisteredUserFactory userFactory) {
        SqliteRoomRepository rooms = new SqliteRoomRepository(db);
        SqliteUserRepository users = new SqliteUserRepository(db, userFactory);
        SqliteBookingRepository bookings = new SqliteBookingRepository(db);
        SqlitePaymentRepository payments = new SqlitePaymentRepository(db);

        File roomsCsv = new File(csvDir, "rooms.csv");
        if (roomsCsv.isFile() && rooms.loadRooms().isEmpty()) {
            rooms.saveRooms(new CsvRoomRepository(roomsCsv.getPath()).loadRooms());
            report(roomsCsv, rooms.loadRooms());
        }
        File usersCsv = new File(csvDir, "users.csv");
        if (usersCsv.isFile() && users.loadUsers().isEmpty()) {
            users.saveUsers(new CsvUserRepository(usersCsv.getPath(), userFactory).loadUsers());
            report(usersCsv, users.loadUsers());
        }
        File bookingsCsv = new File(csvDir, "bookings.csv");
        if (bookingsCsv.isFile() && bookings.loadBookings().isEmpty()) {
            bookings.saveBookings(new CsvBookingRepository(bookingsCsv.getPath()).loadBookings());
            report(bookingsCsv, bookings.loadBookings());
        }
        File paymentsCsv = new File(csvDir, "payments.csv");
        if (paymentsCsv.isFile() && payments.loadPayments().isEmpty()) {
            payments.savePayments(new CsvPaymentRepository(paymentsCsv.getPath()).loadPayments());
            report(paymentsCsv, payments.loadPayments());
        }
    }

    private static void report(File csv, List<?> imported) {
        System.out.println("Imported " + imported.size() + " rows from " + csv.getName() + " into SQLite");
    }
}
