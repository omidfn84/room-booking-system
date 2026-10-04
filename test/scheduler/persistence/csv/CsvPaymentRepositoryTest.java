package scheduler.persistence.csv;

import static org.junit.Assert.*;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.List;

import scheduler.booking.ConcreteStrategies;
import scheduler.booking.Payment;
import scheduler.booking.PaymentMethod;
import scheduler.booking.PaymentStatus;

public class CsvPaymentRepositoryTest{

    @Rule
    public TemporaryFolder tempFolder= new TemporaryFolder ();

    @Test
    public void missingFileIsEmpty () throws Exception{
        File file= new File (tempFolder.getRoot (), "does-not-exist.csv");
        CsvPaymentRepository repo= new CsvPaymentRepository (file.getAbsolutePath ());
        assertTrue (repo.loadPayments ().isEmpty ());
    }

    @Test
    public void paymentSaveAndLoad () throws Exception{
        File file= tempFolder.newFile ("payments.csv");
        CsvPaymentRepository repo= new CsvPaymentRepository (file.getAbsolutePath ());

        Payment payment= new Payment ("P1", "B1", 50.0, PaymentMethod.CREDIT_CARD, PaymentStatus.PAID,
                "2026-01-01T10:00:00", ConcreteStrategies.fromMethod (PaymentMethod.CREDIT_CARD));
        repo.savePayments (List.of (payment));

        List <Payment> loaded= repo.loadPayments ();
        assertEquals (1, loaded.size ());
        Payment reloaded= loaded.get (0);
        assertEquals ("P1", reloaded.getPaymentId ());
        assertEquals ("B1", reloaded.getBookingId ());
        assertEquals (50.0, reloaded.getAmount (), 0.0001);
        assertEquals (PaymentMethod.CREDIT_CARD, reloaded.getMethod ());
        assertEquals (PaymentStatus.PAID, reloaded.getStatus ());
    }

    @Test
    public void paymentStrategyReloaded () throws Exception{
        File file= tempFolder.newFile ("payments.csv");
        CsvPaymentRepository repo= new CsvPaymentRepository (file.getAbsolutePath ());
        repo.savePayments (List.of (
                new Payment ("P1", "B1", 10.0, PaymentMethod.DEBIT_CARD, PaymentStatus.PAID, "d",
                        ConcreteStrategies.fromMethod (PaymentMethod.DEBIT_CARD)),
                new Payment ("P2", "B2", 20.0, PaymentMethod.INSTITUTIONAL_BILLING, PaymentStatus.PENDING, "d",
                        ConcreteStrategies.fromMethod (PaymentMethod.INSTITUTIONAL_BILLING))
        ));
        List <Payment> loaded= repo.loadPayments ();
        assertEquals (PaymentMethod.DEBIT_CARD, loaded.get (0).getMethod ());
        assertEquals (PaymentMethod.INSTITUTIONAL_BILLING, loaded.get (1).getMethod ());
    }

    @Test
    public void saveOverwritesPayments () throws Exception{
        File file= tempFolder.newFile ("payments.csv");
        CsvPaymentRepository repo= new CsvPaymentRepository (file.getAbsolutePath ());
        repo.savePayments (List.of (new Payment ("P1", "B1", 10.0, PaymentMethod.CREDIT_CARD, PaymentStatus.PAID,
                "d", ConcreteStrategies.fromMethod (PaymentMethod.CREDIT_CARD))));
        repo.savePayments (List.of (new Payment ("P2", "B2", 20.0, PaymentMethod.CREDIT_CARD, PaymentStatus.PAID,
                "d", ConcreteStrategies.fromMethod (PaymentMethod.CREDIT_CARD))));

        List <Payment> loaded= repo.loadPayments ();
        assertEquals (1, loaded.size ());
        assertEquals ("P2", loaded.get (0).getPaymentId ());
    }

    @Test
    public void emptyPaymentList () throws Exception{
        File file= tempFolder.newFile ("payments.csv");
        CsvPaymentRepository repo= new CsvPaymentRepository (file.getAbsolutePath ());
        repo.savePayments (List.of ());
        assertTrue (repo.loadPayments ().isEmpty ());
    }
        @Test
    public void sameOrder () throws Exception{
        File file= tempFolder.newFile ("order.csv");
        CsvPaymentRepository repo= new CsvPaymentRepository (file.getAbsolutePath ());
        Payment first= new Payment ("P1", "B1", 10.0, PaymentMethod.CREDIT_CARD, PaymentStatus.PAID, "date1", ConcreteStrategies.fromMethod (PaymentMethod.CREDIT_CARD));
        Payment second= new Payment ("P2", "B2", 20.0, PaymentMethod.DEBIT_CARD, PaymentStatus.PENDING, "date2", ConcreteStrategies.fromMethod (PaymentMethod.DEBIT_CARD));
        repo.savePayments (List.of (first, second));
        List <Payment> loaded= repo.loadPayments ();
        assertEquals ("P1", loaded.get (0).getPaymentId ());
        assertEquals ("P2", loaded.get (1).getPaymentId ());
    }
    @Test
    public void dateSaved () throws Exception{
        File file= tempFolder.newFile ("date.csv");
        CsvPaymentRepository repo= new CsvPaymentRepository (file.getAbsolutePath ());
        Payment payment= new Payment ("P3", "B3", 15.0, PaymentMethod.CREDIT_CARD, PaymentStatus.PAID, "2026-01-01", ConcreteStrategies.fromMethod (PaymentMethod.CREDIT_CARD));
        repo.savePayments (List.of (payment));
        Payment loaded= repo.loadPayments ().get (0);
        assertEquals ("2026-01-01", loaded.getPaymentDate ());
    }
    @Test
    public void refundedStatus () throws Exception{
        File file= tempFolder.newFile ("refund.csv");
        CsvPaymentRepository repo= new CsvPaymentRepository (file.getAbsolutePath ());
        Payment payment= new Payment ("P4", "B4", 30.0, PaymentMethod.DEBIT_CARD, PaymentStatus.REFUNDED, "date", ConcreteStrategies.fromMethod (PaymentMethod.DEBIT_CARD));
        repo.savePayments (List.of (payment));
        Payment loaded= repo.loadPayments ().get (0);
        assertEquals (PaymentStatus.REFUNDED, loaded.getStatus ());
    }
    @Test
    public void failedStatus () throws Exception{
        File file= tempFolder.newFile ("failed.csv");
        CsvPaymentRepository repo= new CsvPaymentRepository (file.getAbsolutePath ());
        Payment payment= new Payment ("P5", "B5", 0.0, PaymentMethod.CREDIT_CARD, PaymentStatus.FAILED, "date", ConcreteStrategies.fromMethod (PaymentMethod.CREDIT_CARD));
        repo.savePayments (List.of (payment));
        Payment loaded= repo.loadPayments ().get (0);
        assertEquals (0.0, loaded.getAmount (), 0.0);
        assertEquals (PaymentStatus.FAILED, loaded.getStatus ());
    }
    @Test
    public void loadedStrategyWorks () throws Exception{
        File file= tempFolder.newFile ("strategy.csv");
        CsvPaymentRepository repo= new CsvPaymentRepository (file.getAbsolutePath ());
        Payment payment= new Payment ("P6", "B6", 10.0, PaymentMethod.INSTITUTIONAL_BILLING, PaymentStatus.PENDING, "date", ConcreteStrategies.fromMethod (PaymentMethod.INSTITUTIONAL_BILLING));
        repo.savePayments (List.of (payment));
        Payment loaded= repo.loadPayments ().get (0);
        assertTrue (loaded.processPayment (10.0));
        assertEquals (PaymentStatus.PAID, loaded.getStatus ());
    }
}

