package com.group10.scheduler.room;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Req5: "Each room has sensors to detect occupancy and scan ID badges for
 * verification. Data is sent to the system."
 *
 * The simulation has one subtle but load-bearing behaviour: scanIDBadge() is
 * what SETS occupancy. BookingManager therefore has to scan before it checks
 * occupancy, and an earlier version of that code had the two calls the other
 * way round and made every check-in fail. These tests pin the ordering
 * dependency down so the bug cannot come back unnoticed.
 */
public class RoomSensorSystemAITest {

    private RoomSensorSystem sensor;

    @Before
    public void setUp() {
        sensor = new RoomSensorSystem("R101-sensor");
    }

    @Test
    public void constructor_storesTheSensorId() {
        assertEquals("R101-sensor", sensor.getSensorId());
    }

    @Test
    public void aNewSensorStartsUnoccupied() {
        assertFalse(sensor.detectOccupancy());
        assertFalse(sensor.getOccupied());
    }

    @Test
    public void scanIDBadge_acceptsARealBadgeId() {
        assertTrue(sensor.scanIDBadge("alice@yorku.ca"));
    }

    @Test
    public void scanIDBadge_isWhatMarksTheRoomOccupied() {
        // This is the ordering dependency: occupancy is a CONSEQUENCE of the
        // scan, so any caller that checks occupancy first will always fail.
        assertFalse(sensor.detectOccupancy());

        sensor.scanIDBadge("alice@yorku.ca");

        assertTrue(sensor.detectOccupancy());
    }

    @Test
    public void scanIDBadge_rejectsANullBadgeId() {
        assertFalse(sensor.scanIDBadge(null));
        assertFalse("A rejected scan must not mark the room occupied", sensor.detectOccupancy());
    }

    @Test
    public void scanIDBadge_rejectsAnEmptyOrWhitespaceBadgeId() {
        assertFalse(sensor.scanIDBadge(""));
        assertFalse(sensor.scanIDBadge("   "));
        assertFalse(sensor.scanIDBadge("\t\n"));
        assertFalse(sensor.detectOccupancy());
    }

    @Test
    public void aFailedScanLeavesAnAlreadyOccupiedRoomOccupied() {
        sensor.scanIDBadge("alice@yorku.ca");
        assertTrue(sensor.detectOccupancy());

        assertFalse(sensor.scanIDBadge(null));

        assertTrue("A rejected scan must not evict the current occupant", sensor.detectOccupancy());
    }

    @Test
    public void scanIDBadge_isIdempotentForRepeatedValidScans() {
        assertTrue(sensor.scanIDBadge("alice@yorku.ca"));
        assertTrue(sensor.scanIDBadge("alice@yorku.ca"));
        assertTrue(sensor.scanIDBadge("bob@yorku.ca"));

        assertTrue(sensor.detectOccupancy());
    }

    @Test
    public void detectOccupancy_reportsWhateverTheOccupiedFlagSays() {
        assertFalse(sensor.detectOccupancy());

        sensor.setOccupied(true);
        assertTrue(sensor.detectOccupancy());

        sensor.setOccupied(false);
        assertFalse(sensor.detectOccupancy());
    }

    @Test
    public void detectOccupancy_hasNoSideEffects() {
        // A pure read: calling it repeatedly must not change the answer.
        sensor.scanIDBadge("alice@yorku.ca");

        assertTrue(sensor.detectOccupancy());
        assertTrue(sensor.detectOccupancy());
        assertTrue(sensor.detectOccupancy());
    }

    @Test
    public void setSensorId_updatesTheId() {
        sensor.setSensorId("R202-sensor");

        assertEquals("R202-sensor", sensor.getSensorId());
    }

    @Test
    public void setOccupied_letsATestSimulateAnEmptyRoomAfterAScan() {
        // Needed to reproduce the "sensor says nobody is there" branch of
        // BookingManager.checkIn() without physical hardware.
        sensor.scanIDBadge("alice@yorku.ca");
        assertTrue(sensor.getOccupied());

        sensor.setOccupied(false);

        assertFalse(sensor.detectOccupancy());
    }

    @Test
    public void theReq5CheckInSequenceSucceedsInTheCorrectOrder() {
        // scan, then confirm - the order BookingManager.checkIn() uses.
        boolean approved = sensor.scanIDBadge("alice@yorku.ca") && sensor.detectOccupancy();

        assertTrue(approved);
    }

    @Test
    public void theReq5CheckInSequenceFailsWhenTheBadgeIsInvalid() {
        boolean approved = sensor.scanIDBadge("") && sensor.detectOccupancy();

        assertFalse(approved);
    }
}
