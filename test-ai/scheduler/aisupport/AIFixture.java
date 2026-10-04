package scheduler.aisupport;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;

import scheduler.accounts.AccountManagement;
import scheduler.accounts.ChiefEventCoordinator;
import scheduler.booking.BookingManager;
import scheduler.facade.SchedulerFacade;
import scheduler.room.Room;
import scheduler.room.RoomManager;
import scheduler.room.RoomStatus;

/**
 * Builds a fully wired, CSV-free system for a single test.
 *
 * Every collaborator is real except the four repositories, which are the
 * in-memory fakes from {@link AIFakes}. Tests therefore exercise the genuine
 * interaction between RoomManager, BookingManager, AccountManagement and the
 * Facade rather than a mocked approximation of it.
 *
 * Two details this class exists to centralise:
 *
 *  1. TIME. Almost every rule in the booking subsystem is relative to
 *     LocalDateTime.now() (Req4's 30-minute check-in window, Req8's
 *     "before the start time" rule, Req9's "before expiry" rule). Tests must
 *     therefore build times relative to now, never hard-coded literals, or
 *     they silently start failing on a future date. The helpers below make
 *     that the path of least resistance.
 *
 *  2. THE SINGLETON. ChiefEventCoordinator keeps its admin registry for the
 *     whole JVM lifetime, so ids leak between test methods. uniqueAdminId()
 *     hands out a fresh id per call so no two tests can collide.
 */
public final class AIFixture {

    private static final AtomicInteger ADMIN_SEQ = new AtomicInteger();

    public final AIFakes.FakeRoomRepo roomRepo;
    public final AIFakes.FakeBookingRepo bookingRepo;
    public final AIFakes.FakePaymentRepo paymentRepo;
    public final AIFakes.FakeUserRepo userRepo;

    public final RoomManager roomManager;
    public final BookingManager bookingManager;
    public final AccountManagement accountManagement;
    public final SchedulerFacade facade;

    public AIFixture() {
        roomRepo = new AIFakes.FakeRoomRepo();
        bookingRepo = new AIFakes.FakeBookingRepo();
        paymentRepo = new AIFakes.FakePaymentRepo();
        userRepo = new AIFakes.FakeUserRepo();

        roomManager = new RoomManager(roomRepo);
        bookingManager = new BookingManager(bookingRepo, paymentRepo, roomManager);
        accountManagement = new AccountManagement(userRepo);
        facade = new SchedulerFacade(roomManager, bookingManager, accountManagement);
    }

    /** Adds an AVAILABLE room and returns it. */
    public Room addRoom(String roomId) {
        Room room = new Room(roomId, 10, "Bergeron", "100", RoomStatus.AVAILABLE);
        roomManager.addRoom(room);
        return room;
    }

    /** Registers a student (hourly rate $20) through the real account subsystem. */
    public void addStudent(String email) {
        accountManagement.createAccount(email, "Passw0rd!", "STUDENT", "Test User", "123456789");
    }

    // ---------- time helpers (all relative to now, never hard-coded) ----------

    /** ISO-8601 string the domain can parse, offset from now by the given minutes. */
    public static String minutesFromNow(long minutes) {
        return LocalDateTime.now().plusMinutes(minutes).truncatedTo(ChronoUnit.SECONDS).toString();
    }

    public static String hoursFromNow(long hours) {
        return LocalDateTime.now().plusHours(hours).truncatedTo(ChronoUnit.SECONDS).toString();
    }

    /** A window that has not started yet: edits and cancellations are allowed (Req8). */
    public static String futureStart() {
        return hoursFromNow(2);
    }

    public static String futureEnd() {
        return hoursFromNow(3);
    }

    /** A window that started 5 minutes ago: inside Req4's 30-minute check-in grace period. */
    public static String startedRecently() {
        return minutesFromNow(-5);
    }

    /** A window that started 2 hours ago: past Req4's 30-minute deadline. */
    public static String startedLongAgo() {
        return hoursFromNow(-2);
    }

    /** An id no other test has used, so the Singleton's registry cannot collide. */
    /** Shared test chief password (same value as test/'s TestChief, since the chief is a JVM-wide Singleton). */
    public static synchronized String chiefPassword() {
        ChiefEventCoordinator chief = ChiefEventCoordinator.getInstance();
        if (!chief.isCredentialConfigured()) {
            chief.configureCredential("Chief-Test-Pass1!");
        }
        return "Chief-Test-Pass1!";
    }

    public static String uniqueAdminId() {
        return "AI-ADMIN-" + ADMIN_SEQ.incrementAndGet() + "-" + System.nanoTime();
    }
}
