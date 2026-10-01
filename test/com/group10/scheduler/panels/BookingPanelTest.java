package com.group10.scheduler.panels;

import static org.junit.Assert.*;
import org.junit.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.lang.reflect.Field;
import java.time.LocalDateTime;

import com.group10.scheduler.accounts.AccountManagement;
import com.group10.scheduler.accounts.FakeUserRepository;
import com.group10.scheduler.booking.BookingManager;
import com.group10.scheduler.booking.FakeBookingRepository;
import com.group10.scheduler.booking.FakePaymentRepository;
import com.group10.scheduler.facade.SchedulerFacade;
import com.group10.scheduler.gui.GUIController;
import com.group10.scheduler.room.FakeRoomRepository;
import com.group10.scheduler.room.Room;
import com.group10.scheduler.room.RoomManager;
import com.group10.scheduler.room.RoomStatus;

/**
 * Tests BookingPanel without opening a new window.
 */
public class BookingPanelTest{

    private GUIController controller;
    private BookingPanel panel;

    private String iso (LocalDateTime t){ return t.toString (); }

    @Before
    public void setUp (){
        RoomManager roomManager= new RoomManager (new FakeRoomRepository ());
        BookingManager bookingManager= new BookingManager (new FakeBookingRepository (), new FakePaymentRepository (), roomManager);
        AccountManagement accountManagement= new AccountManagement (new FakeUserRepository ());
        SchedulerFacade facade= new SchedulerFacade (roomManager, bookingManager, accountManagement);
        controller= new GUIController (facade);

        roomManager.addRoom (new Room ("R1", 10, "Bergeron", "100", RoomStatus.AVAILABLE));
        controller.onRegisterClicked ("alice@yorku.ca", "Passw0rd!", "Alice", "STUDENT", "123456789");

        panel= new BookingPanel (controller);
    }

    @SuppressWarnings ("unchecked")
    private <T> T getField (String name) throws Exception{
        Field f= BookingPanel.class.getDeclaredField (name);
        f.setAccessible (true);
        return (T) f.get (panel);
    }

    private JButton findButtonByText (Container container, String text){
        for (Component component : container.getComponents ()){
            if (component instanceof JButton button){
                if (text.equals (button.getText ())){
                    return button;
                }
            }
            if (component instanceof Container){
                JButton found= findButtonByText ((Container) component, text);
                if (found!= null){
                    return found;
                }
            }
        }
        return null;
    }

    @Test
    public void panelLoads (){
        assertNotNull (panel);
    }

    @Test
    public void searchRooms () throws Exception{
        Field startF= BookingPanel.class.getDeclaredField ("startField");
        Field endF= BookingPanel.class.getDeclaredField ("endField");
        startF.setAccessible (true);
        endF.setAccessible (true);
        ((JTextField) startF.get (panel)).setText (iso (LocalDateTime.now ().plusHours (1)));
        ((JTextField) endF.get (panel)).setText (iso (LocalDateTime.now ().plusHours (2)));

        JButton searchBtn= findButtonByText (panel, "Search Available Rooms");
        assertNotNull (searchBtn);
        searchBtn.doClick ();

        DefaultTableModel roomsModel= getField ("roomsModel");
        assertEquals (1, roomsModel.getRowCount ());
        assertEquals ("R1", roomsModel.getValueAt (0, 0));

        JLabel statusLabel= getField ("statusLabel");
        assertTrue (statusLabel.getText ().contains ("room(s) found"));
    }

    @Test
    public void noRoomsMessage () throws Exception{
        Field startF= BookingPanel.class.getDeclaredField ("startField");
        Field endF= BookingPanel.class.getDeclaredField ("endField");
        startF.setAccessible (true);
        endF.setAccessible (true);
        // Bad dates should show no available rooms.
        ((JTextField) startF.get (panel)).setText ("not-a-date");
        ((JTextField) endF.get (panel)).setText ("also-not-a-date");

        findButtonByText (panel, "Search Available Rooms").doClick ();

        JLabel statusLabel= getField ("statusLabel");
        assertTrue (statusLabel.getText ().contains ("No rooms available"));
    }

    @Test
    public void refreshBookings () throws Exception{
        controller.onBookRoomClicked ("R1", iso (LocalDateTime.now ().plusHours (1)), iso (LocalDateTime.now ().plusHours (2)),
                com.group10.scheduler.booking.PaymentMethod.CREDIT_CARD, "1234567890123456", "12/30", "123");

        panel.refreshMyBookings ();

        DefaultTableModel myBookingsModel= getField ("myBookingsModel");
        assertEquals (1, myBookingsModel.getRowCount ());
    }

    @Test
    public void checkInWithoutSelection () throws Exception{
        findButtonByText (panel, "Check In").doClick ();
        JLabel statusLabel= getField ("statusLabel");
        assertTrue (statusLabel.getText ().contains ("Select one of your bookings first"));
    }

    @Test
    public void checkInSelectedBooking () throws Exception{
        controller.onBookRoomClicked ("R1", iso (LocalDateTime.now ().minusMinutes (10)), iso (LocalDateTime.now ().plusHours (2)),
                com.group10.scheduler.booking.PaymentMethod.CREDIT_CARD, "1234567890123456", "12/30", "123");
        panel.refreshMyBookings ();

        JTable myBookingsTable= getField ("myBookingsTable");
        myBookingsTable.setRowSelectionInterval (0, 0);

        findButtonByText (panel, "Check In").doClick ();

        JLabel statusLabel= getField ("statusLabel");
        assertTrue (statusLabel.getText ().contains ("Checked in"));
    }

    @Test
    public void cancelWithoutSelection () throws Exception{
        findButtonByText (panel, "Cancel").doClick ();
        JLabel statusLabel= getField ("statusLabel");
        assertTrue (statusLabel.getText ().contains ("Select one of your bookings first"));
    }

    @Test
    public void cancelSelectedBooking () throws Exception{
        controller.onBookRoomClicked ("R1", iso (LocalDateTime.now ().plusHours (1)), iso (LocalDateTime.now ().plusHours (2)),
                com.group10.scheduler.booking.PaymentMethod.CREDIT_CARD, "1234567890123456", "12/30", "123");
        panel.refreshMyBookings ();

        JTable myBookingsTable= getField ("myBookingsTable");
        myBookingsTable.setRowSelectionInterval (0, 0);

        findButtonByText (panel, "Cancel").doClick ();

        JLabel statusLabel= getField ("statusLabel");
        assertTrue (statusLabel.getText ().contains ("cancelled"));
    }
    @Test
    public void refreshNoBookings () throws Exception{
        panel.refreshMyBookings ();
        DefaultTableModel myBookingsModel= getField ("myBookingsModel");
        assertEquals (0, myBookingsModel.getRowCount ());
    }
    @Test
    public void tablesAreNotEditable () throws Exception{
        DefaultTableModel roomsModel= getField ("roomsModel");
        DefaultTableModel myBookingsModel= getField ("myBookingsModel");
        assertFalse (roomsModel.isCellEditable (0, 0));
        assertFalse (myBookingsModel.isCellEditable (0, 0));
    }
}
