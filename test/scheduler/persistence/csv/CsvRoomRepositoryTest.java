package scheduler.persistence.csv;

import static org.junit.Assert.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.List;

import scheduler.room.Room;
import scheduler.room.RoomStatus;

public class CsvRoomRepositoryTest{

    @Rule
    public TemporaryFolder tempFolder= new TemporaryFolder ();

    @Test
    public void missingFileIsEmpty () throws Exception{
        File file= new File (tempFolder.getRoot (), "does-not-exist.csv");
        CsvRoomRepository repo= new CsvRoomRepository (file.getAbsolutePath ());
        assertTrue (repo.loadRooms ().isEmpty ());
    }

    @Test
    public void roomSaveAndLoad () throws Exception{
        File file= tempFolder.newFile ("rooms.csv");
        CsvRoomRepository repo= new CsvRoomRepository (file.getAbsolutePath ());

        Room room= new Room ("R1", 25, "Bergeron", "100", RoomStatus.AVAILABLE);
        repo.saveRooms (List.of (room));

        List <Room> loaded= repo.loadRooms ();
        assertEquals (1, loaded.size ());
        Room reloaded= loaded.get (0);
        assertEquals ("R1", reloaded.getRoomId ());
        assertEquals (25, reloaded.getCapacity ());
        assertEquals ("Bergeron", reloaded.getBuilding ());
        assertEquals ("100", reloaded.getRoomNumber ());
        assertEquals (RoomStatus.AVAILABLE, reloaded.getStatus ());
    }

    @Test
    public void saveMultipleRooms () throws Exception{
        File file= tempFolder.newFile ("rooms.csv");
        CsvRoomRepository repo= new CsvRoomRepository (file.getAbsolutePath ());

        repo.saveRooms (List.of (
                new Room ("R1", 10, "Bergeron", "100", RoomStatus.AVAILABLE),
                new Room ("R2", 20, "Ross", "200", RoomStatus.DISABLED)
        ));

        List <Room> loaded= repo.loadRooms ();
        assertEquals (2, loaded.size ());
    }

    @Test
    public void saveOverwritesRooms () throws Exception{
        File file= tempFolder.newFile ("rooms.csv");
        CsvRoomRepository repo= new CsvRoomRepository (file.getAbsolutePath ());

        repo.saveRooms (List.of (new Room ("R1", 10, "Bergeron", "100", RoomStatus.AVAILABLE)));
        repo.saveRooms (List.of (new Room ("R2", 20, "Ross", "200", RoomStatus.DISABLED)));

        List <Room> loaded= repo.loadRooms ();
        assertEquals (1, loaded.size ());
        assertEquals ("R2", loaded.get (0).getRoomId ());
    }

    @Test
    public void emptyRoomList () throws Exception{
        File file= tempFolder.newFile ("rooms.csv");
        CsvRoomRepository repo= new CsvRoomRepository (file.getAbsolutePath ());
        repo.saveRooms (List.of ());
        assertTrue (repo.loadRooms ().isEmpty ());
    }

    @Test
    public void roomStatusesSaved () throws Exception{
        File file= tempFolder.newFile ("rooms.csv");
        CsvRoomRepository repo= new CsvRoomRepository (file.getAbsolutePath ());
        repo.saveRooms (List.of (
                new Room ("R1", 10, "Bergeron", "100", RoomStatus.MAINTENANCE),
                new Room ("R2", 10, "Bergeron", "101", RoomStatus.OCCUPIED)
        ));
        List <Room> loaded= repo.loadRooms ();
        assertEquals (RoomStatus.MAINTENANCE, loaded.get (0).getStatus ());
        assertEquals (RoomStatus.OCCUPIED, loaded.get (1).getStatus ());
    }
    @Test
    public void zeroCapacity () throws Exception{
        File file= tempFolder.newFile ("rooms-zero.csv");
        CsvRoomRepository repo= new CsvRoomRepository (file.getAbsolutePath ());
        Room room= new Room ("R3", 0, "Ross", "300", RoomStatus.AVAILABLE);
        repo.saveRooms (List.of (room));
        List <Room> loaded= repo.loadRooms ();
        assertEquals (0, loaded.get (0).getCapacity ());
    }
    @Test
    public void disabledStatus () throws Exception{
        File file= tempFolder.newFile ("rooms-disabled.csv");
        CsvRoomRepository repo= new CsvRoomRepository (file.getAbsolutePath ());
        Room room= new Room ("R4", 15, "Vari", "400", RoomStatus.DISABLED);
        repo.saveRooms (List.of (room));
        List <Room> loaded= repo.loadRooms ();
        assertEquals (RoomStatus.DISABLED, loaded.get (0).getStatus ());
    }
    @Test
    public void roomInSameOrder () throws Exception{
        File file= tempFolder.newFile ("rooms-order.csv");
        CsvRoomRepository repo= new CsvRoomRepository (file.getAbsolutePath ());
        repo.saveRooms (List.of (new Room ("R5", 10, "LAS", "500", RoomStatus.AVAILABLE), new Room ("R6", 20, "Ross", "600", RoomStatus.AVAILABLE)));
        List <Room> loaded= repo.loadRooms ();
        assertEquals ("R5", loaded.get (0).getRoomId ());
        assertEquals ("R6", loaded.get (1).getRoomId ());
    }
    @Test
    public void roomDataWithSpace () throws Exception{
        File file= tempFolder.newFile ("rooms-spaces.csv");
        CsvRoomRepository repo= new CsvRoomRepository (file.getAbsolutePath ());
        Room room= new Room ("Room 7", 30, "New Building", "Room 700", RoomStatus.AVAILABLE);
        repo.saveRooms (List.of (room));
        Room loaded= repo.loadRooms ().get (0);
        assertEquals ("Room 7", loaded.getRoomId ());
        assertEquals ("New Building", loaded.getBuilding ());
        assertEquals ("Room 700", loaded.getRoomNumber ());
    }
}

