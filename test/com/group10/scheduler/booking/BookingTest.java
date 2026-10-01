package com.group10.scheduler.booking;

import static org.junit.Assert.*;
import org.junit.Test;

import java.time.LocalDateTime;

public class BookingTest{

    private String iso (LocalDateTime t){
        return t.toString ();
    }

    @Test
    public void confirmedState (){
        Booking b= new Booking ("BK1", "u@yorku.ca", "R1",
                iso (LocalDateTime.now ()), iso (LocalDateTime.now ().plusHours (2)), 20.0, BookingStatus.CONFIRMED);
        assertTrue (b.getState () instanceof ConcreteStates.ConfirmedState);
    }

    @Test
    public void checkedInState (){
        Booking b= new Booking ("BK2", "u@yorku.ca", "R1",
                iso (LocalDateTime.now ()), iso (LocalDateTime.now ().plusHours (2)), 20.0, BookingStatus.CHECKED_IN);
        assertTrue (b.getState () instanceof ConcreteStates.CheckedInState);
    }

    @Test
    public void cancelledState (){
        Booking b= new Booking ("BK3", "u@yorku.ca", "R1",
                iso (LocalDateTime.now ()), iso (LocalDateTime.now ().plusHours (2)), 20.0, BookingStatus.CANCELLED);
        assertTrue (b.getState () instanceof ConcreteStates.CancelledState);
    }

    @Test
    public void completedState (){
        Booking b= new Booking ("BK4", "u@yorku.ca", "R1",
                iso (LocalDateTime.now ()), iso (LocalDateTime.now ().plusHours (2)), 20.0, BookingStatus.COMPLETED);
        assertTrue (b.getState () instanceof ConcreteStates.CompletedState);
    }

    @Test
    public void expiredState (){
        Booking b= new Booking ("BK5", "u@yorku.ca", "R1",
                iso (LocalDateTime.now ()), iso (LocalDateTime.now ().plusHours (2)), 20.0, BookingStatus.EXPIRED);
        assertTrue (b.getState () instanceof ConcreteStates.ExpiredState);
    }

    @Test
    public void depositNotForfeited (){
        Booking b= new Booking ("BK1", "u@yorku.ca", "R1",
                iso (LocalDateTime.now ()), iso (LocalDateTime.now ().plusHours (2)), 20.0, BookingStatus.CONFIRMED);
        assertFalse (b.isDepositForfeited ());
    }

    @Test
    public void finalCost (){
        LocalDateTime start= LocalDateTime.now ();
        LocalDateTime end= start.plusHours (3);
        Booking b= new Booking ("BK1", "u@yorku.ca", "R1", iso (start), iso (end), 20.0, BookingStatus.CONFIRMED);
        assertEquals (60.0, b.calculateFinalCost (), 0.0001);
    }

    @Test
    public void invalidDurationCost (){
        LocalDateTime start= LocalDateTime.now ();
        LocalDateTime end= start.minusHours (1);
        Booking b= new Booking ("BK1", "u@yorku.ca", "R1", iso (start), iso (end), 20.0, BookingStatus.CONFIRMED);
        assertEquals (0.0, b.calculateFinalCost (), 0.0001);
    }

    @Test
    public void checkedInBalance (){
        LocalDateTime start= LocalDateTime.now ().minusHours (1);
        LocalDateTime end= start.plusHours (3);
        Booking b= new Booking ("BK1", "u@yorku.ca", "R1", iso (start), iso (end), 20.0, BookingStatus.CONFIRMED);
        b.checkIn ();
        double finalCost= b.calculateFinalCost ();
        assertEquals (finalCost - 20.0, b.calculateRemainingBalance (), 0.0001);
    }

    @Test
    public void fullBalanceBeforeCheckIn (){
        LocalDateTime start= LocalDateTime.now ();
        LocalDateTime end= start.plusHours (3);
        Booking b= new Booking ("BK1", "u@yorku.ca", "R1", iso (start), iso (end), 20.0, BookingStatus.CONFIRMED);
        assertEquals (b.calculateFinalCost (), b.calculateRemainingBalance (), 0.0001);
    }

    @Test
    public void forfeitedDepositBalance (){
        LocalDateTime start= LocalDateTime.now ().minusHours (1);
        LocalDateTime end= start.plusHours (3);
        Booking b= new Booking ("BK1", "u@yorku.ca", "R1", iso (start), iso (end), 20.0, BookingStatus.CONFIRMED);
        b.checkIn ();
        b.forfeitDeposit ();
        assertEquals (b.calculateFinalCost (), b.calculateRemainingBalance (), 0.0001);
    }

    @Test
    public void forfeitDeposit (){
        Booking b= new Booking ("BK1", "u@yorku.ca", "R1",
                iso (LocalDateTime.now ()), iso (LocalDateTime.now ().plusHours (2)), 20.0, BookingStatus.CONFIRMED);
        b.forfeitDeposit ();
        assertTrue (b.isDepositForfeited ());
    }

    @Test(expected= IllegalArgumentException.class)
    public void nullState (){
        Booking b= new Booking ("BK1", "u@yorku.ca", "R1",
                iso (LocalDateTime.now ()), iso (LocalDateTime.now ().plusHours (2)), 20.0, BookingStatus.CONFIRMED);
        b.setState (null);
    }

    @Test
    public void statusChangesWithState (){
        Booking b= new Booking ("BK1", "u@yorku.ca", "R1",
                iso (LocalDateTime.now ()), iso (LocalDateTime.now ().plusHours (2)), 20.0, BookingStatus.CONFIRMED);
        b.setState (new ConcreteStates.CheckedInState ());
        assertEquals (BookingStatus.CHECKED_IN, b.getStatus ());
    }

    @Test
    public void constructorValues (){
        Booking b= new Booking ("BK1", "user@yorku.ca", "R1", "2026-01-01T10:00:00", "2026-01-01T12:00:00", 20.0, BookingStatus.CONFIRMED);
        assertEquals ("BK1", b.getBookingId ());
        assertEquals ("user@yorku.ca", b.getUserEmail ());
        assertEquals ("R1", b.getRoomId ());
        assertEquals ("2026-01-01T10:00:00", b.getStartTime ());
        assertEquals ("2026-01-01T12:00:00", b.getEndTime ());
        assertEquals (20.0, b.getDepositAmount (), 0.0001);
    }

    @Test
    public void checkInTime (){
        Booking b= new Booking ("BK1", "u@yorku.ca", "R1",
                iso (LocalDateTime.now ()), iso (LocalDateTime.now ().plusHours (2)), 20.0, BookingStatus.CONFIRMED);
        assertNull (b.getCheckInTime ());
        b.setCheckInTime ("2026-01-01T09:00:00");
        assertEquals ("2026-01-01T09:00:00", b.getCheckInTime ());
    }

    @Test
    public void successfulCheckInTime (){
        Booking b= new Booking ("BK1", "u@yorku.ca", "R1",
                iso (LocalDateTime.now ().minusHours (1)), iso (LocalDateTime.now ().plusHours (2)), 20.0, BookingStatus.CONFIRMED);
        b.checkIn ();
        assertNotNull (b.getCheckInTime ());
    }

    @Test
    public void bookingToString (){
        Booking b= new Booking ("BK1", "u@yorku.ca", "R1", "2026-01-01T10:00:00", "2026-01-01T12:00:00", 20.0, BookingStatus.CONFIRMED);
        String result= b.toString ();
        assertTrue (result.contains ("BK1"));
        assertTrue (result.contains ("R1"));
        assertTrue (result.contains ("CONFIRMED"));
        assertTrue (result.contains ("20.0"));
    }
}

