package com.group10.scheduler.facade;

import com.group10.scheduler.accounts.AccountManagement;
import com.group10.scheduler.accounts.Administrator;
import com.group10.scheduler.accounts.ChiefEventCoordinator;
import com.group10.scheduler.accounts.RegisteredUser;
import com.group10.scheduler.booking.Booking;
import com.group10.scheduler.booking.BookingManager;
import com.group10.scheduler.booking.ConcreteStrategies;
import com.group10.scheduler.booking.Payment;
import com.group10.scheduler.booking.PaymentMethod;
import com.group10.scheduler.booking.PaymentStrategy;
import com.group10.scheduler.room.Room;
import com.group10.scheduler.room.RoomManager;

import java.util.List;

public class SchedulerFacade {
	private final RoomManager roomManager;
	private final BookingManager bookingManager;
	private final AccountManagement accountManagement;

	public SchedulerFacade(RoomManager roomManager, BookingManager bookingManager, AccountManagement accountManagement) {
		this.roomManager = roomManager;
		this.bookingManager = bookingManager;
		this.accountManagement = accountManagement;
	}

	public RegisteredUser createAccount(String email, String password, String accountType, String userName, String organizationId) {

		return accountManagement.createAccount(email, password, accountType, userName, organizationId);
		
	}

	public RegisteredUser login(String email, String password) {
		RegisteredUser user = accountManagement.findByEmail(email);
		if (user != null && user.getPassword().equals(password))
			return user;
		return null;
	}

	public List<Room> getBookableRooms(String start, String end) {
		return bookingManager.getBookableRooms(start, end);
	}

	// The payment dialog's field values arrive as plain strings; mapping them to
	// the right PaymentStrategy happens HERE, behind the facade, so the Strategy
	// pattern never leaks into the GUI. Expected fields per method:
	//   CREDIT_CARD           -> cardNumber, expiryDate, cvv
	//   DEBIT_CARD            -> cardNumber, pin
	//   INSTITUTIONAL_BILLING -> organizationId, billingAccount
	public Booking bookRoom(String userEmail, String roomId, String start, String end, PaymentMethod method, String... paymentFields) {
		RegisteredUser user = accountManagement.findByEmail(userEmail);
		Room room = roomManager.findRoomById(roomId);
		return bookingManager.bookRoom(user, room, start, end, method, paymentFields);
	}

	public boolean checkIn(String userEmail, String bookingId) {
		return bookingManager.checkIn(userEmail, bookingId);
	}

	// Edit, cancel, extend and pay delegate to BookingManager, which owns the
	// room-conflict checks, the deposit refund, and persistence. The facade must
	// NOT talk to the Booking object directly or those rules get bypassed.
	public boolean editBooking(String bookingId, String start, String end) {
		return bookingManager.editBooking(bookingId, start, end);
	}

	public boolean cancelBooking(String bookingId) {
		return bookingManager.cancelBooking(bookingId);
	}

	public boolean extendBooking(String bookingId, String until) {
		return bookingManager.extendBooking(bookingId, until);
	}

	// Same field convention as bookRoom (see comment above).
	public double payForBooking(String bookingId, PaymentMethod method, String... paymentFields) {
		return bookingManager.payForBooking(bookingId, method, paymentFields);
	}


	// Req6: room operations require a valid adminId. The CHIEF (Singleton) is the
	// authority on who is an administrator; the facade only routes the call
	// through the Administrator object - the sole class allowed to mutate rooms.
	public boolean addRoom(String adminId, Room room) {
		Administrator admin = ChiefEventCoordinator.getInstance().findExistingAdministrator(adminId);
		return admin != null && admin.addRoom(room);
	}

	public boolean enableRoom(String adminId, String roomId) {
		Administrator admin = ChiefEventCoordinator.getInstance().findExistingAdministrator(adminId);
		return admin != null && admin.enableRoom(roomId);
	}

	public boolean disableRoom(String adminId, String roomId) {
		Administrator admin = ChiefEventCoordinator.getInstance().findExistingAdministrator(adminId);
		return admin != null && admin.disableRoom(roomId);
	}

	public boolean closeRoom(String adminId, String roomId) {
		Administrator admin = ChiefEventCoordinator.getInstance().findExistingAdministrator(adminId);
		return admin != null && admin.closeRoom(roomId);
	}

	// Admin session support for the AdminController: the chief is the authority.
	public boolean isAdministrator(String adminId) {
		return ChiefEventCoordinator.getInstance().findExistingAdministrator(adminId) != null;
	}

	//Creating admins by using chief singleton (the chief keeps the registry)
	public Administrator generateAdministratorAccount(String adminId, String name, String email) {
	    return ChiefEventCoordinator.getInstance().generateAdministratorAccount(adminId, name, email, roomManager);
	}

	// read-only views for the GUI (safe: managers hand out defensive copies)
	public Payment findPaymentByBookingId(String bookingId) {
		return bookingManager.findPaymentByBookingId(bookingId);
	}

	public List<Room> getAllRooms() {
		return roomManager.getAllRooms();
	}

	public List<Booking> getAllBookings() {
		return bookingManager.getAllBookings();
	}
}