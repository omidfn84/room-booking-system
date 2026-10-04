package com.group10.scheduler.panels;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.group10.scheduler.aisupport.AIDialogs;
import com.group10.scheduler.aisupport.AIFixture;
import com.group10.scheduler.booking.Booking;
import com.group10.scheduler.booking.BookingStatus;
import com.group10.scheduler.booking.PaymentMethod;
import com.group10.scheduler.gui.GUIController;

/**
 * BookingPanel is the busiest screen: search, book, check in, edit, cancel,
 * extend, and pay (Req3, Req4, Req5, Req8, Req9, Req10).
 *
 * Four of its handlers open a modal JOptionPane, which is why the manually
 * written suite explicitly left them uncovered. Here they ARE tested, using
 * AIDialogs to answer the dialog from a background thread the way a user
 * would. Those tests need a real (or virtual) display, so they are guarded
 * with Assume: on a headless machine they are skipped rather than failed,
 * and everything not involving a dialog still runs everywhere.
 *
 * Every dialog-driven test carries a timeout so a dialog that never appears
 * fails the test instead of hanging the build.
 */
public class BookingPanelAITest {

    private AIFixture fx;
    private GUIController controller;
    private BookingPanel panel;

    private static final Color GREEN = new Color(0, 128, 0);
    private static final String[] CARD = {"4111111111111111", "12/27", "123"};

    @Before
    public void setUp() {
        fx = new AIFixture();
        controller = new GUIController(fx.facade);
        fx.addRoom("R1");
        controller.onRegisterClicked("alice@yorku.ca", "Passw0rd!", "Alice", "STUDENT", "123456789");
        panel = new BookingPanel(controller);
    }

    @After
    public void tearDown() {
        AIDialogs.disarmAll();
    }

    /** Dialog-driven tests need a display; skip cleanly when there is none. */
    private void requireDisplay() {
        Assume.assumeFalse("Needs a display to drive modal dialogs", GraphicsEnvironment.isHeadless());
    }

    // ---------- helpers ----------

    @SuppressWarnings("unchecked")
    private <T> T field(String name) throws Exception {
        Field f = BookingPanel.class.getDeclaredField(name);
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

    private void setWindow(String start, String end) throws Exception {
        this.<JTextField>field("startField").setText(start);
        this.<JTextField>field("endField").setText(end);
    }

    private void search() {
        button("Search Available Rooms").doClick();
    }

    /** Creates a booking through the controller and refreshes the table. */
    private Booking seedBooking(String start, String end) {
        Booking b = controller.onBookRoomClicked("R1", start, end, PaymentMethod.CREDIT_CARD, CARD);
        panel.refreshMyBookings();
        return b;
    }

    private void selectFirstBooking() throws Exception {
        this.<JTable>field("myBookingsTable").setRowSelectionInterval(0, 0);
    }

    // ==================== construction ====================

    @Test
    public void panelBuildsWithEveryActionTheUserNeeds() {
        assertNotNull(findButton(panel, "Search Available Rooms"));
        assertNotNull(findButton(panel, "Book Selected Room"));
        assertNotNull(findButton(panel, "Refresh"));
        assertNotNull(findButton(panel, "Check In"));
        assertNotNull(findButton(panel, "Edit..."));
        assertNotNull(findButton(panel, "Cancel"));
        assertNotNull(findButton(panel, "Extend To..."));
        assertNotNull(findButton(panel, "Pay Remaining Balance"));
    }

    @Test
    public void theTimeFieldsArePrefilledWithASensibleDefaultWindow() throws Exception {
        // Defaults are now and now+1h, so a demo can search without typing.
        String start = this.<JTextField>field("startField").getText();
        String end = this.<JTextField>field("endField").getText();

        assertFalse(start.isBlank());
        assertFalse(end.isBlank());
        assertTrue("The default window must be forward in time", start.compareTo(end) < 0);
    }

    @Test
    public void thePaymentMethodBoxOffersAllThreeReq10Methods() throws Exception {
        JComboBox<PaymentMethod> box = field("methodBox");

        assertEquals(3, box.getItemCount());
        assertEquals(PaymentMethod.CREDIT_CARD, box.getItemAt(0));
        assertEquals(PaymentMethod.DEBIT_CARD, box.getItemAt(1));
        assertEquals(PaymentMethod.INSTITUTIONAL_BILLING, box.getItemAt(2));
    }

    @Test
    public void bothTablesStartEmptyAndReadOnly() throws Exception {
        DefaultTableModel rooms = field("roomsModel");
        DefaultTableModel bookings = field("myBookingsModel");

        assertEquals(0, rooms.getRowCount());
        assertEquals(4, rooms.getColumnCount());
        assertFalse(rooms.isCellEditable(0, 0));

        assertEquals(0, bookings.getRowCount());
        assertEquals(5, bookings.getColumnCount());
        assertFalse(bookings.isCellEditable(0, 0));
    }

    // ==================== Req3: searching ====================

    @Test
    public void search_listsTheAvailableRoomsAndReportsTheCount() throws Exception {
        setWindow(AIFixture.futureStart(), AIFixture.futureEnd());

        search();

        assertEquals("1 room(s) found.", status());
        assertEquals(GREEN, statusColour());
        DefaultTableModel model = field("roomsModel");
        assertEquals(1, model.getRowCount());
        assertEquals("R1", model.getValueAt(0, 0));
    }

    @Test
    public void search_reportsWhenNothingIsAvailable() throws Exception {
        fx.roomManager.disableRoom("R1");
        setWindow(AIFixture.futureStart(), AIFixture.futureEnd());

        search();

        assertEquals("No rooms available for that window.", status());
        assertEquals(0, this.<DefaultTableModel>field("roomsModel").getRowCount());
    }

    @Test
    public void search_withUnparseableTimesIsReportedRatherThanCrashing() throws Exception {
        setWindow("garbage", AIFixture.futureEnd());

        search();

        assertEquals("No rooms available for that window.", status());
    }

    @Test
    public void search_replacesPreviousResultsInsteadOfAppending() throws Exception {
        setWindow(AIFixture.futureStart(), AIFixture.futureEnd());

        search();
        search();
        search();

        assertEquals(1, this.<DefaultTableModel>field("roomsModel").getRowCount());
    }

    @Test
    public void search_hidesARoomOnceItIsBookedForThatWindow() throws Exception {
        String start = AIFixture.futureStart();
        String end = AIFixture.futureEnd();
        setWindow(start, end);
        search();
        assertEquals(1, this.<DefaultTableModel>field("roomsModel").getRowCount());

        seedBooking(start, end);
        search();

        assertEquals(0, this.<DefaultTableModel>field("roomsModel").getRowCount());
        assertEquals("No rooms available for that window.", status());
    }

    // ==================== My Bookings table ====================

    @Test
    public void refreshMyBookings_showsTheSessionUsersBookings() throws Exception {
        Booking booking = seedBooking(AIFixture.futureStart(), AIFixture.futureEnd());

        DefaultTableModel model = field("myBookingsModel");
        assertEquals(1, model.getRowCount());
        assertEquals(booking.getBookingId(), model.getValueAt(0, 0));
        assertEquals("R1", model.getValueAt(0, 1));
        assertEquals(BookingStatus.CONFIRMED, model.getValueAt(0, 4));
    }

    @Test
    public void refreshMyBookings_isEmptyBeforeAnythingIsBooked() throws Exception {
        panel.refreshMyBookings();

        assertEquals(0, this.<DefaultTableModel>field("myBookingsModel").getRowCount());
    }

    @Test
    public void refreshMyBookings_doesNotDuplicateRowsWhenCalledRepeatedly() throws Exception {
        seedBooking(AIFixture.futureStart(), AIFixture.futureEnd());

        panel.refreshMyBookings();
        panel.refreshMyBookings();

        assertEquals(1, this.<DefaultTableModel>field("myBookingsModel").getRowCount());
    }

    @Test
    public void theRefreshButtonRebuildsTheBookingsTable() throws Exception {
        controller.onBookRoomClicked("R1", AIFixture.futureStart(), AIFixture.futureEnd(),
                PaymentMethod.CREDIT_CARD, CARD);

        button("Refresh").doClick();

        assertEquals(1, this.<DefaultTableModel>field("myBookingsModel").getRowCount());
    }

    // ==================== guard clauses (no dialog involved) ====================

    @Test
    public void bookingActionsReportThatNothingIsSelected() throws Exception {
        seedBooking(AIFixture.futureStart(), AIFixture.futureEnd());
        this.<JTable>field("myBookingsTable").clearSelection();

        button("Check In").doClick();

        assertEquals("Select one of your bookings first.", status());
        assertEquals(Color.RED, statusColour());
    }

    @Test
    public void bookRejectsAClickWhenNoRoomRowIsSelected() throws Exception {
        setWindow(AIFixture.futureStart(), AIFixture.futureEnd());
        search();
        this.<JTable>field("roomsTable").clearSelection();

        button("Book Selected Room").doClick();

        assertEquals("Select a room first.", status());
        assertEquals(Color.RED, statusColour());
    }

    // ==================== Req4 + Req5: check in ====================

    @Test
    public void checkIn_succeedsInsideTheWindowAndUpdatesTheTable() throws Exception {
        seedBooking(AIFixture.startedRecently(), AIFixture.hoursFromNow(1));
        selectFirstBooking();

        button("Check In").doClick();

        assertTrue(status().startsWith("Checked in."));
        assertEquals(GREEN, statusColour());
        assertEquals(BookingStatus.CHECKED_IN, this.<DefaultTableModel>field("myBookingsModel").getValueAt(0, 4));
    }

    @Test
    public void checkIn_isReportedAsRejectedBeforeTheBookingStarts() throws Exception {
        seedBooking(AIFixture.futureStart(), AIFixture.futureEnd());
        selectFirstBooking();

        button("Check In").doClick();

        assertTrue(status().startsWith("Check-in rejected"));
        assertEquals(Color.RED, statusColour());
    }

    // ==================== Req8: cancel ====================

    @Test
    public void cancel_succeedsBeforeStartAndShowsTheRefundMessage() throws Exception {
        seedBooking(AIFixture.futureStart(), AIFixture.futureEnd());
        selectFirstBooking();

        button("Cancel").doClick();

        assertEquals("Booking cancelled. Deposit refunded.", status());
        assertEquals(GREEN, statusColour());
        assertEquals(BookingStatus.CANCELLED, this.<DefaultTableModel>field("myBookingsModel").getValueAt(0, 4));
    }

    @Test
    public void cancel_isReportedAsRejectedOnceTheBookingHasStarted() throws Exception {
        seedBooking(AIFixture.startedRecently(), AIFixture.hoursFromNow(1));
        selectFirstBooking();

        button("Cancel").doClick();

        assertTrue(status().startsWith("Cancel rejected"));
        assertEquals(Color.RED, statusColour());
    }

    // ==================== Req10: booking through the payment dialog ====================

    @Test(timeout = 20_000)
    public void book_collectsCreditCardDetailsFromTheDialogAndBooksTheRoom() throws Exception {
        requireDisplay();
        setWindow(AIFixture.futureStart(), AIFixture.futureEnd());
        search();
        this.<JTable>field("roomsTable").setRowSelectionInterval(0, 0);
        this.<JComboBox<PaymentMethod>>field("methodBox").setSelectedItem(PaymentMethod.CREDIT_CARD);

        AIDialogs.okWithFields("4111111111111111", "12/27", "123");
        button("Book Selected Room").doClick();

        assertTrue("Expected a booking confirmation, got: " + status(), status().startsWith("Booked!"));
        assertTrue("The student deposit should be shown", status().contains("20.00"));
        assertEquals(1, this.<DefaultTableModel>field("myBookingsModel").getRowCount());
    }

    @Test(timeout = 20_000)
    public void book_collectsDebitCardDetailsFromItsOwnDialog() throws Exception {
        requireDisplay();
        setWindow(AIFixture.futureStart(), AIFixture.futureEnd());
        search();
        this.<JTable>field("roomsTable").setRowSelectionInterval(0, 0);
        this.<JComboBox<PaymentMethod>>field("methodBox").setSelectedItem(PaymentMethod.DEBIT_CARD);

        AIDialogs.okWithFields("4111222233334444", "1234");
        button("Book Selected Room").doClick();

        assertTrue("Expected a booking confirmation, got: " + status(), status().startsWith("Booked!"));
    }

    @Test(timeout = 20_000)
    public void book_collectsInstitutionalBillingDetailsFromItsOwnDialog() throws Exception {
        requireDisplay();
        setWindow(AIFixture.futureStart(), AIFixture.futureEnd());
        search();
        this.<JTable>field("roomsTable").setRowSelectionInterval(0, 0);
        this.<JComboBox<PaymentMethod>>field("methodBox").setSelectedItem(PaymentMethod.INSTITUTIONAL_BILLING);

        AIDialogs.okWithFields("987654321", "YORK-2026");
        button("Book Selected Room").doClick();

        assertTrue("Expected a booking confirmation, got: " + status(), status().startsWith("Booked!"));
    }

    @Test(timeout = 20_000)
    public void book_reportsAnErrorWhenTheCardDetailsAreLeftBlank() throws Exception {
        // Empty fields make the strategy decline, which surfaces as the
        // manager's IllegalStateException and then the panel's error branch.
        requireDisplay();
        setWindow(AIFixture.futureStart(), AIFixture.futureEnd());
        search();
        this.<JTable>field("roomsTable").setRowSelectionInterval(0, 0);
        this.<JComboBox<PaymentMethod>>field("methodBox").setSelectedItem(PaymentMethod.CREDIT_CARD);

        AIDialogs.okWithFields("", "", "");
        button("Book Selected Room").doClick();

        assertTrue("Expected an error, got: " + status(), status().startsWith("Error:"));
        assertEquals(Color.RED, statusColour());
        assertEquals(0, this.<DefaultTableModel>field("myBookingsModel").getRowCount());
    }

    @Test(timeout = 20_000)
    public void book_doesNothingWhenTheUserCancelsThePaymentDialog() throws Exception {
        requireDisplay();
        setWindow(AIFixture.futureStart(), AIFixture.futureEnd());
        search();
        this.<JTable>field("roomsTable").setRowSelectionInterval(0, 0);
        String before = status();

        AIDialogs.cancelNext();
        button("Book Selected Room").doClick();

        assertEquals("Cancelling must leave the panel exactly as it was", before, status());
        assertEquals(0, this.<DefaultTableModel>field("myBookingsModel").getRowCount());
    }

    // ==================== Req8: edit through two input dialogs ====================

    @Test(timeout = 20_000)
    public void edit_asksForBothTimesAndMovesTheBooking() throws Exception {
        requireDisplay();
        seedBooking(AIFixture.futureStart(), AIFixture.futureEnd());
        selectFirstBooking();
        String newStart = AIFixture.hoursFromNow(6);
        String newEnd = AIFixture.hoursFromNow(7);

        AIDialogs.answerWithText(newStart, newEnd);
        button("Edit...").doClick();

        assertEquals("Booking updated.", status());
        assertEquals(newStart, this.<DefaultTableModel>field("myBookingsModel").getValueAt(0, 2));
        assertEquals(newEnd, this.<DefaultTableModel>field("myBookingsModel").getValueAt(0, 3));
    }

    @Test(timeout = 20_000)
    public void edit_isReportedAsRejectedAfterTheBookingHasStarted() throws Exception {
        requireDisplay();
        seedBooking(AIFixture.startedRecently(), AIFixture.hoursFromNow(1));
        selectFirstBooking();

        AIDialogs.answerWithText(AIFixture.hoursFromNow(6), AIFixture.hoursFromNow(7));
        button("Edit...").doClick();

        assertTrue("Expected a rejection, got: " + status(), status().startsWith("Edit rejected"));
        assertEquals(Color.RED, statusColour());
    }

    @Test(timeout = 20_000)
    public void edit_abandonsQuietlyWhenTheFirstDialogIsCancelled() throws Exception {
        requireDisplay();
        Booking booking = seedBooking(AIFixture.futureStart(), AIFixture.futureEnd());
        String originalStart = booking.getStartTime();
        selectFirstBooking();

        AIDialogs.cancelNext();
        button("Edit...").doClick();

        assertEquals("A cancelled edit must not change the booking", originalStart, booking.getStartTime());
    }

    // ==================== Req9: extend through an input dialog ====================

    @Test(timeout = 20_000)
    public void extend_pushesTheEndTimeOut() throws Exception {
        requireDisplay();
        seedBooking(AIFixture.startedRecently(), AIFixture.hoursFromNow(1));
        selectFirstBooking();
        String later = AIFixture.hoursFromNow(3);

        AIDialogs.answerWithText(later);
        button("Extend To...").doClick();

        assertEquals("Booking extended.", status());
        assertEquals(later, this.<DefaultTableModel>field("myBookingsModel").getValueAt(0, 3));
    }

    @Test(timeout = 20_000)
    public void extend_isReportedAsRejectedForAnEarlierEndTime() throws Exception {
        requireDisplay();
        seedBooking(AIFixture.startedRecently(), AIFixture.hoursFromNow(2));
        selectFirstBooking();

        AIDialogs.answerWithText(AIFixture.hoursFromNow(1));
        button("Extend To...").doClick();

        assertTrue("Expected a rejection, got: " + status(), status().startsWith("Extend rejected"));
    }

    @Test(timeout = 20_000)
    public void extend_abandonsQuietlyWhenTheDialogIsCancelled() throws Exception {
        requireDisplay();
        Booking booking = seedBooking(AIFixture.startedRecently(), AIFixture.hoursFromNow(1));
        String originalEnd = booking.getEndTime();
        selectFirstBooking();

        AIDialogs.cancelNext();
        button("Extend To...").doClick();

        assertEquals(originalEnd, booking.getEndTime());
    }

    // ==================== Req4 + Req10: paying the balance ====================

    @Test(timeout = 20_000)
    public void pay_settlesTheBalanceAndCompletesTheBooking() throws Exception {
        requireDisplay();
        seedBooking(AIFixture.startedRecently(), AIFixture.hoursFromNow(2));
        selectFirstBooking();
        button("Check In").doClick();
        selectFirstBooking();

        this.<JComboBox<PaymentMethod>>field("methodBox").setSelectedItem(PaymentMethod.CREDIT_CARD);
        AIDialogs.okWithFields("4111111111111111", "12/27", "123");
        button("Pay Remaining Balance").doClick();

        assertTrue("Expected a payment confirmation, got: " + status(), status().startsWith("Paid $"));
        assertEquals(BookingStatus.COMPLETED, this.<DefaultTableModel>field("myBookingsModel").getValueAt(0, 4));
    }

    @Test(timeout = 20_000)
    public void pay_doesNothingWhenThePaymentDialogIsCancelled() throws Exception {
        requireDisplay();
        seedBooking(AIFixture.startedRecently(), AIFixture.hoursFromNow(2));
        selectFirstBooking();
        button("Check In").doClick();
        selectFirstBooking();
        String before = status();

        AIDialogs.cancelNext();
        button("Pay Remaining Balance").doClick();

        assertEquals(before, status());
    }
}
