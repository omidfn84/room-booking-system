package com.group10.scheduler.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.group10.scheduler.accounts.Administrator;
import com.group10.scheduler.gui.AdminController;
import com.group10.scheduler.room.Room;

import java.awt.*;

/**
 * Management view. Two sections gated differently:
 *  - Chief section (Req2): always visible — generating admins is the chief's act.
 *  - Room management (Req6): locked until an ADMIN LOGS IN with a valid admin id.
 * The panel is humble: every decision is made by AdminController.
 */
public class AdminPanel extends JPanel {
    private final AdminController controller;

    // chief section
    private JTextField newAdminIdField = new JTextField(8);
    private JTextField newAdminNameField = new JTextField(10);
    private JTextField newAdminEmailField = new JTextField(12);

    // admin session
    private JTextField adminLoginField = new JTextField(8);
    private JButton adminLoginBtn = new JButton("Log in as admin");
    private JLabel adminSessionLabel = new JLabel("Not logged in as admin.");

    // room management (disabled until admin login)
    private JTextField roomIdField = new JTextField(8);
    private JTextField capacityField = new JTextField(4);
    private JTextField buildingField = new JTextField(8);
    private JTextField roomNumberField = new JTextField(6);
    private JButton addBtn = new JButton("Add room");
    private JButton enableBtn = new JButton("Enable selected");
    private JButton disableBtn = new JButton("Disable selected");
    private JButton closeBtn = new JButton("Close for maintenance");

    private JLabel statusLabel = new JLabel(" ");

    private DefaultTableModel roomsModel = new DefaultTableModel(
            new Object[]{"Room ID", "Building", "Room #", "Capacity", "Status"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private JTable roomsTable = new JTable(roomsModel);

    public AdminPanel(AdminController controller) {
        this.controller = controller;
        buildUI();
        setRoomManagementEnabled(false);
        refreshRooms();
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // --- Chief section (Req2) ---
        JPanel chiefForm = new JPanel();
        chiefForm.setBorder(BorderFactory.createTitledBorder("Chief event coordinator — generate administrator"));
        chiefForm.add(new JLabel("Admin ID:")); chiefForm.add(newAdminIdField);
        chiefForm.add(new JLabel("Name:")); chiefForm.add(newAdminNameField);
        chiefForm.add(new JLabel("Email:")); chiefForm.add(newAdminEmailField);
        JButton generateBtn = new JButton("Generate admin");
        chiefForm.add(generateBtn);
        generateBtn.addActionListener(e -> handleGenerateAdmin());

        // --- Admin session row ---
        JPanel sessionRow = new JPanel();
        sessionRow.setBorder(BorderFactory.createTitledBorder("Administrator session"));
        sessionRow.add(new JLabel("Admin ID:"));
        sessionRow.add(adminLoginField);
        sessionRow.add(adminLoginBtn);
        sessionRow.add(adminSessionLabel);
        adminLoginBtn.addActionListener(e -> handleAdminLogin());

        // --- Room management (Req6, requires admin session) ---
        JPanel addForm = new JPanel();
        addForm.setBorder(BorderFactory.createTitledBorder("Room management — admin only"));
        addForm.add(new JLabel("Room ID:")); addForm.add(roomIdField);
        addForm.add(new JLabel("Building:")); addForm.add(buildingField);
        addForm.add(new JLabel("Room #:")); addForm.add(roomNumberField);
        addForm.add(new JLabel("Capacity:")); addForm.add(capacityField);
        addForm.add(addBtn);
        addBtn.addActionListener(e -> handleAddRoom());

        JPanel north = new JPanel(new GridLayout(3, 1, 0, 6));
        north.add(chiefForm);
        north.add(sessionRow);
        north.add(addForm);

        add(north, BorderLayout.NORTH);
        add(new JScrollPane(roomsTable), BorderLayout.CENTER);

        JPanel actionsRow = new JPanel();
        JButton refreshBtn = new JButton("Refresh");
        actionsRow.add(refreshBtn);
        actionsRow.add(enableBtn);
        actionsRow.add(disableBtn);
        actionsRow.add(closeBtn);

        refreshBtn.addActionListener(e -> refreshRooms());
        enableBtn.addActionListener(e -> withSelectedRoom(roomId -> controller.onEnableRoomClicked(roomId)));
        disableBtn.addActionListener(e -> withSelectedRoom(roomId -> controller.onDisableRoomClicked(roomId)));
        closeBtn.addActionListener(e -> withSelectedRoom(roomId -> controller.onCloseRoomClicked(roomId)));

        JPanel south = new JPanel(new BorderLayout());
        south.add(actionsRow, BorderLayout.NORTH);
        south.add(statusLabel, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);
    }

    private void handleAdminLogin() {
        boolean ok = controller.onAdminLoginClicked(adminLoginField.getText().trim());
        if (ok) {
            adminSessionLabel.setText("Logged in as admin " + controller.getCurrentAdminId());
            setStatus("Administrator session started.", false);
        } else {
            adminSessionLabel.setText("Not logged in as admin.");
            setStatus("Unknown admin ID.", true);
        }
        setRoomManagementEnabled(ok);
    }

    private void setRoomManagementEnabled(boolean enabled) {
        roomIdField.setEnabled(enabled);
        capacityField.setEnabled(enabled);
        buildingField.setEnabled(enabled);
        roomNumberField.setEnabled(enabled);
        addBtn.setEnabled(enabled);
        enableBtn.setEnabled(enabled);
        disableBtn.setEnabled(enabled);
        closeBtn.setEnabled(enabled);
    }

    private void handleGenerateAdmin() {
        try {
            Administrator admin = controller.onGenerateAdminClicked(
                    newAdminIdField.getText().trim(),
                    newAdminNameField.getText().trim(),
                    newAdminEmailField.getText().trim());
            if (admin == null) {
                setStatus("Admin ID already exists.", true);
            } else {
                setStatus("Generated: " + admin, false);
                adminLoginField.setText(newAdminIdField.getText().trim());
            }
        } catch (Exception ex) {
            setStatus("Error: " + ex.getMessage(), true);
        }
    }

    private void handleAddRoom() {
        try {
            int capacity = Integer.parseInt(capacityField.getText().trim());
            boolean ok = controller.onAddRoomClicked(
                    roomIdField.getText().trim(), capacity,
                    buildingField.getText().trim(), roomNumberField.getText().trim());
            setStatus(ok ? "Room added: " + roomIdField.getText().trim()
                         : "Add rejected (not logged in as admin, or duplicate room ID).", !ok);
            refreshRooms();
        } catch (NumberFormatException nfe) {
            setStatus("Capacity must be a number.", true);
        } catch (Exception ex) {
            setStatus("Error: " + ex.getMessage(), true);
        }
    }

    private void withSelectedRoom(java.util.function.Predicate<String> action) {
        int row = roomsTable.getSelectedRow();
        if (row < 0) { setStatus("Select a room first.", true); return; }
        String roomId = (String) roomsModel.getValueAt(row, 0);
        boolean ok = action.test(roomId);
        setStatus(ok ? "Updated " + roomId : "Update failed for " + roomId + ".", !ok);
        refreshRooms();
    }

    public void refreshRooms() {
        roomsModel.setRowCount(0);
        for (Room r : controller.getAllRooms()) {
            roomsModel.addRow(new Object[]{r.getRoomId(), r.getBuilding(), r.getRoomNumber(), r.getCapacity(), r.getStatus()});
        }
    }

    private void setStatus(String text, boolean isError) {
        statusLabel.setText(text);
        statusLabel.setForeground(isError ? Color.RED : new Color(0, 128, 0));
    }
}