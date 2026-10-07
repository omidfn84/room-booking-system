package scheduler.web;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import scheduler.web.SessionStore.Session;

/** Tests for the in-memory session store behind the login cookie. */
public class SessionStoreTest {

    @Test
    public void aUserSessionCanBeFoundByItsToken() {
        SessionStore store = new SessionStore();
        Session created = store.createUserSession("a@yorku.ca", "Alice", "STUDENT", 20.0, 123456789L);
        Session found = store.find(created.token);
        assertSame(created, found);
        assertEquals("a@yorku.ca", found.email);
        assertEquals("STUDENT", found.accountType);
        assertEquals(20.0, found.hourlyRate, 0.0);
        assertEquals(123456789L, found.organizationId);
        assertFalse(found.isAdmin());
    }

    @Test
    public void anAdminSessionHasAnAdminIdAndNoEmail() {
        SessionStore store = new SessionStore();
        Session admin = store.createAdminSession("admin-1", "Root");
        assertTrue(admin.isAdmin());
        assertEquals("admin-1", admin.adminId);
        assertNull(admin.email);
        assertEquals("ADMIN", admin.accountType);
    }

    @Test
    public void unknownEmptyAndNullTokensFindNothing() {
        SessionStore store = new SessionStore();
        store.createUserSession("a@yorku.ca", "Alice", "STUDENT", 20.0, 1L);
        assertNull(store.find("not-a-token"));
        assertNull(store.find(""));
        assertNull(store.find(null));
    }

    @Test
    public void aRemovedSessionIsGone() {
        SessionStore store = new SessionStore();
        Session session = store.createUserSession("a@yorku.ca", "Alice", "STUDENT", 20.0, 1L);
        store.remove(session.token);
        assertNull(store.find(session.token));
        store.remove(session.token);   // removing twice, or removing null, is harmless
        store.remove(null);
        assertEquals(0, store.size());
    }

    @Test
    public void tokensAreLongAndNeverRepeat() {
        SessionStore store = new SessionStore();
        Set<String> tokens = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            String token = store.createUserSession("a@yorku.ca", "Alice", "STUDENT", 20.0, 1L).token;
            assertEquals("32 random bytes in base64url", 43, token.length());
            assertTrue(token, token.matches("[A-Za-z0-9_-]+"));   // safe inside a cookie
            assertTrue("token repeated", tokens.add(token));
        }
    }

    @Test
    public void theStoreNeverGrowsPastItsLimit() {
        SessionStore store = new SessionStore();
        Session newest = null;
        for (int i = 0; i < 2500; i++) {
            newest = store.createUserSession("u" + i + "@yorku.ca", "User", "STUDENT", 20.0, 1L);
        }
        assertTrue("size " + store.size(), store.size() <= 2000);
        assertNotNull("the newest session is always kept", store.find(newest.token));
    }
}
