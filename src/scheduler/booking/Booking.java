package scheduler.booking;
import java.time.*;

public class Booking{
	
    private String bookingId;
    private String userEmail;
    private String roomId;
    private String startTime;
    private String endTime;
    private double depositAmount;
    private boolean depositForfeited;   // set by forfeitDeposit(), used by calculateRemainingBalance()
    private String checkInTime;         // non-null == the user checked in (replaces the old checkedIn flag)
    private BookingStatus status;       
    private BookingState state;

    public Booking(String bookingId, String userEmail, String roomId, String startTime, String endTime, double depositAmount, BookingStatus status) {
        this.bookingId = bookingId;
        this.userEmail = userEmail;
        this.roomId = roomId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.depositAmount = depositAmount;
        this.status = status;
        this.state = stateOf (status);
        this.depositForfeited = false;
    }

    /** Status -> state object. Lives in Booking because BookingStatus is Booking's
     *  responsibility (class diagram) - the state classes never touch the enum. */
    private static BookingState stateOf (BookingStatus status){
        return switch (status){
            case CONFIRMED-> new ConcreteStates.ConfirmedState ();
            case CHECKED_IN-> new ConcreteStates.CheckedInState ();
            case CANCELLED-> new ConcreteStates.CancelledState ();
            case COMPLETED-> new ConcreteStates.CompletedState ();
            case EXPIRED-> new ConcreteStates.ExpiredState ();
        };
    }
    /** State object -> status. The reverse mapping, also Booking's responsibility. */
    private static BookingStatus statusOf (BookingState state){
        if (state instanceof ConcreteStates.ConfirmedState){
            return BookingStatus.CONFIRMED;
        }
        if (state instanceof ConcreteStates.CheckedInState){
            return BookingStatus.CHECKED_IN;
        }
        if (state instanceof ConcreteStates.CancelledState){
            return BookingStatus.CANCELLED;
        }
        if (state instanceof ConcreteStates.CompletedState){
            return BookingStatus.COMPLETED;
        }
        if (state instanceof ConcreteStates.ExpiredState){
            return BookingStatus.EXPIRED;
        }
        throw new IllegalArgumentException ("Unknown booking state: " + state.getClass ().getSimpleName ());
    }
    
    
    public boolean checkIn (){
        boolean successful= state.checkIn (this);
        if (successful){
            this.checkInTime= LocalDateTime.now ().toString ();
        }
        return successful;
    }
    public boolean cancelBooking (){
        return state.cancel (this);
    }
    public boolean editBooking (String start, String end){
        return state.edit (this, start, end);
    }
    public boolean extendBooking (String until){
        return state.extend (this, until);
    }
    /**
     * Req3 calculates the complete price of the booking.
     * Because Req4 defines the deposit as one hour's fee, depositAmount is used as the hourly rate.
     */
    public double calculateFinalCost (){
        LocalDateTime start= LocalDateTime.parse (startTime);
        LocalDateTime end= LocalDateTime.parse (endTime);
        double durationHours= Duration.between (start, end).toMinutes ()/ 60.0;
        if (durationHours <= 0){
            return 0.0;
        }
        return durationHours * depositAmount;
    }
    /**
     * Req4: Returns the amount still owed after applying the deposit when the user checks in.
     * If the user never checked in (or the deposit was forfeited), the deposit is NOT applied.
     */
    public double calculateRemainingBalance (){
        double finalCost= calculateFinalCost ();
        if (depositForfeited || checkInTime== null){
            return finalCost;
        }
        return finalCost - depositAmount;
    }
    /** Req4: called when a user doesn't check in within the 30 minutes window. */
    public void forfeitDeposit (){
        // The paid deposit is kept and not refunded.
        this.depositForfeited= true;
    }
    // Getters and setters used by persistence and GUI
    public String getBookingId (){
        return bookingId;
    }
    public String getUserEmail (){
        return userEmail;
    }
    public String getRoomId (){
        return roomId;
    }
    public String getStartTime (){
        return startTime;
    }
    public void setStartTime (String startTime){
        this.startTime= startTime;
    }
    public String getEndTime (){
        return endTime;
    }
    public void setEndTime (String endTime){
        this.endTime= endTime;
    }
    public double getDepositAmount (){
        return depositAmount;
    }
    public String getCheckInTime (){
        return checkInTime;
    }
    public void setCheckInTime (String checkInTime){
        this.checkInTime= checkInTime;
    }
    public boolean isDepositForfeited (){
        return depositForfeited;
    }
    public void setDepositForfeited (boolean depositForfeited){
        this.depositForfeited= depositForfeited;
    }
    
    /** Called by the concrete state classes to transition. Status is updated in the
     *  same breath via statusOf(), so field and state object can never disagree. */
    public void setState (BookingState state){
        if (state== null){
            throw new IllegalArgumentException ("Booking state cannot be null.");
        }
        this.state= state;
        this.status= statusOf (state);
    }
    
    public BookingState getState (){
        return state;
    }

    public BookingStatus getStatus (){
        return status;
    }


    @Override
    public String toString() {
        return "Booking[" + bookingId + "] room=" + roomId 
            + " | " + startTime + " → " + endTime 
            + " | status=" + status 
            + " | deposit=$" + depositAmount;
    }
}