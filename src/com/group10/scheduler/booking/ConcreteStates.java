package com.group10.scheduler.booking;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class ConcreteStates {
    private ConcreteStates (){

    }
    public static class ConfirmedState implements BookingState{
        @Override
        public boolean checkIn (Booking booking){
            booking.setState (new CheckedInState ());
            return true;
        }
        @Override
        public boolean cancel (Booking booking){
            // Req8: bookings can only be cancelled BEFORE the start time.
            if (!isBefore (LocalDateTime.now (), booking.getStartTime ())){
                return false;
            }
            booking.setState (new CancelledState ());
            return true;
        }
        @Override
        public boolean edit (Booking booking, String start, String end){
            // Req8: bookings can only be edited BEFORE the start time.
            if (!isBefore (LocalDateTime.now (), booking.getStartTime ())){
                return false;
            }
            LocalDateTime newStart= parseTime (start);
            LocalDateTime newEnd= parseTime (end);
            if (newStart== null || newEnd== null || !newStart.isBefore (newEnd)){
                return false;
            }
            booking.setStartTime (start);
            booking.setEndTime (end);
            return true;
        }
        @Override
        public boolean extend (Booking booking, String until){
            return extendIfBeforeExpiry (booking, until);
        }
        @Override
        public void expire (Booking booking){
            booking.forfeitDeposit ();
            booking.setState (new ExpiredState ());
        }
        @Override
        public void complete (Booking booking){
            booking.setState (new CompletedState ());
        }
    }
    public static class CheckedInState implements BookingState{
        @Override
        public boolean checkIn (Booking booking){
            return false;
        }
        @Override
        public boolean cancel (Booking booking){
            return false;
        }
        @Override
        public boolean edit (Booking booking, String start, String end){
            return false;
        }
        @Override
        public boolean extend (Booking booking, String until){
            return extendIfBeforeExpiry (booking, until);
        }
        @Override
        public void expire (Booking booking){
            //no-op, already checked in
        }
        @Override
        public void complete (Booking booking){
            booking.setState (new CompletedState ());
        }
    }
    public static class CancelledState implements BookingState{
        @Override
        public boolean checkIn (Booking booking){
            return false;
        }
        @Override
        public boolean cancel (Booking booking){
            return false;
        }
        @Override
        public boolean edit (Booking booking, String start, String end){
            return false;
        }
        @Override
        public boolean extend (Booking booking, String until){
            return false;
        }
        @Override
        public void expire (Booking booking){
            //no-op
        }
        @Override
        public void complete (Booking booking){
            // no-op
        }
    }
    public static class CompletedState implements BookingState{
        @Override
        public boolean checkIn (Booking booking){
            return false;
        }
        @Override
        public boolean cancel (Booking booking){
            return false;
        }
        @Override
        public boolean edit (Booking booking, String start, String end){
            return false;
        }
        @Override
        public boolean extend (Booking booking, String until){
            return false;
        }
        @Override
        public void expire (Booking booking){
            // no-op
        }
        @Override
        public void complete (Booking booking){
            // no-op
        }
    }
    public static class ExpiredState implements BookingState{
        @Override
        public boolean checkIn (Booking booking){
            return false;
        }
        @Override
        public boolean cancel (Booking booking){
            return false;
        }
        @Override
        public boolean edit (Booking booking, String start, String end){
            return false;
        }
        @Override
        public boolean extend (Booking booking, String until){
            return false;
        }
        @Override
        public void expire (Booking booking){
            // no-op
        }
        @Override
        public void complete (Booking booking){
            // no op
        }
    }
    
    /** Req9: extensions are only allowed before expiry, and only to a LATER end time.
     *  (Room availability for the extension window is checked by BookingManager.) */
    private static boolean extendIfBeforeExpiry (Booking booking, String until){
        LocalDateTime currentEnd= parseTime (booking.getEndTime ());
        LocalDateTime newEnd= parseTime (until);
        if (currentEnd== null || newEnd== null){
            return false;
        }
        if (!LocalDateTime.now ().isBefore (currentEnd)){
            return false;
        }
        if (!newEnd.isAfter (currentEnd)){
            return false;
        }
        booking.setEndTime (until);
        return true;
    }
    
    
    
    private static boolean isBefore (LocalDateTime moment, String time){
        LocalDateTime parsed= parseTime (time);
        return parsed!= null && moment.isBefore (parsed);
    }
    private static LocalDateTime parseTime (String value){
        try{
            return LocalDateTime.parse (value);
        } catch (DateTimeParseException | NullPointerException exception){
            return null;
        }
    }
}