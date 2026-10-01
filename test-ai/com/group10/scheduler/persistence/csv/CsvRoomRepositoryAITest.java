package com.group10.scheduler.persistence.csv;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.group10.scheduler.room.Room;
import com.group10.scheduler.room.RoomStatus;

public class CsvRoomRepositoryAITest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private File csvFile;
	private CsvRoomRepository repository;

	@Before
	public void setUp() throws Exception {
		csvFile = temporaryFolder.newFile("rooms.csv");
		repository = new CsvRoomRepository(csvFile.getAbsolutePath());
	}

	@Test
	public void loadRoomsFromMissingFileReturnsEmptyList() {
		File missingFile =
				new File(temporaryFolder.getRoot(), "missing-rooms.csv");

		CsvRoomRepository missingRepository =
				new CsvRoomRepository(missingFile.getAbsolutePath());

		List<Room> rooms = missingRepository.loadRooms();

		assertNotNull(rooms);
		assertTrue(rooms.isEmpty());
	}

	@Test
	public void saveAndLoadRoomPreservesAllInformation() {
		Room room = new Room(
				"R101",
				40,
				"LAS",
				"101",
				RoomStatus.AVAILABLE);

		repository.saveRooms(Arrays.asList(room));

		List<Room> loadedRooms = repository.loadRooms();

		assertEquals(1, loadedRooms.size());

		Room loaded = loadedRooms.get(0);

		assertEquals("R101", loaded.getRoomId());
		assertEquals(40, loaded.getCapacity());
		assertEquals("LAS", loaded.getBuilding());
		assertEquals("101", loaded.getRoomNumber());
		assertEquals(RoomStatus.AVAILABLE, loaded.getStatus());
	}

	@Test
	public void saveAndLoadMultipleRoomsPreservesOrder() {
		List<Room> rooms = Arrays.asList(
				new Room(
						"R101",
						40,
						"LAS",
						"101",
						RoomStatus.AVAILABLE),

				new Room(
						"R202",
						25,
						"ACE",
						"202",
						RoomStatus.DISABLED),

				new Room(
						"R303",
						100,
						"DB",
						"303",
						RoomStatus.MAINTENANCE));

		repository.saveRooms(rooms);

		List<Room> loaded = repository.loadRooms();

		assertEquals(3, loaded.size());
		assertEquals("R101", loaded.get(0).getRoomId());
		assertEquals("R202", loaded.get(1).getRoomId());
		assertEquals("R303", loaded.get(2).getRoomId());
	}

	@Test
	public void availableStatusIsPreserved() {
		Room loaded = saveAndLoadRoomWithStatus(RoomStatus.AVAILABLE);

		assertEquals(RoomStatus.AVAILABLE, loaded.getStatus());
	}

	@Test
	public void disabledStatusIsPreserved() {
		Room loaded = saveAndLoadRoomWithStatus(RoomStatus.DISABLED);

		assertEquals(RoomStatus.DISABLED, loaded.getStatus());
	}

	@Test
	public void maintenanceStatusIsPreserved() {
		Room loaded = saveAndLoadRoomWithStatus(RoomStatus.MAINTENANCE);

		assertEquals(RoomStatus.MAINTENANCE, loaded.getStatus());
	}

	@Test
	public void occupiedStatusIsPreserved() {
		Room loaded = saveAndLoadRoomWithStatus(RoomStatus.OCCUPIED);

		assertEquals(RoomStatus.OCCUPIED, loaded.getStatus());
	}

	@Test
	public void saveEmptyListCreatesLoadableEmptyFile() {
		repository.saveRooms(new ArrayList<>());

		List<Room> loaded = repository.loadRooms();

		assertNotNull(loaded);
		assertTrue(loaded.isEmpty());
	}

	@Test
	public void saveRoomsWritesExpectedHeader() throws Exception {
		repository.saveRooms(new ArrayList<>());

		String firstLine =
				Files.readAllLines(csvFile.toPath()).get(0);

		assertEquals(
				"roomId,capacity,building,roomNumber,status",
				firstLine);
	}

	@Test
	public void secondSaveOverwritesPreviouslySavedRooms() {
		Room firstRoom = new Room(
				"R101",
				40,
				"LAS",
				"101",
				RoomStatus.AVAILABLE);

		Room secondRoom = new Room(
				"R202",
				20,
				"ACE",
				"202",
				RoomStatus.DISABLED);

		repository.saveRooms(Arrays.asList(firstRoom));
		repository.saveRooms(Arrays.asList(secondRoom));

		List<Room> loaded = repository.loadRooms();

		assertEquals(1, loaded.size());
		assertEquals("R202", loaded.get(0).getRoomId());
	}

	@Test(expected = NumberFormatException.class)
	public void malformedCapacityCausesException() throws Exception {
		String csv =
				"roomId,capacity,building,roomNumber,status\n"
				+ "R101,invalid,LAS,101,AVAILABLE\n";

		Files.writeString(csvFile.toPath(), csv);

		repository.loadRooms();
	}

	@Test(expected = IllegalArgumentException.class)
	public void malformedStatusCausesException() throws Exception {
		String csv =
				"roomId,capacity,building,roomNumber,status\n"
				+ "R101,40,LAS,101,UNKNOWN\n";

		Files.writeString(csvFile.toPath(), csv);

		repository.loadRooms();
	}

	@Test
	public void commasInsideBuildingNameArePreserved() {
		Room room = new Room(
				"R101",
				40,
				"Life Sciences, Building",
				"101",
				RoomStatus.AVAILABLE);

		repository.saveRooms(Arrays.asList(room));

		Room loaded = repository.loadRooms().get(0);

		assertEquals(
				"Life Sciences, Building",
				loaded.getBuilding());
	}

	private Room saveAndLoadRoomWithStatus(RoomStatus status) {
		Room room = new Room(
				"R101",
				40,
				"LAS",
				"101",
				status);

		repository.saveRooms(Arrays.asList(room));

		return repository.loadRooms().get(0);
	}
}
