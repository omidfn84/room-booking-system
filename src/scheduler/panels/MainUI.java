package scheduler.panels;

import scheduler.accounts.AccountManagement;
import scheduler.accounts.RegisteredUserFactory;
import scheduler.booking.BookingManager;
import scheduler.facade.SchedulerFacade;
import scheduler.gui.AdminController;
import scheduler.gui.GUIController;
import scheduler.persistence.BookingRepository;
import scheduler.persistence.PaymentRepository;
import scheduler.persistence.RoomRepository;
import scheduler.persistence.UserRepository;
import scheduler.persistence.sql.CsvToSqlMigration;
import scheduler.persistence.sql.SqliteBookingRepository;
import scheduler.persistence.sql.SqliteDatabase;
import scheduler.persistence.sql.SqlitePaymentRepository;
import scheduler.persistence.sql.SqliteRoomRepository;
import scheduler.persistence.sql.SqliteUserRepository;
import scheduler.room.RoomManager;

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
     * Wires the SQLite persistence layer (Adapter pattern), domain managers, and
     * Facade, then launches the GUI. This is the composition root — the only
     * place in the app that mentions concrete repository classes and file paths.
     */
    public static void main(String[] args) {
        String dataDir = "data"; // relative to the working directory; holds scheduler.db
        new java.io.File(dataDir).mkdirs(); // SQLite cannot create missing directories

        SqliteDatabase db = new SqliteDatabase(dataDir + "/scheduler.db");
        RegisteredUserFactory userFactory = new RegisteredUserFactory();
        // carry over data saved by older CSV-based versions of the app
        CsvToSqlMigration.migrateIfNeeded(dataDir, db, userFactory);

        // program to the «Target» interfaces (Adapter pattern): the rest of the
        // system never knows these are SQL-backed
        RoomRepository roomRepo = new SqliteRoomRepository(db);
        BookingRepository bookingRepo = new SqliteBookingRepository(db);
        UserRepository userRepo = new SqliteUserRepository(db, userFactory);
        PaymentRepository paymentRepo = new SqlitePaymentRepository(db);

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