package scheduler.web;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

import scheduler.accounts.AccountManagement;
import scheduler.accounts.RegisteredUserFactory;
import scheduler.booking.BookingManager;
import scheduler.facade.SchedulerFacade;
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


public final class WebApp {

    /** Settings for one server */
    public static final class Config {
        public int port = 8080;
        public boolean demo = true;
        public String dataDir = "data";
        public String publicDir = "web/public";
        public String chiefPassword;
        public int passwordAttemptsPerMinute = 30;  // log-ins plus sign-ups allowed per minute, for the whole server
        public int maxNewAccountsInDemo = 200;      // accounts visitors may create on the demo site before sign-up closes
    }

    private final HttpServer server;
    private final ExecutorService workers;
    private final Path tempDataDir;

    private WebApp(HttpServer server, ExecutorService workers, Path tempDataDir) {
        this.server = server;
        this.workers = workers;
        this.tempDataDir = tempDataDir;
    }

    /** Builds the system, starts listening, and returns once the server is up. */
    public static WebApp start(Config config) {
        try {

            Path tempDir = config.demo ? Files.createTempDirectory("scheduler-demo-") : null;
            String dataDir = config.demo ? tempDir.toString() : config.dataDir;
            new File(dataDir).mkdirs();                              // SQLite cannot create missing folders

            SchedulerFacade facade = buildFacade(dataDir);
            DemoData demo = config.demo ? new DemoData(facade, config.chiefPassword) : null;

            // new InetSocketAddress(port) listens on every network interface, which a hosting platform needs.
            HttpServer server = HttpServer.create(new InetSocketAddress(config.port), 0);
            ExecutorService workers = Executors.newFixedThreadPool(8); // up to 8 requests are handled at once
            server.setExecutor(workers);
            RateLimiter passwordLimiter = new RateLimiter(config.passwordAttemptsPerMinute, 60_000); // 60,000 ms = one minute
            server.createContext("/api/", new ApiHandler(facade, new SessionStore(), demo, passwordLimiter,
                    config.maxNewAccountsInDemo)); // JSON API
            server.createContext("/", new StaticHandler(Paths.get(config.publicDir)));       // the page itself
            server.start();
            return new WebApp(server, workers, tempDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not start the web server", e);
        }
    }

    // The same wiring as MainUI.main(): SQLite adapters behind the repository
    // interfaces, then the managers, then the facade. Nothing here is web-specific.
    private static SchedulerFacade buildFacade(String dataDir) {
        SqliteDatabase db = new SqliteDatabase(dataDir + "/scheduler.db");
        RegisteredUserFactory userFactory = new RegisteredUserFactory();
        CsvToSqlMigration.migrateIfNeeded(dataDir, db, userFactory);  // imports old CSV data once, if any exists

        RoomRepository roomRepo = new SqliteRoomRepository(db);
        BookingRepository bookingRepo = new SqliteBookingRepository(db);
        UserRepository userRepo = new SqliteUserRepository(db, userFactory);
        PaymentRepository paymentRepo = new SqlitePaymentRepository(db);

        RoomManager roomManager = new RoomManager(roomRepo);
        BookingManager bookingManager = new BookingManager(bookingRepo, paymentRepo, roomManager);
        AccountManagement accountManagement = new AccountManagement(userRepo);
        return new SchedulerFacade(roomManager, bookingManager, accountManagement);
    }

    /** The port the server is really listening on (useful when the config asked for port 0). */
    public int port() {
        return server.getAddress().getPort();
    }

    /** Stops the server and, in demo mode, deletes the temporary database. */
    public void stop() {
        server.stop(0);                       // 0 = do not wait for open connections
        workers.shutdownNow();
        if (tempDataDir != null) {
            File[] files = tempDataDir.toFile().listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();            // only files this server created in its own temp folder
                }
            }
            tempDataDir.toFile().delete();
        }
    }
}
