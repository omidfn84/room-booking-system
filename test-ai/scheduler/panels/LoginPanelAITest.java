package scheduler.panels;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.junit.Before;
import org.junit.Test;

import scheduler.aisupport.AIFixture;
import scheduler.gui.GUIController;

/**
 * LoginPanel is the Req1 surface: register an account with a unique valid
 * email and a strong password, then log in.
 *
 * Two panel-specific responsibilities are tested here that the controller
 * tests cannot cover:
 *
 *  - the status label, which is the only feedback a user gets. Its COLOUR
 *    carries meaning too (red for failure, green for success), so both are
 *    asserted.
 *  - the onLoginSuccess callback, which MainUI uses to refresh the other tabs
 *    and switch to them. Firing it after a failed login would drop the user
 *    into a booking screen with no session.
 */
public class LoginPanelAITest {

    private AIFixture fx;
    private GUIController controller;
    private LoginPanel panel;
    private AtomicInteger successCallbacks;

    private static final Color GREEN = new Color(0, 128, 0);

    @Before
    public void setUp() {
        fx = new AIFixture();
        controller = new GUIController(fx.facade);
        successCallbacks = new AtomicInteger();
        panel = new LoginPanel(controller, successCallbacks::incrementAndGet);
    }

    // ---------- helpers ----------

    @SuppressWarnings("unchecked")
    private <T> T field(String name) throws Exception {
        Field f = LoginPanel.class.getDeclaredField(name);
        f.setAccessible(true);
        return (T) f.get(panel);
    }

    private JButton button(String text) {
        JButton found = findButton(panel, text);
        assertNotNull("No button labelled '" + text + "'", found);
        return found;
    }

    private JButton findButton(Container container, String text) {
        for (Component c : container.getComponents()) {
            if (c instanceof JButton && text.equals(((JButton) c).getText())) {
                return (JButton) c;
            }
            if (c instanceof Container) {
                JButton nested = findButton((Container) c, text);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private String status() throws Exception {
        return this.<JLabel>field("statusLabel").getText();
    }

    private Color statusColour() throws Exception {
        return this.<JLabel>field("statusLabel").getForeground();
    }

    private void fillRegistrationForm(String email, String password, String userName,
                                       String accountType, String orgId) throws Exception {
        this.<JTextField>field("emailField").setText(email);
        this.<JPasswordField>field("passwordField").setText(password);
        this.<JTextField>field("userNameField").setText(userName);
        this.<JComboBox<String>>field("accountTypeBox").setSelectedItem(accountType);
        this.<JTextField>field("idField").setText(orgId);
    }

    private void registerAlice() throws Exception {
        fillRegistrationForm("alice@yorku.ca", "Passw0rd!", "Alice", "STUDENT", "123456789");
        button("Register").doClick();
    }

    // ==================== construction ====================

    @Test
    public void panelBuildsWithBothActionsAndAllFiveInputs() throws Exception {
        assertNotNull(findButton(panel, "Login"));
        assertNotNull(findButton(panel, "Register"));
        assertNotNull(field("emailField"));
        assertNotNull(field("passwordField"));
        assertNotNull(field("userNameField"));
        assertNotNull(field("accountTypeBox"));
        assertNotNull(field("idField"));
    }

    @Test
    public void theAccountTypeBoxOffersExactlyTheFourReq1Types() throws Exception {
        JComboBox<String> box = field("accountTypeBox");

        assertEquals(4, box.getItemCount());
        assertEquals("STUDENT", box.getItemAt(0));
        assertEquals("FACULTY", box.getItemAt(1));
        assertEquals("STAFF", box.getItemAt(2));
        assertEquals("PARTNER", box.getItemAt(3));
    }

    @Test
    public void theStatusLabelStartsBlankAndNoCallbackHasFired() throws Exception {
        assertEquals(" ", status());
        assertEquals(0, successCallbacks.get());
    }

    // ==================== Req1: registration ====================

    @Test
    public void register_createsTheAccountAndReportsItInGreen() throws Exception {
        registerAlice();

        assertTrue(status().startsWith("Account created for Alice"));
        assertEquals(GREEN, statusColour());
        assertNotNull(controller.getCurrentUser());
    }

    @Test
    public void register_firesTheSuccessCallbackSoMainUiCanSwitchTabs() throws Exception {
        registerAlice();

        assertEquals(1, successCallbacks.get());
    }

    @Test
    public void register_reportsADuplicateEmailInRedWithoutFiringTheCallback() throws Exception {
        registerAlice();
        int callbacksAfterFirst = successCallbacks.get();

        fillRegistrationForm("alice@yorku.ca", "Passw0rd!", "Alice Two", "STUDENT", "123456789");
        button("Register").doClick();

        assertTrue(status().startsWith("Error:"));
        assertEquals(Color.RED, statusColour());
        assertEquals("A rejected registration must not advance the UI",
                callbacksAfterFirst, successCallbacks.get());
    }

    @Test
    public void register_reportsAWeakPasswordAsAnError() throws Exception {
        // Req1 requires uppercase, lowercase, numbers and symbols.
        fillRegistrationForm("bob@yorku.ca", "password", "Bob", "STUDENT", "123456789");

        button("Register").doClick();

        assertTrue(status().startsWith("Error:"));
        assertEquals(Color.RED, statusColour());
        assertEquals(0, successCallbacks.get());
    }

    @Test
    public void register_reportsAMalformedEmailAsAnError() throws Exception {
        fillRegistrationForm("not-an-email", "Passw0rd!", "Bob", "STUDENT", "123456789");

        button("Register").doClick();

        assertTrue(status().startsWith("Error:"));
        assertEquals(0, successCallbacks.get());
    }

    @Test
    public void register_reportsABadOrganisationIdAsAnError() throws Exception {
        // The panel passes the raw text through; AccountManagement owns the
        // 9-digit rule and its message is what the label shows.
        fillRegistrationForm("bob@yorku.ca", "Passw0rd!", "Bob", "STUDENT", "abc");

        button("Register").doClick();

        assertTrue(status().startsWith("Error:"));
        assertEquals(0, successCallbacks.get());
    }

    @Test
    public void register_reportsANonUniversityEmailForAUniversityAccountType() throws Exception {
        // Req1: "University accounts require verification."
        fillRegistrationForm("bob@gmail.com", "Passw0rd!", "Bob", "STUDENT", "123456789");

        button("Register").doClick();

        assertTrue(status().startsWith("Error:"));
        assertEquals(0, successCallbacks.get());
    }

    @Test
    public void register_acceptsAPartnerWithANonUniversityEmail() throws Exception {
        // PARTNER is the one account type that does not require verification.
        fillRegistrationForm("pat@company.com", "Passw0rd!", "Pat", "PARTNER", "987654321");

        button("Register").doClick();

        assertTrue(status().startsWith("Account created for Pat"));
        assertEquals(GREEN, statusColour());
    }

    @Test
    public void register_trimsWhitespaceAroundTheTypedFields() throws Exception {
        fillRegistrationForm("   alice@yorku.ca  ", "Passw0rd!", "  Alice  ", "STUDENT", "  123456789  ");

        button("Register").doClick();

        assertTrue("Stray spaces must not defeat validation", status().startsWith("Account created"));
        assertEquals("alice@yorku.ca", controller.getCurrentUser().getEmail());
    }

    // ==================== Req1: login ====================

    @Test
    public void login_succeedsWithTheCorrectCredentialsAndReportsInGreen() throws Exception {
        registerAlice();

        this.<JTextField>field("emailField").setText("alice@yorku.ca");
        this.<JPasswordField>field("passwordField").setText("Passw0rd!");
        button("Login").doClick();

        assertEquals("Logged in as Alice", status());
        assertEquals(GREEN, statusColour());
    }

    @Test
    public void login_firesTheSuccessCallback() throws Exception {
        registerAlice();
        int before = successCallbacks.get();

        this.<JTextField>field("emailField").setText("alice@yorku.ca");
        this.<JPasswordField>field("passwordField").setText("Passw0rd!");
        button("Login").doClick();

        assertEquals(before + 1, successCallbacks.get());
    }

    @Test
    public void login_reportsAWrongPasswordInRedWithoutFiringTheCallback() throws Exception {
        registerAlice();
        int before = successCallbacks.get();

        this.<JTextField>field("emailField").setText("alice@yorku.ca");
        this.<JPasswordField>field("passwordField").setText("WrongPass1!");
        button("Login").doClick();

        assertEquals("Login failed: invalid email or password.", status());
        assertEquals(Color.RED, statusColour());
        assertEquals("A failed login must never advance the UI", before, successCallbacks.get());
    }

    @Test
    public void login_reportsAnUnknownEmailWithTheSameMessage() throws Exception {
        // Deliberately identical to the wrong-password message so the form
        // does not reveal which accounts exist.
        this.<JTextField>field("emailField").setText("ghost@yorku.ca");
        this.<JPasswordField>field("passwordField").setText("Passw0rd!");

        button("Login").doClick();

        assertEquals("Login failed: invalid email or password.", status());
        assertEquals(0, successCallbacks.get());
    }

    @Test
    public void login_withEmptyFieldsIsReportedRatherThanCrashing() throws Exception {
        this.<JTextField>field("emailField").setText("");
        this.<JPasswordField>field("passwordField").setText("");

        button("Login").doClick();

        assertEquals("Login failed: invalid email or password.", status());
        assertEquals(Color.RED, statusColour());
    }

    @Test
    public void login_trimsTheEmailSoTrailingSpacesStillAuthenticate() throws Exception {
        registerAlice();

        this.<JTextField>field("emailField").setText("  alice@yorku.ca  ");
        this.<JPasswordField>field("passwordField").setText("Passw0rd!");
        button("Login").doClick();

        assertEquals("Logged in as Alice", status());
    }

    @Test
    public void aFailedLoginAfterASuccessfulOneShowsTheFailureAndClearsTheSession() throws Exception {
        registerAlice();
        this.<JTextField>field("emailField").setText("alice@yorku.ca");
        this.<JPasswordField>field("passwordField").setText("Passw0rd!");
        button("Login").doClick();
        assertNotNull(controller.getCurrentUser());

        this.<JPasswordField>field("passwordField").setText("WrongPass1!");
        button("Login").doClick();

        assertEquals("Login failed: invalid email or password.", status());
        assertNull("The stale session must not survive a failed login", controller.getCurrentUser());
    }

    @Test
    public void switchingAccountTypeChangesWhichSubclassIsCreated() throws Exception {
        fillRegistrationForm("fay@yorku.ca", "Passw0rd!", "Fay", "FACULTY", "111111111");
        button("Register").doClick();

        assertEquals("FACULTY must produce the $30 rate",
                30.0, controller.getCurrentUser().getHourlyRate(), 0.001);
    }
}
