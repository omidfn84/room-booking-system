package com.group10.scheduler.booking;

import static org.junit.Assert.*;
import org.junit.Test;

public class PaymentTest{

    private PaymentStrategy alwaysSucceeds (){
        return new PaymentStrategy (){
            public boolean pay (double amount){ return true; }
            public boolean refund (double amount){ return true; }
        };
    }

    private PaymentStrategy alwaysFails (){
        return new PaymentStrategy (){
            public boolean pay (double amount){ return false; }
            public boolean refund (double amount){ return false; }
        };
    }

    @Test
    public void depositWorks (){
        Payment p= new Payment ("P1", "B1", 0, PaymentMethod.CREDIT_CARD, PaymentStatus.PENDING, "2026-01-01", alwaysSucceeds ());
        assertTrue (p.processDeposit (50.0));
        assertEquals (PaymentStatus.PAID, p.getStatus ());
        assertEquals (50.0, p.getAmount (), 0.0001);
    }

    @Test
    public void depositWithoutStrategy (){
        Payment p= new Payment ("P1", "B1", 0, PaymentMethod.CREDIT_CARD, PaymentStatus.PENDING, "2026-01-01", null);
        assertFalse (p.processDeposit (50.0));
        assertEquals (PaymentStatus.FAILED, p.getStatus ());
    }

    @Test
    public void zeroDeposit (){
        Payment p= new Payment ("P1", "B1", 0, PaymentMethod.CREDIT_CARD, PaymentStatus.PENDING, "2026-01-01", alwaysSucceeds ());
        assertFalse (p.processDeposit (0.0));
        assertEquals (PaymentStatus.FAILED, p.getStatus ());
    }

    @Test
    public void negativeDeposit (){
        Payment p= new Payment ("P1", "B1", 0, PaymentMethod.CREDIT_CARD, PaymentStatus.PENDING, "2026-01-01", alwaysSucceeds ());
        assertFalse (p.processDeposit (-10.0));
    }

    @Test
    public void rejectedDeposit (){
        Payment p= new Payment ("P1", "B1", 0, PaymentMethod.CREDIT_CARD, PaymentStatus.PENDING, "2026-01-01", alwaysFails ());
        assertFalse (p.processDeposit (50.0));
        assertEquals (PaymentStatus.FAILED, p.getStatus ());
    }

    @Test
    public void paymentWorks (){
        Payment p= new Payment ("P1", "B1", 0, PaymentMethod.DEBIT_CARD, PaymentStatus.PENDING, "2026-01-01", alwaysSucceeds ());
        assertTrue (p.processPayment (200.0));
        assertEquals (PaymentStatus.PAID, p.getStatus ());
    }

    @Test
    public void paymentWithoutStrategy (){
        Payment p= new Payment ("P1", "B1", 0, PaymentMethod.DEBIT_CARD, PaymentStatus.PENDING, "2026-01-01", null);
        assertFalse (p.processPayment (200.0));
    }

    @Test
    public void refundWorks (){
        Payment p= new Payment ("P1", "B1", 0, PaymentMethod.CREDIT_CARD, PaymentStatus.PENDING, "2026-01-01", alwaysSucceeds ());
        p.processDeposit (50.0);
        assertTrue (p.refundPayment ());
        assertEquals (PaymentStatus.REFUNDED, p.getStatus ());
    }

    @Test
    public void refundBeforePayment (){
        Payment p= new Payment ("P1", "B1", 0, PaymentMethod.CREDIT_CARD, PaymentStatus.PENDING, "2026-01-01", alwaysSucceeds ());
        assertFalse (p.refundPayment ());
    }

    @Test
    public void rejectedRefund (){
        Payment p= new Payment ("P1", "B1", 0, PaymentMethod.CREDIT_CARD, PaymentStatus.PAID, "2026-01-01", alwaysFails ());
        // Refund is not allowed before a successful payment.
        Payment paid= new Payment ("P2", "B2", 50.0, PaymentMethod.CREDIT_CARD, PaymentStatus.PAID, "2026-01-01", alwaysFails ());
        assertFalse (paid.refundPayment ());
    }

    @Test
    public void refundWithoutStrategy (){
        Payment p= new Payment ("P1", "B1", 50.0, PaymentMethod.CREDIT_CARD, PaymentStatus.PAID, "2026-01-01", null);
        assertFalse (p.refundPayment ());
    }

    @Test
    public void constructorValues (){
        PaymentStrategy s= alwaysSucceeds ();
        Payment p= new Payment ("P1", "B1", 100.0, PaymentMethod.INSTITUTIONAL_BILLING, PaymentStatus.PENDING, "2026-01-01", s);
        assertEquals ("P1", p.getPaymentId ());
        assertEquals ("B1", p.getBookingId ());
        assertEquals (100.0, p.getAmount (), 0.0001);
        assertEquals (PaymentMethod.INSTITUTIONAL_BILLING, p.getMethod ());
        assertEquals (PaymentStatus.PENDING, p.getStatus ());
        assertEquals ("2026-01-01", p.getPaymentDate ());
    }

    @Test
    public void replaceStrategy (){
        Payment p= new Payment ("P1", "B1", 0, PaymentMethod.CREDIT_CARD, PaymentStatus.PENDING, "2026-01-01", alwaysFails ());
        assertFalse (p.processDeposit (10.0));
        p.setStrategy (alwaysSucceeds ());
        assertTrue (p.processDeposit (10.0));
    }

    @Test
    public void paymentToString (){
        Payment p= new Payment ("P1", "B1", 100.0, PaymentMethod.CREDIT_CARD, PaymentStatus.PAID, "2026-01-01", alwaysSucceeds ());
        String result= p.toString ();
        assertTrue (result.contains ("P1"));
        assertTrue (result.contains ("B1"));
        assertTrue (result.contains ("100.0"));
        assertTrue (result.contains ("CREDIT_CARD"));
        assertTrue (result.contains ("PAID"));
    }
}

