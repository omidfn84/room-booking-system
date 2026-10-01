package com.group10.scheduler.booking;
import com.group10.scheduler.accounts.RegisteredUser;
import com.group10.scheduler.persistence.BookingRepository;
import com.group10.scheduler.persistence.PaymentRepository;
import com.group10.scheduler.room.Room;
import com.group10.scheduler.room.RoomManager;

import java.time.*;
import java.time.format.*;
import java.util.*;

public class BookingManager{
	
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final RoomManager roomManager;
    private List <Booking> bookings;
    private List <Payment> payments;
    
    public BookingManager (BookingRepository bookingRepository, PaymentRepository paymentRepository, RoomManager roomManager){
        this.bookingRepository= bookingRepository;
        this.paymentRepository= paymentRepository;
        this.roomManager= roomManager;
        this.bookings= new ArrayList <> (bookingRepository.loadBookings ());
        this.payments= new ArrayList <> (paymentRepository.loadPayments ());
    }
    public Booking findBooking (String bookingId){
        for (Booking booking : bookings){
            if (bookingId.equals (booking.getBookingId ())){
                return booking;
            }
        }
        return null;
    }
    public Payment findPaymentByBookingId (String bookingId){
        for (Payment payment : payments){
            if (bookingId.equals (payment.getBookingId ())){
                return payment;
            }
        }
        return null;
    }
    /** Req3/Req4: Creates a booking and charges one hour's fee as a deposit. */
    public Booking bookRoom (RegisteredUser user, Room room, String start, String end, PaymentMethod method,  String... paymentFields){
    	
    	PaymentStrategy strategy = buildStrategy(method, paymentFields);
        
    	LocalDateTime startTime;
        LocalDateTime endTime;
        if (user== null || room== null){
            throw new IllegalArgumentException ("User or room cannot be null.");
        }
        if (method== null || strategy== null){
            throw new IllegalArgumentException("Payment method or strategy cannot be null.");
        }
        try{
            startTime= LocalDateTime.parse (start);
            endTime= LocalDateTime.parse (end);
        } catch (DateTimeParseException exception){
            throw new IllegalArgumentException ("Start and end times must use the correct format.");
        }
        if (!startTime.isBefore (endTime)){
            throw new IllegalArgumentException ("Booking start time must be before end time.");
        }
        if (!room.isAvailable ()){
            throw new IllegalStateException ("Room " + room.getRoomId () + " is not available");
        }
        if (hasConflict (room.getRoomId (), null, start, end)){
            throw new IllegalStateException ("Room is already booked during the set period of time.");
        }
        double deposit= user.getHourlyRate ();
        //UUID.randomUUID for generating uniquee bookingId
        String bookingId= UUID.randomUUID ().toString ();
        Payment payment= new Payment(UUID.randomUUID ().toString (), bookingId, deposit, method, PaymentStatus.PENDING, LocalDateTime.now ().toString (), strategy);
        if (!payment.processDeposit (deposit)){
            throw new IllegalStateException ("The deposit payment failed.");
        }
        Booking booking= new Booking(bookingId, user.getEmail (), room.getRoomId (), start, end, deposit, BookingStatus.CONFIRMED);
        bookings.add (booking);
        payments.add (payment);
        bookingRepository.saveBookings (bookings);
        paymentRepository.savePayments (payments);
        return booking;
    }
    /** Req4: check-in must happen within 30 minutes of start time, or the deposit is forfeited.
     *  Req5: verifies occupancy and scans the ID badge via the room's sensor system. */
    public boolean checkIn (String userEmail, String bookingId){
    	
        LocalDateTime bookingStart;
        Booking booking= findBooking (bookingId);
        if (booking== null || userEmail== null || !userEmail.equals (booking.getUserEmail ())){
            return false;
        }
        try{
            bookingStart= LocalDateTime.parse (booking.getStartTime ());
        } catch (DateTimeParseException exception){
            return false;
        }
        LocalDateTime checkInDeadline = bookingStart.plusMinutes (30);
        // The user cannot check in before the booking starts
        if (LocalDateTime.now ().isBefore (bookingStart)){
            return false;
        }
        // After 30 minutes, the booking expires and the deposit is lost.
        if (LocalDateTime.now ().isAfter (checkInDeadline)){
            booking.getState ().expire (booking);
            bookingRepository.saveBookings (bookings);
            return false;
        }
        Room room= roomManager.findRoomById (booking.getRoomId ());
        if (room== null || room.getSensorSystem ()== null){
            return false;
        }
        // Req5: scan the ID badge FIRST (the scan is what marks the room occupied),
        // then confirm the occupancy sensor sees the user. The old order
        // (detectOccupancy && scanIDBadge) short-circuited on the unoccupied room
        // and made every check-in fail.
        boolean sensorApproved= room.getSensorSystem ().scanIDBadge (userEmail) && room.getSensorSystem ().detectOccupancy ();
        if (!sensorApproved){
            return false;
        }
        // Booking.checkIn () records the check-in timestamp itself on success.
        boolean ok= booking.checkIn ();
        bookingRepository.saveBookings (bookings);
        return ok;
    }
    
    /** Req8: edits are only allowed before the start time (enforced by the state)
     *  and only if the room stays conflict free for the new time range. */
    public boolean editBooking (String bookingId, String start, String end){
    	
        Booking booking= findBooking (bookingId);
        if (booking== null){
            return false;
        }
        if (hasConflict (booking.getRoomId (), bookingId, start, end)){
            return false;
        }
        boolean ok= booking.editBooking (start, end);
        if (ok){
            bookingRepository.saveBookings (bookings);
        }
        return ok;
    }
    /** Req8: cancellation only before the start time (enforced by the state).
     *  A paid deposit is refunded when the cancellation succeeds. */
    public boolean cancelBooking (String bookingId){
    	
        Booking booking= findBooking (bookingId);
        if (booking== null){
            return false;
        }
        boolean ok= booking.cancelBooking ();
        if (ok){
            Payment payment= findPaymentByBookingId (bookingId);
            if (payment!= null && payment.refundPayment ()){
                paymentRepository.savePayments (payments);
            }
            bookingRepository.saveBookings (bookings);
        }
        return ok;
    }
    
    /** Req9: bookings may be extended before expiry if the room is available for the extra time. */
    public boolean extendBooking (String bookingId, String until){
        Booking booking= findBooking (bookingId);
        if (booking== null){
            return false;
        }
        if (hasConflict (booking.getRoomId (), bookingId, booking.getEndTime (), until)){
            return false;
        }
        boolean ok= booking.extendBooking (until);
        if (ok){
            bookingRepository.saveBookings (bookings);
        }
        return ok;
    }
    
    
	// (method, fields) -> ConcreteStrategy. The only place this mapping exists.
	private PaymentStrategy buildStrategy(PaymentMethod method, String... fields) {
		if (method == null || fields == null) {
			throw new IllegalArgumentException("Payment method and details are required.");
		}
		switch (method) {
			case CREDIT_CARD:
				if (fields.length != 3) {
					throw new IllegalArgumentException("Credit card needs cardNumber, expiryDate, cvv.");
				}
				return new ConcreteStrategies.CreditCardStrategy(fields[0], fields[1], fields[2]);
			case DEBIT_CARD:
				if (fields.length != 2) {
					throw new IllegalArgumentException("Debit card needs cardNumber, pin.");
				}
				return new ConcreteStrategies.DebitCardStrategy(fields[0], fields[1]);
			case INSTITUTIONAL_BILLING:
				if (fields.length != 2) {
					throw new IllegalArgumentException("Institutional billing needs organizationId, billingAccount.");
				}
				long orgId;
				try {
					orgId = Long.parseLong(fields[0].trim());
				} catch (NumberFormatException nfe) {
					throw new IllegalArgumentException("Organization ID must be a number.");
				}
				return new ConcreteStrategies.InstitutionalBillingStrategy(orgId, fields[1]);
			default:
				throw new IllegalArgumentException("Unsupported payment method: " + method);
		}
	}
    
    /** Req4/Req10: pays the remaining balance (final cost minus the applied deposit).
     *  A successful final payment completes the booking. */
    public double payForBooking (String bookingId, PaymentMethod method, String... paymentFields){
    	
    	PaymentStrategy strategy = buildStrategy(method, paymentFields);
        Booking booking= findBooking (bookingId);
        if (booking== null || method== null || strategy== null){
            return -1;
        }
        double remaining= booking.calculateRemainingBalance ();
        if (remaining<= 0){
            return 0;
        }
        Payment payment= new Payment (UUID.randomUUID ().toString (), bookingId, remaining, method, PaymentStatus.PENDING, LocalDateTime.now ().toString (), strategy);
        boolean ok= payment.processPayment (remaining);
        payments.add (payment);
        paymentRepository.savePayments (payments);
        if (ok){
            booking.getState ().complete (booking);
            bookingRepository.saveBookings (bookings);
        }
        return remaining;
    }
    /**
     * Saves changes made directly through the Booking State pattern; such as cancellation, editing, or extension.
     */
    public void persistBookings (){
        bookingRepository.saveBookings (bookings);
    }
    /** Req3 and Req9: Returns rooms that are enabled and not already booked during the requested time range. */
    public List <Room> getBookableRooms (String start, String end){
        LocalDateTime startTime;
        LocalDateTime endTime;
        try{
            startTime= LocalDateTime.parse (start);
            endTime= LocalDateTime.parse (end);
        } catch (DateTimeParseException exception){
            return new ArrayList <> ();
        }
        if (!startTime.isBefore (endTime)){
            return new ArrayList <> ();
        }
        List <String> bookedRoomIds= new ArrayList <> ();
        for (Booking booking : bookings){
            if (booking.getStatus ()== BookingStatus.CANCELLED || booking.getStatus ()== BookingStatus.EXPIRED || booking.getStatus ()== BookingStatus.COMPLETED){
                continue;
            }
            if (overlaps (booking.getStartTime (), booking.getEndTime (), start, end)){
                bookedRoomIds.add (booking.getRoomId ());
            }
        }
        List <Room> availableRooms= new ArrayList <> ();
        for (Room room : roomManager.getAvailableRooms ()){
            if (!bookedRoomIds.contains (room.getRoomId ())){
                availableRooms.add (room);
            }
        }
        return availableRooms;
    }
    
    
    /** Returns true when the room already has an active booking overlapping [start, end].
     *  excludeBookingId lets edit/extend ignore the booking being changed. */
    private boolean hasConflict (String roomId, String excludeBookingId, String start, String end){
        for (Booking booking : bookings){
            if (booking.getBookingId ().equals (excludeBookingId)){
                continue;
            }
            if (booking.getStatus ()== BookingStatus.CANCELLED || booking.getStatus ()== BookingStatus.EXPIRED || booking.getStatus ()== BookingStatus.COMPLETED){
                // COMPLETED also frees the slot: the room was already released.
                continue;
            }
            if (!booking.getRoomId ().equals (roomId)){
                continue;
            }
            if (overlaps (booking.getStartTime (), booking.getEndTime (), start, end)){
                return true;
            }
        }
        return false;
    }
    
    private boolean overlaps (String start, String end, String start2, String end2){
        try{
            LocalDateTime firstStart = LocalDateTime.parse (start);
            LocalDateTime firstEnd= LocalDateTime.parse (end);
            LocalDateTime secondStart= LocalDateTime.parse (start2);
            LocalDateTime secondEnd= LocalDateTime.parse(end2);
            return firstStart.isBefore (secondEnd) && secondStart.isBefore (firstEnd);
        } catch (DateTimeParseException exception){
            // A row with unparseable times cannot be proven conflict free.
            return true;
        }
    }
    
    public List<Booking> getAllBookings() {
        return new ArrayList<> (bookings);
    }
    public List<Payment> getAllPayments() {
        return new ArrayList<> (payments);
    }
}