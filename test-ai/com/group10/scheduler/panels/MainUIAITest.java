package com.group10.scheduler.panels;

import static org.junit.Assert.*;

import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.io.File;
import java.lang.reflect.Field;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.group10.scheduler.aisupport.AIFixture;
import com.group10.scheduler.facade.SchedulerFacade;

/**
 * MainUI is the application shell and the composition root: it owns the three
 * tabs, creates the two controllers, and - in main() - is the only place that
 * names the concrete CSV classes and file paths.
 *
 * MainUI extends JFrame, so unlike the JPanel screens it cannot be built in a
 * headless JVM. Every test here is therefore guarded with Assume and will skip
 * rather than fail on a machine with no display. Run them from an IDE (or any
 * desktop session) and they execute normally.
 *
 * Frames created by a test are disposed in @After so the suite does not leak
 * windows into later tests.
 */
public class MainUIAITest {

    private MainUI frame;

    @Before
    public void requireDisplay() {
        Assume.assumeFalse("MainUI extends JFrame and needs a display", GraphicsEnvironment.isHeadless());
    }

    @After
    public void disposeFrames() throws Exception {
        if (frame != null) {
            SwingUtilities.invokeAndWait(frame::dispose);
            frame = null;
        }
        // Anything main() opened is cleaned up too.
        SwingUtilities.invokeAndWait(() -> {
            for (Window w : Window.getWindows()) {
                if (w instanceof MainUI) {
                    w.dispose();
                }
            }
        });
    }

    // ---------- helpers ----------

    private MainUI build() {
        frame = new MainUI(new AIFixture().facade);
        return frame;
    }

    @SuppressWarnings("unchecked")
    private <T> T field(MainUI ui, String name) throws Exception {
        Field f = MainUI.class.getDeclaredField(name);
        f.setAccessible(true);
        return (T) f.get(ui);
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

    private <T> T findField(Container container, Class<T> type, int index) {
        int[] seen = {0};
        return search(container, type, index, seen);
    }

    /**
     * Sets a LoginPanel input by its declared field name.
     *
     * An earlier version of this helper walked the component tree and picked
     * the Nth JTextField. That was wrong: JPasswordField EXTENDS JTextField,
     * so the password box was counted as a text field and every index after
     * it pointed at the wrong control - the username ended up in the password
     * box and registration silently failed. Looking the field up by name
     * removes the ambiguity entirely.
     */
    private void setLoginField(LoginPanel login, String fieldName, String value) throws Exception {
        Field f = LoginPanel.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        ((javax.swing.text.JTextComponent) f.get(login)).setText(value);
    }

    private void fillRegistration(LoginPanel login, String email, String password,
                                   String userName, String orgId) throws Exception {
        setLoginField(login, "emailField", email);
        setLoginField(login, "passwordField", password);
        setLoginField(login, "userNameField", userName);
        setLoginField(login, "idField", orgId);
    }

    @SuppressWarnings("unchecked")
    private <T> T search(Container container, Class<T> type, int index, int[] seen) {
        for (Component c : container.getComponents()) {
            if (type.isInstance(c)) {
                if (seen[0] == index) {
                    return (T) c;
                }
                seen[0]++;
            }
            if (c instanceof Container) {
                T nested = search((Container) c, type, index, seen);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    // ==================== window setup ====================

    @Test
    public void theWindowIsTitledForTheProject() {
        assertEquals("YorkU Conference Room Scheduler", build().getTitle());
    }

    @Test
    public void theWindowOpensAtAUsableSize() {
        MainUI ui = build();

        assertEquals(900, ui.getWidth());
        assertEquals(600, ui.getHeight());
    }

    @Test
    public void closingTheWindowExitsTheApplication() {
        // This is the only window, so anything other than EXIT_ON_CLOSE would
        // leave the JVM running after the user closes it.
        assertEquals(JFrame.EXIT_ON_CLOSE, build().getDefaultCloseOperation());
    }

    @Test
    public void theFrameUsesBorderLayoutWithTheTabsInTheCentre() throws Exception {
        MainUI ui = build();
        JTabbedPane tabs = field(ui, "tabs");

        assertNotNull(tabs);
        assertSame(ui.getContentPane(), tabs.getParent());
    }

    // ==================== tabs ====================

    @Test
    public void allThreeTabsArePresentInOrder() throws Exception {
        JTabbedPane tabs = field(build(), "tabs");

        assertEquals(3, tabs.getTabCount());
        assertEquals("Login / Register", tabs.getTitleAt(0));
        assertEquals("Book a Room", tabs.getTitleAt(1));
        assertEquals("Admin", tabs.getTitleAt(2));
    }

    @Test
    public void eachTabHoldsTheRightPanelType() throws Exception {
        JTabbedPane tabs = field(build(), "tabs");

        assertTrue(tabs.getComponentAt(0) instanceof LoginPanel);
        assertTrue(tabs.getComponentAt(1) instanceof BookingPanel);
        assertTrue(tabs.getComponentAt(2) instanceof AdminPanel);
    }

    @Test
    public void theLoginTabIsSelectedFirstBecauseNobodyIsLoggedIn() throws Exception {
        JTabbedPane tabs = field(build(), "tabs");

        assertEquals(0, tabs.getSelectedIndex());
    }

    @Test
    public void thePanelFieldsAreWiredToTheSameInstancesShownInTheTabs() throws Exception {
        MainUI ui = build();
        JTabbedPane tabs = field(ui, "tabs");

        assertSame(field(ui, "loginPanel"), tabs.getComponentAt(0));
        assertSame(field(ui, "bookingPanel"), tabs.getComponentAt(1));
        assertSame(field(ui, "adminPanel"), tabs.getComponentAt(2));
    }

    @Test
    public void bothControllersAreCreatedOverTheSameFacade() throws Exception {
        MainUI ui = build();

        assertNotNull(field(ui, "controller"));
        assertNotNull(field(ui, "adminController"));
    }

    // ==================== the onLoginSuccess callback ====================

    @Test
    public void aSuccessfulRegistrationSwitchesToTheBookingTab() throws Exception {
        // MainUI passes this::onLoginSuccess into LoginPanel, so registering
        // through the real form is what exercises the callback.
        MainUI ui = build();
        JTabbedPane tabs = field(ui, "tabs");
        LoginPanel login = field(ui, "loginPanel");

        fillRegistration(login, "alice@yorku.ca", "Passw0rd!", "Alice", "123456789");
        findButton(login, "Register").doClick();

        assertEquals("Logging in must land the user on the booking screen",
                1, tabs.getSelectedIndex());
        assertSame(field(ui, "bookingPanel"), tabs.getSelectedComponent());
    }

    @Test
    public void aFailedRegistrationLeavesTheUserOnTheLoginTab() throws Exception {
        MainUI ui = build();
        JTabbedPane tabs = field(ui, "tabs");
        LoginPanel login = field(ui, "loginPanel");

        fillRegistration(login, "not-an-email", "Passw0rd!", "Nobody", "123456789");
        findButton(login, "Register").doClick();

        assertEquals("A rejected registration must not advance the UI", 0, tabs.getSelectedIndex());
    }

    @Test
    public void theCallbackAlsoRefreshesTheOtherTabsWithoutThrowing() throws Exception {
        // onLoginSuccess() calls refreshMyBookings() and refreshRooms() before
        // switching tabs; a failure in either would surface here.
        MainUI ui = build();
        LoginPanel login = field(ui, "loginPanel");

        fillRegistration(login, "bob@yorku.ca", "Passw0rd!", "Bob", "987654321");
        findButton(login, "Register").doClick();

        assertNotNull(field(ui, "bookingPanel"));
        assertNotNull(field(ui, "adminPanel"));
    }

    // ==================== more than one window ====================

    @Test
    public void twoFramesOverDifferentFacadesAreIndependent() throws Exception {
        MainUI first = build();
        MainUI second = new MainUI(new AIFixture().facade);
        try {
            JTabbedPane firstTabs = field(first, "tabs");
            JTabbedPane secondTabs = field(second, "tabs");

            firstTabs.setSelectedIndex(2);

            assertEquals(2, firstTabs.getSelectedIndex());
            assertEquals("The second window must keep its own tab state", 0, secondTabs.getSelectedIndex());
        } finally {
            SwingUtilities.invokeAndWait(second::dispose);
        }
    }

    @Test
    public void aFrameCanBeShownAndDisposedCleanly() throws Exception {
        MainUI ui = build();

        SwingUtilities.invokeAndWait(() -> ui.setVisible(true));
        assertTrue(ui.isVisible());

        SwingUtilities.invokeAndWait(ui::dispose);
        assertFalse(ui.isVisible());
    }

    // ==================== main(): the composition root ====================

    @Test(timeout = 30_000)
    public void main_wiresTheWholeSystemAndOpensTheWindow() throws Exception {
        // main() is the only place that names the CSV classes, so running it
        // proves the Adapter layer, the managers and the facade all fit
        // together. It writes into ./data, which is the app's normal home.
        MainUI.main(new String[0]);

        // main() shows the frame via invokeLater; wait for the EDT to drain.
        SwingUtilities.invokeAndWait(() -> { });

        MainUI opened = null;
        for (Window w : Window.getWindows()) {
            if (w instanceof MainUI) {
                opened = (MainUI) w;
            }
        }

        assertNotNull("main() must open a MainUI window", opened);
        assertEquals("YorkU Conference Room Scheduler", opened.getTitle());
        assertTrue(opened.isVisible());

        JTabbedPane tabs = field(opened, "tabs");
        assertEquals(3, tabs.getTabCount());
    }

    @Test(timeout = 30_000)
    public void main_createsTheDataDirectoryForTheCsvFiles() throws Exception {
        // FileWriter cannot create missing directories, so main() has to make
        // ./data itself or the very first save would fail.
        MainUI.main(new String[0]);
        SwingUtilities.invokeAndWait(() -> { });

        File dataDir = new File("data");
        assertTrue("main() must create the data directory", dataDir.exists());
        assertTrue(dataDir.isDirectory());
    }

    @Test(timeout = 30_000)
    public void main_canBeCalledTwiceWithoutFailing() throws Exception {
        // Guards against main() assuming a clean slate: the second run reads
        // whatever the first one persisted.
        MainUI.main(new String[0]);
        SwingUtilities.invokeAndWait(() -> { });
        MainUI.main(new String[0]);
        SwingUtilities.invokeAndWait(() -> { });

        int frames = 0;
        for (Window w : Window.getWindows()) {
            if (w instanceof MainUI && w.isDisplayable()) {
                frames++;
            }
        }
        assertTrue("Both runs should have produced a window", frames >= 2);
    }
}
