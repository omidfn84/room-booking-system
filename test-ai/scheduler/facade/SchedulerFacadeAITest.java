package scheduler.facade;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import scheduler.accounts.Administrator;
import scheduler.accounts.RegisteredUser;
import scheduler.aisupport.AIFixture;
import scheduler.booking.Booking;
import scheduler.booking.BookingStatus;
import scheduler.booking.PaymentMethod;
import scheduler.room.Room;
import scheduler.room.RoomStatus;

/**
 * SchedulerFacade is the single entry point the GUI is allowed to touch. Its
 * job is routing, not rule-making, so these tests check two things:
 *
 *  1. Each method reaches the right subsystem and returns the right answer.
 *  2. The facade does NOT bypass the rules underneath it. In particular room
 *     operations must go through an Administrator obtained from the
 *     ChiefEventCoordinator Singleton (Req2/Req6) - passing an unknown admin
 *     id has to fail rather than quietly mutate a room.
 *
 * The chief is a JVM-wide Singleton, so every test that creates an admin uses
 * AIFixture.uniqueAdminId() to stay isolated from its neighbours.
 */
public class SchedulerFacadeAITest {

    private AIFixture fx;
    private SchedulerFacade facade;

    private static final String[] CARD = {"4111111111111111", "12/27", "123"};

    @Before
    public void setUp() {
        fx = new AIFixture();
        facade = fx.facade;
    }

    /** Registers an admin with the chief and returns its unique id. */
    private String newAdmin() {
        String id = AIFixture.uniqueAdminId();
        assertNotNull(facade.generateAdministratorAccount(AIFixture.chiefPassword(), id, "Admin", "admin@yorku.ca"));
        return id;
    }

    // ==================== accounts (Req1) ====================

    @Test
    public void createAccount_registersAUserThroughTheAccountSubsystem() {
        RegisteredUser user = facade.createAccount("alice@yorku.ca", "Passw0rd!", "STUDENT", "Alice", "123456789");

        assertNotNull(user);
        assertEquals("alice@yorku.ca", user.getEmail());
        assertEquals(20.0, user.getHourlyRate(), 0.001);
    }

    @Test
    public void login_returnsTheUserOnlyWhenThePasswordMatches() {
        facade.createAccount("alice@yorku.ca", "Passw0rd!", "STUDENT", "Alice", "123456789");

        assertNotNull(facade.login("alice@yorku.ca", "Passw0rd!"));
        assertNull("A wrong password must not authenticate", facade.login("alice@yorku.ca", "WrongPass1!"));
    }

    @Test
    public void login_returnsNullForAnUnknownEmail() {
        assertNull(facade.login("nobody@yorku.ca", "Passw0rd!"));
    }

    // ==================== Req2: the chief Singleton guards admin creation ====================

    @Test
    public void generateAdministratorAccount_createsAnAdministrator() {
        String id = AIFixture.uniqueAdminId();

        Administrator admin = facade.generateAdministratorAccount(AIFixture.chiefPassword(), id, "Ada", "ada@yorku.ca");

        assertNotNull(admin);
        assertEquals(id, admin.getAdminId());
    }

    @Test
    public void generateAdministratorAccount_refusesADuplicateAdminId() {
        String id = newAdmin();

        assertNull("The chief must not issue the same admin id twice",
                facade.generateAdministratorAccount(AIFixture.chiefPassword(), id, "Someone Else", "else@yorku.ca"));
    }

    @Test
    public void isAdministrator_recognisesOnlyIdsTheChiefIssued() {
        String id = newAdmin();

        assertTrue(facade.isAdministrator(id));
        assertFalse(facade.isAdministrator("NEVER-ISSUED"));
        assertFalse(facade.isAdministrator(null));
    }

    @Test
    public void theSameChiefInstanceBacksEveryFacade() {
        // Req2: "one person". A second facade must see the first facade's
        // admins, because they share the Singleton.
        String id = newAdmin();
        SchedulerFacade another = new AIFixture().facade;

        assertTrue("The chief registry is JVM-wide by design", another.isAdministrator(id));
    }

    // ==================== Req6/Req7: room operations require a real admin ====================

    @Test
    public void addRoom_succeedsForAValidAdmin() {
        String id = newAdmin();

        assertTrue(facade.addRoom(id, new Room("R1", 10, "Bergeron", "100", RoomStatus.AVAILABLE)));
        assertEquals(1, facade.getAllRooms().size());
    }

    @Test
    public void addRoom_isRefusedForAnUnknownAdminId() {
        assertFalse(facade.addRoom("NEVER-ISSUED", new Room("R1", 10, "Bergeron", "100", RoomStatus.AVAILABLE)));
        assertTrue("An unauthorised add must not create a room", facade.getAllRooms().isEmpty());
    }

    @Test
    public void enableDisableAndCloseRoom_allRequireAValidAdminId() {
        String id = newAdmin();
        facade.addRoom(id, new Room("R1", 10, "Bergeron", "100", RoomStatus.AVAILABLE));

        assertFalse(facade.disableRoom("NEVER-ISSUED", "R1"));
        assertFalse(facade.enableRoom("NEVER-ISSUED", "R1"));
        assertFalse(facade.closeRoom("NEVER-ISSUED", "R1"));
        assertEquals("Rejected calls must leave the status untouched",
                RoomStatus.AVAILABLE, fx.roomManager.findRoomById("R1").getStatus());

        assertTrue(facade.disableRoom(id, "R1"));
        assertEquals(RoomStatus.DISABLED, fx.roomManager.findRoomById("R1").getStatus());

        assertTrue(facade.enableRoom(id, "R1"));
        assertEquals(RoomStatus.AVAILABLE, fx.roomManager.findRoomById("R1").getStatus());

        assertTrue(facade.closeRoom(id, "R1"));
        assertEquals(RoomStatus.MAINTENANCE, fx.roomManager.findRoomById("R1").getStatus());
    }

    @Test
    public void roomOperationsReturnFalseForAnUnknownRoomEvenWithAValidAdmin() {
        String id = newAdmin();

        assertFalse(facade.enableRoom(id, "R-NOPE"));
        assertFalse(facade.disableRoom(id, "R-NOPE"));
        assertFalse(facade.closeRoom(id, "R-NOPE"));
    }

    // ==================== Req3/Req4/Req10: booking flow ====================

    @Test
    public void bookRoom_resolvesTheEmailAndRoomIdThenDelegates() {
        fx.addRoom("R1");
        fx.addStudent("alice@yorku.ca");

        Booking booking = facade.bookRoom("alice@yorku.ca", "R1",
                AIFixture.futureStart(), AIFixture.futureEnd(), PaymentMethod.CREDIT_CARD, CARD);

        assertNotNull(booking);
        assertEquals("alice@yorku.ca", booking.getUserEmail());
        assertEquals("R1", booking.getRoomId());
        assertEquals(20.0, booking.getDepositAmount(), 0.001);
    }

    @Test
    public void bookRoom_failsWhenTheEmailOrRoomCannotBeResolved() {
        fx.addRoom("R1");
        fx.addStudent("alice@yorku.ca");

        try {
            facade.bookRoom("ghost@yorku.ca", "R1", AIFixture.futureStart(), AIFixture.futureEnd(),
                    PaymentMethod.CREDIT_CARD, CARD);
            fail("An unknown email resolves to a null user and must be rejected");
        } catch (IllegalArgumentException expected) {
        }

        try {
            facade.bookRoom("alice@yorku.ca", "R-NOPE", AIFixture.futureStart(), AIFixture.futureEnd(),
                    PaymentMethod.CREDIT_CARD, CARD);
            fail("An unknown room id resolves to a null room and must be rejected");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void getBookableRooms_hidesRoomsAlreadyBookedForThatWindow() {
        fx.addRoom("R1");
        fx.addStudent("alice@yorku.ca");
        String start = AIFixture.futureStart();
        String end = AIFixture.futureEnd();

        assertEquals(1, facade.getBookableRooms(start, end).size());

        facade.bookRoom("alice@yorku.ca", "R1", start, end, PaymentMethod.CREDIT_CARD, CARD);

        assertTrue(facade.getBookableRooms(start, end).isEmpty());
    }

    @Test
    public void checkIn_routesThroughToTheBookingSubsystem() {
        fx.addRoom("R1");
        fx.addStudent("alice@yorku.ca");
        Booking booking = facade.bookRoom("alice@yorku.ca", "R1",
                AIFixture.startedRecently(), AIFixture.hoursFromNow(1), PaymentMethod.CREDIT_CARD, CARD);

        assertTrue(facade.checkIn("alice@yorku.ca", booking.getBookingId()));
        assertEquals(BookingStatus.CHECKED_IN, booking.getStatus());
    }

    @Test
    public void editCancelAndExtend_delegateToBookingManagerRatherThanTheBookingObject() {
        // Going straight to Booking would skip the room-conflict check and the
        // deposit refund, so these must return the manager's answer.
        fx.addRoom("R1");
        fx.addStudent("alice@yorku.ca");
        Booking booking = facade.bookRoom("alice@yorku.ca", "R1",
                AIFixture.futureStart(), AIFixture.futureEnd(), PaymentMethod.CREDIT_CARD, CARD);
        String id = booking.getBookingId();

        assertTrue(facade.editBooking(id, AIFixture.hoursFromNow(6), AIFixture.hoursFromNow(7)));
        assertTrue(facade.cancelBooking(id));
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());

        assertFalse("A cancelled booking cannot be extended", facade.extendBooking(id, AIFixture.hoursFromNow(9)));
    }

    @Test
    public void editCancelExtendAndPay_returnFalsyValuesForAnUnknownBookingId() {
        assertFalse(facade.editBooking("no-such-booking", AIFixture.futureStart(), AIFixture.futureEnd()));
        assertFalse(facade.cancelBooking("no-such-booking"));
        assertFalse(facade.extendBooking("no-such-booking", AIFixture.hoursFromNow(9)));
        assertEquals(-1.0, facade.payForBooking("no-such-booking", PaymentMethod.CREDIT_CARD, CARD), 0.001);
    }

    @Test
    public void payForBooking_settlesTheBalanceAndCompletesTheBooking() {
        fx.addRoom("R1");
        fx.addStudent("alice@yorku.ca");
        Booking booking = facade.bookRoom("alice@yorku.ca", "R1",
                AIFixture.startedRecently(), AIFixture.hoursFromNow(2), PaymentMethod.CREDIT_CARD, CARD);
        facade.checkIn("alice@yorku.ca", booking.getBookingId());

        double paid = facade.payForBooking(booking.getBookingId(), PaymentMethod.CREDIT_CARD, CARD);

        assertTrue(paid > 0);
        assertEquals(BookingStatus.COMPLETED, booking.getStatus());
    }

    @Test
    public void findPaymentByBookingId_returnsTheDepositReceipt() {
        fx.addRoom("R1");
        fx.addStudent("alice@yorku.ca");
        Booking booking = facade.bookRoom("alice@yorku.ca", "R1",
                AIFixture.futureStart(), AIFixture.futureEnd(), PaymentMethod.CREDIT_CARD, CARD);

        assertNotNull(facade.findPaymentByBookingId(booking.getBookingId()));
        assertNull(facade.findPaymentByBookingId("no-such-booking"));
    }

    @Test
    public void readOnlyViewsExposeTheCurrentRoomsAndBookings() {
        String id = newAdmin();
        facade.addRoom(id, new Room("R1", 10, "Bergeron", "100", RoomStatus.AVAILABLE));
        fx.addStudent("alice@yorku.ca");
        facade.bookRoom("alice@yorku.ca", "R1", AIFixture.futureStart(), AIFixture.futureEnd(),
                PaymentMethod.CREDIT_CARD, CARD);

        List<Room> rooms = facade.getAllRooms();
        List<Booking> bookings = facade.getAllBookings();

        assertEquals(1, rooms.size());
        assertEquals(1, bookings.size());
    }
}
