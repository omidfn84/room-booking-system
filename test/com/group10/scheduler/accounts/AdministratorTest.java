package com.group10.scheduler.accounts;

import static org.junit.Assert.*;
import org.junit.*;
import com.group10.scheduler.room.FakeRoomRepository;
import com.group10.scheduler.room.Room;
import com.group10.scheduler.room.RoomManager;
import com.group10.scheduler.room.RoomStatus;
/**
 * Testss the administrator room controls.
 */
public class AdministratorTest{
    private RoomManager roomManager;
    private Administrator admin;
    @Before
    public void setUp (){
        roomManager= new RoomManager (new FakeRoomRepository ());
        admin= new Administrator ("admin1", "Alice", "alice@yorku.ca", roomManager);
    }
    @Test
    public void constructor (){
        assertEquals ("admin1", admin.getAdminId ());
        assertEquals ("Alice", admin.getName ());
        assertEquals ("alice@yorku.ca", admin.getEmail ());
    }
    @Test
    public void changeAdminFields (){
        admin.setAdminId ("admin2");
        admin.setName ("Alicia");
        admin.setEmail ("alicia@yorku.ca");
        assertEquals ("admin2", admin.getAdminId ());
        assertEquals ("Alicia", admin.getName ());
        assertEquals ("alicia@yorku.ca", admin.getEmail ());
    }
    @Test
    public void addRoom (){
        Room room= new Room ("R100", 10, "Bergeron", "100", RoomStatus.AVAILABLE);
        assertTrue (admin.addRoom (room));
        assertNotNull (roomManager.findRoomById ("R100"));
    }
    @Test
    public void duplicateRoom (){
        Room room= new Room ("R100", 10, "Bergeron", "100", RoomStatus.AVAILABLE);
        admin.addRoom (room);
        assertFalse (admin.addRoom (room));
    }
    @Test
    public void enableRoom (){
        Room room= new Room ("R101", 10, "Bergeron", "101", RoomStatus.DISABLED);
        admin.addRoom (room);
        assertTrue (admin.enableRoom ("R101"));
        assertEquals (RoomStatus.AVAILABLE, roomManager.findRoomById ("R101").getStatus ());
    }
    @Test
    public void disableRoom (){
        Room room= new Room ("R102", 10, "Bergeron", "102", RoomStatus.AVAILABLE);
        admin.addRoom (room);
        assertTrue (admin.disableRoom ("R102"));
        assertEquals (RoomStatus.DISABLED, roomManager.findRoomById ("R102").getStatus ());
    }
    @Test
    public void closeRoom (){
        Room room= new Room ("R103", 10, "Bergeron", "103", RoomStatus.AVAILABLE);
        admin.addRoom (room);
        assertTrue (admin.closeRoom ("R103"));
        assertEquals (RoomStatus.MAINTENANCE, roomManager.findRoomById ("R103").getStatus ());
    }
    @Test
    public void enableMissingRoom (){
        assertFalse (admin.enableRoom ("no-such-room"));
    }
    @Test
    public void adminToString (){
        String result= admin.toString ();
        assertTrue (result.contains ("admin1"));
        assertTrue (result.contains ("Alice"));
        assertTrue (result.contains ("alice@yorku.ca"));
    }
    @Test
    public void nonExistentRoom (){
        assertFalse (admin.disableRoom ("no such room"));
    }
}

