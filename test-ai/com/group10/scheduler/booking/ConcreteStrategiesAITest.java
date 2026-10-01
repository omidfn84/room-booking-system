package com.group10.scheduler.booking;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Req10: credit cards, debit cards, and institutional billing. Each is a
 * «ConcreteStrategy» that validates its own fields, so Payment never has to
 * ask which method it is holding.
 *
 * The validation rule is the same shape in all three: a positive amount plus
 * every field present and non-empty. These tests therefore check each field
 * individually - a strategy that only checked the first field would still
 * pass a single happy-path test.
 */
public class ConcreteStrategiesAITest {

    // ==================== CreditCardStrategy ====================

    @Test
    public void creditCard_acceptsACompleteCardAndAPositiveAmount() {
        PaymentStrategy strategy = new ConcreteStrategies.CreditCardStrategy("4111111111111111", "12/27", "123");

        assertTrue(strategy.pay(20.0));
        assertTrue(strategy.refund(20.0));
    }

    @Test
    public void creditCard_rejectsNonPositiveAmounts() {
        PaymentStrategy strategy = new ConcreteStrategies.CreditCardStrategy("4111111111111111", "12/27", "123");

        assertFalse(strategy.pay(0.0));
        assertFalse(strategy.pay(-5.0));
        assertFalse(strategy.refund(0.0));
        assertFalse(strategy.refund(-5.0));
    }

    @Test
    public void creditCard_rejectsAMissingCardNumber() {
        assertFalse(new ConcreteStrategies.CreditCardStrategy(null, "12/27", "123").pay(20.0));
        assertFalse(new ConcreteStrategies.CreditCardStrategy("", "12/27", "123").pay(20.0));
    }

    @Test
    public void creditCard_rejectsAMissingExpiryDate() {
        assertFalse(new ConcreteStrategies.CreditCardStrategy("4111111111111111", null, "123").pay(20.0));
        assertFalse(new ConcreteStrategies.CreditCardStrategy("4111111111111111", "", "123").pay(20.0));
    }

    @Test
    public void creditCard_rejectsAMissingCvv() {
        assertFalse(new ConcreteStrategies.CreditCardStrategy("4111111111111111", "12/27", null).pay(20.0));
        assertFalse(new ConcreteStrategies.CreditCardStrategy("4111111111111111", "12/27", "").pay(20.0));
    }

    @Test
    public void creditCard_refundEnforcesTheSameFieldRulesAsPay() {
        // A refund path that validated less than the pay path would let a
        // half-built strategy hand money back.
        assertFalse(new ConcreteStrategies.CreditCardStrategy(null, "12/27", "123").refund(20.0));
        assertFalse(new ConcreteStrategies.CreditCardStrategy("4111111111111111", "", "123").refund(20.0));
        assertFalse(new ConcreteStrategies.CreditCardStrategy("4111111111111111", "12/27", null).refund(20.0));
    }

    // ==================== DebitCardStrategy ====================

    @Test
    public void debitCard_acceptsACompleteCardAndAPositiveAmount() {
        PaymentStrategy strategy = new ConcreteStrategies.DebitCardStrategy("4111222233334444", "1234");

        assertTrue(strategy.pay(30.0));
        assertTrue(strategy.refund(30.0));
    }

    @Test
    public void debitCard_rejectsNonPositiveAmounts() {
        PaymentStrategy strategy = new ConcreteStrategies.DebitCardStrategy("4111222233334444", "1234");

        assertFalse(strategy.pay(0.0));
        assertFalse(strategy.refund(-1.0));
    }

    @Test
    public void debitCard_rejectsAMissingCardNumberOrPin() {
        assertFalse(new ConcreteStrategies.DebitCardStrategy(null, "1234").pay(30.0));
        assertFalse(new ConcreteStrategies.DebitCardStrategy("", "1234").pay(30.0));
        assertFalse(new ConcreteStrategies.DebitCardStrategy("4111222233334444", null).pay(30.0));
        assertFalse(new ConcreteStrategies.DebitCardStrategy("4111222233334444", "").pay(30.0));
    }

    @Test
    public void debitCard_refundEnforcesTheSameFieldRulesAsPay() {
        assertFalse(new ConcreteStrategies.DebitCardStrategy(null, "1234").refund(30.0));
        assertFalse(new ConcreteStrategies.DebitCardStrategy("4111222233334444", "").refund(30.0));
    }

    // ==================== InstitutionalBillingStrategy ====================

    @Test
    public void institutional_acceptsARealOrganisationAndAccount() {
        PaymentStrategy strategy = new ConcreteStrategies.InstitutionalBillingStrategy(987654321L, "YORK-2026");

        assertTrue(strategy.pay(50.0));
        assertTrue(strategy.refund(50.0));
    }

    @Test
    public void institutional_rejectsANonPositiveOrganisationId() {
        // organizationId > 0 is the rule: zero and negatives are not real orgs.
        assertFalse(new ConcreteStrategies.InstitutionalBillingStrategy(0L, "YORK-2026").pay(50.0));
        assertFalse(new ConcreteStrategies.InstitutionalBillingStrategy(-1L, "YORK-2026").pay(50.0));
    }

    @Test
    public void institutional_rejectsAMissingBillingAccount() {
        assertFalse(new ConcreteStrategies.InstitutionalBillingStrategy(987654321L, null).pay(50.0));
        assertFalse(new ConcreteStrategies.InstitutionalBillingStrategy(987654321L, "").pay(50.0));
    }

    @Test
    public void institutional_rejectsNonPositiveAmounts() {
        PaymentStrategy strategy = new ConcreteStrategies.InstitutionalBillingStrategy(987654321L, "YORK-2026");

        assertFalse(strategy.pay(0.0));
        assertFalse(strategy.refund(-10.0));
    }

    @Test
    public void institutional_refundEnforcesTheSameFieldRulesAsPay() {
        assertFalse(new ConcreteStrategies.InstitutionalBillingStrategy(0L, "YORK-2026").refund(50.0));
        assertFalse(new ConcreteStrategies.InstitutionalBillingStrategy(987654321L, "").refund(50.0));
    }

    // ==================== fromMethod factory helper ====================

    @Test
    public void fromMethod_returnsTheMatchingStrategyForEveryPaymentMethod() {
        assertTrue(ConcreteStrategies.fromMethod(PaymentMethod.CREDIT_CARD)
                instanceof ConcreteStrategies.CreditCardStrategy);
        assertTrue(ConcreteStrategies.fromMethod(PaymentMethod.DEBIT_CARD)
                instanceof ConcreteStrategies.DebitCardStrategy);
        assertTrue(ConcreteStrategies.fromMethod(PaymentMethod.INSTITUTIONAL_BILLING)
                instanceof ConcreteStrategies.InstitutionalBillingStrategy);
    }

    @Test
    public void fromMethod_buildsPlaceholdersThatActuallyPass_soReloadedPaymentsStayUsable() {
        // This helper exists for rebuilding a strategy after a CSV reload,
        // where the card details were never persisted. The placeholders must
        // still satisfy their own validation or a reloaded payment could
        // never be refunded.
        for (PaymentMethod method : PaymentMethod.values()) {
            assertTrue(method + " placeholder must validate", ConcreteStrategies.fromMethod(method).pay(10.0));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromMethod_rejectsNull() {
        ConcreteStrategies.fromMethod(null);
    }
}
