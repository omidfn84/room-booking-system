package scheduler.persistence;

import java.util.List;

import scheduler.room.Room;

public interface RoomRepository {
    List<Room> loadRooms();
    void saveRooms(List<Room> rooms);
}
