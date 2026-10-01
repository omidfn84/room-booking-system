package com.group10.scheduler.persistence.csv;

import static org.junit.Assert.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import com.group10.scheduler.booking.Booking;
import com.group10.scheduler.booking.BookingStatus;

public class CsvBookingRepositoryTest{

    @Rule
    public TemporaryFolder tempFolder= new TemporaryFolder ();

    @Test
    public void missingFileIsEmpty () throws Exception{
        File file= new File (tempFolder.getRoot (), "does-not-exist.csv");
        CsvBookingRepository repo= new CsvBookingRepository (file.getAbsolutePath ());
        assertTrue (repo.loadBookings ().isEmpty ());
    }

    @Test
    public void bookingSaveAndLoad () throws Exception{
        File file= tempFolder.newFile ("bookings.csv");
        CsvBookingRepository repo= new CsvBookingRepository (file.getAbsolutePath ());

        Booking booking= new Booking ("BK1", "alice@yorku.ca", "R1",
                "2026-01-01T10:00:00", "2026-01-01T12:00:00", 20.0, BookingStatus.CONFIRMED);
        repo.saveBookings (List.of (booking));

        List <Booking> loaded= repo.loadBookings ();
        assertEquals (1, loaded.size ());
        Booking reloaded= loaded.get (0);
        assertEquals ("BK1", reloaded.getBookingId ());
        assertEquals ("alice@yorku.ca", reloaded.getUserEmail ());
        assertEquals ("R1", reloaded.getRoomId ());
        assertEquals (20.0, reloaded.getDepositAmount (), 0.0001);
        assertEquals (BookingStatus.CONFIRMED, reloaded.getStatus ());
    }

    // A booking without check-in should still load with a null check-in time.
    @Test
    public void noCheckInStaysNull () throws Exception{
        File file= tempFolder.newFile ("bookings.csv");
        CsvBookingRepository repo= new CsvBookingRepository (file.getAbsolutePath ());

        Booking booking= new Booking ("BK1", "alice@yorku.ca", "R1",
                "2026-01-01T10:00:00", "2026-01-01T12:00:00", 20.0, BookingStatus.CONFIRMED);
        repo.saveBookings (List.of (booking));

        Booking reloaded= repo.loadBookings ().get (0);
        assertNull (reloaded.getCheckInTime ());
    }

    @Test
    public void checkInTimeSaved () throws Exception{
        File file= tempFolder.newFile ("bookings.csv");
        CsvBookingRepository repo= new CsvBookingRepository (file.getAbsolutePath ());

        Booking booking= new Booking ("BK1", "alice@yorku.ca", "R1",
                "2026-01-01T09:00:00", "2026-01-01T12:00:00", 20.0, BookingStatus.CHECKED_IN);
        booking.setCheckInTime ("2026-01-01T09:05:00");
        repo.saveBookings (List.of (booking));

        Booking reloaded= repo.loadBookings ().get (0);
        assertEquals ("2026-01-01T09:05:00", reloaded.getCheckInTime ());
    }

    @Test
    public void badRowIsSkipped () throws Exception{
        File file= tempFolder.newFile ("bookings.csv");
        try (FileWriter fw= new FileWriter (file, false)){
            fw.write ("bookingId,userEmail,roomId,startTime,endTime,depositAmount,checkInTime,status\n");
            fw.write ("BK1,alice@yorku.ca,R1,2026-01-01T10:00:00,2026-01-01T12:00:00,NOT_A_NUMBER,,CONFIRMED\n");
            fw.write ("BK2,bob@yorku.ca,R2,2026-01-01T10:00:00,2026-01-01T12:00:00,20.0,,CONFIRMED\n");
        }

        CsvBookingRepository repo= new CsvBookingRepository (file.getAbsolutePath ());
        List <Booking> loaded= repo.loadBookings ();

        assertEquals (1, loaded.size ());
        assertEquals ("BK2", loaded.get (0).getBookingId ());
    }

    @Test
    public void saveOverwritesBookings () throws Exception{
        File file= tempFolder.newFile ("bookings.csv");
        CsvBookingRepository repo= new CsvBookingRepository (file.getAbsolutePath ());

        repo.saveBookings (List.of (new Booking ("BK1", "a@yorku.ca", "R1",
                "2026-01-01T10:00:00", "2026-01-01T12:00:00", 20.0, BookingStatus.CONFIRMED)));
        repo.saveBookings (List.of (new Booking ("BK2", "b@yorku.ca", "R2",
                "2026-01-01T10:00:00", "2026-01-01T12:00:00", 20.0, BookingStatus.CONFIRMED)));

        List <Booking> loaded= repo.loadBookings ();
        assertEquals (1, loaded.size ());
        assertEquals ("BK2", loaded.get (0).getBookingId ());
    }

    @Test
    public void emptyBookingList () throws Exception{
        File file= tempFolder.newFile ("bookings.csv");
        CsvBookingRepository repo= new CsvBookingRepository (file.getAbsolutePath ());
        repo.saveBookings (List.of ());
        assertTrue (repo.loadBookings ().isEmpty ());
    }
    @Test
    public void sameOrder () throws Exception{
        File file= tempFolder.newFile ("order.csv");
        CsvBookingRepository repo= new CsvBookingRepository (file.getAbsolutePath ());
        Booking first= new Booking ("B1", "a@yorku.ca", "R1", "2026-01-01T10:00:00", "2026-01-01T11:00:00", 20.0, BookingStatus.CONFIRMED);
        Booking second= new Booking ("B2", "b@yorku.ca", "R2", "2026-01-01T12:00:00", "2026-01-01T13:00:00", 25.0, BookingStatus.CANCELLED);
        repo.saveBookings (List.of (first, second));
        List <Booking> loaded= repo.loadBookings ();
        assertEquals ("B1", loaded.get (0).getBookingId ());
        assertEquals ("B2", loaded.get (1).getBookingId ());
    }
    @Test
    public void badStatus () throws Exception{
        File file= tempFolder.newFile ("bad-status.csv");
        try (FileWriter writer= new FileWriter (file)){
            writer.write ("bookingId,userEmail,roomId,startTime,endTime,depositAmount,checkInTime,status\n");
            writer.write ("B3,a@yorku.ca,R3,2026-01-01T10:00:00,2026-01-01T11:00:00,20.0,,BAD\n");
            writer.write ("B4,b@yorku.ca,R4,2026-01-01T12:00:00,2026-01-01T13:00:00,25.0,,COMPLETED\n");
        }
        CsvBookingRepository repo= new CsvBookingRepository (file.getAbsolutePath ());
        List <Booking> loaded= repo.loadBookings ();
        assertEquals (1, loaded.size ());
        assertEquals ("B4", loaded.get (0).getBookingId ());
    }
    @Test
    public void blankCheckIn () throws Exception{
        File file= tempFolder.newFile ("blank.csv");
        try (FileWriter writer= new FileWriter (file)){
            writer.write ("bookingId,userEmail,roomId,startTime,endTime,depositAmount,checkInTime,status\n");
            writer.write ("B5,a@yorku.ca,R5,2026-01-01T10:00:00,2026-01-01T11:00:00,20.0,   ,CONFIRMED\n");
        }
        CsvBookingRepository repo= new CsvBookingRepository (file.getAbsolutePath ());
        Booking loaded= repo.loadBookings ().get (0);
        assertNull (loaded.getCheckInTime ());
    }
}

