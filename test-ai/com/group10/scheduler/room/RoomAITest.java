package com.group10.scheduler.room;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Req7: rooms carry a unique id, capacity, and building/room location.
 * Req6: rooms can be enabled, disabled, or closed for maintenance.
 * Req5: every room owns a sensor system.
 *
 * The critical invariant tested throughout is that isAvailable() is true for
 * exactly one status. Booking permission is decided by that single method, so
 * a room that reports itself available while DISABLED would let users book a
 * room that is physically shut.
 */
public class RoomAITest {

    private Room room;

    @Before
    public void setUp() {
        room = new Room("R101", 25, "Lassonde", "101", RoomStatus.AVAILABLE);
    }

    @Test
    public void constructor_storesEveryReq7FieldItWasGiven() {
        assertEquals("R101", room.getRoomId());
        assertEquals(25, room.getCapacity());
        assertEquals("Lassonde", room.getBuilding());
        assertEquals("101", room.getRoomNumber());
        assertEquals(RoomStatus.AVAILABLE, room.getStatus());
    }

    @Test
    public void constructor_givesEveryRoomItsOwnSensorSystem() {
        // Req5: "Each room has sensors" - the 1-to-1 relationship must be
        // established at construction, not wired up later by a caller.
        assertNotNull(room.getSensorSystem());
        assertEquals("R101-sensor", room.getSensorSystem().getSensorId());
    }

    @Test
    public void constructor_givesTwoRoomsIndependentSensors() {
        Room other = new Room("R202", 10, "Bergeron", "202", RoomStatus.AVAILABLE);

        assertNotSame(room.getSensorSystem(), other.getSensorSystem());
        room.getSensorSystem().scanIDBadge("alice@yorku.ca");
        assertFalse("One room's occupancy must not leak into another",
                other.getSensorSystem().detectOccupancy());
    }

    @Test
    public void enable_makesTheRoomAvailableFromAnyOtherStatus() {
        room.disable();
        room.enable();
        assertEquals(RoomStatus.AVAILABLE, room.getStatus());

        room.closeForMaintenance();
        room.enable();
        assertEquals(RoomStatus.AVAILABLE, room.getStatus());

        room.setStatus(RoomStatus.OCCUPIED);
        room.enable();
        assertEquals(RoomStatus.AVAILABLE, room.getStatus());
    }

    @Test
    public void disable_setsTheStatusToDisabled() {
        room.disable();

        assertEquals(RoomStatus.DISABLED, room.getStatus());
    }

    @Test
    public void closeForMaintenance_setsTheStatusToMaintenance() {
        // Req6: "Rooms can be closed temporarily for repairs or maintenance."
        room.closeForMaintenance();

        assertEquals(RoomStatus.MAINTENANCE, room.getStatus());
    }

    @Test
    public void isAvailable_isTrueOnlyForTheAvailableStatus() {
        // The single most important assertion about Room: exactly one of the
        // four statuses may permit booking.
        for (RoomStatus status : RoomStatus.values()) {
            room.setStatus(status);
            if (status == RoomStatus.AVAILABLE) {
                assertTrue("AVAILABLE must be bookable", room.isAvailable());
            } else {
                assertFalse(status + " must not be bookable", room.isAvailable());
            }
        }
    }

    @Test
    public void isAvailable_followsTheReq6TransitionsRatherThanTheInitialStatus() {
        assertTrue(room.isAvailable());

        room.disable();
        assertFalse(room.isAvailable());

        room.enable();
        assertTrue(room.isAvailable());

        room.closeForMaintenance();
        assertFalse(room.isAvailable());
    }

    @Test
    public void aRoomCanBeConstructedInAnyStatus() {
        assertFalse(new Room("R1", 5, "B", "1", RoomStatus.DISABLED).isAvailable());
        assertFalse(new Room("R2", 5, "B", "2", RoomStatus.MAINTENANCE).isAvailable());
        assertFalse(new Room("R3", 5, "B", "3", RoomStatus.OCCUPIED).isAvailable());
        assertTrue(new Room("R4", 5, "B", "4", RoomStatus.AVAILABLE).isAvailable());
    }

    @Test
    public void settersUsedByThePersistenceLayerRoundTripTheirValues() {
        room.setRoomId("R999");
        room.setCapacity(80);
        room.setBuilding("Ross");
        room.setRoomNumber("999");
        room.setStatus(RoomStatus.MAINTENANCE);

        assertEquals("R999", room.getRoomId());
        assertEquals(80, room.getCapacity());
        assertEquals("Ross", room.getBuilding());
        assertEquals("999", room.getRoomNumber());
        assertEquals(RoomStatus.MAINTENANCE, room.getStatus());
    }

    @Test
    public void sensorSystem_isTheSameInstanceForTheLifeOfTheRoom() {
        // Room exposes no setter for the sensor, so the occupancy state
        // recorded by a badge scan has to survive every later call. If
        // getSensorSystem() rebuilt the sensor each time, Req5's check-in
        // sequence (scan, then confirm occupancy) could never succeed.
        RoomSensorSystem first = room.getSensorSystem();

        room.disable();
        room.enable();
        room.setStatus(RoomStatus.OCCUPIED);

        assertSame(first, room.getSensorSystem());

        room.getSensorSystem().scanIDBadge("alice@yorku.ca");
        assertTrue(room.getSensorSystem().detectOccupancy());
    }

    @Test
    public void toString_showsTheFieldsAnAdminNeedsToIdentifyTheRoom() {
        String text = room.toString();

        assertTrue(text.contains("R101"));
        assertTrue(text.contains("Lassonde"));
        assertTrue(text.contains("101"));
        assertTrue(text.contains("25"));
        assertTrue(text.contains("AVAILABLE"));
    }

    @Test
    public void roomStatus_declaresExactlyTheFourStatesTheSystemUses() {
        RoomStatus[] all = RoomStatus.values();

        assertEquals(4, all.length);
        assertEquals(RoomStatus.AVAILABLE, RoomStatus.valueOf("AVAILABLE"));
        assertEquals(RoomStatus.DISABLED, RoomStatus.valueOf("DISABLED"));
        assertEquals(RoomStatus.MAINTENANCE, RoomStatus.valueOf("MAINTENANCE"));
        assertEquals(RoomStatus.OCCUPIED, RoomStatus.valueOf("OCCUPIED"));
    }

    @Test
    public void roomStatus_namesAreStableBecauseTheCsvLayerPersistsThem() {
        // CsvRoomRepository writes status.name() and reads it back with
        // valueOf(), so renaming a constant would break every saved file.
        for (RoomStatus status : RoomStatus.values()) {
            assertEquals(status, RoomStatus.valueOf(status.name()));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void roomStatus_valueOfRejectsAnUnknownName() {
        RoomStatus.valueOf("NOT_A_REAL_STATUS");
    }
}
