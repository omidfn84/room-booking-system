package scheduler.web;

import static org.junit.Assert.*;
import static scheduler.web.WebTestClient.json;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import scheduler.accounts.TestChief;
import scheduler.web.WebTestClient.Response;

/**
 * End-to-end tests of the public demo: a real server is started on a free
 * port and driven over HTTP, exactly as a browser would drive it. Nothing is
 * faked, so these also exercise the facade, the managers and SQLite.
 *
 * All tests share one server. To stay independent, each test books its own
 * day (see {@link #day()}), so no test can take a room another test needs.
 */
public class WebDemoApiTest {

    private static WebApp app;
    private static int port;
    private static final AtomicInteger NEXT_DAY = new AtomicInteger(2);

    @BeforeClass
    public static void startServer() {
        WebApp.Config config = new WebApp.Config();
        config.port = 0;                              // any free port
        config.demo = true;
        config.chiefPassword = TestChief.password();  // the chief is a JVM-wide Singleton shared with other tests
        config.passwordAttemptsPerMinute = 100_000;   // the limit has its own test class; here it must not get in the way
        app = WebApp.start(config);
        port = app.port();
    }

    @AfterClass
    public static void stopServer() {
        app.stop();
    }

    // ---------------------------------------------------------------- helpers

    private static WebTestClient visitor() {
        return new WebTestClient(port);
    }

    /** A new visitor already signed in to one of the demo accounts. */
    private static WebTestClient signedIn(String account) throws Exception {
        WebTestClient client = visitor();
        assertEquals(200, client.post("/api/demo-login", json("account", account)).status);
        return client;
    }

    /** A future day (10:00) that no other test uses. */
    private static LocalDateTime day() {
        return LocalDateTime.now().plusDays(NEXT_DAY.getAndIncrement()).withHour(10).truncatedTo(ChronoUnit.HOURS);
    }

    private static String t(LocalDateTime time) {
        return time.truncatedTo(ChronoUnit.MINUTES).toString();
    }

    private static List<Map<String, Object>> search(WebTestClient client, LocalDateTime start, LocalDateTime end) throws Exception {
        Response r = client.get("/api/rooms/available?start=" + t(start) + "&end=" + t(end));
        assertEquals(r.text, 200, r.status);
        return r.list();
    }

    private static Response book(WebTestClient client, String roomId, LocalDateTime start, LocalDateTime end) throws Exception {
        return client.post("/api/bookings", json("roomId", roomId, "start", t(start), "end", t(end), "paymentMethod", "CREDIT_CARD"));
    }

    /** Books the first free room for the window and returns the new booking. */
    private static Map<String, Object> bookAny(WebTestClient client, LocalDateTime start, LocalDateTime end) throws Exception {
        String roomId = (String) search(client, start, end).get(0).get("roomId");
        Response r = book(client, roomId, start, end);
        assertEquals(r.text, 200, r.status);
        return r.object();
    }

    private static Map<String, Object> findBooking(WebTestClient client, Object bookingId) throws Exception {
        for (Map<String, Object> booking : client.get("/api/bookings").list()) {
            if (booking.get("bookingId").equals(bookingId)) {
                return booking;
            }
        }
        return null;
    }

    private static boolean hasRoom(List<Map<String, Object>> rooms, String roomId) {
        return rooms.stream().anyMatch(room -> room.get("roomId").equals(roomId));
    }

    private static Response action(WebTestClient client, Object bookingId, String action, String body) throws Exception {
        return client.post("/api/bookings/" + bookingId + "/" + action, body);
    }

    private static final String PAY = json("paymentMethod", "DEBIT_CARD");

    // ---------------------------------------------------------------- signed out

    @Test
    public void configDescribesTheDemo() throws Exception {
        Map<String, Object> config = visitor().get("/api/config").object();
        assertEquals(Boolean.TRUE, config.get("demo"));
        assertNotNull(LocalDateTime.parse((String) config.get("now")));
        assertEquals(List.of("CREDIT_CARD", "DEBIT_CARD", "INSTITUTIONAL_BILLING"), config.get("paymentMethods"));
        List<?> accounts = (List<?>) config.get("demoAccounts");
        assertEquals(3, accounts.size());
        Map<?, ?> student = (Map<?, ?>) accounts.get(0);
        assertEquals("student", student.get("key"));
        assertEquals(20L, student.get("hourlyRate"));   // comes from Student.getHourlyRate(), not from the web layer
    }

    @Test
    public void configNeverLeaksEmailsOrTheAdminId() throws Exception {
        String text = visitor().get("/api/config").text;
        assertFalse(text.contains("@"));
        assertFalse(text.contains("demo-admin"));
    }

    @Test
    public void aNewVisitorIsNotSignedIn() throws Exception {
        Response r = visitor().get("/api/me");
        assertEquals(200, r.status);
        assertEquals(Boolean.FALSE, r.object().get("authenticated"));
    }

    @Test
    public void everyUserAndAdminEndpointNeedsASession() throws Exception {
        WebTestClient anonymous = visitor();
        assertEquals(401, anonymous.get("/api/bookings").status);
        assertEquals(401, anonymous.get("/api/rooms/available?start=2030-01-01T10:00&end=2030-01-01T11:00").status);
        assertEquals(401, anonymous.post("/api/bookings", "{}").status);
        assertEquals(401, anonymous.post("/api/bookings/x/cancel", "{}").status);
        assertEquals(401, anonymous.get("/api/admin/rooms").status);
        assertEquals(401, anonymous.post("/api/admin/rooms", "{}").status);
        assertEquals(401, anonymous.post("/api/admin/rooms/BRG-213/disable", "{}").status);
    }

    // ---------------------------------------------------------------- signing in

    @Test
    public void demoLoginStartsASessionWithASafeCookie() throws Exception {
        WebTestClient client = visitor();
        Response r = client.post("/api/demo-login", json("account", "student"));
        assertEquals(200, r.status);
        assertEquals("user", r.object().get("role"));
        assertEquals("STUDENT", r.object().get("accountType"));

        String cookie = r.header("Set-Cookie");
        assertTrue(cookie, cookie.startsWith("sid="));
        assertTrue(cookie, cookie.contains("HttpOnly"));       // page scripts cannot read it
        assertTrue(cookie, cookie.contains("SameSite=Lax"));   // not sent on cross-site POSTs
        assertTrue("token should be long and random", client.sessionCookie().length() >= 40);

        assertEquals(Boolean.TRUE, client.get("/api/me").object().get("authenticated"));
    }

    @Test
    public void sessionTokensAreDifferentForEveryVisitor() throws Exception {
        assertNotEquals(signedIn("student").sessionCookie(), signedIn("student").sessionCookie());
    }

    @Test
    public void cookieIsMarkedSecureWhenTheHostServesHttps() throws Exception {
        // hosting platforms terminate https and pass the original scheme in this header
        java.net.http.HttpResponse<String> r = java.net.http.HttpClient.newHttpClient().send(
                java.net.http.HttpRequest.newBuilder(java.net.URI.create("http://localhost:" + port + "/api/demo-login"))
                        .header("Content-Type", "application/json").header("X-Forwarded-Proto", "https")
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json("account", "partner"))).build(),
                java.net.http.HttpResponse.BodyHandlers.ofString());
        assertTrue(r.headers().firstValue("Set-Cookie").get().contains("; Secure"));
    }

    @Test
    public void unknownDemoAccountIsRejected() throws Exception {
        assertEquals(400, visitor().post("/api/demo-login", json("account", "chief")).status);
        assertEquals(400, visitor().post("/api/demo-login", "{}").status);
    }

    @Test
    public void aDemoUserStartsWithOneBookingToCheckInAndOneToEdit() throws Exception {
        List<Map<String, Object>> bookings = signedIn("partner").get("/api/bookings").list();
        assertTrue(bookings.size() >= 2);
        String now = t(LocalDateTime.now().plusMinutes(1));
        assertTrue("one booking has started", bookings.stream().anyMatch(b -> ((String) b.get("start")).compareTo(now) < 0));
        assertTrue("one booking is in the future", bookings.stream().anyMatch(b -> ((String) b.get("start")).compareTo(now) > 0));
    }

    @Test
    public void signingInAgainDoesNotPileUpSampleBookings() throws Exception {
        int first = signedIn("faculty").get("/api/bookings").list().size();
        int second = signedIn("faculty").get("/api/bookings").list().size();
        assertEquals(first, second);
    }

    @Test
    public void logoutEndsTheSessionOnTheServer() throws Exception {
        WebTestClient client = signedIn("student");
        String token = client.sessionCookie();
        assertEquals(200, client.post("/api/logout", "{}").status);
        assertEquals(401, client.get("/api/bookings").status);

        // replaying the old token by hand must not work either
        java.net.http.HttpResponse<String> replay = java.net.http.HttpClient.newHttpClient().send(
                java.net.http.HttpRequest.newBuilder(java.net.URI.create("http://localhost:" + port + "/api/bookings"))
                        .header("Cookie", "sid=" + token).GET().build(),
                java.net.http.HttpResponse.BodyHandlers.ofString());
        assertEquals(401, replay.statusCode());
    }

    @Test
    public void aMadeUpSessionTokenIsNotAccepted() throws Exception {
        java.net.http.HttpResponse<String> r = java.net.http.HttpClient.newHttpClient().send(
                java.net.http.HttpRequest.newBuilder(java.net.URI.create("http://localhost:" + port + "/api/bookings"))
                        .header("Cookie", "sid=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA").GET().build(),
                java.net.http.HttpResponse.BodyHandlers.ofString());
        assertEquals(401, r.statusCode());
    }

    @Test
    public void aVisitorCanCreateTheirOwnAccountAndLogInOnTheDemoSite() throws Exception {
        WebTestClient client = visitor();
        Response register = client.post("/api/register", json("email", "visitor.one@example.com", "password", "Str0ng!Pass",
                "userName", "Visitor One", "accountType", "PARTNER", "organizationId", "123456789"));
        assertEquals(register.text, 200, register.status);
        assertEquals("their own account is not a demo session", Boolean.FALSE, register.object().get("demoSession"));
        assertFalse(register.object().containsKey("demoPayment"));
        assertEquals(200, client.post("/api/logout", "{}").status);

        assertEquals(401, client.post("/api/login", json("email", "visitor.one@example.com", "password", "Wrong!Pass1")).status);
        Response login = client.post("/api/login", json("email", "visitor.one@example.com", "password", "Str0ng!Pass"));
        assertEquals(login.text, 200, login.status);
        assertEquals("Visitor One", login.object().get("name"));
    }

    @Test
    public void theDemoAccountsCannotBeLoggedInToWithAPassword() throws Exception {
        // their passwords are random and never shown, so only the demo button opens them
        for (String guess : new String[] {"", "password", "Demo123!", "demo.student@yorku.ca"}) {
            assertEquals(401, visitor().post("/api/login", json("email", "demo.student@yorku.ca", "password", guess)).status);
        }
    }

    @Test
    public void meTellsThePageWhichSessionsAreDemoSessions() throws Exception {
        Map<String, Object> student = signedIn("student").get("/api/me").object();
        assertEquals(Boolean.TRUE, student.get("demoSession"));
        Map<?, ?> demoPayment = (Map<?, ?>) student.get("demoPayment");
        assertEquals("one entry per payment method", 3, demoPayment.size());
        assertEquals("4242424242424242", ((Map<?, ?>) demoPayment.get("CREDIT_CARD")).get("cardNumber"));
        assertEquals("DEMO-BILLING", ((Map<?, ?>) demoPayment.get("INSTITUTIONAL_BILLING")).get("billingAccount"));

        Map<String, Object> admin = signedIn("admin").get("/api/me").object();
        assertEquals(Boolean.TRUE, admin.get("demoSession"));
        assertFalse("an administrator pays for nothing", admin.containsKey("demoPayment"));
    }

    @Test
    public void someoneWithTheirOwnAccountMustTypeSensiblePaymentDetails() throws Exception {
        WebTestClient client = visitor();
        assertEquals(200, client.post("/api/register", json("email", "visitor.two@example.com", "password", "Str0ng!Pass",
                "userName", "Visitor Two", "accountType", "PARTNER", "organizationId", "123456789")).status);
        LocalDateTime start = day();
        String roomId = (String) search(client, start, start.plusHours(1)).get(0).get("roomId");

        Response none = client.post("/api/bookings", json("roomId", roomId, "start", t(start), "end", t(start.plusHours(1)), "paymentMethod", "CREDIT_CARD"));
        assertEquals("no card details at all", 400, none.status);
        assertEquals("Enter the card number (12 to 19 digits).", none.error());

        Response badExpiry = client.post("/api/bookings", json("roomId", roomId, "start", t(start), "end", t(start.plusHours(1)),
                "paymentMethod", "CREDIT_CARD", "cardNumber", "4242 4242 4242 4242", "expiryDate", "13/30", "cvv", "123"));
        assertEquals(400, badExpiry.status);
        assertEquals("Enter the expiry date as MM/YY.", badExpiry.error());
        assertEquals("nothing was booked by the rejected requests", 0, client.get("/api/bookings").list().size());

        Response ok = client.post("/api/bookings", json("roomId", roomId, "start", t(start), "end", t(start.plusHours(1)),
                "paymentMethod", "CREDIT_CARD", "cardNumber", "4242 4242 4242 4242", "expiryDate", "12/30", "cvv", "123"));
        assertEquals(ok.text, 200, ok.status);
        assertEquals("PAID", ok.object().get("depositStatus"));
        assertFalse("the card number is never sent back", ok.text.contains("4242"));
    }

    // ---------------------------------------------------------------- the booking flow

    @Test
    public void bookCheckInAndPay() throws Exception {
        WebTestClient student = signedIn("student");
        LocalDateTime start = LocalDateTime.now().minusMinutes(2);   // already started: inside the check-in window
        LocalDateTime end = start.plusHours(3);
        Map<String, Object> booking = bookAny(student, start, end);
        Object id = booking.get("bookingId");
        String roomId = (String) booking.get("roomId");

        assertEquals("CONFIRMED", booking.get("status"));
        assertEquals(20L, booking.get("deposit"));                   // one hour at the student rate (Req4)
        assertEquals(60L, booking.get("totalCost"));                 // 3 hours x $20
        assertEquals("PAID", booking.get("depositStatus"));
        assertFalse("a booked room leaves the search", hasRoom(search(student, start, end), roomId));

        Response checkIn = action(student, id, "check-in", "{}");
        assertEquals(checkIn.text, 200, checkIn.status);
        assertEquals("CHECKED_IN", checkIn.object().get("status"));
        assertEquals(40L, checkIn.object().get("remainingBalance")); // total minus the deposit
        assertNotNull(checkIn.object().get("checkInTime"));

        Response pay = action(student, id, "pay", PAY);
        assertEquals(pay.text, 200, pay.status);
        assertEquals(40L, pay.object().get("amountPaid"));
        assertEquals("COMPLETED", pay.object().get("status"));
        assertTrue("a completed booking frees the room", hasRoom(search(student, start, end), roomId));
    }

    @Test
    public void theDemoSuppliesPaymentDetailsItselfAndIgnoresAnyThatAreSent() throws Exception {
        WebTestClient student = signedIn("student");
        LocalDateTime start = day();
        String roomId = (String) search(student, start, start.plusHours(1)).get(0).get("roomId");
        // an empty card number would make the payment strategy fail if the server used it
        Response r = student.post("/api/bookings", json("roomId", roomId, "start", t(start), "end", t(start.plusHours(1)),
                "paymentMethod", "CREDIT_CARD", "cardNumber", "", "expiryDate", "", "cvv", ""));
        assertEquals(r.text, 200, r.status);
        assertEquals("PAID", r.object().get("depositStatus"));
    }

    @Test
    public void everyPaymentMethodWorks() throws Exception {
        WebTestClient partner = signedIn("partner");
        LocalDateTime start = day();
        int hour = 0;
        for (String method : new String[] {"CREDIT_CARD", "DEBIT_CARD", "INSTITUTIONAL_BILLING"}) {
            LocalDateTime from = start.plusHours(hour++);
            String roomId = (String) search(partner, from, from.plusHours(1)).get(0).get("roomId");
            Response r = partner.post("/api/bookings", json("roomId", roomId, "start", t(from), "end", t(from.plusHours(1)), "paymentMethod", method));
            assertEquals(method + ": " + r.text, 200, r.status);
            assertEquals(50L, r.object().get("deposit"));            // partner rate
        }
    }

    @Test
    public void unknownPaymentMethodIsRejected() throws Exception {
        WebTestClient student = signedIn("student");
        LocalDateTime start = day();
        Response r = student.post("/api/bookings", json("roomId", "BRG-213", "start", t(start), "end", t(start.plusHours(1)), "paymentMethod", "BITCOIN"));
        assertEquals(400, r.status);
    }

    @Test
    public void theSameRoomCannotBeBookedTwiceForOverlappingTimes() throws Exception {
        WebTestClient student = signedIn("student");
        WebTestClient faculty = signedIn("faculty");
        LocalDateTime start = day();
        Map<String, Object> first = bookAny(student, start, start.plusHours(2));
        Response second = book(faculty, (String) first.get("roomId"), start.plusHours(1), start.plusHours(3));
        assertEquals(409, second.status);
        assertTrue(second.error(), second.error().contains("already booked"));
    }

    @Test
    public void twentySimultaneousRequestsForOneRoomProduceExactlyOneBooking() throws Exception {
        LocalDateTime start = day();
        String body = json("roomId", "ACE-010", "start", t(start), "end", t(start.plusHours(1)), "paymentMethod", "CREDIT_CARD");
        List<WebTestClient> clients = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            clients.add(signedIn(i % 2 == 0 ? "student" : "faculty"));
        }
        ExecutorService pool = Executors.newFixedThreadPool(20);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<Integer>> results = new ArrayList<>();
        for (WebTestClient client : clients) {
            Callable<Integer> task = () -> {
                go.await();                                           // all threads start together
                return client.post("/api/bookings", body).status;
            };
            results.add(pool.submit(task));
        }
        go.countDown();
        int ok = 0;
        int conflict = 0;
        for (Future<Integer> result : results) {
            int status = result.get();
            if (status == 200) ok++;
            if (status == 409) conflict++;
        }
        pool.shutdown();
        assertEquals("exactly one request may win the room", 1, ok);
        assertEquals(19, conflict);
    }

    @Test
    public void heavyParallelUseNeverDoubleBooksARoomOrBreaksTheServer() throws Exception {
        // 12 visitors hammer the same 2 rooms and 12 time slots at once. The managers
        // keep plain ArrayLists, so without the web layer's request lock this produces
        // either a server error (a list changed while being read) or two bookings for
        // one room and time. Every answer must be 200 (booked) or 409 (already taken).
        // (A timing test cannot prove the lock correct: with the lock removed, this
        // test failed in 4 of 5 trial runs, so it catches the mistake often, not always.)
        LocalDateTime base = LocalDateTime.now().plusDays(60).withHour(8).truncatedTo(ChronoUnit.HOURS);
        String[] rooms = {"BRG-213", "LAS-1006"};
        String[] accounts = {"student", "faculty", "partner"};
        int visitors = 12;
        int attemptsEach = 24;
        List<WebTestClient> clients = new ArrayList<>();
        for (int i = 0; i < visitors; i++) {
            clients.add(signedIn(accounts[i % accounts.length]));
        }
        ExecutorService pool = Executors.newFixedThreadPool(visitors);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<List<Integer>>> results = new ArrayList<>();
        for (int v = 0; v < visitors; v++) {
            WebTestClient client = clients.get(v);
            int offset = v;
            Callable<List<Integer>> task = () -> {
                List<Integer> statuses = new ArrayList<>();
                go.await();
                for (int i = 0; i < attemptsEach; i++) {
                    int slot = (i + offset) % 12;                     // every slot is wanted by several visitors
                    LocalDateTime start = base.plusHours(slot);
                    statuses.add(book(client, rooms[i % rooms.length], start, start.plusHours(1)).status);
                    statuses.add(client.get("/api/bookings").status); // reading while others write
                }
                return statuses;
            };
            results.add(pool.submit(task));
        }
        go.countDown();
        int booked = 0;
        for (Future<List<Integer>> result : results) {
            List<Integer> statuses = result.get();
            for (int i = 0; i < statuses.size(); i++) {
                int status = statuses.get(i);
                if (i % 2 == 0) {
                    assertTrue("booking answered " + status, status == 200 || status == 409);
                    if (status == 200) booked++;
                } else {
                    assertEquals("listing bookings", 200, status);
                }
            }
        }
        pool.shutdown();

        // 2 rooms x 12 one-hour slots = 24 places, each of which was requested: all must be taken exactly once.
        assertEquals(24, booked);
        List<String> taken = new ArrayList<>();
        for (String account : accounts) {
            for (Map<String, Object> booking : signedIn(account).get("/api/bookings").list()) {
                if (((String) booking.get("start")).startsWith(t(base).substring(0, 10))) {
                    taken.add(booking.get("roomId") + " " + booking.get("start"));
                }
            }
        }
        assertEquals("one stored booking per place", 24, taken.size());
        assertEquals("no place booked twice", 24, new java.util.HashSet<>(taken).size());
    }

    @Test
    public void bookingAnUnknownRoomIsNotFound() throws Exception {
        LocalDateTime start = day();
        assertEquals(404, book(signedIn("student"), "NO-SUCH-ROOM", start, start.plusHours(1)).status);
    }

    @Test
    public void payingIsRefusedBeforeCheckIn() throws Exception {
        WebTestClient student = signedIn("student");
        LocalDateTime start = day();
        Object id = bookAny(student, start, start.plusHours(2)).get("bookingId");
        Response pay = action(student, id, "pay", PAY);
        assertEquals(409, pay.status);
        assertEquals("CONFIRMED", findBooking(student, id).get("status"));
    }

    @Test
    public void checkInIsRefusedBeforeTheStartTime() throws Exception {
        WebTestClient student = signedIn("student");
        LocalDateTime start = day();
        Object id = bookAny(student, start, start.plusHours(1)).get("bookingId");
        assertEquals(409, action(student, id, "check-in", "{}").status);
        assertEquals("CONFIRMED", findBooking(student, id).get("status"));
    }

    @Test
    public void cancelRefundsTheDepositAndFreesTheRoom() throws Exception {
        WebTestClient student = signedIn("student");
        LocalDateTime start = day();
        Map<String, Object> booking = bookAny(student, start, start.plusHours(1));
        Object id = booking.get("bookingId");

        Response cancel = action(student, id, "cancel", "{}");
        assertEquals(cancel.text, 200, cancel.status);
        assertEquals("CANCELLED", cancel.object().get("status"));
        assertEquals("REFUNDED", cancel.object().get("depositStatus"));
        assertTrue(hasRoom(search(student, start, start.plusHours(1)), (String) booking.get("roomId")));

        assertEquals("a cancelled booking cannot be cancelled again", 409, action(student, id, "cancel", "{}").status);
        assertEquals(409, action(student, id, "check-in", "{}").status);
    }

    @Test
    public void aStartedBookingCannotBeCancelledOrEdited() throws Exception {
        WebTestClient student = signedIn("student");
        LocalDateTime start = LocalDateTime.now().minusMinutes(3);
        Object id = bookAny(student, start, start.plusHours(1)).get("bookingId");
        assertEquals(409, action(student, id, "cancel", "{}").status);
        LocalDateTime later = day();
        assertEquals(409, action(student, id, "edit", json("start", t(later), "end", t(later.plusHours(1)))).status);
    }

    @Test
    public void editMovesABookingAndExtendLengthensIt() throws Exception {
        WebTestClient faculty = signedIn("faculty");
        LocalDateTime start = day();
        Object id = bookAny(faculty, start, start.plusHours(1)).get("bookingId");

        Response edit = action(faculty, id, "edit", json("start", t(start.plusHours(2)), "end", t(start.plusHours(4))));
        assertEquals(edit.text, 200, edit.status);
        assertEquals(t(start.plusHours(2)), edit.object().get("start"));
        assertEquals(60L, edit.object().get("totalCost"));            // 2 hours x $30

        Response extend = action(faculty, id, "extend", json("until", t(start.plusHours(5))));
        assertEquals(extend.text, 200, extend.status);
        assertEquals(t(start.plusHours(5)), extend.object().get("end"));

        assertEquals("the new end must be later", 409, action(faculty, id, "extend", json("until", t(start.plusHours(3)))).status);
    }

    @Test
    public void editAndExtendCannotOverlapSomeoneElsesBooking() throws Exception {
        WebTestClient student = signedIn("student");
        WebTestClient faculty = signedIn("faculty");
        LocalDateTime start = day();
        String roomId = (String) bookAny(student, start.plusHours(2), start.plusHours(3)).get("roomId");
        Response mine = book(faculty, roomId, start, start.plusHours(1));
        assertEquals(mine.text, 200, mine.status);
        Object id = mine.object().get("bookingId");

        assertEquals(409, action(faculty, id, "extend", json("until", t(start.plusHours(3)))).status);
        assertEquals(409, action(faculty, id, "edit", json("start", t(start.plusHours(2)), "end", t(start.plusHours(3)))).status);
        assertEquals(t(start.plusHours(1)), findBooking(faculty, id).get("end"));
    }

    // ---------------------------------------------------------------- one user cannot touch another's booking

    @Test
    public void anotherUsersBookingCannotBeSeenOrChanged() throws Exception {
        WebTestClient student = signedIn("student");
        WebTestClient faculty = signedIn("faculty");
        LocalDateTime start = day();
        Object id = bookAny(student, start, start.plusHours(1)).get("bookingId");

        assertNull("not listed for another user", findBooking(faculty, id));
        // every action answers 404, the same as for an id that does not exist
        assertEquals(404, action(faculty, id, "cancel", "{}").status);
        assertEquals(404, action(faculty, id, "check-in", "{}").status);
        assertEquals(404, action(faculty, id, "pay", PAY).status);
        assertEquals(404, action(faculty, id, "extend", json("until", t(start.plusHours(2)))).status);
        assertEquals(404, action(faculty, id, "edit", json("start", t(start.plusHours(3)), "end", t(start.plusHours(4)))).status);
        assertEquals(404, action(faculty, "00000000-0000-0000-0000-000000000000", "cancel", "{}").status);

        Map<String, Object> untouched = findBooking(student, id);
        assertEquals("CONFIRMED", untouched.get("status"));
        assertEquals(t(start), untouched.get("start"));
        assertEquals(t(start.plusHours(1)), untouched.get("end"));
    }

    @Test
    public void anAdministratorCannotBookAndAUserCannotAdminister() throws Exception {
        WebTestClient admin = signedIn("admin");
        WebTestClient student = signedIn("student");
        assertEquals("admin", admin.get("/api/me").object().get("role"));
        assertEquals(403, admin.get("/api/bookings").status);
        assertEquals(403, student.get("/api/admin/rooms").status);
        assertEquals(403, student.post("/api/admin/rooms/BRG-213/disable", "{}").status);
        assertEquals(403, student.post("/api/admin/rooms", json("roomId", "X-1", "building", "B", "roomNumber", "1", "capacity", 4)).status);
    }

    // ---------------------------------------------------------------- administrator

    @Test
    public void aRoomTakenOutOfServiceLeavesSearchAndStaysOutForTheNextLogIn() throws Exception {
        WebTestClient admin = signedIn("admin");
        WebTestClient student = signedIn("student");
        LocalDateTime start = day();
        assertTrue(hasRoom(search(student, start, start.plusHours(1)), "DB-1004"));

        Response off = admin.post("/api/admin/rooms/DB-1004/disable", "{}");
        assertEquals(200, off.status);
        assertEquals("DISABLED", off.object().get("status"));
        assertFalse(hasRoom(search(student, start, start.plusHours(1)), "DB-1004"));
        assertEquals("a disabled room cannot be booked directly either", 409, book(student, "DB-1004", start, start.plusHours(1)).status);

        Response closed = admin.post("/api/admin/rooms/DB-1004/close", "{}");
        assertEquals("MAINTENANCE", closed.object().get("status"));

        // Logging in again straight away must NOT bring the room back: this is how one
        // visitor switches a room off as administrator and then sees the effect as a student.
        // (The 10-minute automatic restore is tested with a fake clock in DemoDataTest.)
        WebTestClient nextLogIn = signedIn("faculty");
        assertFalse(hasRoom(search(nextLogIn, start, start.plusHours(1)), "DB-1004"));

        assertEquals("AVAILABLE", admin.post("/api/admin/rooms/DB-1004/enable", "{}").object().get("status"));
        assertTrue(hasRoom(search(nextLogIn, start, start.plusHours(1)), "DB-1004"));
    }

    @Test
    public void adminCanEnableARoomAgain() throws Exception {
        WebTestClient admin = signedIn("admin");
        assertEquals("DISABLED", admin.post("/api/admin/rooms/SCL-204/disable", "{}").object().get("status"));
        assertEquals("AVAILABLE", admin.post("/api/admin/rooms/SCL-204/enable", "{}").object().get("status"));
    }

    @Test
    public void adminAddsARoomAndItBecomesBookable() throws Exception {
        WebTestClient admin = signedIn("admin");
        Response add = admin.post("/api/admin/rooms", json("roomId", "VH-1005", "building", "Vari Hall", "roomNumber", "1005", "capacity", 25));
        assertEquals(add.text, 200, add.status);
        assertEquals("AVAILABLE", add.object().get("status"));
        assertTrue(hasRoom(admin.get("/api/admin/rooms").list(), "VH-1005"));

        LocalDateTime start = day();
        WebTestClient student = signedIn("student");
        assertTrue(hasRoom(search(student, start, start.plusHours(1)), "VH-1005"));
        assertEquals(200, book(student, "VH-1005", start, start.plusHours(1)).status);

        Response duplicate = admin.post("/api/admin/rooms", json("roomId", "VH-1005", "building", "Vari Hall", "roomNumber", "1005", "capacity", 25));
        assertEquals(409, duplicate.status);
    }

    @Test
    public void adminRoomInputIsValidated() throws Exception {
        WebTestClient admin = signedIn("admin");
        assertEquals(400, admin.post("/api/admin/rooms", json("roomId", "bad id!", "building", "B", "roomNumber", "1", "capacity", 4)).status);
        assertEquals(400, admin.post("/api/admin/rooms", json("roomId", "", "building", "B", "roomNumber", "1", "capacity", 4)).status);
        assertEquals(400, admin.post("/api/admin/rooms", json("roomId", "A".repeat(21), "building", "B", "roomNumber", "1", "capacity", 4)).status);
        assertEquals(400, admin.post("/api/admin/rooms", json("roomId", "OK-1", "building", " ", "roomNumber", "1", "capacity", 4)).status);
        assertEquals(400, admin.post("/api/admin/rooms", json("roomId", "OK-1", "building", "B".repeat(41), "roomNumber", "1", "capacity", 4)).status);
        assertEquals(400, admin.post("/api/admin/rooms", json("roomId", "OK-1", "building", "B", "roomNumber", "1", "capacity", 0)).status);
        assertEquals(400, admin.post("/api/admin/rooms", json("roomId", "OK-1", "building", "B", "roomNumber", "1", "capacity", 1001)).status);
        assertEquals(400, admin.post("/api/admin/rooms", json("roomId", "OK-1", "building", "B", "roomNumber", "1", "capacity", "many")).status);
        assertEquals(400, admin.post("/api/admin/rooms", json("roomId", "OK-1", "building", "B", "roomNumber", "1", "capacity", 2.5)).status);
        assertEquals(400, admin.post("/api/admin/rooms", json("roomId", "OK-1", "building", "B", "roomNumber", "1")).status);
        assertFalse("nothing was added by the rejected requests", hasRoom(admin.get("/api/admin/rooms").list(), "OK-1"));
    }

    @Test
    public void adminActionsOnUnknownRoomsOrActionsAreNotFound() throws Exception {
        WebTestClient admin = signedIn("admin");
        assertEquals(404, admin.post("/api/admin/rooms/NOPE/disable", "{}").status);
        assertEquals(404, admin.post("/api/admin/rooms/BRG-213/demolish", "{}").status);
    }

    @Test
    public void htmlInARoomNameComesBackAsEscapedText() throws Exception {
        WebTestClient admin = signedIn("admin");
        Response add = admin.post("/api/admin/rooms", json("roomId", "XSS-1", "building", "<img src=x onerror=alert(1)>", "roomNumber", "1", "capacity", 2));
        assertEquals(200, add.status);
        assertFalse("raw angle brackets never appear in a response", add.text.contains("<"));
        assertEquals("<img src=x onerror=alert(1)>", add.object().get("building"));   // the value itself is preserved
    }

    // ---------------------------------------------------------------- demo limits and bad input

    @Test
    public void demoRefusesUnreasonableTimes() throws Exception {
        WebTestClient student = signedIn("student");
        LocalDateTime now = LocalDateTime.now();
        assertEquals("started 2 hours ago", 400, book(student, "BRG-213", now.minusHours(2), now.plusHours(1)).status);
        assertEquals("13 hours long", 400, book(student, "BRG-213", now.plusDays(1), now.plusDays(1).plusHours(13)).status);
        assertEquals("200 days ahead", 400, book(student, "BRG-213", now.plusDays(200), now.plusDays(200).plusHours(1)).status);
        assertEquals("ends before it starts", 400, book(student, "BRG-213", now.plusDays(1), now.plusDays(1).minusHours(1)).status);
        assertEquals(400, student.post("/api/bookings", json("roomId", "BRG-213", "start", "tomorrow", "end", "later", "paymentMethod", "CREDIT_CARD")).status);
        assertEquals(400, student.get("/api/rooms/available?start=nonsense&end=2030-01-01T10:00").status);
        assertEquals(400, student.get("/api/rooms/available").status);
    }

    @Test
    public void extendCannotStretchABookingPastTheDemoLimit() throws Exception {
        WebTestClient student = signedIn("student");
        LocalDateTime start = day();
        Object id = bookAny(student, start, start.plusHours(1)).get("bookingId");
        assertEquals(400, action(student, id, "extend", json("until", t(start.plusHours(13)))).status);
    }

    @Test
    public void malformedRequestsGetClearErrors() throws Exception {
        WebTestClient student = signedIn("student");
        assertEquals("not JSON", 400, student.post("/api/bookings", "{not json").status);
        assertEquals("an array instead of an object", 400, student.post("/api/bookings", "[1,2]").status);
        assertEquals("missing fields", 400, student.post("/api/bookings", "{}").status);
        assertEquals("wrong field type", 400, student.post("/api/bookings", json("roomId", 5, "start", 1, "end", 2, "paymentMethod", 3)).status);
        assertEquals("form post", 415, student.postAs("application/x-www-form-urlencoded", "/api/logout", "a=b").status);
        assertEquals("plain text post", 415, student.postAs("text/plain", "/api/bookings", "{}").status);
        assertEquals("too large", 413, student.post("/api/bookings", json("roomId", "x".repeat(20_000))).status);
        assertEquals(404, student.get("/api/nothing-here").status);
        assertEquals(404, student.post("/api/bookings/some-id/explode", "{}").status);
        assertEquals(405, student.method("DELETE", "/api/bookings").status);
        assertEquals("still signed in after all of that", Boolean.TRUE, student.get("/api/me").object().get("authenticated"));
    }

    @Test
    public void errorsAreJsonAndNeverCached() throws Exception {
        Response r = visitor().get("/api/bookings");
        assertTrue(r.header("Content-Type").startsWith("application/json"));
        assertEquals("no-store", r.header("Cache-Control"));
        assertEquals("nosniff", r.header("X-Content-Type-Options"));
        assertNotNull(r.error());
    }
}
