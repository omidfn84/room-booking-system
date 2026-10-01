package com.group10.scheduler.room;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.group10.scheduler.aisupport.AIFakes;

/**
 * RoomManager owns the room inventory (Req6, Req7) and is the «Client» side of
 * the Adapter pattern: it talks only to the RoomRepository interface and never
 * to the CSV classes behind it.
 *
 * Two things are asserted repeatedly here:
 *  - unique room ids (Req7), enforced on add
 *  - every successful mutation is written through to the repository, not just
 *    applied to the in-memory list
 */
public class RoomManagerAITest {

    private AIFakes.FakeRoomRepo repo;
    private RoomManager manager;

    @Before
    public void setUp() {
        repo = new AIFakes.FakeRoomRepo();
        manager = new RoomManager(repo);
    }

    private Room room(String id) {
        return new Room(id, 10, "Bergeron", "100", RoomStatus.AVAILABLE);
    }

    @Test
    public void constructor_loadsWhateverTheRepositoryAlreadyHeld() {
        RoomManager reloaded = new RoomManager(new AIFakes.FakeRoomRepo(List.of(room("R1"), room("R2"))));

        assertEquals(2, reloaded.getAllRooms().size());
        assertNotNull(reloaded.findRoomById("R1"));
        assertNotNull(reloaded.findRoomById("R2"));
    }

    @Test
    public void constructor_startsEmptyWhenTheRepositoryIsEmpty() {
        assertTrue(manager.getAllRooms().isEmpty());
    }

    @Test
    public void addRoom_storesTheRoomAndPersistsIt() {
        assertTrue(manager.addRoom(room("R1")));

        assertEquals(1, manager.getAllRooms().size());
        assertEquals("The change must reach the repository, not just the list", 1, repo.saveCount);
        assertEquals(1, repo.saved.size());
    }

    @Test
    public void addRoom_rejectsADuplicateRoomIdWithoutPersisting() {
        // Req7: "Rooms have unique identification numbers."
        manager.addRoom(room("R1"));
        int savesAfterFirst = repo.saveCount;

        assertFalse(manager.addRoom(room("R1")));

        assertEquals(1, manager.getAllRooms().size());
        assertEquals("A rejected add must not touch the repository", savesAfterFirst, repo.saveCount);
    }

    @Test
    public void addRoom_rejectsNullWithoutThrowing() {
        assertFalse(manager.addRoom(null));
        assertTrue(manager.getAllRooms().isEmpty());
        assertEquals(0, repo.saveCount);
    }

    @Test
    public void addRoom_acceptsSeveralDistinctRooms() {
        assertTrue(manager.addRoom(room("R1")));
        assertTrue(manager.addRoom(room("R2")));
        assertTrue(manager.addRoom(room("R3")));

        assertEquals(3, manager.getAllRooms().size());
        assertEquals(3, repo.saveCount);
    }

    @Test
    public void findRoomById_returnsTheMatchingRoomOrNull() {
        Room r1 = room("R1");
        manager.addRoom(r1);

        assertSame(r1, manager.findRoomById("R1"));
        assertNull(manager.findRoomById("R-NOPE"));
    }

    @Test
    public void findRoomById_isCaseSensitive() {
        manager.addRoom(room("R1"));

        assertNull("Room ids are exact strings, not case-insensitive keys", manager.findRoomById("r1"));
    }

    @Test
    public void enableRoom_makesTheRoomAvailableAgainAndPersists() {
        Room r1 = room("R1");
        manager.addRoom(r1);
        manager.disableRoom("R1");
        int savesBefore = repo.saveCount;

        assertTrue(manager.enableRoom("R1"));

        assertEquals(RoomStatus.AVAILABLE, r1.getStatus());
        assertEquals(savesBefore + 1, repo.saveCount);
    }

    @Test
    public void disableRoom_setsTheStatusToDisabledAndPersists() {
        Room r1 = room("R1");
        manager.addRoom(r1);
        int savesBefore = repo.saveCount;

        assertTrue(manager.disableRoom("R1"));

        assertEquals(RoomStatus.DISABLED, r1.getStatus());
        assertEquals(savesBefore + 1, repo.saveCount);
    }

    @Test
    public void closeRoom_setsTheStatusToMaintenanceAndPersists() {
        // Req6: "Rooms can be closed temporarily for repairs or maintenance."
        Room r1 = room("R1");
        manager.addRoom(r1);
        int savesBefore = repo.saveCount;

        assertTrue(manager.closeRoom("R1"));

        assertEquals(RoomStatus.MAINTENANCE, r1.getStatus());
        assertEquals(savesBefore + 1, repo.saveCount);
    }

    @Test
    public void stateChangingOperationsReturnFalseForAnUnknownRoomIdAndDoNotPersist() {
        int savesBefore = repo.saveCount;

        assertFalse(manager.enableRoom("R-NOPE"));
        assertFalse(manager.disableRoom("R-NOPE"));
        assertFalse(manager.closeRoom("R-NOPE"));

        assertEquals("A no-op must not write to the repository", savesBefore, repo.saveCount);
    }

    @Test
    public void theReq6TransitionsCanBeAppliedInAnyOrder() {
        Room r1 = room("R1");
        manager.addRoom(r1);

        manager.disableRoom("R1");
        assertEquals(RoomStatus.DISABLED, r1.getStatus());

        manager.closeRoom("R1");
        assertEquals(RoomStatus.MAINTENANCE, r1.getStatus());

        manager.enableRoom("R1");
        assertEquals(RoomStatus.AVAILABLE, r1.getStatus());

        manager.closeRoom("R1");
        assertEquals(RoomStatus.MAINTENANCE, r1.getStatus());
    }

    @Test
    public void getAvailableRooms_returnsOnlyRoomsWhoseStatusIsAvailable() {
        manager.addRoom(room("R1"));
        manager.addRoom(room("R2"));
        manager.addRoom(room("R3"));
        manager.disableRoom("R2");
        manager.closeRoom("R3");

        List<Room> available = manager.getAvailableRooms();

        assertEquals(1, available.size());
        assertEquals("R1", available.get(0).getRoomId());
    }

    @Test
    public void getAvailableRooms_isEmptyWhenEveryRoomIsOutOfService() {
        manager.addRoom(room("R1"));
        manager.addRoom(room("R2"));
        manager.disableRoom("R1");
        manager.closeRoom("R2");

        assertTrue(manager.getAvailableRooms().isEmpty());
    }

    @Test
    public void getAvailableRooms_reflectsLaterStatusChanges() {
        manager.addRoom(room("R1"));
        assertEquals(1, manager.getAvailableRooms().size());

        manager.disableRoom("R1");
        assertEquals(0, manager.getAvailableRooms().size());

        manager.enableRoom("R1");
        assertEquals(1, manager.getAvailableRooms().size());
    }

    @Test
    public void getAllRooms_includesRoomsRegardlessOfStatus() {
        manager.addRoom(room("R1"));
        manager.addRoom(room("R2"));
        manager.disableRoom("R2");

        assertEquals("The admin table must show disabled rooms too", 2, manager.getAllRooms().size());
    }

    @Test
    public void savedSnapshotContainsEveryRoomAfterEachWrite() {
        manager.addRoom(room("R1"));
        manager.addRoom(room("R2"));

        assertEquals(2, repo.saved.size());
    }
}
