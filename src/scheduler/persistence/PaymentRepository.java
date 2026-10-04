package scheduler.persistence;

import java.util.List;

import scheduler.booking.Payment;

public interface PaymentRepository {
    List<Payment> loadPayments();
    void savePayments(List<Payment> payments);
}
