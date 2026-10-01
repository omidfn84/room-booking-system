package com.group10.scheduler.booking;

/**
 * Strategy pattern interface. Placeholder shape matching the class diagram —
 * swap in the teammate's real implementation when ready.
 */
public interface PaymentStrategy {
    boolean pay(double amount);
    boolean refund(double amount);
}
