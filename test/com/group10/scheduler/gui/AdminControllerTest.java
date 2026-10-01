package com.group10.scheduler.gui;

import static org.junit.Assert.*;
import org.junit.*;

import com.group10.scheduler.accounts.Administrator;
import com.group10.scheduler.accounts.AccountManagement;
import com.group10.scheduler.accounts.FakeUserRepository;
import com.group10.scheduler.booking.BookingManager;
import com.group10.scheduler.booking.FakeBookingRepository;
import com.group10.scheduler.booking.FakePaymentRepository;
import com.group10.scheduler.facade.SchedulerFacade;
import com.group10.scheduler.room.FakeRoomRepository;
import com.group10.scheduler.room.RoomManager;

public class AdminControllerTest{

    private SchedulerFacade facade;
    private AdminController controller;

    @Before
    public void setUp (){
        RoomManager roomManager= new RoomManager (new FakeRoomRepository ());
        BookingManager bookingManager= new BookingManager (new FakeBookingRepository (), new FakePaymentRepository (), roomManager);
        AccountManagement accountManagement= new AccountManagement (new FakeUserRepository ());
        facade= new SchedulerFacade (roomManager, bookingManager, accountManagement);
        controller= new AdminController (facade);
    }

    @Test
    public void generateAdmin (){
        Administrator admin= controller.onGenerateAdminClicked ("gui-admin-1", "Alice Admin", "alice.admin@yorku.ca");
        assertNotNull (admin);
    }

    @Test
    public void noAdminSession (){
        assertFalse (controller.isAdminLoggedIn ());
    }

    @Test
    public void adminLogin (){
        controller.onGenerateAdminClicked ("gui-admin-2", "Bob Admin", "bob.admin@yorku.ca");
        assertTrue (controller.onAdminLoginClicked ("gui-admin-2"));
        assertTrue (controller.isAdminLoggedIn ());
        assertEquals ("gui-admin-2", controller.getCurrentAdminId ());
    }

    @Test
    public void invalidAdminLogin (){
        assertFalse (controller.onAdminLoginClicked ("not-an-admin"));
        assertFalse (controller.isAdminLoggedIn ());
    }

    @Test
    public void adminLogout (){
        controller.onGenerateAdminClicked ("gui-admin-3", "Carl Admin", "carl.admin@yorku.ca");
        controller.onAdminLoginClicked ("gui-admin-3");
        controller.onAdminLogoutClicked ();
        assertFalse (controller.isAdminLoggedIn ());
        assertNull (controller.getCurrentAdminId ());
    }

    @Test
    public void addRoomWithoutLogin (){
        assertFalse (controller.onAddRoomClicked ("R1", 10, "Bergeron", "100"));
    }

    @Test
    public void addRoom (){
        controller.onGenerateAdminClicked ("gui-admin-4", "Dan Admin", "dan.admin@yorku.ca");
        controller.onAdminLoginClicked ("gui-admin-4");
        assertTrue (controller.onAddRoomClicked ("R1", 10, "Bergeron", "100"));
        assertEquals (1, controller.getAllRooms ().size ());
    }

    @Test
    public void enableRoomWithoutLogin (){
        assertFalse (controller.onEnableRoomClicked ("R1"));
    }

    @Test
    public void enableRoom (){
        controller.onGenerateAdminClicked ("gui-admin-5", "Eve Admin", "eve.admin@yorku.ca");
        controller.onAdminLoginClicked ("gui-admin-5");
        controller.onAddRoomClicked ("R2", 5, "Ross", "200");
        assertTrue (controller.onDisableRoomClicked ("R2"));
        assertTrue (controller.onEnableRoomClicked ("R2"));
    }

    @Test
    public void disableRoomWithoutLogin (){
        assertFalse (controller.onDisableRoomClicked ("R1"));
    }

    @Test
    public void closeRoomWithoutLogin (){
        assertFalse (controller.onCloseRoomClicked ("R1"));
    }

    @Test
    public void closeRoom (){
        controller.onGenerateAdminClicked ("gui-admin-6", "Frank Admin", "frank.admin@yorku.ca");
        controller.onAdminLoginClicked ("gui-admin-6");
        controller.onAddRoomClicked ("R3", 8, "Bergeron", "300");
        assertTrue (controller.onCloseRoomClicked ("R3"));
    }

    @Test
    public void allRooms (){
        assertTrue (controller.getAllRooms ().isEmpty ());
    }
}
