package com.group10.scheduler.booking;

import java.util.ArrayList;
import java.util.List;

import com.group10.scheduler.persistence.BookingRepository;

public class FakeBookingRepository implements BookingRepository {

    public List <Booking> savedBookings= new ArrayList <>();
    public int saveCallCount= 0;
    private final List <Booking> initialBookings;

    public FakeBookingRepository (){
        this.initialBookings= new ArrayList <>();
    }

    public FakeBookingRepository (List <Booking> initialBookings){
        this.initialBookings= new ArrayList <>(initialBookings);
    }

    @Override
    public List <Booking> loadBookings (){
        return new ArrayList <>(initialBookings);
    }

    @Override
    public void saveBookings (List <Booking> bookings){
        savedBookings= new ArrayList <>(bookings);
        saveCallCount++;
    }
}

