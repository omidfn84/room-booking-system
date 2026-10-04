package scheduler.persistence;

import java.util.List;

import scheduler.booking.Booking;

public interface BookingRepository {
    List<Booking> loadBookings();
    void saveBookings(List<Booking> bookings);
}
