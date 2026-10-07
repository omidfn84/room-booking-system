package scheduler.web;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Remembers who is signed in.
 *
 * The browser only ever holds a random token (in a cookie). Everything else
 * stays on the server in this map, so a visitor cannot edit their own role or
 * email. Sessions live in memory and are forgotten when the server restarts.
 */
final class SessionStore {

    /** One signed-in browser. Either a registered user or an administrator. */
    static final class Session {
        final String token;          // the random value stored in the cookie
        final String email;          // the user's email, or null for an administrator
        final String adminId;        // the administrator id, or null for a user
        final String name;           // display name
        final String accountType;    // STUDENT / FACULTY / STAFF / PARTNER, or ADMIN
        final double hourlyRate;     // shown in the page; 0 for an administrator
        final long organizationId;   // used for institutional billing in the demo
        final boolean demoAccount;   // true for the one-click demo accounts, false for an account someone registered
        private volatile long expiresAt; // time in milliseconds after which the session is dead

        Session(String token, String email, String adminId, String name, String accountType,
                double hourlyRate, long organizationId, boolean demoAccount) {
            this.token = token;
            this.email = email;
            this.adminId = adminId;
            this.name = name;
            this.accountType = accountType;
            this.hourlyRate = hourlyRate;
            this.organizationId = organizationId;
            this.demoAccount = demoAccount;
        }

        boolean isAdmin() {
            return adminId != null;
        }
    }

    static final long IDLE_MILLIS = 2 * 60 * 60 * 1000L; // a session ends after 2 hours without a request
    private static final int MAX_SESSIONS = 2000;         // upper bound so the map cannot grow forever

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom(); // cryptographically strong, so tokens cannot be guessed

    /** A session for someone who signed in with their own email and password. */
    Session createUserSession(String email, String name, String accountType, double hourlyRate, long organizationId) {
        return store(new Session(newToken(), email, null, name, accountType, hourlyRate, organizationId, false));
    }

    /** A session for a visitor who clicked one of the demo accounts. */
    Session createDemoUserSession(String email, String name, String accountType, double hourlyRate, long organizationId) {
        return store(new Session(newToken(), email, null, name, accountType, hourlyRate, organizationId, true));
    }

    /** On the web the only administrator is the demo one, so an admin session is always a demo session. */
    Session createAdminSession(String adminId, String name) {
        return store(new Session(newToken(), null, adminId, name, "ADMIN", 0, 0, true));
    }

    /** Returns the live session for a cookie token, or null. Each hit pushes the expiry back. */
    Session find(String token) {
        if (token == null) {
            return null;
        }
        Session session = sessions.get(token);
        if (session == null) {
            return null;
        }
        long now = System.currentTimeMillis();
        if (session.expiresAt < now) {      // too old: forget it
            sessions.remove(token);
            return null;
        }
        session.expiresAt = now + IDLE_MILLIS;
        return session;
    }

    void remove(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    int size() {
        return sessions.size();
    }

    private Session store(Session session) {
        if (sessions.size() >= MAX_SESSIONS) {
            makeRoom();
        }
        session.expiresAt = System.currentTimeMillis() + IDLE_MILLIS;
        sessions.put(session.token, session);
        return session;
    }

    // Drops expired sessions; if the map is still full, drops the one closest to expiring.
    private void makeRoom() {
        long now = System.currentTimeMillis();
        Session oldest = null;
        for (Iterator<Session> it = sessions.values().iterator(); it.hasNext();) {
            Session s = it.next();
            if (s.expiresAt < now) {
                it.remove();
            } else if (oldest == null || s.expiresAt < oldest.expiresAt) {
                oldest = s;
            }
        }
        if (sessions.size() >= MAX_SESSIONS && oldest != null) {
            sessions.remove(oldest.token);
        }
    }

    // 32 random bytes = 256 bits, written in URL-safe base64 so it fits in a cookie.
    private String newToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
