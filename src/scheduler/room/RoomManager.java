package scheduler.room;
import java.util.ArrayList;
import java.util.List;

import scheduler.persistence.RoomRepository;

public class RoomManager {

	private final RoomRepository roomRepository;
    private List<Room> rooms = new ArrayList<>();

    
    public RoomManager(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
        this.rooms = new ArrayList<>(roomRepository.loadRooms());
    }
    
    public boolean addRoom(Room room){
    	
        if (room == null || findRoomById(room.getRoomId()) != null) {
        	
            return false; 
        
        }
        rooms.add(room);
        roomRepository.saveRooms(rooms);
        
        return true;
    }


    // Search for a room using its room ID
    public Room findRoomById(String roomId) {

        for (Room room : rooms) {
            if (room.getRoomId().equals(roomId)) {
                return room;
            }
        }
        return null;
    }

    public boolean enableRoom(String roomId) {
        Room room = findRoomById(roomId);
        if (room == null) {
            return false;
        }
        room.enable();
        roomRepository.saveRooms(rooms);
        return true;
    }


    public boolean disableRoom(String roomId) {
        Room room = findRoomById(roomId);
        if (room == null) {
            return false;
        }
        room.disable();
        roomRepository.saveRooms(rooms);
        return true;
    }


    public boolean closeRoom(String roomId) {
        Room room = findRoomById(roomId);
        if (room == null) {
            return false;
        }
        room.closeForMaintenance();
        roomRepository.saveRooms(rooms);
        return true;
    }


    public List<Room> getAvailableRooms() {

        List<Room> availableRooms = new ArrayList<>();

        for (Room room : rooms) {
            if (room.isAvailable()) {
                availableRooms.add(room);
            }
        }

        return availableRooms;
    }
    
    public List<Room> getAllRooms() {
        return rooms;
    }
}