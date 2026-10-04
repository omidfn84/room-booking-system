package com.group10.scheduler.persistence.sql;

import static org.junit.Assert.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.List;

import com.group10.scheduler.accounts.RegisteredUserFactory;
import com.group10.scheduler.accounts.Student;
import com.group10.scheduler.booking.Booking;
import com.group10.scheduler.booking.BookingStatus;
import com.group10.scheduler.booking.ConcreteStrategies;
import com.group10.scheduler.booking.Payment;
import com.group10.scheduler.booking.PaymentMethod;
import com.group10.scheduler.booking.PaymentStatus;
import com.group10.scheduler.persistence.csv.CsvBookingRepository;
import com.group10.scheduler.persistence.csv.CsvPaymentRepository;
import com.group10.scheduler.persistence.csv.CsvRoomRepository;
import com.group10.scheduler.persistence.csv.CsvUserRepository;
import com.group10.scheduler.room.Room;
import com.group10.scheduler.room.RoomStatus;

public class CsvToSqlMigrationTest{

    @Rule
    public TemporaryFolder tempFolder= new TemporaryFolder ();

    private final RegisteredUserFactory factory= new RegisteredUserFactory ();
    private File dir;
    private SqliteDatabase db;

    @Before
    public void setUp () throws Exception{
        dir= tempFolder.getRoot ();
        db= new SqliteDatabase (new File (dir, "scheduler.db").getAbsolutePath ());
    }

    private void writeCsvFiles (){
        new CsvRoomRepository (new File (dir, "rooms.csv").getPath ())
                .saveRooms (List.of (new Room ("R1", 10, "Bergeron", "100", RoomStatus.AVAILABLE)));
        new CsvUserRepository (new File (dir, "users.csv").getPath (), factory)
                .saveUsers (List.of (new Student ("alice@yorku.ca", "Passw0rd!", "STUDENT", "Alice", 1L)));
        new CsvBookingRepository (new File (dir, "bookings.csv").getPath ())
                .saveBookings (List.of (new Booking ("B1", "alice@yorku.ca", "R1", "s", "e", 5.0, BookingStatus.CONFIRMED)));
        new CsvPaymentRepository (new File (dir, "payments.csv").getPath ())
                .savePayments (List.of (new Payment ("P1", "B1", 5.0, PaymentMethod.CREDIT_CARD, PaymentStatus.PAID,
                        "d", ConcreteStrategies.fromMethod (PaymentMethod.CREDIT_CARD))));
    }

    @Test
    public void importsEveryCsvFile (){
        writeCsvFiles ();
        CsvToSqlMigration.migrateIfNeeded (dir.getPath (), db, factory);

        assertEquals ("R1", new SqliteRoomRepository (db).loadRooms ().get (0).getRoomId ());
        assertEquals ("alice@yorku.ca", new SqliteUserRepository (db, factory).loadUsers ().get (0).getEmail ());
        assertEquals ("B1", new SqliteBookingRepository (db).loadBookings ().get (0).getBookingId ());
        assertEquals ("P1", new SqlitePaymentRepository (db).loadPayments ().get (0).getPaymentId ());
    }

    @Test
    public void noCsvFilesLeavesDatabaseEmpty (){
        CsvToSqlMigration.migrateIfNeeded (dir.getPath (), db, factory);
        assertTrue (new SqliteRoomRepository (db).loadRooms ().isEmpty ());
        assertTrue (new SqliteUserRepository (db, factory).loadUsers ().isEmpty ());
    }

    @Test
    public void doesNotOverwriteExistingSqlData (){
        writeCsvFiles ();
        SqliteRoomRepository rooms= new SqliteRoomRepository (db);
        rooms.saveRooms (List.of (new Room ("NEW", 5, "Ross", "1", RoomStatus.AVAILABLE)));

        CsvToSqlMigration.migrateIfNeeded (dir.getPath (), db, factory);
        CsvToSqlMigration.migrateIfNeeded (dir.getPath (), db, factory);

        List <Room> loaded= rooms.loadRooms ();
        assertEquals (1, loaded.size ());
        assertEquals ("NEW", loaded.get (0).getRoomId ());
        assertEquals (1, new SqliteUserRepository (db, factory).loadUsers ().size ());
    }
}
