package scheduler.web;

import static org.junit.Assert.*;
import static scheduler.web.WebTestClient.json;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import scheduler.persistence.sql.SqliteDatabase;
import scheduler.persistence.sql.SqliteRoomRepository;
import scheduler.room.Room;
import scheduler.room.RoomStatus;
import scheduler.web.WebTestClient.Response;

/**
 * End-to-end tests with demo mode switched off: real registration, password
 * login, payment details supplied by the user, data kept in a SQLite file,
 * and the page files served from disk. Everything lives in a temporary
 * folder, so the project's own data/ folder is never touched.
 */
public class WebFullModeApiTest {

    @ClassRule
    public static TemporaryFolder temp = new TemporaryFolder();

    private static WebApp app;
    private static int port;
    private static File dataDir;
    private static File publicDir;
    private static final AtomicInteger NEXT = new AtomicInteger(1);
    private static final String PASSWORD = "Str0ng!Pass";

    @BeforeClass
    public static void startServer() throws Exception {
        dataDir = temp.newFolder("data");
        publicDir = temp.newFolder("site", "public");
        // Full mode has no administrator on the web, so the rooms are put in the database first.
        seedRooms(dataDir);
        Files.write(new File(publicDir, "index.html").toPath(), "<!doctype html><title>home</title>".getBytes(StandardCharsets.UTF_8));
        Files.write(new File(publicDir, "app.js").toPath(), "'use strict';".getBytes(StandardCharsets.UTF_8));
        Files.write(new File(publicDir, "styles.css").toPath(), "body{}".getBytes(StandardCharsets.UTF_8));
        Files.write(new File(publicDir, ".hidden.txt").toPath(), "hidden".getBytes(StandardCharsets.UTF_8));
        Files.write(new File(publicDir, "notes.md").toPath(), "notes".getBytes(StandardCharsets.UTF_8));
        new File(publicDir, "sub.txt").mkdir();                                    // a folder that looks like a file
        // a file with an allowed extension that sits OUTSIDE the public folder
        Files.write(new File(publicDir.getParentFile(), "secret.txt").toPath(), "top secret".getBytes(StandardCharsets.UTF_8));
        app = start(dataDir);
        port = app.port();
    }

    @AfterClass
    public static void stopServer() {
        app.stop();
    }

    private static void seedRooms(File dir) {
        SqliteDatabase db = new SqliteDatabase(new File(dir, "scheduler.db").getAbsolutePath());
        new SqliteRoomRepository(db).saveRooms(List.of(
                new Room("R1", 10, "Bergeron Centre", "101", RoomStatus.AVAILABLE),
                new Room("R2", 6, "Scott Library", "202", RoomStatus.AVAILABLE)));
    }

    private static WebApp start(File dir) {
        WebApp.Config config = new WebApp.Config();
        config.port = 0;
        config.demo = false;
        config.dataDir = dir.getAbsolutePath();
        config.publicDir = publicDir.getAbsolutePath();
        config.passwordAttemptsPerMinute = 100_000;   // the limit has its own test class; here it must not get in the way
        return WebApp.start(config);
    }

    // ---------------------------------------------------------------- helpers

    private static WebTestClient visitor() {
        return new WebTestClient(port);
    }

    private static String newEmail() {
        return "user" + NEXT.getAndIncrement() + "@yorku.ca";
    }

    private static String registration(String email, String password, String type) {
        return json("email", email, "password", password, "userName", "Test User", "accountType", type, "organizationId", "123456789");
    }

    /** A new visitor with a freshly registered (and signed-in) student account. */
    private static WebTestClient registered() throws Exception {
        WebTestClient client = visitor();
        Response r = client.post("/api/register", registration(newEmail(), PASSWORD, "STUDENT"));
        assertEquals(r.text, 200, r.status);
        return client;
    }

    /** A future day (10:00) no other test uses. */
    private static LocalDateTime day() {
        return LocalDateTime.now().plusDays(NEXT.getAndIncrement()).withHour(10).truncatedTo(ChronoUnit.HOURS);
    }

    private static String t(LocalDateTime time) {
        return time.truncatedTo(ChronoUnit.MINUTES).toString();
    }

    private static String creditCardBooking(String roomId, LocalDateTime start, LocalDateTime end) {
        return json("roomId", roomId, "start", t(start), "end", t(end), "paymentMethod", "CREDIT_CARD",
                "cardNumber", "4242424242424242", "expiryDate", "12/30", "cvv", "123");
    }

    // ---------------------------------------------------------------- accounts

    @Test
    public void configSaysDemoIsOff() throws Exception {
        Map<String, Object> config = visitor().get("/api/config").object();
        assertEquals(Boolean.FALSE, config.get("demo"));
        assertTrue(((List<?>) config.get("demoAccounts")).isEmpty());
    }

    @Test
    public void demoLoginIsClosedOutsideDemoMode() throws Exception {
        assertEquals(403, visitor().post("/api/demo-login", json("account", "student")).status);
        assertEquals(403, visitor().post("/api/demo-login", json("account", "admin")).status);
    }

    @Test
    public void registeringSignsTheNewUserIn() throws Exception {
        WebTestClient client = visitor();
        String email = newEmail();
        Response r = client.post("/api/register", registration(email, PASSWORD, "FACULTY"));
        assertEquals(r.text, 200, r.status);
        Map<String, Object> me = client.get("/api/me").object();
        assertEquals(Boolean.TRUE, me.get("authenticated"));
        assertEquals(email, me.get("email"));
        assertEquals("FACULTY", me.get("accountType"));
        assertEquals(30L, me.get("hourlyRate"));
    }

    @Test
    public void theAccountRulesAreEnforcedAndTheirMessagesReachTheBrowser() throws Exception {
        WebTestClient client = visitor();
        Response weak = client.post("/api/register", registration(newEmail(), "short", "STUDENT"));
        assertEquals(400, weak.status);
        assertEquals("Password must contain at least 8 characters.", weak.error());

        Response notYork = client.post("/api/register", registration("someone@gmail.com", PASSWORD, "STUDENT"));
        assertEquals(400, notYork.status);
        assertEquals("University accounts must use a university email.", notYork.error());

        Response badType = client.post("/api/register", registration(newEmail(), PASSWORD, "WIZARD"));
        assertEquals(400, badType.status);

        Response badOrg = client.post("/api/register", json("email", newEmail(), "password", PASSWORD, "userName", "X",
                "accountType", "STUDENT", "organizationId", "12"));
        assertEquals(400, badOrg.status);

        assertEquals("nobody was signed in by a failed registration", Boolean.FALSE, client.get("/api/me").object().get("authenticated"));
    }

    @Test
    public void anEmailCanOnlyBeRegisteredOnce() throws Exception {
        String email = newEmail();
        assertEquals(200, visitor().post("/api/register", registration(email, PASSWORD, "STUDENT")).status);
        Response again = visitor().post("/api/register", registration(email, PASSWORD, "STUDENT"));
        assertEquals(400, again.status);
        assertEquals("This email is already registered.", again.error());
    }

    @Test
    public void loginNeedsTheRightPassword() throws Exception {
        String email = newEmail();
        assertEquals(200, visitor().post("/api/register", registration(email, PASSWORD, "STUDENT")).status);

        WebTestClient client = visitor();
        Response wrong = client.post("/api/login", json("email", email, "password", "Wrong!Pass1"));
        assertEquals(401, wrong.status);
        Response unknown = client.post("/api/login", json("email", "nobody@yorku.ca", "password", PASSWORD));
        assertEquals(401, unknown.status);
        assertEquals("the two failures look identical", wrong.error(), unknown.error());
        assertNull("no session cookie after failed logins", client.sessionCookie());

        Response ok = client.post("/api/login", json("email", email, "password", PASSWORD));
        assertEquals(ok.text, 200, ok.status);
        assertEquals(email, ok.object().get("email"));
    }

    @Test
    public void passwordsNeverAppearInAnyResponse() throws Exception {
        WebTestClient client = visitor();
        Response r = client.post("/api/register", registration(newEmail(), PASSWORD, "STUDENT"));
        assertFalse(r.text.contains(PASSWORD));
        assertFalse(r.text.toLowerCase().contains("pbkdf2"));
        assertFalse(client.get("/api/me").text.toLowerCase().contains("password"));
    }

    @Test
    public void thereIsNoAdministratorAccessOnTheWebInFullMode() throws Exception {
        WebTestClient user = registered();
        assertEquals(403, user.get("/api/admin/rooms").status);
        assertEquals(403, user.post("/api/admin/rooms/R1/disable", "{}").status);
        assertEquals(401, visitor().get("/api/admin/rooms").status);
    }

    // ---------------------------------------------------------------- bookings with real payment details

    @Test
    public void bookingUsesThePaymentDetailsFromTheRequest() throws Exception {
        WebTestClient user = registered();
        LocalDateTime start = day();
        Response ok = user.post("/api/bookings", creditCardBooking("R1", start, start.plusHours(1)));
        assertEquals(ok.text, 200, ok.status);
        assertEquals("PAID", ok.object().get("depositStatus"));

        Response missing = user.post("/api/bookings", json("roomId", "R2", "start", t(start), "end", t(start.plusHours(1)),
                "paymentMethod", "CREDIT_CARD"));
        assertEquals("card fields are required", 400, missing.status);

        Response blankCvv = user.post("/api/bookings", json("roomId", "R2", "start", t(start), "end", t(start.plusHours(1)),
                "paymentMethod", "CREDIT_CARD", "cardNumber", "4242424242424242", "expiryDate", "12/30", "cvv", ""));
        assertEquals(400, blankCvv.status);
        assertEquals("Enter the 3 or 4 digit security code.", blankCvv.error());

        Response badOrg = user.post("/api/bookings", json("roomId", "R2", "start", t(start), "end", t(start.plusHours(1)),
                "paymentMethod", "INSTITUTIONAL_BILLING", "organizationId", "abc", "billingAccount", "ACC-1"));
        assertEquals(400, badOrg.status);

        assertEquals("only the successful booking exists", 1, user.get("/api/bookings").list().size());
    }

    @Test
    public void aBookingNobodyCheckedInToShowsAsExpiredWithTheDepositKept() throws Exception {
        WebTestClient user = registered();
        LocalDateTime start = LocalDateTime.now().minusHours(2);     // full mode has no "not in the past" limit
        Response booked = user.post("/api/bookings", creditCardBooking("R2", start, start.plusHours(1)));
        assertEquals(booked.text, 200, booked.status);
        assertEquals("CONFIRMED", booked.object().get("status"));

        Map<String, Object> listed = user.get("/api/bookings").list().get(0);
        assertEquals("EXPIRED", listed.get("status"));                // Req4: 30 minutes passed without a check-in
        assertEquals(Boolean.TRUE, listed.get("depositForfeited"));
        assertEquals(409, user.post("/api/bookings/" + listed.get("bookingId") + "/check-in", "{}").status);
    }

    @Test
    public void accountsAndBookingsSurviveARestart() throws Exception {
        File dir = temp.newFolder();
        seedRooms(dir);
        String email = newEmail();
        LocalDateTime start = day();

        WebApp first = start(dir);
        WebTestClient before = new WebTestClient(first.port());
        assertEquals(200, before.post("/api/register", registration(email, PASSWORD, "STUDENT")).status);
        Response booked = before.post("/api/bookings", creditCardBooking("R1", start, start.plusHours(2)));
        assertEquals(booked.text, 200, booked.status);
        first.stop();

        WebApp second = start(dir);                                   // a new server reading the same SQLite file
        try {
            WebTestClient after = new WebTestClient(second.port());
            assertEquals("the old session is gone", 401, after.get("/api/bookings").status);
            assertEquals(200, after.post("/api/login", json("email", email, "password", PASSWORD)).status);
            List<Map<String, Object>> bookings = after.get("/api/bookings").list();
            assertEquals(1, bookings.size());
            assertEquals(booked.object().get("bookingId"), bookings.get(0).get("bookingId"));
            assertEquals("CONFIRMED", bookings.get(0).get("status"));
        } finally {
            second.stop();
        }
        assertTrue("full mode keeps its database after stop()", new File(dir, "scheduler.db").isFile());
    }

    // ---------------------------------------------------------------- the page files

    @Test
    public void theFrontPageIsServedWithSecurityHeaders() throws Exception {
        Response r = visitor().get("/");
        assertEquals(200, r.status);
        assertTrue(r.text.contains("<title>home</title>"));
        assertEquals("text/html; charset=utf-8", r.header("Content-Type"));
        assertTrue(r.header("Content-Security-Policy").contains("default-src 'self'"));
        assertEquals("nosniff", r.header("X-Content-Type-Options"));
        assertEquals("DENY", r.header("X-Frame-Options"));
        assertEquals(r.text, visitor().get("/index.html").text);
    }

    @Test
    public void scriptsAndStylesGetTheRightContentType() throws Exception {
        assertEquals("text/javascript; charset=utf-8", visitor().get("/app.js").header("Content-Type"));
        assertEquals("text/css; charset=utf-8", visitor().get("/styles.css").header("Content-Type"));
    }

    @Test
    public void headReturnsHeadersWithoutABody() throws Exception {
        Response r = visitor().method("HEAD", "/app.js");
        assertEquals(200, r.status);
        assertEquals("", r.text);
    }

    @Test
    public void filesOutsideThePublicFolderCannotBeRead() throws Exception {
        // secret.txt really exists one level above the public folder
        for (String path : new String[] {"/..%2fsecret.txt", "/%2e%2e/secret.txt", "/../secret.txt",
                "/x/..%2f..%2fsecret.txt", "/..%5csecret.txt", "/%2e%2e%2fsecret.txt"}) {
            Response r = visitor().get(path);
            assertEquals(path, 404, r.status);
            assertFalse(path, r.text.contains("top secret"));
        }
    }

    @Test
    public void hiddenFilesUnknownTypesFoldersAndMissingFilesAreNotServed() throws Exception {
        assertEquals(404, visitor().get("/.hidden.txt").status);
        assertEquals(404, visitor().get("/notes.md").status);
        assertEquals(404, visitor().get("/sub.txt").status);
        assertEquals(404, visitor().get("/missing.html").status);
    }

    @Test
    public void thePageFilesAreReadOnly() throws Exception {
        assertEquals(405, visitor().post("/index.html", "{}").status);
        assertEquals(405, visitor().method("DELETE", "/app.js").status);
    }

    @Test
    public void theRealPageFilesExistAndLoadNothingFromOtherSites() throws Exception {
        // The shipped page must work under "default-src 'self'": no inline script or style, no outside hosts.
        String html = new String(Files.readAllBytes(new File("web/public/index.html").toPath()), StandardCharsets.UTF_8);
        assertTrue(html.contains("<script src=\"app.js\" defer></script>"));
        assertTrue(html.contains("<link rel=\"stylesheet\" href=\"styles.css\">"));
        assertFalse("no inline style attributes", html.contains("style=\""));
        assertFalse("no inline event handlers", html.matches("(?s).*\\son[a-z]+=\".*"));
        String js = new String(Files.readAllBytes(new File("web/public/app.js").toPath()), StandardCharsets.UTF_8);
        assertFalse("text is added with textContent, never innerHTML", js.contains(".innerHTML"));
        assertTrue(new File("web/public/styles.css").isFile());
    }
}
