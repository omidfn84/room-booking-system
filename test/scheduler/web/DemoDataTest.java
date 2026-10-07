package scheduler.web;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import scheduler.accounts.AccountManagement;
import scheduler.accounts.FakeUserRepository;
import scheduler.accounts.TestChief;
import scheduler.booking.BookingManager;
import scheduler.booking.FakeBookingRepository;
import scheduler.booking.FakePaymentRepository;
import scheduler.facade.SchedulerFacade;
import scheduler.room.FakeRoomRepository;
import scheduler.room.Room;
import scheduler.room.RoomManager;
import scheduler.room.RoomStatus;

/**
 * Tests for the demo's self-repair: a sample room an administrator takes out
 * of service comes back after 10 minutes. The clock is a variable, so
 * "10 minutes later" takes no time at all.
 */
public class DemoDataTest {

    private SchedulerFacade facade;
    private DemoData demo;
    private long now = 5_000_000;      // the fake clock, in milliseconds

    @Before
    public void setUp() {
        RoomManager rooms = new RoomManager(new FakeRoomRepository());
        BookingManager bookings = new BookingManager(new FakeBookingRepository(), new FakePaymentRepository(), rooms);
        facade = new SchedulerFacade(rooms, bookings, new AccountManagement(new FakeUserRepository()));
        demo = new DemoData(facade, TestChief.password());
        demo.clock = () -> now;
    }

    private RoomStatus status(String roomId) {
        for (Room room : facade.getAllRooms()) {
            if (room.getRoomId().equals(roomId)) {
                return room.getStatus();
            }
        }
        throw new AssertionError("no room " + roomId);
    }

    // What the API does when the demo administrator presses "Switch off".
    private void switchOff(String roomId) {
        assertTrue(facade.disableRoom(demo.adminId(), roomId));
        demo.noteRoomStatus(roomId, false);
    }

    @Test
    public void theDemoStartsWithSixRoomsInServiceAndThreeAccounts() {
        assertEquals(6, facade.getAllRooms().size());
        for (Room room : facade.getAllRooms()) {
            assertEquals(RoomStatus.AVAILABLE, room.getStatus());
        }
        assertEquals(3, demo.accounts().size());
        assertNotNull(demo.find("student"));
        assertNull(demo.find("admin"));        // the administrator is not a booking account
        assertTrue(facade.isAdministrator(demo.adminId()));
    }

    @Test
    public void aRoomSwitchedOffAMomentAgoStaysOffWhenSomeoneLogsIn() {
        switchOff("BRG-213");
        now += DemoData.ROOM_RESTORE_MILLIS - 1;        // one millisecond short of 10 minutes
        demo.refreshForNewVisitor();
        assertEquals(RoomStatus.DISABLED, status("BRG-213"));
    }

    @Test
    public void aRoomOffForTenMinutesComesBackAtTheNextLogIn() {
        switchOff("BRG-213");
        now += DemoData.ROOM_RESTORE_MILLIS;
        demo.refreshForNewVisitor();
        assertEquals(RoomStatus.AVAILABLE, status("BRG-213"));
    }

    @Test
    public void theTenMinutesCountFromWhenTheRoomFirstWentOutOfService() {
        switchOff("LAS-1006");
        now += 6 * 60 * 1000L;
        assertTrue(facade.closeRoom(demo.adminId(), "LAS-1006"));   // 6 minutes later: off -> maintenance
        demo.noteRoomStatus("LAS-1006", false);
        now += 4 * 60 * 1000L;                                       // 10 minutes after it first went off
        demo.refreshForNewVisitor();
        assertEquals(RoomStatus.AVAILABLE, status("LAS-1006"));
    }

    @Test
    public void aRoomTheAdministratorPutBackIsNotTouchedAgain() {
        switchOff("SCL-204");
        assertTrue(facade.enableRoom(demo.adminId(), "SCL-204"));
        demo.noteRoomStatus("SCL-204", true);
        switchOff("SCL-204");                                        // off again: the 10 minutes start now
        now += DemoData.ROOM_RESTORE_MILLIS - 1;
        demo.refreshForNewVisitor();
        assertEquals(RoomStatus.DISABLED, status("SCL-204"));
    }

    @Test
    public void roomsAVisitorAddedAreNeverRestored() {
        assertTrue(facade.addRoom(demo.adminId(), new Room("EXTRA-1", 4, "Vari Hall", "1", RoomStatus.AVAILABLE)));
        switchOff("EXTRA-1");
        now += 10 * DemoData.ROOM_RESTORE_MILLIS;
        demo.refreshForNewVisitor();
        assertEquals(RoomStatus.DISABLED, status("EXTRA-1"));
    }

    @Test
    public void onlyTheRoomsThatAreDueComeBack() {
        switchOff("ACE-010");
        now += 7 * 60 * 1000L;
        switchOff("DB-1004");                                        // 7 minutes after the first one
        now += 3 * 60 * 1000L;
        demo.refreshForNewVisitor();
        assertEquals("off for 10 minutes", RoomStatus.AVAILABLE, status("ACE-010"));
        assertEquals("off for only 3 minutes", RoomStatus.DISABLED, status("DB-1004"));
    }
}
