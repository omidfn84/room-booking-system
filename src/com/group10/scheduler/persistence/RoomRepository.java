package com.group10.scheduler.persistence;

import java.util.List;

import com.group10.scheduler.room.Room;

public interface RoomRepository {
    List<Room> loadRooms();
    void saveRooms(List<Room> rooms);
}
