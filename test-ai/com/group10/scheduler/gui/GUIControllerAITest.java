package com.group10.scheduler.gui;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.group10.scheduler.accounts.RegisteredUser;
import com.group10.scheduler.aisupport.AIFixture;
import com.group10.scheduler.booking.Booking;
import com.group10.scheduler.booking.BookingStatus;
import com.group10.scheduler.booking.PaymentMethod;
import com.group10.scheduler.room.Room;

/**
 * GUIController is the registered-user half of the presentation layer. It owns
 * exactly one piece of state - the logged-in user - and every action is
 * performed AS that user.
 *
 * The behaviour worth testing here is the session, not the business rules:
 * who the controller believes it is acting for, what happens before anyone has
 * logged in, and that My Bookings never leaks another user's data.
 */
public class GUIControllerAITest {

    private AIFixture fx;
    private GUIController controller;

    private static final String[] CARD = {"4111111111111111", "12/27", "123"};

    @Before
    public void setUp() {
        fx = new AIFixture();
        controller = new GUIController(fx.facade);
        fx.addRoom("R1");
    }

    private void registerAndLoginAlice() {
        controller.onRegisterClicked("alice@yorku.ca", "Passw0rd!", "Alice", "STUDENT", "123456789");
    }

    @Test
    public void aFreshControllerHasNobodyLoggedIn() {
        assertNull(controller.getCurrentUser());
    }

    @Test
    public void onRegisterClicked_createsTheAccountAndOpensASession() {
        RegisteredUser user = controller.onRegisterClicked("alice@yorku.ca", "Passw0rd!", "Alice", "STUDENT", "123456789");

        assertNotNull(user);
        assertEquals("alice@yorku.ca", user.getEmail());
        assertSame("Registering must log the new user straight in", user, controller.getCurrentUser());
    }

    @Test
    public void onRegisterClicked_propagatesValidationFailuresToTheGuiAsExceptions() {
        // LoginPanel shows ex.getMessage() in its status label, so the
        // exception has to travel rather than be swallowed here.
        try {
            controller.onRegisterClicked("not-an-email", "Passw0rd!", "Alice", "STUDENT", "123456789");
            fail("Expected the account subsystem's validation to surface");
        } catch (IllegalArgumentException expected) {
            assertNotNull(expected.getMessage());
        }
        assertNull("A failed registration must not open a session", controller.getCurrentUser());
    }

    @Test
    public void onLoginClicked_storesTheUserOnSuccess() {
        registerAndLoginAlice();

        RegisteredUser user = controller.onLoginClicked("alice@yorku.ca", "Passw0rd!");

        assertNotNull(user);
        assertEquals("alice@yorku.ca", controller.getCurrentUser().getEmail());
    }

    @Test
    public void onLoginClicked_clearsTheSessionOnFailure() {
        // The controller assigns the result unconditionally, so a failed login
        // after a good one must not leave the previous user signed in.
        registerAndLoginAlice();
        assertNotNull(controller.getCurrentUser());

        assertNull(controller.onLoginClicked("alice@yorku.ca", "WrongPass1!"));
        assertNull("A failed login must not keep the old session alive", controller.getCurrentUser());
    }

    @Test
    public void onLoginClicked_returnsNullForAnUnknownEmail() {
        assertNull(controller.onLoginClicked("ghost@yorku.ca", "Passw0rd!"));
        assertNull(controller.getCurrentUser());
    }

    @Test
    public void onSearchRoomsClicked_worksWithoutASessionBecauseBrowsingIsPublic() {
        List<Room> rooms = controller.onSearchRoomsClicked(AIFixture.futureStart(), AIFixture.futureEnd());

        assertEquals(1, rooms.size());
    }

    @Test
    public void onSearchRoomsClicked_returnsAnEmptyListForUnparseableInput() {
        assertTrue(controller.onSearchRoomsClicked("garbage", AIFixture.futureEnd()).isEmpty());
    }

    @Test
    public void onBookRoomClicked_booksAsTheLoggedInUser() {
        registerAndLoginAlice();

        Booking booking = controller.onBookRoomClicked("R1", AIFixture.futureStart(), AIFixture.futureEnd(),
                PaymentMethod.CREDIT_CARD, CARD);

        assertNotNull(booking);
        assertEquals("The session user, not a parameter, decides the owner", "alice@yorku.ca", booking.getUserEmail());
    }

    @Test(expected = NullPointerException.class)
    public void onBookRoomClicked_failsWhenNobodyIsLoggedIn() {
        // The controller reads currentUser.getEmail() directly. BookingPanel
        // only enables booking after login, so this documents the current
        // contract: booking without a session is a programming error, not a
        // user-facing outcome.
        controller.onBookRoomClicked("R1", AIFixture.futureStart(), AIFixture.futureEnd(),
                PaymentMethod.CREDIT_CARD, CARD);
    }

    @Test
    public void onCheckInClicked_checksInTheSessionUsersBooking() {
        registerAndLoginAlice();
        Booking booking = controller.onBookRoomClicked("R1", AIFixture.startedRecently(), AIFixture.hoursFromNow(1),
                PaymentMethod.CREDIT_CARD, CARD);

        assertTrue(controller.onCheckInClicked(booking.getBookingId()));
        assertEquals(BookingStatus.CHECKED_IN, booking.getStatus());
    }

    @Test
    public void onCheckInClicked_returnsFalseForAnUnknownBooking() {
        registerAndLoginAlice();

        assertFalse(controller.onCheckInClicked("no-such-booking"));
    }

    @Test
    public void onCancelClicked_cancelsBeforeStartAndRefusesAfterwards() {
        registerAndLoginAlice();
        Booking future = controller.onBookRoomClicked("R1", AIFixture.futureStart(), AIFixture.futureEnd(),
                PaymentMethod.CREDIT_CARD, CARD);

        assertTrue(controller.onCancelClicked(future.getBookingId()));
        assertEquals(BookingStatus.CANCELLED, future.getStatus());
        assertFalse("Cancelling twice must fail", controller.onCancelClicked(future.getBookingId()));
    }

    @Test
    public void onEditBookingClicked_movesTheBookingBeforeItStarts() {
        registerAndLoginAlice();
        Booking booking = controller.onBookRoomClicked("R1", AIFixture.futureStart(), AIFixture.futureEnd(),
                PaymentMethod.CREDIT_CARD, CARD);
        String newStart = AIFixture.hoursFromNow(6);
        String newEnd = AIFixture.hoursFromNow(7);

        assertTrue(controller.onEditBookingClicked(booking.getBookingId(), newStart, newEnd));
        assertEquals(newStart, booking.getStartTime());
    }

    @Test
    public void onExtendClicked_pushesTheEndTimeOut() {
        registerAndLoginAlice();
        Booking booking = controller.onBookRoomClicked("R1", AIFixture.startedRecently(), AIFixture.hoursFromNow(1),
                PaymentMethod.CREDIT_CARD, CARD);
        String later = AIFixture.hoursFromNow(3);

        assertTrue(controller.onExtendClicked(booking.getBookingId(), later));
        assertEquals(later, booking.getEndTime());
    }

    @Test
    public void onPayClicked_returnsTheAmountChargedAndMinusOneWhenUnknown() {
        registerAndLoginAlice();
        Booking booking = controller.onBookRoomClicked("R1", AIFixture.startedRecently(), AIFixture.hoursFromNow(2),
                PaymentMethod.CREDIT_CARD, CARD);
        controller.onCheckInClicked(booking.getBookingId());

        assertTrue(controller.onPayClicked(booking.getBookingId(), PaymentMethod.CREDIT_CARD, CARD) > 0);
        assertEquals(-1.0, controller.onPayClicked("no-such-booking", PaymentMethod.CREDIT_CARD, CARD), 0.001);
    }

    @Test
    public void getPaymentForBooking_returnsTheReceiptOrNull() {
        registerAndLoginAlice();
        Booking booking = controller.onBookRoomClicked("R1", AIFixture.futureStart(), AIFixture.futureEnd(),
                PaymentMethod.CREDIT_CARD, CARD);

        assertNotNull(controller.getPaymentForBooking(booking.getBookingId()));
        assertNull(controller.getPaymentForBooking("no-such-booking"));
    }

    @Test
    public void getMyBookings_isEmptyWhenNobodyIsLoggedIn() {
        // Must return an empty list rather than throwing: BookingPanel calls
        // refreshMyBookings() before any login has happened.
        assertTrue(controller.getMyBookings().isEmpty());
    }

    @Test
    public void getMyBookings_returnsOnlyTheSessionUsersBookings() {
        fx.addRoom("R2");

        registerAndLoginAlice();
        controller.onBookRoomClicked("R1", AIFixture.futureStart(), AIFixture.futureEnd(),
                PaymentMethod.CREDIT_CARD, CARD);

        controller.onRegisterClicked("bob@yorku.ca", "Passw0rd!", "Bob", "FACULTY", "987654321");
        controller.onBookRoomClicked("R2", AIFixture.futureStart(), AIFixture.futureEnd(),
                PaymentMethod.CREDIT_CARD, CARD);

        List<Booking> bobs = controller.getMyBookings();
        assertEquals(1, bobs.size());
        assertEquals("bob@yorku.ca", bobs.get(0).getUserEmail());

        controller.onLoginClicked("alice@yorku.ca", "Passw0rd!");
        List<Booking> alices = controller.getMyBookings();
        assertEquals(1, alices.size());
        assertEquals("alice@yorku.ca", alices.get(0).getUserEmail());
    }

    @Test
    public void getMyBookings_growsAsTheSessionUserBooksMore() {
        fx.addRoom("R2");
        registerAndLoginAlice();
        assertTrue(controller.getMyBookings().isEmpty());

        controller.onBookRoomClicked("R1", AIFixture.futureStart(), AIFixture.futureEnd(),
                PaymentMethod.CREDIT_CARD, CARD);
        assertEquals(1, controller.getMyBookings().size());

        controller.onBookRoomClicked("R2", AIFixture.futureStart(), AIFixture.futureEnd(),
                PaymentMethod.CREDIT_CARD, CARD);
        assertEquals(2, controller.getMyBookings().size());
    }
}
