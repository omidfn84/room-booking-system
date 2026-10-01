package com.group10.scheduler.gui;

import static org.junit.Assert.*;
import org.junit.*;

import java.time.LocalDateTime;
import java.util.List;

import com.group10.scheduler.accounts.AccountManagement;
import com.group10.scheduler.accounts.FakeUserRepository;
import com.group10.scheduler.accounts.RegisteredUser;
import com.group10.scheduler.booking.Booking;
import com.group10.scheduler.booking.BookingManager;
import com.group10.scheduler.booking.FakeBookingRepository;
import com.group10.scheduler.booking.FakePaymentRepository;
import com.group10.scheduler.booking.PaymentMethod;
import com.group10.scheduler.facade.SchedulerFacade;
import com.group10.scheduler.room.FakeRoomRepository;
import com.group10.scheduler.room.Room;
import com.group10.scheduler.room.RoomManager;
import com.group10.scheduler.room.RoomStatus;

public class GUIControllerTest{

    private SchedulerFacade facade;
    private GUIController controller;
    private final String[] creditCardFields= {"1234567890123456", "12/30", "123"};

    private String iso (LocalDateTime t){ return t.toString (); }

    @Before
    public void setUp (){
        RoomManager roomManager= new RoomManager (new FakeRoomRepository ());
        BookingManager bookingManager= new BookingManager (new FakeBookingRepository (), new FakePaymentRepository (), roomManager);
        AccountManagement accountManagement= new AccountManagement (new FakeUserRepository ());
        facade= new SchedulerFacade (roomManager, bookingManager, accountManagement);
        controller= new GUIController (facade);

        roomManager.addRoom (new Room ("R1", 10, "Bergeron", "100", RoomStatus.AVAILABLE));
    }

    @Test
    public void noCurrentUser (){
        assertNull (controller.getCurrentUser ());
    }

    @Test
    public void registerUser (){
        RegisteredUser u= controller.onRegisterClicked ("alice@yorku.ca", "Passw0rd!", "Alice", "STUDENT", "123456789");
        assertNotNull (u);
        assertEquals (u, controller.getCurrentUser ());
    }

    @Test
    public void validLogin (){
        facade.createAccount ("alice@yorku.ca", "Passw0rd!", "STUDENT", "Alice", "123456789");
        RegisteredUser u= controller.onLoginClicked ("alice@yorku.ca", "Passw0rd!");
        assertNotNull (u);
        assertEquals (u, controller.getCurrentUser ());
    }

    @Test
    public void invalidLogin (){
        RegisteredUser u= controller.onLoginClicked ("nobody@yorku.ca", "wrong");
        assertNull (u);
        assertNull (controller.getCurrentUser ());
    }

    @Test
    public void searchRooms (){
        List <Room> rooms= controller.onSearchRoomsClicked (iso (LocalDateTime.now ().plusHours (1)), iso (LocalDateTime.now ().plusHours (2)));
        assertEquals (1, rooms.size ());
    }

    @Test
    public void bookRoomUsesCurrentUser (){
        controller.onRegisterClicked ("alice@yorku.ca", "Passw0rd!", "Alice", "STUDENT", "123456789");
        Booking b= controller.onBookRoomClicked ("R1", iso (LocalDateTime.now ().plusHours (1)),
                iso (LocalDateTime.now ().plusHours (2)), PaymentMethod.CREDIT_CARD, creditCardFields);
        assertNotNull (b);
        assertEquals ("alice@yorku.ca", b.getUserEmail ());
    }

    @Test
    public void checkIn (){
        controller.onRegisterClicked ("alice@yorku.ca", "Passw0rd!", "Alice", "STUDENT", "123456789");
        Booking b= controller.onBookRoomClicked ("R1", iso (LocalDateTime.now ().minusMinutes (10)),
                iso (LocalDateTime.now ().plusHours (2)), PaymentMethod.CREDIT_CARD, creditCardFields);
        assertTrue (controller.onCheckInClicked (b.getBookingId ()));
    }

    @Test
    public void cancel (){
        controller.onRegisterClicked ("alice@yorku.ca", "Passw0rd!", "Alice", "STUDENT", "123456789");
        Booking b= controller.onBookRoomClicked ("R1", iso (LocalDateTime.now ().plusHours (1)),
                iso (LocalDateTime.now ().plusHours (2)), PaymentMethod.CREDIT_CARD, creditCardFields);
        assertTrue (controller.onCancelClicked (b.getBookingId ()));
    }

    @Test
    public void extend (){
        controller.onRegisterClicked ("alice@yorku.ca", "Passw0rd!", "Alice", "STUDENT", "123456789");
        Booking b= controller.onBookRoomClicked ("R1", iso (LocalDateTime.now ().minusMinutes (30)),
                iso (LocalDateTime.now ().plusHours (1)), PaymentMethod.CREDIT_CARD, creditCardFields);
        assertTrue (controller.onExtendClicked (b.getBookingId (), iso (LocalDateTime.now ().plusHours (3))));
    }

    @Test
    public void editBooking (){
        controller.onRegisterClicked ("alice@yorku.ca", "Passw0rd!", "Alice", "STUDENT", "123456789");
        Booking b= controller.onBookRoomClicked ("R1", iso (LocalDateTime.now ().plusHours (2)),
                iso (LocalDateTime.now ().plusHours (4)), PaymentMethod.CREDIT_CARD, creditCardFields);
        assertTrue (controller.onEditBookingClicked (b.getBookingId (),
                iso (LocalDateTime.now ().plusHours (3)), iso (LocalDateTime.now ().plusHours (5))));
    }

    @Test
    public void pay (){
        controller.onRegisterClicked ("alice@yorku.ca", "Passw0rd!", "Alice", "STUDENT", "123456789");
        Booking b= controller.onBookRoomClicked ("R1", iso (LocalDateTime.now ().minusMinutes (10)),
                iso (LocalDateTime.now ().plusHours (2)), PaymentMethod.CREDIT_CARD, creditCardFields);
        controller.onCheckInClicked (b.getBookingId ());
        double remaining= controller.onPayClicked (b.getBookingId (), PaymentMethod.CREDIT_CARD, creditCardFields);
        assertTrue (remaining > 0);
    }

    @Test
    public void findPayment (){
        controller.onRegisterClicked ("alice@yorku.ca", "Passw0rd!", "Alice", "STUDENT", "123456789");
        Booking b= controller.onBookRoomClicked ("R1", iso (LocalDateTime.now ().plusHours (1)),
                iso (LocalDateTime.now ().plusHours (2)), PaymentMethod.CREDIT_CARD, creditCardFields);
        assertNotNull (controller.getPaymentForBooking (b.getBookingId ()));
    }

    @Test
    public void noUserBookings (){
        assertTrue (controller.getMyBookings ().isEmpty ());
    }

    @Test
    public void currentUserBookings (){
        controller.onRegisterClicked ("alice@yorku.ca", "Passw0rd!", "Alice", "STUDENT", "123456789");
        controller.onBookRoomClicked ("R1", iso (LocalDateTime.now ().plusHours (1)),
                iso (LocalDateTime.now ().plusHours (2)), PaymentMethod.CREDIT_CARD, creditCardFields);

        // Another user's booking should not be included.
        facade.createAccount ("bob@yorku.ca", "Passw0rd!", "STAFF", "Bob", "987654321");
        facade.bookRoom ("bob@yorku.ca", "R1", iso (LocalDateTime.now ().plusHours (5)),
                iso (LocalDateTime.now ().plusHours (6)), PaymentMethod.CREDIT_CARD, creditCardFields);

        List <Booking> myBookings= controller.getMyBookings ();
        assertEquals (1, myBookings.size ());
        assertEquals ("alice@yorku.ca", myBookings.get (0).getUserEmail ());
    }
}

