package com.group10.scheduler.persistence;

import java.util.List;

import com.group10.scheduler.booking.Booking;

public interface BookingRepository {
    List<Booking> loadBookings();
    void saveBookings(List<Booking> bookings);
}
