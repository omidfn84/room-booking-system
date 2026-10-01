package com.group10.scheduler.booking;

public class Payment{
    private String paymentId;
    private String bookingId; 
    private double amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private String paymentDate;
    private PaymentStrategy strategy;
    
    public Payment (String paymentId, String bookingId, double amount, PaymentMethod method, PaymentStatus status, String paymentDate, PaymentStrategy strategy){
        this.paymentId= paymentId;
        this.bookingId= bookingId;
        this.amount= amount;
        this.method= method;
        this.status= status;
        this.paymentDate= paymentDate;
        this.strategy= strategy;
    }

    public boolean processDeposit (double amount){
        this.amount= amount;
        if (strategy== null || amount<= 0){
            this.status= PaymentStatus.FAILED;
            return false;
        }
        boolean successful= strategy.pay (amount);
        this.status= successful ? PaymentStatus.PAID : PaymentStatus.FAILED;
        return successful;
    }
    
    public boolean processPayment (double amount){
        this.amount= amount;
        if (strategy== null || amount<= 0){
            this.status= PaymentStatus.FAILED;
            return false;
        }
        boolean successful= strategy.pay (amount);
        this.status= successful ? PaymentStatus.PAID : PaymentStatus.FAILED;
        return successful;
    }
    
    
    public boolean refundPayment (){
        if (strategy== null || amount<= 0 || status!= PaymentStatus.PAID){
            return false;
        }
        boolean successful= strategy.refund (amount);
        if (successful){
            this.status= PaymentStatus.REFUNDED;
        }
        return successful;
    }
    
    // getter and setters
    public String getPaymentId (){
        return paymentId;
    }
    public String getBookingId (){
        return bookingId;
    }
    public double getAmount (){
        return amount;
    }
    public PaymentMethod getMethod (){
        return method;
    }
    public PaymentStatus getStatus (){
        return status;
    }
    public String getPaymentDate (){
        return paymentDate;
    }
    
    public void setStrategy (PaymentStrategy strategy){
        this.strategy= strategy;
    }
    
    @Override
    public String toString() {
        return "Payment[" + paymentId + "] booking=" + bookingId 
            + " | $" + amount 
            + " | method=" + method 
            + " | status=" + status;
    }
}
