package scheduler.gui;

import scheduler.accounts.RegisteredUser;
import scheduler.booking.Booking;
import scheduler.booking.Payment;
import scheduler.booking.PaymentMethod;
import scheduler.facade.SchedulerFacade;
import scheduler.room.Room;

import java.util.List;

/**
 * GUIController — presentation logic for the REGISTERED USER actor
 * (Req1, Req3, Req4, Req5, Req8, Req9, Req10). Session state is the
 * logged-in RegisteredUser; every action acts as that user. The chief and
 * administrator actors live in AdminController with their own admin session.
 * Both controllers depend on SchedulerFacade only.
 */
public class GUIController {

	private final SchedulerFacade facade;
    private RegisteredUser currentUser;

    public GUIController(SchedulerFacade facade) {
        this.facade = facade;
    }

    public RegisteredUser getCurrentUser() { 
    	return currentUser; 
    }

    public RegisteredUser onLoginClicked(String email, String password) {
        currentUser = facade.login(email, password);
        return currentUser;
    }


    // organizationId stays a String all the way down: the account subsystem
    // validates the 9-digit rule and produces the message the GUI displays.
    public RegisteredUser onRegisterClicked(String email, String password, String userName, String accountType, String organizationId) {
        RegisteredUser user = facade.createAccount(email, password, accountType, userName, organizationId);
        currentUser = user;
        return user;
    }

    public List<Room> onSearchRoomsClicked(String start, String end) {
        return facade.getBookableRooms(start, end);
    }

    /** paymentFields are the raw dialog values; see SchedulerFacade.bookRoom
     *  for the per-method field convention. */
    public Booking onBookRoomClicked(String roomId, String start, String end, PaymentMethod method, String... paymentFields) {
        return facade.bookRoom(currentUser.getEmail(), roomId, start, end, method, paymentFields);
    }

    public boolean onCheckInClicked(String bookingId) {
        return facade.checkIn(currentUser.getEmail(), bookingId);
    }

    public boolean onCancelClicked(String bookingId) {
        return facade.cancelBooking(bookingId);
    }

    public boolean onExtendClicked(String bookingId, String until) {
        return facade.extendBooking(bookingId, until);
    }

    public boolean onEditBookingClicked(String bookingId, String start, String end) {
        return facade.editBooking(bookingId, start, end);
    }

    public double onPayClicked(String bookingId, PaymentMethod method, String... paymentFields) {
        return facade.payForBooking(bookingId, method, paymentFields);
    }

    /** Payment receipt for the GUI (shown after paying, or in a history view). */
    public Payment getPaymentForBooking(String bookingId) {
        return facade.findPaymentByBookingId(bookingId);
    }

    public List<Booking> getMyBookings() {
        if (currentUser == null) 
        	return List.of();
        return facade.getAllBookings().stream().filter(b -> b.getUserEmail().equals(currentUser.getEmail())).toList();
    }
}