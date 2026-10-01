package com.group10.scheduler.persistence;

import java.util.List;

import com.group10.scheduler.booking.Payment;

public interface PaymentRepository {
    List<Payment> loadPayments();
    void savePayments(List<Payment> payments);
}
