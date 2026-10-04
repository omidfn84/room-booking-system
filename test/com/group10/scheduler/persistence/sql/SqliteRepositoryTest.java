package com.group10.scheduler.persistence.sql;

import static org.junit.Assert.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import com.group10.scheduler.accounts.RegisteredUser;
import com.group10.scheduler.accounts.RegisteredUserFactory;
import com.group10.scheduler.accounts.Student;
import com.group10.scheduler.booking.Booking;
import com.group10.scheduler.booking.BookingStatus;
import com.group10.scheduler.booking.ConcreteStrategies;
import com.group10.scheduler.booking.Payment;
import com.group10.scheduler.booking.PaymentMethod;
import com.group10.scheduler.booking.PaymentStatus;
import com.group10.scheduler.room.Room;
import com.group10.scheduler.room.RoomStatus;

public class SqliteRepositoryTest{

    @Rule
    public TemporaryFolder tempFolder= new TemporaryFolder ();

    private final RegisteredUserFactory factory= new RegisteredUserFactory ();
    private SqliteDatabase db;

    @Before
    public void setUp () throws Exception{
        db= new SqliteDatabase (new File (tempFolder.getRoot (), "test.db").getAbsolutePath ());
    }

    @Test
    public void newDatabaseIsEmpty (){
        assertTrue (new SqliteRoomRepository (db).loadRooms ().isEmpty ());
        assertTrue (new SqliteUserRepository (db, factory).loadUsers ().isEmpty ());
        assertTrue (new SqliteBookingRepository (db).loadBookings ().isEmpty ());
        assertTrue (new SqlitePaymentRepository (db).loadPayments ().isEmpty ());
    }

    @Test
    public void roomSaveAndLoad (){
        SqliteRoomRepository repo= new SqliteRoomRepository (db);
        repo.saveRooms (List.of (
                new Room ("R1", 25, "Bergeron", "100", RoomStatus.AVAILABLE),
                new Room ("R2", 10, "Ross", "200", RoomStatus.MAINTENANCE)));

        List <Room> loaded= repo.loadRooms ();
        assertEquals (2, loaded.size ());
        Room r1= loaded.get (0);
        assertEquals ("R1", r1.getRoomId ());
        assertEquals (25, r1.getCapacity ());
        assertEquals ("Bergeron", r1.getBuilding ());
        assertEquals ("100", r1.getRoomNumber ());
        assertEquals (RoomStatus.AVAILABLE, r1.getStatus ());
        assertEquals (RoomStatus.MAINTENANCE, loaded.get (1).getStatus ());
    }

    @Test
    public void saveOverwritesPreviousRows (){
        SqliteRoomRepository repo= new SqliteRoomRepository (db);
        repo.saveRooms (List.of (new Room ("R1", 10, "Bergeron", "100", RoomStatus.AVAILABLE)));
        repo.saveRooms (List.of (new Room ("R2", 20, "Ross", "200", RoomStatus.DISABLED)));

        List <Room> loaded= repo.loadRooms ();
        assertEquals (1, loaded.size ());
        assertEquals ("R2", loaded.get (0).getRoomId ());
    }

    @Test
    public void dataSurvivesReopeningTheDatabase (){
        String path= new File (tempFolder.getRoot (), "reopen.db").getAbsolutePath ();
        new SqliteRoomRepository (new SqliteDatabase (path))
                .saveRooms (List.of (new Room ("R1", 10, "Bergeron", "100", RoomStatus.AVAILABLE)));

        assertEquals (1, new SqliteRoomRepository (new SqliteDatabase (path)).loadRooms ().size ());
    }

    @Test
    public void failedSaveRollsBackAndKeepsOldRows (){
        SqliteRoomRepository repo= new SqliteRoomRepository (db);
        repo.saveRooms (List.of (new Room ("R1", 10, "Bergeron", "100", RoomStatus.AVAILABLE)));

        // duplicate primary key makes the insert fail half-way through
        try{
            repo.saveRooms (List.of (
                    new Room ("R2", 10, "Ross", "1", RoomStatus.AVAILABLE),
                    new Room ("R2", 10, "Ross", "2", RoomStatus.AVAILABLE)));
            fail ("expected the save to fail");
        }
        catch (RuntimeException expected){
            assertTrue (expected.getMessage ().contains ("rooms"));
        }

        List <Room> loaded= repo.loadRooms ();
        assertEquals (1, loaded.size ());
        assertEquals ("R1", loaded.get (0).getRoomId ());
    }

    @Test
    public void userSaveAndLoad (){
        SqliteUserRepository repo= new SqliteUserRepository (db, factory);
        RegisteredUser user= new Student ("alice@yorku.ca", "Passw0rd!", "STUDENT", "Alice", 123456789L);
        repo.saveUsers (List.of (user));

        List <RegisteredUser> loaded= repo.loadUsers ();
        assertEquals (1, loaded.size ());
        RegisteredUser reloaded= loaded.get (0);
        assertTrue (reloaded instanceof Student);
        assertEquals ("alice@yorku.ca", reloaded.getEmail ());
        assertEquals ("Passw0rd!", reloaded.getPassword ());
        assertEquals ("Alice", reloaded.getUserName ());
        assertEquals (123456789L, reloaded.getOrganizationId ());
    }

    @Test
    public void bookingSaveAndLoadKeepsNullCheckIn (){
        SqliteBookingRepository repo= new SqliteBookingRepository (db);
        Booking notCheckedIn= new Booking ("B1", "a@yorku.ca", "R1", "2026-10-05T10:00", "2026-10-05T11:00", 20.0, BookingStatus.CONFIRMED);
        Booking checkedIn= new Booking ("B2", "a@yorku.ca", "R1", "2026-10-06T10:00", "2026-10-06T11:00", 20.0, BookingStatus.CHECKED_IN);
        checkedIn.setCheckInTime ("2026-10-06T10:05");
        repo.saveBookings (List.of (notCheckedIn, checkedIn));

        List <Booking> loaded= repo.loadBookings ();
        assertEquals (2, loaded.size ());
        Booking b1= loaded.get (0);
        assertEquals ("B1", b1.getBookingId ());
        assertEquals ("a@yorku.ca", b1.getUserEmail ());
        assertEquals ("R1", b1.getRoomId ());
        assertEquals ("2026-10-05T10:00", b1.getStartTime ());
        assertEquals ("2026-10-05T11:00", b1.getEndTime ());
        assertEquals (20.0, b1.getDepositAmount (), 0.0001);
        assertNull (b1.getCheckInTime ());
        assertEquals (BookingStatus.CONFIRMED, b1.getStatus ());
        assertEquals ("2026-10-06T10:05", loaded.get (1).getCheckInTime ());
        assertEquals (BookingStatus.CHECKED_IN, loaded.get (1).getStatus ());
    }

    @Test
    public void corruptedBookingRowIsSkipped () throws Exception{
        SqliteBookingRepository repo= new SqliteBookingRepository (db);
        repo.saveBookings (List.of (
                new Booking ("B1", "a@yorku.ca", "R1", "s", "e", 5.0, BookingStatus.CONFIRMED)));
        try (Connection conn= db.connect (); Statement st= conn.createStatement ()){
            st.executeUpdate ("INSERT INTO bookings VALUES ('B2','a@yorku.ca','R1','s','e',5.0,NULL,'UNKNOWN')");
        }

        List <Booking> loaded= repo.loadBookings ();
        assertEquals (1, loaded.size ());
        assertEquals ("B1", loaded.get (0).getBookingId ());
    }

    @Test
    public void paymentSaveAndLoad (){
        SqlitePaymentRepository repo= new SqlitePaymentRepository (db);
        repo.savePayments (List.of (new Payment ("P1", "B1", 42.5, PaymentMethod.DEBIT_CARD, PaymentStatus.PAID,
                "2026-10-05T09:00", ConcreteStrategies.fromMethod (PaymentMethod.DEBIT_CARD))));

        List <Payment> loaded= repo.loadPayments ();
        assertEquals (1, loaded.size ());
        Payment p= loaded.get (0);
        assertEquals ("P1", p.getPaymentId ());
        assertEquals ("B1", p.getBookingId ());
        assertEquals (42.5, p.getAmount (), 0.0001);
        assertEquals (PaymentMethod.DEBIT_CARD, p.getMethod ());
        assertEquals (PaymentStatus.PAID, p.getStatus ());
        assertEquals ("2026-10-05T09:00", p.getPaymentDate ());
    }

    @Test
    public void valuesAreStoredAsDataNotSql (){
        SqliteRoomRepository repo= new SqliteRoomRepository (db);
        repo.saveRooms (List.of (new Room ("R1'); DROP TABLE rooms; --", 1, "O'Neil Hall", "1", RoomStatus.AVAILABLE)));

        Room loaded= repo.loadRooms ().get (0);
        assertEquals ("R1'); DROP TABLE rooms; --", loaded.getRoomId ());
        assertEquals ("O'Neil Hall", loaded.getBuilding ());
    }
}
