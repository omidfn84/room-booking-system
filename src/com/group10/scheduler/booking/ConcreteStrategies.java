package com.group10.scheduler.booking;

//Three payment methods required by req10
public final class ConcreteStrategies{
    private ConcreteStrategies (){

    }
    public static class CreditCardStrategy implements PaymentStrategy{
        private final String cardNumber;
        private final String expiryDate;
        private final String cvv;
        public CreditCardStrategy (String cardNumber, String expiryDate, String cvv){
            this.cardNumber= cardNumber;
            this.expiryDate= expiryDate;
            this.cvv= cvv;
        }
        @Override
        public boolean pay (double amount){
            return amount> 0 && expiryDate!= null && !expiryDate.isEmpty () && cvv!= null && !cvv.isEmpty () && cardNumber!= null && !cardNumber.isEmpty ();
        }
        @Override
        public boolean refund (double amount){
            return amount> 0 && expiryDate!= null && !expiryDate.isEmpty () && cvv!= null && !cvv.isEmpty () && cardNumber!= null && !cardNumber.isEmpty ();
        }
    }
    public static class DebitCardStrategy implements PaymentStrategy{
        private final String cardNumber;
        private final String pin;
        public DebitCardStrategy (String cardNumber, String pin){
            this.cardNumber= cardNumber;
            this.pin= pin;
        }
        @Override
        public boolean pay (double amount){
            return amount> 0 && cardNumber!= null && !cardNumber.isEmpty () && pin!= null && !pin.isEmpty ();
        }
        @Override
        public boolean refund (double amount){
            return amount> 0 && cardNumber!= null && !cardNumber.isEmpty () && pin!= null && !pin.isEmpty ();
        }
    }
    public static class InstitutionalBillingStrategy implements PaymentStrategy{
        private final long organizationId;
        private final String billingAccount;
        public InstitutionalBillingStrategy (long organizationId, String billingAccount){
            this.organizationId= organizationId;
            this.billingAccount= billingAccount;
        }
        @Override
        public boolean pay (double amount){
            return amount> 0 && organizationId> 0 && billingAccount!= null && !billingAccount.isEmpty ();
        }
        @Override
        public boolean refund (double amount){
            return amount> 0 && organizationId> 0 && billingAccount!= null && !billingAccount.isEmpty ();
        }
    }
    public static PaymentStrategy fromMethod (PaymentMethod method){
        if (method== null){
            throw new IllegalArgumentException ("Payment method cannot be null.");
        }
        return switch (method){
            case CREDIT_CARD-> new CreditCardStrategy ("0000000000000000", "12/30", "000");
            case DEBIT_CARD-> new DebitCardStrategy ("0000000000000000", "0000");
            case INSTITUTIONAL_BILLING-> new InstitutionalBillingStrategy (1L, "DEFAULT");
            //default-> throw new IllegalArgumentException("Unknown method: " + method);
        };
    }
}
