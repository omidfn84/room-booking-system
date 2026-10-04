package scheduler.room;

public class Room {
    private String roomId;
    private int capacity;
    private String building;
    private String roomNumber;
    private RoomStatus status;
    private final RoomSensorSystem sensorSystem;


    public Room(String roomId, int capacity, String building, String roomNumber, RoomStatus status) {
        this.roomId = roomId;
        this.capacity = capacity;
        this.building = building;
        this.roomNumber = roomNumber;
        this.status = status;
        this.sensorSystem = new RoomSensorSystem(roomId + "-sensor");
    }



	public void enable() {
		status = RoomStatus.AVAILABLE;
	}

	public void disable() {
		status = RoomStatus.DISABLED;
	}

	public void closeForMaintenance() {
		status = RoomStatus.MAINTENANCE;
	}

	public boolean isAvailable() {
		return (status == RoomStatus.AVAILABLE);
	}

	// Getter and setter methods
	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public String getBuilding() {
		return building;
	}

	public void setBuilding(String building) {
		this.building = building;
	}

	public String getRoomNumber() {
		return roomNumber;
	}

	public void setRoomNumber(String roomNumber) {
		this.roomNumber = roomNumber;
	}
	public RoomStatus getStatus() {
	    return status;
	}
	public void setStatus(RoomStatus status) {
		this.status = status;
	}
	
    public RoomSensorSystem getSensorSystem() {
        return sensorSystem;
    }
    @Override
    public String toString() {
        return "Room[" + roomId + "] " + building + "-" + roomNumber 
            + " | capacity=" + capacity 
            + " | status=" + status;
    }

	
}