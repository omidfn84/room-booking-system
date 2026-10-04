package scheduler.booking;

import static org.junit.Assert.*;

import org.junit.Test;

import scheduler.aisupport.AIFixture;

/**
 * Booking is the «Context» of the State pattern. Its own responsibilities are
 * narrow: hold the booking data, keep the BookingState object and the
 * BookingStatus enum in agreement, delegate the four lifecycle operations to
 * the current state, and compute cost (Req3) and remaining balance (Req4).
 *
 * These tests focus on that contract. The transition RULES themselves belong
 * to the state classes and are tested in ConcreteStatesAITest.
 */
public class BookingAITest {

    private Booking confirmed() {
        return new Booking("B1", "alice@yorku.ca", "R1",
                AIFixture.futureStart(), AIFixture.futureEnd(), 20.0, BookingStatus.CONFIRMED);
    }

    private Booking withWindow(String start, String end, BookingStatus status) {
        return new Booking("B1", "alice@yorku.ca", "R1", start, end, 20.0, status);
    }

    @Test
    public void constructor_storesEveryFieldItWasGiven() {
        Booking booking = new Booking("B9", "bob@yorku.ca", "R7", "2030-01-01T09:00", "2030-01-01T11:00", 30.0, BookingStatus.CONFIRMED);

        assertEquals("B9", booking.getBookingId());
        assertEquals("bob@yorku.ca", booking.getUserEmail());
        assertEquals("R7", booking.getRoomId());
        assertEquals("2030-01-01T09:00", booking.getStartTime());
        assertEquals("2030-01-01T11:00", booking.getEndTime());
        assertEquals(30.0, booking.getDepositAmount(), 0.001);
    }

    @Test
    public void constructor_derivesTheStateObjectFromTheStatusItWasGiven() {
        // The status -> state mapping is Booking's job (stateOf). Each enum
        // constant must produce the matching ConcreteStates class.
        assertTrue(withWindow("2030-01-01T09:00", "2030-01-01T11:00", BookingStatus.CONFIRMED)
                .getState() instanceof ConcreteStates.ConfirmedState);
        assertTrue(withWindow("2030-01-01T09:00", "2030-01-01T11:00", BookingStatus.CHECKED_IN)
                .getState() instanceof ConcreteStates.CheckedInState);
        assertTrue(withWindow("2030-01-01T09:00", "2030-01-01T11:00", BookingStatus.CANCELLED)
                .getState() instanceof ConcreteStates.CancelledState);
        assertTrue(withWindow("2030-01-01T09:00", "2030-01-01T11:00", BookingStatus.COMPLETED)
                .getState() instanceof ConcreteStates.CompletedState);
        assertTrue(withWindow("2030-01-01T09:00", "2030-01-01T11:00", BookingStatus.EXPIRED)
                .getState() instanceof ConcreteStates.ExpiredState);
    }

    @Test
    public void constructor_startsWithTheDepositNotForfeitedAndNoCheckInTime() {
        Booking booking = confirmed();
        assertFalse(booking.isDepositForfeited());
        assertNull(booking.getCheckInTime());
    }

    @Test
    public void setState_updatesTheStatusEnumInTheSameBreath() {
        // The whole point of statusOf(): the state object and the enum can
        // never be allowed to disagree, because persistence writes the enum
        // while behaviour comes from the object.
        Booking booking = confirmed();
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());

        booking.setState(new ConcreteStates.CancelledState());

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertTrue(booking.getState() instanceof ConcreteStates.CancelledState);
    }

    @Test
    public void setState_mapsEveryConcreteStateBackToItsStatus() {
        Booking booking = confirmed();

        booking.setState(new ConcreteStates.CheckedInState());
        assertEquals(BookingStatus.CHECKED_IN, booking.getStatus());

        booking.setState(new ConcreteStates.CompletedState());
        assertEquals(BookingStatus.COMPLETED, booking.getStatus());

        booking.setState(new ConcreteStates.ExpiredState());
        assertEquals(BookingStatus.EXPIRED, booking.getStatus());

        booking.setState(new ConcreteStates.ConfirmedState());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setState_rejectsNullRatherThanCorruptingTheBooking() {
        confirmed().setState(null);
    }

    @Test
    public void setState_rejectsAStateClassItDoesNotRecognise() {
        // statusOf() throws on an unknown implementation. Anonymous
        // BookingState implementations are exactly that case: a booking whose
        // status could not be persisted must not be allowed to exist.
        Booking booking = confirmed();
        BookingState unknown = new BookingState() {
            public boolean checkIn(Booking b) { return false; }
            public boolean cancel(Booking b) { return false; }
            public boolean edit(Booking b, String s, String e) { return false; }
            public boolean extend(Booking b, String u) { return false; }
            public void expire(Booking b) { }
            public void complete(Booking b) { }
        };

        try {
            booking.setState(unknown);
            fail("Expected IllegalArgumentException for an unrecognised state class");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Unknown booking state"));
        }
    }

    @Test
    public void checkIn_recordsATimestampWhenTheStateAllowsIt() {
        Booking booking = confirmed();

        assertTrue(booking.checkIn());
        assertNotNull("A successful check-in must stamp the time (Req4 needs it)", booking.getCheckInTime());
        assertEquals(BookingStatus.CHECKED_IN, booking.getStatus());
    }

    @Test
    public void checkIn_leavesTheTimestampNullWhenTheStateRefuses() {
        // A cancelled booking cannot be checked into, so no timestamp may be
        // written - calculateRemainingBalance() keys off exactly this field.
        Booking booking = withWindow(AIFixture.futureStart(), AIFixture.futureEnd(), BookingStatus.CANCELLED);

        assertFalse(booking.checkIn());
        assertNull(booking.getCheckInTime());
    }

    @Test
    public void lifecycleMethods_delegateToTheCurrentState() {
        // Same call, opposite answers, decided purely by the state object.
        Booking open = withWindow(AIFixture.futureStart(), AIFixture.futureEnd(), BookingStatus.CONFIRMED);
        Booking closed = withWindow(AIFixture.futureStart(), AIFixture.futureEnd(), BookingStatus.CANCELLED);

        assertTrue(open.cancelBooking());
        assertFalse(closed.cancelBooking());

        assertFalse(closed.editBooking(AIFixture.futureStart(), AIFixture.futureEnd()));
        assertFalse(closed.extendBooking(AIFixture.hoursFromNow(9)));
    }

    @Test
    public void calculateFinalCost_multipliesDurationInHoursByTheHourlyRate() {
        // Req3/Req4: depositAmount doubles as the hourly rate, since the
        // deposit is defined as exactly one hour's fee.
        Booking twoHours = new Booking("B1", "a@b.ca", "R1", "2030-01-01T09:00", "2030-01-01T11:00", 20.0, BookingStatus.CONFIRMED);
        assertEquals(40.0, twoHours.calculateFinalCost(), 0.001);
    }

    @Test
    public void calculateFinalCost_handlesFractionalHours() {
        Booking ninetyMinutes = new Booking("B1", "a@b.ca", "R1", "2030-01-01T09:00", "2030-01-01T10:30", 20.0, BookingStatus.CONFIRMED);
        assertEquals(30.0, ninetyMinutes.calculateFinalCost(), 0.001);
    }

    @Test
    public void calculateFinalCost_isZeroWhenTheWindowIsInvertedOrEmpty() {
        Booking inverted = new Booking("B1", "a@b.ca", "R1", "2030-01-01T11:00", "2030-01-01T09:00", 20.0, BookingStatus.CONFIRMED);
        assertEquals(0.0, inverted.calculateFinalCost(), 0.001);

        Booking zeroLength = new Booking("B2", "a@b.ca", "R1", "2030-01-01T09:00", "2030-01-01T09:00", 20.0, BookingStatus.CONFIRMED);
        assertEquals(0.0, zeroLength.calculateFinalCost(), 0.001);
    }

    @Test
    public void calculateRemainingBalance_appliesTheDepositOnlyAfterAnActualCheckIn() {
        // Req4: "Otherwise, it is applied to the final cost."
        Booking booking = new Booking("B1", "a@b.ca", "R1", "2030-01-01T09:00", "2030-01-01T11:00", 20.0, BookingStatus.CONFIRMED);

        // no check-in yet -> full price
        assertEquals(40.0, booking.calculateRemainingBalance(), 0.001);

        booking.setCheckInTime("2030-01-01T09:05");
        assertEquals("Deposit must come off once the user has checked in", 20.0, booking.calculateRemainingBalance(), 0.001);
    }

    @Test
    public void calculateRemainingBalance_ignoresTheDepositOnceItHasBeenForfeited() {
        // Req4: "If the user does not check in within 30 minutes of the start
        // time, the deposit is lost" - a lost deposit must not also be credited.
        Booking booking = new Booking("B1", "a@b.ca", "R1", "2030-01-01T09:00", "2030-01-01T11:00", 20.0, BookingStatus.CONFIRMED);
        booking.setCheckInTime("2030-01-01T09:05");
        booking.forfeitDeposit();

        assertTrue(booking.isDepositForfeited());
        assertEquals(40.0, booking.calculateRemainingBalance(), 0.001);
    }

    @Test
    public void forfeitDeposit_isRecordedAsAFlagAndDoesNotZeroTheAmount() {
        // The money was already collected; forfeiting means "not refunded",
        // not "never charged". The amount must survive for the receipt.
        Booking booking = confirmed();

        booking.forfeitDeposit();

        assertTrue(booking.isDepositForfeited());
        assertEquals(20.0, booking.getDepositAmount(), 0.001);
    }

    @Test
    public void settersUsedByPersistenceRoundTripTheirValues() {
        Booking booking = confirmed();

        booking.setStartTime("2031-05-05T08:00");
        booking.setEndTime("2031-05-05T09:00");
        booking.setCheckInTime("2031-05-05T08:10");
        booking.setDepositForfeited(true);

        assertEquals("2031-05-05T08:00", booking.getStartTime());
        assertEquals("2031-05-05T09:00", booking.getEndTime());
        assertEquals("2031-05-05T08:10", booking.getCheckInTime());
        assertTrue(booking.isDepositForfeited());
    }

    @Test
    public void toString_showsTheFieldsAHumanNeedsToIdentifyTheBooking() {
        String text = confirmed().toString();

        assertTrue(text.contains("B1"));
        assertTrue(text.contains("R1"));
        assertTrue(text.contains("CONFIRMED"));
        assertTrue(text.contains("20.0"));
    }
}
