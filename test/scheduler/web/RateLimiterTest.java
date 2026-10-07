package scheduler.web;

import static org.junit.Assert.*;

import org.junit.Test;

/** Tests for the limiter that caps log-in and sign-up attempts. Time is a variable here, not the real clock. */
public class RateLimiterTest {

    private long now = 1_000_000;   // the fake clock's current time in milliseconds

    private RateLimiter limiter(int limit) {
        return new RateLimiter(limit, 60_000, () -> now);
    }

    @Test
    public void allowsExactlyTheLimitWithinOneWindow() {
        RateLimiter limiter = limiter(3);
        assertTrue(limiter.tryAcquire());
        assertTrue(limiter.tryAcquire());
        assertTrue(limiter.tryAcquire());
        assertFalse("the 4th use in the same minute is refused", limiter.tryAcquire());
        assertFalse(limiter.tryAcquire());
    }

    @Test
    public void staysClosedUntilTheWindowIsOver() {
        RateLimiter limiter = limiter(1);
        assertTrue(limiter.tryAcquire());
        now += 59_999;                              // one millisecond before the minute is up
        assertFalse(limiter.tryAcquire());
        now += 1;                                   // the minute is up
        assertTrue(limiter.tryAcquire());
        assertFalse("the new window has the same limit", limiter.tryAcquire());
    }

    @Test
    public void refusedUsesDoNotExtendTheWindow() {
        RateLimiter limiter = limiter(1);
        assertTrue(limiter.tryAcquire());
        for (int i = 0; i < 100; i++) {
            now += 500;                             // someone keeps hammering for 50 seconds
            assertFalse(limiter.tryAcquire());
        }
        now += 10_000;                              // 60 seconds after the first use
        assertTrue(limiter.tryAcquire());
    }

    @Test
    public void aLimitOfZeroAllowsNothing() {
        assertFalse(limiter(0).tryAcquire());
    }

    @Test
    public void worksWithTheRealClockToo() {
        RateLimiter limiter = new RateLimiter(2, 60_000);
        assertTrue(limiter.tryAcquire());
        assertTrue(limiter.tryAcquire());
        assertFalse(limiter.tryAcquire());
    }
}
