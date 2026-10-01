package com.group10.scheduler.aisupport;

import java.util.ArrayList;
import java.util.List;

import com.group10.scheduler.accounts.RegisteredUser;
import com.group10.scheduler.booking.Booking;
import com.group10.scheduler.booking.Payment;
import com.group10.scheduler.persistence.BookingRepository;
import com.group10.scheduler.persistence.PaymentRepository;
import com.group10.scheduler.persistence.RoomRepository;
import com.group10.scheduler.persistence.UserRepository;
import com.group10.scheduler.room.Room;

/**
 * In-memory test doubles for the four «Target» repository interfaces.
 *
 * The AI test suite is kept deliberately self-contained: it declares its own
 * fakes rather than reusing the ones in the manually written suite, so that
 * test-ai/ can be compiled, run, and measured for coverage completely
 * independently of test/. Each fake also records how many times it was asked
 * to save, which lets tests assert that a manager actually persisted a change
 * rather than only mutating its in-memory list.
 */
public final class AIFakes {

    private AIFakes() {
    }

    public static class FakeRoomRepo implements RoomRepository {
        public List<Room> saved = new ArrayList<>();
        public int saveCount = 0;
        private final List<Room> initial;

        public FakeRoomRepo() {
            this(new ArrayList<>());
        }

        public FakeRoomRepo(List<Room> initial) {
            this.initial = new ArrayList<>(initial);
        }

        @Override
        public List<Room> loadRooms() {
            return new ArrayList<>(initial);
        }

        @Override
        public void saveRooms(List<Room> rooms) {
            saved = new ArrayList<>(rooms);
            saveCount++;
        }
    }

    public static class FakeBookingRepo implements BookingRepository {
        public List<Booking> saved = new ArrayList<>();
        public int saveCount = 0;
        private final List<Booking> initial;

        public FakeBookingRepo() {
            this(new ArrayList<>());
        }

        public FakeBookingRepo(List<Booking> initial) {
            this.initial = new ArrayList<>(initial);
        }

        @Override
        public List<Booking> loadBookings() {
            return new ArrayList<>(initial);
        }

        @Override
        public void saveBookings(List<Booking> bookings) {
            saved = new ArrayList<>(bookings);
            saveCount++;
        }
    }

    public static class FakePaymentRepo implements PaymentRepository {
        public List<Payment> saved = new ArrayList<>();
        public int saveCount = 0;
        private final List<Payment> initial;

        public FakePaymentRepo() {
            this(new ArrayList<>());
        }

        public FakePaymentRepo(List<Payment> initial) {
            this.initial = new ArrayList<>(initial);
        }

        @Override
        public List<Payment> loadPayments() {
            return new ArrayList<>(initial);
        }

        @Override
        public void savePayments(List<Payment> payments) {
            saved = new ArrayList<>(payments);
            saveCount++;
        }
    }

    public static class FakeUserRepo implements UserRepository {
        public List<RegisteredUser> saved = new ArrayList<>();
        public int saveCount = 0;
        private final List<RegisteredUser> initial;

        public FakeUserRepo() {
            this(new ArrayList<>());
        }

        public FakeUserRepo(List<RegisteredUser> initial) {
            this.initial = new ArrayList<>(initial);
        }

        @Override
        public List<RegisteredUser> loadUsers() {
            return new ArrayList<>(initial);
        }

        @Override
        public void saveUsers(List<RegisteredUser> users) {
            saved = new ArrayList<>(users);
            saveCount++;
        }
    }
}
