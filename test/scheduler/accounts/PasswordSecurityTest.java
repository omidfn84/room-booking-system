package scheduler.accounts;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import scheduler.persistence.UserRepository;
import scheduler.room.FakeRoomRepository;
import scheduler.room.RoomManager;

/**
 * Passwords are stored as salted PBKDF2 hashes, login compares hashes, and
 * generating administrators requires the chief password.
 */
public class PasswordSecurityTest {

    // ---------- PasswordHasher ----------

    @Test
    public void hashIsNotThePlainTextAndVerifies() {
        String hash = PasswordHasher.hash("Passw0rd!");
        assertFalse(hash.contains("Passw0rd!"));
        assertTrue(hash.startsWith("pbkdf2_sha512$210000$"));
        assertTrue(PasswordHasher.isHash(hash));
        assertTrue(PasswordHasher.verify("Passw0rd!", hash));
    }

    @Test
    public void wrongPasswordDoesNotVerify() {
        String hash = PasswordHasher.hash("Passw0rd!");
        assertFalse(PasswordHasher.verify("passw0rd!", hash));
        assertFalse(PasswordHasher.verify("", hash));
        assertFalse(PasswordHasher.verify(null, hash));
    }

    @Test
    public void samePasswordGetsADifferentSaltEachTime() {
        String a = PasswordHasher.hash("Passw0rd!");
        String b = PasswordHasher.hash("Passw0rd!");
        assertNotEquals(a, b);
        assertTrue(PasswordHasher.verify("Passw0rd!", a));
        assertTrue(PasswordHasher.verify("Passw0rd!", b));
    }

    @Test
    public void malformedOrPlainTextValuesNeverVerify() {
        assertFalse(PasswordHasher.isHash("Passw0rd!"));
        assertFalse(PasswordHasher.isHash(null));
        assertFalse(PasswordHasher.verify("Passw0rd!", "Passw0rd!"));
        assertFalse(PasswordHasher.verify("x", "pbkdf2_sha512$abc$!!!$!!!"));
        assertFalse(PasswordHasher.verify("x", "pbkdf2_sha512$0$AAAA$AAAA"));
    }

    // ---------- AccountManagement ----------

    @Test
    public void createAccountStoresAHashNotThePassword() {
        FakeUserRepository repo = new FakeUserRepository();
        AccountManagement am = new AccountManagement(repo);
        am.createAccount("alice@yorku.ca", "Passw0rd!", "STUDENT", "Alice", "123456789");

        String stored = repo.savedUsers.get(0).getPasswordHash();
        assertNotEquals("Passw0rd!", stored);
        assertTrue(PasswordHasher.isHash(stored));
    }

    @Test
    public void authenticateAcceptsOnlyTheRightPassword() {
        AccountManagement am = new AccountManagement(new FakeUserRepository());
        RegisteredUser alice = am.createAccount("alice@yorku.ca", "Passw0rd!", "STUDENT", "Alice", "123456789");

        assertSame(alice, am.authenticate("alice@yorku.ca", "Passw0rd!"));
        assertSame(alice, am.authenticate("  ALICE@yorku.ca ", "Passw0rd!"));
        assertNull(am.authenticate("alice@yorku.ca", "wrong"));
        assertNull(am.authenticate("alice@yorku.ca", null));
        assertNull(am.authenticate("nobody@yorku.ca", "Passw0rd!"));
        assertNull(am.authenticate(null, "Passw0rd!"));
    }

    @Test
    public void legacyPlainTextPasswordsAreHashedOnLoadAndStillWork() {
        List<RegisteredUser> legacy = new ArrayList<>();
        legacy.add(new Student("old@yorku.ca", "OldPassw0rd!", "STUDENT", "Old", 1L));
        List<List<RegisteredUser>> saves = new ArrayList<>();
        UserRepository repo = new UserRepository() {
            @Override public List<RegisteredUser> loadUsers() { return legacy; }
            @Override public void saveUsers(List<RegisteredUser> users) { saves.add(new ArrayList<>(users)); }
        };

        AccountManagement am = new AccountManagement(repo);

        assertEquals("the upgraded hashes are written back once", 1, saves.size());
        String stored = saves.get(0).get(0).getPasswordHash();
        assertTrue(PasswordHasher.isHash(stored));
        assertNotNull(am.authenticate("old@yorku.ca", "OldPassw0rd!"));
        assertNull(am.authenticate("old@yorku.ca", PasswordHasher.hash("OldPassw0rd!")));
    }

    @Test
    public void alreadyHashedUsersAreNotRewrittenOnLoad() {
        List<RegisteredUser> current = new ArrayList<>();
        current.add(new Student("new@yorku.ca", PasswordHasher.hash("Passw0rd!"), "STUDENT", "New", 1L));
        int[] saves = {0};
        UserRepository repo = new UserRepository() {
            @Override public List<RegisteredUser> loadUsers() { return current; }
            @Override public void saveUsers(List<RegisteredUser> users) { saves[0]++; }
        };

        new AccountManagement(repo);
        assertEquals(0, saves[0]);
    }

    // ---------- chief password ----------

    private final RoomManager roomManager = new RoomManager(new FakeRoomRepository());

    @Test
    public void generatingAnAdminNeedsTheChiefPassword() {
        ChiefEventCoordinator chief = ChiefEventCoordinator.getInstance();
        TestChief.password();
        try {
            chief.generateAdministratorAccount("wrong", "sec-admin-1", "Mallory", "m@yorku.ca", roomManager);
            fail("a wrong chief password must be refused");
        } catch (SecurityException expected) {
            assertEquals("Incorrect chief password.", expected.getMessage());
        }
        try {
            chief.generateAdministratorAccount(null, "sec-admin-1", "Mallory", "m@yorku.ca", roomManager);
            fail("a missing chief password must be refused");
        } catch (SecurityException expected) {
            // refused
        }
        assertNull(chief.findExistingAdministrator("sec-admin-1"));
        assertNotNull(chief.generateAdministratorAccount(TestChief.password(), "sec-admin-1", "Ada", "a@yorku.ca", roomManager));
    }

    @Test
    public void blankAdminIdIsRefused() {
        assertNull(ChiefEventCoordinator.getInstance()
                .generateAdministratorAccount(TestChief.password(), "  ", "Nobody", "n@yorku.ca", roomManager));
    }

    @Test(expected = IllegalStateException.class)
    public void chiefPasswordCannotBeReplacedOnceSet() {
        TestChief.password();
        ChiefEventCoordinator.getInstance().configureCredential("attacker-chosen");
    }

    @Test
    public void unconfiguredChiefRefusesToGenerateAdmins() throws Exception {
        // a fresh instance (bypassing the Singleton) to see the unconfigured state
        Constructor<ChiefEventCoordinator> ctor = ChiefEventCoordinator.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        ChiefEventCoordinator fresh = ctor.newInstance();

        assertFalse(fresh.isCredentialConfigured());
        try {
            fresh.generateAdministratorAccount("anything", "sec-admin-2", "Eve", "e@yorku.ca", roomManager);
            fail("an unconfigured chief must not generate admins");
        } catch (IllegalStateException expected) {
            // refused
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void blankChiefPasswordIsRejected() throws Exception {
        Constructor<ChiefEventCoordinator> ctor = ChiefEventCoordinator.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        ctor.newInstance().configureCredential("   ");
    }
}
