package scheduler.persistence.csv;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import scheduler.persistence.RoomRepository;
import scheduler.room.Room;
import scheduler.room.RoomStatus;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter: implements the domain-facing RoomRepository (Target) by wrapping
 * javacsv's CsvReader/CsvWriter (Adaptee). RoomManager only ever talks to
 * RoomRepository — it never imports com.csvreader.
 */
public class CsvRoomRepository implements RoomRepository {

    private final String filePath;
    private static final String[] HEADERS = {"roomId", "capacity", "building", "roomNumber", "status"};

    public CsvRoomRepository(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public List<Room> loadRooms() {
        List<Room> rooms = new ArrayList<>();
        CsvReader reader = null;
        try {
            reader = new CsvReader(filePath);
            reader.readHeaders();
            while (reader.readRecord()) {
                String roomId = reader.get("roomId");
                int capacity = Integer.parseInt(reader.get("capacity"));
                String building = reader.get("building");
                String roomNumber = reader.get("roomNumber");
                RoomStatus status = RoomStatus.valueOf(reader.get("status"));
                rooms.add(new Room(roomId, capacity, building, roomNumber, status));
            }
        } catch (IOException e) {
            // First run / file doesn't exist yet — start with an empty list.
            System.out.println("No existing rooms.csv found, starting empty: " + e.getMessage());
        } finally {
            if (reader != null) reader.close();
        }
        return rooms;
    }

    @Override
    public void saveRooms(List<Room> rooms) {
        CsvWriter writer = null;
        try {
            writer = new CsvWriter(new FileWriter(filePath, false), ',');
            for (String h : HEADERS) writer.write(h);
            writer.endRecord();

            for (Room r : rooms) {
                writer.write(r.getRoomId());
                writer.write(String.valueOf(r.getCapacity()));
                writer.write(r.getBuilding());
                writer.write(r.getRoomNumber());
                writer.write(r.getStatus().name());
                writer.endRecord();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save rooms.csv", e);
        } finally {
            if (writer != null) writer.close();
        }
    }
}
