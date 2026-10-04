package scheduler.booking;

import static org.junit.Assert.*;
import org.junit.Test;

import java.time.LocalDateTime;

public class ConcreteStatesTest{

    private String future (int hours){
        return LocalDateTime.now ().plusHours (hours).toString ();
    }

    private String past (int hours){
        return LocalDateTime.now ().minusHours (hours).toString ();
    }

    private Booking confirmedBooking (String start, String end){
        return new Booking ("BK1", "user@yorku.ca", "R1", start, end, 20.0, BookingStatus.CONFIRMED);
    }

    @Test
    public void confirmedCheckIn (){
        Booking b= confirmedBooking (past (1), future (2));
        assertTrue (b.checkIn ());
        assertEquals (BookingStatus.CHECKED_IN, b.getStatus ());
    }

    @Test
    public void cancelBeforeStart (){
        Booking b= confirmedBooking (future (1), future (2));
        assertTrue (b.cancelBooking ());
        assertEquals (BookingStatus.CANCELLED, b.getStatus ());
    }

    @Test
    public void cancelAfterStart (){
        Booking b= confirmedBooking (past (1), future (2));
        assertFalse (b.cancelBooking ());
        assertEquals (BookingStatus.CONFIRMED, b.getStatus ());
    }

    @Test
    public void editBeforeStart (){
        Booking b= confirmedBooking (future (2), future (4));
        String newStart= future (3);
        String newEnd= future (5);
        assertTrue (b.editBooking (newStart, newEnd));
        assertEquals (newStart, b.getStartTime ());
        assertEquals (newEnd, b.getEndTime ());
    }

    @Test
    public void editAfterStart (){
        Booking b= confirmedBooking (past (1), future (2));
        assertFalse (b.editBooking (future (1), future (3)));
    }

    @Test
    public void invalidEditRange (){
        Booking b= confirmedBooking (future (2), future (4));
        assertFalse (b.editBooking (future (5), future (3)));
    }

    @Test
    public void badEditDates (){
        Booking b= confirmedBooking (future (2), future (4));
        assertFalse (b.editBooking ("not-a-date", "also-not-a-date"));
    }

    @Test
    public void earlierExtension (){
        Booking b= confirmedBooking (past (1), future (2));
        assertFalse (b.extendBooking (future (1)));
    }

    @Test
    public void extensionAfterEnd (){
        Booking b= confirmedBooking (past (3), past (1));
        assertFalse (b.extendBooking (future (1)));
    }

    @Test
    public void expireConfirmed (){
        Booking b= confirmedBooking (past (1), future (1));
        b.getState ().expire (b);
        assertEquals (BookingStatus.EXPIRED, b.getStatus ());
        assertTrue (b.isDepositForfeited ());
    }

    @Test
    public void completeConfirmed (){
        Booking b= confirmedBooking (past (2), past (1));
        b.getState ().complete (b);
        assertEquals (BookingStatus.COMPLETED, b.getStatus ());
    }

    @Test
    public void checkInAgain (){
        Booking b= new Booking ("BK2", "user@yorku.ca", "R1", past (1), future (2), 20.0, BookingStatus.CHECKED_IN);
        assertFalse (b.checkIn ());
    }

    @Test
    public void cancelCheckedIn (){
        Booking b= new Booking ("BK2", "user@yorku.ca", "R1", past (1), future (2), 20.0, BookingStatus.CHECKED_IN);
        assertFalse (b.cancelBooking ());
    }

    @Test
    public void editCheckedIn (){
        Booking b= new Booking ("BK2", "user@yorku.ca", "R1", past (1), future (2), 20.0, BookingStatus.CHECKED_IN);
        assertFalse (b.editBooking (future (1), future (3)));
    }

    @Test
    public void extendCheckedIn (){
        Booking b= new Booking ("BK2", "user@yorku.ca", "R1", past (1), future (2), 20.0, BookingStatus.CHECKED_IN);
        assertTrue (b.extendBooking (future (4)));
    }

    @Test
    public void checkedInExpireNoChange (){
        Booking b= new Booking ("BK2", "user@yorku.ca", "R1", past (1), future (2), 20.0, BookingStatus.CHECKED_IN);
        b.getState ().expire (b);
        assertEquals (BookingStatus.CHECKED_IN, b.getStatus ());
    }

    @Test
    public void completeCheckedIn (){
        Booking b= new Booking ("BK2", "user@yorku.ca", "R1", past (2), past (1), 20.0, BookingStatus.CHECKED_IN);
        b.getState ().complete (b);
        assertEquals (BookingStatus.COMPLETED, b.getStatus ());
    }

    @Test
    public void cancelledRejectsActions (){
        Booking b= new Booking ("BK3", "user@yorku.ca", "R1", past (2), future (1), 20.0, BookingStatus.CANCELLED);
        assertFalse (b.checkIn ());
        assertFalse (b.cancelBooking ());
        assertFalse (b.editBooking (future (1), future (2)));
        assertFalse (b.extendBooking (future (3)));
    }

    @Test
    public void completedRejectsActions (){
        Booking b= new Booking ("BK4", "user@yorku.ca", "R1", past (2), past (1), 20.0, BookingStatus.COMPLETED);
        assertFalse (b.checkIn ());
        assertFalse (b.cancelBooking ());
        assertFalse (b.editBooking (future (1), future (2)));
        assertFalse (b.extendBooking (future (3)));
    }

    @Test
    public void expiredRejectsActions (){
        Booking b= new Booking ("BK5", "user@yorku.ca", "R1", past (3), past (2), 20.0, BookingStatus.EXPIRED);
        assertFalse (b.checkIn ());
        assertFalse (b.cancelBooking ());
        assertFalse (b.editBooking (future (1), future (2)));
        assertFalse (b.extendBooking (future (3)));
    }
        @Test
    public void cancelledStateNoChanges (){
        Booking b= new Booking ("BK3", "user@yorku.ca", "R1", past (2), future (1), 20.0, BookingStatus.CANCELLED);
        b.getState ().expire (b);
        b.getState ().complete (b);
        assertEquals (BookingStatus.CANCELLED, b.getStatus ());
    }
}
