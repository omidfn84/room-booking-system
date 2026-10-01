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

import com.group10.scheduler.booking.Booking;
import com.group10.scheduler.booking.BookingStatus;

public class CsvBookingRepositoryAITest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private File csvFile;
	private CsvBookingRepository repository;

	@Before
	public void setUp() throws Exception {
		csvFile = temporaryFolder.newFile("bookings.csv");
		repository = new CsvBookingRepository(csvFile.getAbsolutePath());
	}

	@Test
	public void loadBookingsFromMissingFileReturnsEmptyList() {
		File missingFile =
				new File(temporaryFolder.getRoot(), "missing-bookings.csv");

		CsvBookingRepository missingRepository =
				new CsvBookingRepository(missingFile.getAbsolutePath());

		List<Booking> bookings = missingRepository.loadBookings();

		assertNotNull(bookings);
		assertTrue(bookings.isEmpty());
	}

	@Test
	public void saveAndLoadBookingPreservesAllInformation() {
		Booking booking = new Booking(
				"B001",
				"student@yorku.ca",
				"R101",
				"2026-08-01T10:00",
				"2026-08-01T12:00",
				20.0,
				BookingStatus.CONFIRMED);

		booking.setCheckInTime("2026-08-01T09:55");

		repository.saveBookings(Arrays.asList(booking));

		List<Booking> loadedBookings = repository.loadBookings();

		assertEquals(1, loadedBookings.size());

		Booking loaded = loadedBookings.get(0);

		assertEquals("B001", loaded.getBookingId());
		assertEquals("student@yorku.ca", loaded.getUserEmail());
		assertEquals("R101", loaded.getRoomId());
		assertEquals("2026-08-01T10:00", loaded.getStartTime());
		assertEquals("2026-08-01T12:00", loaded.getEndTime());
		assertEquals(20.0, loaded.getDepositAmount(), 0.001);
		assertEquals("2026-08-01T09:55", loaded.getCheckInTime());
		assertEquals(BookingStatus.CONFIRMED, loaded.getStatus());
	}

	@Test
	public void nullCheckInTimeRemainsNullAfterLoading() {
		Booking booking = createBooking(
				"B001",
				BookingStatus.CONFIRMED);

		repository.saveBookings(Arrays.asList(booking));

		Booking loaded = repository.loadBookings().get(0);

		assertNull(loaded.getCheckInTime());
	}

	@Test
	public void saveAndLoadMultipleBookingsPreservesOrder() {
		List<Booking> bookings = Arrays.asList(
				createBooking("B001", BookingStatus.CONFIRMED),
				createBooking("B002", BookingStatus.CHECKED_IN),
				createBooking("B003", BookingStatus.CANCELLED));

		repository.saveBookings(bookings);

		List<Booking> loaded = repository.loadBookings();

		assertEquals(3, loaded.size());
		assertEquals("B001", loaded.get(0).getBookingId());
		assertEquals("B002", loaded.get(1).getBookingId());
		assertEquals("B003", loaded.get(2).getBookingId());
	}

	@Test
	public void confirmedStatusIsPreserved() {
		Booking loaded =
				saveAndLoadBookingWithStatus(BookingStatus.CONFIRMED);

		assertEquals(BookingStatus.CONFIRMED, loaded.getStatus());
	}

	@Test
	public void checkedInStatusIsPreserved() {
		Booking loaded =
				saveAndLoadBookingWithStatus(BookingStatus.CHECKED_IN);

		assertEquals(BookingStatus.CHECKED_IN, loaded.getStatus());
	}

	@Test
	public void cancelledStatusIsPreserved() {
		Booking loaded =
				saveAndLoadBookingWithStatus(BookingStatus.CANCELLED);

		assertEquals(BookingStatus.CANCELLED, loaded.getStatus());
	}

	@Test
	public void completedStatusIsPreserved() {
		Booking loaded =
				saveAndLoadBookingWithStatus(BookingStatus.COMPLETED);

		assertEquals(BookingStatus.COMPLETED, loaded.getStatus());
	}

	@Test
	public void expiredStatusIsPreserved() {
		Booking loaded =
				saveAndLoadBookingWithStatus(BookingStatus.EXPIRED);

		assertEquals(BookingStatus.EXPIRED, loaded.getStatus());
	}

	@Test
	public void saveEmptyListCreatesLoadableEmptyFile() {
		repository.saveBookings(new ArrayList<>());

		List<Booking> loaded = repository.loadBookings();

		assertNotNull(loaded);
		assertTrue(loaded.isEmpty());
	}

	@Test
	public void saveBookingsWritesExpectedHeader() throws Exception {
		repository.saveBookings(new ArrayList<>());

		String firstLine =
				Files.readAllLines(csvFile.toPath()).get(0);

		assertEquals(
				"bookingId,userEmail,roomId,startTime,endTime,"
				+ "depositAmount,checkInTime,status",
				firstLine);
	}

	@Test
	public void secondSaveOverwritesPreviouslySavedBookings() {
		Booking firstBooking =
				createBooking("B001", BookingStatus.CONFIRMED);

		Booking secondBooking =
				createBooking("B002", BookingStatus.CANCELLED);

		repository.saveBookings(Arrays.asList(firstBooking));
		repository.saveBookings(Arrays.asList(secondBooking));

		List<Booking> loaded = repository.loadBookings();

		assertEquals(1, loaded.size());
		assertEquals("B002", loaded.get(0).getBookingId());
	}

	@Test
	public void corruptedRowIsSkippedWhileValidRowsAreLoaded()
			throws Exception {

		String csv =
				"bookingId,userEmail,roomId,startTime,endTime,"
				+ "depositAmount,checkInTime,status\n"
				+ "B001,user1@yorku.ca,R101,2026-08-01T10:00,"
				+ "2026-08-01T11:00,20.0,,CONFIRMED\n"
				+ "B002,user2@yorku.ca,R102,2026-08-01T12:00,"
				+ "2026-08-01T13:00,invalid,,CONFIRMED\n"
				+ "B003,user3@yorku.ca,R103,2026-08-01T14:00,"
				+ "2026-08-01T15:00,30.0,,COMPLETED\n";

		Files.writeString(csvFile.toPath(), csv);

		List<Booking> loaded = repository.loadBookings();

		assertEquals(2, loaded.size());
		assertEquals("B001", loaded.get(0).getBookingId());
		assertEquals("B003", loaded.get(1).getBookingId());
	}

	@Test
	public void invalidStatusRowIsSkipped() throws Exception {
		String csv =
				"bookingId,userEmail,roomId,startTime,endTime,"
				+ "depositAmount,checkInTime,status\n"
				+ "B001,user@yorku.ca,R101,2026-08-01T10:00,"
				+ "2026-08-01T11:00,20.0,,UNKNOWN\n";

		Files.writeString(csvFile.toPath(), csv);

		List<Booking> loaded = repository.loadBookings();

		assertTrue(loaded.isEmpty());
	}

	@Test
	public void commasInsideUserEmailFieldArePreservedByCsvLibrary() {
		Booking booking = new Booking(
				"B001",
				"user,name@yorku.ca",
				"R101",
				"2026-08-01T10:00",
				"2026-08-01T11:00",
				20.0,
				BookingStatus.CONFIRMED);

		repository.saveBookings(Arrays.asList(booking));

		Booking loaded = repository.loadBookings().get(0);

		assertEquals("user,name@yorku.ca", loaded.getUserEmail());
	}

	private Booking createBooking(
			String bookingId, BookingStatus status) {

		return new Booking(
				bookingId,
				"user@yorku.ca",
				"R101",
				"2026-08-01T10:00",
				"2026-08-01T12:00",
				20.0,
				status);
	}

	private Booking saveAndLoadBookingWithStatus(
			BookingStatus status) {

		Booking booking = createBooking("B001", status);

		repository.saveBookings(Arrays.asList(booking));

		return repository.loadBookings().get(0);
	}
}
