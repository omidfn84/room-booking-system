package com.group10.scheduler.panels;

import static org.junit.Assert.*;
import org.junit.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.lang.reflect.*;
import com.group10.scheduler.accounts.AccountManagement;
import com.group10.scheduler.accounts.FakeUserRepository;
import com.group10.scheduler.booking.BookingManager;
import com.group10.scheduler.booking.FakeBookingRepository;
import com.group10.scheduler.booking.FakePaymentRepository;
import com.group10.scheduler.facade.SchedulerFacade;
import com.group10.scheduler.gui.AdminController;
import com.group10.scheduler.room.FakeRoomRepository;
import com.group10.scheduler.room.RoomManager;
import com.group10.scheduler.room.RoomStatus;
/**
 * Testing AdminPanel without creating a new window.
 * Every test is done with a unique administrator id since
 * ChiefEventCoordinator is a Singleton and retains all the created
 * administrators throughoutt the tests.
 */
public class AdminPanelTest{
    private AdminController controller;
    private AdminPanel panel;
    @Before
    public void setUp (){
        RoomManager roomManager= new RoomManager (new FakeRoomRepository ());
        BookingManager bookingManager= new BookingManager (new FakeBookingRepository (), new FakePaymentRepository (), roomManager);
        AccountManagement accountManagement= new AccountManagement (new FakeUserRepository ());
        SchedulerFacade facade= new SchedulerFacade (roomManager, bookingManager, accountManagement);
        controller= new AdminController (facade);
        panel= new AdminPanel (controller);
    }
    @SuppressWarnings ("unchecked")
    // Since all the fields of AdminPanel are private, we will use reflection to be able
// to access them without altering the AdminPanel class itself.
    private <T> T getField (String name) throws Exception{
        Field field= AdminPanel.class.getDeclaredField (name);
        field.setAccessible (true);
        return (T) field.get (panel);
    }
    private void Text (String fieldName, String value) throws Exception{
        JTextField field= getField (fieldName);
        field.setText (value);
    }
    private void callMethod (String methodName) throws Exception{
        // This is only called when the button is disabled and would not otherwise
        // call the action method from the doClick () method.
        Method method= AdminPanel.class.getDeclaredMethod (methodName);
        method.setAccessible (true);
        method.invoke (panel);
    }
    // Invokes the private function that creates an admin.
    private void generateAdmin () throws Exception{
        callMethod ("handleGenerateAdmin");
    }
    // Taps the admin login button through the private field.
    private void loginAdmin () throws Exception{
        ((JButton) getField ("adminLoginBtn")).doClick ();
    }
    private void login (String adminId) throws Exception{
        controller.onGenerateAdminClicked (adminId, "Test Admin", adminId + "@yorku.ca");
        Text ("adminLoginField", adminId);
        loginAdmin ();
    }
    private void fillRoom (String roomId, String capacity, String building, String roomNumber) throws Exception{
        Text ("roomIdField", roomId);
        Text ("capacityField", capacity);
        Text ("buildingField", building);
        Text ("roomNumberField", roomNumber);
    }
    @Test
    public void panelMadeWithRoomManagementDisabled () throws Exception{
        assertFalse (((JButton) getField ("addBtn")).isEnabled ());
        assertFalse (((JButton) getField ("enableBtn")).isEnabled ());
        assertFalse (((JButton) getField ("disableBtn")).isEnabled ());
        assertFalse (((JButton) getField ("closeBtn")).isEnabled ());
    }
    @Test
    public void generateAdminLoginFieldStatus () throws Exception{
        Text ("newAdminIdField", "panel-admin-1");
        Text ("newAdminNameField", "Alice Admin");
        Text ("newAdminEmailField", "alice.admin@yorku.ca");
        generateAdmin ();
        JTextField loginField= getField ("adminLoginField");
        JLabel statusLabel= getField ("statusLabel");
        assertEquals ("panel-admin-1", loginField.getText ());
        assertTrue (statusLabel.getText ().startsWith ("Generated:"));
    }
    @Test
    public void generateAdminForError () throws Exception{
        controller.onGenerateAdminClicked ("panel-admin-2", "Bob", "bob@yorku.ca");
        Text ("newAdminIdField", "panel-admin-2");
        Text ("newAdminNameField", "Bob Two");
        Text ("newAdminEmailField", "bob2@yorku.ca");
        generateAdmin ();
        JLabel statusLabel= getField ("statusLabel");
        assertEquals ("Admin ID already exists.", statusLabel.getText ());
    }
    @Test
    public void adminLoginRoomManagement () throws Exception{
        login ("panel-admin-3");
        assertTrue (((JButton) getField ("addBtn")).isEnabled ());
        JLabel sessionLabel= getField ("adminSessionLabel");
        assertTrue (sessionLabel.getText ().contains ("panel-admin-3"));
    }
    @Test
    public void adminLoginRoomManagementDisabled () throws Exception{
        Text ("adminLoginField", "not-an-admin");
        loginAdmin ();
        assertFalse (((JButton) getField ("addBtn")).isEnabled ());
        JLabel statusLabel= getField ("statusLabel");
        assertEquals ("Unknown admin ID.", statusLabel.getText ());
    }
    @Test
    public void addRoomRejected () throws Exception{
        fillRoom ("R1", "10", "Bergeron", "100");
        // The Add Room button is disabled before the administrator logs in.
        // Since the button is disabled, the doClick () is ignored and the
        // private method is directly called to ensure that it refuses the request.
        callMethod ("handleAddRoom");
        JLabel statusLabel= getField ("statusLabel");
        assertTrue (statusLabel.getText ().contains ("Add rejected"));
    }
    @Test
    public void addRoomRefreshesTable () throws Exception{
        login ("panel-admin-4");
        fillRoom ("R2", "5", "Ross", "200");
        ((JButton) getField ("addBtn")).doClick ();
        JLabel statusLabel= getField ("statusLabel");
        DefaultTableModel roomsModel= getField ("roomsModel");
        assertTrue (statusLabel.getText ().startsWith ("Room added"));
        assertEquals (1, roomsModel.getRowCount ());
    }
    @Test
    public void addRoomForCapacity () throws Exception{
        login ("panel-admin-5");
        fillRoom ("R3", "not-a-number", "Ross", "300");
        ((JButton) getField ("addBtn")).doClick ();
        JLabel statusLabel= getField ("statusLabel");
        assertEquals ("Capacity must be a number.", statusLabel.getText ());
    }
    @Test
    public void disableButtonFirstMessage () throws Exception{
        login ("panel-admin-6");
        ((JButton) getField ("disableBtn")).doClick ();
        JLabel statusLabel= getField ("statusLabel");
        assertEquals ("Select a room first.", statusLabel.getText ());
    }
    @Test
    public void disableThenEnableTableStatus () throws Exception{
        login ("panel-admin-7");
        fillRoom ("R4", "8", "LAS", "400");
        ((JButton) getField ("addBtn")).doClick ();
        JTable roomsTable= getField ("roomsTable");
        DefaultTableModel roomsModel= getField ("roomsModel");
        roomsTable.setRowSelectionInterval (0, 0);
        ((JButton) getField ("disableBtn")).doClick ();
        assertEquals (RoomStatus.DISABLED, roomsModel.getValueAt (0, 4));
        roomsTable.setRowSelectionInterval (0, 0);
        ((JButton) getField ("enableBtn")).doClick ();
        assertEquals (RoomStatus.AVAILABLE, roomsModel.getValueAt (0, 4));
    }
    @Test
    public void maintenance () throws Exception{
        login ("panel-admin-8");
        fillRoom ("R5", "12", "LAS", "500");
        ((JButton) getField ("addBtn")).doClick ();
        JTable roomsTable= getField ("roomsTable");
        DefaultTableModel roomsModel= getField ("roomsModel");
        roomsTable.setRowSelectionInterval (0, 0);
        ((JButton) getField ("closeBtn")).doClick ();
        assertEquals (RoomStatus.MAINTENANCE, roomsModel.getValueAt (0, 4));
    }
    @Test
    public void refreshButton () throws Exception{
        login ("panel-admin-9");
        fillRoom ("R6", "15", "Vari", "600");
        ((JButton) getField ("addBtn")).doClick ();
        DefaultTableModel roomsModel= getField ("roomsModel");
        roomsModel.setRowCount (0);
        assertEquals (0, roomsModel.getRowCount ());
        panel.refreshRooms ();
        assertEquals (1, roomsModel.getRowCount ());
        assertEquals ("R6", roomsModel.getValueAt (0, 0));
    }
}
