package scheduler.web;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.LongSupplier;

import scheduler.accounts.Administrator;
import scheduler.accounts.RegisteredUser;
import scheduler.booking.Booking;
import scheduler.booking.BookingStatus;
import scheduler.booking.PaymentMethod;
import scheduler.facade.SchedulerFacade;
import scheduler.room.Room;
import scheduler.room.RoomStatus;

final class DemoData {

    /** A demo account a visitor can sign in to with one click. */
    static final class Account {
        final String key;            // short name used by the page, e.g. "student"
        final String email;
        final String name;
        final String accountType;    // STUDENT / FACULTY / PARTNER
        final double hourlyRate;     // taken from the real user object, never hard-coded here
        final long organizationId;

        Account(String key, RegisteredUser user) {
            this.key = key;
            this.email = user.getEmail();
            this.name = user.getUserName();
            this.accountType = user.getAccountType();
            this.hourlyRate = user.getHourlyRate();
            this.organizationId = user.getOrganizationId();
        }
    }

    // Limits for the public demo. They are web-layer guards, not business rules.
    static final int MAX_BOOKINGS = 500;       // total bookings the demo database may hold
    static final int MAX_ROOMS = 40;           // total rooms the demo database may hold
    static final long ROOM_RESTORE_MILLIS = 10 * 60 * 1000L; // a sample room switched off comes back after 10 minutes
    static final int MAX_BOOKING_HOURS = 12;   // longest booking a visitor can make
    static final int MAX_DAYS_AHEAD = 90;      // how far in the future a booking may start
    static final int PAST_GRACE_MINUTES = 30;  // a booking may start up to 30 minutes ago (so check-in can be tried at once)

    private static final String DEMO_ORGANIZATION_ID = "100000001"; // any 9 digits pass the account rules

    private final SchedulerFacade facade;
    private final List<Account> accounts = new ArrayList<>();
    private final List<String> seededRoomIds = new ArrayList<>();
    private final String adminId;
    // roomId -> the time (in milliseconds) a sample room was taken out of service by the demo administrator
    private final Map<String, Long> outOfServiceSince = new HashMap<>();
    LongSupplier clock = System::currentTimeMillis;   // where "now" comes from; a test replaces it with a fake clock

    DemoData(SchedulerFacade facade, String chiefPassword) {
        this.facade = facade;
        SecureRandom random = new SecureRandom();

        // The chief keeps one registry of administrators for the whole JVM, so the
        // id gets a random suffix to stay unique even if two servers start in one JVM (tests do this).
        this.adminId = "demo-admin-" + Long.toHexString(random.nextLong());
        Administrator admin = facade.generateAdministratorAccount(chiefPassword, adminId, "Demo Admin", "demo.admin@example.com");
        if (admin == null) {
            throw new IllegalStateException("Could not create the demo administrator.");
        }

        // Sample rooms, added through the administrator like any real room (Req7).
        addRoom("BRG-213", 8, "Bergeron Centre", "213");
        addRoom("BRG-313", 12, "Bergeron Centre", "313");
        addRoom("LAS-1006", 20, "Lassonde Building", "1006");
        addRoom("SCL-204", 4, "Scott Library", "204");
        addRoom("ACE-010", 30, "Accolade East", "010");
        addRoom("DB-1004", 6, "Dahdaleh Building", "1004");

        // Demo users. Nobody signs in with these passwords (the demo button signs in
        // directly), so each gets a random one that is never shown or stored in plain text.
        accounts.add(createUser("student", "demo.student@yorku.ca", "STUDENT", "Demo Student", random));
        accounts.add(createUser("faculty", "demo.faculty@yorku.ca", "FACULTY", "Demo Faculty", random));
        accounts.add(createUser("partner", "demo.partner@example.com", "PARTNER", "Demo Partner", random));
    }

    private void addRoom(String roomId, int capacity, String building, String roomNumber) {
        facade.addRoom(adminId, new Room(roomId, capacity, building, roomNumber, RoomStatus.AVAILABLE));
        seededRoomIds.add(roomId);
    }

    private Account createUser(String key, String email, String type, String name, SecureRandom random) {
        // "Aa1!" guarantees the upper/lower/digit/symbol rules; the hex part makes it unguessable.
        String password = "Aa1!" + Long.toHexString(random.nextLong()) + Long.toHexString(random.nextLong());
        RegisteredUser user = facade.createAccount(email, password, type, name, DEMO_ORGANIZATION_ID);
        return new Account(key, user);
    }

    List<Account> accounts() {
        return Collections.unmodifiableList(accounts);
    }

    String adminId() {
        return adminId;
    }

    /** Finds a demo account by its short name, or returns null. */
    Account find(String key) {
        for (Account account : accounts) {
            if (account.key.equals(key)) {
                return account;
            }
        }
        return null;
    }

    /**
     * Called after the demo administrator changes a room's status, so the demo
     * knows how long each sample room has been out of service.
     */
    void noteRoomStatus(String roomId, boolean inService) {
        if (!seededRoomIds.contains(roomId)) {
            return;                                      // rooms a visitor added are left alone
        }
        if (inService) {
            outOfServiceSince.remove(roomId);
        } else {
            outOfServiceSince.putIfAbsent(roomId, clock.getAsLong()); // keep the time it FIRST went out of service
        }
    }

    /**
     * Called whenever someone starts a demo session. Sample rooms that have been
     * out of service for 10 minutes are put back, so a later visitor never finds
     * an empty demo. A room switched off a moment ago stays off, so the visitor
     * who did it can log in as a student and see that it has left the search.
     */
    void refreshForNewVisitor() {
        long now = clock.getAsLong();
        for (Iterator<Map.Entry<String, Long>> it = outOfServiceSince.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, Long> entry = it.next();
            if (now - entry.getValue() >= ROOM_RESTORE_MILLIS) {
                facade.enableRoom(adminId, entry.getKey());
                it.remove();
            }
        }
    }

    /**
     * Gives a demo user something to look at: one booking that can be checked in
     * to right now and one for tomorrow that can be edited or cancelled. Nothing
     * is added while the user still has a live booking.
     */
    void ensureSampleBookings(Account account) {
        LocalDateTime now = LocalDateTime.now();
        int total = 0;
        for (Booking booking : facade.getAllBookings()) {
            total++;
            boolean mine = booking.getUserEmail().equals(account.email);
            boolean active = booking.getStatus() == BookingStatus.CONFIRMED || booking.getStatus() == BookingStatus.CHECKED_IN;
            if (mine && active && LocalDateTime.parse(booking.getEndTime()).isAfter(now)) {
                return; // the user already has a live booking
            }
        }
        if (total + 2 > MAX_BOOKINGS) {
            return; // the demo is full; do not add more
        }
        // Started 5 minutes ago: inside the 30-minute check-in window (Req4).
        LocalDateTime startedJustNow = now.minusMinutes(5).truncatedTo(ChronoUnit.MINUTES);
        // Two hours long, so a balance is left to pay after the one-hour deposit is applied (Req4, Req10).
        bookFirstFreeRoom(account, startedJustNow, startedJustNow.plusHours(2));
        // Tomorrow at 10:00: still in the future, so edit and cancel are allowed (Req8).
        LocalDateTime tomorrow = now.plusDays(1).withHour(10).truncatedTo(ChronoUnit.HOURS);
        bookFirstFreeRoom(account, tomorrow, tomorrow.plusHours(1));
    }

    private void bookFirstFreeRoom(Account account, LocalDateTime start, LocalDateTime end) {
        List<Room> free = facade.getBookableRooms(start.toString(), end.toString());
        if (free.isEmpty()) {
            return; // every room is taken for that time; skip the sample booking
        }
        facade.bookRoom(account.email, free.get(0).getRoomId(), start.toString(), end.toString(),
                PaymentMethod.CREDIT_CARD, paymentFields(PaymentMethod.CREDIT_CARD, account.organizationId));
    }

    /**
     * The made-up payment details the demo site hands to the facade, in the order
     * SchedulerFacade.bookRoom expects for each method. PaymentInput decides when
     * they are used: always for demo accounts, and on the demo site also in place
     * of whatever a person with their own account typed.
     */
    static String[] paymentFields(PaymentMethod method, long organizationId) {
        switch (method) {
            case CREDIT_CARD:
                return new String[] {"4242424242424242", "12/30", "123"};   // cardNumber, expiryDate, cvv
            case DEBIT_CARD:
                return new String[] {"4242424242424242", "0000"};           // cardNumber, pin
            case INSTITUTIONAL_BILLING:
                return new String[] {String.valueOf(organizationId), "DEMO-BILLING"}; // organizationId, billingAccount
            default:
                throw new IllegalArgumentException("Unsupported payment method: " + method);
        }
    }
}
