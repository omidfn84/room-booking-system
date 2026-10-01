package com.group10.scheduler.panels;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.group10.scheduler.booking.Booking;
import com.group10.scheduler.booking.PaymentMethod;
import com.group10.scheduler.gui.GUIController;
import com.group10.scheduler.room.Room;

import java.awt.*;
import java.util.List;

public class BookingPanel extends JPanel {
    private final GUIController controller;

    private JTextField startField = new JTextField(
            java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES).toString(), 16);
    private JTextField endField = new JTextField(
            java.time.LocalDateTime.now().plusHours(1).truncatedTo(java.time.temporal.ChronoUnit.MINUTES).toString(), 16);

    
    private JComboBox<PaymentMethod> methodBox = new JComboBox<>(PaymentMethod.values());
    private JLabel statusLabel = new JLabel(" ");

    private DefaultTableModel roomsModel = new DefaultTableModel(
            new Object[]{"Room ID", "Building", "Room #", "Capacity"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private JTable roomsTable = new JTable(roomsModel);

    private DefaultTableModel myBookingsModel = new DefaultTableModel(
            new Object[]{"Booking ID", "Room", "Start", "End", "Status"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private JTable myBookingsTable = new JTable(myBookingsModel);

    public BookingPanel(GUIController controller) {
        this.controller = controller;
        setBorder(new javax.swing.border.EmptyBorder(12, 12, 12, 12));
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));

        // --- Top: search form ---
        JPanel searchForm = new JPanel();
        searchForm.add(new JLabel("Start (yyyy-MM-ddTHH:mm):"));
        searchForm.add(startField);
        searchForm.add(new JLabel("End:"));
        searchForm.add(endField);
        JButton searchBtn = new JButton("Search Available Rooms");
        searchForm.add(searchBtn);
        searchBtn.addActionListener(e -> handleSearch());

        // --- Middle: results table + book button ---
        JPanel resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.add(new JScrollPane(roomsTable), BorderLayout.CENTER);
        JPanel bookRow = new JPanel();
        bookRow.add(new JLabel("Payment method:"));
        bookRow.add(methodBox);
        JButton bookBtn = new JButton("Book Selected Room");
        bookRow.add(bookBtn);
        resultsPanel.add(bookRow, BorderLayout.SOUTH);
        bookBtn.addActionListener(e -> handleBook());

        // --- Bottom: my bookings + actions ---
        JPanel myBookingsPanel = new JPanel(new BorderLayout());
        myBookingsPanel.add(new JLabel("My Bookings"), BorderLayout.NORTH);
        myBookingsPanel.add(new JScrollPane(myBookingsTable), BorderLayout.CENTER);

        JPanel actionsRow = new JPanel();
        JButton refreshBtn = new JButton("Refresh");
        JButton checkInBtn = new JButton("Check In");
        JButton editBtn = new JButton("Edit...");
        JButton cancelBtn = new JButton("Cancel");
        JButton extendBtn = new JButton("Extend To...");
        JButton payBtn = new JButton("Pay Remaining Balance");
        actionsRow.add(refreshBtn);
        actionsRow.add(checkInBtn);
        actionsRow.add(editBtn);
        actionsRow.add(cancelBtn);
        actionsRow.add(extendBtn);
        actionsRow.add(payBtn);
        myBookingsPanel.add(actionsRow, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> refreshMyBookings());
        checkInBtn.addActionListener(e -> handleCheckIn());
        editBtn.addActionListener(e -> handleEdit());
        cancelBtn.addActionListener(e -> handleCancel());
        extendBtn.addActionListener(e -> handleExtend());
        payBtn.addActionListener(e -> handlePay());

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, resultsPanel, myBookingsPanel);
        split.setResizeWeight(0.5);

        add(searchForm, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    /** Called by MainUI whenever this tab becomes visible / after login. */
    public void refreshMyBookings() {
        myBookingsModel.setRowCount(0);
        List<Booking> mine = controller.getMyBookings();
        for (Booking b : mine) {
            myBookingsModel.addRow(new Object[]{
                    b.getBookingId(), b.getRoomId(), b.getStartTime(), b.getEndTime(), b.getStatus()
            });
        }
    }

    private void handleSearch() {
        try {
            roomsModel.setRowCount(0);
            List<Room> rooms = controller.onSearchRoomsClicked(startField.getText().trim(), endField.getText().trim());
            for (Room r : rooms) {
                roomsModel.addRow(new Object[]{r.getRoomId(), r.getBuilding(), r.getRoomNumber(), r.getCapacity()});
            }
            setStatus(rooms.isEmpty() ? "No rooms available for that window." : rooms.size() + " room(s) found.", false);
        } catch (Exception ex) {
            setStatus("Error: " + ex.getMessage(), true);
        }
    }

    private void handleBook() {
        int row = roomsTable.getSelectedRow();
        if (row < 0) { setStatus("Select a room first.", true); return; }
        String roomId = (String) roomsModel.getValueAt(row, 0);
        PaymentMethod method = (PaymentMethod) methodBox.getSelectedItem();
        String[] paymentFields = promptForPaymentFields(method);
        if (paymentFields == null) return; // user cancelled the payment dialog
        try {
            Booking b = controller.onBookRoomClicked(roomId, startField.getText().trim(), endField.getText().trim(),method, paymentFields);
            setStatus(String.format("Booked! Deposit charged: $%.2f. Booking ID: %s", b.getDepositAmount(), b.getBookingId()), false);
            refreshMyBookings();
        } catch (Exception ex) {
            setStatus("Error: " + ex.getMessage(), true);
        }
    }

    private void handleCheckIn() {
        withSelectedBooking(bookingId -> {
            boolean ok = controller.onCheckInClicked(bookingId);
            setStatus(ok ? "Checked in. Use Pay Balance to settle the remaining amount."
                         : "Check-in rejected (outside window or invalid state).", !ok);
            refreshMyBookings();
        });
    }


    // Req8: edit both times before the booking starts
    private void handleEdit() {
        withSelectedBooking(bookingId -> {
            String start = JOptionPane.showInputDialog(this, "New start time (yyyy-MM-ddTHH:mm):");
            if (start == null || start.isBlank()) return;
            String end = JOptionPane.showInputDialog(this, "New end time (yyyy-MM-ddTHH:mm):");
            if (end == null || end.isBlank()) return;
            boolean ok = controller.onEditBookingClicked(bookingId, start.trim(), end.trim());
            setStatus(ok ? "Booking updated." : "Edit rejected (after start time, bad times, or room conflict).", !ok);
            refreshMyBookings();
        });
    }

    private void handleCancel() {
        withSelectedBooking(bookingId -> {
            boolean ok = controller.onCancelClicked(bookingId);
            setStatus(ok ? "Booking cancelled. Deposit refunded." : "Cancel rejected (already started?).", !ok); // TODO: add the amount?
            refreshMyBookings();
        });
    }

    private void handleExtend() {
        withSelectedBooking(bookingId -> {
            String until = JOptionPane.showInputDialog(this, "Extend end time to (yyyy-MM-ddTHH:mm):");
            if (until == null || until.isBlank()) return;
            boolean ok = controller.onExtendClicked(bookingId, until.trim());
            setStatus(ok ? "Booking extended." : "Extend rejected (expired, not later, or room conflict).", !ok);
            refreshMyBookings();
        });
    }

    // Req4/Req10: pay the remaining balance (final cost minus applied deposit)
    private void handlePay() {
        withSelectedBooking(bookingId -> {
            PaymentMethod method = (PaymentMethod) methodBox.getSelectedItem();
            String[] paymentFields = promptForPaymentFields(method);
            if (paymentFields == null) 
            	return;
            
            double paid = controller.onPayClicked(bookingId, method, paymentFields);
            setStatus(paid >= 0 ? String.format("Paid $%.2f. Booking completed.", paid) : "Payment rejected (check in first, or payment failed).", paid < 0);
            refreshMyBookings();
        });
    }


    /** Req10: collects the payment dialog's field values as plain strings.
     *  The facade maps them to the right strategy internally - this panel
     *  knows nothing about the booking subsystem. Null = user cancelled. */
    private String[] promptForPaymentFields(PaymentMethod method) {
        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        switch (method) {
            case CREDIT_CARD: {
                JTextField card = new JTextField(16);
                JTextField expiry = new JTextField(5);
                JTextField cvv = new JTextField(3);
                form.add(new JLabel("Card number:")); form.add(card);
                form.add(new JLabel("Expiry (MM/YY):")); form.add(expiry);
                form.add(new JLabel("CVV:")); form.add(cvv);
                if (JOptionPane.showConfirmDialog(this, form, "Credit Card Details",
                        JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return null;
                return new String[]{card.getText().trim(), expiry.getText().trim(), cvv.getText().trim()};
            }
            case DEBIT_CARD: {
                JTextField card = new JTextField(16);
                JPasswordField pin = new JPasswordField(4);
                form.add(new JLabel("Card number:")); form.add(card);
                form.add(new JLabel("PIN:")); form.add(pin);
                if (JOptionPane.showConfirmDialog(this, form, "Debit Card Details",
                        JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return null;
                return new String[]{card.getText().trim(), new String(pin.getPassword())};
            }
            case INSTITUTIONAL_BILLING: {
                JTextField orgId = new JTextField(9);
                JTextField account = new JTextField(12);
                form.add(new JLabel("Organization ID:")); form.add(orgId);
                form.add(new JLabel("Billing account:")); form.add(account);
                if (JOptionPane.showConfirmDialog(this, form, "Institutional Billing Details",
                        JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return null;
                // raw strings; the facade validates and parses the organization id
                return new String[]{orgId.getText().trim(), account.getText().trim()};
            }
        }
        return null;
    }

    private void withSelectedBooking(java.util.function.Consumer<String> action) {
        int row = myBookingsTable.getSelectedRow();
        if (row < 0) { setStatus("Select one of your bookings first.", true); return; }
        String bookingId = (String) myBookingsModel.getValueAt(row, 0);
        action.accept(bookingId);
    }

    private void setStatus(String text, boolean isError) {
        statusLabel.setText(text);
        statusLabel.setForeground(isError ? Color.RED : new Color(0, 128, 0));
    }
}