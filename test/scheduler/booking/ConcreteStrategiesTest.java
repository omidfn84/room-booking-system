package scheduler.booking;

import static org.junit.Assert.*;
import org.junit.Test;

import scheduler.booking.ConcreteStrategies.CreditCardStrategy;
import scheduler.booking.ConcreteStrategies.DebitCardStrategy;
import scheduler.booking.ConcreteStrategies.InstitutionalBillingStrategy;

public class ConcreteStrategiesTest{

    @Test
    public void validCreditCard (){
        CreditCardStrategy s= new CreditCardStrategy ("1234567890123456", "12/30", "123");
        assertTrue (s.pay (100.0));
    }

    @Test
    public void zeroCreditAmount (){
        CreditCardStrategy s= new CreditCardStrategy ("1234567890123456", "12/30", "123");
        assertFalse (s.pay (0.0));
    }

    @Test
    public void negativeCreditAmount (){
        CreditCardStrategy s= new CreditCardStrategy ("1234567890123456", "12/30", "123");
        assertFalse (s.pay (-50.0));
    }

    @Test
    public void missingCvv (){
        CreditCardStrategy s= new CreditCardStrategy ("1234567890123456", "12/30", "");
        assertFalse (s.pay (100.0));
    }

    @Test
    public void nullExpiryDate (){
        CreditCardStrategy s= new CreditCardStrategy ("1234567890123456", null, "123");
        assertFalse (s.pay (100.0));
    }

    @Test
    public void creditRefund (){
        CreditCardStrategy s= new CreditCardStrategy ("1234567890123456", "12/30", "123");
        assertTrue (s.refund (50.0));
        assertFalse (s.refund (0.0));
    }

    @Test
    public void validDebitCard (){
        DebitCardStrategy s= new DebitCardStrategy ("1234567890123456", "1234");
        assertTrue (s.pay (75.0));
    }

    @Test
    public void missingPin (){
        DebitCardStrategy s= new DebitCardStrategy ("1234567890123456", "");
        assertFalse (s.pay (75.0));
    }

    @Test
    public void zeroDebitAmount (){
        DebitCardStrategy s= new DebitCardStrategy ("1234567890123456", "1234");
        assertFalse (s.pay (0.0));
    }

    @Test
    public void debitRefund (){
        DebitCardStrategy s= new DebitCardStrategy ("1234567890123456", "1234");
        assertTrue (s.refund (20.0));
    }

    @Test
    public void validInstitutionalBilling (){
        InstitutionalBillingStrategy s= new InstitutionalBillingStrategy (123456789L, "ACC-001");
        assertTrue (s.pay (200.0));
    }

    @Test
    public void zeroOrganizationId (){
        InstitutionalBillingStrategy s= new InstitutionalBillingStrategy (0L, "ACC-001");
        assertFalse (s.pay (200.0));
    }

    @Test
    public void emptyBillingAccount (){
        InstitutionalBillingStrategy s= new InstitutionalBillingStrategy (123456789L, "");
        assertFalse (s.pay (200.0));
    }

    @Test
    public void billingRefund (){
        InstitutionalBillingStrategy s= new InstitutionalBillingStrategy (123456789L, "ACC-001");
        assertTrue (s.refund (200.0));
    }

    @Test
    public void creditStrategy (){
        assertTrue (ConcreteStrategies.fromMethod (PaymentMethod.CREDIT_CARD) instanceof CreditCardStrategy);
    }

    @Test
    public void debitStrategy (){
        assertTrue (ConcreteStrategies.fromMethod (PaymentMethod.DEBIT_CARD) instanceof DebitCardStrategy);
    }

    @Test
    public void billingStrategy (){
        assertTrue (ConcreteStrategies.fromMethod (PaymentMethod.INSTITUTIONAL_BILLING) instanceof InstitutionalBillingStrategy);
    }

    @Test(expected= IllegalArgumentException.class)
    public void nullPaymentMethod (){
        ConcreteStrategies.fromMethod (null);
    }

    @Test
    public void returnedStrategyWorks (){
        assertTrue (ConcreteStrategies.fromMethod (PaymentMethod.CREDIT_CARD).pay (10.0));
    }
}

