package scheduler.accounts;

/**
 * The chief is a JVM-wide Singleton whose password can only be set once, so
 * every test that generates administrators shares this one test password.
 * (test-ai's AIFixture.chiefPassword() uses the same value.)
 */
public final class TestChief {

    public static final String PASSWORD = "Chief-Test-Pass1!";

    private TestChief() {}

    /** Makes sure the chief is configured with the shared test password and returns it. */
    public static synchronized String password() {
        ChiefEventCoordinator chief = ChiefEventCoordinator.getInstance();
        if (!chief.isCredentialConfigured()) {
            chief.configureCredential(PASSWORD);
        }
        return PASSWORD;
    }
}
