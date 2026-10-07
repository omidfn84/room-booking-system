package scheduler.web;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import scheduler.accounts.RegisteredUser;
import scheduler.booking.Booking;
import scheduler.booking.BookingStatus;
import scheduler.booking.Payment;
import scheduler.booking.PaymentMethod;
import scheduler.facade.SchedulerFacade;
import scheduler.room.Room;
import scheduler.room.RoomStatus;
import scheduler.web.SessionStore.Session;

/**
 * The JSON API. Every request under /api/ arrives here.
 *
 * This class plays the same role for the browser that GUIController and
 * AdminController play for Swing: it only calls SchedulerFacade. What it adds
 * is what a network needs and a desktop window does not: sessions, checking
 * that a booking belongs to the person asking, and input limits.
 */
final class ApiHandler implements HttpHandler {

    private static final int MAX_BODY_BYTES = 16 * 1024;  // no request needs more than 16 KB
    private static final String COOKIE = "sid";           // name of the session cookie

    private final SchedulerFacade facade;
    private final SessionStore sessions;
    private final DemoData demo;      // null when the server is not in demo mode
    private final RateLimiter passwordLimiter; // caps log-in and sign-up attempts per minute
    private final int maxNewAccountsInDemo;    // how many accounts visitors may create on the demo site
    private int accountsCreated;      // how many accounts visitors have created since the server started

    // The managers keep plain ArrayLists and were written for a single-user GUI.
    // The server handles requests on several threads, so every call into the
    // facade is made while holding this lock: one request at a time.
    private final Object lock = new Object();

    ApiHandler(SchedulerFacade facade, SessionStore sessions, DemoData demo, RateLimiter passwordLimiter,
               int maxNewAccountsInDemo) {
        this.facade = facade;
        this.sessions = sessions;
        this.demo = demo;
        this.passwordLimiter = passwordLimiter;
        this.maxNewAccountsInDemo = maxNewAccountsInDemo;
    }

    // ============================================================ entry point

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            Request request = new Request(exchange);       // reads cookies, path, query and body
            Object result;
            synchronized (lock) {
                request.session = sessions.find(request.cookies.get(COOKIE));
                result = route(request);
            }
            send(exchange, 200, result);
        } catch (HttpError e) {
            sendError(exchange, e.status(), e.getMessage());
        } catch (NumberFormatException e) {
            sendError(exchange, 400, "A number in the request is not valid.");
        } catch (IllegalArgumentException e) {
            sendError(exchange, 400, e.getMessage());      // validation messages from the domain, e.g. "Email format is invalid."
        } catch (IllegalStateException e) {
            sendError(exchange, 409, e.getMessage());      // e.g. "Room is already booked during the set period of time."
        } catch (SecurityException e) {
            sendError(exchange, 403, e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();                           // full details go to the server log only
            sendError(exchange, 500, "Something went wrong on the server.");
        } finally {
            exchange.close();
        }
    }

    // Decides which method answers the request, based on the HTTP method and the path.
    private Object route(Request r) {
        String[] p = r.segments;                            // "/api/bookings/42/cancel" -> [bookings, 42, cancel]
        boolean get = r.method.equals("GET");
        boolean post = r.method.equals("POST");
        if (!get && !post) {
            throw new HttpError(405, "Only GET and POST are supported.");
        }

        if (p.length == 1) {
            if (get && p[0].equals("config")) return config();
            if (get && p[0].equals("me")) return me(r);
            if (post && p[0].equals("demo-login")) return demoLogin(r);
            if (post && p[0].equals("login")) return login(r);
            if (post && p[0].equals("register")) return register(r);
            if (post && p[0].equals("logout")) return logout(r);
            if (get && p[0].equals("bookings")) return myBookings(r);
            if (post && p[0].equals("bookings")) return createBooking(r);
        }
        if (p.length == 2 && get && p[0].equals("rooms") && p[1].equals("available")) {
            return availableRooms(r);
        }
        if (p.length == 3 && post && p[0].equals("bookings")) {
            return bookingAction(r, p[1], p[2]);
        }
        if (p.length == 2 && p[0].equals("admin") && p[1].equals("rooms")) {
            return get ? adminRooms(r) : adminAddRoom(r);
        }
        if (p.length == 4 && post && p[0].equals("admin") && p[1].equals("rooms")) {
            return adminRoomAction(r, p[2], p[3]);
        }
        throw new HttpError(404, "Not found.");
    }

    // ============================================================ public endpoints

    // GET /api/config - what the page needs to draw itself before anyone signs in.
    private Object config() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("demo", demo != null);
        out.put("zone", ZoneId.systemDefault().getId());   // all times in the system are in this zone
        out.put("now", LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).toString()); // the server's clock, not the browser's
        out.put("paymentMethods", Arrays.asList(PaymentMethod.values())); // a List, so it is written as a JSON array
        List<Object> accounts = new ArrayList<>();
        if (demo != null) {
            for (DemoData.Account a : demo.accounts()) {
                Map<String, Object> account = new LinkedHashMap<>();
                account.put("key", a.key);
                account.put("name", a.name);
                account.put("accountType", a.accountType);
                account.put("hourlyRate", a.hourlyRate);
                accounts.add(account);
            }
            out.put("maxBookingHours", DemoData.MAX_BOOKING_HOURS);
        }
        out.put("demoAccounts", accounts);
        return out;
    }

    // GET /api/me - who the cookie belongs to. Never an error: signed-out visitors get authenticated=false.
    private Object me(Request r) {
        Map<String, Object> out = new LinkedHashMap<>();
        Session s = r.session;
        out.put("authenticated", s != null);
        if (s != null) {
            out.put("role", s.isAdmin() ? "admin" : "user");
            out.put("name", s.name);
            out.put("email", s.email);
            out.put("accountType", s.accountType);
            out.put("hourlyRate", s.hourlyRate);
            out.put("demoSession", s.demoAccount);
            if (s.demoAccount && !s.isAdmin()) {
                // The made-up payment details, so the payment screen can show what the demo will "pay" with.
                Map<String, Object> demoPayment = new LinkedHashMap<>();
                for (PaymentMethod method : PaymentMethod.values()) {
                    demoPayment.put(method.name(), PaymentInput.demoDisplay(method, s.organizationId));
                }
                out.put("demoPayment", demoPayment);
            }
        }
        return out;
    }

    // POST /api/demo-login {"account":"student"} - one-click sign-in, demo mode only.
    private Object demoLogin(Request r) {
        if (demo == null) {
            throw new HttpError(403, "Demo accounts are switched off on this server.");
        }
        String key = r.text("account");
        Session session;
        if (key.equals("admin")) {
            session = sessions.createAdminSession(demo.adminId(), "Demo Admin");
            demo.refreshForNewVisitor();
        } else {
            DemoData.Account account = demo.find(key);
            if (account == null) {
                throw new HttpError(400, "Unknown demo account.");
            }
            demo.refreshForNewVisitor();
            demo.ensureSampleBookings(account);
            session = sessions.createDemoUserSession(account.email, account.name, account.accountType,
                    account.hourlyRate, account.organizationId);
        }
        return startSession(r, session);
    }

    // POST /api/login {"email":..., "password":...}
    private Object login(Request r) {
        limitPasswordWork();
        RegisteredUser user = facade.login(r.text("email"), r.text("password"));
        if (user == null) {
            // one message for "no such email" and "wrong password", so the site does not reveal who has an account
            throw new HttpError(401, "Email or password is incorrect.");
        }
        return startSession(r, sessionFor(user));
    }

    // POST /api/register {"email","password","userName","accountType","organizationId"}
    private Object register(Request r) {
        limitPasswordWork();
        if (demo != null && accountsCreated >= maxNewAccountsInDemo) {
            throw new HttpError(409, "This demo site has reached its limit of new accounts. Use a demo account instead.");
        }
        RegisteredUser user = facade.createAccount(r.text("email"), r.text("password"), r.text("accountType"),
                r.text("userName"), r.text("organizationId"));  // the account subsystem validates everything
        accountsCreated++;
        return startSession(r, sessionFor(user));
    }

    // Checking or hashing a password is deliberately slow, so the number of
    // log-ins and sign-ups per minute is capped. The one-click demo accounts are
    // not affected: they never involve a password.
    private void limitPasswordWork() {
        if (!passwordLimiter.tryAcquire()) {
            throw new HttpError(429, "Too many log-in attempts right now. Wait a minute and try again.");
        }
    }

    private Session sessionFor(RegisteredUser user) {
        return sessions.createUserSession(user.getEmail(), user.getUserName(), user.getAccountType(),
                user.getHourlyRate(), user.getOrganizationId());
    }

    // Replaces any earlier session of this browser and sends the new cookie.
    private Object startSession(Request r, Session session) {
        if (r.session != null) {
            sessions.remove(r.session.token);
        }
        r.session = session;
        r.setSessionCookie(session.token, SessionStore.IDLE_MILLIS / 1000);
        return me(r);
    }

    // POST /api/logout
    private Object logout(Request r) {
        if (r.session != null) {
            sessions.remove(r.session.token);
        }
        r.session = null;
        r.setSessionCookie("", 0);                         // Max-Age=0 tells the browser to delete the cookie
        return me(r);
    }

    // ============================================================ user endpoints

    // GET /api/rooms/available?start=...&end=...   (Req3)
    private Object availableRooms(Request r) {
        requireUser(r);
        String start = r.queryText("start");
        String end = r.queryText("end");
        checkTimes(start, end);
        List<Object> out = new ArrayList<>();
        for (Room room : facade.getBookableRooms(start, end)) {
            out.add(roomJson(room));
        }
        return out;
    }

    // GET /api/bookings - the signed-in user's own bookings, newest start time first.
    private Object myBookings(Request r) {
        Session s = requireUser(r);
        expireNoShows(s.email);
        List<Booking> mine = new ArrayList<>();
        for (Booking booking : facade.getAllBookings()) {
            if (booking.getUserEmail().equals(s.email)) {
                mine.add(booking);
            }
        }
        mine.sort(Comparator.comparing(Booking::getStartTime).reversed()); // ISO date strings sort correctly as text
        List<Object> out = new ArrayList<>();
        for (Booking booking : mine) {
            out.add(bookingJson(booking));
        }
        return out;
    }

    // Req4 says a booking that is not checked in to within 30 minutes expires and
    // loses its deposit. BookingManager applies that rule when check-in is
    // attempted, so a late attempt is made here on the user's behalf: the list
    // then shows EXPIRED instead of a stale CONFIRMED.
    private void expireNoShows(String email) {
        LocalDateTime now = LocalDateTime.now();
        for (Booking booking : facade.getAllBookings()) {
            if (!booking.getUserEmail().equals(email) || booking.getStatus() != BookingStatus.CONFIRMED) {
                continue;
            }
            LocalDateTime start;
            try {
                start = LocalDateTime.parse(booking.getStartTime());
            } catch (DateTimeParseException unreadable) {
                continue;                                   // leave rows with a damaged start time alone
            }
            if (start.plusMinutes(30).isBefore(now)) {
                facade.checkIn(email, booking.getBookingId()); // past the deadline: this expires the booking
            }
        }
    }

    // POST /api/bookings {"roomId","start","end","paymentMethod", ...payment fields}   (Req3, Req4, Req10)
    private Object createBooking(Request r) {
        Session s = requireUser(r);
        String start = r.text("start");
        String end = r.text("end");
        checkTimes(start, end);
        if (demo != null && facade.getAllBookings().size() >= DemoData.MAX_BOOKINGS) {
            throw new HttpError(409, "The demo has reached its booking limit. It is cleared when the server restarts.");
        }
        String roomId = r.text("roomId");
        if (facade.getAllRooms().stream().noneMatch(room -> room.getRoomId().equals(roomId))) {
            throw new HttpError(404, "Room not found.");
        }
        PaymentMethod method = paymentMethod(r);
        Booking booking = facade.bookRoom(s.email, roomId, start, end, method, paymentFields(r, s, method));
        return bookingJson(booking);
    }

    // POST /api/bookings/{id}/{action}   (Req4, Req5, Req8, Req9, Req10)
    private Object bookingAction(Request r, String bookingId, String action) {
        Session s = requireUser(r);
        Booking booking = ownBooking(s, bookingId);
        switch (action) {
            case "check-in":
                if (!facade.checkIn(s.email, bookingId)) {
                    throw new HttpError(409, "Check-in was refused. It opens at the start time and closes 30 minutes later; "
                            + "after that the booking expires and the deposit is kept.");
                }
                break;
            case "cancel":
                if (!facade.cancelBooking(bookingId)) {
                    throw new HttpError(409, "This booking can no longer be cancelled. Cancelling is only possible before the start time.");
                }
                break;
            case "edit": {
                String start = r.text("start");
                String end = r.text("end");
                checkTimes(start, end);
                if (!facade.editBooking(bookingId, start, end)) {
                    throw new HttpError(409, "The booking was not changed. Edits are only possible before the start time, "
                            + "and the room must be free for the new time.");
                }
                break;
            }
            case "extend": {
                String until = r.text("until");
                LocalDateTime newEnd = parseTime(until);
                LocalDateTime start = parseTime(booking.getStartTime());
                if (demo != null && Duration.between(start, newEnd).toMinutes() > DemoData.MAX_BOOKING_HOURS * 60L) {
                    throw new HttpError(400, "In the demo, a booking can be at most " + DemoData.MAX_BOOKING_HOURS + " hours long.");
                }
                if (!facade.extendBooking(bookingId, until)) {
                    throw new HttpError(409, "The booking was not extended. The new end must be later than the current one, "
                            + "the booking must not have ended, and the room must be free for the extra time.");
                }
                break;
            }
            case "pay": {
                // The deposit is only applied to the bill at check-in (Req4), so the
                // balance is paid after check-in. The page offers Pay only then; this
                // check makes the server enforce it too.
                if (booking.getStatus() != BookingStatus.CHECKED_IN) {
                    throw new HttpError(409, "Check in first: the remaining balance is paid after check-in.");
                }
                PaymentMethod method = paymentMethod(r);
                double paid = facade.payForBooking(bookingId, method, paymentFields(r, s, method));
                if (paid < 0) {
                    throw new HttpError(409, "The payment was rejected.");
                }
                Map<String, Object> out = bookingJson(ownBooking(s, bookingId));
                out.put("amountPaid", paid);
                return out;
            }
            default:
                throw new HttpError(404, "Not found.");
        }
        return bookingJson(ownBooking(s, bookingId));      // send back the booking in its new state
    }

    // Finds a booking by id, but only if it belongs to the signed-in user.
    // Someone else's booking gets the same 404 as a missing one, so ids cannot be probed.
    private Booking ownBooking(Session s, String bookingId) {
        for (Booking booking : facade.getAllBookings()) {
            if (booking.getBookingId().equals(bookingId) && booking.getUserEmail().equals(s.email)) {
                return booking;
            }
        }
        throw new HttpError(404, "Booking not found.");
    }

    private PaymentMethod paymentMethod(Request r) {
        String name = r.text("paymentMethod");
        try {
            return PaymentMethod.valueOf(name);
        } catch (IllegalArgumentException unknown) {
            throw new HttpError(400, "Unknown payment method.");
        }
    }

    // The payment values handed to the facade, in the order it documents.
    // PaymentInput decides whether the typed details, or the demo's made-up ones, are used.
    private String[] paymentFields(Request r, Session s, PaymentMethod method) {
        return PaymentInput.forFacade(demo != null, s.demoAccount, method, r.body, s.organizationId);
    }

    // ============================================================ admin endpoints (Req6, Req7)

    // GET /api/admin/rooms - every room, whatever its status.
    private Object adminRooms(Request r) {
        requireAdmin(r);
        List<Object> out = new ArrayList<>();
        for (Room room : facade.getAllRooms()) {
            out.add(roomJson(room));
        }
        return out;
    }

    // POST /api/admin/rooms {"roomId","capacity","building","roomNumber"}
    private Object adminAddRoom(Request r) {
        Session s = requireAdmin(r);
        String roomId = r.text("roomId").trim();
        String building = r.text("building").trim();
        String roomNumber = r.text("roomNumber").trim();
        long capacity = r.wholeNumber("capacity");
        if (!roomId.matches("[A-Za-z0-9_-]{1,20}")) {
            throw new HttpError(400, "Room ID must be 1 to 20 letters, digits, '-' or '_'.");
        }
        if (!isShortLabel(building) || !isShortLabel(roomNumber)) {
            throw new HttpError(400, "Building and room number are required (40 characters at most).");
        }
        if (capacity < 1 || capacity > 1000) {
            throw new HttpError(400, "Capacity must be between 1 and 1000.");
        }
        if (demo != null && facade.getAllRooms().size() >= DemoData.MAX_ROOMS) {
            throw new HttpError(409, "The demo has reached its room limit.");
        }
        Room room = new Room(roomId, (int) capacity, building, roomNumber, RoomStatus.AVAILABLE);
        if (!facade.addRoom(s.adminId, room)) {
            throw new HttpError(409, "A room with that ID already exists.");
        }
        return roomJson(room);
    }

    // true for 1 to 40 characters with no control characters (tabs, new lines, ...)
    private static boolean isShortLabel(String value) {
        return !value.isEmpty() && value.length() <= 40 && value.chars().noneMatch(Character::isISOControl);
    }

    // POST /api/admin/rooms/{roomId}/{enable|disable|close}
    private Object adminRoomAction(Request r, String roomId, String action) {
        Session s = requireAdmin(r);
        boolean ok;
        switch (action) {
            case "enable":  ok = facade.enableRoom(s.adminId, roomId);  break;
            case "disable": ok = facade.disableRoom(s.adminId, roomId); break;
            case "close":   ok = facade.closeRoom(s.adminId, roomId);   break;
            default: throw new HttpError(404, "Not found.");
        }
        if (!ok) {
            throw new HttpError(404, "Room not found.");
        }
        if (demo != null) {
            demo.noteRoomStatus(roomId, action.equals("enable")); // the demo restores sample rooms after a while
        }
        for (Room room : facade.getAllRooms()) {
            if (room.getRoomId().equals(roomId)) {
                return roomJson(room);
            }
        }
        throw new HttpError(404, "Room not found.");
    }

    // ============================================================ guards

    private Session requireUser(Request r) {
        if (r.session == null) {
            throw new HttpError(401, "Please log in first.");
        }
        if (r.session.isAdmin()) {
            throw new HttpError(403, "Administrators manage rooms; log in as a user to book one.");
        }
        return r.session;
    }

    private Session requireAdmin(Request r) {
        if (r.session == null) {
            throw new HttpError(401, "Please log in first.");
        }
        // The session says "admin", and the chief (through the facade) must agree.
        if (!r.session.isAdmin() || !facade.isAdministrator(r.session.adminId)) {
            throw new HttpError(403, "Administrator access is required.");
        }
        return r.session;
    }

    // Checks that both times are readable and in order; in demo mode also that they are reasonable.
    private void checkTimes(String start, String end) {
        LocalDateTime from = parseTime(start);
        LocalDateTime to = parseTime(end);
        if (!from.isBefore(to)) {
            throw new HttpError(400, "The end time must be after the start time.");
        }
        if (demo == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        if (from.isBefore(now.minusMinutes(DemoData.PAST_GRACE_MINUTES))) {
            throw new HttpError(400, "In the demo, a booking cannot start more than "
                    + DemoData.PAST_GRACE_MINUTES + " minutes in the past.");
        }
        if (from.isAfter(now.plusDays(DemoData.MAX_DAYS_AHEAD))) {
            throw new HttpError(400, "In the demo, a booking must start within the next " + DemoData.MAX_DAYS_AHEAD + " days.");
        }
        if (Duration.between(from, to).toMinutes() > DemoData.MAX_BOOKING_HOURS * 60L) {
            throw new HttpError(400, "In the demo, a booking can be at most " + DemoData.MAX_BOOKING_HOURS + " hours long.");
        }
    }

    private static LocalDateTime parseTime(String value) {
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            throw new HttpError(400, "Times must look like 2026-10-07T14:30.");
        }
    }

    // ============================================================ JSON shapes

    private static Map<String, Object> roomJson(Room room) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("roomId", room.getRoomId());
        out.put("building", room.getBuilding());
        out.put("roomNumber", room.getRoomNumber());
        out.put("capacity", room.getCapacity());
        out.put("status", room.getStatus());
        return out;
    }

    private Map<String, Object> bookingJson(Booking booking) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("bookingId", booking.getBookingId());
        out.put("roomId", booking.getRoomId());
        for (Room room : facade.getAllRooms()) {            // add the room's location for display
            if (room.getRoomId().equals(booking.getRoomId())) {
                out.put("building", room.getBuilding());
                out.put("roomNumber", room.getRoomNumber());
            }
        }
        out.put("start", booking.getStartTime());
        out.put("end", booking.getEndTime());
        out.put("status", booking.getStatus());
        out.put("deposit", booking.getDepositAmount());
        out.put("depositForfeited", booking.isDepositForfeited());
        out.put("checkInTime", booking.getCheckInTime());
        out.put("totalCost", booking.calculateFinalCost());
        out.put("remainingBalance", booking.calculateRemainingBalance());
        Payment deposit = facade.findPaymentByBookingId(booking.getBookingId()); // the first payment of a booking is its deposit
        out.put("depositStatus", deposit == null ? null : deposit.getStatus());
        return out;
    }

    // ============================================================ sending

    private static void send(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = Json.write(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");            // answers depend on who is signed in
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");    // browsers must not guess another type
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
    }

    private static void sendError(HttpExchange exchange, int status, String message) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", message == null ? "Request failed." : message);
        send(exchange, status, body);
    }

    // ============================================================ one request

    /** The parts of an HTTP request the API uses, read once when the request arrives. */
    private static final class Request {
        final HttpExchange exchange;
        final String method;
        final String[] segments;                           // path after /api/, split on '/'
        final Map<String, String> query = new HashMap<>();
        final Map<String, String> cookies = new HashMap<>();
        final Map<String, Object> body;                    // parsed JSON body; empty for GET
        Session session;                                   // filled in by handle()

        Request(HttpExchange exchange) throws IOException {
            this.exchange = exchange;
            this.method = exchange.getRequestMethod();

            String path = exchange.getRequestURI().getPath();          // already URL-decoded, e.g. /api/bookings
            String rest = path.substring(Math.min(path.length(), "/api/".length()));
            this.segments = rest.isEmpty() ? new String[0] : rest.split("/", -1);

            String rawQuery = exchange.getRequestURI().getRawQuery();  // e.g. start=2026-10-07T10%3A00&end=...
            if (rawQuery != null) {
                for (String pair : rawQuery.split("&")) {
                    int eq = pair.indexOf('=');
                    if (eq > 0) {
                        query.put(decode(pair.substring(0, eq)), decode(pair.substring(eq + 1)));
                    }
                }
            }

            List<String> cookieHeaders = exchange.getRequestHeaders().get("Cookie");
            if (cookieHeaders != null) {
                for (String header : cookieHeaders) {
                    for (String pair : header.split(";")) {             // "sid=abc; theme=dark"
                        int eq = pair.indexOf('=');
                        if (eq > 0) {
                            cookies.put(pair.substring(0, eq).trim(), pair.substring(eq + 1).trim());
                        }
                    }
                }
            }

            this.body = method.equals("POST") ? readJsonBody() : new HashMap<>();
        }

        private static String decode(String value) {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        }

        private Map<String, Object> readJsonBody() throws IOException {
            // Requiring a JSON content type also protects against cross-site form
            // posts: a plain HTML form on another site cannot send this header.
            String type = exchange.getRequestHeaders().getFirst("Content-Type");
            if (type == null || !type.toLowerCase().startsWith("application/json")) {
                throw new HttpError(415, "Send the request body as application/json.");
            }
            byte[] bytes;
            try (InputStream in = exchange.getRequestBody()) {
                bytes = in.readNBytes(MAX_BODY_BYTES + 1);              // read one byte more than allowed...
            }
            if (bytes.length > MAX_BODY_BYTES) {                        // ...so an oversized body is detected
                throw new HttpError(413, "The request is too large.");
            }
            String text = new String(bytes, StandardCharsets.UTF_8);
            return text.isBlank() ? new HashMap<>() : Json.parseObject(text);
        }

        /** A required text field of the JSON body. */
        String text(String field) {
            Object value = body.get(field);
            if (!(value instanceof String)) {
                throw new HttpError(400, "Missing field: " + field + ".");
            }
            return (String) value;
        }

        /** A required whole-number field of the JSON body; accepts 12 and "12". */
        long wholeNumber(String field) {
            Object value = body.get(field);
            if (value instanceof Long) {
                return (Long) value;
            }
            if (value instanceof String) {
                return Long.parseLong(((String) value).trim());        // a NumberFormatException becomes a 400
            }
            throw new HttpError(400, "Field " + field + " must be a whole number.");
        }

        /** A required query-string value, e.g. ?start=... */
        String queryText(String name) {
            String value = query.get(name);
            if (value == null) {
                throw new HttpError(400, "Missing query parameter: " + name + ".");
            }
            return value;
        }

        void setSessionCookie(String token, long maxAgeSeconds) {
            // HttpOnly: page scripts cannot read the token. SameSite=Lax: other sites cannot
            // make the browser send it with a POST. Secure: only over https (when the host uses it).
            String cookie = COOKIE + "=" + token + "; Path=/; HttpOnly; SameSite=Lax; Max-Age=" + maxAgeSeconds;
            if ("https".equalsIgnoreCase(exchange.getRequestHeaders().getFirst("X-Forwarded-Proto"))) {
                cookie += "; Secure";
            }
            exchange.getResponseHeaders().add("Set-Cookie", cookie);
        }
    }
}
