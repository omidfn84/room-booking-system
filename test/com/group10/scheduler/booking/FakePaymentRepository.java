package com.group10.scheduler.booking;

import java.util.ArrayList;
import java.util.List;

import com.group10.scheduler.persistence.PaymentRepository;

public class FakePaymentRepository implements PaymentRepository {

    public List <Payment> savedPayments= new ArrayList <>();
    public int saveCallCount= 0;
    private final List <Payment> initialPayments;

    public FakePaymentRepository (){
        this.initialPayments= new ArrayList <>();
    }

    public FakePaymentRepository (List <Payment> initialPayments){
        this.initialPayments= new ArrayList <>(initialPayments);
    }

    @Override
    public List <Payment> loadPayments (){
        return new ArrayList <>(initialPayments);
    }

    @Override
    public void savePayments (List <Payment> payments){
        savedPayments= new ArrayList <>(payments);
        saveCallCount++;
    }
}
