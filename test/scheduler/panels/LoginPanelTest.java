package scheduler.panels;

import static org.junit.Assert.*;
import org.junit.*;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;

import scheduler.accounts.AccountManagement;
import scheduler.accounts.FakeUserRepository;
import scheduler.booking.BookingManager;
import scheduler.booking.FakeBookingRepository;
import scheduler.booking.FakePaymentRepository;
import scheduler.facade.SchedulerFacade;
import scheduler.gui.GUIController;
import scheduler.room.FakeRoomRepository;
import scheduler.room.RoomManager;

/**
 * Login and Registration buttons testing without a window being created.
 */
public class LoginPanelTest{

    private GUIController controller;
    private LoginPanel panel;
    private boolean loginSuccessCalled;

    @Before
    public void setUp (){
        RoomManager roomManager= new RoomManager (new FakeRoomRepository ());
        BookingManager bookingManager= new BookingManager (new FakeBookingRepository (), new FakePaymentRepository (), roomManager);
        AccountManagement accountManagement= new AccountManagement (new FakeUserRepository ());
        SchedulerFacade facade= new SchedulerFacade (roomManager, bookingManager, accountManagement);
        controller= new GUIController (facade);

        loginSuccessCalled= false;
        panel= new LoginPanel (controller, () -> loginSuccessCalled= true);
    }

    private void setFieldText (String fieldName, String value) throws Exception{
        Field field= LoginPanel.class.getDeclaredField (fieldName);
        field.setAccessible (true);
        JTextField textField= (JTextField) field.get (panel);
        textField.setText (value);
    }

    private void setAccountType (String value) throws Exception{
        Field f= LoginPanel.class.getDeclaredField ("accountTypeBox");
        f.setAccessible (true);
        @SuppressWarnings ("unchecked")
        JComboBox <String> box= (JComboBox <String>) f.get (panel);
        box.setSelectedItem (value);
    }

    private String getStatusText () throws Exception{
        Field f= LoginPanel.class.getDeclaredField ("statusLabel");
        f.setAccessible (true);
        JLabel label= (JLabel) f.get (panel);
        return label.getText ();
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
    public void validLogin () throws Exception{
        controller.onRegisterClicked ("alice@yorku.ca", "Passw0rd!", "Alice", "STUDENT", "123456789");
        setFieldText ("emailField", "alice@yorku.ca");
        setFieldText ("passwordField", "Passw0rd!");

        JButton loginBtn= findButtonByText (panel, "Login");
        assertNotNull (loginBtn);
        loginBtn.doClick ();

        assertTrue (getStatusText ().contains ("Logged in as"));
        assertTrue (loginSuccessCalled);
    }

    @Test
    public void invalidLogin () throws Exception{
        setFieldText ("emailField", "nobody@yorku.ca");
        setFieldText ("passwordField", "WrongPass1!");

        JButton loginBtn= findButtonByText (panel, "Login");
        loginBtn.doClick ();

        assertTrue (getStatusText ().contains ("Login failed"));
        assertFalse (loginSuccessCalled);
    }

    @Test
    public void validRegistration () throws Exception{
        setFieldText ("emailField", "bob@yorku.ca");
        setFieldText ("passwordField", "Passw0rd!");
        setFieldText ("userNameField", "Bob");
        setAccountType ("STAFF");
        setFieldText ("idField", "123456789");

        JButton registerBtn= findButtonByText (panel, "Register");
        registerBtn.doClick ();

        assertTrue (getStatusText ().contains ("Account created for Bob"));
        assertTrue (loginSuccessCalled);
    }

    @Test
    public void invalidRegistration () throws Exception{
        setFieldText ("emailField", "not-an-email");
        setFieldText ("passwordField", "weak");
        setFieldText ("userNameField", "Carl");
        setAccountType ("STUDENT");
        setFieldText ("idField", "123");

        JButton registerBtn= findButtonByText (panel, "Register");
        registerBtn.doClick ();

        assertTrue (getStatusText ().startsWith ("Error:"));
        assertFalse (loginSuccessCalled);
    }
    @Test
    public void initialStatusEmpty () throws Exception{
        assertEquals (" ", getStatusText ());
        assertFalse (loginSuccessCalled);
    }
    @Test
    public void loginEmailWithSpaces () throws Exception{
        controller.onRegisterClicked ("loginspace@yorku.ca", "Passw0rd!", "John", "STUDENT", "123456789");
        setFieldText ("emailField", "  loginspace@yorku.ca  ");
        setFieldText ("passwordField", "Passw0rd!");
        JButton loginBtn= findButtonByText (panel, "Login");
        loginBtn.doClick ();
        assertTrue (getStatusText ().contains ("Logged in as John"));
        assertTrue (loginSuccessCalled);
    }
    @Test
    public void registerWithNormalEmail () throws Exception{
        setFieldText ("emailField", "partner@gmail.com");
        setFieldText ("passwordField", "Passw0rd!");
        setFieldText ("userNameField", "Partner");
        setAccountType ("PARTNER");
        setFieldText ("idField", "123456789");
        JButton registerBtn= findButtonByText (panel, "Register");
        registerBtn.doClick ();
        assertTrue (getStatusText ().contains ("Account created for Partner"));
        assertTrue (loginSuccessCalled);
    }
    @Test
    public void universityNeedsYorkEmail () throws Exception{
        setFieldText ("emailField", "staff@gmail.com");
        setFieldText ("passwordField", "Passw0rd!");
        setFieldText ("userNameField", "Staff");
        setAccountType ("STAFF");
        setFieldText ("idField", "123456789");
        JButton registerBtn= findButtonByText (panel, "Register");
        registerBtn.doClick ();
        assertTrue (getStatusText ().contains ("University accounts must use a university email"));
        assertFalse (loginSuccessCalled);
    }
    @Test
    public void duplicateEmailError () throws Exception{
        controller.onRegisterClicked ("duplicate@yorku.ca", "Passw0rd!", "First", "STUDENT", "123456789");
        setFieldText ("emailField", "duplicate@yorku.ca");
        setFieldText ("passwordField", "Passw0rd!");
        setFieldText ("userNameField", "Second");
        setAccountType ("STUDENT");
        setFieldText ("idField", "987654321");
        JButton registerBtn= findButtonByText (panel, "Register");
        registerBtn.doClick ();
        assertTrue (getStatusText ().contains ("already registered"));
        assertFalse (loginSuccessCalled);
    }
}

