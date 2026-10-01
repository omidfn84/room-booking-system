package com.group10.scheduler.booking;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.group10.scheduler.accounts.Faculty;
import com.group10.scheduler.accounts.Partner;
import com.group10.scheduler.accounts.RegisteredUser;
import com.group10.scheduler.accounts.Staff;
import com.group10.scheduler.accounts.Student;
import com.group10.scheduler.aisupport.AIFakes;
import com.group10.scheduler.aisupport.AIFixture;
import com.group10.scheduler.room.Room;
import com.group10.scheduler.room.RoomManager;
import com.group10.scheduler.room.RoomStatus;

/**
 * BookingManager is where most of the requirements actually live:
 *
 *   Req3  - book an available room, deposit set from the user's hourly rate
 *   Req4  - deposit charged upfront; 30-minute check-in window; forfeiture
 *   Req5  - sensor badge scan + occupancy check during check-in
 *   Req8  - edit/cancel before start, with a deposit refund on cancel
 *   Req9  - extend before expiry if the room is free for the extra time
 *   Req10 - the three payment methods, via buildStrategy()
 *
 * Times are always built relative to now (never hard-coded), because every
 * rule above is evaluated against LocalDateTime.now().
 */
public class BookingManagerAITest {

    private AIFakes.FakeRoomRepo roomRepo;
    private AIFakes.FakeBookingRepo bookingRepo;
    private AIFakes.FakePaymentRepo paymentRepo;
    private RoomManager roomManager;
    private BookingManager manager;
    private Room room;
    private RegisteredUser student;

    private static final String[] CARD = {"4111111111111111", "12/27", "123"};

    @Before
    public void setUp() {
        roomRepo = new AIFakes.FakeRoomRepo();
        bookingRepo = new AIFakes.FakeBookingRepo();
        paymentRepo = new AIFakes.FakePaymentRepo();
        roomManager = new RoomManager(roomRepo);
        manager = new BookingManager(bookingRepo, paymentRepo, roomManager);

        room = new Room("R1", 10, "Bergeron", "100", RoomStatus.AVAILABLE);
        roomManager.addRoom(room);
        student = new Student("alice@yorku.ca", "Passw0rd!", "STUDENT", "Alice", 123456789L);
    }

    /** Books R1 for a window that has not started yet. */
    private Booking bookFuture() {
        return manager.bookRoom(student, room, AIFixture.futureStart(), AIFixture.futureEnd(),
                PaymentMethod.CREDIT_CARD, CARD);
    }

    // ==================== Req3: booking a room ====================

    @Test
    public void bookRoom_createsAConfirmedBookingLinkedToTheUserAndRoom() {
        Booking booking = bookFuture();

        assertNotNull(booking);
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals("alice@yorku.ca", booking.getUserEmail());
        assertEquals("R1", booking.getRoomId());
        assertNotNull(booking.getBookingId());
    }

    @Test
    public void bookRoom_setsTheDepositFromTheUsersHourlyRate() {
        // Req3's rate table, charged as Req4's one-hour deposit:
        // student $20, faculty $30, staff $40, partner $50.
        assertEquals(20.0, bookFuture().getDepositAmount(), 0.001);

        Room r2 = new Room("R2", 10, "Bergeron", "200", RoomStatus.AVAILABLE);
        Room r3 = new Room("R3", 10, "Bergeron", "300", RoomStatus.AVAILABLE);
        Room r4 = new Room("R4", 10, "Bergeron", "400", RoomStatus.AVAILABLE);
        roomManager.addRoom(r2);
        roomManager.addRoom(r3);
        roomManager.addRoom(r4);

        RegisteredUser faculty = new Faculty("f@yorku.ca", "Passw0rd!", "FACULTY", "Fay", 1L);
        RegisteredUser staff = new Staff("s@yorku.ca", "Passw0rd!", "STAFF", "Sam", 2L);
        RegisteredUser partner = new Partner("p@corp.com", "Passw0rd!", "PARTNER", "Pat", 3L);

        assertEquals(30.0, manager.bookRoom(faculty, r2, AIFixture.futureStart(), AIFixture.futureEnd(),
                PaymentMethod.CREDIT_CARD, CARD).getDepositAmount(), 0.001);
        assertEquals(40.0, manager.bookRoom(staff, r3, AIFixture.futureStart(), AIFixture.futureEnd(),
                PaymentMethod.CREDIT_CARD, CARD).getDepositAmount(), 0.001);
        assertEquals(50.0, manager.bookRoom(partner, r4, AIFixture.futureStart(), AIFixture.futureEnd(),
                PaymentMethod.CREDIT_CARD, CARD).getDepositAmount(), 0.001);
    }

    @Test
    public void bookRoom_chargesTheDepositUpfrontAndPersistsBothRecords() {
        // Req4: "One hour's fee is charged upfront."
        Booking booking = bookFuture();

        Payment payment = manager.findPaymentByBookingId(booking.getBookingId());
        assertNotNull("A booking must produce a deposit payment", payment);
        assertEquals(PaymentStatus.PAID, payment.getStatus());
        assertEquals(20.0, payment.getAmount(), 0.001);

        assertEquals(1, bookingRepo.saveCount);
        assertEquals(1, paymentRepo.saveCount);
    }

    @Test
    public void bookRoom_rejectsANullUserOrRoom() {
        try {
            manager.bookRoom(null, room, AIFixture.futureStart(), AIFixture.futureEnd(), PaymentMethod.CREDIT_CARD, CARD);
            fail("Expected IllegalArgumentException for a null user");
        } catch (IllegalArgumentException expected) {
            // the facade passes null when an email or room id does not exist
        }

        try {
            manager.bookRoom(student, null, AIFixture.futureStart(), AIFixture.futureEnd(), PaymentMethod.CREDIT_CARD, CARD);
            fail("Expected IllegalArgumentException for a null room");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void bookRoom_rejectsUnparseableOrInvertedTimes() {
        try {
            manager.bookRoom(student, room, "not-a-time", AIFixture.futureEnd(), PaymentMethod.CREDIT_CARD, CARD);
            fail("Expected IllegalArgumentException for an unparseable start time");
        } catch (IllegalArgumentException expected) {
        }

        try {
            manager.bookRoom(student, room, AIFixture.hoursFromNow(4), AIFixture.hoursFromNow(2), PaymentMethod.CREDIT_CARD, CARD);
            fail("Expected IllegalArgumentException when end precedes start");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void bookRoom_refusesARoomThatIsNotAvailable() {
        // Req6: a disabled or under-maintenance room must not be bookable.
        room.disable();

        try {
            bookFuture();
            fail("Expected IllegalStateException for a disabled room");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("R1"));
        }
    }

    @Test
    public void bookRoom_refusesToDoubleBookTheSameRoomAndWindow() {
        String start = AIFixture.futureStart();
        String end = AIFixture.futureEnd();
        manager.bookRoom(student, room, start, end, PaymentMethod.CREDIT_CARD, CARD);

        try {
            manager.bookRoom(student, room, start, end, PaymentMethod.CREDIT_CARD, CARD);
            fail("Expected IllegalStateException for an overlapping booking");
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void bookRoom_allowsTheSameRoomForANonOverlappingWindow() {
        manager.bookRoom(student, room, AIFixture.hoursFromNow(2), AIFixture.hoursFromNow(3), PaymentMethod.CREDIT_CARD, CARD);

        Booking later = manager.bookRoom(student, room, AIFixture.hoursFromNow(4), AIFixture.hoursFromNow(5),
                PaymentMethod.CREDIT_CARD, CARD);

        assertNotNull(later);
        assertEquals(2, manager.getAllBookings().size());
    }

    // ==================== Req10: buildStrategy, reached through bookRoom ====================

    @Test
    public void bookRoom_acceptsAllThreePaymentMethodsWithTheirOwnFieldSets() {
        Room r2 = new Room("R2", 10, "Bergeron", "200", RoomStatus.AVAILABLE);
        Room r3 = new Room("R3", 10, "Bergeron", "300", RoomStatus.AVAILABLE);
        roomManager.addRoom(r2);
        roomManager.addRoom(r3);

        assertNotNull(manager.bookRoom(student, room, AIFixture.futureStart(), AIFixture.futureEnd(),
                PaymentMethod.CREDIT_CARD, "4111111111111111", "12/27", "123"));
        assertNotNull(manager.bookRoom(student, r2, AIFixture.futureStart(), AIFixture.futureEnd(),
                PaymentMethod.DEBIT_CARD, "4111222233334444", "1234"));
        assertNotNull(manager.bookRoom(student, r3, AIFixture.futureStart(), AIFixture.futureEnd(),
                PaymentMethod.INSTITUTIONAL_BILLING, "987654321", "YORK-2026"));
    }

    @Test
    public void bookRoom_rejectsTheWrongNumberOfPaymentFieldsForEachMethod() {
        // Each method has a fixed field count; a mismatch means the GUI sent
        // the wrong dialog's values and must not be silently accepted.
        try {
            manager.bookRoom(student, room, AIFixture.futureStart(), AIFixture.futureEnd(),
                    PaymentMethod.CREDIT_CARD, "4111111111111111", "12/27");
            fail("Credit card needs exactly three fields");
        } catch (IllegalArgumentException expected) {
        }

        try {
            manager.bookRoom(student, room, AIFixture.futureStart(), AIFixture.futureEnd(),
                    PaymentMethod.DEBIT_CARD, "4111222233334444");
            fail("Debit card needs exactly two fields");
        } catch (IllegalArgumentException expected) {
        }

        try {
            manager.bookRoom(student, room, AIFixture.futureStart(), AIFixture.futureEnd(),
                    PaymentMethod.INSTITUTIONAL_BILLING, "987654321");
            fail("Institutional billing needs exactly two fields");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void bookRoom_rejectsANonNumericOrganisationId() {
        try {
            manager.bookRoom(student, room, AIFixture.futureStart(), AIFixture.futureEnd(),
                    PaymentMethod.INSTITUTIONAL_BILLING, "not-a-number", "YORK-2026");
            fail("Expected IllegalArgumentException for a non-numeric organisation id");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Organization ID"));
        }
    }

    @Test
    public void bookRoom_rejectsANullPaymentMethod() {
        try {
            manager.bookRoom(student, room, AIFixture.futureStart(), AIFixture.futureEnd(), null, CARD);
            fail("Expected IllegalArgumentException for a null payment method");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void bookRoom_failsWhenTheDepositPaymentIsDeclined() {
        // Empty card fields make CreditCardStrategy.pay() return false; no
        // booking may be created for a payment that did not go through.
        try {
            manager.bookRoom(student, room, AIFixture.futureStart(), AIFixture.futureEnd(),
                    PaymentMethod.CREDIT_CARD, "", "", "");
            fail("Expected IllegalStateException when the deposit is declined");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("deposit"));
        }
        assertTrue("No booking may survive a failed deposit", manager.getAllBookings().isEmpty());
    }

    // ==================== Req4 + Req5: check-in ====================

    @Test
    public void checkIn_succeedsInsideTheThirtyMinuteWindowAndRecordsTheTime() {
        Booking booking = manager.bookRoom(student, room, AIFixture.startedRecently(), AIFixture.hoursFromNow(1),
                PaymentMethod.CREDIT_CARD, CARD);

        assertTrue(manager.checkIn("alice@yorku.ca", booking.getBookingId()));
        assertEquals(BookingStatus.CHECKED_IN, booking.getStatus());
        assertNotNull(booking.getCheckInTime());
    }

    @Test
    public void checkIn_marksTheRoomOccupiedViaTheBadgeScan() {
        // Req5: the scan is what flips the sensor to occupied, and the
        // occupancy check then has to agree before check-in is allowed.
        Booking booking = manager.bookRoom(student, room, AIFixture.startedRecently(), AIFixture.hoursFromNow(1),
                PaymentMethod.CREDIT_CARD, CARD);
        assertFalse("Room starts unoccupied", room.getSensorSystem().detectOccupancy());

        manager.checkIn("alice@yorku.ca", booking.getBookingId());

        assertTrue("The badge scan must report the room as occupied", room.getSensorSystem().detectOccupancy());
    }

    @Test
    public void checkIn_isRefusedBeforeTheBookingHasEvenStarted() {
        Booking booking = bookFuture();

        assertFalse(manager.checkIn("alice@yorku.ca", booking.getBookingId()));
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
    }

    @Test
    public void checkIn_expiresTheBookingAndForfeitsTheDepositAfterThirtyMinutes() {
        // Req4: "If the user does not check in within 30 minutes of the start
        // time, the deposit is lost."
        Booking booking = manager.bookRoom(student, room, AIFixture.startedLongAgo(), AIFixture.hoursFromNow(1),
                PaymentMethod.CREDIT_CARD, CARD);

        assertFalse(manager.checkIn("alice@yorku.ca", booking.getBookingId()));
        assertEquals(BookingStatus.EXPIRED, booking.getStatus());
        assertTrue("The deposit must be forfeited, not refunded", booking.isDepositForfeited());
    }

    @Test
    public void checkIn_refusesAUserWhoDoesNotOwnTheBooking() {
        Booking booking = manager.bookRoom(student, room, AIFixture.startedRecently(), AIFixture.hoursFromNow(1),
                PaymentMethod.CREDIT_CARD, CARD);

        assertFalse(manager.checkIn("mallory@yorku.ca", booking.getBookingId()));
        assertFalse(manager.checkIn(null, booking.getBookingId()));
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
    }

    @Test
    public void checkIn_returnsFalseForAnUnknownBookingId() {
        assertFalse(manager.checkIn("alice@yorku.ca", "no-such-booking"));
    }

    @Test
    public void checkIn_returnsFalseWhenTheRoomNoLongerExists() {
        // The booking references a room id; if the room record is gone there
        // is no sensor to satisfy Req5.
        Booking orphan = new Booking("ORPHAN", "alice@yorku.ca", "GHOST-ROOM",
                AIFixture.startedRecently(), AIFixture.hoursFromNow(1), 20.0, BookingStatus.CONFIRMED);
        BookingManager withOrphan = new BookingManager(
                new AIFakes.FakeBookingRepo(List.of(orphan)), new AIFakes.FakePaymentRepo(), roomManager);

        assertFalse(withOrphan.checkIn("alice@yorku.ca", "ORPHAN"));
    }

    @Test
    public void checkIn_returnsFalseWhenTheStoredStartTimeIsUnparseable() {
        Booking corrupt = new Booking("BAD", "alice@yorku.ca", "R1",
                "not-a-timestamp", AIFixture.hoursFromNow(1), 20.0, BookingStatus.CONFIRMED);
        BookingManager withCorrupt = new BookingManager(
                new AIFakes.FakeBookingRepo(List.of(corrupt)), new AIFakes.FakePaymentRepo(), roomManager);

        assertFalse(withCorrupt.checkIn("alice@yorku.ca", "BAD"));
    }

    // ==================== Req8: edit and cancel ====================

    @Test
    public void editBooking_movesBothTimesAndPersistsTheChange() {
        Booking booking = bookFuture();
        int savesBefore = bookingRepo.saveCount;
        String newStart = AIFixture.hoursFromNow(6);
        String newEnd = AIFixture.hoursFromNow(7);

        assertTrue(manager.editBooking(booking.getBookingId(), newStart, newEnd));

        assertEquals(newStart, booking.getStartTime());
        assertEquals(newEnd, booking.getEndTime());
        assertEquals(savesBefore + 1, bookingRepo.saveCount);
    }

    @Test
    public void editBooking_returnsFalseForAnUnknownBookingId() {
        assertFalse(manager.editBooking("no-such-booking", AIFixture.futureStart(), AIFixture.futureEnd()));
    }

    @Test
    public void editBooking_refusesToMoveOntoAnotherBookingOfTheSameRoom() {
        Booking first = manager.bookRoom(student, room, AIFixture.hoursFromNow(2), AIFixture.hoursFromNow(3),
                PaymentMethod.CREDIT_CARD, CARD);
        manager.bookRoom(student, room, AIFixture.hoursFromNow(5), AIFixture.hoursFromNow(6),
                PaymentMethod.CREDIT_CARD, CARD);

        assertFalse("Editing into an occupied window must be refused",
                manager.editBooking(first.getBookingId(), AIFixture.hoursFromNow(5), AIFixture.hoursFromNow(6)));
    }

    @Test
    public void editBooking_ignoresTheBookingsOwnWindowWhenCheckingForConflicts() {
        // Without the excludeBookingId guard a booking would collide with
        // itself and no edit could ever succeed.
        Booking booking = manager.bookRoom(student, room, AIFixture.hoursFromNow(2), AIFixture.hoursFromNow(4),
                PaymentMethod.CREDIT_CARD, CARD);

        assertTrue(manager.editBooking(booking.getBookingId(), AIFixture.hoursFromNow(3), AIFixture.hoursFromNow(5)));
    }

    @Test
    public void cancelBooking_succeedsBeforeStartAndRefundsTheDeposit() {
        Booking booking = bookFuture();
        Payment deposit = manager.findPaymentByBookingId(booking.getBookingId());
        assertEquals(PaymentStatus.PAID, deposit.getStatus());

        assertTrue(manager.cancelBooking(booking.getBookingId()));

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertEquals("Req8 cancellation must refund the deposit", PaymentStatus.REFUNDED, deposit.getStatus());
    }

    @Test
    public void cancelBooking_isRefusedOnceTheBookingHasStarted() {
        Booking booking = manager.bookRoom(student, room, AIFixture.startedRecently(), AIFixture.hoursFromNow(1),
                PaymentMethod.CREDIT_CARD, CARD);

        assertFalse(manager.cancelBooking(booking.getBookingId()));
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
    }

    @Test
    public void cancelBooking_returnsFalseForAnUnknownBookingId() {
        assertFalse(manager.cancelBooking("no-such-booking"));
    }

    @Test
    public void cancelBooking_freesTheRoomForSomebodyElse() {
        String start = AIFixture.futureStart();
        String end = AIFixture.futureEnd();
        Booking booking = manager.bookRoom(student, room, start, end, PaymentMethod.CREDIT_CARD, CARD);

        manager.cancelBooking(booking.getBookingId());

        assertNotNull("A cancelled slot must become bookable again",
                manager.bookRoom(student, room, start, end, PaymentMethod.CREDIT_CARD, CARD));
    }

    // ==================== Req9: extend ====================

    @Test
    public void extendBooking_pushesTheEndTimeOutAndPersists() {
        Booking booking = manager.bookRoom(student, room, AIFixture.startedRecently(), AIFixture.hoursFromNow(1),
                PaymentMethod.CREDIT_CARD, CARD);
        String later = AIFixture.hoursFromNow(3);

        assertTrue(manager.extendBooking(booking.getBookingId(), later));
        assertEquals(later, booking.getEndTime());
    }

    @Test
    public void extendBooking_returnsFalseForAnUnknownBookingId() {
        assertFalse(manager.extendBooking("no-such-booking", AIFixture.hoursFromNow(3)));
    }

    @Test
    public void extendBooking_isRefusedWhenTheRoomIsBookedForTheExtraTime() {
        // Req9: "if the room is available" - the extension window itself has
        // to be conflict free, not just the original booking.
        Booking first = manager.bookRoom(student, room, AIFixture.startedRecently(), AIFixture.hoursFromNow(1),
                PaymentMethod.CREDIT_CARD, CARD);
        manager.bookRoom(student, room, AIFixture.hoursFromNow(2), AIFixture.hoursFromNow(4),
                PaymentMethod.CREDIT_CARD, CARD);

        assertFalse(manager.extendBooking(first.getBookingId(), AIFixture.hoursFromNow(3)));
    }

    // ==================== Req4 + Req10: paying the balance ====================

    @Test
    public void payForBooking_chargesTheBalanceAfterTheDepositAndCompletesTheBooking() {
        Booking booking = manager.bookRoom(student, room, AIFixture.startedRecently(), AIFixture.hoursFromNow(1),
                PaymentMethod.CREDIT_CARD, CARD);
        manager.checkIn("alice@yorku.ca", booking.getBookingId());

        double finalCost = booking.calculateFinalCost();
        double paid = manager.payForBooking(booking.getBookingId(), PaymentMethod.CREDIT_CARD, CARD);

        assertEquals("Deposit must be credited against the final cost",
                finalCost - booking.getDepositAmount(), paid, 0.01);
        assertEquals(BookingStatus.COMPLETED, booking.getStatus());
    }

    @Test
    public void payForBooking_returnsMinusOneForAnUnknownBooking() {
        assertEquals(-1.0, manager.payForBooking("no-such-booking", PaymentMethod.CREDIT_CARD, CARD), 0.001);
    }

    @Test
    public void payForBooking_returnsZeroWhenNothingIsOwed() {
        // A one-hour booking costs exactly one hour's fee, which the deposit
        // already covers once the user has checked in.
        Booking booking = manager.bookRoom(student, room, AIFixture.minutesFromNow(-5), AIFixture.minutesFromNow(55),
                PaymentMethod.CREDIT_CARD, CARD);
        manager.checkIn("alice@yorku.ca", booking.getBookingId());

        assertEquals(0.0, manager.payForBooking(booking.getBookingId(), PaymentMethod.CREDIT_CARD, CARD), 0.01);
    }

    @Test
    public void payForBooking_recordsTheSecondPaymentSeparatelyFromTheDeposit() {
        Booking booking = manager.bookRoom(student, room, AIFixture.startedRecently(), AIFixture.hoursFromNow(2),
                PaymentMethod.CREDIT_CARD, CARD);
        manager.checkIn("alice@yorku.ca", booking.getBookingId());
        int paymentsBefore = manager.getAllPayments().size();

        manager.payForBooking(booking.getBookingId(), PaymentMethod.DEBIT_CARD, "4111222233334444", "1234");

        assertEquals(paymentsBefore + 1, manager.getAllPayments().size());
    }

    // ==================== Req3 + Req9: room search ====================

    @Test
    public void getBookableRooms_listsRoomsThatAreFreeForTheWindow() {
        List<Room> free = manager.getBookableRooms(AIFixture.futureStart(), AIFixture.futureEnd());

        assertEquals(1, free.size());
        assertEquals("R1", free.get(0).getRoomId());
    }

    @Test
    public void getBookableRooms_excludesARoomAlreadyBookedForThatWindow() {
        String start = AIFixture.futureStart();
        String end = AIFixture.futureEnd();
        manager.bookRoom(student, room, start, end, PaymentMethod.CREDIT_CARD, CARD);

        assertTrue(manager.getBookableRooms(start, end).isEmpty());
        assertEquals("A different window must still be free", 1,
                manager.getBookableRooms(AIFixture.hoursFromNow(8), AIFixture.hoursFromNow(9)).size());
    }

    @Test
    public void getBookableRooms_treatsCancelledExpiredAndCompletedSlotsAsFree() {
        String start = AIFixture.futureStart();
        String end = AIFixture.futureEnd();
        Booking booking = manager.bookRoom(student, room, start, end, PaymentMethod.CREDIT_CARD, CARD);
        assertTrue(manager.getBookableRooms(start, end).isEmpty());

        manager.cancelBooking(booking.getBookingId());

        assertEquals("A cancelled booking must release the room", 1, manager.getBookableRooms(start, end).size());
    }

    @Test
    public void getBookableRooms_excludesRoomsThatAreDisabledOrUnderMaintenance() {
        room.disable();
        assertTrue(manager.getBookableRooms(AIFixture.futureStart(), AIFixture.futureEnd()).isEmpty());

        room.closeForMaintenance();
        assertTrue(manager.getBookableRooms(AIFixture.futureStart(), AIFixture.futureEnd()).isEmpty());

        room.enable();
        assertEquals(1, manager.getBookableRooms(AIFixture.futureStart(), AIFixture.futureEnd()).size());
    }

    @Test
    public void getBookableRooms_returnsAnEmptyListForInvalidInputInsteadOfThrowing() {
        // The GUI passes raw text straight from a JTextField, so bad input is
        // routine and must not surface as an exception.
        assertTrue(manager.getBookableRooms("garbage", AIFixture.futureEnd()).isEmpty());
        assertTrue(manager.getBookableRooms(AIFixture.futureStart(), "garbage").isEmpty());
        assertTrue(manager.getBookableRooms(AIFixture.hoursFromNow(4), AIFixture.hoursFromNow(2)).isEmpty());
    }

    // ==================== lookups, persistence, defensive copies ====================

    @Test
    public void findBooking_returnsTheBookingOrNullWithoutThrowing() {
        Booking booking = bookFuture();

        assertSame(booking, manager.findBooking(booking.getBookingId()));
        assertNull(manager.findBooking("no-such-booking"));
    }

    @Test
    public void findPaymentByBookingId_returnsTheDepositOrNull() {
        Booking booking = bookFuture();

        assertNotNull(manager.findPaymentByBookingId(booking.getBookingId()));
        assertNull(manager.findPaymentByBookingId("no-such-booking"));
    }

    @Test
    public void constructor_rehydratesBookingsAndPaymentsFromTheRepositories() {
        Booking existing = new Booking("OLD", "bob@yorku.ca", "R1",
                AIFixture.futureStart(), AIFixture.futureEnd(), 30.0, BookingStatus.CONFIRMED);
        BookingManager reloaded = new BookingManager(
                new AIFakes.FakeBookingRepo(List.of(existing)), new AIFakes.FakePaymentRepo(), roomManager);

        assertEquals(1, reloaded.getAllBookings().size());
        assertNotNull(reloaded.findBooking("OLD"));
    }

    @Test
    public void persistBookings_writesThroughToTheRepository() {
        int before = bookingRepo.saveCount;

        manager.persistBookings();

        assertEquals(before + 1, bookingRepo.saveCount);
    }

    @Test
    public void getAllBookingsAndPayments_handOutCopiesSoCallersCannotMutateInternalState() {
        bookFuture();

        manager.getAllBookings().clear();
        manager.getAllPayments().clear();

        assertEquals("Clearing the returned list must not empty the manager", 1, manager.getAllBookings().size());
        assertEquals(1, manager.getAllPayments().size());
    }
}
