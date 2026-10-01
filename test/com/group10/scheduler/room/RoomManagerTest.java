package com.group10.scheduler.room;

import static org.junit.Assert.*;
import org.junit.*;

public class RoomManagerTest{

    private FakeRoomRepository repo;
    private RoomManager roomManager;

    @Before
    public void setUp (){
        repo= new FakeRoomRepository ();
        roomManager= new RoomManager (repo);
    }

    @Test
    public void addRoom (){
        Room room= new Room ("R100", 10, "Bergeron", "100", RoomStatus.AVAILABLE);
        assertTrue (roomManager.addRoom (room));
    }

    @Test
    public void saveRoom (){
        Room room= new Room ("R100", 10, "Bergeron", "100", RoomStatus.AVAILABLE);
        roomManager.addRoom (room);
        assertEquals (1, repo.saveCallCount);
        assertTrue (repo.savedRooms.contains (room));
    }

    @Test
    public void nullRoom (){
        assertFalse (roomManager.addRoom (null));
    }

    @Test
    public void duplicateRoom (){
        Room room1= new Room ("R100", 10, "Bergeron", "100", RoomStatus.AVAILABLE);
        Room room2= new Room ("R100", 20, "Ross", "200", RoomStatus.AVAILABLE);
        roomManager.addRoom (room1);
        assertFalse (roomManager.addRoom (room2));
    }

    @Test
    public void findRoom (){
        Room room= new Room ("R100", 10, "Bergeron", "100", RoomStatus.AVAILABLE);
        roomManager.addRoom (room);
        assertSame (room, roomManager.findRoomById ("R100"));
    }

    @Test
    public void missingRoom (){
        assertNull (roomManager.findRoomById ("no-such-room"));
    }

    @Test
    public void enableRoom (){
        Room room= new Room ("R100", 10, "Bergeron", "100", RoomStatus.DISABLED);
        roomManager.addRoom (room);
        assertTrue (roomManager.enableRoom ("R100"));
        assertEquals (RoomStatus.AVAILABLE, room.getStatus ());
    }

    @Test
    public void enableMissingRoom (){
        assertFalse (roomManager.enableRoom ("no-such-room"));
    }

    @Test
    public void disableRoom (){
        Room room= new Room ("R100", 10, "Bergeron", "100", RoomStatus.AVAILABLE);
        roomManager.addRoom (room);
        assertTrue (roomManager.disableRoom ("R100"));
        assertEquals (RoomStatus.DISABLED, room.getStatus ());
    }

    @Test
    public void disableMissingRoom (){
        assertFalse (roomManager.disableRoom ("no-such-room"));
    }

    @Test
    public void closeRoom (){
        Room room= new Room ("R100", 10, "Bergeron", "100", RoomStatus.AVAILABLE);
        roomManager.addRoom (room);
        assertTrue (roomManager.closeRoom ("R100"));
        assertEquals (RoomStatus.MAINTENANCE, room.getStatus ());
    }

    @Test
    public void closeMissingRoom (){
        assertFalse (roomManager.closeRoom ("no-such-room"));
    }

    @Test
    public void availableRooms (){
        Room available= new Room ("R100", 10, "Bergeron", "100", RoomStatus.AVAILABLE);
        Room disabled= new Room ("R101", 10, "Bergeron", "101", RoomStatus.DISABLED);
        roomManager.addRoom (available);
        roomManager.addRoom (disabled);

        java.util.List <Room> result= roomManager.getAvailableRooms ();
        assertEquals (1, result.size ());
        assertTrue (result.contains (available));
        assertFalse (result.contains (disabled));
    }

    @Test
    public void noAvailableRooms (){
        Room disabled= new Room ("R101", 10, "Bergeron", "101", RoomStatus.DISABLED);
        roomManager.addRoom (disabled);
        assertTrue (roomManager.getAvailableRooms ().isEmpty ());
    }

    @Test
    public void allRooms (){
        Room available= new Room ("R100", 10, "Bergeron", "100", RoomStatus.AVAILABLE);
        Room disabled= new Room ("R101", 10, "Bergeron", "101", RoomStatus.DISABLED);
        roomManager.addRoom (available);
        roomManager.addRoom (disabled);

        assertEquals (2, roomManager.getAllRooms ().size ());
    }

    @Test
    public void loadRooms (){
        Room preloaded= new Room ("R999", 5, "Ross", "999", RoomStatus.AVAILABLE);
        FakeRoomRepository preloadedRepo= new FakeRoomRepository (java.util.List.of (preloaded));
        RoomManager rm= new RoomManager (preloadedRepo);
        assertNotNull (rm.findRoomById ("R999"));
    }
}
