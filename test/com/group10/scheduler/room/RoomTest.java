package com.group10.scheduler.room;

import static org.junit.Assert.*;
import org.junit.*;

public class RoomTest{

    private Room room;

    @Before
    public void setUp (){
        room= new Room ("R100", 25, "Bergeron", "100", RoomStatus.AVAILABLE);
    }

    @Test
    public void constructorValues (){
        assertEquals ("R100", room.getRoomId ());
        assertEquals (25, room.getCapacity ());
        assertEquals ("Bergeron", room.getBuilding ());
        assertEquals ("100", room.getRoomNumber ());
        assertEquals (RoomStatus.AVAILABLE, room.getStatus ());
    }

    @Test
    public void createsSensor (){
        assertNotNull (room.getSensorSystem ());
    }

    @Test
    public void sensorId (){
        assertEquals ("R100-sensor", room.getSensorSystem ().getSensorId ());
    }

    @Test
    public void availableRoom (){
        assertTrue (room.isAvailable ());
    }

    @Test
    public void unavailableRoom (){
        room.disable ();
        assertFalse (room.isAvailable ());
    }

    @Test
    public void enableRoom (){
        room.disable ();
        room.enable ();
        assertEquals (RoomStatus.AVAILABLE, room.getStatus ());
        assertTrue (room.isAvailable ());
    }

    @Test
    public void disableRoom (){
        room.disable ();
        assertEquals (RoomStatus.DISABLED, room.getStatus ());
    }

    @Test
    public void maintenanceRoom (){
        room.closeForMaintenance ();
        assertEquals (RoomStatus.MAINTENANCE, room.getStatus ());
        assertFalse (room.isAvailable ());
    }

    @Test
    public void changeRoomFields (){
        room.setRoomId ("R200");
        room.setCapacity (50);
        room.setBuilding ("Ross");
        room.setRoomNumber ("200");
        room.setStatus (RoomStatus.OCCUPIED);

        assertEquals ("R200", room.getRoomId ());
        assertEquals (50, room.getCapacity ());
        assertEquals ("Ross", room.getBuilding ());
        assertEquals ("200", room.getRoomNumber ());
        assertEquals (RoomStatus.OCCUPIED, room.getStatus ());
    }

    @Test
    public void roomToString (){
        String result= room.toString ();
        assertTrue (result.contains ("R100"));
        assertTrue (result.contains ("Bergeron"));
        assertTrue (result.contains ("100"));
        assertTrue (result.contains ("25"));
        assertTrue (result.contains ("AVAILABLE"));
    }

    @Test
    public void occupiedRoomUnavailable (){
        room.setStatus (RoomStatus.OCCUPIED);
        assertFalse (room.isAvailable ());
    }
}
