package scheduler.web;

import java.util.function.LongSupplier;

/**
 * Allows an action at most N times per time window, counted for the whole server.
 *
 * It protects sign-in and sign-up: checking a password is deliberately slow
 * (PBKDF2), so without a limit anyone could keep the server busy just by
 * sending login attempts.
 */
final class RateLimiter {

    private final int limit;              // how many times the action may happen per window
    private final long windowMillis;      // length of one window in milliseconds
    private final LongSupplier clock;     // where "now" comes from; tests pass in a fake clock

    private long windowStart;             // when the current window began
    private int used;                     // how many times the action happened in this window

    RateLimiter(int limit, long windowMillis) {
        this(limit, windowMillis, System::currentTimeMillis);
    }

    RateLimiter(int limit, long windowMillis, LongSupplier clock) {
        this.limit = limit;
        this.windowMillis = windowMillis;
        this.clock = clock;
        this.windowStart = clock.getAsLong();
    }

    /** Returns true and counts one use if the limit is not reached yet; otherwise returns false. */
    synchronized boolean tryAcquire() {
        long now = clock.getAsLong();
        if (now - windowStart >= windowMillis) {   // the old window is over: start a new one
            windowStart = now;
            used = 0;
        }
        if (used >= limit) {
            return false;
        }
        used++;
        return true;
    }
}
