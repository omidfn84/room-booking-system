package scheduler.booking;

import static org.junit.Assert.*;

import org.junit.Test;

import scheduler.aisupport.AIFixture;

/**
 * The five «ConcreteState» classes hold the booking lifecycle rules that would
 * otherwise be an if/else chain inside Booking:
 *
 *   Req8 - edit and cancel only BEFORE the start time
 *   Req9 - extend only before expiry, and only to a LATER end time
 *   Req4 - expiring a confirmed booking forfeits the deposit
 *
 * Three of the states (Cancelled, Completed, Expired) are terminal: every
 * operation must be refused. Those are tested exhaustively rather than
 * sampled, because a single method accidentally returning true would let a
 * finished booking be resurrected.
 */
public class ConcreteStatesAITest {

    private Booking booking(String start, String end, BookingStatus status) {
        return new Booking("B1", "alice@yorku.ca", "R1", start, end, 20.0, status);
    }

    /** A booking that has not started yet - the editable/cancellable window. */
    private Booking future(BookingStatus status) {
        return booking(AIFixture.futureStart(), AIFixture.futureEnd(), status);
    }

    /** A booking whose start time has already passed. */
    private Booking started(BookingStatus status) {
        return booking(AIFixture.startedRecently(), AIFixture.hoursFromNow(1), status);
    }

    // ==================== ConfirmedState ====================

    @Test
    public void confirmed_checkInMovesTheBookingToCheckedIn() {
        Booking b = future(BookingStatus.CONFIRMED);

        assertTrue(new ConcreteStates.ConfirmedState().checkIn(b));
        assertEquals(BookingStatus.CHECKED_IN, b.getStatus());
    }

    @Test
    public void confirmed_cancelSucceedsBeforeTheStartTime() {
        // Req8: "Bookings can be edited or canceled before the start time."
        Booking b = future(BookingStatus.CONFIRMED);

        assertTrue(b.cancelBooking());
        assertEquals(BookingStatus.CANCELLED, b.getStatus());
    }

    @Test
    public void confirmed_cancelIsRefusedOnceTheBookingHasStarted() {
        Booking b = started(BookingStatus.CONFIRMED);

        assertFalse(b.cancelBooking());
        assertEquals("A refused cancel must leave the booking untouched", BookingStatus.CONFIRMED, b.getStatus());
    }

    @Test
    public void confirmed_cancelIsRefusedWhenTheStartTimeCannotBeParsed() {
        // parseTime() returns null on garbage, and isBefore() then answers
        // false - an unreadable booking is never treated as still open.
        Booking b = booking("not-a-timestamp", AIFixture.futureEnd(), BookingStatus.CONFIRMED);

        assertFalse(b.cancelBooking());
    }

    @Test
    public void confirmed_editRewritesBothTimesBeforeTheStartTime() {
        Booking b = future(BookingStatus.CONFIRMED);
        String newStart = AIFixture.hoursFromNow(5);
        String newEnd = AIFixture.hoursFromNow(6);

        assertTrue(b.editBooking(newStart, newEnd));
        assertEquals(newStart, b.getStartTime());
        assertEquals(newEnd, b.getEndTime());
    }

    @Test
    public void confirmed_editIsRefusedOnceTheBookingHasStarted() {
        Booking b = started(BookingStatus.CONFIRMED);
        String originalStart = b.getStartTime();

        assertFalse(b.editBooking(AIFixture.hoursFromNow(5), AIFixture.hoursFromNow(6)));
        assertEquals(originalStart, b.getStartTime());
    }

    @Test
    public void confirmed_editRejectsAnInvertedOrEmptyNewWindow() {
        Booking b = future(BookingStatus.CONFIRMED);

        // end before start
        assertFalse(b.editBooking(AIFixture.hoursFromNow(6), AIFixture.hoursFromNow(5)));
        // start equal to end
        String same = AIFixture.hoursFromNow(6);
        assertFalse(b.editBooking(same, same));
    }

    @Test
    public void confirmed_editRejectsUnparseableNewTimes() {
        Booking b = future(BookingStatus.CONFIRMED);

        assertFalse(b.editBooking("garbage", AIFixture.hoursFromNow(6)));
        assertFalse(b.editBooking(AIFixture.hoursFromNow(5), "garbage"));
        assertFalse(b.editBooking(null, null));
    }

    @Test
    public void confirmed_expireForfeitsTheDepositAndMovesToExpired() {
        // Req4: missing the check-in window costs the user the deposit.
        Booking b = future(BookingStatus.CONFIRMED);

        new ConcreteStates.ConfirmedState().expire(b);

        assertTrue("Req4 requires the deposit to be lost on expiry", b.isDepositForfeited());
        assertEquals(BookingStatus.EXPIRED, b.getStatus());
    }

    @Test
    public void confirmed_completeMovesToCompletedWithoutTouchingTheDeposit() {
        Booking b = future(BookingStatus.CONFIRMED);

        new ConcreteStates.ConfirmedState().complete(b);

        assertEquals(BookingStatus.COMPLETED, b.getStatus());
        assertFalse(b.isDepositForfeited());
    }

    // ==================== CheckedInState ====================

    @Test
    public void checkedIn_refusesASecondCheckInAndAnyEditOrCancel() {
        // Once the user is in the room the booking is locked: Req8's edit and
        // cancel window has closed, and checking in twice is meaningless.
        Booking b = started(BookingStatus.CHECKED_IN);

        assertFalse(b.checkIn());
        assertFalse(b.cancelBooking());
        assertFalse(b.editBooking(AIFixture.hoursFromNow(5), AIFixture.hoursFromNow(6)));
    }

    @Test
    public void checkedIn_stillAllowsExtendingWhileTheBookingIsRunning() {
        // Req9 deliberately survives check-in: a user already in the room is
        // exactly who wants more time.
        Booking b = booking(AIFixture.startedRecently(), AIFixture.hoursFromNow(1), BookingStatus.CHECKED_IN);
        String later = AIFixture.hoursFromNow(3);

        assertTrue(b.extendBooking(later));
        assertEquals(later, b.getEndTime());
    }

    @Test
    public void checkedIn_expireIsANoOpBecauseTheUserAlreadyArrived() {
        Booking b = started(BookingStatus.CHECKED_IN);

        new ConcreteStates.CheckedInState().expire(b);

        assertEquals("A checked-in booking must never expire", BookingStatus.CHECKED_IN, b.getStatus());
        assertFalse(b.isDepositForfeited());
    }

    @Test
    public void checkedIn_completeMovesToCompleted() {
        Booking b = started(BookingStatus.CHECKED_IN);

        new ConcreteStates.CheckedInState().complete(b);

        assertEquals(BookingStatus.COMPLETED, b.getStatus());
    }

    // ==================== extend rules (Req9), shared by two states ====================

    @Test
    public void extend_isRefusedWhenTheNewEndIsNotLaterThanTheCurrentEnd() {
        Booking b = booking(AIFixture.startedRecently(), AIFixture.hoursFromNow(2), BookingStatus.CONFIRMED);
        String currentEnd = b.getEndTime();

        assertFalse("Shortening is not extending", b.extendBooking(AIFixture.hoursFromNow(1)));
        assertFalse("Same end time is not an extension", b.extendBooking(currentEnd));
        assertEquals(currentEnd, b.getEndTime());
    }

    @Test
    public void extend_isRefusedOnceTheBookingHasAlreadyExpired() {
        // Req9 says "before expiry" - a booking whose end time has passed is
        // gone, even if the requested new end is in the future.
        Booking b = booking(AIFixture.hoursFromNow(-3), AIFixture.hoursFromNow(-1), BookingStatus.CONFIRMED);

        assertFalse(b.extendBooking(AIFixture.hoursFromNow(2)));
    }

    @Test
    public void extend_isRefusedWhenEitherTimestampIsUnparseable() {
        Booking good = booking(AIFixture.startedRecently(), AIFixture.hoursFromNow(1), BookingStatus.CONFIRMED);
        assertFalse(good.extendBooking("garbage"));

        Booking badEnd = booking(AIFixture.startedRecently(), "garbage", BookingStatus.CONFIRMED);
        assertFalse(badEnd.extendBooking(AIFixture.hoursFromNow(3)));
    }

    // ==================== terminal states ====================

    @Test
    public void cancelled_refusesEveryOperation() {
        Booking b = future(BookingStatus.CANCELLED);
        ConcreteStates.CancelledState state = new ConcreteStates.CancelledState();

        assertFalse(state.checkIn(b));
        assertFalse(state.cancel(b));
        assertFalse(state.edit(b, AIFixture.hoursFromNow(5), AIFixture.hoursFromNow(6)));
        assertFalse(state.extend(b, AIFixture.hoursFromNow(9)));

        // expire and complete are no-ops: a cancelled booking stays cancelled
        state.expire(b);
        assertEquals(BookingStatus.CANCELLED, b.getStatus());
        state.complete(b);
        assertEquals(BookingStatus.CANCELLED, b.getStatus());
        assertFalse(b.isDepositForfeited());
    }

    @Test
    public void completed_refusesEveryOperation() {
        Booking b = future(BookingStatus.COMPLETED);
        ConcreteStates.CompletedState state = new ConcreteStates.CompletedState();

        assertFalse(state.checkIn(b));
        assertFalse(state.cancel(b));
        assertFalse(state.edit(b, AIFixture.hoursFromNow(5), AIFixture.hoursFromNow(6)));
        assertFalse(state.extend(b, AIFixture.hoursFromNow(9)));

        state.expire(b);
        assertEquals(BookingStatus.COMPLETED, b.getStatus());
        state.complete(b);
        assertEquals(BookingStatus.COMPLETED, b.getStatus());
        assertFalse("A paid, completed booking must never forfeit its deposit", b.isDepositForfeited());
    }

    @Test
    public void expired_refusesEveryOperation() {
        Booking b = future(BookingStatus.EXPIRED);
        ConcreteStates.ExpiredState state = new ConcreteStates.ExpiredState();

        assertFalse(state.checkIn(b));
        assertFalse(state.cancel(b));
        assertFalse(state.edit(b, AIFixture.hoursFromNow(5), AIFixture.hoursFromNow(6)));
        assertFalse(state.extend(b, AIFixture.hoursFromNow(9)));

        // expire() must not forfeit the deposit a second time
        state.expire(b);
        assertEquals(BookingStatus.EXPIRED, b.getStatus());
        state.complete(b);
        assertEquals(BookingStatus.EXPIRED, b.getStatus());
    }

    @Test
    public void terminalStates_cannotBeEscapedThroughTheBookingFacadeEither() {
        // Same assertions but driven through Booking's delegating methods,
        // proving the Context adds no accidental back door.
        for (BookingStatus terminal : new BookingStatus[]{
                BookingStatus.CANCELLED, BookingStatus.COMPLETED, BookingStatus.EXPIRED}) {

            Booking b = future(terminal);

            assertFalse(terminal + " must refuse checkIn", b.checkIn());
            assertFalse(terminal + " must refuse cancel", b.cancelBooking());
            assertFalse(terminal + " must refuse edit", b.editBooking(AIFixture.hoursFromNow(5), AIFixture.hoursFromNow(6)));
            assertFalse(terminal + " must refuse extend", b.extendBooking(AIFixture.hoursFromNow(9)));
            assertEquals(terminal, b.getStatus());
        }
    }
}
