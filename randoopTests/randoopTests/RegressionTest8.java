package randoopTests;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RegressionTest8 {

    public static boolean debug = false;

    @Test
    public void test4001() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4001");
        com.group10.scheduler.room.RoomStatus roomStatus4 = com.group10.scheduler.room.RoomStatus.AVAILABLE;
        com.group10.scheduler.room.Room room5 = new com.group10.scheduler.room.Room("", (int) (byte) 0, "hi! |  | hi! | rate=$20.0/hr | orgId=-1", "hi!", roomStatus4);
        java.lang.String str6 = room5.toString();
        boolean boolean7 = room5.isAvailable();
        int int8 = room5.getCapacity();
        boolean boolean9 = room5.isAvailable();
        com.group10.scheduler.room.RoomStatus roomStatus10 = room5.getStatus();
        room5.setBuilding("Booking[Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $1.0 | method=DEBIT_CARD | status=REFUNDED] room=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 |  \u2192 hi! |  | hi! | rate=$20.0/hr | orgId=-1 | status=CONFIRMED | deposit=$52.0");
        room5.setBuilding("hi! |  | hi! | rate=$20.0/hr | orgId=-1 | hi! |  | rate=$20.0/hr | orgId=-1");
        room5.closeForMaintenance();
        int int16 = room5.getCapacity();
        org.junit.Assert.assertTrue("'" + roomStatus4 + "' != '" + com.group10.scheduler.room.RoomStatus.AVAILABLE + "'", roomStatus4.equals(com.group10.scheduler.room.RoomStatus.AVAILABLE));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str6 + "' != '" + "Room[] hi! |  | hi! | rate=$20.0/hr | orgId=-1-hi! | capacity=0 | status=AVAILABLE" + "'", str6.equals("Room[] hi! |  | hi! | rate=$20.0/hr | orgId=-1-hi! | capacity=0 | status=AVAILABLE"));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean7 + "' != '" + true + "'", boolean7 == true);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + int8 + "' != '" + 0 + "'", int8 == 0);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean9 + "' != '" + true + "'", boolean9 == true);
        org.junit.Assert.assertTrue("'" + roomStatus10 + "' != '" + com.group10.scheduler.room.RoomStatus.AVAILABLE + "'", roomStatus10.equals(com.group10.scheduler.room.RoomStatus.AVAILABLE));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + int16 + "' != '" + 0 + "'", int16 == 0);
    }

    @Test
    public void test4002() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4002");
        com.group10.scheduler.room.RoomManager roomManager0 = null;
        com.group10.scheduler.booking.BookingManager bookingManager1 = null;
        com.group10.scheduler.accounts.AccountManagement accountManagement2 = null;
        com.group10.scheduler.facade.SchedulerFacade schedulerFacade3 = new com.group10.scheduler.facade.SchedulerFacade(roomManager0, bookingManager1, accountManagement2);
        boolean boolean5 = schedulerFacade3.isAdministrator("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0");
        boolean boolean8 = schedulerFacade3.closeRoom("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0", "hi!");
        boolean boolean11 = schedulerFacade3.closeRoom("hi!", "Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $1.0 | method=DEBIT_CARD | status=PAID");
        com.group10.scheduler.booking.PaymentMethod paymentMethod16 = com.group10.scheduler.booking.PaymentMethod.INSTITUTIONAL_BILLING;
        com.group10.scheduler.booking.PaymentStrategy paymentStrategy17 = com.group10.scheduler.booking.ConcreteStrategies.fromMethod(paymentMethod16);
        com.group10.scheduler.booking.PaymentStatus paymentStatus18 = com.group10.scheduler.booking.PaymentStatus.REFUNDED;
        com.group10.scheduler.booking.PaymentMethod paymentMethod23 = com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD;
        com.group10.scheduler.booking.PaymentStatus paymentStatus24 = com.group10.scheduler.booking.PaymentStatus.REFUNDED;
        com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy debitCardStrategy28 = new com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy("hi!", "hi! |  | hi! | rate=$20.0/hr | orgId=-1");
        com.group10.scheduler.booking.Payment payment29 = new com.group10.scheduler.booking.Payment("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0", "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100", 1.0d, paymentMethod23, paymentStatus24, "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100", (com.group10.scheduler.booking.PaymentStrategy) debitCardStrategy28);
        boolean boolean31 = debitCardStrategy28.pay((double) 0.0f);
        com.group10.scheduler.booking.Payment payment32 = new com.group10.scheduler.booking.Payment("hi!", "hi!", (double) (byte) 10, paymentMethod16, paymentStatus18, "hi!", (com.group10.scheduler.booking.PaymentStrategy) debitCardStrategy28);
        java.lang.String str33 = payment32.getPaymentId();
        boolean boolean35 = payment32.processDeposit((double) '#');
        java.lang.String str36 = payment32.getPaymentDate();
        boolean boolean38 = payment32.processPayment((double) 10);
        com.group10.scheduler.booking.PaymentStatus paymentStatus39 = payment32.getStatus();
        java.lang.String str40 = payment32.getBookingId();
        java.lang.String str41 = payment32.toString();
        com.group10.scheduler.booking.PaymentMethod paymentMethod42 = payment32.getMethod();
        java.lang.String[] strArray46 = new java.lang.String[] { "Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $10.0 | method=DEBIT_CARD | status=PAID", "Room[2026-07-27T14:30:05.936766] 2026-07-27T14:30:05.936766-Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | capacity=0 | status=OCCUPIED", "hi! |  | hi! | rate=$20.0/hr | orgId=0" };
        // The following exception was thrown during execution in test generation
        try {
            double double47 = schedulerFacade3.payForBooking("hi!", paymentMethod42, strArray46);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"com.group10.scheduler.booking.BookingManager.payForBooking(String, com.group10.scheduler.booking.PaymentMethod, String[])\" because \"this.bookingManager\" is null");
        } catch (java.lang.NullPointerException e) {
        // Expected exception.
        }
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean5 + "' != '" + false + "'", boolean5 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean8 + "' != '" + false + "'", boolean8 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean11 + "' != '" + false + "'", boolean11 == false);
        org.junit.Assert.assertTrue("'" + paymentMethod16 + "' != '" + com.group10.scheduler.booking.PaymentMethod.INSTITUTIONAL_BILLING + "'", paymentMethod16.equals(com.group10.scheduler.booking.PaymentMethod.INSTITUTIONAL_BILLING));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNotNull(paymentStrategy17);
        org.junit.Assert.assertTrue("'" + paymentStatus18 + "' != '" + com.group10.scheduler.booking.PaymentStatus.REFUNDED + "'", paymentStatus18.equals(com.group10.scheduler.booking.PaymentStatus.REFUNDED));
        org.junit.Assert.assertTrue("'" + paymentMethod23 + "' != '" + com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD + "'", paymentMethod23.equals(com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD));
        org.junit.Assert.assertTrue("'" + paymentStatus24 + "' != '" + com.group10.scheduler.booking.PaymentStatus.REFUNDED + "'", paymentStatus24.equals(com.group10.scheduler.booking.PaymentStatus.REFUNDED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean31 + "' != '" + false + "'", boolean31 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str33 + "' != '" + "hi!" + "'", str33.equals("hi!"));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean35 + "' != '" + true + "'", boolean35 == true);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str36 + "' != '" + "hi!" + "'", str36.equals("hi!"));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean38 + "' != '" + true + "'", boolean38 == true);
        org.junit.Assert.assertTrue("'" + paymentStatus39 + "' != '" + com.group10.scheduler.booking.PaymentStatus.PAID + "'", paymentStatus39.equals(com.group10.scheduler.booking.PaymentStatus.PAID));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str40 + "' != '" + "hi!" + "'", str40.equals("hi!"));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str41 + "' != '" + "Payment[hi!] booking=hi! | $10.0 | method=INSTITUTIONAL_BILLING | status=PAID" + "'", str41.equals("Payment[hi!] booking=hi! | $10.0 | method=INSTITUTIONAL_BILLING | status=PAID"));
        org.junit.Assert.assertTrue("'" + paymentMethod42 + "' != '" + com.group10.scheduler.booking.PaymentMethod.INSTITUTIONAL_BILLING + "'", paymentMethod42.equals(com.group10.scheduler.booking.PaymentMethod.INSTITUTIONAL_BILLING));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNotNull(strArray46);
    }

    @Test
    public void test4003() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4003");
        com.group10.scheduler.booking.ConcreteStates.CheckedInState checkedInState0 = new com.group10.scheduler.booking.ConcreteStates.CheckedInState();
        com.group10.scheduler.booking.BookingStatus bookingStatus7 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking8 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus7);
        boolean boolean9 = booking8.cancelBooking();
        boolean boolean11 = checkedInState0.extend(booking8, "");
        com.group10.scheduler.booking.ConcreteStates.CancelledState cancelledState12 = new com.group10.scheduler.booking.ConcreteStates.CancelledState();
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState13 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.BookingStatus bookingStatus20 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking21 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus20);
        boolean boolean24 = booking21.editBooking("", "hi!");
        confirmedState13.complete(booking21);
        com.group10.scheduler.booking.BookingState bookingState26 = booking21.getState();
        boolean boolean27 = cancelledState12.checkIn(booking21);
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState28 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.BookingStatus bookingStatus35 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking36 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus35);
        boolean boolean39 = booking36.editBooking("", "hi!");
        confirmedState28.complete(booking36);
        boolean boolean43 = cancelledState12.edit(booking36, "hi!", "hi!");
        boolean boolean45 = checkedInState0.extend(booking36, "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0");
        java.lang.String str46 = booking36.getRoomId();
        double double47 = booking36.getDepositAmount();
        org.junit.Assert.assertTrue("'" + bookingStatus7 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus7.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean9 + "' != '" + false + "'", boolean9 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean11 + "' != '" + false + "'", boolean11 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus20 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus20.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean24 + "' != '" + false + "'", boolean24 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNotNull(bookingState26);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean27 + "' != '" + false + "'", boolean27 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus35 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus35.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean39 + "' != '" + false + "'", boolean39 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean43 + "' != '" + false + "'", boolean43 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean45 + "' != '" + false + "'", boolean45 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str46 + "' != '" + "hi!" + "'", str46.equals("hi!"));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + double47 + "' != '" + 0.0d + "'", double47 == 0.0d);
    }

    @Test
    public void test4004() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4004");
        com.group10.scheduler.booking.PaymentMethod paymentMethod6 = com.group10.scheduler.booking.PaymentMethod.INSTITUTIONAL_BILLING;
        com.group10.scheduler.booking.PaymentMethod paymentMethod10 = com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD;
        com.group10.scheduler.booking.PaymentStatus paymentStatus11 = com.group10.scheduler.booking.PaymentStatus.REFUNDED;
        com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy debitCardStrategy15 = new com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy("hi!", "hi! |  | hi! | rate=$20.0/hr | orgId=-1");
        com.group10.scheduler.booking.Payment payment16 = new com.group10.scheduler.booking.Payment("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0", "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100", 1.0d, paymentMethod10, paymentStatus11, "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100", (com.group10.scheduler.booking.PaymentStrategy) debitCardStrategy15);
        boolean boolean18 = payment16.processPayment((double) 0.0f);
        com.group10.scheduler.booking.PaymentStatus paymentStatus19 = payment16.getStatus();
        com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy debitCardStrategy23 = new com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy("", "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100");
        boolean boolean25 = debitCardStrategy23.refund((double) (byte) 1);
        com.group10.scheduler.booking.Payment payment26 = new com.group10.scheduler.booking.Payment("Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $0.0 | method=DEBIT_CARD | status=FAILED", "Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $1.0 | method=DEBIT_CARD | status=REFUNDED", (double) (byte) 1, paymentMethod6, paymentStatus19, "hi!", (com.group10.scheduler.booking.PaymentStrategy) debitCardStrategy23);
        com.group10.scheduler.booking.PaymentMethod paymentMethod27 = payment26.getMethod();
        com.group10.scheduler.booking.PaymentStrategy paymentStrategy28 = com.group10.scheduler.booking.ConcreteStrategies.fromMethod(paymentMethod27);
        com.group10.scheduler.booking.PaymentMethod paymentMethod32 = com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD;
        com.group10.scheduler.booking.PaymentStatus paymentStatus33 = com.group10.scheduler.booking.PaymentStatus.REFUNDED;
        com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy debitCardStrategy37 = new com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy("hi!", "hi! |  | hi! | rate=$20.0/hr | orgId=-1");
        com.group10.scheduler.booking.Payment payment38 = new com.group10.scheduler.booking.Payment("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0", "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100", 1.0d, paymentMethod32, paymentStatus33, "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100", (com.group10.scheduler.booking.PaymentStrategy) debitCardStrategy37);
        java.lang.String str39 = payment38.getBookingId();
        boolean boolean41 = payment38.processDeposit((double) 52L);
        java.lang.String str42 = payment38.getPaymentDate();
        com.group10.scheduler.booking.PaymentStatus paymentStatus43 = payment38.getStatus();
        com.group10.scheduler.booking.ConcreteStrategies.CreditCardStrategy creditCardStrategy48 = new com.group10.scheduler.booking.ConcreteStrategies.CreditCardStrategy("Room[] -2026-07-27T14:30:05.936766 | capacity=-1 | status=MAINTENANCE", "Room[] - | capacity=0 | status=AVAILABLE |  | Room[] hi! |  | hi! | rate=$20.0/hr | orgId=-1-hi! | capacity=0 | status=DISABLED | rate=$20.0/hr | orgId=100", "Room[Room[] Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 |  | rate=$40.0/hr | orgId=100-hi! | capacity=0 | status=AVAILABLE] Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 |  | rate=$40.0/hr | orgId=100-hi! | capacity=0 | status=DISABLED");
        com.group10.scheduler.booking.Payment payment49 = new com.group10.scheduler.booking.Payment("Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $-1.0 | method=DEBIT_CARD | status=FAILED", "Room[] Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 |  | rate=$40.0/hr | orgId=100-hi! | capacity=0 | status=AVAILABLE | Room[] - | capacity=0 | status=AVAILABLE | Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $0.0 | method=DEBIT_CARD | status=FAILED | rate=$50.0/hr | orgId=10", 97.0d, paymentMethod27, paymentStatus43, "Room[] -hi! | capacity=0 | status=AVAILABLE", (com.group10.scheduler.booking.PaymentStrategy) creditCardStrategy48);
        org.junit.Assert.assertTrue("'" + paymentMethod6 + "' != '" + com.group10.scheduler.booking.PaymentMethod.INSTITUTIONAL_BILLING + "'", paymentMethod6.equals(com.group10.scheduler.booking.PaymentMethod.INSTITUTIONAL_BILLING));
        org.junit.Assert.assertTrue("'" + paymentMethod10 + "' != '" + com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD + "'", paymentMethod10.equals(com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD));
        org.junit.Assert.assertTrue("'" + paymentStatus11 + "' != '" + com.group10.scheduler.booking.PaymentStatus.REFUNDED + "'", paymentStatus11.equals(com.group10.scheduler.booking.PaymentStatus.REFUNDED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean18 + "' != '" + false + "'", boolean18 == false);
        org.junit.Assert.assertTrue("'" + paymentStatus19 + "' != '" + com.group10.scheduler.booking.PaymentStatus.FAILED + "'", paymentStatus19.equals(com.group10.scheduler.booking.PaymentStatus.FAILED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean25 + "' != '" + false + "'", boolean25 == false);
        org.junit.Assert.assertTrue("'" + paymentMethod27 + "' != '" + com.group10.scheduler.booking.PaymentMethod.INSTITUTIONAL_BILLING + "'", paymentMethod27.equals(com.group10.scheduler.booking.PaymentMethod.INSTITUTIONAL_BILLING));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNotNull(paymentStrategy28);
        org.junit.Assert.assertTrue("'" + paymentMethod32 + "' != '" + com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD + "'", paymentMethod32.equals(com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD));
        org.junit.Assert.assertTrue("'" + paymentStatus33 + "' != '" + com.group10.scheduler.booking.PaymentStatus.REFUNDED + "'", paymentStatus33.equals(com.group10.scheduler.booking.PaymentStatus.REFUNDED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str39 + "' != '" + "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100" + "'", str39.equals("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100"));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean41 + "' != '" + true + "'", boolean41 == true);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str42 + "' != '" + "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100" + "'", str42.equals("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100"));
        org.junit.Assert.assertTrue("'" + paymentStatus43 + "' != '" + com.group10.scheduler.booking.PaymentStatus.PAID + "'", paymentStatus43.equals(com.group10.scheduler.booking.PaymentStatus.PAID));
    }

    @Test
    public void test4005() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4005");
        com.group10.scheduler.room.RoomStatus roomStatus4 = com.group10.scheduler.room.RoomStatus.AVAILABLE;
        com.group10.scheduler.room.Room room5 = new com.group10.scheduler.room.Room("", (int) (byte) 0, "hi! |  | hi! | rate=$20.0/hr | orgId=-1", "hi!", roomStatus4);
        int int6 = room5.getCapacity();
        room5.enable();
        java.lang.String str8 = room5.getRoomNumber();
        room5.closeForMaintenance();
        org.junit.Assert.assertTrue("'" + roomStatus4 + "' != '" + com.group10.scheduler.room.RoomStatus.AVAILABLE + "'", roomStatus4.equals(com.group10.scheduler.room.RoomStatus.AVAILABLE));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + int6 + "' != '" + 0 + "'", int6 == 0);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str8 + "' != '" + "hi!" + "'", str8.equals("hi!"));
    }

    @Test
    public void test4006() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4006");
        com.group10.scheduler.room.RoomStatus roomStatus4 = com.group10.scheduler.room.RoomStatus.AVAILABLE;
        com.group10.scheduler.room.Room room5 = new com.group10.scheduler.room.Room("", (int) (byte) 0, "hi! |  | hi! | rate=$20.0/hr | orgId=-1", "hi!", roomStatus4);
        room5.setBuilding("");
        room5.closeForMaintenance();
        int int9 = room5.getCapacity();
        room5.setBuilding("");
        room5.disable();
        com.group10.scheduler.room.RoomSensorSystem roomSensorSystem13 = room5.getSensorSystem();
        com.group10.scheduler.room.RoomSensorSystem roomSensorSystem14 = room5.getSensorSystem();
        org.junit.Assert.assertTrue("'" + roomStatus4 + "' != '" + com.group10.scheduler.room.RoomStatus.AVAILABLE + "'", roomStatus4.equals(com.group10.scheduler.room.RoomStatus.AVAILABLE));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + int9 + "' != '" + 0 + "'", int9 == 0);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNotNull(roomSensorSystem13);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNotNull(roomSensorSystem14);
    }

    @Test
    public void test4007() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4007");
        com.group10.scheduler.room.RoomStatus roomStatus4 = com.group10.scheduler.room.RoomStatus.AVAILABLE;
        com.group10.scheduler.room.Room room5 = new com.group10.scheduler.room.Room("", (int) (byte) 0, "hi! |  | hi! | rate=$20.0/hr | orgId=-1", "hi!", roomStatus4);
        room5.setBuilding("");
        room5.setCapacity((int) '#');
        com.group10.scheduler.room.RoomSensorSystem roomSensorSystem10 = room5.getSensorSystem();
        room5.enable();
        java.lang.String str12 = room5.toString();
        room5.setCapacity(0);
        org.junit.Assert.assertTrue("'" + roomStatus4 + "' != '" + com.group10.scheduler.room.RoomStatus.AVAILABLE + "'", roomStatus4.equals(com.group10.scheduler.room.RoomStatus.AVAILABLE));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNotNull(roomSensorSystem10);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str12 + "' != '" + "Room[] -hi! | capacity=35 | status=AVAILABLE" + "'", str12.equals("Room[] -hi! | capacity=35 | status=AVAILABLE"));
    }

    @Test
    public void test4008() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4008");
        com.group10.scheduler.room.RoomSensorSystem roomSensorSystem1 = new com.group10.scheduler.room.RoomSensorSystem("");
        roomSensorSystem1.setOccupied(false);
        roomSensorSystem1.setSensorId("hi!");
        roomSensorSystem1.setSensorId("Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $1.0 | method=DEBIT_CARD | status=PAID");
        roomSensorSystem1.setOccupied(true);
        boolean boolean11 = roomSensorSystem1.scanIDBadge("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100");
        boolean boolean13 = roomSensorSystem1.scanIDBadge("Booking[Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $1.0 | method=DEBIT_CARD | status=REFUNDED] room=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 |  \u2192 hi! |  | hi! | rate=$20.0/hr | orgId=-1 | status=CONFIRMED | deposit=$52.0");
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean11 + "' != '" + true + "'", boolean11 == true);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean13 + "' != '" + true + "'", boolean13 == true);
    }

    @Test
    public void test4009() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4009");
        com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy debitCardStrategy2 = new com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy("hi!", "hi! |  | hi! | rate=$20.0/hr | orgId=-1");
        boolean boolean4 = debitCardStrategy2.refund((double) 0);
        boolean boolean6 = debitCardStrategy2.pay((double) 1.0f);
        boolean boolean8 = debitCardStrategy2.pay((double) 1);
        boolean boolean10 = debitCardStrategy2.refund((double) '4');
        boolean boolean12 = debitCardStrategy2.refund(1.0d);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean4 + "' != '" + false + "'", boolean4 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean6 + "' != '" + true + "'", boolean6 == true);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean8 + "' != '" + true + "'", boolean8 == true);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean10 + "' != '" + true + "'", boolean10 == true);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean12 + "' != '" + true + "'", boolean12 == true);
    }

    @Test
    public void test4010() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4010");
        com.group10.scheduler.room.RoomManager roomManager0 = null;
        com.group10.scheduler.booking.BookingManager bookingManager1 = null;
        com.group10.scheduler.accounts.AccountManagement accountManagement2 = null;
        com.group10.scheduler.facade.SchedulerFacade schedulerFacade3 = new com.group10.scheduler.facade.SchedulerFacade(roomManager0, bookingManager1, accountManagement2);
        boolean boolean6 = schedulerFacade3.disableRoom("hi!", "");
        boolean boolean9 = schedulerFacade3.closeRoom("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 |  | rate=$40.0/hr | orgId=100", "");
        boolean boolean12 = schedulerFacade3.enableRoom("Booking[] room=hi! |  \u2192  | status=COMPLETED | deposit=$0.0", "Room[] hi! |  | hi! | rate=$20.0/hr | orgId=-1-hi! | capacity=0 | status=AVAILABLE");
        com.group10.scheduler.room.RoomStatus roomStatus18 = com.group10.scheduler.room.RoomStatus.AVAILABLE;
        com.group10.scheduler.room.Room room19 = new com.group10.scheduler.room.Room("", (int) (byte) 0, "hi! |  | hi! | rate=$20.0/hr | orgId=-1", "hi!", roomStatus18);
        java.lang.String str20 = room19.toString();
        com.group10.scheduler.room.RoomStatus roomStatus21 = room19.getStatus();
        room19.setBuilding("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 |  | rate=$40.0/hr | orgId=100");
        java.lang.String str24 = room19.getBuilding();
        room19.setBuilding("");
        room19.setRoomId("Room[] hi! |  | hi! | rate=$20.0/hr | orgId=-1-hi! | capacity=0 | status=DISABLED");
        room19.setRoomNumber("Booking[] room=hi! |  \u2192  | status=CONFIRMED | deposit=$0.0");
        java.lang.String str31 = room19.getBuilding();
        boolean boolean32 = schedulerFacade3.addRoom("hi!", room19);
        boolean boolean33 = room19.isAvailable();
        room19.setBuilding("Room[hi! |  | hi! | rate=$20.0/hr | orgId=-1] Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $1.0 | method=DEBIT_CARD | status=REFUNDED-Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 |  | rate=$40.0/hr | orgId=100 | capacity=97 | status=MAINTENANCE");
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean6 + "' != '" + false + "'", boolean6 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean9 + "' != '" + false + "'", boolean9 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean12 + "' != '" + false + "'", boolean12 == false);
        org.junit.Assert.assertTrue("'" + roomStatus18 + "' != '" + com.group10.scheduler.room.RoomStatus.AVAILABLE + "'", roomStatus18.equals(com.group10.scheduler.room.RoomStatus.AVAILABLE));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str20 + "' != '" + "Room[] hi! |  | hi! | rate=$20.0/hr | orgId=-1-hi! | capacity=0 | status=AVAILABLE" + "'", str20.equals("Room[] hi! |  | hi! | rate=$20.0/hr | orgId=-1-hi! | capacity=0 | status=AVAILABLE"));
        org.junit.Assert.assertTrue("'" + roomStatus21 + "' != '" + com.group10.scheduler.room.RoomStatus.AVAILABLE + "'", roomStatus21.equals(com.group10.scheduler.room.RoomStatus.AVAILABLE));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str24 + "' != '" + "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 |  | rate=$40.0/hr | orgId=100" + "'", str24.equals("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 |  | rate=$40.0/hr | orgId=100"));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str31 + "' != '" + "" + "'", str31.equals(""));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean32 + "' != '" + false + "'", boolean32 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean33 + "' != '" + true + "'", boolean33 == true);
    }

    @Test
    public void test4011() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4011");
        com.group10.scheduler.booking.PaymentMethod paymentMethod3 = com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD;
        com.group10.scheduler.booking.PaymentStatus paymentStatus4 = com.group10.scheduler.booking.PaymentStatus.REFUNDED;
        com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy debitCardStrategy8 = new com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy("hi!", "hi! |  | hi! | rate=$20.0/hr | orgId=-1");
        com.group10.scheduler.booking.Payment payment9 = new com.group10.scheduler.booking.Payment("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0", "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100", 1.0d, paymentMethod3, paymentStatus4, "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100", (com.group10.scheduler.booking.PaymentStrategy) debitCardStrategy8);
        boolean boolean11 = payment9.processPayment((double) 0.0f);
        java.lang.String str12 = payment9.getPaymentDate();
        boolean boolean14 = payment9.processPayment((double) ' ');
        com.group10.scheduler.booking.PaymentStatus paymentStatus15 = payment9.getStatus();
        org.junit.Assert.assertTrue("'" + paymentMethod3 + "' != '" + com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD + "'", paymentMethod3.equals(com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD));
        org.junit.Assert.assertTrue("'" + paymentStatus4 + "' != '" + com.group10.scheduler.booking.PaymentStatus.REFUNDED + "'", paymentStatus4.equals(com.group10.scheduler.booking.PaymentStatus.REFUNDED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean11 + "' != '" + false + "'", boolean11 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str12 + "' != '" + "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100" + "'", str12.equals("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100"));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean14 + "' != '" + true + "'", boolean14 == true);
        org.junit.Assert.assertTrue("'" + paymentStatus15 + "' != '" + com.group10.scheduler.booking.PaymentStatus.PAID + "'", paymentStatus15.equals(com.group10.scheduler.booking.PaymentStatus.PAID));
    }

    @Test
    public void test4012() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4012");
        com.group10.scheduler.room.RoomStatus roomStatus12 = com.group10.scheduler.room.RoomStatus.AVAILABLE;
        com.group10.scheduler.room.Room room13 = new com.group10.scheduler.room.Room("", (int) (byte) 0, "hi! |  | hi! | rate=$20.0/hr | orgId=-1", "hi!", roomStatus12);
        java.lang.String str14 = room13.toString();
        boolean boolean15 = room13.isAvailable();
        com.group10.scheduler.room.RoomStatus roomStatus16 = com.group10.scheduler.room.RoomStatus.DISABLED;
        room13.setStatus(roomStatus16);
        com.group10.scheduler.room.Room room18 = new com.group10.scheduler.room.Room("", (int) (byte) 10, "Room[] -hi! | capacity=0 | status=AVAILABLE", "2026-07-27T14:30:04.935991", roomStatus16);
        com.group10.scheduler.room.Room room19 = new com.group10.scheduler.room.Room("Booking[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=52] room=Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $100.0 | method=DEBIT_CARD | status=PAID | Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $100.0 | method=DEBIT_CARD | status=PAID \u2192 Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | status=CHECKED_IN | deposit=$1.0", 1, "Room[] -hi! | capacity=0 | status=MAINTENANCE", "hi! |  | hi! | rate=$20.0/hr | orgId=10", roomStatus16);
        org.junit.Assert.assertTrue("'" + roomStatus12 + "' != '" + com.group10.scheduler.room.RoomStatus.AVAILABLE + "'", roomStatus12.equals(com.group10.scheduler.room.RoomStatus.AVAILABLE));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str14 + "' != '" + "Room[] hi! |  | hi! | rate=$20.0/hr | orgId=-1-hi! | capacity=0 | status=AVAILABLE" + "'", str14.equals("Room[] hi! |  | hi! | rate=$20.0/hr | orgId=-1-hi! | capacity=0 | status=AVAILABLE"));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean15 + "' != '" + true + "'", boolean15 == true);
        org.junit.Assert.assertTrue("'" + roomStatus16 + "' != '" + com.group10.scheduler.room.RoomStatus.DISABLED + "'", roomStatus16.equals(com.group10.scheduler.room.RoomStatus.DISABLED));
    }

    @Test
    public void test4013() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4013");
        com.group10.scheduler.room.RoomManager roomManager0 = null;
        com.group10.scheduler.booking.BookingManager bookingManager1 = null;
        com.group10.scheduler.accounts.AccountManagement accountManagement2 = null;
        com.group10.scheduler.facade.SchedulerFacade schedulerFacade3 = new com.group10.scheduler.facade.SchedulerFacade(roomManager0, bookingManager1, accountManagement2);
        boolean boolean6 = schedulerFacade3.disableRoom("hi!", "");
        boolean boolean9 = schedulerFacade3.closeRoom("", "");
        com.group10.scheduler.room.RoomStatus roomStatus15 = com.group10.scheduler.room.RoomStatus.AVAILABLE;
        com.group10.scheduler.room.Room room16 = new com.group10.scheduler.room.Room("", (int) (byte) 0, "hi! |  | hi! | rate=$20.0/hr | orgId=-1", "hi!", roomStatus15);
        room16.setBuilding("");
        room16.closeForMaintenance();
        int int20 = room16.getCapacity();
        room16.setBuilding("");
        boolean boolean23 = schedulerFacade3.addRoom("Booking[] room=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! \u2192 Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | status=COMPLETED | deposit=$32.0", room16);
        boolean boolean26 = schedulerFacade3.closeRoom("hi! |  | hi! | rate=$20.0/hr | orgId=0", "Room[] - | capacity=0 | status=AVAILABLE");
        boolean boolean28 = schedulerFacade3.isAdministrator("Payment[Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $0.0 | method=DEBIT_CARD | status=FAILED] booking=Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $1.0 | method=DEBIT_CARD | status=REFUNDED | $1.0 | method=INSTITUTIONAL_BILLING | status=FAILED");
        com.group10.scheduler.booking.PaymentMethod paymentMethod39 = com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD;
        com.group10.scheduler.booking.PaymentStatus paymentStatus40 = com.group10.scheduler.booking.PaymentStatus.REFUNDED;
        com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy debitCardStrategy44 = new com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy("hi!", "hi! |  | hi! | rate=$20.0/hr | orgId=-1");
        com.group10.scheduler.booking.Payment payment45 = new com.group10.scheduler.booking.Payment("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0", "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100", 1.0d, paymentMethod39, paymentStatus40, "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100", (com.group10.scheduler.booking.PaymentStrategy) debitCardStrategy44);
        com.group10.scheduler.booking.PaymentMethod paymentMethod46 = payment45.getMethod();
        com.group10.scheduler.booking.PaymentStatus paymentStatus47 = payment45.getStatus();
        com.group10.scheduler.booking.PaymentMethod paymentMethod48 = payment45.getMethod();
        com.group10.scheduler.booking.PaymentStrategy paymentStrategy49 = com.group10.scheduler.booking.ConcreteStrategies.fromMethod(paymentMethod48);
        com.group10.scheduler.booking.PaymentMethod paymentMethod53 = com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD;
        com.group10.scheduler.booking.PaymentStatus paymentStatus54 = com.group10.scheduler.booking.PaymentStatus.REFUNDED;
        com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy debitCardStrategy58 = new com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy("hi!", "hi! |  | hi! | rate=$20.0/hr | orgId=-1");
        com.group10.scheduler.booking.Payment payment59 = new com.group10.scheduler.booking.Payment("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0", "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100", 1.0d, paymentMethod53, paymentStatus54, "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100", (com.group10.scheduler.booking.PaymentStrategy) debitCardStrategy58);
        java.lang.String str60 = payment59.getBookingId();
        com.group10.scheduler.booking.PaymentMethod paymentMethod61 = payment59.getMethod();
        boolean boolean63 = payment59.processDeposit((double) (byte) 0);
        com.group10.scheduler.booking.PaymentMethod paymentMethod64 = payment59.getMethod();
        java.lang.String str65 = payment59.getPaymentDate();
        boolean boolean67 = payment59.processPayment((double) 100);
        java.lang.String str68 = payment59.toString();
        java.lang.String str69 = payment59.getBookingId();
        com.group10.scheduler.booking.PaymentStatus paymentStatus70 = payment59.getStatus();
        com.group10.scheduler.booking.ConcreteStrategies.CreditCardStrategy creditCardStrategy75 = new com.group10.scheduler.booking.ConcreteStrategies.CreditCardStrategy("Booking[] room=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! \u2192 Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | status=COMPLETED | deposit=$32.0", "Room[] Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 |  | rate=$40.0/hr | orgId=100-hi! | capacity=0 | status=AVAILABLE", "Room[] -hi! | capacity=35 | status=MAINTENANCE");
        com.group10.scheduler.booking.Payment payment76 = new com.group10.scheduler.booking.Payment("Room[] hi! |  | hi! | rate=$20.0/hr | orgId=-1-hi! | capacity=10 | status=AVAILABLE", " | Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | rate=$40.0/hr | orgId=35", (double) 100L, paymentMethod48, paymentStatus70, "2026-07-27T14:30:05.936766", (com.group10.scheduler.booking.PaymentStrategy) creditCardStrategy75);
        java.lang.String[] strArray77 = new java.lang.String[] {};
        // The following exception was thrown during execution in test generation
        try {
            com.group10.scheduler.booking.Booking booking78 = schedulerFacade3.bookRoom("Room[] -hi! | capacity=0 | status=MAINTENANCE", "Room[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] -hi! | capacity=35 | status=AVAILABLE", "Room[] Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $1.0 | method=DEBIT_CARD | status=PAID-Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | capacity=0 | status=OCCUPIED |  | hi! | rate=$20.0/hr | orgId=1", "hi! |  | Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | rate=$40.0/hr | orgId=-1", paymentMethod48, strArray77);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"com.group10.scheduler.accounts.AccountManagement.findByEmail(String)\" because \"this.accountManagement\" is null");
        } catch (java.lang.NullPointerException e) {
        // Expected exception.
        }
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean6 + "' != '" + false + "'", boolean6 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean9 + "' != '" + false + "'", boolean9 == false);
        org.junit.Assert.assertTrue("'" + roomStatus15 + "' != '" + com.group10.scheduler.room.RoomStatus.AVAILABLE + "'", roomStatus15.equals(com.group10.scheduler.room.RoomStatus.AVAILABLE));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + int20 + "' != '" + 0 + "'", int20 == 0);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean23 + "' != '" + false + "'", boolean23 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean26 + "' != '" + false + "'", boolean26 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean28 + "' != '" + false + "'", boolean28 == false);
        org.junit.Assert.assertTrue("'" + paymentMethod39 + "' != '" + com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD + "'", paymentMethod39.equals(com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD));
        org.junit.Assert.assertTrue("'" + paymentStatus40 + "' != '" + com.group10.scheduler.booking.PaymentStatus.REFUNDED + "'", paymentStatus40.equals(com.group10.scheduler.booking.PaymentStatus.REFUNDED));
        org.junit.Assert.assertTrue("'" + paymentMethod46 + "' != '" + com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD + "'", paymentMethod46.equals(com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD));
        org.junit.Assert.assertTrue("'" + paymentStatus47 + "' != '" + com.group10.scheduler.booking.PaymentStatus.REFUNDED + "'", paymentStatus47.equals(com.group10.scheduler.booking.PaymentStatus.REFUNDED));
        org.junit.Assert.assertTrue("'" + paymentMethod48 + "' != '" + com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD + "'", paymentMethod48.equals(com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNotNull(paymentStrategy49);
        org.junit.Assert.assertTrue("'" + paymentMethod53 + "' != '" + com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD + "'", paymentMethod53.equals(com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD));
        org.junit.Assert.assertTrue("'" + paymentStatus54 + "' != '" + com.group10.scheduler.booking.PaymentStatus.REFUNDED + "'", paymentStatus54.equals(com.group10.scheduler.booking.PaymentStatus.REFUNDED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str60 + "' != '" + "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100" + "'", str60.equals("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100"));
        org.junit.Assert.assertTrue("'" + paymentMethod61 + "' != '" + com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD + "'", paymentMethod61.equals(com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean63 + "' != '" + false + "'", boolean63 == false);
        org.junit.Assert.assertTrue("'" + paymentMethod64 + "' != '" + com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD + "'", paymentMethod64.equals(com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str65 + "' != '" + "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100" + "'", str65.equals("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100"));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean67 + "' != '" + true + "'", boolean67 == true);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str68 + "' != '" + "Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $100.0 | method=DEBIT_CARD | status=PAID" + "'", str68.equals("Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $100.0 | method=DEBIT_CARD | status=PAID"));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str69 + "' != '" + "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100" + "'", str69.equals("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100"));
        org.junit.Assert.assertTrue("'" + paymentStatus70 + "' != '" + com.group10.scheduler.booking.PaymentStatus.PAID + "'", paymentStatus70.equals(com.group10.scheduler.booking.PaymentStatus.PAID));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNotNull(strArray77);
    }

    @Test
    public void test4014() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4014");
        com.group10.scheduler.room.RoomStatus roomStatus4 = com.group10.scheduler.room.RoomStatus.AVAILABLE;
        com.group10.scheduler.room.Room room5 = new com.group10.scheduler.room.Room("", (int) (byte) 0, "hi! |  | hi! | rate=$20.0/hr | orgId=-1", "hi!", roomStatus4);
        room5.setBuilding("");
        room5.disable();
        room5.setCapacity((-1));
        room5.setRoomId("hi!");
        room5.setCapacity(0);
        java.lang.String str15 = room5.getRoomId();
        org.junit.Assert.assertTrue("'" + roomStatus4 + "' != '" + com.group10.scheduler.room.RoomStatus.AVAILABLE + "'", roomStatus4.equals(com.group10.scheduler.room.RoomStatus.AVAILABLE));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str15 + "' != '" + "hi!" + "'", str15.equals("hi!"));
    }

    @Test
    public void test4015() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4015");
        com.group10.scheduler.room.RoomStatus roomStatus4 = com.group10.scheduler.room.RoomStatus.AVAILABLE;
        com.group10.scheduler.room.Room room5 = new com.group10.scheduler.room.Room("", (int) (byte) 0, "hi! |  | hi! | rate=$20.0/hr | orgId=-1", "hi!", roomStatus4);
        java.lang.String str6 = room5.toString();
        boolean boolean7 = room5.isAvailable();
        int int8 = room5.getCapacity();
        boolean boolean9 = room5.isAvailable();
        com.group10.scheduler.room.RoomStatus roomStatus10 = room5.getStatus();
        room5.setBuilding("Booking[Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $1.0 | method=DEBIT_CARD | status=REFUNDED] room=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 |  \u2192 hi! |  | hi! | rate=$20.0/hr | orgId=-1 | status=CONFIRMED | deposit=$52.0");
        java.lang.String str13 = room5.getRoomId();
        org.junit.Assert.assertTrue("'" + roomStatus4 + "' != '" + com.group10.scheduler.room.RoomStatus.AVAILABLE + "'", roomStatus4.equals(com.group10.scheduler.room.RoomStatus.AVAILABLE));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str6 + "' != '" + "Room[] hi! |  | hi! | rate=$20.0/hr | orgId=-1-hi! | capacity=0 | status=AVAILABLE" + "'", str6.equals("Room[] hi! |  | hi! | rate=$20.0/hr | orgId=-1-hi! | capacity=0 | status=AVAILABLE"));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean7 + "' != '" + true + "'", boolean7 == true);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + int8 + "' != '" + 0 + "'", int8 == 0);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean9 + "' != '" + true + "'", boolean9 == true);
        org.junit.Assert.assertTrue("'" + roomStatus10 + "' != '" + com.group10.scheduler.room.RoomStatus.AVAILABLE + "'", roomStatus10.equals(com.group10.scheduler.room.RoomStatus.AVAILABLE));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str13 + "' != '" + "" + "'", str13.equals(""));
    }

    @Test
    public void test4016() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4016");
        com.group10.scheduler.booking.BookingStatus bookingStatus6 = com.group10.scheduler.booking.BookingStatus.COMPLETED;
        com.group10.scheduler.booking.Booking booking7 = new com.group10.scheduler.booking.Booking("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0", "hi!", "", "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0", "hi!", (double) (short) 10, bookingStatus6);
        booking7.setEndTime("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100");
        java.lang.String str10 = booking7.getCheckInTime();
        org.junit.Assert.assertTrue("'" + bookingStatus6 + "' != '" + com.group10.scheduler.booking.BookingStatus.COMPLETED + "'", bookingStatus6.equals(com.group10.scheduler.booking.BookingStatus.COMPLETED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNull(str10);
    }

    @Test
    public void test4017() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4017");
        com.group10.scheduler.room.RoomStatus roomStatus4 = com.group10.scheduler.room.RoomStatus.AVAILABLE;
        com.group10.scheduler.room.Room room5 = new com.group10.scheduler.room.Room("", (int) (byte) 0, "hi! |  | hi! | rate=$20.0/hr | orgId=-1", "hi!", roomStatus4);
        java.lang.String str6 = room5.toString();
        com.group10.scheduler.room.RoomStatus roomStatus7 = room5.getStatus();
        room5.setBuilding("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 |  | rate=$40.0/hr | orgId=100");
        java.lang.String str10 = room5.getRoomId();
        int int11 = room5.getCapacity();
        boolean boolean12 = room5.isAvailable();
        room5.setRoomId("Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $1.0 | method=DEBIT_CARD | status=PAID |  | hi! | rate=$20.0/hr | orgId=-1");
        org.junit.Assert.assertTrue("'" + roomStatus4 + "' != '" + com.group10.scheduler.room.RoomStatus.AVAILABLE + "'", roomStatus4.equals(com.group10.scheduler.room.RoomStatus.AVAILABLE));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str6 + "' != '" + "Room[] hi! |  | hi! | rate=$20.0/hr | orgId=-1-hi! | capacity=0 | status=AVAILABLE" + "'", str6.equals("Room[] hi! |  | hi! | rate=$20.0/hr | orgId=-1-hi! | capacity=0 | status=AVAILABLE"));
        org.junit.Assert.assertTrue("'" + roomStatus7 + "' != '" + com.group10.scheduler.room.RoomStatus.AVAILABLE + "'", roomStatus7.equals(com.group10.scheduler.room.RoomStatus.AVAILABLE));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str10 + "' != '" + "" + "'", str10.equals(""));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + int11 + "' != '" + 0 + "'", int11 == 0);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean12 + "' != '" + true + "'", boolean12 == true);
    }

    @Test
    public void test4018() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4018");
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState0 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState1 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.BookingStatus bookingStatus8 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking9 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus8);
        boolean boolean12 = booking9.editBooking("", "hi!");
        confirmedState1.complete(booking9);
        boolean boolean16 = booking9.editBooking("hi!", "hi!");
        boolean boolean17 = confirmedState0.checkIn(booking9);
        com.group10.scheduler.booking.ConcreteStates.CompletedState completedState18 = new com.group10.scheduler.booking.ConcreteStates.CompletedState();
        com.group10.scheduler.booking.BookingStatus bookingStatus25 = com.group10.scheduler.booking.BookingStatus.COMPLETED;
        com.group10.scheduler.booking.Booking booking26 = new com.group10.scheduler.booking.Booking("", "", "hi!", "hi!", "", (double) 0, bookingStatus25);
        completedState18.expire(booking26);
        boolean boolean28 = booking26.cancelBooking();
        boolean boolean30 = confirmedState0.extend(booking26, "Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $50.0 | method=DEBIT_CARD | status=PAID");
        com.group10.scheduler.booking.ConcreteStates.CompletedState completedState31 = new com.group10.scheduler.booking.ConcreteStates.CompletedState();
        com.group10.scheduler.booking.BookingStatus bookingStatus38 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking39 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus38);
        boolean boolean40 = booking39.cancelBooking();
        boolean boolean41 = completedState31.checkIn(booking39);
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState42 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.BookingStatus bookingStatus49 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking50 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus49);
        boolean boolean53 = booking50.editBooking("", "hi!");
        confirmedState42.complete(booking50);
        com.group10.scheduler.booking.BookingStatus bookingStatus61 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking62 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus61);
        boolean boolean63 = booking62.cancelBooking();
        confirmedState42.expire(booking62);
        com.group10.scheduler.booking.BookingState bookingState65 = booking62.getState();
        completedState31.complete(booking62);
        com.group10.scheduler.booking.BookingStatus bookingStatus79 = com.group10.scheduler.booking.BookingStatus.EXPIRED;
        com.group10.scheduler.booking.Booking booking80 = new com.group10.scheduler.booking.Booking("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100", "", "hi!", "hi!", "", (double) 1, bookingStatus79);
        com.group10.scheduler.booking.Booking booking81 = new com.group10.scheduler.booking.Booking("Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $0.0 | method=DEBIT_CARD | status=FAILED", "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100", "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0", "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100", "hi!", (double) 'a', bookingStatus79);
        boolean boolean82 = completedState31.checkIn(booking81);
        confirmedState0.complete(booking81);
        // The following exception was thrown during execution in test generation
        try {
            double double84 = booking81.calculateRemainingBalance();
            org.junit.Assert.fail("Expected exception of type java.time.format.DateTimeParseException; message: Text 'Booking[] room=hi! |  →  | status=CHECKED_IN | deposit=$0.0 | hi...' could not be parsed at index 0");
        } catch (java.time.format.DateTimeParseException e) {
        // Expected exception.
        }
        org.junit.Assert.assertTrue("'" + bookingStatus8 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus8.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean12 + "' != '" + false + "'", boolean12 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean16 + "' != '" + false + "'", boolean16 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean17 + "' != '" + true + "'", boolean17 == true);
        org.junit.Assert.assertTrue("'" + bookingStatus25 + "' != '" + com.group10.scheduler.booking.BookingStatus.COMPLETED + "'", bookingStatus25.equals(com.group10.scheduler.booking.BookingStatus.COMPLETED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean28 + "' != '" + false + "'", boolean28 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean30 + "' != '" + false + "'", boolean30 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus38 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus38.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean40 + "' != '" + false + "'", boolean40 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean41 + "' != '" + false + "'", boolean41 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus49 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus49.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean53 + "' != '" + false + "'", boolean53 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus61 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus61.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean63 + "' != '" + false + "'", boolean63 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNotNull(bookingState65);
        org.junit.Assert.assertTrue("'" + bookingStatus79 + "' != '" + com.group10.scheduler.booking.BookingStatus.EXPIRED + "'", bookingStatus79.equals(com.group10.scheduler.booking.BookingStatus.EXPIRED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean82 + "' != '" + false + "'", boolean82 == false);
    }

    @Test
    public void test4019() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4019");
        com.group10.scheduler.room.RoomStatus roomStatus4 = com.group10.scheduler.room.RoomStatus.AVAILABLE;
        com.group10.scheduler.room.Room room5 = new com.group10.scheduler.room.Room("", (int) (byte) 0, "hi! |  | hi! | rate=$20.0/hr | orgId=-1", "hi!", roomStatus4);
        room5.setBuilding("");
        room5.setCapacity((int) '#');
        com.group10.scheduler.room.RoomSensorSystem roomSensorSystem10 = room5.getSensorSystem();
        room5.setRoomNumber("2026-07-27T14:30:05.936766");
        room5.setRoomNumber("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 |  | rate=$40.0/hr | orgId=100");
        com.group10.scheduler.room.RoomStatus roomStatus19 = com.group10.scheduler.room.RoomStatus.DISABLED;
        com.group10.scheduler.room.Room room20 = new com.group10.scheduler.room.Room("", (int) (byte) 10, "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100", "hi!", roomStatus19);
        room5.setStatus(roomStatus19);
        room5.setRoomId("");
        org.junit.Assert.assertTrue("'" + roomStatus4 + "' != '" + com.group10.scheduler.room.RoomStatus.AVAILABLE + "'", roomStatus4.equals(com.group10.scheduler.room.RoomStatus.AVAILABLE));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNotNull(roomSensorSystem10);
        org.junit.Assert.assertTrue("'" + roomStatus19 + "' != '" + com.group10.scheduler.room.RoomStatus.DISABLED + "'", roomStatus19.equals(com.group10.scheduler.room.RoomStatus.DISABLED));
    }

    @Test
    public void test4020() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4020");
        com.group10.scheduler.accounts.Staff staff5 = new com.group10.scheduler.accounts.Staff("Booking[] room=hi! |  \u2192  | status=COMPLETED | deposit=$0.0", "Booking[] room=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! \u2192 Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | status=COMPLETED | deposit=$32.0", "Booking[] room=hi! |  \u2192  | status=COMPLETED | deposit=$0.0", "Room[] hi! |  | hi! | rate=$20.0/hr | orgId=-1-hi! | capacity=0 | status=MAINTENANCE", (long) 10);
        java.lang.String str6 = staff5.getEmail();
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str6 + "' != '" + "Booking[] room=hi! |  \u2192  | status=COMPLETED | deposit=$0.0" + "'", str6.equals("Booking[] room=hi! |  \u2192  | status=COMPLETED | deposit=$0.0"));
    }

    @Test
    public void test4021() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4021");
        com.group10.scheduler.booking.PaymentMethod paymentMethod3 = com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD;
        com.group10.scheduler.booking.PaymentStatus paymentStatus4 = com.group10.scheduler.booking.PaymentStatus.REFUNDED;
        com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy debitCardStrategy8 = new com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy("hi!", "hi! |  | hi! | rate=$20.0/hr | orgId=-1");
        com.group10.scheduler.booking.Payment payment9 = new com.group10.scheduler.booking.Payment("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0", "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100", 1.0d, paymentMethod3, paymentStatus4, "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100", (com.group10.scheduler.booking.PaymentStrategy) debitCardStrategy8);
        java.lang.String str10 = payment9.getBookingId();
        com.group10.scheduler.booking.PaymentMethod paymentMethod11 = payment9.getMethod();
        com.group10.scheduler.booking.PaymentStrategy paymentStrategy12 = null;
        payment9.setStrategy(paymentStrategy12);
        com.group10.scheduler.booking.ConcreteStrategies.InstitutionalBillingStrategy institutionalBillingStrategy16 = new com.group10.scheduler.booking.ConcreteStrategies.InstitutionalBillingStrategy((long) (short) 1, "Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $0.0 | method=DEBIT_CARD | status=FAILED");
        payment9.setStrategy((com.group10.scheduler.booking.PaymentStrategy) institutionalBillingStrategy16);
        org.junit.Assert.assertTrue("'" + paymentMethod3 + "' != '" + com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD + "'", paymentMethod3.equals(com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD));
        org.junit.Assert.assertTrue("'" + paymentStatus4 + "' != '" + com.group10.scheduler.booking.PaymentStatus.REFUNDED + "'", paymentStatus4.equals(com.group10.scheduler.booking.PaymentStatus.REFUNDED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str10 + "' != '" + "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100" + "'", str10.equals("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100"));
        org.junit.Assert.assertTrue("'" + paymentMethod11 + "' != '" + com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD + "'", paymentMethod11.equals(com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD));
    }

    @Test
    public void test4022() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4022");
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState0 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState1 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState2 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.BookingStatus bookingStatus9 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking10 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus9);
        boolean boolean13 = booking10.editBooking("", "hi!");
        confirmedState2.complete(booking10);
        boolean boolean17 = booking10.editBooking("hi!", "hi!");
        boolean boolean18 = confirmedState1.checkIn(booking10);
        com.group10.scheduler.booking.BookingState bookingState19 = booking10.getState();
        java.lang.String str20 = booking10.getUserEmail();
        confirmedState0.expire(booking10);
        com.group10.scheduler.booking.ConcreteStates.CheckedInState checkedInState22 = new com.group10.scheduler.booking.ConcreteStates.CheckedInState();
        com.group10.scheduler.booking.BookingStatus bookingStatus29 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking30 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus29);
        boolean boolean31 = booking30.cancelBooking();
        boolean boolean33 = checkedInState22.extend(booking30, "");
        com.group10.scheduler.booking.BookingStatus bookingStatus40 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking41 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus40);
        checkedInState22.complete(booking41);
        boolean boolean44 = confirmedState0.extend(booking41, "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=52");
        com.group10.scheduler.booking.ConcreteStates.CancelledState cancelledState45 = new com.group10.scheduler.booking.ConcreteStates.CancelledState();
        com.group10.scheduler.booking.Booking booking46 = null;
        boolean boolean49 = cancelledState45.edit(booking46, "hi!", "");
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState50 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.BookingStatus bookingStatus57 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking58 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus57);
        boolean boolean61 = booking58.editBooking("", "hi!");
        confirmedState50.complete(booking58);
        boolean boolean65 = booking58.editBooking("hi!", "hi!");
        boolean boolean66 = cancelledState45.checkIn(booking58);
        com.group10.scheduler.booking.ConcreteStates.CheckedInState checkedInState67 = new com.group10.scheduler.booking.ConcreteStates.CheckedInState();
        com.group10.scheduler.booking.Booking booking68 = null;
        checkedInState67.expire(booking68);
        com.group10.scheduler.booking.ConcreteStates.CheckedInState checkedInState70 = new com.group10.scheduler.booking.ConcreteStates.CheckedInState();
        com.group10.scheduler.booking.BookingStatus bookingStatus77 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking78 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus77);
        boolean boolean79 = booking78.cancelBooking();
        boolean boolean80 = checkedInState70.checkIn(booking78);
        checkedInState67.complete(booking78);
        boolean boolean82 = cancelledState45.checkIn(booking78);
        com.group10.scheduler.booking.BookingStatus bookingStatus89 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking90 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus89);
        boolean boolean92 = cancelledState45.extend(booking90, "Booking[] room=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! \u2192 Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | status=COMPLETED | deposit=$32.0");
        boolean boolean94 = confirmedState0.extend(booking90, "hi! |  | hi! | rate=$20.0/hr | orgId=-1");
        double double95 = booking90.getDepositAmount();
        boolean boolean96 = booking90.cancelBooking();
        org.junit.Assert.assertTrue("'" + bookingStatus9 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus9.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean13 + "' != '" + false + "'", boolean13 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean17 + "' != '" + false + "'", boolean17 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean18 + "' != '" + true + "'", boolean18 == true);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNotNull(bookingState19);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str20 + "' != '" + "hi!" + "'", str20.equals("hi!"));
        org.junit.Assert.assertTrue("'" + bookingStatus29 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus29.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean31 + "' != '" + false + "'", boolean31 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean33 + "' != '" + false + "'", boolean33 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus40 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus40.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean44 + "' != '" + false + "'", boolean44 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean49 + "' != '" + false + "'", boolean49 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus57 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus57.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean61 + "' != '" + false + "'", boolean61 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean65 + "' != '" + false + "'", boolean65 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean66 + "' != '" + false + "'", boolean66 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus77 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus77.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean79 + "' != '" + false + "'", boolean79 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean80 + "' != '" + false + "'", boolean80 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean82 + "' != '" + false + "'", boolean82 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus89 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus89.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean92 + "' != '" + false + "'", boolean92 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean94 + "' != '" + false + "'", boolean94 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + double95 + "' != '" + 0.0d + "'", double95 == 0.0d);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean96 + "' != '" + false + "'", boolean96 == false);
    }

    @Test
    public void test4023() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4023");
        com.group10.scheduler.accounts.ChiefEventCoordinator chiefEventCoordinator0 = com.group10.scheduler.accounts.ChiefEventCoordinator.getInstance();
        com.group10.scheduler.accounts.Administrator administrator2 = chiefEventCoordinator0.findExistingAdministrator("");
        com.group10.scheduler.accounts.Administrator administrator4 = chiefEventCoordinator0.findExistingAdministrator("");
        com.group10.scheduler.accounts.Administrator administrator6 = chiefEventCoordinator0.findExistingAdministrator("");
        com.group10.scheduler.accounts.Administrator administrator8 = chiefEventCoordinator0.findExistingAdministrator("Booking[] room=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! \u2192 Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | status=COMPLETED | deposit=$32.0");
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNotNull(chiefEventCoordinator0);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNull(administrator2);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNull(administrator4);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNull(administrator6);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNull(administrator8);
    }

    @Test
    public void test4024() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4024");
        com.group10.scheduler.room.RoomStatus roomStatus4 = com.group10.scheduler.room.RoomStatus.AVAILABLE;
        com.group10.scheduler.room.Room room5 = new com.group10.scheduler.room.Room("", (int) (byte) 0, "hi! |  | hi! | rate=$20.0/hr | orgId=-1", "hi!", roomStatus4);
        java.lang.String str6 = room5.toString();
        com.group10.scheduler.room.RoomStatus roomStatus7 = room5.getStatus();
        room5.disable();
        room5.setRoomNumber("");
        java.lang.String str11 = room5.toString();
        room5.enable();
        com.group10.scheduler.room.RoomStatus roomStatus17 = com.group10.scheduler.room.RoomStatus.AVAILABLE;
        com.group10.scheduler.room.Room room18 = new com.group10.scheduler.room.Room("", (int) (byte) 0, "hi! |  | hi! | rate=$20.0/hr | orgId=-1", "hi!", roomStatus17);
        room18.setBuilding("");
        room18.enable();
        com.group10.scheduler.room.RoomStatus roomStatus22 = room18.getStatus();
        com.group10.scheduler.room.RoomSensorSystem roomSensorSystem23 = room18.getSensorSystem();
        java.lang.String str24 = room18.getRoomId();
        com.group10.scheduler.room.RoomStatus roomStatus25 = room18.getStatus();
        room5.setStatus(roomStatus25);
        org.junit.Assert.assertTrue("'" + roomStatus4 + "' != '" + com.group10.scheduler.room.RoomStatus.AVAILABLE + "'", roomStatus4.equals(com.group10.scheduler.room.RoomStatus.AVAILABLE));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str6 + "' != '" + "Room[] hi! |  | hi! | rate=$20.0/hr | orgId=-1-hi! | capacity=0 | status=AVAILABLE" + "'", str6.equals("Room[] hi! |  | hi! | rate=$20.0/hr | orgId=-1-hi! | capacity=0 | status=AVAILABLE"));
        org.junit.Assert.assertTrue("'" + roomStatus7 + "' != '" + com.group10.scheduler.room.RoomStatus.AVAILABLE + "'", roomStatus7.equals(com.group10.scheduler.room.RoomStatus.AVAILABLE));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str11 + "' != '" + "Room[] hi! |  | hi! | rate=$20.0/hr | orgId=-1- | capacity=0 | status=DISABLED" + "'", str11.equals("Room[] hi! |  | hi! | rate=$20.0/hr | orgId=-1- | capacity=0 | status=DISABLED"));
        org.junit.Assert.assertTrue("'" + roomStatus17 + "' != '" + com.group10.scheduler.room.RoomStatus.AVAILABLE + "'", roomStatus17.equals(com.group10.scheduler.room.RoomStatus.AVAILABLE));
        org.junit.Assert.assertTrue("'" + roomStatus22 + "' != '" + com.group10.scheduler.room.RoomStatus.AVAILABLE + "'", roomStatus22.equals(com.group10.scheduler.room.RoomStatus.AVAILABLE));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNotNull(roomSensorSystem23);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str24 + "' != '" + "" + "'", str24.equals(""));
        org.junit.Assert.assertTrue("'" + roomStatus25 + "' != '" + com.group10.scheduler.room.RoomStatus.AVAILABLE + "'", roomStatus25.equals(com.group10.scheduler.room.RoomStatus.AVAILABLE));
    }

    @Test
    public void test4025() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4025");
        com.group10.scheduler.booking.ConcreteStates.CompletedState completedState0 = new com.group10.scheduler.booking.ConcreteStates.CompletedState();
        com.group10.scheduler.booking.BookingStatus bookingStatus7 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking8 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus7);
        boolean boolean9 = booking8.cancelBooking();
        boolean boolean10 = completedState0.checkIn(booking8);
        com.group10.scheduler.booking.ConcreteStates.CheckedInState checkedInState11 = new com.group10.scheduler.booking.ConcreteStates.CheckedInState();
        com.group10.scheduler.booking.BookingStatus bookingStatus18 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking19 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus18);
        boolean boolean20 = booking19.cancelBooking();
        boolean boolean21 = checkedInState11.checkIn(booking19);
        boolean boolean24 = booking19.editBooking("", "");
        completedState0.expire(booking19);
        boolean boolean27 = booking19.extendBooking("");
        boolean boolean28 = booking19.cancelBooking();
        org.junit.Assert.assertTrue("'" + bookingStatus7 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus7.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean9 + "' != '" + false + "'", boolean9 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean10 + "' != '" + false + "'", boolean10 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus18 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus18.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean20 + "' != '" + false + "'", boolean20 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean21 + "' != '" + false + "'", boolean21 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean24 + "' != '" + false + "'", boolean24 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean27 + "' != '" + false + "'", boolean27 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean28 + "' != '" + false + "'", boolean28 == false);
    }

    @Test
    public void test4026() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4026");
        com.group10.scheduler.room.RoomManager roomManager0 = null;
        com.group10.scheduler.booking.BookingManager bookingManager1 = null;
        com.group10.scheduler.accounts.AccountManagement accountManagement2 = null;
        com.group10.scheduler.facade.SchedulerFacade schedulerFacade3 = new com.group10.scheduler.facade.SchedulerFacade(roomManager0, bookingManager1, accountManagement2);
        com.group10.scheduler.room.Room room5 = null;
        boolean boolean6 = schedulerFacade3.addRoom("", room5);
        boolean boolean9 = schedulerFacade3.enableRoom("hi!", "");
        boolean boolean12 = schedulerFacade3.enableRoom("", "hi!");
        boolean boolean15 = schedulerFacade3.enableRoom("Booking[] room=hi! |  \u2192  | status=COMPLETED | deposit=$0.0", "hi! |  | hi! | rate=$20.0/hr | orgId=-1 | Room[] Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $1.0 | method=DEBIT_CARD | status=PAID-Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | capacity=0 | status=OCCUPIED | Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | rate=$50.0/hr | orgId=-1");
        boolean boolean18 = schedulerFacade3.disableRoom("Room[] hi! |  | hi! | rate=$20.0/hr | orgId=-1-hi! | capacity=0 | status=MAINTENANCE", "");
        com.group10.scheduler.booking.PaymentMethod paymentMethod29 = com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD;
        com.group10.scheduler.booking.PaymentMethod paymentMethod33 = com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD;
        com.group10.scheduler.booking.PaymentStatus paymentStatus34 = com.group10.scheduler.booking.PaymentStatus.REFUNDED;
        com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy debitCardStrategy38 = new com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy("hi!", "hi! |  | hi! | rate=$20.0/hr | orgId=-1");
        com.group10.scheduler.booking.Payment payment39 = new com.group10.scheduler.booking.Payment("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0", "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100", 1.0d, paymentMethod33, paymentStatus34, "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100", (com.group10.scheduler.booking.PaymentStrategy) debitCardStrategy38);
        com.group10.scheduler.booking.PaymentMethod paymentMethod40 = payment39.getMethod();
        com.group10.scheduler.booking.PaymentStatus paymentStatus41 = payment39.getStatus();
        com.group10.scheduler.booking.ConcreteStrategies.CreditCardStrategy creditCardStrategy46 = new com.group10.scheduler.booking.ConcreteStrategies.CreditCardStrategy("", "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0", "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0");
        boolean boolean48 = creditCardStrategy46.refund((double) (byte) 0);
        com.group10.scheduler.booking.Payment payment49 = new com.group10.scheduler.booking.Payment("hi! |  | hi! | rate=$20.0/hr | orgId=-1", "hi! |  | hi! | rate=$20.0/hr | orgId=-1", (double) (byte) 10, paymentMethod29, paymentStatus41, "", (com.group10.scheduler.booking.PaymentStrategy) creditCardStrategy46);
        com.group10.scheduler.booking.PaymentMethod paymentMethod53 = com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD;
        com.group10.scheduler.booking.PaymentStatus paymentStatus54 = com.group10.scheduler.booking.PaymentStatus.REFUNDED;
        com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy debitCardStrategy58 = new com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy("hi!", "hi! |  | hi! | rate=$20.0/hr | orgId=-1");
        com.group10.scheduler.booking.Payment payment59 = new com.group10.scheduler.booking.Payment("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0", "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100", 1.0d, paymentMethod53, paymentStatus54, "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100", (com.group10.scheduler.booking.PaymentStrategy) debitCardStrategy58);
        com.group10.scheduler.booking.PaymentStatus paymentStatus60 = payment59.getStatus();
        boolean boolean62 = payment59.processPayment((double) (byte) 1);
        boolean boolean64 = payment59.processDeposit((double) 1L);
        com.group10.scheduler.booking.PaymentStatus paymentStatus65 = payment59.getStatus();
        com.group10.scheduler.booking.ConcreteStrategies.InstitutionalBillingStrategy institutionalBillingStrategy69 = new com.group10.scheduler.booking.ConcreteStrategies.InstitutionalBillingStrategy((long) (short) 100, "Room[] -hi! | capacity=0 | status=AVAILABLE");
        com.group10.scheduler.booking.Payment payment70 = new com.group10.scheduler.booking.Payment("Room[] Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $1.0 | method=DEBIT_CARD | status=PAID-Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | capacity=0 | status=OCCUPIED", "hi! |  | hi! | rate=$20.0/hr | orgId=-1 | hi! |  | rate=$20.0/hr | orgId=-1", 50.0d, paymentMethod29, paymentStatus65, "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=52", (com.group10.scheduler.booking.PaymentStrategy) institutionalBillingStrategy69);
        com.group10.scheduler.booking.PaymentMethod paymentMethod74 = com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD;
        com.group10.scheduler.booking.PaymentStatus paymentStatus75 = com.group10.scheduler.booking.PaymentStatus.REFUNDED;
        com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy debitCardStrategy79 = new com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy("hi!", "hi! |  | hi! | rate=$20.0/hr | orgId=-1");
        com.group10.scheduler.booking.Payment payment80 = new com.group10.scheduler.booking.Payment("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0", "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100", 1.0d, paymentMethod74, paymentStatus75, "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100", (com.group10.scheduler.booking.PaymentStrategy) debitCardStrategy79);
        java.lang.String str81 = payment80.getBookingId();
        com.group10.scheduler.booking.PaymentMethod paymentMethod82 = payment80.getMethod();
        boolean boolean84 = payment80.processDeposit((double) (byte) 0);
        com.group10.scheduler.booking.PaymentStatus paymentStatus85 = payment80.getStatus();
        com.group10.scheduler.booking.ConcreteStrategies.InstitutionalBillingStrategy institutionalBillingStrategy89 = new com.group10.scheduler.booking.ConcreteStrategies.InstitutionalBillingStrategy(100L, "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0");
        com.group10.scheduler.booking.Payment payment90 = new com.group10.scheduler.booking.Payment("hi! |  | hi! | rate=$20.0/hr | orgId=0", "Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $32.0 | method=DEBIT_CARD | status=PAID", (double) 0, paymentMethod29, paymentStatus85, "Room[] -2026-07-27T14:30:05.936766 | capacity=-1 | status=MAINTENANCE", (com.group10.scheduler.booking.PaymentStrategy) institutionalBillingStrategy89);
        java.lang.String[] strArray93 = new java.lang.String[] { "Room[hi! |  | hi! | rate=$20.0/hr | orgId=-1 | hi! | Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | rate=$50.0/hr | orgId=97] Room[] Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 |  | rate=$40.0/hr | orgId=100-hi! | capacity=0 | status=AVAILABLE-Room[] hi! |  | hi! | rate=$20.0/hr | orgId=-1-hi! | capacity=10 | status=AVAILABLE | capacity=10 | status=AVAILABLE", "Booking[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] room= | Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 \u2192 hi! | status=COMPLETED | deposit=$10.0" };
        // The following exception was thrown during execution in test generation
        try {
            double double94 = schedulerFacade3.payForBooking("Room[hi! |  | hi! | rate=$20.0/hr | orgId=-1] -hi! | capacity=10 | status=AVAILABLE", paymentMethod29, strArray93);
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"com.group10.scheduler.booking.BookingManager.payForBooking(String, com.group10.scheduler.booking.PaymentMethod, String[])\" because \"this.bookingManager\" is null");
        } catch (java.lang.NullPointerException e) {
        // Expected exception.
        }
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean6 + "' != '" + false + "'", boolean6 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean9 + "' != '" + false + "'", boolean9 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean12 + "' != '" + false + "'", boolean12 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean15 + "' != '" + false + "'", boolean15 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean18 + "' != '" + false + "'", boolean18 == false);
        org.junit.Assert.assertTrue("'" + paymentMethod29 + "' != '" + com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD + "'", paymentMethod29.equals(com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD));
        org.junit.Assert.assertTrue("'" + paymentMethod33 + "' != '" + com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD + "'", paymentMethod33.equals(com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD));
        org.junit.Assert.assertTrue("'" + paymentStatus34 + "' != '" + com.group10.scheduler.booking.PaymentStatus.REFUNDED + "'", paymentStatus34.equals(com.group10.scheduler.booking.PaymentStatus.REFUNDED));
        org.junit.Assert.assertTrue("'" + paymentMethod40 + "' != '" + com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD + "'", paymentMethod40.equals(com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD));
        org.junit.Assert.assertTrue("'" + paymentStatus41 + "' != '" + com.group10.scheduler.booking.PaymentStatus.REFUNDED + "'", paymentStatus41.equals(com.group10.scheduler.booking.PaymentStatus.REFUNDED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean48 + "' != '" + false + "'", boolean48 == false);
        org.junit.Assert.assertTrue("'" + paymentMethod53 + "' != '" + com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD + "'", paymentMethod53.equals(com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD));
        org.junit.Assert.assertTrue("'" + paymentStatus54 + "' != '" + com.group10.scheduler.booking.PaymentStatus.REFUNDED + "'", paymentStatus54.equals(com.group10.scheduler.booking.PaymentStatus.REFUNDED));
        org.junit.Assert.assertTrue("'" + paymentStatus60 + "' != '" + com.group10.scheduler.booking.PaymentStatus.REFUNDED + "'", paymentStatus60.equals(com.group10.scheduler.booking.PaymentStatus.REFUNDED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean62 + "' != '" + true + "'", boolean62 == true);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean64 + "' != '" + true + "'", boolean64 == true);
        org.junit.Assert.assertTrue("'" + paymentStatus65 + "' != '" + com.group10.scheduler.booking.PaymentStatus.PAID + "'", paymentStatus65.equals(com.group10.scheduler.booking.PaymentStatus.PAID));
        org.junit.Assert.assertTrue("'" + paymentMethod74 + "' != '" + com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD + "'", paymentMethod74.equals(com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD));
        org.junit.Assert.assertTrue("'" + paymentStatus75 + "' != '" + com.group10.scheduler.booking.PaymentStatus.REFUNDED + "'", paymentStatus75.equals(com.group10.scheduler.booking.PaymentStatus.REFUNDED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str81 + "' != '" + "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100" + "'", str81.equals("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100"));
        org.junit.Assert.assertTrue("'" + paymentMethod82 + "' != '" + com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD + "'", paymentMethod82.equals(com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean84 + "' != '" + false + "'", boolean84 == false);
        org.junit.Assert.assertTrue("'" + paymentStatus85 + "' != '" + com.group10.scheduler.booking.PaymentStatus.FAILED + "'", paymentStatus85.equals(com.group10.scheduler.booking.PaymentStatus.FAILED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNotNull(strArray93);
    }

    @Test
    public void test4027() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4027");
        com.group10.scheduler.booking.ConcreteStates.CompletedState completedState0 = new com.group10.scheduler.booking.ConcreteStates.CompletedState();
        com.group10.scheduler.booking.ConcreteStates.ExpiredState expiredState1 = new com.group10.scheduler.booking.ConcreteStates.ExpiredState();
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState2 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState3 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.BookingStatus bookingStatus10 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking11 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus10);
        boolean boolean14 = booking11.editBooking("", "hi!");
        confirmedState3.complete(booking11);
        boolean boolean18 = booking11.editBooking("hi!", "hi!");
        boolean boolean19 = confirmedState2.checkIn(booking11);
        com.group10.scheduler.booking.BookingState bookingState20 = booking11.getState();
        expiredState1.expire(booking11);
        com.group10.scheduler.booking.ConcreteStates.ExpiredState expiredState22 = new com.group10.scheduler.booking.ConcreteStates.ExpiredState();
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState23 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState24 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.BookingStatus bookingStatus31 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking32 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus31);
        boolean boolean35 = booking32.editBooking("", "hi!");
        confirmedState24.complete(booking32);
        boolean boolean39 = booking32.editBooking("hi!", "hi!");
        boolean boolean40 = confirmedState23.checkIn(booking32);
        com.group10.scheduler.booking.BookingState bookingState41 = booking32.getState();
        expiredState22.expire(booking32);
        boolean boolean43 = booking32.checkIn();
        boolean boolean46 = expiredState1.edit(booking32, "hi!", "");
        java.lang.String str47 = booking32.toString();
        java.lang.String str48 = booking32.toString();
        boolean boolean49 = booking32.isDepositForfeited();
        completedState0.complete(booking32);
        com.group10.scheduler.booking.BookingStatus bookingStatus57 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking58 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus57);
        boolean boolean59 = booking58.cancelBooking();
        booking58.setStartTime("hi!");
        booking58.setStartTime("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100");
        double double64 = booking58.getDepositAmount();
        boolean boolean65 = booking58.isDepositForfeited();
        boolean boolean66 = completedState0.checkIn(booking58);
        org.junit.Assert.assertTrue("'" + bookingStatus10 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus10.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean14 + "' != '" + false + "'", boolean14 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean18 + "' != '" + false + "'", boolean18 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean19 + "' != '" + true + "'", boolean19 == true);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNotNull(bookingState20);
        org.junit.Assert.assertTrue("'" + bookingStatus31 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus31.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean35 + "' != '" + false + "'", boolean35 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean39 + "' != '" + false + "'", boolean39 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean40 + "' != '" + true + "'", boolean40 == true);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNotNull(bookingState41);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean43 + "' != '" + false + "'", boolean43 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean46 + "' != '" + false + "'", boolean46 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str47 + "' != '" + "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0" + "'", str47.equals("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0"));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str48 + "' != '" + "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0" + "'", str48.equals("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0"));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean49 + "' != '" + false + "'", boolean49 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus57 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus57.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean59 + "' != '" + false + "'", boolean59 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + double64 + "' != '" + 0.0d + "'", double64 == 0.0d);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean65 + "' != '" + false + "'", boolean65 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean66 + "' != '" + false + "'", boolean66 == false);
    }

    @Test
    public void test4028() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4028");
        com.group10.scheduler.booking.ConcreteStates.CheckedInState checkedInState0 = new com.group10.scheduler.booking.ConcreteStates.CheckedInState();
        com.group10.scheduler.booking.BookingStatus bookingStatus7 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking8 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus7);
        boolean boolean9 = booking8.cancelBooking();
        boolean boolean11 = checkedInState0.extend(booking8, "");
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState12 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState13 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.BookingStatus bookingStatus20 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking21 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus20);
        boolean boolean24 = booking21.editBooking("", "hi!");
        confirmedState13.complete(booking21);
        boolean boolean28 = booking21.editBooking("hi!", "hi!");
        boolean boolean29 = confirmedState12.checkIn(booking21);
        booking21.setDepositForfeited(false);
        boolean boolean33 = checkedInState0.extend(booking21, "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0");
        booking21.setDepositForfeited(false);
        java.lang.String str36 = booking21.getCheckInTime();
        com.group10.scheduler.booking.BookingStatus bookingStatus37 = booking21.getStatus();
        boolean boolean38 = booking21.checkIn();
        java.lang.String str39 = booking21.getStartTime();
        java.lang.String str40 = booking21.getUserEmail();
        java.lang.String str41 = booking21.toString();
        org.junit.Assert.assertTrue("'" + bookingStatus7 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus7.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean9 + "' != '" + false + "'", boolean9 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean11 + "' != '" + false + "'", boolean11 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus20 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus20.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean24 + "' != '" + false + "'", boolean24 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean28 + "' != '" + false + "'", boolean28 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean29 + "' != '" + true + "'", boolean29 == true);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean33 + "' != '" + false + "'", boolean33 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNull(str36);
        org.junit.Assert.assertTrue("'" + bookingStatus37 + "' != '" + com.group10.scheduler.booking.BookingStatus.CHECKED_IN + "'", bookingStatus37.equals(com.group10.scheduler.booking.BookingStatus.CHECKED_IN));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean38 + "' != '" + false + "'", boolean38 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str39 + "' != '" + "" + "'", str39.equals(""));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str40 + "' != '" + "hi!" + "'", str40.equals("hi!"));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str41 + "' != '" + "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0" + "'", str41.equals("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0"));
    }

    @Test
    public void test4029() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4029");
        com.group10.scheduler.accounts.Faculty faculty5 = new com.group10.scheduler.accounts.Faculty("Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $1.0 | method=DEBIT_CARD | status=REFUNDED", "Booking[] room=hi! |  \u2192  | status=CONFIRMED | deposit=$0.0", "Booking[] room=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! \u2192 Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | status=COMPLETED | deposit=$32.0", "Room[] hi! |  | hi! | rate=$20.0/hr | orgId=-1-hi! | capacity=0 | status=DISABLED", (long) (short) 0);
        java.lang.String str6 = faculty5.getUserName();
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str6 + "' != '" + "Room[] hi! |  | hi! | rate=$20.0/hr | orgId=-1-hi! | capacity=0 | status=DISABLED" + "'", str6.equals("Room[] hi! |  | hi! | rate=$20.0/hr | orgId=-1-hi! | capacity=0 | status=DISABLED"));
    }

    @Test
    public void test4030() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4030");
        com.group10.scheduler.booking.ConcreteStates.CheckedInState checkedInState0 = new com.group10.scheduler.booking.ConcreteStates.CheckedInState();
        com.group10.scheduler.booking.Booking booking1 = null;
        // The following exception was thrown during execution in test generation
        try {
            boolean boolean3 = checkedInState0.extend(booking1, "Room[] hi! |  | hi! | rate=$20.0/hr | orgId=-1-hi! | capacity=0 | status=MAINTENANCE");
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"com.group10.scheduler.booking.Booking.getEndTime()\" because \"booking\" is null");
        } catch (java.lang.NullPointerException e) {
        // Expected exception.
        }
    }

    @Test
    public void test4031() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4031");
        com.group10.scheduler.booking.ConcreteStates.CancelledState cancelledState0 = new com.group10.scheduler.booking.ConcreteStates.CancelledState();
        com.group10.scheduler.booking.Booking booking1 = null;
        boolean boolean4 = cancelledState0.edit(booking1, "", "");
        com.group10.scheduler.booking.BookingStatus bookingStatus11 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking12 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus11);
        boolean boolean15 = booking12.editBooking("", "hi!");
        boolean boolean16 = cancelledState0.checkIn(booking12);
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState17 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.BookingStatus bookingStatus24 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking25 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus24);
        boolean boolean28 = booking25.editBooking("", "hi!");
        confirmedState17.complete(booking25);
        com.group10.scheduler.booking.BookingStatus bookingStatus36 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking37 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus36);
        boolean boolean38 = booking37.cancelBooking();
        confirmedState17.expire(booking37);
        com.group10.scheduler.booking.BookingState bookingState40 = booking37.getState();
        boolean boolean41 = cancelledState0.checkIn(booking37);
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState42 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.BookingStatus bookingStatus49 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking50 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus49);
        boolean boolean53 = booking50.editBooking("", "hi!");
        confirmedState42.complete(booking50);
        com.group10.scheduler.booking.BookingState bookingState55 = booking50.getState();
        boolean boolean57 = cancelledState0.extend(booking50, "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0");
        com.group10.scheduler.booking.ConcreteStates.CancelledState cancelledState58 = new com.group10.scheduler.booking.ConcreteStates.CancelledState();
        com.group10.scheduler.booking.Booking booking59 = null;
        boolean boolean62 = cancelledState58.edit(booking59, "", "");
        com.group10.scheduler.booking.BookingStatus bookingStatus69 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking70 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus69);
        boolean boolean73 = booking70.editBooking("", "hi!");
        boolean boolean74 = cancelledState58.checkIn(booking70);
        boolean boolean75 = booking70.checkIn();
        boolean boolean76 = booking70.cancelBooking();
        cancelledState0.complete(booking70);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean4 + "' != '" + false + "'", boolean4 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus11 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus11.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean15 + "' != '" + false + "'", boolean15 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean16 + "' != '" + false + "'", boolean16 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus24 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus24.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean28 + "' != '" + false + "'", boolean28 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus36 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus36.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean38 + "' != '" + false + "'", boolean38 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNotNull(bookingState40);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean41 + "' != '" + false + "'", boolean41 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus49 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus49.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean53 + "' != '" + false + "'", boolean53 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNotNull(bookingState55);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean57 + "' != '" + false + "'", boolean57 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean62 + "' != '" + false + "'", boolean62 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus69 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus69.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean73 + "' != '" + false + "'", boolean73 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean74 + "' != '" + false + "'", boolean74 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean75 + "' != '" + true + "'", boolean75 == true);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean76 + "' != '" + false + "'", boolean76 == false);
    }

    @Test
    public void test4032() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4032");
        com.group10.scheduler.accounts.Staff staff5 = new com.group10.scheduler.accounts.Staff("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0", "hi!", "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100", "", (long) 0);
        double double6 = staff5.getHourlyRate();
        double double7 = staff5.getHourlyRate();
        double double8 = staff5.getHourlyRate();
        java.lang.String str9 = staff5.getUserName();
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + double6 + "' != '" + 40.0d + "'", double6 == 40.0d);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + double7 + "' != '" + 40.0d + "'", double7 == 40.0d);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + double8 + "' != '" + 40.0d + "'", double8 == 40.0d);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str9 + "' != '" + "" + "'", str9.equals(""));
    }

    @Test
    public void test4033() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4033");
        com.group10.scheduler.booking.ConcreteStates.CancelledState cancelledState0 = new com.group10.scheduler.booking.ConcreteStates.CancelledState();
        com.group10.scheduler.booking.Booking booking1 = null;
        boolean boolean4 = cancelledState0.edit(booking1, "hi!", "");
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState5 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.BookingStatus bookingStatus12 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking13 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus12);
        boolean boolean16 = booking13.editBooking("", "hi!");
        confirmedState5.complete(booking13);
        boolean boolean20 = booking13.editBooking("hi!", "hi!");
        boolean boolean21 = cancelledState0.checkIn(booking13);
        java.lang.String str22 = booking13.getBookingId();
        boolean boolean24 = booking13.extendBooking("Room[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] hi! |  | hi! | rate=$20.0/hr | orgId=-1-hi! | capacity=0 | status=AVAILABLE");
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean4 + "' != '" + false + "'", boolean4 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus12 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus12.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean16 + "' != '" + false + "'", boolean16 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean20 + "' != '" + false + "'", boolean20 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean21 + "' != '" + false + "'", boolean21 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str22 + "' != '" + "" + "'", str22.equals(""));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean24 + "' != '" + false + "'", boolean24 == false);
    }

    @Test
    public void test4034() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4034");
        com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy debitCardStrategy2 = new com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=52", "Booking[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] room= | Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 \u2192 hi! | status=COMPLETED | deposit=$10.0");
    }

    @Test
    public void test4035() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4035");
        com.group10.scheduler.room.RoomSensorSystem roomSensorSystem1 = new com.group10.scheduler.room.RoomSensorSystem("Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $1.0 | method=DEBIT_CARD | status=REFUNDED | Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $1.0 | method=DEBIT_CARD | status=PAID | Room[] Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $1.0 | method=DEBIT_CARD | status=PAID-Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | capacity=0 | status=OCCUPIED | rate=$20.0/hr | orgId=-1");
    }

    @Test
    public void test4036() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4036");
        com.group10.scheduler.booking.ConcreteStates.CheckedInState checkedInState0 = new com.group10.scheduler.booking.ConcreteStates.CheckedInState();
        com.group10.scheduler.booking.BookingStatus bookingStatus7 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking8 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus7);
        boolean boolean9 = booking8.cancelBooking();
        boolean boolean10 = checkedInState0.checkIn(booking8);
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState11 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.BookingStatus bookingStatus18 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking19 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus18);
        boolean boolean22 = booking19.editBooking("", "hi!");
        confirmedState11.complete(booking19);
        com.group10.scheduler.booking.BookingState bookingState24 = booking19.getState();
        boolean boolean25 = checkedInState0.checkIn(booking19);
        com.group10.scheduler.booking.ConcreteStates.CheckedInState checkedInState26 = new com.group10.scheduler.booking.ConcreteStates.CheckedInState();
        com.group10.scheduler.booking.BookingStatus bookingStatus33 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking34 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus33);
        boolean boolean35 = booking34.cancelBooking();
        boolean boolean36 = checkedInState26.checkIn(booking34);
        com.group10.scheduler.booking.ConcreteStates.CancelledState cancelledState37 = new com.group10.scheduler.booking.ConcreteStates.CancelledState();
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState38 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.BookingStatus bookingStatus45 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking46 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus45);
        boolean boolean49 = booking46.editBooking("", "hi!");
        confirmedState38.complete(booking46);
        com.group10.scheduler.booking.BookingState bookingState51 = booking46.getState();
        boolean boolean52 = cancelledState37.checkIn(booking46);
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState53 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.BookingStatus bookingStatus60 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking61 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus60);
        boolean boolean64 = booking61.editBooking("", "hi!");
        confirmedState53.complete(booking61);
        boolean boolean68 = cancelledState37.edit(booking61, "hi!", "hi!");
        booking61.setStartTime("");
        boolean boolean73 = checkedInState26.edit(booking61, "hi!", "hi!");
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState74 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState75 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.BookingStatus bookingStatus82 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking83 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus82);
        boolean boolean86 = booking83.editBooking("", "hi!");
        confirmedState75.complete(booking83);
        boolean boolean90 = booking83.editBooking("hi!", "hi!");
        boolean boolean91 = confirmedState74.checkIn(booking83);
        boolean boolean93 = checkedInState26.extend(booking83, "Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $0.0 | method=DEBIT_CARD | status=FAILED");
        boolean boolean95 = checkedInState0.extend(booking83, "Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $50.0 | method=DEBIT_CARD | status=PAID");
        java.lang.String str96 = booking83.getRoomId();
        org.junit.Assert.assertTrue("'" + bookingStatus7 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus7.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean9 + "' != '" + false + "'", boolean9 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean10 + "' != '" + false + "'", boolean10 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus18 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus18.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean22 + "' != '" + false + "'", boolean22 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNotNull(bookingState24);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean25 + "' != '" + false + "'", boolean25 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus33 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus33.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean35 + "' != '" + false + "'", boolean35 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean36 + "' != '" + false + "'", boolean36 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus45 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus45.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean49 + "' != '" + false + "'", boolean49 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNotNull(bookingState51);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean52 + "' != '" + false + "'", boolean52 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus60 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus60.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean64 + "' != '" + false + "'", boolean64 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean68 + "' != '" + false + "'", boolean68 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean73 + "' != '" + false + "'", boolean73 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus82 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus82.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean86 + "' != '" + false + "'", boolean86 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean90 + "' != '" + false + "'", boolean90 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean91 + "' != '" + true + "'", boolean91 == true);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean93 + "' != '" + false + "'", boolean93 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean95 + "' != '" + false + "'", boolean95 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str96 + "' != '" + "hi!" + "'", str96.equals("hi!"));
    }

    @Test
    public void test4037() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4037");
        com.group10.scheduler.room.RoomManager roomManager0 = null;
        com.group10.scheduler.booking.BookingManager bookingManager1 = null;
        com.group10.scheduler.accounts.AccountManagement accountManagement2 = null;
        com.group10.scheduler.facade.SchedulerFacade schedulerFacade3 = new com.group10.scheduler.facade.SchedulerFacade(roomManager0, bookingManager1, accountManagement2);
        boolean boolean6 = schedulerFacade3.enableRoom("", "hi!");
        boolean boolean9 = schedulerFacade3.enableRoom("Booking[] room=hi! |  \u2192  | status=CONFIRMED | deposit=$0.0", "-sensor");
        // The following exception was thrown during execution in test generation
        try {
            com.group10.scheduler.booking.Payment payment11 = schedulerFacade3.findPaymentByBookingId(" |  | hi! | rate=$20.0/hr | orgId=-1");
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"com.group10.scheduler.booking.BookingManager.findPaymentByBookingId(String)\" because \"this.bookingManager\" is null");
        } catch (java.lang.NullPointerException e) {
        // Expected exception.
        }
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean6 + "' != '" + false + "'", boolean6 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean9 + "' != '" + false + "'", boolean9 == false);
    }

    @Test
    public void test4038() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4038");
        com.group10.scheduler.booking.PaymentMethod paymentMethod3 = com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD;
        com.group10.scheduler.booking.PaymentStatus paymentStatus4 = com.group10.scheduler.booking.PaymentStatus.REFUNDED;
        com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy debitCardStrategy8 = new com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy("hi!", "hi! |  | hi! | rate=$20.0/hr | orgId=-1");
        com.group10.scheduler.booking.Payment payment9 = new com.group10.scheduler.booking.Payment("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0", "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100", 1.0d, paymentMethod3, paymentStatus4, "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100", (com.group10.scheduler.booking.PaymentStrategy) debitCardStrategy8);
        java.lang.String str10 = payment9.getBookingId();
        boolean boolean12 = payment9.processDeposit(50.0d);
        java.lang.String str13 = payment9.toString();
        com.group10.scheduler.booking.PaymentStatus paymentStatus14 = payment9.getStatus();
        boolean boolean15 = payment9.refundPayment();
        org.junit.Assert.assertTrue("'" + paymentMethod3 + "' != '" + com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD + "'", paymentMethod3.equals(com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD));
        org.junit.Assert.assertTrue("'" + paymentStatus4 + "' != '" + com.group10.scheduler.booking.PaymentStatus.REFUNDED + "'", paymentStatus4.equals(com.group10.scheduler.booking.PaymentStatus.REFUNDED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str10 + "' != '" + "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100" + "'", str10.equals("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100"));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean12 + "' != '" + true + "'", boolean12 == true);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str13 + "' != '" + "Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $50.0 | method=DEBIT_CARD | status=PAID" + "'", str13.equals("Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $50.0 | method=DEBIT_CARD | status=PAID"));
        org.junit.Assert.assertTrue("'" + paymentStatus14 + "' != '" + com.group10.scheduler.booking.PaymentStatus.PAID + "'", paymentStatus14.equals(com.group10.scheduler.booking.PaymentStatus.PAID));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean15 + "' != '" + true + "'", boolean15 == true);
    }

    @Test
    public void test4039() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4039");
        com.group10.scheduler.accounts.ChiefEventCoordinator chiefEventCoordinator0 = com.group10.scheduler.accounts.ChiefEventCoordinator.getInstance();
        com.group10.scheduler.accounts.Administrator administrator2 = chiefEventCoordinator0.findExistingAdministrator("");
        com.group10.scheduler.accounts.Administrator administrator4 = chiefEventCoordinator0.findExistingAdministrator("");
        com.group10.scheduler.accounts.Administrator administrator6 = chiefEventCoordinator0.findExistingAdministrator("");
        com.group10.scheduler.room.RoomManager roomManager10 = null;
        // The following exception was thrown during execution in test generation
        try {
            com.group10.scheduler.accounts.Administrator administrator11 = chiefEventCoordinator0.generateAdministratorAccount("Room[] -2026-07-27T14:30:04.935991 | capacity=0 | status=DISABLED", "Booking[] room=hi! |  \u2192  | status=COMPLETED | deposit=$0.0 | hi! |  | hi! | rate=$20.0/hr | orgId=-1 | Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $1.0 | method=DEBIT_CARD | status=PAID | rate=$50.0/hr | orgId=-1", "Booking[] room=hi! |  \u2192 Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $100.0 | method=DEBIT_CARD | status=PAID | status=COMPLETED | deposit=$0.0", roomManager10);
            org.junit.Assert.fail("Expected exception of type java.lang.IllegalStateException; message: RoomManager is null.");
        } catch (java.lang.IllegalStateException e) {
        // Expected exception.
        }
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNotNull(chiefEventCoordinator0);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNull(administrator2);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNull(administrator4);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNull(administrator6);
    }

    @Test
    public void test4040() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4040");
        com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy debitCardStrategy2 = new com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy(" | Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $1.0 | method=DEBIT_CARD | status=PAID | Room[] Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 |  | rate=$40.0/hr | orgId=100-hi! | capacity=0 | status=AVAILABLE | rate=$40.0/hr | orgId=100", "Booking[] room=hi! |  \u2192  | status=COMPLETED | deposit=$0.0 | hi! |  | hi! | rate=$20.0/hr | orgId=-1 | Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $1.0 | method=DEBIT_CARD | status=PAID | rate=$50.0/hr | orgId=-1");
    }

    @Test
    public void test4041() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4041");
        com.group10.scheduler.booking.ConcreteStates.CompletedState completedState0 = new com.group10.scheduler.booking.ConcreteStates.CompletedState();
        com.group10.scheduler.booking.ConcreteStates.CancelledState cancelledState1 = new com.group10.scheduler.booking.ConcreteStates.CancelledState();
        com.group10.scheduler.booking.Booking booking2 = null;
        boolean boolean5 = cancelledState1.edit(booking2, "hi!", "");
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState6 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.BookingStatus bookingStatus13 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking14 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus13);
        boolean boolean17 = booking14.editBooking("", "hi!");
        confirmedState6.complete(booking14);
        boolean boolean21 = booking14.editBooking("hi!", "hi!");
        boolean boolean22 = cancelledState1.checkIn(booking14);
        com.group10.scheduler.booking.BookingStatus bookingStatus29 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking30 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus29);
        boolean boolean31 = booking30.cancelBooking();
        booking30.setStartTime("hi!");
        boolean boolean34 = booking30.cancelBooking();
        boolean boolean35 = cancelledState1.cancel(booking30);
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState36 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState37 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.BookingStatus bookingStatus44 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking45 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus44);
        boolean boolean48 = booking45.editBooking("", "hi!");
        confirmedState37.complete(booking45);
        boolean boolean52 = booking45.editBooking("hi!", "hi!");
        boolean boolean53 = confirmedState36.checkIn(booking45);
        boolean boolean54 = cancelledState1.checkIn(booking45);
        boolean boolean55 = completedState0.checkIn(booking45);
        java.lang.String str56 = booking45.getCheckInTime();
        java.lang.String str57 = booking45.getCheckInTime();
        boolean boolean58 = booking45.checkIn();
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean5 + "' != '" + false + "'", boolean5 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus13 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus13.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean17 + "' != '" + false + "'", boolean17 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean21 + "' != '" + false + "'", boolean21 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean22 + "' != '" + false + "'", boolean22 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus29 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus29.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean31 + "' != '" + false + "'", boolean31 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean34 + "' != '" + false + "'", boolean34 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean35 + "' != '" + false + "'", boolean35 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus44 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus44.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean48 + "' != '" + false + "'", boolean48 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean52 + "' != '" + false + "'", boolean52 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean53 + "' != '" + true + "'", boolean53 == true);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean54 + "' != '" + false + "'", boolean54 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean55 + "' != '" + false + "'", boolean55 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNull(str56);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNull(str57);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean58 + "' != '" + false + "'", boolean58 == false);
    }

    @Test
    public void test4042() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4042");
        com.group10.scheduler.booking.ConcreteStates.ExpiredState expiredState0 = new com.group10.scheduler.booking.ConcreteStates.ExpiredState();
        com.group10.scheduler.booking.ConcreteStates.CompletedState completedState1 = new com.group10.scheduler.booking.ConcreteStates.CompletedState();
        com.group10.scheduler.booking.ConcreteStates.ExpiredState expiredState2 = new com.group10.scheduler.booking.ConcreteStates.ExpiredState();
        com.group10.scheduler.booking.BookingStatus bookingStatus9 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking10 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus9);
        boolean boolean11 = booking10.cancelBooking();
        expiredState2.complete(booking10);
        boolean boolean15 = completedState1.edit(booking10, "", "");
        com.group10.scheduler.booking.ConcreteStates.CompletedState completedState16 = new com.group10.scheduler.booking.ConcreteStates.CompletedState();
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState17 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState18 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.BookingStatus bookingStatus25 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking26 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus25);
        boolean boolean29 = booking26.editBooking("", "hi!");
        confirmedState18.complete(booking26);
        boolean boolean33 = booking26.editBooking("hi!", "hi!");
        boolean boolean34 = confirmedState17.checkIn(booking26);
        com.group10.scheduler.booking.BookingState bookingState35 = booking26.getState();
        completedState16.expire(booking26);
        java.lang.String str37 = booking26.getEndTime();
        boolean boolean38 = completedState1.cancel(booking26);
        boolean boolean39 = expiredState0.cancel(booking26);
        com.group10.scheduler.booking.ConcreteStates.ExpiredState expiredState40 = new com.group10.scheduler.booking.ConcreteStates.ExpiredState();
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState41 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState42 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.BookingStatus bookingStatus49 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking50 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus49);
        boolean boolean53 = booking50.editBooking("", "hi!");
        confirmedState42.complete(booking50);
        boolean boolean57 = booking50.editBooking("hi!", "hi!");
        boolean boolean58 = confirmedState41.checkIn(booking50);
        java.lang.String str59 = booking50.toString();
        expiredState40.expire(booking50);
        boolean boolean61 = expiredState0.cancel(booking50);
        org.junit.Assert.assertTrue("'" + bookingStatus9 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus9.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean11 + "' != '" + false + "'", boolean11 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean15 + "' != '" + false + "'", boolean15 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus25 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus25.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean29 + "' != '" + false + "'", boolean29 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean33 + "' != '" + false + "'", boolean33 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean34 + "' != '" + true + "'", boolean34 == true);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNotNull(bookingState35);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str37 + "' != '" + "" + "'", str37.equals(""));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean38 + "' != '" + false + "'", boolean38 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean39 + "' != '" + false + "'", boolean39 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus49 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus49.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean53 + "' != '" + false + "'", boolean53 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean57 + "' != '" + false + "'", boolean57 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean58 + "' != '" + true + "'", boolean58 == true);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str59 + "' != '" + "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0" + "'", str59.equals("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0"));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean61 + "' != '" + false + "'", boolean61 == false);
    }

    @Test
    public void test4043() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4043");
        com.group10.scheduler.accounts.Faculty faculty5 = new com.group10.scheduler.accounts.Faculty("Booking[] room=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! \u2192 Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | status=COMPLETED | deposit=$32.0", "Booking[] room=hi! |  \u2192  | status=CONFIRMED | deposit=$0.0", "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 |  | rate=$40.0/hr | orgId=100 | hi! |  | hi! | rate=$20.0/hr | orgId=-1 | hi! | Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | rate=$50.0/hr | orgId=97 | Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | rate=$20.0/hr | orgId=32", "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 |  | Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | rate=$40.0/hr | orgId=0", (long) (short) -1);
        long long6 = faculty5.getOrganizationId();
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + long6 + "' != '" + (-1L) + "'", long6 == (-1L));
    }

    @Test
    public void test4044() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4044");
        com.group10.scheduler.room.RoomManager roomManager0 = null;
        com.group10.scheduler.booking.BookingManager bookingManager1 = null;
        com.group10.scheduler.accounts.AccountManagement accountManagement2 = null;
        com.group10.scheduler.facade.SchedulerFacade schedulerFacade3 = new com.group10.scheduler.facade.SchedulerFacade(roomManager0, bookingManager1, accountManagement2);
        boolean boolean6 = schedulerFacade3.disableRoom("hi!", "");
        boolean boolean9 = schedulerFacade3.closeRoom("hi! |  | hi! | rate=$20.0/hr | orgId=-1", "hi! |  | hi! | rate=$20.0/hr | orgId=-1");
        boolean boolean12 = schedulerFacade3.closeRoom("Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $0.0 | method=DEBIT_CARD | status=FAILED", "Room[] -hi! | capacity=0 | status=AVAILABLE");
        // The following exception was thrown during execution in test generation
        try {
            java.util.List<com.group10.scheduler.room.Room> roomList13 = schedulerFacade3.getAllRooms();
            org.junit.Assert.fail("Expected exception of type java.lang.NullPointerException; message: Cannot invoke \"com.group10.scheduler.room.RoomManager.getAllRooms()\" because \"this.roomManager\" is null");
        } catch (java.lang.NullPointerException e) {
        // Expected exception.
        }
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean6 + "' != '" + false + "'", boolean6 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean9 + "' != '" + false + "'", boolean9 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean12 + "' != '" + false + "'", boolean12 == false);
    }

    @Test
    public void test4045() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4045");
        com.group10.scheduler.accounts.Partner partner5 = new com.group10.scheduler.accounts.Partner("hi! |  | hi! | rate=$20.0/hr | orgId=-1 | hi! | Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | rate=$50.0/hr | orgId=97", "2026-07-27T14:30:04.935991", "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 |  | Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | rate=$40.0/hr | orgId=0", "hi! |  | hi! | rate=$20.0/hr | orgId=-1 | Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $1.0 | method=DEBIT_CARD | status=REFUNDED | Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $1.0 | method=DEBIT_CARD | status=REFUNDED | rate=$50.0/hr | orgId=1", (long) '#');
    }

    @Test
    public void test4046() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4046");
        com.group10.scheduler.booking.ConcreteStates.ExpiredState expiredState0 = new com.group10.scheduler.booking.ConcreteStates.ExpiredState();
        com.group10.scheduler.booking.BookingStatus bookingStatus7 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking8 = new com.group10.scheduler.booking.Booking("Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $1.0 | method=DEBIT_CARD | status=REFUNDED", "hi! |  | hi! | rate=$20.0/hr | orgId=-1", "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100", "", "hi! |  | hi! | rate=$20.0/hr | orgId=-1", (double) '4', bookingStatus7);
        boolean boolean10 = expiredState0.extend(booking8, "Room[] Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $1.0 | method=DEBIT_CARD | status=PAID-Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | capacity=0 | status=OCCUPIED");
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState11 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.BookingStatus bookingStatus18 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking19 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus18);
        boolean boolean22 = booking19.editBooking("", "hi!");
        confirmedState11.complete(booking19);
        com.group10.scheduler.booking.BookingStatus bookingStatus30 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking31 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus30);
        boolean boolean32 = booking31.cancelBooking();
        confirmedState11.expire(booking31);
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState34 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.BookingStatus bookingStatus41 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking42 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus41);
        boolean boolean45 = booking42.editBooking("", "hi!");
        confirmedState34.complete(booking42);
        boolean boolean49 = booking42.editBooking("hi!", "hi!");
        confirmedState11.complete(booking42);
        com.group10.scheduler.booking.ConcreteStates.CancelledState cancelledState51 = new com.group10.scheduler.booking.ConcreteStates.CancelledState();
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState52 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.BookingStatus bookingStatus59 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking60 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus59);
        boolean boolean63 = booking60.editBooking("", "hi!");
        confirmedState52.complete(booking60);
        com.group10.scheduler.booking.BookingState bookingState65 = booking60.getState();
        boolean boolean66 = cancelledState51.checkIn(booking60);
        boolean boolean68 = confirmedState11.extend(booking60, "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0");
        com.group10.scheduler.booking.BookingStatus bookingStatus75 = com.group10.scheduler.booking.BookingStatus.COMPLETED;
        com.group10.scheduler.booking.Booking booking76 = new com.group10.scheduler.booking.Booking("", "", "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0", "hi!", "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0", (double) ' ', bookingStatus75);
        java.lang.String str77 = booking76.getRoomId();
        boolean boolean78 = booking76.isDepositForfeited();
        boolean boolean80 = confirmedState11.extend(booking76, "");
        java.lang.String str81 = booking76.toString();
        java.lang.String str82 = booking76.getRoomId();
        java.lang.String str83 = booking76.getCheckInTime();
        expiredState0.expire(booking76);
        org.junit.Assert.assertTrue("'" + bookingStatus7 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus7.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean10 + "' != '" + false + "'", boolean10 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus18 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus18.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean22 + "' != '" + false + "'", boolean22 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus30 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus30.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean32 + "' != '" + false + "'", boolean32 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus41 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus41.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean45 + "' != '" + false + "'", boolean45 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean49 + "' != '" + false + "'", boolean49 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus59 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus59.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean63 + "' != '" + false + "'", boolean63 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNotNull(bookingState65);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean66 + "' != '" + false + "'", boolean66 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean68 + "' != '" + false + "'", boolean68 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus75 + "' != '" + com.group10.scheduler.booking.BookingStatus.COMPLETED + "'", bookingStatus75.equals(com.group10.scheduler.booking.BookingStatus.COMPLETED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str77 + "' != '" + "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0" + "'", str77.equals("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0"));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean78 + "' != '" + false + "'", boolean78 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean80 + "' != '" + false + "'", boolean80 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str81 + "' != '" + "Booking[] room=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! \u2192 Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | status=COMPLETED | deposit=$32.0" + "'", str81.equals("Booking[] room=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! \u2192 Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | status=COMPLETED | deposit=$32.0"));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str82 + "' != '" + "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0" + "'", str82.equals("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0"));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertNull(str83);
    }

    @Test
    public void test4047() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4047");
        com.group10.scheduler.booking.ConcreteStates.ExpiredState expiredState0 = new com.group10.scheduler.booking.ConcreteStates.ExpiredState();
        com.group10.scheduler.booking.BookingStatus bookingStatus7 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking8 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus7);
        boolean boolean9 = expiredState0.checkIn(booking8);
        com.group10.scheduler.booking.ConcreteStates.ConfirmedState confirmedState10 = new com.group10.scheduler.booking.ConcreteStates.ConfirmedState();
        com.group10.scheduler.booking.BookingStatus bookingStatus17 = com.group10.scheduler.booking.BookingStatus.CONFIRMED;
        com.group10.scheduler.booking.Booking booking18 = new com.group10.scheduler.booking.Booking("", "hi!", "hi!", "", "", (double) 0, bookingStatus17);
        boolean boolean21 = booking18.editBooking("", "hi!");
        confirmedState10.complete(booking18);
        boolean boolean25 = booking18.editBooking("hi!", "hi!");
        booking18.setDepositForfeited(false);
        boolean boolean28 = expiredState0.checkIn(booking18);
        boolean boolean31 = booking18.editBooking("Room[] Payment[Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0] booking=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100 | $1.0 | method=DEBIT_CARD | status=PAID-Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | capacity=0 | status=OCCUPIED", "Booking[] room=Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! \u2192 Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | status=COMPLETED | deposit=$32.0");
        org.junit.Assert.assertTrue("'" + bookingStatus7 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus7.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean9 + "' != '" + false + "'", boolean9 == false);
        org.junit.Assert.assertTrue("'" + bookingStatus17 + "' != '" + com.group10.scheduler.booking.BookingStatus.CONFIRMED + "'", bookingStatus17.equals(com.group10.scheduler.booking.BookingStatus.CONFIRMED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean21 + "' != '" + false + "'", boolean21 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean25 + "' != '" + false + "'", boolean25 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean28 + "' != '" + false + "'", boolean28 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean31 + "' != '" + false + "'", boolean31 == false);
    }

    @Test
    public void test4048() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4048");
        com.group10.scheduler.room.RoomSensorSystem roomSensorSystem1 = new com.group10.scheduler.room.RoomSensorSystem("Booking[] room=hi! | 2026-07-27T14:30:04.935991 \u2192  | status=CONFIRMED | deposit=$0.0");
        boolean boolean2 = roomSensorSystem1.getOccupied();
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean2 + "' != '" + false + "'", boolean2 == false);
    }

    @Test
    public void test4049() throws Throwable {
        if (debug)
            System.out.format("%n%s%n", "RegressionTest8.test4049");
        com.group10.scheduler.booking.PaymentMethod paymentMethod3 = com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD;
        com.group10.scheduler.booking.PaymentStatus paymentStatus4 = com.group10.scheduler.booking.PaymentStatus.REFUNDED;
        com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy debitCardStrategy8 = new com.group10.scheduler.booking.ConcreteStrategies.DebitCardStrategy("hi!", "hi! |  | hi! | rate=$20.0/hr | orgId=-1");
        com.group10.scheduler.booking.Payment payment9 = new com.group10.scheduler.booking.Payment("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0", "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100", 1.0d, paymentMethod3, paymentStatus4, "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100", (com.group10.scheduler.booking.PaymentStrategy) debitCardStrategy8);
        boolean boolean11 = payment9.processPayment((double) 0.0f);
        java.lang.String str12 = payment9.getBookingId();
        java.lang.String str13 = payment9.getBookingId();
        java.lang.String str14 = payment9.getPaymentId();
        com.group10.scheduler.booking.PaymentStatus paymentStatus15 = payment9.getStatus();
        boolean boolean16 = payment9.refundPayment();
        org.junit.Assert.assertTrue("'" + paymentMethod3 + "' != '" + com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD + "'", paymentMethod3.equals(com.group10.scheduler.booking.PaymentMethod.DEBIT_CARD));
        org.junit.Assert.assertTrue("'" + paymentStatus4 + "' != '" + com.group10.scheduler.booking.PaymentStatus.REFUNDED + "'", paymentStatus4.equals(com.group10.scheduler.booking.PaymentStatus.REFUNDED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean11 + "' != '" + false + "'", boolean11 == false);
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str12 + "' != '" + "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100" + "'", str12.equals("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100"));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str13 + "' != '" + "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100" + "'", str13.equals("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0 | hi! | hi! | rate=$50.0/hr | orgId=100"));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + str14 + "' != '" + "Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0" + "'", str14.equals("Booking[] room=hi! |  \u2192  | status=CHECKED_IN | deposit=$0.0"));
        org.junit.Assert.assertTrue("'" + paymentStatus15 + "' != '" + com.group10.scheduler.booking.PaymentStatus.FAILED + "'", paymentStatus15.equals(com.group10.scheduler.booking.PaymentStatus.FAILED));
        // Regression assertion (captures the current behavior of the code)
        org.junit.Assert.assertTrue("'" + boolean16 + "' != '" + false + "'", boolean16 == false);
    }
}

