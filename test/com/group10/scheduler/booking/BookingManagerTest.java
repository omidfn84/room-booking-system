package com.group10.scheduler.booking;

import static org.junit.Assert.*;
import org.junit.*;
import java.time.LocalDateTime;
import java.util.List;
import com.group10.scheduler.accounts.RegisteredUser;
import com.group10.scheduler.accounts.Student;
import com.group10.scheduler.room.FakeRoomRepository;
import com.group10.scheduler.room.Room;
import com.group10.scheduler.room.RoomManager;
import com.group10.scheduler.room.RoomStatus;
public class BookingManagerTest{
    private FakeBookingRepository bookingRepo;
    private FakePaymentRepository paymentRepo;
    private RoomManager roomManager;
    private BookingManager bookingManager;
    private Room room;
    private RegisteredUser student;
    private String iso (LocalDateTime t){
        return t.toString ();
    }
    @Before
    public void setUp (){
        bookingRepo= new FakeBookingRepository ();
        paymentRepo= new FakePaymentRepository ();
        roomManager= new RoomManager (new FakeRoomRepository ());
        room= new Room ("R1", 10, "Bergeron", "100", RoomStatus.AVAILABLE);
        roomManager.addRoom (room);
        bookingManager= new BookingManager (bookingRepo, paymentRepo, roomManager);
        student= new Student ("alice@yorku.ca", "Passw0rd!", "STUDENT", "Alice", 123456789L);
    }
    private String [] validCreditCardFields (){
        return new String[]{"1234567890123456", "12/30", "123"};
    }
    @Test
    public void bookRoom (){
        Booking b= bookingManager.bookRoom (student, room, iso (LocalDateTime.now ().plusHours (1)), iso (LocalDateTime.now ().plusHours (3)), PaymentMethod.CREDIT_CARD, validCreditCardFields ());
        assertNotNull (b);
        assertEquals (BookingStatus.CONFIRMED, b.getStatus ());
        assertEquals (20.0, b.getDepositAmount (), 0.0001);
        assertEquals (1, bookingRepo.saveCallCount);
        assertEquals (1, paymentRepo.saveCallCount);
    }
    @Test (expected= IllegalArgumentException.class)
    public void nullUser (){
        bookingManager.bookRoom (null, room, iso (LocalDateTime.now ().plusHours (1)), iso (LocalDateTime.now ().plusHours (2)), PaymentMethod.CREDIT_CARD, validCreditCardFields ());
    }
    @Test (expected= IllegalArgumentException.class)
    public void nullRoom (){
        bookingManager.bookRoom (student, null, iso (LocalDateTime.now ().plusHours (1)), iso (LocalDateTime.now ().plusHours (2)), PaymentMethod.CREDIT_CARD, validCreditCardFields ());
    }
    @Test (expected= IllegalArgumentException.class)
    public void badDates (){
        bookingManager.bookRoom (student, room, "not-a-date", "also-not-a-date", PaymentMethod.CREDIT_CARD, validCreditCardFields ());
    }
    @Test (expected= IllegalArgumentException.class)
    public void invalidTimeRange (){
        LocalDateTime t= LocalDateTime.now ().plusHours (2);
        bookingManager.bookRoom (student, room, iso (t), iso (t), PaymentMethod.CREDIT_CARD, validCreditCardFields ());
    }
    @Test (expected= IllegalStateException.class)
    public void unavailableRoom (){
        room.disable ();
        bookingManager.bookRoom (student, room, iso (LocalDateTime.now ().plusHours (1)), iso (LocalDateTime.now ().plusHours (2)), PaymentMethod.CREDIT_CARD, validCreditCardFields ());
    }
    @Test (expected= IllegalStateException.class)
    public void bookingConflict (){
        LocalDateTime start= LocalDateTime.now ().plusHours (1);
        LocalDateTime end= LocalDateTime.now ().plusHours (3);
        bookingManager.bookRoom (student, room, iso (start), iso (end), PaymentMethod.CREDIT_CARD, validCreditCardFields ());
        // Same room and overlapping time.
        bookingManager.bookRoom (student, room, iso (start.plusMinutes (30)), iso (end.plusMinutes (30)), PaymentMethod.CREDIT_CARD, validCreditCardFields ());
    }
    @Test (expected= IllegalStateException.class)
    public void depositFailure (){
        // An empty card number makes the payment fail.
        bookingManager.bookRoom (student, room, iso (LocalDateTime.now ().plusHours (1)), iso (LocalDateTime.now ().plusHours (2)), PaymentMethod.CREDIT_CARD, "", "12/30", "123");
    }
    @Test (expected= IllegalArgumentException.class)
    public void nullPaymentMethod (){
        bookingManager.bookRoom (student, room, iso (LocalDateTime.now ().plusHours (1)), iso (LocalDateTime.now ().plusHours (2)), null, validCreditCardFields ());
    }
    @Test
    public void checkInWorks (){
        Booking b= bookingManager.bookRoom (student, room, iso (LocalDateTime.now ().minusMinutes (10)), iso (LocalDateTime.now ().plusHours (2)), PaymentMethod.CREDIT_CARD, validCreditCardFields ());
        assertTrue (bookingManager.checkIn (student.getEmail (), b.getBookingId ()));
        assertEquals (BookingStatus.CHECKED_IN, b.getStatus ());
    }
    @Test
    public void missingBookingCheckIn (){
        assertFalse (bookingManager.checkIn ("alice@yorku.ca", "no-such-booking"));
    }
    @Test
    public void wrongUserCheckIn (){
        Booking b= bookingManager.bookRoom (student, room, iso (LocalDateTime.now ().minusMinutes (10)),
                iso (LocalDateTime.now ().plusHours (2)), PaymentMethod.CREDIT_CARD, validCreditCardFields ());
        assertFalse (bookingManager.checkIn ("someoneelse@yorku.ca", b.getBookingId ()));
    }
    @Test
    public void earlyCheckIn (){
        Booking b= bookingManager.bookRoom (student, room, iso (LocalDateTime.now ().plusHours (1)),
                iso (LocalDateTime.now ().plusHours (3)), PaymentMethod.CREDIT_CARD, validCreditCardFields ());
        assertFalse (bookingManager.checkIn (student.getEmail (), b.getBookingId ()));
    }
    @Test
    public void lateCheckIn (){
        Booking b= bookingManager.bookRoom (student, room, iso (LocalDateTime.now ().minusMinutes (1)), iso (LocalDateTime.now ().plusHours (2)), PaymentMethod.CREDIT_CARD, validCreditCardFields ());
        // Move the start time outside the checkin window.
        b.setStartTime (iso (LocalDateTime.now ().minusMinutes (45)));
        assertFalse (bookingManager.checkIn (student.getEmail (), b.getBookingId ()));
        assertEquals (BookingStatus.EXPIRED, b.getStatus ());
        assertTrue (b.isDepositForfeited ());
    }
    @Test
    public void missingRoomCheckin (){
        Booking orphan= new Booking ("orphan1", "alice@yorku.ca", "NO-SUCH-ROOM", iso (LocalDateTime.now ().minusMinutes (5)), iso (LocalDateTime.now ().plusHours (2)), 20.0, BookingStatus.CONFIRMED);
        FakeBookingRepository preloaded= new FakeBookingRepository (List.of (orphan));
        BookingManager bm= new BookingManager (preloaded, new FakePaymentRepository (), roomManager);
        assertFalse (bm.checkIn ("alice@yorku.ca", "orphan1"));
    }
    @Test
    public void editBooking (){
        Booking b= bookingManager.bookRoom (student, room, iso (LocalDateTime.now ().plusHours (2)), iso (LocalDateTime.now ().plusHours (4)), PaymentMethod.CREDIT_CARD, validCreditCardFields ());
        String newStart= iso (LocalDateTime.now ().plusHours (3));
        String newEnd= iso (LocalDateTime.now ().plusHours (5));
        assertTrue (bookingManager.editBooking (b.getBookingId (), newStart, newEnd));
    }
    @Test
    public void editMissingBooking (){
        assertFalse (bookingManager.editBooking ("no-such-booking", "x", "y"));
    }
    @Test
    public void editConflict (){
        Room room2= new Room ("R2", 5, "Ross", "200", RoomStatus.AVAILABLE);
        roomManager.addRoom (room2);
        LocalDateTime start= LocalDateTime.now ().plusHours (1);
        LocalDateTime end= LocalDateTime.now ().plusHours (3);
        Booking b1= bookingManager.bookRoom (student, room, iso (start), iso (end), PaymentMethod.CREDIT_CARD, validCreditCardFields ());
        Booking b2= bookingManager.bookRoom (student, room, iso (start.plusHours (5)), iso (end.plusHours (5)), PaymentMethod.CREDIT_CARD, validCreditCardFields ());
        // Move the second booking into the first booking's time.
        assertFalse (bookingManager.editBooking (b2.getBookingId (), iso (start), iso (end)));
    }
    @Test
    public void cancelBooking (){
        Booking b= bookingManager.bookRoom (student, room, iso (LocalDateTime.now ().plusHours (1)), iso (LocalDateTime.now ().plusHours (2)), PaymentMethod.CREDIT_CARD, validCreditCardFields ());
        assertTrue (bookingManager.cancelBooking (b.getBookingId ()));
        assertEquals (BookingStatus.CANCELLED, b.getStatus ());
        Payment payment= bookingManager.findPaymentByBookingId (b.getBookingId ());
        assertEquals (PaymentStatus.REFUNDED, payment.getStatus ());
    }
    @Test
    public void cancelMissingBooking (){
        assertFalse (bookingManager.cancelBooking ("no-such-booking"));
    }
    @Test
    public void lateCancellation (){
        Booking b= bookingManager.bookRoom (student, room, iso (LocalDateTime.now ().minusMinutes (5)), iso (LocalDateTime.now ().plusHours (2)), PaymentMethod.CREDIT_CARD, validCreditCardFields ());
        assertFalse (bookingManager.cancelBooking (b.getBookingId ()));
    }
    @Test
    public void extendBooking (){
        Booking b= bookingManager.bookRoom (student, room, iso (LocalDateTime.now ().minusMinutes (30)), iso (LocalDateTime.now ().plusHours (1)), PaymentMethod.CREDIT_CARD, validCreditCardFields ());
        assertTrue (bookingManager.extendBooking (b.getBookingId (), iso (LocalDateTime.now ().plusHours (3))));
    }
    @Test
    public void extendMissingBooking (){
        assertFalse (bookingManager.extendBooking ("no-such-booking", iso (LocalDateTime.now ().plusHours (1))));
    }
    @Test
    public void extendConflict (){
        LocalDateTime start1= LocalDateTime.now ().minusMinutes (30);
        LocalDateTime end1= LocalDateTime.now ().plusHours (1);
        Booking b1= bookingManager.bookRoom (student, room, iso (start1), iso (end1), PaymentMethod.CREDIT_CARD, validCreditCardFields ());
        LocalDateTime start2= end1.plusMinutes (30);
        LocalDateTime end2= start2.plusHours (1);
        bookingManager.bookRoom (student, room, iso (start2), iso (end2), PaymentMethod.CREDIT_CARD, validCreditCardFields ());
        // The extension overllaps another booking.
        assertFalse (bookingManager.extendBooking (b1.getBookingId (), iso (end2.minusMinutes (1))));
    }
    @Test
    public void payMissingBooking (){
        assertEquals (-1.0, bookingManager.payForBooking ("no-such-booking", PaymentMethod.CREDIT_CARD, validCreditCardFields ()), 0.0001);
    }
    @Test
    public void noRemainingBalance (){
        // A booking with no cost should not create another payment.
        LocalDateTime t= LocalDateTime.now ().plusHours (1);
        Booking b= new Booking ("BK1", "alice@yorku.ca", "R1", iso (t), iso (t), 20.0, BookingStatus.CONFIRMED);
        FakeBookingRepository preloaded= new FakeBookingRepository (List.of (b));
        BookingManager bm= new BookingManager (preloaded, new FakePaymentRepository (), roomManager);
        assertEquals (0.0, bm.payForBooking ("BK1", PaymentMethod.CREDIT_CARD, validCreditCardFields ()), 0.0001);
    }
    @Test
    public void paymentCompletesBooking (){
    	Booking b= bookingManager.bookRoom (student, room, iso (LocalDateTime.now ().minusMinutes (10)), iso (LocalDateTime.now ().plusHours (2)), PaymentMethod.CREDIT_CARD, validCreditCardFields ());
        bookingManager.checkIn (student.getEmail (), b.getBookingId ());
        double remaining= bookingManager.payForBooking (b.getBookingId (), PaymentMethod.CREDIT_CARD, validCreditCardFields ());
        assertTrue (remaining > 0);
        assertEquals (BookingStatus.COMPLETED, b.getStatus ());
    }
    @Test
    public void findAvailableRoom (){
        List <Room> rooms= bookingManager.getBookableRooms (iso (LocalDateTime.now ().plusHours (1)), iso (LocalDateTime.now ().plusHours (2)));
        assertTrue (rooms.contains (room));
    }
    @Test
    public void searchBadDates (){
        assertTrue (bookingManager.getBookableRooms ("bad", "worse").isEmpty ());
    }
    @Test
    public void searchInvalidRange (){
        LocalDateTime t= LocalDateTime.now ().plusHours (1);
        assertTrue (bookingManager.getBookableRooms (iso (t), iso (t)).isEmpty ());
    }
    @Test
    public void searchIgnoresBookedRoom (){
        LocalDateTime start= LocalDateTime.now ().plusHours (1);
        LocalDateTime end= LocalDateTime.now ().plusHours (3);
        bookingManager.bookRoom (student, room, iso (start), iso (end), PaymentMethod.CREDIT_CARD, validCreditCardFields ());
        List <Room> rooms= bookingManager.getBookableRooms (iso (start.plusMinutes (30)), iso (end.plusMinutes (30)));
        assertFalse (rooms.contains (room));
    }
    @Test
    public void searchCancelled (){
        LocalDateTime start= LocalDateTime.now ().plusHours (1);
        LocalDateTime end= LocalDateTime.now ().plusHours (3);
        Booking b= bookingManager.bookRoom (student, room, iso (start), iso (end), PaymentMethod.CREDIT_CARD, validCreditCardFields ());
        bookingManager.cancelBooking (b.getBookingId ());
        List <Room> rooms= bookingManager.getBookableRooms (iso (start), iso (end));
        assertTrue (rooms.contains (room));
    }
    @Test
    public void allBookings (){
        bookingManager.bookRoom (student, room, iso (LocalDateTime.now ().plusHours (1)), iso (LocalDateTime.now ().plusHours (2)), PaymentMethod.CREDIT_CARD, validCreditCardFields ());
        assertEquals (1, bookingManager.getAllBookings ().size ());
    }
    @Test
    public void allPayments (){
        bookingManager.bookRoom (student, room, iso (LocalDateTime.now ().plusHours (1)), iso (LocalDateTime.now ().plusHours (2)), PaymentMethod.CREDIT_CARD, validCreditCardFields ());
        assertEquals (1, bookingManager.getAllPayments ().size ());
    }
    @Test
    public void missingBooking (){
        assertNull (bookingManager.findBooking ("no-such-id"));
    }
    @Test
    public void missingPayment (){
        assertNull (bookingManager.findPaymentByBookingId ("no-such-id"));
    }
    @Test
    public void loadRepositories (){
        Booking preloadedBooking= new Booking ("BK-preload", "alice@yorku.ca", "R1", iso (LocalDateTime.now ().plusHours (1)), iso (LocalDateTime.now ().plusHours (2)), 20.0, BookingStatus.CONFIRMED);
        FakeBookingRepository preloadedRepo= new FakeBookingRepository (List.of (preloadedBooking));
        BookingManager bm= new BookingManager (preloadedRepo, new FakePaymentRepository (), roomManager);
        assertNotNull (bm.findBooking ("BK-preload"));
    }
    @Test (expected= IllegalArgumentException.class)
    public void wrongCredit (){
        bookingManager.bookRoom (student, room, iso (LocalDateTime.now ().plusHours (1)), iso (LocalDateTime.now ().plusHours (2)), PaymentMethod.CREDIT_CARD, "only-one-field");
    }
    @Test
    public void debitPayment (){
        Booking b= bookingManager.bookRoom (student, room, iso (LocalDateTime.now ().plusHours (1)), iso (LocalDateTime.now ().plusHours (2)), PaymentMethod.DEBIT_CARD, "1234567890123456", "1234");
        assertNotNull (b);
    }
    @Test
    public void institutionalPayment (){
        Booking b= bookingManager.bookRoom (student, room, iso (LocalDateTime.now ().plusHours (1)), iso (LocalDateTime.now ().plusHours (2)), PaymentMethod.INSTITUTIONAL_BILLING, "123456789", "ACC-1");
        assertNotNull (b);
    }
    @Test (expected= IllegalArgumentException.class)
    public void badOrganization (){
        bookingManager.bookRoom (student, room, iso (LocalDateTime.now ().plusHours (1)), iso (LocalDateTime.now ().plusHours (2)), PaymentMethod.INSTITUTIONAL_BILLING, "not-a-number", "ACC-1");
    }
}

