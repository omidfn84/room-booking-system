package com.group10.scheduler.panels;

import com.group10.scheduler.accounts.AccountManagement;
import com.group10.scheduler.accounts.RegisteredUserFactory;
import com.group10.scheduler.booking.BookingManager;
import com.group10.scheduler.facade.SchedulerFacade;
import com.group10.scheduler.gui.AdminController;
import com.group10.scheduler.gui.GUIController;
import com.group10.scheduler.persistence.csv.CsvBookingRepository;
import com.group10.scheduler.persistence.csv.CsvPaymentRepository;
import com.group10.scheduler.persistence.BookingRepository;
import com.group10.scheduler.persistence.PaymentRepository;
import com.group10.scheduler.persistence.RoomRepository;
import com.group10.scheduler.persistence.UserRepository;
import com.group10.scheduler.persistence.csv.CsvRoomRepository;
import com.group10.scheduler.persistence.csv.CsvUserRepository;
import com.group10.scheduler.room.RoomManager;

import javax.swing.*;
import java.awt.*;

public class MainUI extends JFrame {

    private final GUIController controller;
    private final AdminController adminController;
    private final JTabbedPane tabs = new JTabbedPane();
    private BookingPanel bookingPanel;
    private AdminPanel adminPanel;
    private LoginPanel loginPanel;

    public MainUI(SchedulerFacade facade) {
        super("YorkU Conference Room Scheduler");
        this.controller = new GUIController(facade); 
        this.adminController = new AdminController(facade);
        buildUI();
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void buildUI() {
        
    	loginPanel = new LoginPanel(controller, this::onLoginSuccess);
        bookingPanel = new BookingPanel(controller);
        adminPanel = new AdminPanel(adminController);

        tabs.addTab("Login / Register", loginPanel);
        tabs.addTab("Book a Room", bookingPanel);
        tabs.addTab("Admin", adminPanel);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tabs, BorderLayout.CENTER);
    }

    private void onLoginSuccess() {
        bookingPanel.refreshMyBookings();
        adminPanel.refreshRooms();
        tabs.setSelectedComponent(bookingPanel);
    }

    /**
     * Wires the CSV persistence layer (Adapter pattern), domain managers, and
     * Facade, then launches the GUI. This is the composition root — the only
     * place in the app that mentions concrete CSV classes and file paths.
     */
    public static void main(String[] args) {
        String dataDir = "data"; // relative to the working directory; contains the CSV files
        new java.io.File(dataDir).mkdirs(); // FileWriter cannot create missing directories

        // program to the «Target» interfaces (Adapter pattern): the rest of the
        // system never knows these are CSV-backed
        RoomRepository roomRepo = new CsvRoomRepository(dataDir + "/rooms.csv");
        BookingRepository bookingRepo = new CsvBookingRepository(dataDir + "/bookings.csv");
        UserRepository userRepo = new CsvUserRepository(dataDir + "/users.csv", new RegisteredUserFactory());
        PaymentRepository paymentRepo = new CsvPaymentRepository(dataDir + "/payments.csv");

        RoomManager roomManager = new RoomManager(roomRepo);
        BookingManager bookingManager = new BookingManager(bookingRepo, paymentRepo, roomManager);
        AccountManagement accountManagement = new AccountManagement(userRepo);

        SchedulerFacade facade = new SchedulerFacade(roomManager, bookingManager, accountManagement);

        // Free win: the Nimbus look-and-feel ships with the JDK and instantly
        // modernizes every Swing component. Falls back silently if unavailable.
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception lafException) {
            // keep the default look-and-feel
        }

        SwingUtilities.invokeLater(() -> {
            MainUI frame = new MainUI(facade);
            frame.setVisible(true);
        });
    }
}