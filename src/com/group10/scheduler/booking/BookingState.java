package com.group10.scheduler.booking;

/**
 * State pattern interface. Pure transition logic: states move a booking to a
 * new state via booking.setState(new SomeState()). Status is Booking's own
 * concern - this interface and its implementations never touch BookingStatus.
 */
public interface BookingState {
    boolean checkIn(Booking booking);
    boolean cancel(Booking booking);
    boolean edit(Booking booking, String start, String end);
    boolean extend(Booking booking, String until);
    void expire(Booking booking);
    void complete(Booking booking);
}