package scheduler.room;

import java.util.ArrayList;
import java.util.List;

import scheduler.persistence.RoomRepository;

/**
 * Keeps room data in memory for the tests.
 */
public class FakeRoomRepository implements RoomRepository {

    public List <Room> savedRooms= new ArrayList <>();
    public int saveCallCount= 0;
    private final List <Room> initialRooms;

    public FakeRoomRepository (){
        this.initialRooms= new ArrayList <>();
    }

    public FakeRoomRepository (List <Room> initialRooms){
        this.initialRooms= new ArrayList <>(initialRooms);
    }

    @Override
    public List <Room> loadRooms (){
        return new ArrayList <>(initialRooms);
    }

    @Override
    public void saveRooms (List <Room> rooms){
        savedRooms= new ArrayList <>(rooms);
        saveCallCount++;
    }
}
