package scheduler.facade;

import scheduler.accounts.TestChief;

import static org.junit.Assert.*;
import org.junit.*;

import java.time.LocalDateTime;

import scheduler.accounts.AccountManagement;
import scheduler.accounts.Administrator;
import scheduler.accounts.FakeUserRepository;
import scheduler.accounts.RegisteredUser;
import scheduler.booking.Booking;
import scheduler.booking.BookingManager;
import scheduler.booking.FakeBookingRepository;
import scheduler.booking.FakePaymentRepository;
import scheduler.booking.PaymentMethod;
import scheduler.room.FakeRoomRepository;
import scheduler.room.Room;
import scheduler.room.RoomManager;
import scheduler.room.RoomStatus;

public class SchedulerFacadeTest{

    private RoomManager roomManager;
    private BookingManager bookingManager;
    private AccountManagement accountManagement;
    private SchedulerFacade facade;
    private final String[] creditCardFields= {"1234567890123456", "12/30", "123"};

    private String iso (LocalDateTime t){ return t.toString (); }

    @Before
    public void setUp (){
        roomManager= new RoomManager (new FakeRoomRepository ());
        bookingManager= new BookingManager (new FakeBookingRepository (), new FakePaymentRepository (), roomManager);
        accountManagement= new AccountManagement (new FakeUserRepository ());
        facade= new SchedulerFacade (roomManager, bookingManager, accountManagement);

        roomManager.addRoom (new Room ("R1", 10, "Bergeron", "100", RoomStatus.AVAILABLE));
    }

    @Test
    public void createAccount (){
        RegisteredUser u= facade.createAccount ("alice@yorku.ca", "Passw0rd!", "STUDENT", "Alice", "123456789");
        assertNotNull (u);
        assertEquals ("alice@yorku.ca", u.getEmail ());
    }

    @Test
    public void loginWorks (){
        facade.createAccount ("alice@yorku.ca", "Passw0rd!", "STUDENT", "Alice", "123456789");
        RegisteredUser u= facade.login ("alice@yorku.ca", "Passw0rd!");
        assertNotNull (u);
    }

    @Test
    public void wrongPassword (){
        facade.createAccount ("alice@yorku.ca", "Passw0rd!", "STUDENT", "Alice", "123456789");
        assertNull (facade.login ("alice@yorku.ca", "WrongPassword1!"));
    }

    @Test
    public void missingUserLogin (){
        assertNull (facade.login ("nobody@yorku.ca", "Passw0rd!"));
    }

    @Test
    public void availableRooms (){
        java.util.List <Room> rooms= facade.getBookableRooms (iso (LocalDateTime.now ().plusHours (1)), iso (LocalDateTime.now ().plusHours (2)));
        assertEquals (1, rooms.size ());
    }

    @Test
    public void bookRoom (){
        facade.createAccount ("alice@yorku.ca", "Passw0rd!", "STUDENT", "Alice", "123456789");
        Booking b= facade.bookRoom ("alice@yorku.ca", "R1",
                iso (LocalDateTime.now ().plusHours (1)), iso (LocalDateTime.now ().plusHours (2)),
                PaymentMethod.CREDIT_CARD, creditCardFields);
        assertNotNull (b);
    }

    @Test(expected= IllegalArgumentException.class)
    public void missingUserBooking (){
        facade.bookRoom ("nobody@yorku.ca", "R1",
                iso (LocalDateTime.now ().plusHours (1)), iso (LocalDateTime.now ().plusHours (2)),
                PaymentMethod.CREDIT_CARD, creditCardFields);
    }

    @Test(expected= IllegalArgumentException.class)
    public void missingRoomBooking (){
        facade.createAccount ("alice@yorku.ca", "Passw0rd!", "STUDENT", "Alice", "123456789");
        facade.bookRoom ("alice@yorku.ca", "NO-SUCH-ROOM",
                iso (LocalDateTime.now ().plusHours (1)), iso (LocalDateTime.now ().plusHours (2)),
                PaymentMethod.CREDIT_CARD, creditCardFields);
    }

    @Test
    public void checkIn (){
        facade.createAccount ("alice@yorku.ca", "Passw0rd!", "STUDENT", "Alice", "123456789");
        Booking b= facade.bookRoom ("alice@yorku.ca", "R1",
                iso (LocalDateTime.now ().minusMinutes (10)), iso (LocalDateTime.now ().plusHours (2)),
                PaymentMethod.CREDIT_CARD, creditCardFields);
        assertTrue (facade.checkIn ("alice@yorku.ca", b.getBookingId ()));
    }

    @Test
    public void editBooking (){
        facade.createAccount ("alice@yorku.ca", "Passw0rd!", "STUDENT", "Alice", "123456789");
        Booking b= facade.bookRoom ("alice@yorku.ca", "R1",
                iso (LocalDateTime.now ().plusHours (2)), iso (LocalDateTime.now ().plusHours (4)),
                PaymentMethod.CREDIT_CARD, creditCardFields);
        assertTrue (facade.editBooking (b.getBookingId (), iso (LocalDateTime.now ().plusHours (3)), iso (LocalDateTime.now ().plusHours (5))));
    }

    @Test
    public void cancelBooking (){
        facade.createAccount ("alice@yorku.ca", "Passw0rd!", "STUDENT", "Alice", "123456789");
        Booking b= facade.bookRoom ("alice@yorku.ca", "R1",
                iso (LocalDateTime.now ().plusHours (1)), iso (LocalDateTime.now ().plusHours (2)),
                PaymentMethod.CREDIT_CARD, creditCardFields);
        assertTrue (facade.cancelBooking (b.getBookingId ()));
    }

    @Test
    public void extendBooking (){
        facade.createAccount ("alice@yorku.ca", "Passw0rd!", "STUDENT", "Alice", "123456789");
        Booking b= facade.bookRoom ("alice@yorku.ca", "R1",
                iso (LocalDateTime.now ().minusMinutes (30)), iso (LocalDateTime.now ().plusHours (1)),
                PaymentMethod.CREDIT_CARD, creditCardFields);
        assertTrue (facade.extendBooking (b.getBookingId (), iso (LocalDateTime.now ().plusHours (3))));
    }

    @Test
    public void payForBooking (){
        facade.createAccount ("alice@yorku.ca", "Passw0rd!", "STUDENT", "Alice", "123456789");
        Booking b= facade.bookRoom ("alice@yorku.ca", "R1",
                iso (LocalDateTime.now ().minusMinutes (10)), iso (LocalDateTime.now ().plusHours (2)),
                PaymentMethod.CREDIT_CARD, creditCardFields);
        facade.checkIn ("alice@yorku.ca", b.getBookingId ());
        double remaining= facade.payForBooking (b.getBookingId (), PaymentMethod.CREDIT_CARD, creditCardFields);
        assertTrue (remaining > 0);
    }

    @Test
    public void generateAdmin (){
        Administrator admin= facade.generateAdministratorAccount (TestChief.password (), "facade-admin-1", "Alice Admin", "alice.admin@yorku.ca");
        assertNotNull (admin);
    }

    @Test
    public void administratorFound (){
        facade.generateAdministratorAccount (TestChief.password (), "facade-admin-2", "Bob Admin", "bob.admin@yorku.ca");
        assertTrue (facade.isAdministrator ("facade-admin-2"));
    }

    @Test
    public void unknownAdministrator (){
        assertFalse (facade.isAdministrator ("no-such-admin-id"));
    }

    @Test
    public void addRoom (){
        facade.generateAdministratorAccount (TestChief.password (), "facade-admin-3", "Carl Admin", "carl.admin@yorku.ca");
        Room newRoom= new Room ("R2", 5, "Ross", "200", RoomStatus.AVAILABLE);
        assertTrue (facade.addRoom ("facade-admin-3", newRoom));
    }

    @Test
    public void addRoomWithoutAdmin (){
        Room newRoom= new Room ("R2", 5, "Ross", "200", RoomStatus.AVAILABLE);
        assertFalse (facade.addRoom ("not-an-admin", newRoom));
    }

    @Test
    public void enableRoom (){
        facade.generateAdministratorAccount (TestChief.password (), "facade-admin-4", "Dan Admin", "dan.admin@yorku.ca");
        roomManager.disableRoom ("R1");
        assertTrue (facade.enableRoom ("facade-admin-4", "R1"));
    }

    @Test
    public void enableRoomWithoutAdmin (){
        assertFalse (facade.enableRoom ("not-an-admin", "R1"));
    }

    @Test
    public void disableRoom (){
        facade.generateAdministratorAccount (TestChief.password (), "facade-admin-5", "Eve Admin", "eve.admin@yorku.ca");
        assertTrue (facade.disableRoom ("facade-admin-5", "R1"));
    }

    @Test
    public void closeRoom (){
        facade.generateAdministratorAccount (TestChief.password (), "facade-admin-6", "Frank Admin", "frank.admin@yorku.ca");
        assertTrue (facade.closeRoom ("facade-admin-6", "R1"));
    }

    @Test
    public void closeRoomWithoutAdmin (){
        assertFalse (facade.closeRoom ("not-an-admin", "R1"));
    }

    @Test
    public void findPayment (){
        facade.createAccount ("alice@yorku.ca", "Passw0rd!", "STUDENT", "Alice", "123456789");
        Booking b= facade.bookRoom ("alice@yorku.ca", "R1",
                iso (LocalDateTime.now ().plusHours (1)), iso (LocalDateTime.now ().plusHours (2)),
                PaymentMethod.CREDIT_CARD, creditCardFields);
        assertNotNull (facade.findPaymentByBookingId (b.getBookingId ()));
    }

    @Test
    public void allRooms (){
        assertEquals (1, facade.getAllRooms ().size ());
    }

    @Test
    public void allBookings (){
        facade.createAccount ("alice@yorku.ca", "Passw0rd!", "STUDENT", "Alice", "123456789");
        facade.bookRoom ("alice@yorku.ca", "R1",
                iso (LocalDateTime.now ().plusHours (1)), iso (LocalDateTime.now ().plusHours (2)),
                PaymentMethod.CREDIT_CARD, creditCardFields);
        assertEquals (1, facade.getAllBookings ().size ());
    }
}

