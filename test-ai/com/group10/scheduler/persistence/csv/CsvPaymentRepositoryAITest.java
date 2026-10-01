package com.group10.scheduler.persistence.csv;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.group10.scheduler.booking.Payment;
import com.group10.scheduler.booking.PaymentMethod;
import com.group10.scheduler.booking.PaymentStatus;
import com.group10.scheduler.booking.ConcreteStrategies;

public class CsvPaymentRepositoryAITest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private File csvFile;
	private CsvPaymentRepository repository;

	@Before
	public void setUp() throws Exception {
		csvFile = temporaryFolder.newFile("payments.csv");
		repository = new CsvPaymentRepository(csvFile.getAbsolutePath());
	}

	@Test
	public void loadPaymentsFromMissingFileReturnsEmptyList() {
		File missingFile =
				new File(temporaryFolder.getRoot(), "missing-payments.csv");

		CsvPaymentRepository missingRepository =
				new CsvPaymentRepository(missingFile.getAbsolutePath());

		List<Payment> payments = missingRepository.loadPayments();

		assertNotNull(payments);
		assertTrue(payments.isEmpty());
	}

	@Test
	public void saveAndLoadPaymentPreservesAllInformation() {
		Payment payment = new Payment(
				"P001",
				"B001",
				50.0,
				PaymentMethod.CREDIT_CARD,
				PaymentStatus.PAID,
				"2026-08-01",
				ConcreteStrategies.fromMethod(
						PaymentMethod.CREDIT_CARD));

		repository.savePayments(Arrays.asList(payment));

		List<Payment> loadedPayments = repository.loadPayments();

		assertEquals(1, loadedPayments.size());

		Payment loaded = loadedPayments.get(0);

		assertEquals("P001", loaded.getPaymentId());
		assertEquals("B001", loaded.getBookingId());
		assertEquals(50.0, loaded.getAmount(), 0.001);
		assertEquals(PaymentMethod.CREDIT_CARD, loaded.getMethod());
		assertEquals(PaymentStatus.PAID, loaded.getStatus());
		assertEquals("2026-08-01", loaded.getPaymentDate());
	}

	@Test
	public void saveAndLoadMultiplePaymentsPreservesOrder() {
		List<Payment> payments = Arrays.asList(
				createPayment(
						"P001",
						PaymentMethod.CREDIT_CARD,
						PaymentStatus.PAID),

				createPayment(
						"P002",
						PaymentMethod.DEBIT_CARD,
						PaymentStatus.PENDING),

				createPayment(
						"P003",
						PaymentMethod.INSTITUTIONAL_BILLING,
						PaymentStatus.REFUNDED));

		repository.savePayments(payments);

		List<Payment> loaded = repository.loadPayments();

		assertEquals(3, loaded.size());
		assertEquals("P001", loaded.get(0).getPaymentId());
		assertEquals("P002", loaded.get(1).getPaymentId());
		assertEquals("P003", loaded.get(2).getPaymentId());
	}

	@Test
	public void creditCardMethodIsPreserved() {
		Payment loaded =
				saveAndLoadPaymentWithMethod(
						PaymentMethod.CREDIT_CARD);

		assertEquals(
				PaymentMethod.CREDIT_CARD,
				loaded.getMethod());
	}

	@Test
	public void debitCardMethodIsPreserved() {
		Payment loaded =
				saveAndLoadPaymentWithMethod(
						PaymentMethod.DEBIT_CARD);

		assertEquals(
				PaymentMethod.DEBIT_CARD,
				loaded.getMethod());
	}

	@Test
	public void institutionalBillingMethodIsPreserved() {
		Payment loaded =
				saveAndLoadPaymentWithMethod(
						PaymentMethod.INSTITUTIONAL_BILLING);

		assertEquals(
				PaymentMethod.INSTITUTIONAL_BILLING,
				loaded.getMethod());
	}

	@Test
	public void pendingStatusIsPreserved() {
		Payment loaded =
				saveAndLoadPaymentWithStatus(
						PaymentStatus.PENDING);

		assertEquals(PaymentStatus.PENDING, loaded.getStatus());
	}

	@Test
	public void paidStatusIsPreserved() {
		Payment loaded =
				saveAndLoadPaymentWithStatus(
						PaymentStatus.PAID);

		assertEquals(PaymentStatus.PAID, loaded.getStatus());
	}

	@Test
	public void failedStatusIsPreserved() {
		Payment loaded =
				saveAndLoadPaymentWithStatus(
						PaymentStatus.FAILED);

		assertEquals(PaymentStatus.FAILED, loaded.getStatus());
	}

	@Test
	public void refundedStatusIsPreserved() {
		Payment loaded =
				saveAndLoadPaymentWithStatus(
						PaymentStatus.REFUNDED);

		assertEquals(PaymentStatus.REFUNDED, loaded.getStatus());
	}

	@Test
	public void saveEmptyListCreatesLoadableEmptyFile() {
		repository.savePayments(new ArrayList<>());

		List<Payment> loaded = repository.loadPayments();

		assertNotNull(loaded);
		assertTrue(loaded.isEmpty());
	}

	@Test
	public void savePaymentsWritesExpectedHeader() throws Exception {
		repository.savePayments(new ArrayList<>());

		String firstLine =
				Files.readAllLines(csvFile.toPath()).get(0);

		assertEquals(
				"paymentId,bookingId,amount,method,status,paymentDate",
				firstLine);
	}

	@Test
	public void secondSaveOverwritesPreviouslySavedPayments() {
		Payment firstPayment =
				createPayment(
						"P001",
						PaymentMethod.CREDIT_CARD,
						PaymentStatus.PAID);

		Payment secondPayment =
				createPayment(
						"P002",
						PaymentMethod.DEBIT_CARD,
						PaymentStatus.FAILED);

		repository.savePayments(Arrays.asList(firstPayment));
		repository.savePayments(Arrays.asList(secondPayment));

		List<Payment> loaded = repository.loadPayments();

		assertEquals(1, loaded.size());
		assertEquals("P002", loaded.get(0).getPaymentId());
	}

	@Test(expected = NumberFormatException.class)
	public void malformedAmountCausesException() throws Exception {
		String csv =
				"paymentId,bookingId,amount,method,status,paymentDate\n"
				+ "P001,B001,invalid,CREDIT_CARD,PAID,2026-08-01\n";

		Files.writeString(csvFile.toPath(), csv);

		repository.loadPayments();
	}

	@Test(expected = IllegalArgumentException.class)
	public void malformedPaymentMethodCausesException()
			throws Exception {

		String csv =
				"paymentId,bookingId,amount,method,status,paymentDate\n"
				+ "P001,B001,50.0,CASH,PAID,2026-08-01\n";

		Files.writeString(csvFile.toPath(), csv);

		repository.loadPayments();
	}

	@Test(expected = IllegalArgumentException.class)
	public void malformedPaymentStatusCausesException()
			throws Exception {

		String csv =
				"paymentId,bookingId,amount,method,status,paymentDate\n"
				+ "P001,B001,50.0,CREDIT_CARD,UNKNOWN,2026-08-01\n";

		Files.writeString(csvFile.toPath(), csv);

		repository.loadPayments();
	}

	@Test
	public void commasInsideBookingIdArePreserved() {
		Payment payment = new Payment(
				"P001",
				"BOOKING,001",
				50.0,
				PaymentMethod.CREDIT_CARD,
				PaymentStatus.PAID,
				"2026-08-01",
				ConcreteStrategies.fromMethod(
						PaymentMethod.CREDIT_CARD));

		repository.savePayments(Arrays.asList(payment));

		Payment loaded = repository.loadPayments().get(0);

		assertEquals("BOOKING,001", loaded.getBookingId());
	}

	@Test
	public void loadedPaymentHasWorkingCreditCardStrategy() {
		Payment payment = createPayment(
				"P001",
				PaymentMethod.CREDIT_CARD,
				PaymentStatus.PENDING);

		repository.savePayments(Arrays.asList(payment));

		Payment loaded = repository.loadPayments().get(0);

		assertTrue(loaded.processPayment(25.0));
		assertEquals(PaymentStatus.PAID, loaded.getStatus());
	}

	@Test
	public void loadedPaymentHasWorkingDebitCardStrategy() {
		Payment payment = createPayment(
				"P001",
				PaymentMethod.DEBIT_CARD,
				PaymentStatus.PENDING);

		repository.savePayments(Arrays.asList(payment));

		Payment loaded = repository.loadPayments().get(0);

		assertTrue(loaded.processPayment(25.0));
		assertEquals(PaymentStatus.PAID, loaded.getStatus());
	}

	@Test
	public void loadedPaymentHasWorkingInstitutionalStrategy() {
		Payment payment = createPayment(
				"P001",
				PaymentMethod.INSTITUTIONAL_BILLING,
				PaymentStatus.PENDING);

		repository.savePayments(Arrays.asList(payment));

		Payment loaded = repository.loadPayments().get(0);

		assertTrue(loaded.processPayment(25.0));
		assertEquals(PaymentStatus.PAID, loaded.getStatus());
	}

	private Payment createPayment(
			String paymentId,
			PaymentMethod method,
			PaymentStatus status) {

		return new Payment(
				paymentId,
				"B001",
				50.0,
				method,
				status,
				"2026-08-01",
				ConcreteStrategies.fromMethod(method));
	}

	private Payment saveAndLoadPaymentWithMethod(
			PaymentMethod method) {

		Payment payment = createPayment(
				"P001",
				method,
				PaymentStatus.PAID);

		repository.savePayments(Arrays.asList(payment));

		return repository.loadPayments().get(0);
	}

	private Payment saveAndLoadPaymentWithStatus(
			PaymentStatus status) {

		Payment payment = createPayment(
				"P001",
				PaymentMethod.CREDIT_CARD,
				status);

		repository.savePayments(Arrays.asList(payment));

		return repository.loadPayments().get(0);
	}
}
