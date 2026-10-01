package com.group10.scheduler.booking;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Payment is the «Context» of the Strategy pattern. It must delegate the
 * actual pay/refund decision to whichever PaymentStrategy it holds and then
 * translate that boolean into a PaymentStatus - without ever inspecting which
 * concrete strategy it is.
 *
 * A recording stub is used instead of a real strategy so the tests can prove
 * delegation happened, which a real strategy would hide.
 */
public class PaymentAITest {

    /** Records what it was asked, and answers however the test wants. */
    private static class SpyStrategy implements PaymentStrategy {
        boolean answer;
        int payCalls = 0;
        int refundCalls = 0;
        double lastAmount = Double.NaN;

        SpyStrategy(boolean answer) {
            this.answer = answer;
        }

        @Override
        public boolean pay(double amount) {
            payCalls++;
            lastAmount = amount;
            return answer;
        }

        @Override
        public boolean refund(double amount) {
            refundCalls++;
            lastAmount = amount;
            return answer;
        }
    }

    private Payment payment(PaymentStrategy strategy) {
        return new Payment("P1", "B1", 20.0, PaymentMethod.CREDIT_CARD,
                PaymentStatus.PENDING, "2026-07-01T10:00", strategy);
    }

    @Test
    public void constructor_storesEveryFieldItWasGiven() {
        Payment p = new Payment("P7", "B7", 45.5, PaymentMethod.DEBIT_CARD,
                PaymentStatus.PENDING, "2026-07-01T10:00", new SpyStrategy(true));

        assertEquals("P7", p.getPaymentId());
        assertEquals("B7", p.getBookingId());
        assertEquals(45.5, p.getAmount(), 0.001);
        assertEquals(PaymentMethod.DEBIT_CARD, p.getMethod());
        assertEquals(PaymentStatus.PENDING, p.getStatus());
        assertEquals("2026-07-01T10:00", p.getPaymentDate());
    }

    @Test
    public void processDeposit_delegatesToTheStrategyAndMarksItPaid() {
        SpyStrategy spy = new SpyStrategy(true);
        Payment p = payment(spy);

        assertTrue(p.processDeposit(20.0));

        assertEquals("Payment must not decide for itself", 1, spy.payCalls);
        assertEquals(20.0, spy.lastAmount, 0.001);
        assertEquals(PaymentStatus.PAID, p.getStatus());
    }

    @Test
    public void processDeposit_marksItFailedWhenTheStrategyRefuses() {
        Payment p = payment(new SpyStrategy(false));

        assertFalse(p.processDeposit(20.0));
        assertEquals(PaymentStatus.FAILED, p.getStatus());
    }

    @Test
    public void processDeposit_overwritesTheStoredAmountWithWhatWasCharged() {
        Payment p = payment(new SpyStrategy(true));

        p.processDeposit(99.0);

        assertEquals(99.0, p.getAmount(), 0.001);
    }

    @Test
    public void processDeposit_failsFastOnANonPositiveAmountWithoutCallingTheStrategy() {
        SpyStrategy spy = new SpyStrategy(true);
        Payment p = payment(spy);

        assertFalse(p.processDeposit(0.0));
        assertEquals(PaymentStatus.FAILED, p.getStatus());
        assertEquals("A zero charge must never reach the payment provider", 0, spy.payCalls);
    }

    @Test
    public void processDeposit_failsWhenNoStrategyIsSet() {
        Payment p = payment(null);

        assertFalse(p.processDeposit(20.0));
        assertEquals(PaymentStatus.FAILED, p.getStatus());
    }

    @Test
    public void processPayment_behavesLikeProcessDepositForTheFinalBalance() {
        SpyStrategy spy = new SpyStrategy(true);
        Payment p = payment(spy);

        assertTrue(p.processPayment(35.0));
        assertEquals(1, spy.payCalls);
        assertEquals(35.0, p.getAmount(), 0.001);
        assertEquals(PaymentStatus.PAID, p.getStatus());
    }

    @Test
    public void processPayment_marksItFailedWhenTheStrategyRefusesOrTheAmountIsInvalid() {
        assertFalse(payment(new SpyStrategy(false)).processPayment(35.0));
        assertFalse(payment(new SpyStrategy(true)).processPayment(-1.0));
        assertFalse(payment(null).processPayment(35.0));
    }

    @Test
    public void refundPayment_onlyWorksOnceThePaymentHasActuallyBeenPaid() {
        // Refunding a PENDING or FAILED payment would hand back money that
        // was never collected.
        Payment pending = payment(new SpyStrategy(true));
        assertEquals(PaymentStatus.PENDING, pending.getStatus());
        assertFalse(pending.refundPayment());

        Payment failed = payment(new SpyStrategy(false));
        failed.processDeposit(20.0);
        assertEquals(PaymentStatus.FAILED, failed.getStatus());
        assertFalse(failed.refundPayment());
    }

    @Test
    public void refundPayment_delegatesAndMarksItRefundedOnSuccess() {
        SpyStrategy spy = new SpyStrategy(true);
        Payment p = payment(spy);
        p.processDeposit(20.0);

        assertTrue(p.refundPayment());

        assertEquals(1, spy.refundCalls);
        assertEquals(PaymentStatus.REFUNDED, p.getStatus());
    }

    @Test
    public void refundPayment_leavesTheStatusPaidWhenTheStrategyRefuses() {
        // The money is still with us, so the payment is still PAID - it must
        // not be optimistically marked REFUNDED.
        SpyStrategy spy = new SpyStrategy(true);
        Payment p = payment(spy);
        p.processDeposit(20.0);
        spy.answer = false;

        assertFalse(p.refundPayment());
        assertEquals(PaymentStatus.PAID, p.getStatus());
    }

    @Test
    public void refundPayment_cannotBeRunTwice() {
        Payment p = payment(new SpyStrategy(true));
        p.processDeposit(20.0);

        assertTrue(p.refundPayment());
        assertFalse("A REFUNDED payment must not refund again", p.refundPayment());
        assertEquals(PaymentStatus.REFUNDED, p.getStatus());
    }

    @Test
    public void refundPayment_failsWhenNoStrategyIsSet() {
        Payment p = payment(new SpyStrategy(true));
        p.processDeposit(20.0);
        p.setStrategy(null);

        assertFalse(p.refundPayment());
    }

    @Test
    public void setStrategy_swapsTheAlgorithmAtRuntime() {
        // This is the Strategy pattern's headline benefit and the reason the
        // CSV layer can rebuild a payment without its original card details.
        SpyStrategy first = new SpyStrategy(false);
        SpyStrategy second = new SpyStrategy(true);
        Payment p = payment(first);

        assertFalse(p.processDeposit(20.0));

        p.setStrategy(second);
        assertTrue(p.processDeposit(20.0));
        assertEquals(1, first.payCalls);
        assertEquals(1, second.payCalls);
    }

    @Test
    public void payment_worksWithEveryRealStrategyFromReq10() {
        // End-to-end through the actual concrete strategies rather than a spy.
        Payment credit = new Payment("P1", "B1", 20.0, PaymentMethod.CREDIT_CARD, PaymentStatus.PENDING,
                "2026-07-01T10:00", new ConcreteStrategies.CreditCardStrategy("4111111111111111", "12/27", "123"));
        Payment debit = new Payment("P2", "B2", 30.0, PaymentMethod.DEBIT_CARD, PaymentStatus.PENDING,
                "2026-07-01T10:00", new ConcreteStrategies.DebitCardStrategy("4111222233334444", "1234"));
        Payment billing = new Payment("P3", "B3", 50.0, PaymentMethod.INSTITUTIONAL_BILLING, PaymentStatus.PENDING,
                "2026-07-01T10:00", new ConcreteStrategies.InstitutionalBillingStrategy(987654321L, "YORK-2026"));

        assertTrue(credit.processDeposit(20.0));
        assertTrue(debit.processDeposit(30.0));
        assertTrue(billing.processDeposit(50.0));

        assertTrue(credit.refundPayment());
        assertTrue(debit.refundPayment());
        assertTrue(billing.refundPayment());
    }

    @Test
    public void toString_showsTheFieldsAHumanNeedsToIdentifyThePayment() {
        Payment p = payment(new SpyStrategy(true));
        String text = p.toString();

        assertTrue(text.contains("P1"));
        assertTrue(text.contains("B1"));
        assertTrue(text.contains("CREDIT_CARD"));
        assertTrue(text.contains("PENDING"));
    }
}
